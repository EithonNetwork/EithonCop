package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.command.CommandSender;

public class CommandHandler implements ICommandHandler {
	private static final String BLACKLIST_COMMAND = "/eithoncop blacklist <profanity>";
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
			addCommand(commandParser);
		} else {
			commandParser.showCommandSyntax();
		}
		return true;
	}

	void addCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("eithoncop.blacklist")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		String profanity = commandParser.getArgumentStringAsLowercase();
		if (profanity == null) return;

		CommandSender sender = commandParser.getSender();
		boolean success = this._controller.addProfanity(sender, profanity);
		if (!success) return;
		Config.M.profanityAdded.sendMessage(sender, profanity);
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("blacklist")) {
			sender.sendMessage(BLACKLIST_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}
	}
}