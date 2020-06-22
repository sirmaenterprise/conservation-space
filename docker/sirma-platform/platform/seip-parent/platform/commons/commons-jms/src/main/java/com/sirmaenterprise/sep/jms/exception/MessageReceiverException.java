package com.sirmaenterprise.sep.jms.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirmaenterprise.sep.jms.api.MessageReceiverResponse;

/**
 * Message receiver exception indicating that something went wrong during a jms message
 * receive/consume. This exception must be thrown so the execution flow can properly terminate the
 * transaction.
 * 
 * @author nvelkov
 */
public class MessageReceiverException extends EmfRuntimeException {

	private static final long serialVersionUID = -7935472184614355002L;

	private final MessageReceiverResponse response;

	/**
	 * Initialize a message receiver exception.
	 * 
	 * @param response
	 *            the response that spawned the exception
	 */
	public MessageReceiverException(MessageReceiverResponse response) {
		super();
		this.response = response;
	}

	/**
	 * Initialize a message receiver exception
	 * 
	 * @param response
	 *            the response that spawned the exception
	 * @param causedBy
	 *            the cause exception
	 */
	public MessageReceiverException(MessageReceiverResponse response, Throwable causedBy) {
		super(causedBy);
		this.response = response;
	}

	/**
	 * Get the response.
	 * 
	 * @return the response
	 */
	public MessageReceiverResponse getResponse() {
		return response;
	}
}
