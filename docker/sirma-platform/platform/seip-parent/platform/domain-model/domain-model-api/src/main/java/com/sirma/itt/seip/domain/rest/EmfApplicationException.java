/**
 * Copyright (c) 2013 16.09.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.domain.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.ApplicationException;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Special type of exception containing an error message.
 *
 * @author Adrian Mitev
 */
@ApplicationException(rollback = true)
public class EmfApplicationException extends EmfRuntimeException {

	private static final long serialVersionUID = 3333368704683246849L;

	private final Map<String, String> messages;

	/**
	 * Initializes fields.
	 *
	 * @param message
	 *            exception message.
	 */
	public EmfApplicationException(String message) {
		super(message);
		messages = new HashMap<>();
	}

	/**
	 * Initializes fields.
	 *
	 * @param message
	 *            exception message.
	 * @param messages
	 *            exception messages as key-value pairs.
	 */
	public EmfApplicationException(String message, Map<String, String> messages) {
		super(message);
		this.messages = messages;
	}

	/**
	 * Initializes fields.
	 *
	 * @param message
	 *            exception message.
	 * @param cause
	 *            root cause
	 */
	public EmfApplicationException(String message, Throwable cause) {
		super(message, cause);
		messages = new HashMap<>();
	}

	/**
	 * Initializes fields.
	 *
	 * @param message
	 *            exception message.
	 * @param messages
	 *            exception messages as key-value pairs.
	 * @param cause
	 *            root cause
	 */
	public EmfApplicationException(String message, Map<String, String> messages, Throwable cause) {
		super(message, cause);
		this.messages = messages;
	}

	/**
	 * Getter method for messages.
	 *
	 * @return the messages
	 */
	public Map<String, String> getMessages() {
		return messages;
	}

}
