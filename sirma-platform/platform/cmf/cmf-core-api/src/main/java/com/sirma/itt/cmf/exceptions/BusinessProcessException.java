package com.sirma.itt.cmf.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Indicated error in business process execution.
 *
 * @author bbanchev
 */
public class BusinessProcessException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6014364492474632343L;
 
	/**
	 * Instantiates a new business process exception.
	 *
	 * @param message the message
	 */
	public BusinessProcessException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new business process exception.
	 *
	 * @param causedBy the caused by
	 */
	public BusinessProcessException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new business process exception.
	 *
	 * @param message the message
	 * @param causedBy the caused by
	 */
	public BusinessProcessException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
