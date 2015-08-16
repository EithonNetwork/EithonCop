package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.command.CommandSender;

public class CommandHandler implements ICommandHandler {
	private static final String BLACKLIST_COMMAND = "/eithoncop blacklist add|remove <profanity> [<isliteral>] [<synonyms>]";
	private static final String WHITELIST_COMMAND = "/eithoncop whitelist add|remove <accepted word>";
	private static final String UNMUTE_COMMAND = "/eithoncop unmute <player>";
	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	public boolean onCommand(CommandParser commandParser) {
		String command = commandParser.getArgumentCommand();
		if (command == null) return false;

		if (command.equals("blacklist")) {
			blacklistCommand(commandParser);
		} else if (command.equals("whitelist")) {
			whitelistCommand(commandParser);
		} else if (command.equals("tempmute")) {
			tempMuteCommand(commandParser);
		} else if (command.equals("unmute")) {
			unmuteCommand(commandParser);
		} else {
			commandParser.showCommandSyntax();
		}
		return true;
	}

	void blacklistCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithoncop.blacklist")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3)) return;

		String subCommand = commandParser.getArgumentStringAsLowercase();
		if (!subCommand.equalsIgnoreCase("add") 
				&& !subCommand.equalsIgnoreCase("remove")) {
			showCommandSyntax(commandParser.getSender(), "blacklist");
			return;
		}

		String profanity = commandParser.getArgumentStringAsLowercase();
		if (profanity == null) return;

		CommandSender sender = commandParser.getSender();

		if (subCommand.equalsIgnoreCase("add")) {
			if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3)) return;
			int isLiteralAsInt = commandParser.getArgumentInteger(1);
			boolean isLiteral = isLiteralAsInt != 0;
			String synonyms = commandParser.getArgumentRest();
			String word = this._controller.addProfanity(sender, profanity, isLiteral, synonyms);
			if (word == null) return;
			Config.M.profanityAdded.sendMessage(sender, word);
		} else if (subCommand.equalsIgnoreCase("remove")) {
			if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3, 3)) return;
			String word = this._controller.removeProfanity(sender, profanity);
			if (word == null) return;
			Config.M.profanityRemoved.sendMessage(sender, word);
		}
	}

	void whitelistCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithoncop.whitelist")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3, 3)) return;

		String subCommand = commandParser.getArgumentStringAsLowercase();
		if (!subCommand.equalsIgnoreCase("add") && !subCommand.equalsIgnoreCase("remove")) {
			showCommandSyntax(commandParser.getSender(), "blacklist");
			return;
		}

		String acceptedWord = commandParser.getArgumentStringAsLowercase();
		if (acceptedWord == null) return;
		acceptedWord = this._controller.normalize(acceptedWord);

		CommandSender sender = commandParser.getSender();

		if (subCommand.equalsIgnoreCase("add")) {
			String profanity = this._controller.addAccepted(sender, acceptedWord);
			if (profanity == null) return;
			Config.M.acceptedWordAdded.sendMessage(sender, acceptedWord, profanity);
		} else if (subCommand.equalsIgnoreCase("remove")) {
			String profanity = this._controller.removeAccepted(sender, acceptedWord);
			if (profanity == null) return;
			Config.M.acceptedWordRemoved.sendMessage(sender, acceptedWord, profanity);
		}
	}

	void tempMuteCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithoncop.tempmute")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 4)) return;
		
		EithonPlayer eithonPlayer = commandParser.getArgumentEithonPlayer((EithonPlayer)null);
		if (eithonPlayer == null) {
			commandParser.showCommandSyntax();
			return;
		}

		CommandSender sender = commandParser.getSender();
		
		long timeInSeconds = commandParser.getArgumentTimeAsSeconds(Config.V.defaultTempMuteInSeconds);
		String reason = commandParser.getArgumentRest(Config.V.defaultTempMuteReason);

		boolean success = this._controller.tempMute(sender, eithonPlayer, timeInSeconds, reason);
		if (!success) return;
		Config.M.tempMutedPlayer.sendMessage(sender, eithonPlayer.getName(), TimeMisc.secondsToString(timeInSeconds), reason);		
	}

	void unmuteCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithoncop.unmute")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;
		
		EithonPlayer eithonPlayer = commandParser.getArgumentEithonPlayer((EithonPlayer)null);
		if (eithonPlayer == null) {
			commandParser.showCommandSyntax();
			return;
		}

		CommandSender sender = commandParser.getSender();
		
		boolean success = this._controller.unmute(sender, eithonPlayer);
		if (!success) return;
		Config.M.unmutedPlayer.sendMessage(sender, eithonPlayer.getName());		
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("blacklist")) {
			sender.sendMessage(BLACKLIST_COMMAND);
		} else if (command.equals("whitelist")) {
			sender.sendMessage(WHITELIST_COMMAND);
		} else if (command.equals("tempmute")) {
			Config.M.tempMuteCommandDoc.sendMessage(sender);
		} else if (command.equals("unmute")) {
			sender.sendMessage(UNMUTE_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}
	}
}