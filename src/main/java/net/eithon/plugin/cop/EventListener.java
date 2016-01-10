package net.eithon.plugin.cop;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.dthielke.herochat.Channel;
import com.dthielke.herochat.ChannelChatEvent;

public final class EventListener implements Listener {

	private Controller _controller;
	private EithonPlugin _eithonPlugin;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._eithonPlugin = eithonPlugin;
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

	void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "EventListener.%s: %s", method, message);
	}
}
