package com.sirma.itt.emf.resources;

import java.util.List;

import javax.enterprise.event.Observes;

import com.sirma.itt.emf.resources.event.GroupSync;
import com.sirma.itt.emf.scheduler.SchedulerAction;
import com.sirma.itt.emf.security.model.Group;
import com.sirma.itt.emf.security.model.User;

/**
 * Defines a methods for retrieving the list of groups.
 *
 * @author bbanchev
 */
public interface GroupService extends SchedulerAction {

	/**
	 * The Enum GroupSorter possible fields.
	 */
	enum GroupSorter {

		/** The display name. */
		DISPLAY_NAME,
		/** The group name. */
		GROUP_NAME;
	}

	/**
	 * Gets the all groups in the system without filtering.
	 *
	 * @return the all groups
	 */
	List<Group> getAllGroups();

	/**
	 * Gets the sorted group by the provided argument.
	 *
	 * @param sortColumn
	 *            the sort column
	 * @return the list of groups after sorting
	 */
	List<Group> getSortedGroup(final GroupSorter sortColumn);

	/**
	 * Find group by id.
	 *
	 * @param groupId
	 *            the group id
	 * @return the emf group
	 */
	Group findGroup(String groupId);

	/**
	 * Gets the authorities for single user.
	 *
	 * @param user
	 *            the user to get for
	 * @return the authorities ( the group he is member - all levels)
	 */
	List<Group> getAuthorities(User user);

	/**
	 * Gets the filtered groups.
	 *
	 * @param filter
	 *            the filter pattern ( * or ? are allowed);
	 * @return the filtered groups or empty list
	 */
	List<Group> getFilteredGroups(String filter);

	/**
	 * Gets the users for authority from underlying provider.
	 *
	 * @param group
	 *            the group to search in
	 * @return list of user for group
	 */
	List<String> getUsersInAuthority(Group group);

	/**
	 * Async update.
	 * 
	 * @param event
	 *            the event
	 */
	void asyncUpdate(@Observes GroupSync event);

}
