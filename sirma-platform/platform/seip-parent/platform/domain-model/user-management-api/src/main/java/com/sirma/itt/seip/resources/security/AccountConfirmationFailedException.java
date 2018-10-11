package com.sirma.itt.seip.resources.security;

import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Thrown when account confirmation fails. Carries the type of the failure via {@link ConfirmationFailType}.
 *
 * @author smustafov
 */
public class AccountConfirmationFailedException extends SecurityException {

	private static final long serialVersionUID = -4657120024493268787L;

	private final ConfirmationFailType failType;
	private final String userAccount;

	/**
	 * Instantiates a account confirmation fail exception.
	 *
	 * @param failType type of the confirmation failure
	 * @param userAccount the affected user account
	 */
	public AccountConfirmationFailedException(ConfirmationFailType failType, String userAccount) {
		this(failType, userAccount, null);
	}

	/**
	 * Instantiates a new password change fail exception.
	 *
	 * @param failType type of the confirmation failure
	 * @param userAccount the affected user account
	 * @param causedBy caused by
	 */
	public AccountConfirmationFailedException(ConfirmationFailType failType, String userAccount, Throwable causedBy) {
		super(null, causedBy);
		this.failType = failType;
		this.userAccount = userAccount;
	}

	/**
	 * Getter method for failType.
	 *
	 * @return the failType
	 */
	public ConfirmationFailType getFailType() {
		return failType;
	}

	/**
	 * The affected user account
	 *
	 * @return the user account
	 */
	public String getUserAccount() {
		return userAccount;
	}

	/**
	 * Type of the failure that occurred during account confirmation process.
	 *
	 * @author smustafov
	 */
	public enum ConfirmationFailType {
		/**
		 * This occurs if the user does not exist.
		 */
		USER_DOES_NOT_EXIST("resources.security.accounts.userDoesNotExists"),
		/**
		 * This error occurs when invalid credentials are provided.
		 */
		INVALID_PASSWORD("resources.security.accounts.invalidPassword"),
		/**
		 * Invalid user name is given.
		 */
		INVALID_USERNAME("resources.security.accounts.invalidUserName"),
		/**
		 * The key/confirmation code provided has expired.
		 */
		INVALID_CONFIRMATION_CODE("resources.security.accounts.invalidConfirmationCode"),
		/**
		 * Invalid or expired confirmation code provided to validate.
		 */
		INVALID_OR_EXPIRED_CONFIRMATION_CODE("resources.security.accounts.invalidConfirmationCode"),
		/**
		 * The key/confirmation code provided has expired.
		 */
		VERIFICATION_CODE_EXPIRED("resources.security.accounts.verificationCodeExpired"),
		/**
		 * Captcha answer is invalid.
		 */
		INVALID_CAPTCHA_ANSWER("resources.security.accounts.invalidCaptchaAnswer"),
		/**
		 * This error occurs when an account is locked after multiple incorrect login attempts and the user attempts to log in again.
		 */
		ACCOUNT_LOCKED("resources.security.accounts.accountLocked"),
		/**
		 * Unexpected code provided to validate.
		 */
		UNEXPECTED_CODE("resources.security.accounts.unexpectedCode"),
		/**
		 * Unexpected error
		 */
		UNKNOWN("resources.security.accounts.unknown");

		private final String labelId;

		ConfirmationFailType(String labelId) {
			this.labelId = labelId;
		}

		/**
		 * Returns the label id that represents the current error type
		 *
		 * @return the label id
		 */
		public String getLabelId() {
			return labelId;
		}
	}

}
