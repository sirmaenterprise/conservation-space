package com.sirma.sep.keycloak.synchronization;

/**
 * Defines actions that users should do if set. These actions can be triggered on user login or via sending email.
 *
 * @author smustafov
 */
public enum UserActions {

	/**
	 * The user must verify that they have a valid email account. An email will be sent to the user with a link they
	 * have to click.
	 */
	VERIFY_EMAIL,

	/**
	 * The user must update their profile information, i.e. their name, address, email, and/or phone number.
	 */
	UPDATE_PROFILE,

	/**
	 * The user must configure a one-time password generator on their mobile device (Two-Factor authentication).
	 */
	CONFIGURE_TOTP,

	/**
	 * The user must change their password.
	 */
	UPDATE_PASSWORD,

	/**
	 * The user must agree to the terms and conditions of the application.
	 */
	TERMS_AND_CONDITIONS

}
