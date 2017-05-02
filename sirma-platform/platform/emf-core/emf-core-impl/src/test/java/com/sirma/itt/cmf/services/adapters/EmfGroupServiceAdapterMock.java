package com.sirma.itt.cmf.services.adapters;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.Resource;

/**
 * The Class EmfGroupServiceMock.
 */
public class EmfGroupServiceAdapterMock {

	/**
	 * {@inheritDoc}
	 */

	public EmfGroup findGroup(String groupId) {
		List<Group> allGroups = getAllGroups();
		for (Group group : allGroups) {
			if (group.getName().equals(groupId)) {
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

	public List<Group> getAllGroups() {

		return Collections.unmodifiableList(defaultAllGroups());
	}

	/**
	 * {@inheritDoc}
	 */

	public List<Group> getFilteredGroups(String filter) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */

	public List<Group> getAuthorities(Resource user) {
		if (user.getName().equals("admin")) {
			return Collections.unmodifiableList(defaultAllGroups());
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<String> getUsersInAuthority(Group group) {
		try (ObjectInputStream groupStream = new ObjectInputStream(
				EmfGroupServiceAdapterMock.class.getResourceAsStream(group.getName() + ".ser"))) {
			return (List<String>) groupStream.readObject();
		} catch (Exception e) {
			System.err.println(group.getName());
			return new ArrayList<String>(0);
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
			try (ObjectInputStream groupStream = new ObjectInputStream(
					EmfGroupServiceAdapterMock.class.getResourceAsStream("groups.ser"))) {
				groups = (List<Group>) groupStream.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return groups;
	}
}
