package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class EventListener implements Listener {

	private Controller _controller;
	
	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	@EventHandler
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
		if (e.isCancelled()) return;
		
		// Get the message
		String originalMessage = e.getMessage();
		String maybeLowerase = this._controller.reduceUpperCaseUsage(e.getPlayer(), originalMessage);
		String profaneMessage = this._controller.profanityFilter(e.getPlayer(), maybeLowerase);
		if (this._controller.isDuplicate(e.getPlayer(), profaneMessage)) {
			e.setCancelled(true);
		}
		e.setMessage(profaneMessage);
	}
}
