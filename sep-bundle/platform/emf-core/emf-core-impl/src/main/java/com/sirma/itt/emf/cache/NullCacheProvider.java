package com.sirma.itt.emf.cache;

import java.io.Serializable;


/**
 * Cache provider for {@link NullCache} instances
 *
 * @author BBonev
 */
@CacheProviderType("noCache")
public class NullCacheProvider implements CacheProvider , Serializable{

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3979450696613966904L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache() {
		return new NullCache<K, V>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache(String name) {
		return createCache();
	}

}
