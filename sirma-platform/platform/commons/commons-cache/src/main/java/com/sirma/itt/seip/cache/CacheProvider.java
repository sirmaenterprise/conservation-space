package com.sirma.itt.seip.cache;

import java.io.Serializable;

/**
 * Defines methods for creating {@link SimpleCache} instances. All implementations must be qualified with
 * {@link CacheProviderType} annotations
 *
 * @author BBonev
 */
public interface CacheProvider {

	/**
	 * Creates the default cache instance.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the simple cache
	 */
	<K extends Serializable, V extends Object> SimpleCache<K, V> createCache();

	/**
	 * Creates the cache instance with the given name.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param name
	 *            the name of the cache
	 * @return the simple cache
	 */
	<K extends Serializable, V extends Object> SimpleCache<K, V> createCache(String name);
}
