package net.eithon.plugin.cop.spam;

import net.eithon.library.time.CoolDown;
import net.eithon.plugin.cop.Config;

import org.bukkit.entity.Player;

class ChatCoolDown {
	private CoolDown _coolDown;
	
	ChatCoolDown() {
		this._coolDown = new CoolDown("ChatCoolDown", Config.V.chatCoolDownInSeconds, Config.V.chatCoolDownAllowedTimes);
	}
	
	public boolean addIncidentOrFalse(Player player) {
		return this._coolDown.addIncidentOrFalse(player);
	}

	public long secondsLeft(Player player) {
		return this._coolDown.secondsLeft(player);
	}
}
