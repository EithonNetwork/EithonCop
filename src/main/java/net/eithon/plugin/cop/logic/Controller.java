package net.eithon.plugin.cop.logic;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.cop.Config;
import net.eithon.plugin.cop.profanity.ProfanityFilterController;
import net.eithon.plugin.cop.spam.SpamController;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Controller {
	private ProfanityFilterController _profanityFilterController;
	private SpamController _spamController;
	private MuteController _muteController;
	private EithonPlugin _eithonPlugin;
	
	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
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

	public boolean unmute(CommandSender sender, EithonPlayer eithonPlayer) {
		return this._muteController.unmute(sender, eithonPlayer.getPlayer());
	}

	public String censorMessage(Player player, String originalMessage) {
		verbose("censorMessage", "Enter, Player %s: \"%s\"", player.getName(), originalMessage);
		if (originalMessage == null) return null;
		if (this._spamController.isTooFast(player)) {
			verbose("censorMessage", "Leave, Player %s: too fast! Return null", player.getName());
			Config.M.chattingTooFast.sendMessage(
					player,
					this._spamController.secondsLeft(player), 
					Config.V.chatCoolDownAllowedTimes,
					Config.V.chatCoolDownInSeconds);
			return null;
		}
		String maybeLowerase = this._spamController.reduceUpperCaseUsage(player, originalMessage);
		String profaneMessage = this._profanityFilterController.profanityFilter(player, maybeLowerase);
		verbose("censorMessage", "Player %s: Case and profanity fixed: \"%s\"", player.getName(), profaneMessage);
		if (this._spamController.isDuplicate(player, profaneMessage)) {
			verbose("censorMessage", "Leave, Player %s: Duplicates! Return null", player.getName());
			Config.M.chatDuplicateMessage.sendMessage(player);
			return null;
		}
		verbose("censorMessage", "Player %s: Leave, return \"%s\"", player.getName(), profaneMessage);
		return profaneMessage;
	}

	public boolean isMuted(Player player) {
		return this._muteController.isMuted(player);
	}

	public boolean isPlayerMutedForCommand(Player player, String command) {
		return this._muteController.isPlayerMutedForCommand(player, command);
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Controller.%s: %s", method, message);
	}
}