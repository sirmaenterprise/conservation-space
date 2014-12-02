package com.sirma.itt.cmf.services.adapter;

import java.util.List;

import com.sirma.itt.emf.adapter.CMFAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.security.model.Group;
import com.sirma.itt.emf.security.model.User;

/**
 * Adapter service for accessing the groups from the underlying DM or other system. It is
 * responsible to servce requests to DMS regarding authorities.
 *
 * @author bbanchev
 */
public interface CMFGroupService extends CMFAdapterService {

	/**
	 * Gets all groups without filtering
	 *
	 * @return all groups from DMS
	 * @throws DMSException
	 *             on any error
	 */
	List<Group> getAllGroups() throws DMSException;

	/**
	 * Gets the filtered groups.
	 *
	 * @param filter
	 *            the filter pattern ( including * or ? )
	 * @return the filtered groups or empty collection
	 * @throws DMSException
	 *             on any error
	 */
	List<Group> getFilteredGroups(String filter) throws DMSException;

	/**
	 * Find group by matching entry part
	 *
	 * @param groupId
	 *            the group id
	 * @return the first group or null if not found
	 * @throws DMSException
	 *             on any error
	 */
	Group findGroup(String groupId) throws DMSException;

	/**
	 * Gets the authorities.
	 *
	 * @param user
	 *            the user to get groups for
	 * @return list of groups for user
	 * @throws DMSException
	 *             on any error
	 */
	List<Group> getAuthorities(User user) throws DMSException;

	/**
	 * Gets the users for authority.
	 *
	 * @param group
	 *            the group to search in
	 * @return list of user for group
	 * @throws DMSException
	 *             on any error
	 */
	List<String> getUsersInAuthority(Group group) throws DMSException;

}
