package net.eithon.plugin.cop.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.file.FileMisc;
import net.eithon.library.json.FileContent;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.Config;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Blacklist {
	private EithonPlugin _eithonPlugin;
	private HashMap<String, Profanity> _metaphoneList;
	private HashMap<String, Profanity> _wordList;
	HashMap<String, Profanity> _similarWords;
	private static Metaphone3 metaphone3;

	static {
		metaphone3 = new Metaphone3();
		metaphone3.SetEncodeVowels(true);
		metaphone3.SetEncodeExact(true);
	}

	public Blacklist(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
		this._metaphoneList = new HashMap<String, Profanity>();
		this._wordList = new HashMap<String, Profanity>();
		this._similarWords = new HashMap<String, Profanity>();
	}

	public Profanity add(String word) {
		Profanity profanity = getProfanity(word);
		if (profanity != null) {
			this._eithonPlugin.getEithonLogger().warning("Blacklist.add: Trying to add a word that already exists: \"%s\".", word);
			return profanity;
		}
		profanity = new Profanity(word);
		add(profanity);
		return profanity;
	}

	private void add(Profanity profanity) {
		this._wordList.put(profanity.getWord(), profanity);
		this._metaphoneList.put(profanity.getPrimary(), profanity);
		if (profanity.hasSecondary()) this._metaphoneList.put(profanity.getSecondary(), profanity);
	}

	public boolean isBlacklisted(String word) {
		Profanity profanity = getProfanity(word);
		return (profanity != null) && (profanity.getProfanityLevel(word) <= Config.V.profanityLevel); 
	}

	public String replaceIfBlacklisted(String word) {
		Profanity profanity = getProfanity(word);
		if (profanity == null) return null;
		if (Config.V.saveSimilar 
				&& !profanity.isSameWord(word)
				&& !this._similarWords.containsKey(word)) {
			delayedSaveSimilar(word, profanity);
		}
		if (profanity.getProfanityLevel(word) > Config.V.profanityLevel) {
			if (Config.V.markSimilar && !profanity.isSameWord(word)) {
				return String.format("%s%s%s", Config.V.markSimilarPrefix, word, Config.V.markSimilarPostfix);
			}
			return null;
		}
		String synonym = profanity.getSynonym();
		if (Config.V.markReplacement) {
			return String.format("%s%s%s", Config.V.markReplacementPrefix, synonym, Config.V.markReplacementPostfix);
		}
		return synonym;
	}

	public Profanity getProfanity(String word) {
		String normalized = Profanity.normalize(word);
		Profanity profanity = this._wordList.get(normalized);
		if (profanity != null) return profanity;
		synchronized (metaphone3) {
			metaphone3.SetWord(normalized);
			metaphone3.Encode();
			String encoding = metaphone3.GetMetaph();
			profanity = this._metaphoneList.get(encoding);
			if (profanity == null) {
				encoding = metaphone3.GetAlternateMetaph();
				if (encoding.length() > 0) profanity = this._metaphoneList.get(encoding);
			}
		}
		return profanity;
	}

	private void delayedSaveSimilar(String similarWord, Profanity profanity) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				saveSimilar(similarWord, profanity, false);
			}
		});	
	}

	void delayedSaveSimilar(double waitSeconds, Whitelist whitelist) {
		final Blacklist thisObject = this;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				synchronized (thisObject._similarWords) {
					thisObject.getSimilarStorageFile().delete();
					thisObject.consolidateSimilar(whitelist);
					for (String similarWord : thisObject._similarWords.keySet()) {
						Profanity profanity = thisObject._similarWords.get(similarWord);
						saveSimilar(similarWord, profanity, true);
					}
				}
			}
		}, TimeMisc.secondsToTicks(waitSeconds));	
	}

	protected void consolidateSimilar(Whitelist whitelist) {
		for (Iterator<String> iterator = this._similarWords.keySet().iterator(); iterator.hasNext();) {
		    String word = iterator.next();
		    if (whitelist.isWhitelisted(word)) {
		    	this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Removed similar word \"%s\" as it was whitelisted", word);
		        iterator.remove();
		    } else {
		    	verbose("consolidateSimilar", "Keeping word \"%s\" as it was not whitelisted", word);
		    }
		}
	}

	void saveSimilar(String similarWord, Profanity profanity, boolean force) {
		synchronized (this._similarWords) {
			if (!force && this._similarWords.containsKey(similarWord)) return;
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Added similar %s: %s", similarWord, profanity.toString());
			this._similarWords.put(similarWord, profanity);
			File file = getSimilarStorageFile();
			String line = String.format("%s ~ %s", similarWord, profanity.toString());
			try {
				FileMisc.appendLine(file, line);
			} catch (IOException e) {
				this._eithonPlugin.getEithonLogger().error("Could not write to file %s: %s", file.getName(), e.getMessage());
			}
		}
	}

	public void delayedLoadSimilar(double waitSeconds) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				loadSimilar();
			}
		}, TimeMisc.secondsToTicks(waitSeconds));	
	}

	void loadSimilar() {
		synchronized (this._similarWords) {
			File file = getSimilarStorageFile();
			if (!file.exists()) return;
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = br.readLine()) != null) {
					int pos = line.indexOf(" ~ ");
					if (pos < 0) continue;
					String similarWord = line.substring(0, pos);
					String rest = line.substring(pos+3);
					pos = rest.indexOf(" (");
					if (pos < 0) continue;
					String word = rest.substring(0, pos);
					Profanity profanity = getProfanity(word);
					if (profanity == null) continue;
					if (!profanity.isSameWord(word)) continue;
					this._similarWords.put(similarWord, profanity);
				}
			} catch (FileNotFoundException e) {
				this._eithonPlugin.getEithonLogger().error("(1) Could not read from file %s: %s", file.getName(), e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				this._eithonPlugin.getEithonLogger().error("(2) Could not read from file %s: %s", file.getName(), e.getMessage());
				e.printStackTrace();
			}
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
		for (Profanity profanity : this._metaphoneList.values()) {
			blacklist.add(profanity.toJson());
		}
		if ((blacklist == null) || (blacklist.size() == 0)) {
			this._eithonPlugin.getEithonLogger().info("No profanities saved in blacklist.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Saving %d profanities in blacklist", blacklist.size());
		File file = getBlacklistStorageFile();

		FileContent fileContent = new FileContent("Blacklist", 1, blacklist);
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
		this._metaphoneList = new HashMap<String, Profanity>();
		for (int i = 0; i < array.size(); i++) {
			Profanity profanity = null;
			try {
				profanity = Profanity.getFromJson((JSONObject) array.get(i));
				if (profanity == null) {
					this._eithonPlugin.getEithonLogger().error("Could not load profanity %d (result was null).", i);
					continue;
				}
				this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Loaded profanity %s", profanity.toString());
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

	File getSimilarStorageFile() {
		File file = this._eithonPlugin.getDataFile("similar.txt");
		return file;
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Blacklist.%s(): %s", method, message);
	}
}