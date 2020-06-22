package com.sirma.sep.export;

import com.sirma.itt.seip.exception.EmfException;

/**
 * Indicates an error during content export with wrapped cause if it is not the root cause.
 *
 * @author bbanchev
 */
public class ContentExportException extends EmfException {
	private static final long serialVersionUID = -4486049424240020717L;

	/**
	 * Instantiates a new content export exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public ContentExportException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new content export exception.
	 *
	 * @param message
	 *            the message
	 */
	public ContentExportException(String message) {
		super(message);
	}

}
