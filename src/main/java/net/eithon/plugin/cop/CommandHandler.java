package net.eithon.plugin.cop;

import java.util.List;

import net.eithon.library.command.EithonCommandUtilities;
import net.eithon.library.command.CommandSyntaxException;
import net.eithon.library.command.EithonCommand;
import net.eithon.library.command.ICommandSyntax;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {
	private Controller _controller;
	private ICommandSyntax _commandSyntax;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	public ICommandSyntax getCommandSyntax() {
		if (this._commandSyntax != null) return this._commandSyntax;

		ICommandSyntax commandSyntax = EithonCommand.createRootCommand("eithoncop");
		commandSyntax.setPermissionsAutomatically();

		try {
			setupFreezeCommand(commandSyntax);
			setupBlacklistCommand(commandSyntax);
			setupWhitelistCommand(commandSyntax);
			setupMuteCommand(commandSyntax);
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			return null;
		}
		this._commandSyntax = commandSyntax;
		return this._commandSyntax;
	}

	public void setupMuteCommand(ICommandSyntax commandSyntax)
			throws CommandSyntaxException {
		ICommandSyntax tempmute = commandSyntax.parseCommandSyntax("tempmute <player> <time-span : TIME_SPAN> <reason : REST>")
				.setCommandExecutor(ec->tempMuteCommand(ec));

		tempmute
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec));

		tempmute
		.getParameterSyntax("time-span")
		.setDefault(Config.V.defaultTempMuteInSeconds);

		ICommandSyntax unmute = commandSyntax.parseCommandSyntax("unmute <player>")
				.setCommandExecutor(ec->unmuteCommand(ec));

		unmute
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> getMutedPlayerNames(ec));
	}

	private List<String> getMutedPlayerNames(EithonCommand ec) {
		return this._controller.getMutePlayerNames();
	}

	public void setupFreezeCommand(ICommandSyntax commandSyntax)
			throws CommandSyntaxException {

		ICommandSyntax freeze = commandSyntax.addKeyWord("freeze");

		// freeze on
		ICommandSyntax on = freeze.parseCommandSyntax("on <player>")
				.setCommandExecutor(p -> freezeOnCommand(p));

		on
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec));

		// freeze off
		ICommandSyntax off = freeze.parseCommandSyntax("off <player>")
				.setCommandExecutor(p -> freezeOffCommand(p));

		off
		.getParameterSyntax("player")
		.setMandatoryValues(ec->getFrozenPlayerNames());

		// freeze restore
		ICommandSyntax restore = freeze.parseCommandSyntax(String.format(
				"restore <player> <walk-speed:REAL {_%s_,...}> <fly-speed:REAL {_%s_,...}>",
				Double.toString(Config.V.freezeRestoreWalkSpeed), Double.toString(Config.V.freezeRestoreFlySpeed)))
				.setCommandExecutor(ec -> freezeRestoreCommand(ec));

		restore
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec));

		// freeze list
		freeze.parseCommandSyntax("list")
		.setCommandExecutor(p -> freezeListCommand(p));
	}

	public void setupBlacklistCommand(ICommandSyntax commandSyntax)
			throws CommandSyntaxException {

		ICommandSyntax blacklist = commandSyntax.addKeyWord("blacklist");

		// blacklist add
		blacklist.parseCommandSyntax("add <profanity> <is-literal : BOOLEAN {_true_, false}>")
		.setCommandExecutor(ec -> blacklistAddCommand(ec));

		// blacklist remove
		ICommandSyntax remove = blacklist.parseCommandSyntax("remove <profanity>")
				.setCommandExecutor(ec -> blacklistRemoveCommand(ec));

		remove
		.getParameterSyntax("profanity")
		.setMandatoryValues(ec -> getAllBlacklistedWords(ec));
	}

	private List<String> getAllBlacklistedWords(EithonCommand ec) {
		return this._controller.getAllBlacklistedWords();
	}

	public void setupWhitelistCommand(ICommandSyntax commandSyntax)
			throws CommandSyntaxException {

		ICommandSyntax whitelist = commandSyntax.addKeyWord("whitelist");

		// blacklist add
		whitelist.parseCommandSyntax("add <accepted-word>")
		.setCommandExecutor(ec -> whitelistAddCommand(ec));

		// blacklist remove
		ICommandSyntax remove = whitelist.parseCommandSyntax("remove <accepted-word>")
				.setCommandExecutor(ec -> whitelistRemoveCommand(ec));

		remove
		.getParameterSyntax("accepted-word")
		.setMandatoryValues(ec -> getAllWhitelistedWords(ec));
	}

	private List<String> getAllWhitelistedWords(EithonCommand ec) {
		return this._controller.getAllWhitelistedWords();
	}

	private List<String> getFrozenPlayerNames() {
		return this._controller.getFrozenPlayerNames();
	}

	private String getSenderAsOnlinePlayer(EithonCommand command) {
		return command.getPlayer().getName();
	}

	void blacklistAddCommand(EithonCommand command)
	{
		String profanity = command.getArgument("profanity").asLowerCase();
		if (profanity == null) return;

		CommandSender sender = command.getSender();

		boolean isLiteral = command.getArgument("is-literal").asBoolean();
		String word = this._controller.addProfanity(sender, profanity, isLiteral);
		if (word == null) return;
		Config.M.profanityAdded.sendMessage(sender, word);
	}

	void blacklistRemoveCommand(EithonCommand command)
	{
		String profanity = command.getArgument("profanity").asLowerCase();
		if (profanity == null) return;

		CommandSender sender = command.getSender();

		String word = this._controller.removeProfanity(sender, profanity);
		if (word == null) return;
		Config.M.profanityRemoved.sendMessage(sender, word);
	}

	void whitelistAddCommand(EithonCommand command)
	{
		String acceptedWord = command.getArgument("accepted-word").asLowerCase();
		if (acceptedWord == null) return;
		acceptedWord = this._controller.normalize(acceptedWord);

		CommandSender sender = command.getSender();

		String word = this._controller.addAccepted(sender, acceptedWord);
		if (word == null) return;
		Config.M.acceptedWordAdded.sendMessage(sender, acceptedWord, word);

	}


	void whitelistRemoveCommand(EithonCommand command)
	{
		String acceptedWord = command.getArgument("accepted-word").asLowerCase();
		if (acceptedWord == null) return;
		acceptedWord = this._controller.normalize(acceptedWord);

		CommandSender sender = command.getSender();

		String word = this._controller.removeAccepted(sender, acceptedWord);
		if (word == null) return;
		Config.M.acceptedWordRemoved.sendMessage(sender, acceptedWord, word);
	}

	void tempMuteCommand(EithonCommand command)
	{
		EithonPlayer eithonPlayer = command.getArgument("player").asEithonPlayer();
		if (eithonPlayer == null) return;

		CommandSender sender = command.getSender();

		long timeInSeconds = command.getArgument("time-span").asSeconds();
		String reason = command.getArgument("reason").asString();
		if (reason == null) reason = Config.V.defaultTempMuteReason;

		boolean success = this._controller.tempMute(sender, eithonPlayer, timeInSeconds, reason);
		if (!success) return;
		Config.M.tempMutedPlayer.sendMessage(sender, eithonPlayer.getName(), TimeMisc.secondsToString(timeInSeconds), reason);		
	}

	void unmuteCommand(EithonCommand command)
	{
		EithonPlayer eithonPlayer = command.getArgument("player").asEithonPlayer();
		if (eithonPlayer == null) return;

		CommandSender sender = command.getSender();

		boolean success = this._controller.unmute(sender, eithonPlayer);
		if (!success) return;
		Config.M.unmutedPlayer.sendMessage(sender, eithonPlayer.getName());		
	}

	private void freezeOnCommand(EithonCommand command) {
		CommandSender sender = command.getSender();
		Player player = command.getArgument("player").asPlayer();
		if (player == null) return;

		if (!this._controller.freezePlayer(sender, player)) return;
		Config.M.playerFrozen.sendMessage(sender, player.getName());

	}

	private void freezeOffCommand(EithonCommand command) {
		CommandSender sender = command.getSender();
		OfflinePlayer player = command.getArgument("player").asOfflinePlayer();
		if (player == null) return;

		if (!this._controller.thawPlayer(sender, player)) return;
		Config.M.playerThawn.sendMessage(sender, player.getName());

	}

	private void freezeRestoreCommand(EithonCommand command) {
		CommandSender sender = command.getSender();
		Player player = command.getArgument("player").asPlayer();
		if (player == null) return;
		float walkSpeed = command.getArgument("walk-speed").asFloat();
		float flySpeed = command.getArgument("fly-speed").asFloat();

		if (!this._controller.restorePlayer(sender, player, walkSpeed, flySpeed)) return;
		Config.M.playerRestored.sendMessage(sender, player.getName());
	}

	private void freezeListCommand(EithonCommand command) {
		CommandSender sender = command.getSender();

		this._controller.freezeList(sender);
	}
}