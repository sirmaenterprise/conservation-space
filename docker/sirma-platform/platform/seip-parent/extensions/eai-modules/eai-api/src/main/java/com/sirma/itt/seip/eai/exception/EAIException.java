package com.sirma.itt.seip.eai.exception;

import com.sirma.itt.seip.exception.EmfException;

/**
 * The EAI general checked exception indicates an error in any phase of integration process
 * 
 * @author bbanchev
 */
public class EAIException extends EmfException {

	/** serialVersionUID. */
	private static final long serialVersionUID = -8215254379633498706L;

	/**
	 * Instantiates a new EAI general exception.
	 *
	 * @param message
	 *            the details of exception
	 * @param cause
	 *            the cause of exception
	 */
	public EAIException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new EAI general exception.
	 *
	 * @param message
	 *            the details of exception
	 */
	public EAIException(String message) {
		super(message);
	}

}
