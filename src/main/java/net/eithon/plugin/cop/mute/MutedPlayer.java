package net.eithon.plugin.cop.mute;

import java.time.LocalDateTime;

public class MutedPlayer {
	private LocalDateTime _endTime;
	private long _timeInSeconds;
	private String _reason;

	public MutedPlayer(long timeInSeconds, String reason) {
		this._endTime = LocalDateTime.now().plusSeconds(timeInSeconds);
		this._timeInSeconds = timeInSeconds;
		this._reason = reason;
	}

}
