package net.eithon.plugin.cop;

import net.eithon.plugin.cop.logic.Controller;

import org.bukkit.entity.Player;

public class EithonCopApi {
	private static Controller controller;

	static void initialize(Controller _controller) {
		controller = _controller;
	}

	public static String censorMessage(Player player, String message) {
		if (controller == null) return message;
		return controller.censorMessage(player, message);
	}
}
