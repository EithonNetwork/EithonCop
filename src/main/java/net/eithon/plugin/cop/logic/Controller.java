package net.eithon.plugin.cop.logic;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.Config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Controller {

	private Blacklist _blacklist;
	private Whitelist _whitelist;

	public Controller(EithonPlugin eithonPlugin){
		this._blacklist = new Blacklist(eithonPlugin);
		this._blacklist.delayedLoad();
		this._whitelist = new Whitelist(eithonPlugin);
		this._whitelist.delayedLoad();
	}

	public String addProfanity(CommandSender sender, String word) {
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

	public String addAccepted(CommandSender sender, String word) {
		Profanity profanity = this._blacklist.getProfanity(word);
		if (profanity != null) {
			if (word.equalsIgnoreCase(profanity.getWord())) {
				Config.M.acceptedWordWasBlacklisted.sendMessage(sender, word);
				return null;
			}
			this._whitelist.add(word);
			this._whitelist.delayedSave();
			return profanity.getWord();
		}
		Config.M.notBlacklisted.sendMessage(sender, word);
		return null;
	}

	public String profanityFilter(Player player, String message) {
		char[] inCharArray = message.toCharArray();
		String transformedInMessage = Leet.decode(message.toLowerCase());
		transformedInMessage = transformedInMessage.replaceAll("\\s-", " ");
		char[] transformedCharArray = transformedInMessage.toCharArray();
		StringBuilder inWord = new StringBuilder("");
		StringBuilder transformedWord = new StringBuilder("");
		String outWord;
		StringBuilder outMessage = new StringBuilder("");
		for (int pos = 0; pos < transformedCharArray.length; pos++) {
			char inChar = inCharArray[pos];
			char transformedChar = transformedCharArray[pos];
			if (transformedChar != ' ') {
				inWord.append(inChar);
				transformedWord.append(transformedChar);
				continue;
			}
			// We have a word
			if (transformedWord.length() > 0) {
				outWord = replace(transformedWord.toString(), inWord.toString());
				outMessage.append(outWord);
				outMessage.append(inChar);
				inWord = new StringBuilder();
				transformedWord = new StringBuilder("");
			}
		}
		// We have a word
		if (transformedWord.length() > 0) {
			outWord = replace(transformedWord.toString(), inWord.toString());
			outMessage.append(outWord);
		}

		return outMessage.toString();
	}

	private String replace(String transformedWord, String inWord) {
		if (this._whitelist.isWhitelisted(transformedWord)) return inWord;
		String outWord = this._blacklist.replaceIfBlacklisted(transformedWord);
		if (outWord == null) return inWord;
		String result = casifyAsReferenceWord(outWord, inWord);
		if (Leet.isLeet(inWord)) return Leet.encode(result);
		return result;
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
					result.append(outWord.substring(0, 0).toUpperCase());
					result.append(outWord.substring(1).toLowerCase());
					return result.toString();
				}
			}
		}
		return outWord.toLowerCase();
	}
}