package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.command.CommandSender;

public class CommandHandler implements ICommandHandler {
	private static final String BLACKLIST_COMMAND = "/eithoncop blacklist <profanity>";
	private static final String WHITELIST_COMMAND = "/eithoncop whitelist <accepted word>";
	private Controller _controller;
	private EithonPlugin _eithonPlugin;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
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
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		String profanity = commandParser.getArgumentStringAsLowercase();
		if (profanity == null) return;

		CommandSender sender = commandParser.getSender();
		String addedWord = this._controller.addProfanity(sender, profanity);
		if (addedWord == null) return;
		Config.M.profanityAdded.sendMessage(sender, addedWord);
	}

	void whitelistCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithoncop.whitelist")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		String acceptedWord = commandParser.getArgumentStringAsLowercase();
		if (acceptedWord == null) return;

		CommandSender sender = commandParser.getSender();
		String profanity = this._controller.addAccepted(sender, acceptedWord);
		if (profanity == null) return;
		Config.M.acceptedWordAdded.sendMessage(sender, acceptedWord, profanity);
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