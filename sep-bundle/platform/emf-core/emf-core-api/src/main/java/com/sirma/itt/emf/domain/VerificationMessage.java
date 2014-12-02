package com.sirma.itt.emf.domain;


/**
 * The Class VerificationError.
 * 
 * @author BBonev
 */
public interface VerificationMessage {

	/**
	 * The error type.
	 *
	 * @return the error type
	 */
	MessageType getErrorType();

	/**
	 * The message.
	 *
	 * @return the message
	 */
	String getMessage();
}
