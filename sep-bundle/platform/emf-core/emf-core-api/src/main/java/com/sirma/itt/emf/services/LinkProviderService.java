package com.sirma.itt.emf.services;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * The LinkProviderService provides uri links to a given entity
 */
public interface LinkProviderService {

	/**
	 * Builds the link for given entity as /{context}/entity/....
	 * 
	 * @param instance
	 *            the instance to get uri for
	 * @return the uri
	 */
	public String buildLink(Instance instance);

	/**
	 * Builds a bookmark link for given instance and page tab.
	 * 
	 * @param instance
	 *            the instance
	 * @param tab
	 *            the tab
	 * @return the string
	 */
	public String buildLink(Instance instance, String tab);
}
