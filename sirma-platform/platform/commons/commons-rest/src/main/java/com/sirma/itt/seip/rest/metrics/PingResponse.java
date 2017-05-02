package com.sirma.itt.seip.rest.metrics;

import java.util.Objects;

/**
 * Contains information about the system's state.
 * 
 * @author yasko
 */
public class PingResponse {
	/**
	 * Ping general status constants.
	 * 
	 * @author yasko
	 */
	public enum PingStatus {
		OK
	}
	
	private PingStatus status;

	public PingStatus getStatus() {
		return status;
	}

	public void setStatus(PingStatus status) {
		this.status = status;
	}
	
	/**
	 * Constructs a {@link PingResponse} from a {@link PingStatus}.
	 * 
	 * @param status
	 *            Ping status to use for the response.
	 * @return the constructed {@link PingResponse}.
	 */
	public static PingResponse fromStatus(PingStatus status) {
		Objects.requireNonNull(status, "Ping status is mendatory");
		PingResponse pong = new PingResponse();
		pong.setStatus(status);
		return pong;
	}
}
