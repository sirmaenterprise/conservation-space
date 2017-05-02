package com.sirma.itt.seip.cache;

import java.io.Serializable;

/**
 * Cache provider for default in memory cache.
 *
 * @author BBonev
 */
@CacheProviderType("memory")
public class InMemoryCacheProvider implements CacheProvider, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2051398993082650156L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache() {
		return new MemoryCache<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache(String name) {
		return createCache();
	}

}
