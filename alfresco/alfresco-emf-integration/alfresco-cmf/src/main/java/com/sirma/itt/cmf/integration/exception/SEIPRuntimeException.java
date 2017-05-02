package com.sirma.itt.cmf.integration.exception;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * The SEIPRuntimeException indicates any error in seip integration
 * 
 * @author bbanchev
 */
public class SEIPRuntimeException extends AlfrescoRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8450279925819069065L;

	/**
	 * Instantiates a new SEIP runtime exception.
	 *
	 * @param msgId
	 *            the msg id
	 * @param msgParams
	 *            the msg params
	 * @param cause
	 *            the cause
	 */
	public SEIPRuntimeException(String msgId, Object[] msgParams, Throwable cause) {
		super(msgId, msgParams, cause);
	}

	/**
	 * Instantiates a new SEIP runtime exception.
	 *
	 * @param msgId
	 *            the msg id
	 * @param msgParams
	 *            the msg params
	 */
	public SEIPRuntimeException(String msgId, Object[] msgParams) {
		super(msgId, msgParams);
	}

	/**
	 * Instantiates a new SEIP runtime exception.
	 *
	 * @param msgId
	 *            the msg id
	 * @param cause
	 *            the cause
	 */
	public SEIPRuntimeException(String msgId, Throwable cause) {
		super(msgId, cause);
	}

	/**
	 * Instantiates a new SEIP runtime exception.
	 *
	 * @param msgId
	 *            the msg id
	 */
	public SEIPRuntimeException(String msgId) {
		super(msgId);
	}

}
