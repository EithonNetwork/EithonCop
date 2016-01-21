package net.eithon.plugin.cop.logic;

import net.eithon.library.extensions.EithonPlayer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FrozenPlayer {

	private EithonPlayer _eithonPlayer;
	private float _walkSpeed;
	private float _flySpeed;
	private int _fireTicks;
	private int _foodLevel;
	private boolean _isFlying;
	private Location _location;
	private boolean _canTeleport;

	public FrozenPlayer(Player player) {
		this._eithonPlayer = new EithonPlayer(player);
		this._location = player.getLocation();
		freeze();
	}

	public Player getPlayer() { return this._eithonPlayer.getPlayer();	}
	public String getName() { return this._eithonPlayer.getName(); }

	public void freeze() {
		Player player = getPlayer();
		this._canTeleport = false;
		this._isFlying = player.isFlying();
		try {
			player.setFlying(true);
		} catch (Exception e) {}
		this._walkSpeed = player.getWalkSpeed();
		player.setWalkSpeed(0);
		this._flySpeed = player.getFlySpeed();
		player.setFlySpeed(0);
		this._fireTicks = player.getFireTicks();
		player.setFireTicks(0);
		this._foodLevel = player.getFoodLevel();
		player.setFoodLevel(20);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
	}

	public void thaw() {
		if (!this._eithonPlayer.isOnline()) return;
		this._canTeleport = true;
		Player player = getPlayer();
		player.setWalkSpeed(this._walkSpeed);
		player.setFlySpeed(this._flySpeed);
		player.setFireTicks(this._fireTicks);
		player.setFoodLevel(this._foodLevel);
		try {
			player.setFlying(this._isFlying);
		} catch (Exception e) {}
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

	public void telePortBack() {
		Player player = getPlayer();
		if (!player.isOnline()) return;
		if (this._location.distance(player.getLocation()) < 1.0) return;
		this._canTeleport = true;
		player.teleport(this._location);
		this._canTeleport = false;
	}

	public boolean canTeleport() { return this._canTeleport; }
}
