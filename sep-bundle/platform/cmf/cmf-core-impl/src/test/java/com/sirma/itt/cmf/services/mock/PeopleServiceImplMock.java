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

import com.sirma.itt.cmf.services.adapters.CMFUserServiceMock;
import com.sirma.itt.cmf.services.impl.PeopleServiceImpl;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.resources.PeopleService;
import com.sirma.itt.emf.resources.event.PeopleSync;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Implementation of people service as mock.
 *
 * @author bbanchev
 */
@ApplicationScoped
@Specializes
public class PeopleServiceImplMock extends PeopleServiceImpl implements PeopleService, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4166049889634200581L;
	/** The all users cache. */
	private List<User> allUsers;
	private Map<String, User> userMapping;
	private Map<String, List<User>> sortedMapping;
	/** The last update time. */
	private static final Logger logger = Logger.getLogger(PeopleServiceImplMock.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<User> getAllUsers() {
		return CMFUserServiceMock.defaultUsers();
	}

	/**
	 * Initial load of users after construct.
	 */
	@Override
	@PostConstruct
	public void init() {
		updateUserMapping(getAllUsers());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public List<User> getFilteredUsers(String filter) {
		try {
			return CMFUserServiceMock.defaultUsers();
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to filter users list: " + e.getMessage(), e);
			} else {
				logger.warn("Failed to filter users list: " + e.getMessage());
			}
		}
		return CollectionUtils.emptyList();
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
		User emfUser = userMapping.get(userId.toLowerCase());
		// lets check for missing user
		if (emfUser instanceof EmfUser) {
			return ((EmfUser) emfUser).clone();
		}
		// add wrapping of the object
		return null;
	}

	@Override
	public void execute(SchedulerContext context) throws Exception {
		logger.info("Initiated user synchronization");
	}

	@Override
	public List<User> getSortedUsers(final String sortColumn) {
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
						return (this.nullsAreHigh ? -1 : 1);
					}
					if ((o2 == null) || o2.trim().isEmpty()) {
						return (this.nullsAreHigh ? 1 : -1);
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
		List<User> users = new ArrayList<User>(emfUsers.size());
		for (User user : emfUsers) {
			try {
				userMappingInternal.put(user.getIdentifier().toLowerCase(), user);
				users.add(user);
			} catch (RuntimeException e) {
				if (logger.isDebugEnabled()) {
					logger.error("Failed user synchronization for " + user.getIdentifier(), e);
				} else {
					logger.error("Failed user synchronization for " + user.getIdentifier()
							+ ": caused by: " + e.getMessage());
				}
			}
		}
		userMapping = Collections.unmodifiableMap(userMappingInternal);
		allUsers = Collections.unmodifiableList(emfUsers);
		sortedMapping = new HashMap<>(4);
	}

	/**
	 * Asycn update.
	 *
	 * @param event
	 *            the event
	 */
	@Override
	@Asynchronous
	public void asyncUpdate(@Observes PeopleSync event) {
	}

	@Override
	public void beforeExecute(SchedulerContext context) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterExecute(SchedulerContext context) throws Exception {
		// TODO Auto-generated method stub

	}
}
