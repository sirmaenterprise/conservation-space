package com.sirma.sep.ocr.exception;

/**
 * The {@link OCRFailureException} indicates error during OCR process with the specified description and cause.
 *
 * @author bbanchev
 */
public class OCRFailureException extends Exception {

	private static final long serialVersionUID = -2547409536386682003L;

	/**
	 * Instantiates a new OCR failure exception.
	 *
	 * @param msg the description of the failure
	 * @param cause the exact cause for the failure
	 */
	public OCRFailureException(String msg, Exception cause) {
		super(msg, cause);
	}

	/**
	 * Instantiates a new OCR failure exception.
	 *
	 * @param msg the description of the failure
	 */
	public OCRFailureException(String msg) {
		super(msg);
	}

	/**
	 * Instantiates a new OCR failure exception.
	 *
	 * @param cause the exact cause for the failure
	 */
	public OCRFailureException(Exception cause) {
		super(cause);
	}
}
