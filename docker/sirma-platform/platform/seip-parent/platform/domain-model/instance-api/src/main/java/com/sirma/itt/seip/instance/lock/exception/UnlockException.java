package com.sirma.itt.seip.instance.lock.exception;

import java.util.Map;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.instance.lock.LockInfo;

/**
 * Exception thrown to identify an access error while performing unlock operations.
 *
 * @author BBonev
 */
public class UnlockException extends EmfApplicationException {

	private static final long serialVersionUID = -5341950387105457852L;

	private final LockInfo lockInfo;

	/**
	 * Instantiates a new unlock exception.
	 *
	 * @param lockInfo
	 *            the lock info
	 * @param message
	 *            the message
	 */
	public UnlockException(LockInfo lockInfo, String message) {
		super(message);
		this.lockInfo = lockInfo;
	}

	/**
	 * Instantiates a new unlock exception.
	 *
	 * @param lockInfo
	 *            the lock info
	 * @param message
	 *            the message
	 * @param messages
	 *            the messages
	 */
	public UnlockException(LockInfo lockInfo, String message, Map<String, String> messages) {
		super(message, messages);
		this.lockInfo = lockInfo;
	}

	/**
	 * Instantiates a new unlock exception.
	 *
	 * @param lockInfo
	 *            the lock info
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public UnlockException(LockInfo lockInfo, String message, Throwable cause) {
		super(message, cause);
		this.lockInfo = lockInfo;
	}

	/**
	 * Instantiates a new unlock exception.
	 *
	 * @param lockInfo
	 *            the lock info
	 * @param message
	 *            the message
	 * @param messages
	 *            the messages
	 * @param cause
	 *            the cause
	 */
	public UnlockException(LockInfo lockInfo, String message, Map<String, String> messages, Throwable cause) {
		super(message, messages, cause);
		this.lockInfo = lockInfo;
	}

	/**
	 * Gets the lock info that triggered the current exception.
	 *
	 * @return the lock info
	 */
	public LockInfo getLockInfo() {
		return lockInfo;
	}

}
