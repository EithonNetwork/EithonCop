package net.eithon.plugin.cop.logic;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.profanity.ProfanityFilterController;
import net.eithon.plugin.cop.spam.SpamController;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Controller {
	private ProfanityFilterController _profanityFilterController;
	private SpamController _spamController;
	
	public Controller(EithonPlugin eithonPlugin){
		this._profanityFilterController = new ProfanityFilterController(eithonPlugin);
		this._spamController = new SpamController(eithonPlugin);
	}

	public void disable() {
		this._profanityFilterController.disable();
	}

	public String addProfanity(CommandSender sender, String word, boolean isLiteral, String synonyms) {
		return this._profanityFilterController.addProfanity(sender, word, isLiteral, synonyms);
	}

	public String removeProfanity(CommandSender sender, String word) {
		return this._profanityFilterController.removeProfanity(sender, word);
	}

	public String normalize(String word) {
		return this._profanityFilterController.normalize(word);
	}

	public String addAccepted(CommandSender sender, String word) {
		return this._profanityFilterController.addAccepted(sender, word);
	}

	public String removeAccepted(CommandSender sender, String word) {
		return this._profanityFilterController.removeAccepted(sender, word);
	}

	public String profanityFilter(Player player, String message) {
		return this._profanityFilterController.profanityFilter(player, message);
	}

	public String reduceUpperCaseUsage(Player player, String message) {
		return this._spamController.reduceUpperCaseUsage(player, message);
	}

	public boolean isDuplicate(Player player, String message) {
		return this._spamController.isDuplicate(player, message);
	}
}