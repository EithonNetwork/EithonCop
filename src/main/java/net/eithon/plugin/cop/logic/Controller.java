package net.eithon.plugin.cop.logic;

import java.util.List;
import java.util.stream.Collectors;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.mysql.Database;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.cop.Config;
import net.eithon.plugin.cop.profanity.ProfanityFilterController;
import net.eithon.plugin.cop.spam.SpamController;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Controller {
	private ProfanityFilterController _profanityFilterController;
	private SpamController _spamController;
	private MuteController _muteController;
	private EithonPlugin _eithonPlugin;
	private PlayerCollection<FrozenPlayer> _frozenPlayers;
	private int _repeatCount;
	private boolean _needNewRepeat ;

	public Controller(EithonPlugin eithonPlugin, Database database) throws FatalException{
		this._eithonPlugin = eithonPlugin;
		this._profanityFilterController = new ProfanityFilterController(eithonPlugin, database);
		this._spamController = new SpamController(eithonPlugin);
		this._muteController = new MuteController(eithonPlugin);
		this._frozenPlayers = new PlayerCollection<FrozenPlayer>();
		this._needNewRepeat = true;
	}

	public void disable() {
		this._profanityFilterController.disable();
	}

	public String addProfanity(CommandSender sender, String word, boolean isLiteral) throws FatalException, TryAgainException {
		return this._profanityFilterController.addProfanity(sender, word, isLiteral);
	}

	public String removeProfanity(CommandSender sender, String word) throws FatalException, TryAgainException {
		return this._profanityFilterController.removeProfanity(sender, word);
	}

	public String normalize(String word) {
		return this._profanityFilterController.normalize(word);
	}

	public String addAccepted(CommandSender sender, String word) throws FatalException, TryAgainException {
		return this._profanityFilterController.addAccepted(sender, word);
	}

	public String removeAccepted(CommandSender sender, String word) throws FatalException, TryAgainException {
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
			long secondsLeft = this._spamController.secondsLeft(player);
			if (secondsLeft > 0) {
				Config.M.chattingTooFast.sendMessage(
						player,
						secondsLeft, 
						Config.V.chatCoolDownAllowedTimes,
						Config.V.chatCoolDownInSeconds);
				return null;
			}
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

	public List<String> getMutePlayerNames() {
		return this._muteController.getMutedPlayers().stream().map(p->p.getName()).collect(Collectors.toList());
	}

	public List<String> getAllBlacklistedWords() {
		return this._profanityFilterController.getAllBlacklistedWords();
	}

	public List<String> getAllWhitelistedWords() {
		return this._profanityFilterController.getAllWhitelistedWords();
	}

	public boolean freezePlayer(CommandSender sender, Player player) {
		if (this._frozenPlayers.hasInformation(player)) {
			if (sender != null) Config.M.playerAlreadyFrozen.sendMessage(sender, player.getName());
			return false;
		}
		this._frozenPlayers.put(player, new FrozenPlayer(player));
		if (this._needNewRepeat) {
			this._needNewRepeat = false;
			repeatedlyTeleportFrozenPlayers();
		}
		return true;
	}

	public void playerJoined(Player player) {
		new BukkitRunnable() {
			public void run() {
				refreeze(player);
			}
		}.runTaskLaterAsynchronously(this._eithonPlugin, TimeMisc.secondsToTicks(1));
		
	}

	void refreeze(Player player) {
		FrozenPlayer frozenPlayer = this._frozenPlayers.get(player);
		if (frozenPlayer == null) {
			verbose("playerJoined", "Player=%s was not frozen", player.getName());
			return;
		}
		new BukkitRunnable() {
			public void run() {
				verbose("playerJoined", "Calling refreeze");
				frozenPlayer.refreeze();
			}
		}.runTask(this._eithonPlugin);
	}

	public boolean thawPlayer(CommandSender sender, OfflinePlayer player) {
		FrozenPlayer frozenPlayer = this._frozenPlayers.get(player);
		if (frozenPlayer == null) {
			Config.M.playerNotFrozen.sendMessage(sender, player.getName());
			return false;
		}
		frozenPlayer.thaw();
		this._frozenPlayers.remove(player);
		if (this._frozenPlayers.size() == 0) {
			this._repeatCount++;
			this._needNewRepeat = true;
		}
		return true;
	}

	public boolean restorePlayer(CommandSender sender, Player player, float walkSpeed, float flySpeed) {
		FrozenPlayer.restore(player, walkSpeed, flySpeed);
		return true;
	}

	public boolean isFrozen(Player player) {
		FrozenPlayer frozenPlayer = this._frozenPlayers.get(player);
		if (frozenPlayer == null) return false;
		return frozenPlayer.isFrozen();
	}

	public boolean canTeleport(Player player) {
		FrozenPlayer frozenPlayer = this._frozenPlayers.get(player);
		if (frozenPlayer == null) return true;
		return frozenPlayer.canTeleport();
	}

	public List<String> getFrozenPlayerNames() {
		return this._frozenPlayers.values().stream().map(p->p.getName()).collect(Collectors.toList());
	}

	public void freezeList(CommandSender sender) {
		if ((this._frozenPlayers == null) || (this._frozenPlayers.size() == 0)) {
			sender.sendMessage("No frozen players");
			return;
		}
		for (FrozenPlayer frozenPlayer : this._frozenPlayers) {
			sender.sendMessage(frozenPlayer.getName());
		}
	}

	public void repeatedlyTeleportFrozenPlayers() {
		final int count = ++this._repeatCount;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!teleportFrozenPlayers(count)) this.cancel();
			}
		}.runTaskTimerAsynchronously(
				this._eithonPlugin, 
				0,
				TimeMisc.secondsToTicks(1));
	}

	boolean teleportFrozenPlayers(final int count) {
		if (count != this._repeatCount) return false;
		for (FrozenPlayer frozenPlayer : this._frozenPlayers) {
			new BukkitRunnable() {
				public void run() {
					frozenPlayer.telePortBack();
				}
			}.runTask(this._eithonPlugin);
		}
		return true;
	}

	void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("Controller", method, format, args);	
	}
}