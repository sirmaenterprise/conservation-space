package com.sirmaenterprise.sep.eai.spreadsheet.exception;

import com.sirma.itt.seip.eai.exception.EAIReportableException;

/**
 * Defines exception for not supported by the spreadsheet EAI API content.
 *
 * @author bbanchev
 */
public class EAIUnsupportedContentException extends EAIReportableException {

	private static final long serialVersionUID = 1639100871605771742L;

	/**
	 * Instantiates a new unsupported content exception.
	 *
	 * @param message
	 *            the details of exception
	 * @param cause
	 *            the cause of exception
	 */
	public EAIUnsupportedContentException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new unsupported content exception.
	 *
	 * @param message
	 *            the details of exception
	 */
	public EAIUnsupportedContentException(String message) {
		super(message);
	}

}
