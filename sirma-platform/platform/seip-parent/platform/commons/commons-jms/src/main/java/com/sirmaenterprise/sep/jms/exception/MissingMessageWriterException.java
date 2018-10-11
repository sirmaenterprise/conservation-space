package com.sirmaenterprise.sep.jms.exception;

/**
 * Exception thrown to indicate the absence of a {@link com.sirmaenterprise.sep.jms.convert.MessageWriter}
 * implementation for a particular type during message sending
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/05/2017
 */
public class MissingMessageWriterException extends JmsRuntimeException {

	/**
	 * Instantiate new exception and set the exception message
	 *
	 * @param message the message to set
	 */
	public MissingMessageWriterException(String message) {
		super(message);
	}
}
