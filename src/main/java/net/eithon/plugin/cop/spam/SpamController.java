package net.eithon.plugin.cop.spam;

import java.util.StringTokenizer;

import org.bukkit.entity.Player;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.Config;

public class SpamController {
	private RepeatedLines _repeatedLines;

	public SpamController(EithonPlugin eithonPlugin) {
		this._repeatedLines = new RepeatedLines();
	}

	public String reduceUpperCaseUsage(Player player, String message) {
		if (hasTooManyUpperCaseLetters(message)
				|| hasTooManyUpperCaseWords(message)) {
			return message.toLowerCase();
		}
		return message;
	}

	private boolean hasTooManyUpperCaseWords(String message) {
		int upperCaseWords = 0;
		StringTokenizer tokenizer = new StringTokenizer(message, " ", false);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			boolean isLowerCase = false;
			int upperCaseCharacters = 0;
			for (int i = 0; i < token.length(); i++) {
				char ch = token.charAt(i);
				if (Character.isUpperCase(ch)) {
					upperCaseCharacters++;
				} else if (Character.isLowerCase(ch)) {
					isLowerCase = true;
					break;
				}
			}
			if (!isLowerCase && (upperCaseCharacters > 0)) upperCaseWords++;
		}
		return upperCaseWords > Config.V.maxNumberOfUpperCaseWordsInLine;
	}

	private boolean hasTooManyUpperCaseLetters(String message) {
		int upperCaseLetters = 0;
		for (int i = 0; i < message.length(); i++) {
			char ch = message.charAt(i);
			if (Character.isUpperCase(ch)) upperCaseLetters++;
		}
		return upperCaseLetters > Config.V.maxNumberOfUpperCaseLettersInLine;	
	}

	public boolean isDuplicate(Player player, String line) {
		int sameMessages = 1 + this._repeatedLines.numberOfDuplicates(player, line);
		return sameMessages > Config.V.maxNumberOfRepeatedLines;
	}
}
