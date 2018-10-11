package com.sirmaenterprise.sep.jms.exception;

/**
 * Exception thrown when a connection factory cannot be lookup in the application context.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class ConnectionFactoryNotFoundException extends JmsRuntimeException {

	/**
	 * Instantiate new instance using the given message
	 *
	 * @param message the exception message
	 */
	public ConnectionFactoryNotFoundException(String message) {
		super(message);
	}

	/**
	 * Instantiate new instance using the given cause
	 *
	 * @param causedBy the original exception if any
	 */
	public ConnectionFactoryNotFoundException(Throwable causedBy) {
		super(causedBy);
	}
}
