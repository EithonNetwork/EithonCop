package net.eithon.plugin.cop.profanity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.Config;
import net.eithon.plugin.cop.db.DbWhitelist;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

class Whitelist {
	private EithonPlugin _eithonPlugin;
	private Blacklist _blacklist;
	private HashMap<String, Profanity> _whitelist;

	static void initialize() {
		new Comparator<String>(){
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

	public Profanity create(String word) {
		Profanity profanity = add(word);
		if (profanity == null) return null;
		DbWhitelist.create(Config.V.database, word, profanity.getDbId());
		return profanity;
	}

	private Profanity add(String word) {
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
		DbWhitelist.deleteByWord(Config.V.database, normalized);
		verbose("remove", "Removed: Leave %s", normalized);
		return normalized;
	}

	public boolean isWhitelisted(String word) { return getProfanity(word) != null; }

	Profanity getProfanity(String word) {
		return this._whitelist.get(Profanity.normalize(word));
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
		this._whitelist = new HashMap<String, Profanity>();
		List<DbWhitelist> list = DbWhitelist.findAll(Config.V.database);
		this._eithonPlugin.logInfo("Reading %d whitelisted words from DB.", list.size());
		for (DbWhitelist item : list) {
			String word = item.getWord();
			try {
				add(word);
			} catch (Exception e) {
				if (word != null) this._eithonPlugin.logError("Could not load word %s", word);
				this._eithonPlugin.logError("%s", e.toString());
				throw e;
			}
		}
	}

	public String[] getAllWords() {
		return this._whitelist.keySet().toArray(new String[0]);
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("Whitelist", method, format, args);	
	}
}

