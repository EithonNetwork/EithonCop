package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.logic.Controller;

public final class Plugin extends EithonPlugin {
	private Controller _controller;
	private EventListener _eventListener;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		this._controller = new Controller(this);
		this._eventListener = new EventListener(this, this._controller);
		super.activate(null, this._eventListener);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this._controller = null;
	}
}
