package net.eithon.plugin.cop.mute;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.CoolDown;
import net.eithon.plugin.cop.Config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteController {
	private CoolDown _mutedPlayers;
	private EithonPlugin _eithonPlugin;
	
	public MuteController(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._mutedPlayers = new CoolDown("MutedPlayers", Config.V.defaultTempMuteInSeconds);
	}

	public boolean temporarilyMute(CommandSender sender,
			Player player, long timeInSeconds, String reason) {
		this._mutedPlayers.addIncident(player, timeInSeconds);
		Config.C.tempMutePlayer.executeAs(sender, player.getName(), timeInSeconds, reason);
		return true;
	}

	public boolean unmute(CommandSender sender, Player player) {
		this._mutedPlayers.removePlayer(player);
		Config.C.unutePlayer.executeAs(sender, player.getName());
		return true;
	}

	public boolean isMuted(Player player) {
		return this._mutedPlayers.isInCoolDownPeriod(player);
	}

	public boolean isPlayerMutedForCommand(Player player, String command) {
		if (!this._mutedPlayers.isInCoolDownPeriod(player)) {
			verbose("isPlayerMutedForCommand", "Player %s is not muted for now", player.getName());
			return false;
		}
		verbose("isPlayerMutedForCommand", "Player %s should be muted. Check if command \"%s\" should be muted for him/her.",
				player.getName(), command);
		return commandShouldBeMuted(command);
	}

	private boolean commandShouldBeMuted(String command) {
		String[] tokens = command.split("\\W");
		verbose("commandShouldBeMuted", "Comparing command [%s] ...", String.join(", ", tokens));
		for (String mutedCommand : Config.V.mutedCommands) {
			String[] mutedCommandTokens = mutedCommand.split("\\W");
			verbose("commandShouldBeMuted", "... with muted command [%s]", String.join(", ", mutedCommandTokens));
			if (isSame(tokens, mutedCommandTokens)) {
				verbose("commandShouldBeMuted", "... resulting in true (\"%s\" begins with \"%s\")", command, mutedCommand);
				return true;
			}
		}
		verbose("commandShouldBeMuted", "... no muted commands matched \"%s\".", command);
		return false;
	}

	private boolean isSame(String[] tokens, String[] mutedCommandTokens) {
		if (mutedCommandTokens.length > tokens.length) return false;
		boolean found = true;
		for (int i = 0; i < mutedCommandTokens.length; i++) {
			if (!mutedCommandTokens[i].equalsIgnoreCase(tokens[i])) {
				found = false;
				break;
			}
		}
		return found;
	}

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "MuteController.%s: %s", method, message);
	}
}
