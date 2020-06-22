package com.sirma.itt.seip.rest.utils;

/**
 * Constants for error codes to be send to the client.
 * Clients can identify and process errors easily.
 */
public final class ErrorCode {
	private ErrorCode() {

	}

	public static final int TIMEOUT = 1;
	public static final int VALIDATION = 2;
}
