package com.sirma.itt.cmf.services.adapter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * Adapter service to be responsible for case' documents update ragarding permissions.
 *
 * @author bbanchev
 */
public interface CMFPermissionAdapterService {

	/** The list of allowed users. */
	String LIST_OF_ALLOWED_USERS = "allowedUsers";

	/** The list of allowed groups. */
	String LIST_OF_ALLOWED_GROUPS = "allowedGroups";

	/** The list of active users. */
	String LIST_OF_ACTIVE_USERS = "activeUsers";

	/** The key copyable. */
	String KEY_COPYABLE = "copyable";

	/**
	 * Update case document with case permissions.
	 *
	 * @param instance
	 *            the instance to update documents for
	 * @param additionalProps
	 *            is the properties to additionally add to request.For example
	 * @throws DMSException
	 *             the dMS exception {@link #LIST_OF_ALLOWED_USERS} sets the allowed users. Mapping
	 *             for this properties should be added in the converter mapping.
	 */
	void updateCaseDocuments(CaseInstance instance, Map<String, Serializable> additionalProps)
			throws DMSException;

	/**
	 * Update members for some dms instance, by setting the correct allowed users and groups
	 * members.
	 *
	 * @param instance
	 *            the instance to update
	 * @param resources
	 *            the resources to set
	 * @throws DMSException
	 *             the dMS exception
	 */
	void updateMembers(DMSInstance instance, List<Resource> resources) throws DMSException;

	/**
	 * Generates searchable user id in {@link #LIST_OF_ACTIVE_USERS} or. Throws RuntimeException if
	 * convert error occurs {@link #LIST_OF_ALLOWED_USERS} from the given value.
	 *
	 * @param user
	 *            is the user id
	 * @return searchable in dms userid argument
	 */
	Serializable searchableUserId(Serializable user);

}