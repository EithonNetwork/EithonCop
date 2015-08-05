package net.eithon.plugin.cop.logic;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.mute.MuteController;
import net.eithon.plugin.cop.profanity.ProfanityFilterController;
import net.eithon.plugin.cop.spam.SpamController;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Controller {
	private ProfanityFilterController _profanityFilterController;
	private SpamController _spamController;
	private MuteController _muteController;
	
	public Controller(EithonPlugin eithonPlugin){
		this._profanityFilterController = new ProfanityFilterController(eithonPlugin);
		this._spamController = new SpamController(eithonPlugin);
		this._muteController = new MuteController(eithonPlugin);
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

	public boolean tempMute(CommandSender sender, EithonPlayer eithonPlayer,
			long timeInSeconds, String reason) {
		return this._muteController.temporarilyMute(sender, eithonPlayer.getPlayer(), timeInSeconds, reason);
	}

	public String censorMessage(Player player, String originalMessage) {
		String maybeLowerase = this._spamController.reduceUpperCaseUsage(player, originalMessage);
		String profaneMessage = this._profanityFilterController.profanityFilter(player, maybeLowerase);
		if (this._spamController.isDuplicate(player, profaneMessage)) return null;
		return profaneMessage;
	}

	public boolean isMuted(Player player) {
		return this._muteController.isMuted(player);
	}

	public boolean isPlayerMutedForCommand(Player player, String command) {
		return this._muteController.isPlayerMutedForCommand(player, command);
	}
}