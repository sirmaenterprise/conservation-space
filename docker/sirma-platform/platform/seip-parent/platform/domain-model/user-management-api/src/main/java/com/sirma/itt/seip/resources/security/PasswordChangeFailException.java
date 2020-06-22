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
	private final String policyValue;

	/**
	 * Instantiates a new password change fail exception.
	 *
	 * @param type    type of the password fail type
	 * @param message the exception message
	 */
	public PasswordChangeFailException(PasswordFailType type, String message) {
		this(type, null, message, null);
	}

	/**
	 * Instantiates a new password change fail exception.
	 *
	 * @param type        type of the password fail type
	 * @param policyValue the value of the policy value
	 * @param message     the exception message
	 */
	public PasswordChangeFailException(PasswordFailType type, String policyValue, String message) {
		this(type, policyValue, message, null);
	}

	/**
	 * Instantiates a new password change fail exception.
	 *
	 * @param type     type of the password fail type
	 * @param causedBy the caused exception
	 */
	public PasswordChangeFailException(PasswordFailType type, Throwable causedBy) {
		this(type, null, null, causedBy);
	}

	/**
	 * Instantiates a new password change fail exception with given type, policy value and cause.
	 *
	 * @param type        type of the password fail type
	 * @param policyValue the value of the policy value
	 * @param causedBy    the caused exception
	 */
	public PasswordChangeFailException(PasswordFailType type, String policyValue, Throwable causedBy) {
		this(type, policyValue, null, causedBy);
	}

	/**
	 * Instantiates a new password change fail exception.
	 *
	 * @param type     type of the password fail type
	 * @param message  the exception message
	 * @param causedBy caused by
	 */
	public PasswordChangeFailException(PasswordFailType type, String policyValue, String message, Throwable causedBy) {
		super(message, causedBy);
		this.type = type;
		this.policyValue = policyValue;
	}

	/**
	 * Types of failure in password changing process.
	 *
	 * @author smustafov
	 */
	public enum PasswordFailType {
		SHORT_PASSWORD("change.password.errors.short.pass"),
		LONG_PASSWORD("change.password.errors.long.pass"),
		SAME_PASSWORD("change.password.errors.same"),
		NEW_PASSWORD_EMPTY("change.password.errors.new_required"),
		OLD_PASSWORD_EMPTY("change.password.errors.current_required"),
		WRONG_OLD_PASSWORD("change.password.errors.wrong"),
		UNKNOWN_TYPE("change.password.errors.generic"),
		MIN_LENGTH("change.password.errors.min.length"),
		MIN_DIGITS("change.password.errors.min.digits"),
		MIN_LOWER_CASE_CHARS("change.password.errors.min.lower.case"),
		MIN_UPPER_CASE_CHARS("change.password.errors.min.upper.case"),
		MIN_SPECIAL_CHARS("change.password.errors.min.special.case"),
		NOT_USERNAME("change.password.errors.not.username"),
		HISTORY("change.password.errors.history"),
		BLACKLISTED("change.password.errors.blacklisted");

		private final String labelKey;

		PasswordFailType(String labelKey) {
			this.labelKey = labelKey;
		}

		public String getLabelKey() {
			return labelKey;
		}
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public PasswordFailType getType() {
		return type;
	}

	public String getPolicyValue() {
		return policyValue;
	}
}
