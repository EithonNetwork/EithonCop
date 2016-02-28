package net.eithon.plugin.cop.profanity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.file.FileMisc;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.CoolDown;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.Config;
import net.eithon.plugin.cop.db.DbBlacklist;
import net.eithon.plugin.cop.db.DbSimilar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

class Blacklist {
	private EithonPlugin _eithonPlugin;
	private HashMap<String, Profanity> _metaphoneList;
	private HashMap<String, Profanity> _wordList;
	private HashMap<String, Profanity> _similarWords;
	private CoolDown _recentOffenders;
	private CoolDown _offenders;

	private static Metaphone3 metaphone3;

	static void initialize() {
		metaphone3 = new Metaphone3();
		metaphone3.SetEncodeVowels(true);
		metaphone3.SetEncodeExact(true);
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

	Profanity add(String word, boolean isLiteral) {
		Profanity profanity = Profanity.create(word, isLiteral);
		add(profanity);
		return profanity;
	}

	Profanity remove(String word) {
		Profanity profanity = getProfanity(word);
		if ((profanity == null) || !profanity.isSameWord(word)) {
			this._eithonPlugin.getEithonLogger().warning("Blacklist.add: Trying to remove a word that isn't blacklisted: \"%s\".", word);
			return null;
		}
		this._wordList.remove(word);
		profanity.deleteFromDb();
		if (profanity.isLiteral()) return profanity;
		removeMetaphone(profanity, profanity.getPrimary());
		removeMetaphone(profanity, profanity.getSecondary());
		return profanity;
	}

	void removeMetaphone(Profanity profanity, String metaphone) {
		Profanity found;
		found = this._metaphoneList.get(metaphone);
		if (found != null) {
			if (found.equals(profanity)) this._metaphoneList.remove(metaphone);
		}
	}

	private void add(Profanity profanity) {
		if (this._wordList.containsKey(profanity.getWord())) return;
		this._wordList.put(profanity.getWord(), profanity);
		if (profanity.isLiteral()) return;
		if (!this._metaphoneList.containsKey(profanity.getPrimary())) {
			this._metaphoneList.put(profanity.getPrimary(), profanity);
		}
		if (profanity.hasSecondary() && !this._metaphoneList.containsKey(profanity.getSecondary())) {
			this._metaphoneList.put(profanity.getSecondary(), profanity);
		}
	}

	boolean isBlacklisted(String word) {
		Profanity profanity = getProfanity(word);
		return (profanity != null) && (profanity.getProfanityLevel(word) <= Config.V.profanityLevel); 
	}

	String replaceIfBlacklisted(Player player, String normalized, String originalWord) {
		Profanity profanity = getProfanity(normalized);
		if (profanity == null) return replaceIfBuildingStone(player, normalized, originalWord);
		int profanityLevel = profanity.getProfanityLevel(normalized);
		boolean isConsideredForbidden = profanityLevel <= Config.V.profanityLevel;
		if (Config.V.saveSimilar 
				&& (profanityLevel == Profanity.PROFANITY_LEVEL_SIMILAR)
				&& !this._similarWords.containsKey(normalized)) {
			delayedSaveSimilar(normalized, profanity);
		}
		notifySomePlayers(player, normalized, originalWord, profanity.getWord(), profanityLevel, isConsideredForbidden);
		if (!isConsideredForbidden) {
			if (Config.V.markSimilar && (profanityLevel == Profanity.PROFANITY_LEVEL_SIMILAR)) return markSimilar(originalWord);
			return null;
		}
		String synonym = "****";
		if (Config.V.markReplacement) return markReplacement(synonym);
		return synonym;
	}

	private void delayedSaveSimilar(String similarWord, Profanity profanity) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				saveSimilar(similarWord, profanity, false);
			}
		});	
	}

	void saveSimilar(String similarWord, Profanity profanity, boolean force) {
		synchronized (this._similarWords) {
			if (!force && this._similarWords.containsKey(similarWord)) return;
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Added similar %s: %s", similarWord, profanity.toString());
			this._similarWords.put(similarWord, profanity);
			DbSimilar.create(Config.V.database, similarWord, profanity.getDbId(), false);
		}
	}

	private String markSimilar(String originalWord) {
		return String.format("%s%s%s", Config.V.markSimilarPrefix, originalWord, Config.V.markSimilarPostfix);
	}

	private String markReplacement(String synonym) {
		return String.format("%s%s%s", Config.V.markReplacementPrefix, synonym, Config.V.markReplacementPostfix);
	}

	private String replaceIfBuildingStone(Player player, String normalized,
			String originalWord) {
		for (String buildingBlock : Config.V.profanityBuildingBlocks) {
			if (normalized.contains(buildingBlock)) {
				notifySomePlayers(player, normalized, originalWord, buildingBlock, 
						Profanity.PROFANITY_LEVEL_COMPOSED, Profanity.PROFANITY_LEVEL_COMPOSED <= Config.V.profanityLevel);
				return markReplacement("****");
			}
		}
		return null;
	}

	private void notifySomePlayers(Player player, String normalized, String originalWord, String referenceWord,
			int profanityLevel, boolean isConsideredForbidden) {
		noteAsOffender(player, isConsideredForbidden);
		switch (profanityLevel) {
		case Profanity.PROFANITY_LEVEL_LITERAL:
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("eithoncop.notify-about-profanity")) {
					Config.M.notifyAboutProfanity.sendMessage(p, player == null ? "-" : player.getName(), normalized, originalWord);
				}
			}	
			break;
		case Profanity.PROFANITY_LEVEL_COMPOSED:
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("eithoncop.notify-about-composed")) {
					Config.M.notifyAboutComposed.sendMessage(p, player == null ? "-" : player.getName(), normalized, originalWord, referenceWord);
				}
			}	
			break;
		case Profanity.PROFANITY_LEVEL_SIMILAR:
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("eithoncop.notify-about-similar")) {
					Config.M.notifyAboutSimilar.sendMessage(p, player == null ? "-" : player.getName(), normalized, originalWord, referenceWord);
				}
			}
			break;
		default:
			break;
		}
	}

	private void noteAsOffender(Player player, boolean isConsideredForbidden) {
		if (player == null) return;
		if (!this._offenders.isInCoolDownPeriod(player) 
				&& !this._recentOffenders.isInCoolDownPeriod(player)
				&& !isConsideredForbidden) return;
		if (isConsideredForbidden)  {
			minor("Player %s is an offender.", player.getName());
			this._offenders.addIncident(player);
		}
		minor("Player %s is a recent offender.", player.getName());
		this._recentOffenders.addIncident(player);
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
			if (profanity == null) {
				encoding = metaphone3.GetAlternateMetaph();
				if (encoding.length() > 0) profanity = this._metaphoneList.get(encoding);
			}
		}
		if ((profanity != null) && profanity.isLiteral()) return null;
		return profanity;
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
		this._similarWords = new HashMap<String, Profanity>();
		List<DbSimilar> list = DbSimilar.findAll(Config.V.database);
		for (DbSimilar dbSimilar : list) {
			String similarWord = dbSimilar.getWord();
			Profanity profanity = getProfanity(similarWord);
			if (profanity == null) continue;
			this._similarWords.put(similarWord, profanity);
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
		this._wordList = new HashMap<String, Profanity>();
		List<DbBlacklist> list = DbBlacklist.findAll(Config.V.database);
		this._eithonPlugin.getEithonLogger().info("Reading %d profanities from blacklist DB.", list.size());
		this._metaphoneList = new HashMap<String, Profanity>();
		for (DbBlacklist dbBlacklist : list) {
			Profanity profanity = null;
			try {
				profanity = Profanity.getFromRecord(dbBlacklist);
				if (profanity == null) continue;
				this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Loaded profanity %s", profanity.toString());
				this.add(profanity);
			} catch (Exception e) {
				if (profanity != null) this._eithonPlugin.getEithonLogger().error("Could not load profanity %s", profanity.getWord());
				this._eithonPlugin.getEithonLogger().error("%s", e.toString());
				throw e;
			}
		}
	}

	private File getOffenderLogFile() {
		File file = this._eithonPlugin.getDataFile("offender.log");
		return file;
	}

	private void minor(String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Blacklist: %s", message);
	}

	public String[] getAllWords() {
		return this._wordList.keySet().toArray(new String[0]);
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Blacklist.%s(): %s", method, message);
	}
}