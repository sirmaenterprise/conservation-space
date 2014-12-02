package com.sirma.itt.cmf.services.adapter;

import java.util.List;

import com.sirma.itt.emf.adapter.CMFAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.security.model.User;

/**
 * Adapter service for accessing the users from the underlying DM or other system
 *
 * @author BBonev
 */
public interface CMFUserService extends CMFAdapterService {
	/**
	 * Gets the all registered users.
	 *
	 * @return the all users
	 */
	List<User> getAllUsers() throws DMSException;

	/**
	 * Gets the users that match the given filter.
	 *
	 * @param filter
	 *            the filter
	 * @return the filtered users
	 * @throws DMSException
	 */
	List<User> getFilteredUsers(String filter) throws DMSException;

	/**
	 * Gets the user role for the given site.
	 *
	 * @param userId
	 *            the user id
	 * @param siteId
	 *            the site id
	 * @return the user role or <code>null</code> if not part of the given site
	 * @throws DMSException
	 *             the dMS exception
	 */
	String getUserRole(String userId, String siteId) throws DMSException;

	/**
	 * Finds a user with forced synchronization if not exists in dms.
	 *
	 * @param userId
	 *            is the user to find in dms
	 * @return the user or throws {@link DMSException} if not found or on any error
	 * @throws DMSException
	 *             if user not found or on any error
	 */
	User findUser(String userId) throws DMSException;

}
