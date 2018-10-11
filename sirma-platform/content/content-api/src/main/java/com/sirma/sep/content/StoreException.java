/**
 *
 */
package com.sirma.sep.content;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception to indicate a problem with the {@link ContentStore} operation.
 *
 * @author BBonev
 */
public class StoreException extends EmfRuntimeException {

	private static final long serialVersionUID = -8860370270440618830L;

	/**
	 * Instantiates a new store exception.
	 */
	public StoreException() {
		// just default constuctor
	}

	/**
	 * Instantiates a new store exception.
	 *
	 * @param message
	 *            the message
	 */
	public StoreException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new store exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public StoreException(Throwable causedBy) {
		super(causedBy);
	}

	/**
	 * Instantiates a new store exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public StoreException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

}
