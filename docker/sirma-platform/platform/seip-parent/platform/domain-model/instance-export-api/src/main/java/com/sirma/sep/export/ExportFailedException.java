package com.sirma.sep.export;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Runtime exception thrown when export process fails.
 *
 * @author A. Kunchev
 */
public class ExportFailedException extends EmfRuntimeException {

	private static final long serialVersionUID = -2937735349398471655L;

	/**
	 * Instantiates a new export failed exception.
	 */
	public ExportFailedException() {
		// empty
	}

	/**
	 * Instantiates a new export failed exception with message.
	 *
	 * @param message
	 *            the exception message
	 */
	public ExportFailedException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new export failed exception with cause.
	 *
	 * @param cause
	 *            the cause of the exception
	 */
	public ExportFailedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new export failed exception with message and cause.
	 *
	 * @param message
	 *            the exception message
	 * @param cause
	 *            the cause of the exception
	 */
	public ExportFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
