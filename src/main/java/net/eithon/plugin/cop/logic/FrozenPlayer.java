package net.eithon.plugin.cop.logic;

import net.eithon.library.extensions.EithonPlayer;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FrozenPlayer {

	private EithonPlayer _eithonPlayer;
	private float _walkSpeed;
	private float _flySpeed;
	private int _fireTicks;
	private int _foodLevel;

	public FrozenPlayer(Player player) {
		this._eithonPlayer = new EithonPlayer(player);
		freeze();
	}

	public Player getPlayer() { return this._eithonPlayer.getPlayer();	}
	public String getName() { return this._eithonPlayer.getName(); }
	
	public void freeze() {
		Player player = getPlayer();
		this._walkSpeed = player.getWalkSpeed();
		this._flySpeed = player.getFlySpeed();
		player.setWalkSpeed(0);
		this._fireTicks = player.getFireTicks();
		player.setFireTicks(0);
		this._foodLevel = player.getFoodLevel();
		player.setFoodLevel(20);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));		

	}
	
	public void thaw() {
		if (!this._eithonPlayer.isOnline()) return;
		Player player = getPlayer();
		player.setWalkSpeed(this._walkSpeed);
		player.setFlySpeed(this._flySpeed);
		player.setFireTicks(this._fireTicks);
		player.setFoodLevel(this._foodLevel);
		player.removePotionEffect(PotionEffectType.JUMP);
	}
	
	public static void restore(Player player, float walkSpeed, float flySpeed) {
		if (!player.isOnline()) return;
		player.setWalkSpeed(walkSpeed);
		player.setFlySpeed(flySpeed);
		player.setFireTicks(0);
		player.setFoodLevel(20);
		player.removePotionEffect(PotionEffectType.JUMP);
	}
}
