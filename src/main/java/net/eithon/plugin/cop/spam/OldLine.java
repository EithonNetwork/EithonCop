package net.eithon.plugin.cop.spam;

import java.time.LocalDateTime;

import net.eithon.plugin.cop.Config;

class OldLine {

	private String _line;
	private LocalDateTime _time;

	OldLine(String line) {
		this._line = line;
		this._time = LocalDateTime.now();
	}

	boolean isTooOld() {
		return this._time.plusSeconds(Config.V.secondsToRememberLines).isBefore(LocalDateTime.now());
	}

	String getLine() { return this._line; }
}
