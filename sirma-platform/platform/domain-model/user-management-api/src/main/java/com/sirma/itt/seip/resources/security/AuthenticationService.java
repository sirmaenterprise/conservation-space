package com.sirma.itt.seip.resources.security;

import com.sirma.itt.seip.resources.User;

/**
 * Authentication Service that provides information for the current authentication session.
 *
 * @author BBonev
 */
public interface AuthenticationService {

	/**
	 * Indicates that the current user is authenticated.
	 *
	 * @return true if the user is logged in, false otherwise.
	 */
	boolean isAuthenticated();

	/**
	 * Gets the currently logged in user ID.
	 *
	 * @return the current user
	 */
	String getCurrentUserId();

	/**
	 * Gets the current tenant id for the authenticated user
	 *
	 * @return the current tenant id
	 */
	String getCurrentTenantId();

	/**
	 * Gets the currently authenticated user.
	 *
	 * @return the current user
	 */
	User getCurrentUser();

	/**
	 * Gets the effective authentication.
	 *
	 * @return the effective authentication
	 */
	User getEffectiveAuthentication();

	/**
	 * Checks if is current user is the given user.
	 *
	 * @param userId
	 *            the user id
	 * @return true, if is current user
	 */
	boolean isCurrentUser(String userId);
}
