package net.eithon.plugin.cop;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.cop.logic.Controller;
import net.eithon.plugin.cop.CommandHandler;

public final class Plugin extends EithonPlugin {
	private Controller _controller;
	private EventListener _eventListener;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		this._controller = new Controller(this);
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		this._eventListener = new EventListener(this, this._controller);
		this.getEithonLogger().info("Event listener has been created");
		EithonCopApi.initialize(this._controller);
		super.activate(commandHandler.getCommandSyntax(), this._eventListener);
		this.getEithonLogger().info("Event listener has been activated");
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this._controller.disable();
		this._controller = null;
	}
}
