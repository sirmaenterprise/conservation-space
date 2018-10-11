package com.sirmaenterprise.sep.jms.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Base runtime exception thronw by the module on invalid configuration from the JMS API.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class JmsRuntimeException extends EmfRuntimeException {
	/**
	 * Instantiates a new JMS runtime exception.
	 */
	public JmsRuntimeException() {
		// nothing to do
	}

	/**
	 * Instantiates a new JMS runtime exception.
	 *
	 * @param message the exception message
	 */
	public JmsRuntimeException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new JMS runtime exception.
	 *
	 * @param causedBy the caused by.
	 */
	public JmsRuntimeException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new JMS runtime exception.
	 *
	 * @param message the message
	 * @param causedBy the caused by.
	 */
	public JmsRuntimeException(String message, Throwable causedBy) {
		super(message, causedBy);
	}
}
