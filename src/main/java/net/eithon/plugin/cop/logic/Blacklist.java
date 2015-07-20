package net.eithon.plugin.cop.logic;

import java.io.File;
import java.util.HashMap;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.plugin.Logger.DebugPrintLevel;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Blacklist {
	private EithonPlugin _eithonPlugin;
	private HashMap<String, Profanity> _hashMap;
	private static Metaphone3 metaphone3;

	static {
		metaphone3 = new Metaphone3();
		metaphone3.SetEncodeVowels(true);
		metaphone3.SetEncodeExact(true);
	}

	public Blacklist(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
		this._hashMap = new HashMap<String, Profanity>();
		delayedLoad();
	}
	
	public Profanity add(String word) {
		Profanity profanity = new Profanity(word);
		add(profanity);
		return profanity;
	}

	private void add(Profanity word) {
		this._hashMap.put(word.getPrimary(), word);
		if (word.hasSecondary()) this._hashMap.put(word.getSecondary(), word);
	}

	public boolean isBlacklisted(String word) { return getProfanity(word) != null; }

	public String replaceIfBlacklisted(String word) {
		Profanity forbiddenWord = getProfanity(word);
		if (forbiddenWord == null) return null;
		return forbiddenWord.getSynonym();
	}

	public Profanity getProfanity(String word) {
		synchronized (metaphone3) {
			metaphone3.SetWord(word.toLowerCase());
			metaphone3.Encode();
			String encoding = metaphone3.GetMetaph();
			Profanity forbiddenWord = this._hashMap.get(encoding);
			if (forbiddenWord != null) return forbiddenWord;
			encoding = metaphone3.GetAlternateMetaph();
			if (encoding.length() == 0) return null;
			return this._hashMap.get(encoding);
		}
	}

	public void delayedSave()
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				save();
			}
		});		
	}

	@SuppressWarnings("unchecked")
	public
	void save() {
		JSONArray blacklist = new JSONArray();
		for (Profanity profanity : this._hashMap.values()) {
			blacklist.add(profanity.toJson());
		}
		if ((blacklist == null) || (blacklist.size() == 0)) {
			this._eithonPlugin.getEithonLogger().info("No profanities saved in blacklist.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Saving %d profanities in blacklist", blacklist.size());
		File file = getBlacklistStorageFile();

		FileContent fileContent = new FileContent("TravelPad", 1, blacklist);
		fileContent.save(file);
	}

	public void delayedLoad()
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				load();
			}
		});		
	}

	void load() {
		File file = getBlacklistStorageFile();
		FileContent fileContent = FileContent.loadFromFile(file);
		if (fileContent == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "File was empty.");
			return;			
		}
		JSONArray array = (JSONArray) fileContent.getPayload();
		if ((array == null) || (array.size() == 0)) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The blacklist of profanities was empty.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Restoring %d profanities from blacklist file.", array.size());
		this._hashMap = new HashMap<String, Profanity>();
		for (int i = 0; i < array.size(); i++) {
			Profanity profanity = null;
			try {
				profanity = Profanity.getFromJson((JSONObject) array.get(i));
				if (profanity == null) {
					this._eithonPlugin.getEithonLogger().error("Could not load profanity %d (result was null).", i);
					continue;
				}
				this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Loaded profanity %s", profanity.getWord());
				this.add(profanity);
			} catch (Exception e) {
				this._eithonPlugin.getEithonLogger().error("Could not load profanity %d (exception).", i);
				if (profanity != null) this._eithonPlugin.getEithonLogger().error("Could not load profanity %s", profanity.getWord());
				this._eithonPlugin.getEithonLogger().error("%s", e.toString());
				throw e;
			}
		}
	}

	private File getBlacklistStorageFile() {
		File file = this._eithonPlugin.getDataFile("blacklist.json");
		return file;
	}
}