package com.sirma.itt.cmf.services.adapters;

import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.cmf.services.adapter.CMFUserService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.security.model.User;

/**
 * The Class CMFUserServiceMock.
 */
@ApplicationScoped
public class CMFUserServiceMock implements CMFUserService {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static List<User> users;

	@Override
	public String getUserRole(String userId, String siteId) throws DMSException {
		return null;
	}

	@Override
	public List<User> getAllUsers() throws DMSException {
		return  defaultUsers();
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
						CMFUserServiceMock.class.getResourceAsStream("users.ser"));
				users = Collections.unmodifiableList((List<User>) usersStream.readObject());
				IOUtils.closeQuietly(usersStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return users;
	}

	@Override
	public List<User> getFilteredUsers(String filter) throws DMSException {
		return Collections.unmodifiableList(users);
	}

	@Override
	public User findUser(String id) throws DMSException {
		return null;
	}

}
