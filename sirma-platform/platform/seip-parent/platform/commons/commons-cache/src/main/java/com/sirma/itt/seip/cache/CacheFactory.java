package com.sirma.itt.seip.cache;

import java.io.Serializable;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provider class for cache instances for direct injection. To create the cache instance is used the default cache
 * provider.
 *
 * @author BBonev
 */
@Singleton
public class CacheFactory {

	@Inject
	private CacheProvider cacheProvider;

	/**
	 * Creates a new Cache object.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @return the simple cache&lt;k, v&gt;
	 */
	@Produces
	@Default
	public <K extends Serializable, V extends Object> SimpleCache<K, V> createCache() {
		return cacheProvider.createCache();
	}

}
