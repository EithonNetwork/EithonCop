package net.eithon.plugin.cop.spam;

import java.time.LocalDateTime;

import net.eithon.plugin.cop.Config;

class OldLine {

	private String _line;
	private LocalDateTime _time;
	private int _duplicates;

	OldLine(String line) {
		this._line = line;
		this._time = LocalDateTime.now();
		this._duplicates = 0;
	}

	boolean isTooOld() {
		return this._time.plusSeconds(Config.V.secondsToRememberLines).isBefore(LocalDateTime.now());
	}

	String getLine() { return this._line; }

	public void addDuplicate() { this._duplicates++; }

	public int numberOfDuplicates() { return this._duplicates; }
}
