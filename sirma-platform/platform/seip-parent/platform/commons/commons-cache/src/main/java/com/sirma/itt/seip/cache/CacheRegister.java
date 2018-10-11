package com.sirma.itt.seip.cache;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link CacheRegister} provides means to add and remove caches dynamically
 *
 * @author BBonev
 */
public interface CacheRegister {

	/**
	 * Register cache described by the given configuration. If such cache exists nothing should be done.
	 *
	 * @param configuration
	 *            the configuration to use for registering the cache
	 * @return true, if cache was successfully added
	 */
	default boolean registerCache(CacheConfiguration configuration) {
		if (configuration == null) {
			return false;
		}
		return registerCaches(Collections.singletonList(configuration));
	}

	/**
	 * Register caches described by the given configurations. If such caches exists nothing should be done.
	 *
	 * @param configuration
	 *            the configuration
	 * @return true, if cache were successfully added
	 */
	boolean registerCaches(Collection<CacheConfiguration> configuration);

	/**
	 * Unregister cache identified by the given name
	 *
	 * @param name
	 *            the cache name to remove
	 * @return true, if cache was successfully removed
	 */
	default boolean unregisterCache(String name) {
		if (StringUtils.isBlank(name)) {
			return false;
		}
		return unregisterCaches(Collections.singletonList(name));
	}

	/**
	 * Unregister caches identified for the given names
	 *
	 * @param names
	 *            the cache names to remove
	 * @return true, if cache were successfully removed
	 */
	boolean unregisterCaches(Collection<String> names);
}
