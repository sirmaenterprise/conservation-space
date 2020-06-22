package com.sirma.itt.seip.eai.content.tool.exception;

/**
 * The EAI general runtime exception indicates an error in any phase of integration process
 * 
 * @author bbanchev
 */
public class EAIRuntimeException extends RuntimeException {

	/** serialVersionUID. */
	private static final long serialVersionUID = -8215254379633498706L;

	/**
	 * Instantiates a new EAI general runtime exception.
	 *
	 * @param message
	 *            the details of exception
	 * @param cause
	 *            the cause of exception
	 */
	public EAIRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new EAI general runtime exception.
	 *
	 * @param message
	 *            the details of exception
	 */
	public EAIRuntimeException(String message) {
		super(message);
	}
}
