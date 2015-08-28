package net.eithon.plugin.cop;

import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

	// Censor chats
	@EventHandler
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
		if (e.isCancelled()) return;
		String originalMessage = e.getMessage();
		verbose("onAsyncPlayerChatEvent", "Enter:  \"%s\".", originalMessage);

		String newMessage = this._controller.censorMessage(e.getPlayer(), originalMessage);
		if (newMessage == null) e.setCancelled(true);
		else e.setMessage(newMessage);
		verbose("onAsyncPlayerChatEvent", "Leave:  \"%s\".", newMessage == null ? "null" : newMessage);
	}

	// Censor channel chats, mute channel chats
	@EventHandler
	public void onChannelChatEvent(ChannelChatEvent e) {
		String originalMessage = e.getMessage();
		verbose("onChannelChatEvent", "Enter:  \"%s\".", originalMessage);
		String newMessage = originalMessage;
		Player player = e.getSender().getPlayer();

		if (!isPrivateChannel(e.getChannel())) {
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
		return channel.getMembers().size() < 3;
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
