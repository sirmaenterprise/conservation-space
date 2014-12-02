package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
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

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.services.adapter.CMFUserService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.concurrent.TxAsyncCallableEvent;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.ApplicationStartupEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.TimeoutException;
import com.sirma.itt.emf.resources.PeopleService;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.event.PeopleSync;
import com.sirma.itt.emf.scheduler.SchedulerActionAdapter;
import com.sirma.itt.emf.scheduler.SchedulerConfiguration;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryType;
import com.sirma.itt.emf.scheduler.SchedulerService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Implementation of people service.
 *
 * @author BBonev
 */
@Named
@ApplicationScoped
public class PeopleServiceImpl extends SchedulerActionAdapter implements PeopleService,
		Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4166049889634200581L;

	/** The reload interval. */
	@Inject
	@Config(name = CmfConfigurationProperties.CACHE_USER_UPADTE_SCHEDULE, defaultValue = "0 0/15 * ? * *")
	private String reloadInterval;
	/** The user service. */
	@Inject
	private Instance<CMFUserService> userService;
	/** The all users cache. */
	private List<User> allUsers;

	/** The user mapping. */
	private Map<String, User> userMapping;

	/** The sorted mapping. */
	private Map<String, List<User>> sortedMapping;
	/** The last update time. */
	private static final Logger LOGGER = Logger.getLogger(PeopleServiceImpl.class);

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The scheduler service. */
	@Inject
	private SchedulerService schedulerService;

	/** The sync. */
	private final Object sync = new Object();

	/** Used to restrict multiple simultaneous definition loadings. */
	private ReentrantLock lock = new ReentrantLock();

	/** The event service. */
	@Inject
	private EventService eventService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> getAllUsers() {
		waitForDataToLoad();
		return allUsers;
	}

	/**
	 * Initial load of users after construct.
	 */
	@PostConstruct
	public void init() {
		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.CRON);
		configuration.setIdentifier("REINIT_PEOPLE_CACHE");
		configuration.setCronExpression(reloadInterval);
		// execute in 15 seconds
		schedulerService.schedule(PeopleServiceImpl.class, configuration);
	}

	/**
	 * On application start.
	 * 
	 * @param event
	 *            the event
	 */
	@Secure(runAsSystem = true)
	public void onApplicationStart(@Observes ApplicationStartupEvent event) {
		asyncUpdate(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public List<User> getFilteredUsers(String filter) {
		try {
			List<User> users = userService.get().getFilteredUsers(filter);
			if (!users.isEmpty()) {
				return mergeUsers(users, false);
			}
			return users;
		} catch (DMSException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Failed to filter users list: " + e.getMessage(), e);
			} else {
				LOGGER.warn("Failed to filter users list: " + e.getMessage());
			}
		}
		return CollectionUtils.emptyList();
	}

	/**
	 * Merge external received users to the existing in the system.
	 *
	 * @param users
	 *            is the list of received users
	 * @param saveChanges
	 *            the save changes
	 * @return the merged users
	 */
	private List<User> mergeUsers(List<User> users, boolean saveChanges) {
		List<Pair<String, ResourceType>> userIds = new ArrayList<>(users.size());
		Map<String, User> mapping = null;
		if (saveChanges) {
			mapping = CollectionUtils.createLinkedHashMap(users.size());
		}
		for (User user : users) {
			userIds.add(new Pair<String, ResourceType>(user.getIdentifier(), ResourceType.USER));
			if (saveChanges) {
				mapping.put(user.getIdentifier(), user);
			}
		}
		List<User> loaded = castToUserList(resourceService.load(userIds));
		List<User> updated = new ArrayList<>(loaded.size());
		if (saveChanges) {
			for (User user : loaded) {
				User newData = mapping.get(user.getIdentifier());
				if (newData != null) {
					user.getProperties().putAll(newData.getProperties());
					User resource = resourceService.saveResource(user);
					updated.add(resource);
				} else {
					updated.add(user);
				}
			}
		}
		List<User> resourceToBeAdded = new ArrayList<>(users);
		resourceToBeAdded.removeAll(loaded);
		for (User user : resourceToBeAdded) {
			User addedUser = resourceService.getOrCreateResource(user);
			loaded.add(addedUser);
		}

		// return fully initialized users
		return loaded;
	}

	/**
	 * Cast to user list.
	 *
	 * @param load
	 *            the load
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	private List<User> castToUserList(List<?> load) {
		return (List<User>) load;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public User findUser(String userId) {
		if (StringUtils.isNullOrEmpty(userId)) {
			return null;
		}
		if (SecurityContextManager.getSystemUser().getIdentifier().equals(userId)) {
			return ((EmfUser) SecurityContextManager.getSystemUser()).clone();
		}
		waitForDataToLoad();
		User emfUser = userMapping.get(userId.toLowerCase());
		// lets check for missing user
		if (emfUser instanceof EmfUser) {
			return ((EmfUser) emfUser).clone();
		}
		// add wrapping of the object
		return null;
	}

	/**
	 * Wait for data to load.
	 */
	private void waitForDataToLoad() {
		short iterations = 0;
		int timeout = 15 * 1000;
		while ((allUsers == null) || (userMapping == null) || (sortedMapping == null)) {
			// if we enter more then 4 times we should not stay anymore here
			if (iterations >= 4) {
				String message = "Waited for people synchronization " + (timeout * iterations)
						+ " and failed to happen!";
				LOGGER.error(message);
				throw new TimeoutException(message);
			}
			synchronized (sync) {
				try {
					sync.wait(timeout);
					iterations++;
				} catch (InterruptedException e) {
					LOGGER.warn(e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(SchedulerContext context) throws Exception {
		LOGGER.info("Initiated user synchronization");
		reloadUsers();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> getSortedUsers(final String sortColumn) {
		waitForDataToLoad();
		if (sortColumn == null) {
			return allUsers;
		}
		if (!sortedMapping.containsKey(sortColumn)) {
			List<User> allUsersLocal = new ArrayList<User>(getAllUsers());
			Collections.sort(allUsersLocal, new Comparator<User>() {
				boolean nullsAreHigh = true;

				@Override
				public int compare(User u1, User u2) {
					String o1 = (String) u1.getProperties().get(sortColumn);
					String o2 = (String) u2.getProperties().get(sortColumn);
					if (o1 == o2) {
						return 0;
					}
					if ((o1 == null) || o1.trim().isEmpty()) {
						return this.nullsAreHigh ? -1 : 1;
					}
					if ((o2 == null) || o2.trim().isEmpty()) {
						return this.nullsAreHigh ? 1 : -1;
					}
					return o1.compareToIgnoreCase(o2);
				}
			});
			sortedMapping.put(sortColumn, allUsersLocal);
		}
		return sortedMapping.get(sortColumn);
	}

	/**
	 * Update user mapping.
	 *
	 * @param emfUsers
	 *            the cmf users
	 */
	private void updateUserMapping(List<User> emfUsers) {
		Map<String, User> userMappingInternal = CollectionUtils
				.createLinkedHashMap(emfUsers.size());

		allUsers = Collections.unmodifiableList(mergeUsers(emfUsers, true));
		for (User user : allUsers) {
			userMappingInternal.put(user.getIdentifier().toLowerCase(), user);
		}
		userMapping = Collections.unmodifiableMap(userMappingInternal);
		sortedMapping = new HashMap<>(4);
	}

	/**
	 * Reload users on schedule event. Operation is not synchronized
	 */
	private void reloadUsers() {
		// here we check if someone called a synchronization during
		// asynchronous call
		if (lock.isLocked()) {
			// if the method is called from the asynchronous method then we
			// own the lock
			if (!lock.isHeldByCurrentThread()) {
				// already running
				return;
			}
		}
		try {
			lock.lock();
			if (userService.isUnsatisfied() || userService.isAmbiguous()) {
				LOGGER.warn("No implementation for " + CMFUserService.class
						+ ". User synchronization is not possible!");
				updateUserMapping(Collections.<User> emptyList());
				return;
			}
			CMFUserService service = userService.get();
			updateUserMapping(service.getAllUsers());
		} catch (Exception e) {
			LOGGER.error("Failed to retreive the users list: " + e.getMessage(), e);
			updateUserMapping(new LinkedList<User>());
		} finally {
			lock.unlock();
			synchronized (sync) {
				sync.notifyAll();
			}
		}
	}

	/**
	 * Asycn update.
	 *
	 * @param event
	 *            the event
	 */
	@Override
	@Secure(runAsSystem = true)
	public void asyncUpdate(@Observes PeopleSync event) {
		LOGGER.info("Initiated async user synchronization");
		eventService.fire(new TxAsyncCallableEvent(new ReloadUsers(), SecurityContextManager
				.getCurrentSecurityContext()));
	}

	/**
	 * The Class ReloadUsers.
	 */
	private class ReloadUsers implements Callable<Void> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Void call() throws Exception {
			reloadUsers();
			return null;
		}

	}
}
