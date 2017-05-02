package com.sirma.itt.seip.synchronization;

import com.sirma.itt.seip.exception.EmfException;

/**
 * Exception thrown to indicate a problem with synchronization process.
 *
 * @author BBonev
 */
public class SynchronizationException extends EmfException {

	private static final long serialVersionUID = -3668330220515491071L;

	/**
	 * Instantiates a new synchronization exception.
	 */
	public SynchronizationException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new synchronization exception.
	 *
	 * @param message
	 *            the message
	 */
	public SynchronizationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new synchronization exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public SynchronizationException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new synchronization exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public SynchronizationException(String message, Throwable causedBy) {
		super(message, causedBy);
	}
}
