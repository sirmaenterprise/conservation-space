package com.sirma.itt.seip.instance.util;

import java.io.Serializable;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The LinkProviderService provides uri links to a given entity
 */
public interface LinkProviderService {

	/**
	 * Builds for the given instance
	 *
	 * @param instance
	 *            the instance
	 * @return a bookmark link.
	 */
	default String buildLink(Instance instance) {
		if (instance == null) {
			// will return the default link
			return buildLink((Serializable) null);
		}
		return buildLink(instance.getId());
	}

	/**
	 * Builds a bookmark link for the given instance id, type and page tab. The only required argument is the instance
	 * id.
	 *
	 * @param instanceId
	 *            the instance id to reference the link. Required
	 * @return a bookmark link.
	 */
	String buildLink(Serializable instanceId);
}
