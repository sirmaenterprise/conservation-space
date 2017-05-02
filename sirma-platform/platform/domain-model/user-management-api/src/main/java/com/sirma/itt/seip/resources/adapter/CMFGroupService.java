package com.sirma.itt.seip.resources.adapter;

import java.util.List;

import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;

/**
 * Adapter service for accessing the groups from the underlying DM or other system. It is responsible to service
 * requests to DMS regarding authorities.
 *
 * @author bbanchev
 */
public interface CMFGroupService {

	/**
	 * Gets all groups without filtering
	 *
	 * @return all groups from DMS
	 */
	List<Group> getAllGroups();

	/**
	 * Gets the filtered groups.
	 *
	 * @param filter
	 *            the filter pattern ( including * or ? )
	 * @return the filtered groups or empty collection
	 */
	List<Group> getFilteredGroups(String filter);

	/**
	 * Find group by matching entry part
	 *
	 * @param groupId
	 *            the group id
	 * @return the first group or null if not found
	 */
	Group findGroup(String groupId);

	/**
	 * Gets the authorities.
	 *
	 * @param user
	 *            the user to get groups for
	 * @return list of groups for user
	 */
	List<Group> getAuthorities(Resource user);

	/**
	 * Gets the users for authority.
	 *
	 * @param group
	 *            the group to search in
	 * @return list of user for group
	 */
	List<String> getUsersInAuthority(Group group);

}
