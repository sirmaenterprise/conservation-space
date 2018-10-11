package com.sirma.itt.seip.resources.adapter;

import java.util.List;

import com.sirma.itt.seip.resources.User;

/**
 * Adapter service for accessing the users from the underlying DM or other system
 *
 * @author BBonev
 */
public interface CMFUserService {
	/**
	 * Gets the all registered users.
	 *
	 * @return the all users
	 */
	List<User> getAllUsers();

	/**
	 * Gets the users that match the given filter.
	 *
	 * @param filter
	 *            the filter
	 * @return the filtered users
	 */
	List<User> getFilteredUsers(String filter);

	/**
	 * Gets the user role for the given site.
	 *
	 * @param userId
	 *            the user id
	 * @param siteId
	 *            the site id
	 * @return the user role or <code>null</code> if not part of the given site
	 */
	String getUserRole(String userId, String siteId);

	/**
	 * Finds a user with forced synchronization if not exists in dms.
	 *
	 * @param userId
	 *            is the user to find in dms
	 * @return the user or throws {@link com.sirma.itt.seip.exception.RollbackedRuntimeException} if not found or on any
	 *         error
	 */
	User findUser(String userId);

}
