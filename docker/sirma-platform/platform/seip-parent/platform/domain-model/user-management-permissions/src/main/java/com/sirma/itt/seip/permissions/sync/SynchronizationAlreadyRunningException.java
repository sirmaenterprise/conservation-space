package com.sirma.itt.seip.permissions.sync;

import com.sirma.itt.seip.exception.EmfException;

/**
 * Exception thrown when more than one concurrent synchronization is attempted
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/07/2017
 */
public class SynchronizationAlreadyRunningException extends EmfException {

	/**
	 * Instantiate exception with message
	 *
	 * @param message the message to pass
	 */
	public SynchronizationAlreadyRunningException(String message) {
		super(message);
	}
}
