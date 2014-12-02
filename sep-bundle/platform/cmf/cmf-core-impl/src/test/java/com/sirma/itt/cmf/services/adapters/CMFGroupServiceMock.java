package com.sirma.itt.cmf.services.adapters;

import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.sirma.itt.cmf.services.adapter.CMFGroupService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.Group;
import com.sirma.itt.emf.security.model.User;

/**
 * The Class CMFGroupServiceMock.
 */
@Singleton
@Startup
public class CMFGroupServiceMock implements CMFGroupService {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EmfGroup findGroup(String groupId) throws DMSException {
		List<Group> allGroups = getAllGroups();
		for (Group group : allGroups) {
			if (group.getIdentifier().equals(groupId)) {
				return (EmfGroup) group;
			}
		}
		return null;
	}

	/** The groups. */
	private static List<Group> groups;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Group> getAllGroups() throws DMSException {

		return Collections.unmodifiableList(defaultAllGroups());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Group> getFilteredGroups(String filter) throws DMSException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Group> getAuthorities(User user) throws DMSException {
		if (user.getIdentifier().equals("admin")) {
			return Collections.unmodifiableList(defaultAllGroups());
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getUsersInAuthority(Group group) throws DMSException {
		try (ObjectInputStream groupStream = new ObjectInputStream(
				CMFGroupServiceMock.class.getResourceAsStream(group.getIdentifier() + ".ser"))) {
			return (List<String>) groupStream.readObject();
		} catch (Exception e) {
			throw new DMSException(e);
		}
	}

	/**
	 * Default all groups.
	 *
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static List<Group> defaultAllGroups() {
		if (groups == null) {
			try(ObjectInputStream groupStream = new ObjectInputStream(
						CMFGroupServiceMock.class.getResourceAsStream("groups.ser"))) {
				groups = (List<Group>) groupStream.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return groups;
	}
}
