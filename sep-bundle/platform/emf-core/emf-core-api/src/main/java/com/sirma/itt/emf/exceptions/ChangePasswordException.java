package com.sirma.itt.emf.exceptions;

import java.util.Map;

import com.sirma.itt.emf.rest.EmfApplicationException;

/**
 * Thrown when there was a problem with changing a users password.
 * 
 * @author yasko
 * 
 */
public class ChangePasswordException extends EmfApplicationException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message
	 *            Exception message.
	 * @param messages
	 *            Map with messages
	 */
	public ChangePasswordException(String message, Map<String, String> messages) {
		super(message, messages);
	}

}
