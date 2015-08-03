package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.command.CommandSender;

public class CommandHandler implements ICommandHandler {
	private static final String BLACKLIST_COMMAND = "/eithoncop blacklist add|remove <profanity> [<isliteral>] [<synonyms>]";
	private static final String WHITELIST_COMMAND = "/eithoncop whitelist add|remove <accepted word>";
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

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("blacklist")) {
			sender.sendMessage(BLACKLIST_COMMAND);
		} else if (command.equals("whitelist")) {
			sender.sendMessage(WHITELIST_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}
	}
}