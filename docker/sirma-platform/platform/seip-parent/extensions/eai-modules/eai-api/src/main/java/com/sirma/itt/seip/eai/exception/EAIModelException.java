package com.sirma.itt.seip.eai.exception;

/**
 * The EAI model exception indicates an error during model parsing/generation. Wraps the original cause.
 * 
 * @author bbanchev
 */
public class EAIModelException extends EAIException {
	/** serialVersionUID. */
	private static final long serialVersionUID = -8837350242292187915L;

	/**
	 * Instantiates a new EAI model exception.
	 *
	 * @param message
	 *            the details of exception
	 * @param cause
	 *            the cause of exception
	 */
	public EAIModelException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new EAI model exception.
	 *
	 * @param message
	 *            the details of exception
	 */
	public EAIModelException(String message) {
		super(message);
	}
}
