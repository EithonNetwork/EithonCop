package net.eithon.plugin.cop.profanity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;

class Whitelist {
	private static Comparator<String> stringComparator;

	private EithonPlugin _eithonPlugin;
	private Blacklist _blacklist;
	private HashMap<String, Profanity> _whitelist;

	static void initialize() {
		stringComparator = new Comparator<String>(){
			public int compare(String f1, String f2)
			{
				return f1.compareTo(f2);
			}
		};
	}

	public Whitelist(EithonPlugin eithonPlugin, Blacklist blacklist)
	{
		this._eithonPlugin = eithonPlugin;
		this._blacklist = blacklist;
		this._whitelist = new HashMap<String, Profanity>();
	}

	public Profanity add(String word) {
		verbose("add", "Enter: %s", word);
		String normalized = Profanity.normalize(word);
		Profanity profanity = getProfanity(normalized);
		if (profanity != null) {
			verbose("add", "Already added: Leave %s", profanity.toString());
			return profanity;
		}
		profanity = this._blacklist.getProfanity(normalized);
		if (profanity == null) {
			verbose("add", "No corresponding profanity found: Leave");
			return null;
		}
		this._whitelist.put(normalized, profanity);
		verbose("add", "Added: Leave %s", profanity.toString());
		return profanity;
	}

	public String remove(String word) {
		verbose("remove", "Enter: %s", word);
		String normalized = Profanity.normalize(word);
		this._whitelist.remove(normalized);
		verbose("remove", "Removed: Leave %s", normalized);
		return normalized;
	}

	public boolean isWhitelisted(String word) { return getProfanity(word) != null; }

	Profanity getProfanity(String word) {
		return this._whitelist.get(Profanity.normalize(word));
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
		JSONArray whitelist = new JSONArray();
		List<String> array = sortStrings(this._whitelist.keySet());
		for (String word : array) {
			whitelist.add(word);
		}
		if ((whitelist == null) || (whitelist.size() == 0)) {
			this._eithonPlugin.getEithonLogger().info("No words saved in whitelist.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Saving %d words in whitelist", whitelist.size());
		File file = getWhitelistStorageFile();

		FileContent fileContent = new FileContent("Whitelist", 1, whitelist);
		fileContent.save(file);
	}

	private List<String> sortStrings(Collection<String> collection) {
		ArrayList<String> array = new ArrayList<String>(collection);
		array.sort(stringComparator);
		return array;
	}

	public void delayedLoad(double delaySeconds)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				load();
			}
		}, TimeMisc.secondsToTicks(delaySeconds));		
	}

	void load() {
		File file = getWhitelistStorageFile();
		FileContent fileContent = FileContent.loadFromFile(file);
		if (fileContent == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "File was empty.");
			return;			
		}
		JSONArray array = (JSONArray) fileContent.getPayload();
		if ((array == null) || (array.size() == 0)) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The whitelist was empty.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Restoring %d words from whitelist file.", array.size());
		this._whitelist = new HashMap<String, Profanity>();
		for (int i = 0; i < array.size(); i++) {
			String word = null;
			try {
				word = (String) array.get(i);
				add(word);
			} catch (Exception e) {
				this._eithonPlugin.getEithonLogger().error("Could not load word %d (exception).", i);
				if (word != null) this._eithonPlugin.getEithonLogger().error("Could not load word %s", word);
				this._eithonPlugin.getEithonLogger().error("%s", e.toString());
				throw e;
			}
		}
	}

	private File getWhitelistStorageFile() {
		File file = this._eithonPlugin.getDataFile("whitelist.json");
		return file;
	}

	public String[] getAllWords() {
		return this._whitelist.keySet().toArray(new String[0]);
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Whitelist.%s(): %s", method, message);
	}
}

