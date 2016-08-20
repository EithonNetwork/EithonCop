package net.eithon.plugin.cop.profanity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.mysql.Database;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.db.WhitelistRow;
import net.eithon.plugin.cop.db.WhitelistTable;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

class Whitelist {
	private static WhitelistTable whitelistTable;
	private EithonPlugin _eithonPlugin;
	private Blacklist _blacklist;
	private HashMap<String, Profanity> _whitelist;

	static void initialize(Database database) throws FatalException {
		whitelistTable = new WhitelistTable(database);
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

	public Profanity create(String word) throws FatalException, TryAgainException {
		Profanity profanity = add(word);
		if (profanity == null) return null;
		whitelistTable.create(word, profanity.getDbId());
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

	public String remove(String word) throws FatalException, TryAgainException {
		verbose("remove", "Enter: %s", word);
		String normalized = Profanity.normalize(word);
		this._whitelist.remove(normalized);
		whitelistTable.deleteByWord(normalized);
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
				try {
					load();
				} catch (FatalException | TryAgainException e) {
					e.printStackTrace();
				}
			}
		}, TimeMisc.secondsToTicks(delaySeconds));		
	}

	void load() throws FatalException, TryAgainException {
		this._whitelist = new HashMap<String, Profanity>();
		List<WhitelistRow> list = whitelistTable.findAll();
		this._eithonPlugin.logInfo("Reading %d whitelisted words from DB.", list.size());
		for (WhitelistRow item : list) {
			String word = item.word;
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

