package com.sirma.itt.seip.eai.content.tool.exception;

import java.io.IOException;

/**
 * The EAINetworkException indicates error during communication with external system
 */
public class EAINetworkException extends IOException {

	private static final long serialVersionUID = 1055882137261602630L;

	/**
	 * Instantiates a new EAI network exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public EAINetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new EAI network exception.
	 *
	 * @param message
	 *            the message
	 */
	public EAINetworkException(String message) {
		super(message);
	}

}
