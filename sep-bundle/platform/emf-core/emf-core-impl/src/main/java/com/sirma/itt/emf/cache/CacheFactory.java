package com.sirma.itt.emf.cache;

import java.io.Serializable;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.sirma.itt.emf.cache.CacheProvider;
import com.sirma.itt.emf.cache.SimpleCache;

/**
 * Provider class for cache instances for direct injection. To create the cache
 * instance is used the default cache provider.
 * 
 * @author BBonev
 */
public class CacheFactory {

	/** The cache provider. */
	@Inject
	private CacheProvider cacheProvider;

	/**
	 * Creates a new Cache object.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the simple cache<k, v>
	 */
	@Produces
	@Default
	public <K extends Serializable, V extends Object> SimpleCache<K, V> createCache() {
		return cacheProvider.createCache();
	}

}
