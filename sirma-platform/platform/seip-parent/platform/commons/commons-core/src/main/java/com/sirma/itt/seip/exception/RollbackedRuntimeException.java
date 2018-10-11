/**
 *
 */
package com.sirma.itt.seip.exception;

import javax.ejb.ApplicationException;

/**
 * General runtime application exception that should trigger rollback if thrown in a transaction.
 *
 * @author BBonev
 */
@ApplicationException(rollback = true)
public class RollbackedRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -7655663237797317418L;

	/**
	 * Instantiates a new rollbacked runtime exception.
	 */
	public RollbackedRuntimeException() {
		super();
	}

	/**
	 * Instantiates a new rollbacked runtime exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public RollbackedRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new rollbacked runtime exception.
	 *
	 * @param message
	 *            the message
	 */
	public RollbackedRuntimeException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new rollbacked runtime exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public RollbackedRuntimeException(Throwable cause) {
		super(cause);
	}

}
