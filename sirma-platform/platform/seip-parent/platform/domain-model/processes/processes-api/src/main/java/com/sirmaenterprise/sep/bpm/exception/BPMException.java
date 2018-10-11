package com.sirmaenterprise.sep.bpm.exception;

import com.sirma.itt.seip.exception.EmfException;

/**
 * The {@link BPMException} indicates general exception during operations on business process engine.
 *
 * @author Borislav Banchev
 */
public class BPMException extends EmfException {

	private static final long serialVersionUID = -6342842316803069798L;

	/**
	 * Instantiates a new Business Process exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param enableSuppression
	 *            the enable suppression
	 * @param writableStackTrace
	 *            the writable stack trace
	 */
	public BPMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new Business Process exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public BPMException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new Business Process exception.
	 *
	 * @param message
	 *            the message
	 */
	public BPMException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new Business Process exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public BPMException(Throwable cause) {
		super(cause);
	}

}
