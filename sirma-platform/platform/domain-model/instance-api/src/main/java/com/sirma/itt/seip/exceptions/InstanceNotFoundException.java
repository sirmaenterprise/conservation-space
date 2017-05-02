package com.sirma.itt.seip.exceptions;

import java.io.Serializable;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Generic exception for not found instance. Should be thrown by internal instance services used for retrieving
 * instances.
 *
 * @author A. Kunchev
 */
public class InstanceNotFoundException extends EmfRuntimeException {

	private static final long serialVersionUID = -8495028000089263269L;

	/**
	 * Default constructor.
	 *
	 * @param <S>
	 *            generic type
	 * @param instanceId
	 *            the id of the instance that is not found
	 */
	public <S extends Serializable> InstanceNotFoundException(S instanceId) {
		super("Could not find instance with id - " + instanceId);
	}

	/**
	 * Constructor with option to pass cause.
	 *
	 * @param cause
	 *            the cause led to this exception
	 */
	public InstanceNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with option for cause and message.
	 *
	 * @param message
	 *            the message for the exception
	 * @param cause
	 *            the cause let to this exception
	 */
	public InstanceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
