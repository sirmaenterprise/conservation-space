package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.services.adapter.CMFGroupService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.concurrent.TxAsyncCallableEvent;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.event.ApplicationStartupEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.TimeoutException;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.resources.GroupService;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.event.GroupSync;
import com.sirma.itt.emf.scheduler.SchedulerActionAdapter;
import com.sirma.itt.emf.scheduler.SchedulerConfiguration;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryType;
import com.sirma.itt.emf.scheduler.SchedulerService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.Group;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Implementation of group service.
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Named("groupService")
public class GroupServiceImpl extends SchedulerActionAdapter implements GroupService, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4166049889634200581L;
	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

	/** The reload interval. */
	@Inject
	@Config(name = CmfConfigurationProperties.CACHE_GROUP_UPADTE_SCHEDULE, defaultValue = "0 0/15 * ? * *")
	private String reloadInterval;
	/** The user service. */
	@Inject
	private Instance<CMFGroupService> groupAdapter;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;
	/** The all groups cache. */
	private List<Group> allGroups;
	/** The group mapping. */
	private Map<String, Group> groupMapping;
	/** The group mapping. */
	private Map<Group, List<String>> usersToGroup;
	/** Mapping for user to groups. */
	private Map<User, List<Group>> groupsToUser;

	/** The sorted mapping. */
	private Map<GroupSorter, List<Group>> sortedMapping;

	/** The scheduler service. */
	@Inject
	private SchedulerService schedulerService;

	/** The Constant LOCK. */
	private final Object sync = new Object();
	/** Used to restrict multiple simultaneous definition loadings. */
	private ReentrantLock lock = new ReentrantLock();

	/** The event service. */
	@Inject
	private EventService eventService;

	/**
	 * Initial load of users after construct.
	 */
	@PostConstruct
	public void init() {
		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.CRON);
		configuration.setIdentifier("REINIT_GROUP_CACHE");
		configuration.setCronExpression(reloadInterval);
		// execute in 15 seconds
		schedulerService.schedule(GroupServiceImpl.class, configuration);
	}

	/**
	 * On startup.
	 * 
	 * @param event
	 *            the event
	 */
	@Secure(runAsSystem = true)
	public void onStartup(@Observes ApplicationStartupEvent event) {
		asyncUpdate(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Group> getAllGroups() {
		waitForDataToLoad();
		return allGroups;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Group> getSortedGroup(final GroupSorter sortColumn) {
		waitForDataToLoad();
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
			return groupAdapter.get().getFilteredGroups(filter);
		} catch (DMSException e) {
			LOGGER.error("Failed to filter groups list: " + e.getMessage(), e);
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
		waitForDataToLoad();
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
				Group createdResource = resourceService.getOrCreateResource(PropertiesUtil
						.cleanNullProperties(group));
				groupMappingInternal.put(group.getIdentifier().toLowerCase(), createdResource);
				groupsInteral.add(createdResource);
			} catch (RuntimeException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.error("Failed group synchronization for " + group.getIdentifier(), e);
				} else {
					LOGGER.error("Failed group synchronization for " + group.getIdentifier()
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
		waitForDataToLoad();
		if (groupsToUser.containsKey(user)) {
			return groupsToUser.get(user);
		}
		try {
			List<Group> groups = groupAdapter.get().getAuthorities(user);
			groupsToUser.put(user, groups);
			return groups;
		} catch (DMSException e) {
			LOGGER.error("Failed to retreive user groups list: " + e.getMessage(), e);
		}
		return CollectionUtils.emptyList();
	}

	/**
	 * Wait for data to load.
	 */
	private void waitForDataToLoad() {
		short iterations = 0;
		int timeout = 15 * 1000;
		while ((groupsToUser == null) || (groupMapping == null) || (allGroups == null)
				|| (usersToGroup == null) || (sortedMapping == null)) {
			// if we enter more then 4 times we should not stay anymore here
			if (iterations >= 4) {
				String message = "Waited for group synchronization " + (timeout * iterations)
						+ " and failed to happen!";
				LOGGER.error(message);
				throw new TimeoutException(message);
			}
			synchronized (sync) {
				try {
					sync.wait(timeout);
					iterations++;
				} catch (InterruptedException e) {
					LOGGER.warn("Synchronization is interrupted!", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure(runAsSystem = true)
	public List<String> getUsersInAuthority(Group group) {
		if (group == null) {
			return null;
		}
		try {
			waitForDataToLoad();
			if (usersToGroup.containsKey(group)) {
				return usersToGroup.get(group);
			}
			List<String> usersInAuthority = groupAdapter.get().getUsersInAuthority(group);
			usersToGroup.put(group, usersInAuthority);
			return usersInAuthority;
		} catch (DMSException e) {
			LOGGER.error("Error during extract of authority users", e);
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(SchedulerContext context) throws Exception {
		LOGGER.info("Initiated group synchronization");
		reloadGroups();
	}

	/**
	 * Reload groups on schedule event. Operation is not synchronized
	 */
	private void reloadGroups() {
		// if already locked then we have synchronization running and we should not proceed.
		if (!lock.tryLock()) {
			// already running
			return;
		}
		try {
			lock.lock();

			if (groupAdapter.isUnsatisfied() || groupAdapter.isAmbiguous()) {
				LOGGER.warn("No implementation for " + CMFGroupService.class
						+ ". Group synchronization is not possible!");
				updateGroupMapping(Collections.<Group> emptyList());
				return;
			}
			updateGroupMapping(groupAdapter.get().getAllGroups());
		} catch (Exception e) {
			LOGGER.error("Failed to retreive the groups list: " + e.getMessage(), e);
			allGroups = Collections.emptyList();
			updateGroupMapping(allGroups);
		} finally {
			lock.unlock();
			synchronized (sync) {
				sync.notifyAll();
			}
		}

		// trigger synchronization of all contained resource for all groups
		resourceService.synchContainedResources();
	}

	/**
	 * Asycn update.
	 * 
	 * @param event
	 *            the event
	 */
	@Override
	@Secure(runAsSystem = true)
	public void asyncUpdate(@Observes GroupSync event) {
		LOGGER.info("Initiated async group synchronization");
		eventService.fire(new TxAsyncCallableEvent(new ReloadGroups(), SecurityContextManager
				.getCurrentSecurityContext()));
	}

	/**
	 * The Class ReloadGroups.
	 */
	private class ReloadGroups implements Callable<Void> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Void call() throws Exception {
			reloadGroups();
			return null;
		}

	}

}