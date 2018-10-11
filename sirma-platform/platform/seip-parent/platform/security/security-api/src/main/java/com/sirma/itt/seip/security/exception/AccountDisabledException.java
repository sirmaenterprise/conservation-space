package com.sirma.itt.seip.security.exception;

import com.sirma.itt.seip.security.User;

/**
 * Exception thrown during context authentication process if the authenticated resource is not active and not allowed
 * to login. The client should take proper action to handle this case.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 29/09/2017
 */
public class AccountDisabledException extends AuthenticationException {
	private final User identity;

	/**
	 * Instantiate an exception instance for the given user and message
	 *
	 * @param identity the identity of the disabled user
	 * @param message a custom message to accompany the user id
	 */
	public AccountDisabledException(User identity, String message) {
		super(message);
		this.identity = identity;
	}

	/**
	 * Get the disabled user
	 *
	 * @return the distabled user
	 */
	public User getIdentity() {
		return identity;
	}

	@Override
	public String getFailedIdentity() {
		return identity.getIdentityId();
	}
}
