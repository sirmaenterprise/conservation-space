package com.sirma.itt.seip.patch.exception;

import com.sirma.itt.seip.exception.EmfException;

/**
 * The PatchFailedException indicates error during patch execution. Wraps the actual error.
 *
 * @author bbanchev
 */
public class PatchFailureException extends EmfException {

	/** serialVersionUID. */
	private static final long serialVersionUID = -3635741868501582646L;

	/**
	 * Instantiates a new patch failed exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public PatchFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new patch failed exception.
	 *
	 * @param message
	 *            the message
	 */
	public PatchFailureException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new patch failed exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public PatchFailureException(Throwable cause) {
		super(cause);
	}

}
