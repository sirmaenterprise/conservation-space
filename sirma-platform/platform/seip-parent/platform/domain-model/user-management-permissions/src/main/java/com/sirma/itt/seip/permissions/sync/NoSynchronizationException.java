package com.sirma.itt.seip.permissions.sync;

import com.sirma.itt.seip.exception.EmfException;

/**
 * Exception thrown when synchronization operation is attempted when no initial synchronization is ran.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/07/2017
 */
public class NoSynchronizationException extends EmfException {

	/**
	 * Instantiate new exception with the given message
	 *
	 * @param message the message to pass
	 */
	public NoSynchronizationException(String message) {
		super(message);
	}
}
