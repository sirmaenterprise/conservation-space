package com.sirmaenterprise.sep.bpm.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The {@link BPMRuntimeException} indicates general runtime exception during operations on business process engine.
 *
 * @author bbanchev
 */
public class BPMRuntimeException extends EmfRuntimeException {

	private static final long serialVersionUID = -4340703367312130645L;

	/**
	 * Instantiates a new Business Process runtime exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public BPMRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new Business Process runtime exception.
	 *
	 * @param message
	 *            the message
	 */
	public BPMRuntimeException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new Business Process runtime exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public BPMRuntimeException(Throwable cause) {
		super(cause);
	}

}
