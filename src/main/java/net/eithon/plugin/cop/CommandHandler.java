package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;

import org.bukkit.command.CommandSender;

public class CommandHandler implements ICommandHandler{

	private net.eithon.plugin.cop.Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	@Override
	public boolean onCommand(CommandParser commandParser) {
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(0,0)) return true;
		EithonPlayer player = commandParser.getEithonPlayerOrInformSender();
		if (player == null) return true;
		if (player.hasPermission("cop.noteligible")) {
			Config.M.notEligibleForCop.sendMessage(player.getPlayer());
			return true;
		}

		copCommand(commandParser);
		return true;
	}

	public void copCommand(CommandParser commandParser) {
		this._controller.cop(commandParser.getPlayer());
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {	
		sender.sendMessage("/cop");
	}
}
