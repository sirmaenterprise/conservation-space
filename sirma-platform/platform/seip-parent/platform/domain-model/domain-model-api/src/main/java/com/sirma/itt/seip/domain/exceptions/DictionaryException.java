package com.sirma.itt.seip.domain.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Base Exception of Data Dictionary Exceptions.
 *
 * @author BBonev
 */
public class DictionaryException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7161810792360663669L;

	/**
	 * Instantiates a new dictionary exception.
	 *
	 * @param msgId
	 *            the msg id
	 */
	public DictionaryException(String msgId) {
		super(msgId);
	}

	/**
	 * Instantiates a new dictionary exception.
	 *
	 * @param msgId
	 *            the msg id
	 * @param cause
	 *            the cause
	 */
	public DictionaryException(String msgId, Throwable cause) {
		super(msgId, cause);
	}
}
