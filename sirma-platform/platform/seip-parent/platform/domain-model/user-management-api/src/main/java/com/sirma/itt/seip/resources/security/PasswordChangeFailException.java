package com.sirma.itt.seip.resources.security;

import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Thrown when password does not pass validation or its wrong (not the same as in the platform).
 *
 * @author smustafov
 */
public class PasswordChangeFailException extends SecurityException {

	private static final long serialVersionUID = -2433890759219511458L;

	private final PasswordFailType type;

	/**
	 * Instantiates a new password change fail exception.
	 *
	 * @param type
	 *            type of the password fail type
	 * @param message
	 *            the exception message
	 */
	public PasswordChangeFailException(PasswordFailType type, String message) {
		this(type, message, null);
	}

	/**
	 * Instantiates a new password change fail exception.
	 *
	 * @param type
	 *            type of the password fail type
	 * @param causedBy
	 *            the caused exception
	 */
	public PasswordChangeFailException(PasswordFailType type, Throwable causedBy) {
		this(type, null, causedBy);
	}

	/**
	 * Instantiates a new password change fail exception.
	 *
	 * @param type
	 *            type of the password fail type
	 * @param message
	 *            the exception message
	 * @param causedBy
	 *            caused by
	 */
	public PasswordChangeFailException(PasswordFailType type, String message, Throwable causedBy) {
		super(message, causedBy);
		this.type = type;
	}

	/**
	 * Types of failure in password changing process.
	 *
	 * @author smustafov
	 */
	public enum PasswordFailType {
		SHORT_PASSWORD, LONG_PASSWORD, SAME_PASSWORD, NEW_PASSWORD_EMPTY, OLD_PASSWORD_EMPTY, WRONG_OLD_PASSWORD, UNKNOWN_TYPE
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public PasswordFailType getType() {
		return type;
	}
}
