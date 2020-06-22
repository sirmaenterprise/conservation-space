package com.sirma.itt.seip.cache;

import java.io.Serializable;

/**
 * Cache provider for {@link NullCache} instances
 *
 * @author BBonev
 */
@CacheProviderType("noCache")
public class NullCacheProvider implements CacheProvider, Serializable {

	private static final long serialVersionUID = 3979450696613966904L;

	/** Single default instance of the cache provider */
	public static final CacheProvider INSTANCE = new NullCacheProvider();

	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache() {
		return new NullCache<>();
	}

	@Override
	public <K extends Serializable, V> SimpleCache<K, V> createCache(String name) {
		return createCache();
	}

}
