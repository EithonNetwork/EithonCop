package net.eithon.plugin.cop.logic;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.facades.PermissionsFacade;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FrozenPlayer {

	private static final String EITHONBUNGEE_ACCESS_SERVER = "eithonbungee.access.server";
	private EithonPlayer _eithonPlayer;
	private float _walkSpeed;
	private float _flySpeed;
	private int _fireTicks;
	private int _foodLevel;
	private boolean _isFlying;
	private Location _location;
	private boolean _canTeleport;
	private boolean _allowFlight;
	private boolean _isFrozen;
	private boolean _hasAccessServerPermission;

	public FrozenPlayer(Player player) {
		this._isFrozen = false;
		this._eithonPlayer = new EithonPlayer(player);
		this._location = player.getLocation();
		freeze();
		this._isFrozen = true;
	}

	public Player getPlayer() { return this._eithonPlayer.getPlayer();	}
	public String getName() { return this._eithonPlayer.getName(); }

	public void freeze() {
		Player player = getPlayer();
		this._hasAccessServerPermission = player.hasPermission(EITHONBUNGEE_ACCESS_SERVER);
		this._canTeleport = false;
		this._allowFlight = player.getAllowFlight();
		this._isFlying = player.isFlying();
		this._walkSpeed = player.getWalkSpeed();
		this._flySpeed = player.getFlySpeed();
		this._fireTicks = player.getFireTicks();
		this._foodLevel = player.getFoodLevel();
		refreeze();
	}

	public void refreeze() {
		Player player = getPlayer();
		try {
			player.setAllowFlight(true);
			player.setFlying(true);
		} catch (Exception e) {}
		player.setWalkSpeed(0);
		player.setFlySpeed(0);
		player.setFireTicks(0);
		player.setFoodLevel(20);
		PermissionsFacade.removePlayerPermissionAsync(player, EITHONBUNGEE_ACCESS_SERVER);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
	}

	public void thaw() {
		if (!this._eithonPlayer.isOnline()) return;
		this._isFrozen = false;
		this._canTeleport = true;
		Player player = getPlayer();
		if (this._hasAccessServerPermission) PermissionsFacade.addPlayerPermissionAsync(player, EITHONBUNGEE_ACCESS_SERVER);
		player.setWalkSpeed(this._walkSpeed);
		player.setFlySpeed(this._flySpeed);
		player.setFireTicks(this._fireTicks);
		player.setFoodLevel(this._foodLevel);
		try {
			player.setAllowFlight(this._allowFlight);
			player.setFlying(this._isFlying);
		} catch (Exception e) {}
		player.removePotionEffect(PotionEffectType.JUMP);
	}

	public static void restore(Player player, float walkSpeed, float flySpeed) {
		if (!player.isOnline()) return;
		try {
			player.setWalkSpeed(walkSpeed);
		} catch (Exception e) {}
		try {
			player.setFlySpeed(flySpeed);
		} catch (Exception e) {}
		player.setFireTicks(0);
		player.setFoodLevel(20);
		PermissionsFacade.addPlayerPermissionAsync(player, EITHONBUNGEE_ACCESS_SERVER);
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

	public boolean isFrozen() {
		return this._isFrozen;
	}
}
