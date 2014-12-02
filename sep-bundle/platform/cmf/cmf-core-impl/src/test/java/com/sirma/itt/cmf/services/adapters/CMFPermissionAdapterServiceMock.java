package com.sirma.itt.cmf.services.adapters;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * The Class CMFPermissionAdapterServiceMock.
 */
@ApplicationScoped
public class CMFPermissionAdapterServiceMock implements
		CMFPermissionAdapterService {

	@Override
	public void updateCaseDocuments(CaseInstance instance,
			Map<String, Serializable> additionalProps) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();

	}

	@Override
	public void updateMembers(DMSInstance instance, List<Resource> resources)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();

	}

	@Override
	public String searchableUserId(Serializable users) {
		if (users == null) {
			return null;
		}
		if ((users instanceof Collection) || users.getClass().isArray()) {
			throw new RuntimeException("Only single values is allowed for user id");
		}

		return generatePermissionEntriesValue(users, true);
	}

	/**
	 * Internal methtod to generate comma separated string. Separator is | as it is forbidden for
	 * users.
	 *
	 * @param all
	 *            the whole value
	 * @param searchable
	 *            is this searchable or storable argument
	 * @return the updated string
	 */
	private String generatePermissionEntriesValue(Object all, boolean searchable) {
		StringBuilder builder = new StringBuilder(searchable ? "*|" : "|");
		if (all instanceof Collection) {
			Collection<?> items = (Collection<?>) all;
			// add trailing
			for (Object item : items) {
				if (item instanceof Resource) {
					builder.append(((Resource) item).getIdentifier()).append("|");
				} else if (item != null) {
					builder.append(item.toString()).append("|");
				}
			}
		} else if (all instanceof String) {
			builder.append(all).append("|");
		} else {
			throw new RuntimeException("Invalid user id!");
		}
		if (builder.length() > (searchable ? 2 : 1)) {
			return builder.append(searchable ? "*" : "").toString();
		}
		return "";
	}
}
