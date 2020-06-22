/**
 *
 */
package com.sirma.itt.seip.exception;

import javax.ejb.ApplicationException;

/**
 * General checked application exception that should trigger rollback if thrown in a transaction.
 *
 * @author BBonev
 */
@ApplicationException(rollback = true)
public class RollbackedException extends Exception {
	private static final long serialVersionUID = -4200840674769461131L;

	/**
	 * Instantiates a new rollbacked exception.
	 */
	public RollbackedException() {
		super();
	}

	/**
	 * Instantiates a new rollbacked exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public RollbackedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new rollbacked exception.
	 *
	 * @param message
	 *            the message
	 */
	public RollbackedException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new rollbacked exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public RollbackedException(Throwable cause) {
		super(cause);
	}

}
