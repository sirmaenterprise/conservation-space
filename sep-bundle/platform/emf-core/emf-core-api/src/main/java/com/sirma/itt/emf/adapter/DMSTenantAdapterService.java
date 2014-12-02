package com.sirma.itt.emf.adapter;

import java.util.Set;

/**
 * DMS service that provides the enabled emf containers for definitions and instances.
 * 
 * @author BBonev
 */
public interface DMSTenantAdapterService {

	/**
	 * Gets the emf container IDs.
	 * 
	 * @return the emf containers
	 * @throws DMSException
	 *             the dMS exception
	 */
	Set<String> getEmfContainers() throws DMSException;
}
