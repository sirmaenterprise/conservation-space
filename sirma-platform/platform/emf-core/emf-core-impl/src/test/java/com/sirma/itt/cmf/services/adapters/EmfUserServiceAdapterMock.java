package com.sirma.itt.cmf.services.adapters;

import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.resources.User;

/**
 * The Class EmfUserServiceMock.
 */
public class EmfUserServiceAdapterMock {

	/** The users. */
	private static List<User> users;

	/**
	 * Gets the user role.
	 *
	 * @param userId
	 *            the user id
	 * @param siteId
	 *            the site id
	 * @return the user role
	 * @throws DMSException
	 *             the DMS exception
	 */
	public String getUserRole(String userId, String siteId) {
		return null;
	}

	/**
	 * Gets the all users.
	 *
	 * @return the all users
	 * @throws DMSException
	 *             the DMS exception
	 */
	public List<User> getAllUsers() {
		return defaultUsers();
	}

	/**
	 * Get the serialized test users.
	 *
	 * @return the list of emf users
	 */
	@SuppressWarnings("unchecked")
	public static List<User> defaultUsers() {
		if (users == null) {
			try {
				ObjectInputStream usersStream = new ObjectInputStream(
						EmfUserServiceAdapterMock.class.getResourceAsStream("users.ser"));
				users = Collections.unmodifiableList((List<User>) usersStream.readObject());
				IOUtils.closeQuietly(usersStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return users;
	}

	/**
	 * Gets the filtered users.
	 *
	 * @param filter
	 *            the filter
	 * @return the filtered users
	 * @throws DMSException
	 *             the DMS exception
	 */
	public List<User> getFilteredUsers(String filter) {
		return Collections.unmodifiableList(users);
	}

	/**
	 * Find user.
	 *
	 * @param id
	 *            the id
	 * @return the user
	 * @throws DMSException
	 *             the DMS exception
	 */
	public User findUser(String id) {
		return null;
	}

}
