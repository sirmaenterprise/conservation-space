package com.sirma.itt.seip.cache;

import java.util.Map;

/**
 * Provider for cache configurations. It's a repository for all registered caches in the system.
 *
 * @author BBonev
 */
public interface CacheConfigurationProvider {

	/**
	 * Gets the configuration by name
	 *
	 * @param name
	 *            the name
	 * @return the configuration or <code>null</code> if no such exists
	 */
	CacheConfiguration getConfiguration(String name);

	/**
	 * Gets all configurations mapped by name
	 *
	 * @return the configurations mapping
	 */
	Map<String, CacheConfiguration> getConfigurations();
}
