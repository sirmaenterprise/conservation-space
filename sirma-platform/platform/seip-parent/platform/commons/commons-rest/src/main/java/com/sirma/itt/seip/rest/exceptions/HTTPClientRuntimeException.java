package com.sirma.itt.seip.rest.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Thrown when an error occurs during invocation of {@link com.sirma.itt.seip.rest.client.HTTPClient}.
 *
 * @author smustafov
 */
public class HTTPClientRuntimeException extends EmfRuntimeException {

	/**
	 * Constructs new runtime http client exception with null message.
	 */
	public HTTPClientRuntimeException() {
		super();
	}

	/**
	 * Constructs new runtime http client exception with the given message.
	 *
	 * @param message the exception message
	 */
	public HTTPClientRuntimeException(String message) {
		super(message);
	}

	/**
	 * Constructs new runtime http client exception with the given cause.
	 *
	 * @param cause the caused exception
	 */
	public HTTPClientRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs new runtime http client exception with the given message and cause.
	 *
	 * @param message the exception message
	 * @param cause   the caused exception
	 */
	public HTTPClientRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
