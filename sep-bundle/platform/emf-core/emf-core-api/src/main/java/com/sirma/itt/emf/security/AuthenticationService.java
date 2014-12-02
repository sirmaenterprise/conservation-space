package com.sirma.itt.emf.security;

import com.sirma.itt.emf.security.model.User;


/**
 * Authentication Service that provides information for the current
 * authentication session.
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
	 * Sets the authenticated user. This also overrides the current effective
	 * authentication.
	 * 
	 * @param user
	 *            the new authenticated user
	 */
	void setAuthenticatedUser(User user);

	/**
	 * Sets the effective authentication only without modifying the actual
	 * authentication.
	 * 
	 * @param user
	 *            the new effective authentication
	 */
	void setEffectiveAuthentication(User user);

	/**
	 * Gets the currently logged in user ID.
	 * 
	 * @return the current user
	 */
	String getCurrentUserId();

	/**
	 * Gets the current CMF container. This is the ID of the container that the
	 * new cases will be created. Also used to filter the definitions for cases
	 * and workflows that can be created. If not container is specified into the
	 * context then a default will be returned if specified into the
	 * configuration. If not configured default container <code>null</code>
	 * should be returned.
	 * <p>
	 * In terms of DMS (Alfresco) this is the containing site short name.
	 * 
	 * @return the current container
	 */
	String getCurrentContainer();

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
	 * Modify the current security context to be authenticated as admin user
	 * (fetched from configuration).
	 */
	void authenticateAsAdmin();

	/**
	 * Checks if is current user is the given user.
	 * 
	 * @param userId
	 *            the user id
	 * @return true, if is current user
	 */
	boolean isCurrentUser(String userId);
}
