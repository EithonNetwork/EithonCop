package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public final class EventListener implements Listener {

	private Controller _controller;
	private EithonPlugin _eithonPlugin;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._eithonPlugin = eithonPlugin;
		this._controller = controller;
	}


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
	

	/* HeroChat
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
	 */

	// Mute certain commands
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) return;
		String message = event.getMessage();
		verbose("onPlayerCommandPreprocessEvent", "Intercepted command \"%s\".", message);
		Player player = event.getPlayer();
		if (this._controller.isPlayerMutedForCommand(player, message)) {
			this._eithonPlugin.dbgMajor( "Command \"%s\" will be cancelled for player %s.", 
					message, player.getName());
			event.setCancelled(true);
		}
	}

	// Frozen player that logs in again should be frozen again
	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		verbose("onPlayerJoinEvent", "Player=%s", player.getName());
		this._controller.playerJoined(event.getPlayer());
	}

	// Frozen player that is respawned again should be frozen again
	@EventHandler(ignoreCancelled=true)
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		verbose("onPlayerJoinEvent", "Player=%s", player.getName());
		this._controller.playerJoined(event.getPlayer());
	}


	// Frozen players should not teleport
	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		verbose("onPlayerTeleportEvent", "Enter");
		Player player = event.getPlayer();

		if (this._controller.canTeleport(player)) return;

		verbose("onPlayerTeleportEvent", "Player is frozen. Cancel and return.");
		Config.M.frozenPlayerCannotTeleport.sendMessage(player);
		event.setCancelled(true);
		return;
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

	// Frozen players should not be able to be damaged or damage
	@EventHandler(ignoreCancelled = true)
	public void onPlayerToggleSprintEvent(PlayerToggleSprintEvent event) {
		verbose("onPlayerToggleSprintEvent", "Enter");

		if (!event.isSprinting()) return;
		
		// Frozen player is the damager?
		Player player = event.getPlayer();
		if (this._controller.isFrozen(player)) {
			event.setCancelled(true);
			return;
		}

		verbose("onPlayerToggleSprintEvent", "Leave");
	}

	// Frozen players should not become a target
	@EventHandler(ignoreCancelled = true)
	public void onEntityTargetLivingEntityEvent(EntityTargetLivingEntityEvent event) {
		Entity target = event.getTarget();
		if (!(target instanceof Player)) return;
		Player player = (Player) target;

		if (!(event.getEntity() instanceof Monster)) return;

		if (!this._controller.isFrozen(player)) return;

		event.setCancelled(true);
	}

	// No damage on frozen players
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) return;
		Player player = (Player) entity;

		if (!this._controller.isFrozen(player)) return;

		event.setCancelled(true);
	}
	
	// Frozen players can't toggle flight 
	@EventHandler(ignoreCancelled = true)
	public void onPlayerToggleFlightEvent(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();
		if (!this._controller.isFrozen(player)) return;
		event.setCancelled(true);
	}

	private void verbose(String method, String format, Object... args) {
		this._eithonPlugin.dbgVerbose("EventListener", method, format, args);	
	}
}
