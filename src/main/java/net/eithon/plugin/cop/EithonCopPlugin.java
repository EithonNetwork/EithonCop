package net.eithon.plugin.cop;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.cop.logic.Controller;
import net.eithon.plugin.cop.CommandHandler;

public final class EithonCopPlugin extends EithonPlugin {
	private Controller _controller;
	private EventListener _eventListener;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		try {
			this._controller = new Controller(this, new Database(
					Config.V.databaseUrl, Config.V.databaseUsername, Config.V.databasePassword));
		} catch (FatalException e) {
			e.printStackTrace();
		}
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		this._eventListener = new EventListener(this, this._controller);
		this.logInfo("Event listener has been created");
		EithonCopApi.initialize(this._controller);
		super.activate(commandHandler.getCommandSyntax(), this._eventListener);
		this.logInfo("Event listener has been activated");
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this._controller.disable();
		this._controller = null;
	}
}
