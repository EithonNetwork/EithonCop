package net.eithon.plugin.cop.logic;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.profanity.ProfanityFilterController;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Controller {
	private EithonPlugin _eithonPlugin;
	private ProfanityFilterController _profanityFilterController;
	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._profanityFilterController = new ProfanityFilterController(eithonPlugin);
	}

	public void disable() {
		this._profanityFilterController.disable();
	}

	public String addProfanity(CommandSender sender, String word) {
		return this._profanityFilterController.addProfanity(sender, word);
	}

	public String normalize(String word) {
		return this._profanityFilterController.normalize(word);
	}

	public String addAccepted(CommandSender sender, String word) {
		return this._profanityFilterController.addAccepted(sender, word);
	}

	public String profanityFilter(Player player, String message) {
		return this._profanityFilterController.profanityFilter(player, message);
	}
}