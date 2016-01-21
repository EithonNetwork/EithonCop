package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.dthielke.herochat.Channel;
import com.dthielke.herochat.ChannelChatEvent;

public final class EventListener implements Listener {

	private Controller _controller;
	private EithonPlugin _eithonPlugin;
	private Logger _eithonLogger;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._eithonPlugin = eithonPlugin;
		this._eithonLogger = eithonPlugin.getEithonLogger();
		this._controller = controller;
	}

	/*
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerChatEventLowest(AsyncPlayerChatEvent e) {
		String originalMessage = e.getMessage();
		Player player = e.getPlayer();
		verbose("onAsyncPlayerChatEvent", "Enter:  %s sending: \"%s\".", player.getName(), originalMessage);
		String newMessage = originalMessage;

		newMessage = this._controller.censorMessage(player, originalMessage);
		if (newMessage == null) e.setCancelled(true);
		else e.setMessage(newMessage);

		if (this._controller.isMuted(player)) {
			newMessage = null;
			e.setCancelled(true);
			verbose("onAsyncPlayerChatEvent", "Leave: Trying to mute player %s from sending message \"%s\".",
					player.getName(), originalMessage);
			return;
		}
		verbose("onAsyncPlayerChatEvent", "Leave:  \"%s\".", newMessage == null ? "null" : newMessage);
	}
	 */

	// Censor channel chats, mute channel chats
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChannelChatEvent(ChannelChatEvent e) {
		Player player = e.getSender().getPlayer();
		String originalMessage = e.getMessage();
		verbose("onChannelChatEvent", "Enter:  %s sending on channel %s: \"%s\".", player.getName(), e.getChannel().getName(), originalMessage);
		String newMessage = originalMessage;

		if (isPrivateChannel(e.getChannel())) {
			verbose("onChannelChatEvent", "The message will not be censored, because the channel was private.");			
		} else {
			newMessage = this._controller.censorMessage(player, originalMessage);
			if (newMessage == null) e.setResult(null);
			else e.setMessage(newMessage);
		}

		if (this._controller.isMuted(player)) {
			verbose("onChannelChatEvent", "Leave: Trying to mute player %s from sending message \"%s\".", player.getName(), newMessage);
			e.setResult(null);
			return;
		}
		verbose("onAsyncPlayerChatEvent", "Leave:  \"%s\".", newMessage == null ? "null" : newMessage);
	}

	private boolean isPrivateChannel(Channel channel) {
		if (channel.getMembers().size() > 2) return false;
		return channel.getName().startsWith("convo");
	}

	// Mute certain commands
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		String message = event.getMessage();
		verbose("onPlayerCommandPreprocessEvent", "Intercepted command \"%s\".", message);
		Player player = event.getPlayer();
		if (this._controller.isPlayerMutedForCommand(player, message)) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "Command \"%s\" will be cancelled for player %s.", 
					message, player.getName());
			event.setCancelled(true);
		}
	}


	// Frozen players should not teleport
	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		verbose("onPlayerTeleportEvent", "Enter");
		Player player = event.getPlayer();
		
		if (this._controller.isFrozen(player)) {
			verbose("onPlayerTeleportEvent", "Player is frozen. Cancel and return.");
			Config.M.frozenPlayerCannotTeleport.sendMessage(player);
			event.setCancelled(true);
			return;
		}
	}

	// Frozen players can't interact
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!this._controller.isFrozen(player)) return;
		event.setCancelled(true);
	}

	// Frozen players should not be able to be damaged or damage
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		verbose("onEntityDamageByEntityEvent", "Enter");
		
		// Frozen player is the damager?
		if (event.getDamager() instanceof Player) {
			Player player = (Player) event.getDamager();
			verbose("onEntityDamageByEntityEvent", "Damage by player %s.", player.getName());
			if (this._controller.isFrozen(player)) {
				verbose("onEntityDamageByEntityEvent", 
						"Player %s is not allowed to do damage when frozen.", player.getName());
				event.setCancelled(true);
				verbose("onEntityDamageByEntityEvent", "Leave");
				return;
			}
		}
		// Frozen player being damaged?
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			verbose("onEntityDamageByEntityEvent", "Damage to player %s.", player.getName());
			if (this._controller.isFrozen(player)) {
				verbose("onEntityDamageByEntityEvent", 
						"Player %s is not allowed to receive damage when frozen.", player.getName());
				event.setCancelled(true);
				verbose("onEntityDamageByEntityEvent", "Leave");
				return;
			}
		}
		verbose("onEntityDamageByEntityEvent", "Leave");
	}		

	// Frozen players should not be able to be damaged or damage
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		verbose("onBlockBreakEvent", "Enter");
		
		// Frozen player is the damager?
		Player player = event.getPlayer();
		verbose("onBlockBreakEvent", "Player %s is breaking a block.", player.getName());
		if (this._controller.isFrozen(player)) {
			verbose("onBlockBreakEvent", 
					"Player %s is not allowed to break blocks when frozen.", player.getName());
			event.setCancelled(true);
			verbose("onBlockBreakEvent", "Leave");
			return;
		}
		
		verbose("onBlockBreakEvent", "Leave");
	}			

	// Frozen players should not be able to be damaged or damage
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		verbose("onBlockPlaceEvent", "Enter");
		
		// Frozen player is the damager?
		Player player = event.getPlayer();
		verbose("onBlockPlaceEvent", "Player %s is placing a block.", player.getName());
		if (this._controller.isFrozen(player)) {
			verbose("onBlockPlaceEvent", 
					"Player %s is not allowed to place blocks when frozen.", player.getName());
			event.setCancelled(true);
			verbose("onBlockPlaceEvent", "Leave");
			return;
		}
		
		verbose("onBlockPlaceEvent", "Leave");
	}

	private void verbose(String method, String format, Object... args) {
		String message = String.format(format, args);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "EventListener.%s: %s", method, message);
	}
}
