package net.eithon.plugin.cop.profanity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.file.FileMisc;
import net.eithon.library.json.FileContent;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.CoolDown;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.Config;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class Blacklist {
	private EithonPlugin _eithonPlugin;
	private HashMap<String, Profanity> _metaphoneList;
	private HashMap<String, Profanity> _wordList;
	private HashMap<String, Profanity> _similarWords;
	private CoolDown _recentOffenders;
	private CoolDown _offenders;

	private static Metaphone3 metaphone3;
	private static Comparator<String> stringComparator;

	static {
		metaphone3 = new Metaphone3();
		metaphone3.SetEncodeVowels(true);
		metaphone3.SetEncodeExact(true);

		stringComparator = new Comparator<String>(){
			public int compare(String f1, String f2)
			{
				return f1.compareTo(f2);
			}
		};
	}

	Blacklist(EithonPlugin eithonPlugin)
	{
		this._eithonPlugin = eithonPlugin;
		this._metaphoneList = new HashMap<String, Profanity>();
		this._wordList = new HashMap<String, Profanity>();
		this._similarWords = new HashMap<String, Profanity>();
		this._recentOffenders = new CoolDown("BlacklistRecentOffenders", Config.V.profanityRecentOffenderCooldownInSeconds);
		this._offenders = new CoolDown("BlacklistNotedOffenders", Config.V.profanityOffenderCooldownInSeconds);
	}

	Profanity add(String word) {
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
		if (this._wordList.containsKey(profanity.getWord())) return;
		this._wordList.put(profanity.getWord(), profanity);
		if (profanity.isLiteral()) return;
		this._metaphoneList.put(profanity.getPrimary(), profanity);
		if (profanity.hasSecondary()) this._metaphoneList.put(profanity.getSecondary(), profanity);
	}

	boolean isBlacklisted(String word) {
		Profanity profanity = getProfanity(word);
		return (profanity != null) && (profanity.getProfanityLevel(word) <= Config.V.profanityLevel); 
	}

	String replaceIfBlacklisted(Player player, String normalized, String originalWord) {
		Profanity profanity = getProfanity(normalized);
		verbose("Blacklist.replaceIfBlacklisted", "word=%s, profanity = %s", normalized, profanity);
		if (profanity == null) return null;
		if (Config.V.saveSimilar 
				&& !profanity.isSameWord(normalized)
				&& !this._similarWords.containsKey(normalized)) {
			delayedSaveSimilar(normalized, profanity);
		}
		boolean isConsideredForbidden = profanity.getProfanityLevel(normalized) <= Config.V.profanityLevel;
		notifySomePlayers(player, normalized, originalWord, profanity, isConsideredForbidden);
		if (!isConsideredForbidden) {
			verbose("Blacklist.replaceIfBlacklisted", "Leave, because profanity.getProfanityLevel(%s)=%d > %d", 
					normalized, profanity.getProfanityLevel(normalized), Config.V.profanityLevel);
			return null;
		}
		String synonym = profanity.getSynonym();
		if (Config.V.markReplacement) {
			return String.format("%s%s%s", Config.V.markReplacementPrefix, synonym, Config.V.markReplacementPostfix);
		}
		return synonym;
	}

	private void notifySomePlayers(Player player, String normalized, String originalWord,
			Profanity profanity, boolean isConsideredForbidden) {
		if (profanity.isSameWord(normalized)) {
			noteAsOffender(player, true, isConsideredForbidden);
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("eithoncop.notify-about-profanity")) {
					Config.M.notifyAboutProfanity.sendMessage(p, player == null ? "-" : player.getName(), normalized, originalWord);
				}
			}				
		} else {
			noteAsOffender(player, false, isConsideredForbidden);
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("eithoncop.notify-about-similar")) {
					Config.M.notifyAboutSimilar.sendMessage(p, player == null ? "-" : player.getName(), normalized, originalWord, profanity.getWord());
				}
			}
		}
	}

	private void noteAsOffender(Player player, boolean literal, boolean isConsideredForbidden) {
		if (player == null) return;
		if (!this._offenders.isInCoolDownPeriod(player) 
				&& !this._recentOffenders.isInCoolDownPeriod(player)
				&& !isConsideredForbidden) return;
		if (isConsideredForbidden)  {
			minor("Player %s is an offender.", player.getName());
			this._offenders.addPlayer(player);
		}
		minor("Player %s is a recent offender.", player.getName());
		this._recentOffenders.addPlayer(player);
	}

	boolean isOffender(Player player) { return player == null ? false : this._recentOffenders.isInCoolDownPeriod(player); };

	Profanity getProfanity(String word) {
		String normalized = Profanity.normalize(word);
		Profanity profanity = this._wordList.get(normalized);
		if (profanity != null) return profanity;
		synchronized (metaphone3) {
			metaphone3.SetWord(normalized);
			metaphone3.Encode();
			String encoding = metaphone3.GetMetaph();
			profanity = this._metaphoneList.get(encoding);
			if ((profanity == null) || profanity.isLiteral()) {
				encoding = metaphone3.GetAlternateMetaph();
				if (encoding.length() > 0) profanity = this._metaphoneList.get(encoding);
			}
		}
		if ((profanity != null) && profanity.isLiteral()) return null;
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
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				saveSimilar(whitelist);
			}
		}, TimeMisc.secondsToTicks(waitSeconds));	
	}

	void saveSimilar(Whitelist whitelist) {
		synchronized (this._similarWords) {
			getSimilarStorageFile().delete();
			consolidateSimilar(whitelist);
			for (String similarWord : sortStrings(this._similarWords.keySet())) {
				Profanity profanity = this._similarWords.get(similarWord);
				saveSimilar(similarWord, profanity, true);
			}
		}
	}

	private List<String> sortStrings(Collection<String> collection) {
		ArrayList<String> array = new ArrayList<String>(collection);
		array.sort(stringComparator);
		return array;
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

	void delayedLoadSimilar(double waitSeconds) {
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

	void delayedSaveOffenderMessage(Player player, String message,
			String filteredMessage) {
		if (!Config.V.logOffenderMessages) return;
		if (!isOffender(player)) return;
		
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				saveOffenderMessage(player, message, filteredMessage);
			}
		});
	}

	void saveOffenderMessage(Player player, String message, String filteredMessage) {
		verbose("saveOffenderMessage", "Log message:  %s", message);
		File file = getOffenderLogFile();
		String line = String.format("%s (%s)\n%s\n%s\n", player.getName(), player.getUniqueId().toString(), filteredMessage, message);
		try {
			FileMisc.appendLine(file, line);
		} catch (IOException e) {
			this._eithonPlugin.getEithonLogger().error("Could not write to file %s: %s", file.getName(), e.getMessage());
		}
	}

	void delayedSave()
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
		for (Profanity profanity : Profanity.sortByWord(this._wordList.values())) {
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

	void delayedLoad()
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

	private File getSimilarStorageFile() {
		File file = this._eithonPlugin.getDataFile("similar.txt");
		return file;
	}

	private File getOffenderLogFile() {
		File file = this._eithonPlugin.getDataFile("offender.log");
		return file;
	}

	private void minor(String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Blacklist: %s", message);
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Blacklist.%s(): %s", method, message);
	}
}