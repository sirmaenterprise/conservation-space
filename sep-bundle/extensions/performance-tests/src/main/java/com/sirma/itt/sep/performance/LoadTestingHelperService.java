package com.sirma.itt.sep.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.configuration.ConfigurationFactory;
import com.sirma.itt.emf.resources.PeopleService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.pm.security.PmSecurityModel;

/**
 * The Class LoadTestingHelperService.
 */
@ApplicationScoped
public class LoadTestingHelperService {

	/** The factory. */
	@Inject
	private ConfigurationFactory factory;

	/** The people service. */
	@Inject
	private PeopleService peopleService;

	/** The users. */
	private List<User> users;

	/** The authority roles. */
	private Map<Resource, RoleIdentifier> authorityRoles;

	/** The testable files. */
	private File[] testableFiles;

	/** The instances. */
	private static Map<String, LoadTestingRESTServiceStateWrapper> instances = new HashMap<>();

	/**
	 * Gets the.
	 *
	 * @param sessionId
	 *            the session id
	 * @return the load testing rest service state wrapper
	 */
	public LoadTestingRESTServiceStateWrapper get(String sessionId) {
		synchronized (instances) {
			if (!instances.containsKey(sessionId)) {
				instances.put(sessionId, new LoadTestingRESTServiceStateWrapper(users,
						authorityRoles, testableFiles));
			}
			return instances.get(sessionId);
		}
	}

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		// prepare users and roles
		List<User> defaultUsers = peopleService.getAllUsers();
		users = Collections.unmodifiableList(new ArrayList<User>(defaultUsers.subList(0, 20)));
		RoleIdentifier[] roles = new RoleIdentifier[] { PmSecurityModel.PmRoles.PROJECT_MANAGER,
				SecurityModel.BaseRoles.COLLABORATOR, SecurityModel.BaseRoles.CONTRIBUTOR };
		authorityRoles = new HashMap<Resource, RoleIdentifier>();
		// assign roles
		for (User user : users) {
			authorityRoles.put(user, roles[(int) (Math.random() * roles.length)]);
		}
		authorityRoles = Collections.unmodifiableMap(authorityRoles);

		String testfiles = factory.getConfiguration("test.perfomance.storage.documents");
		testableFiles = new File(testfiles).listFiles();
	}

	/**
	 * Convert array as string using special separator.
	 *
	 * @param treePath
	 *            the tree path
	 * @param separator
	 *            the separator
	 * @return the string
	 */
	public String arrayAsString(int[] treePath, String separator) {
		StringBuilder toString = new StringBuilder();
		for (int i = 0; i < treePath.length; i++) {
			toString.append(treePath[i]);
			if (i + 1 < treePath.length) {
				toString.append(separator);
			}
		}
		return toString.toString();
	}

}
