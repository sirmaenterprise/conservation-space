package com.sirma.itt.cmf.services.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Specializes;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.services.adapters.CMFGroupServiceMock;
import com.sirma.itt.cmf.services.impl.GroupServiceImpl;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.resources.GroupService;
import com.sirma.itt.emf.resources.event.GroupSync;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.Group;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Implementation of group service as mock
 *
 * @author bbanchev
 */
@ApplicationScoped
@Specializes
public class GroupServiceImplMock extends GroupServiceImpl implements GroupService, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4166049889634200581L;
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(GroupServiceImplMock.class);
	/** The all groups cache. */
	private List<Group> allGroups;
	/** The group mapping. */
	private Map<String, Group> groupMapping;
	/** The group mapping. */
	private Map<Group, List<String>> usersToGroup;
	/** Mapping for user to groups. */
	private Map<User, List<Group>> groupsToUser;
	private Map<GroupSorter, List<Group>> sortedMapping;
	// the adapter
	private CMFGroupServiceMock adapterMock = new CMFGroupServiceMock();

	/**
	 * Initial load of users after construct.
	 */
	@Override
	@PostConstruct
	public void init() {

		updateGroupMapping(getAllGroups());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Group> getAllGroups() {
		return CMFGroupServiceMock.defaultAllGroups();
	}

	@Override
	public List<Group> getSortedGroup(final GroupSorter sortColumn) {
		if (sortColumn == null) {
			return allGroups;
		}
		if (!sortedMapping.containsKey(sortColumn)) {
			List<Group> allGroupsLocal = new ArrayList<Group>(this.allGroups);
			Collections.sort(allGroupsLocal, new Comparator<Group>() {
				boolean nullsAreHigh = true;

				@Override
				public int compare(Group g1, Group g2) {
					String o1 = getSortedField(g1, sortColumn);
					String o2 = getSortedField(g2, sortColumn);
					if (o1 == o2) {
						return 0;
					}
					if ((o1 == null) || o1.trim().isEmpty()) {
						return (this.nullsAreHigh ? -1 : 1);
					}
					if ((o2 == null) || o2.trim().isEmpty()) {
						return (this.nullsAreHigh ? 1 : -1);
					}
					return o1.compareToIgnoreCase(o2);
				}

				private String getSortedField(Group group, GroupSorter column) {
					if (column == GroupSorter.DISPLAY_NAME) {
						return group.getDisplayName();
					}
					if (column == GroupSorter.GROUP_NAME) {
						return group.getIdentifier();
					}
					return group.getId().toString();
				}
			});
			sortedMapping.put(sortColumn, allGroupsLocal);
		}
		return sortedMapping.get(sortColumn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public List<Group> getFilteredGroups(String filter) {
		try {
			return adapterMock.getFilteredGroups(filter);
		} catch (DMSException e) {
			logger.error("Failed to filter groups list: " + e.getMessage(), e);
		}
		return CollectionUtils.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public Group findGroup(String groupId) {
		if (StringUtils.isNullOrEmpty(groupId)) {
			return null;
		}
		Group emfGroup = groupMapping.get(groupId.toLowerCase());
		// lets check for missing user
		if (emfGroup instanceof EmfGroup) {
			return ((EmfGroup) emfGroup).clone();
		}
		return null;
	}

	/**
	 * Update group mapping.
	 *
	 * @param groups
	 *            the cmf groups in the system
	 */
	private void updateGroupMapping(List<Group> groups) {
		Map<String, Group> groupMappingInternal = new HashMap<String, Group>(groups.size());
		List<Group> groupsInteral = new ArrayList<Group>(groups.size());
		for (Group group : groups) {
			try {
				groupMappingInternal.put(group.getIdentifier().toLowerCase(), group);
				groupsInteral.add(group);
			} catch (RuntimeException e) {
				if (logger.isDebugEnabled()) {
					logger.error("Failed group synchronization for " + group.getIdentifier(), e);
				} else {
					logger.error("Failed group synchronization for " + group.getIdentifier()
							+ ": caused by: " + e.getMessage());
				}
			}
		}
		groupMapping = Collections.unmodifiableMap(groupMappingInternal);
		allGroups = Collections.unmodifiableList(groupsInteral);
		usersToGroup = Collections.synchronizedMap(new HashMap<Group, List<String>>(allGroups
				.size()));
		// size is unknown currently
		groupsToUser = Collections.synchronizedMap(new HashMap<User, List<Group>>(1000, 0.9f));
		sortedMapping = new HashMap<>(4);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public List<Group> getAuthorities(User user) {
		if (user == null) {
			return null;
		}
		if (groupsToUser.containsKey(user)) {
			return groupsToUser.get(user);
		}
		try {
			List<Group> groups = adapterMock.getAuthorities(user);
			groupsToUser.put(user, groups);
			return groups;
		} catch (DMSException e) {
			logger.error("Failed to retreive user groups list: " + e.getMessage(), e);
		}
		return CollectionUtils.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getUsersInAuthority(Group group) {
		if (group == null) {
			return null;
		}
		try {
			if (usersToGroup.containsKey(group)) {
				return usersToGroup.get(group);
			}
			List<String> usersInAuthority = adapterMock.getUsersInAuthority(group);
			usersToGroup.put(group, usersInAuthority);
			return usersInAuthority;
		} catch (DMSException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
	}

	/**
	 * Asycn update.
	 *
	 * @param event
	 *            the event
	 */
	@Override
	@Asynchronous
	public void asyncUpdate(@Observes GroupSync event) {
	}

}