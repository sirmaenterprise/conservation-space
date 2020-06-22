package com.sirma.itt.seip.resources;

import com.sirma.itt.seip.exception.EmfException;

/**
 * Exception thrown to indicate a problem when accessing the remote user store so the requested operation cannot
 * complete and the caller should take appropriate action.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 03/08/2017
 */
public class RemoteStoreException extends EmfException {

	/**
	 * Instantiate new exception with the given message
	 *
	 * @param message the message to set
	 */
	public RemoteStoreException(String message) {
		super(message);
	}

	/**
	 * Instantiate new exception for the given cause
	 *
	 * @param cause the cause to set
	 */
	public RemoteStoreException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiate new exception with the given message and cause
	 *
	 * @param message the message to set
	 * @param cause the cause to set
	 */
	public RemoteStoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
