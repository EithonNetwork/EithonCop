package net.eithon.plugin.cop.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.file.FileMisc;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.Config;
import net.eithon.plugin.cop.profanity.Blacklist;
import net.eithon.plugin.cop.profanity.Leet;
import net.eithon.plugin.cop.profanity.Profanity;
import net.eithon.plugin.cop.profanity.Whitelist;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class Controller {
	static int profanityWordMinimumLength = 3;
	private EithonPlugin _eithonPlugin;
	private Blacklist _blacklist;
	private Whitelist _whitelist;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._blacklist = new Blacklist(eithonPlugin);
		this._blacklist.delayedLoad();
		this._whitelist = new Whitelist(eithonPlugin, this._blacklist);
		this._whitelist.delayedLoad(1);
		if (Config.V.saveSimilar) {
			this._blacklist.delayedLoadSimilar(2);
		}
		delayedLoadSeed(4);
	}

	public void disable() {
		this._whitelist.save();
		this._blacklist.save();
		this._blacklist.saveSimilar(this._whitelist);
	}

	private void delayedLoadSeed(double delaySeconds)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				loadSeed();
			}
		}, TimeMisc.secondsToTicks(delaySeconds));		
	}

	void loadSeed() {
		File fileIn = getSeedInStorageFile();
		if (!fileIn.exists()) return;
		File fileOut = getSeedOutStorageFile();
		try (BufferedReader br = new BufferedReader(new FileReader(fileIn))) {
			String line;
			while ((line = br.readLine()) != null) {
				String filtered = profanityFilter(null, line);
				FileMisc.appendLine(fileOut, line);
				FileMisc.appendLine(fileOut, filtered);
				FileMisc.appendLine(fileOut, "");
			}
		} catch (FileNotFoundException e) {
			this._eithonPlugin.getEithonLogger().error("(1) Could not read from file %s: %s", fileIn.getName(), e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			this._eithonPlugin.getEithonLogger().error("(2) Could not read from file %s: %s", fileIn.getName(), e.getMessage());
			e.printStackTrace();
		}
	}

	private File getSeedInStorageFile() {
		File file = this._eithonPlugin.getDataFile("seedin.txt");
		return file;
	}

	private File getSeedOutStorageFile() {
		File file = this._eithonPlugin.getDataFile("seedout.txt");
		return file;
	}

	public String addProfanity(CommandSender sender, String word) {
		if (word.length() < profanityWordMinimumLength) {
			Config.M.blackListWordMinimalLength.sendMessage(sender, profanityWordMinimumLength);
			return null;
		}
		Profanity profanity = this._blacklist.getProfanity(word);
		if (profanity == null) {
			profanity = this._blacklist.add(word);
			this._blacklist.delayedSave();
			return profanity.getWord();
		}
		if (word.equalsIgnoreCase(profanity.getWord())) {
			Config.M.duplicateProfanity.sendMessage(sender, word);
		} else {
			Config.M.probablyDuplicateProfanity.sendMessage(sender, word, profanity.getWord());
		}
		return null;
	}

	public String normalize(String word) {
		return Profanity.normalize(word);
	}

	public String addAccepted(CommandSender sender, String word) {
		if (word.length() < profanityWordMinimumLength) {
			Config.M.whitelistWordMinimalLength.sendMessage(sender, profanityWordMinimumLength);
			return null;
		}
		String normalized = Profanity.normalize(word);
		if (this._whitelist.isWhitelisted(normalized)) {
			Config.M.duplicateAcceptedWord.sendMessage(sender, word);
			return null;
		}
		Profanity profanity = this._blacklist.getProfanity(normalized);
		if (profanity != null) {
			if (normalized.equalsIgnoreCase(profanity.getWord())) {
				Config.M.acceptedWordWasBlacklisted.sendMessage(sender, word);
				return null;
			}
			this._whitelist.add(word);
			this._whitelist.delayedSave();
			return profanity.getWord();
		}
		Config.M.acceptedWordWasNotBlacklisted.sendMessage(sender, word);
		return null;
	}

	public String profanityFilter(Player player, String message) {
		verbose("Controller.profanityFilter", "Enter = \"%s\"", message);
		String transformedInMessage = Profanity.normalize(message);
		transformedInMessage = transformedInMessage.replaceAll("\\W", " ");
		StringBuilder inWord = new StringBuilder("");
		StringBuilder transformedWord = new StringBuilder("");
		String outWord;
		StringBuilder outMessage = new StringBuilder("");		
		StringTokenizer st = new StringTokenizer(transformedInMessage, " ", true);
		int pos = 0;
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			verbose("Controller.profanityFilter", "token = \"%s\"", token);
			int tokenLength = token.length();
			if (token.equalsIgnoreCase(" ")) {
				verbose("Controller.profanityFilter", "space");
				inWord.append(message.charAt(pos));
				verbose("Controller.profanityFilter", "inWord = \"%s\"", inWord.toString());
				if (transformedWord.length() == 0) {
					outMessage.append(inWord);
					verbose("Controller.profanityFilter", "outMessage = \"%s\"", outMessage.toString());
					inWord = new StringBuilder();
				}
			} else {
				if (tokenLength < profanityWordMinimumLength) {
					inWord.append(message.substring(pos, pos+tokenLength));
					verbose("Controller.profanityFilter", "inWord2 = \"%s\"", inWord.toString());
					transformedWord.append(token);
					verbose("Controller.profanityFilter", "transformedWord = \"%s\"", transformedWord.toString());
					outWord = replaceWithSynonym(player, transformedWord.toString(), true);
					verbose("Controller.profanityFilter", "outWord = \"%s\"", outWord);
					if (outWord != null) {
						outWord = replace(player, transformedWord.toString(), inWord.toString());
						verbose("Controller.profanityFilter", "outWord2 = \"%s\"", outWord);
						outMessage.append(outWord);
						verbose("Controller.profanityFilter", "outMessage2 = \"%s\"", outMessage.toString());
						inWord = new StringBuilder();
						transformedWord = new StringBuilder("");
					}
				} else {
					if (transformedWord.length() > 0) {
						outWord = replace(player, transformedWord.toString(), inWord.toString());
						verbose("Controller.profanityFilter", "outWord3 = \"%s\"", outWord);
						outMessage.append(outWord);
						verbose("Controller.profanityFilter", "outMessage3 = \"%s\"", outMessage.toString());
						inWord = new StringBuilder();
						transformedWord = new StringBuilder("");
					}
					inWord.append(message.substring(pos, pos+tokenLength));
					outWord = replace(player, token, inWord.toString());
					verbose("Controller.profanityFilter", "outWord4 = \"%s\"", outWord);
					outMessage.append(outWord);
					verbose("Controller.profanityFilter", "outMessage4 = \"%s\"", outMessage.toString());
					inWord = new StringBuilder();
				}
			}
			pos += tokenLength;
		}

		if (transformedWord.length() > 0) {
			outWord = replace(player, transformedWord.toString(), inWord.toString());
			verbose("Controller.profanityFilter", "outWord5 = \"%s\"", outWord);
			outMessage.append(outWord);
			verbose("Controller.profanityFilter", "outMessage5 = \"%s\"", outMessage.toString());
		}

		return outMessage.toString();
	}

	private String replace(CommandSender sender, String transformedWord, String inWord) {
		String outWord = replaceWithSynonym(sender, transformedWord, true);
		if (outWord == null) return inWord;
		String result = casifyAsReferenceWord(outWord, inWord);
		if (Leet.isLeet(inWord)) return Leet.encode(result);
		return result;
	}

	private String replaceWithSynonym(CommandSender sender, String transformedWord, boolean checkPlural) {
		String outWord = replaceWithSynonym(sender, transformedWord);
		if ((outWord != null) || !checkPlural) return outWord;
		String withoutPlural = withoutPlural(transformedWord);
		if (transformedWord.equalsIgnoreCase(withoutPlural)) return null;
		return replaceWithSynonym(sender, withoutPlural);
	}

	private String replaceWithSynonym(CommandSender sender, String transformedWord) {
		verbose("Controller.replaceWithSynonym", "Enter = \"%s\"", transformedWord);
		if (transformedWord.length() < profanityWordMinimumLength) {
			verbose("Controller.replaceWithSynonym", "Too short. Leave null");
			return null;
		}
		if (this._whitelist.isWhitelisted(transformedWord)) {
			verbose("Controller.replaceWithSynonym", "Whitelisted. Leave null");
			return null;
		}
		String result = this._blacklist.replaceIfBlacklisted(sender, transformedWord);
		verbose("Controller.replaceWithSynonym", "Leave = \"%s\"", result);
		return result;
	}

	private String withoutPlural(String transformedWord) {
		if (transformedWord.endsWith("es")) return transformedWord.substring(0, transformedWord.length()-2);
		if (transformedWord.endsWith("s")) return transformedWord.substring(0, transformedWord.length()-1);
		return transformedWord;
	}

	private String casifyAsReferenceWord(String outWord, String referenceWord) {
		char[] charArray = referenceWord.toCharArray();
		char c = charArray[0];
		boolean firstCharacterIsUpperCase = Character.isAlphabetic(c) && Character.isUpperCase(c);
		if (!firstCharacterIsUpperCase) return outWord.toLowerCase();
		for (int i = 1; i < charArray.length; i++) {
			c = charArray[i];
			if (Character.isAlphabetic(c)) {
				if (Character.isUpperCase(c)) return outWord.toUpperCase();
				else {
					StringBuilder result = new StringBuilder();
					result.append(outWord.substring(0, 1).toUpperCase());
					result.append(outWord.substring(1).toLowerCase());
					return result.toString();
				}
			}
		}
		return outWord.toLowerCase();
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}