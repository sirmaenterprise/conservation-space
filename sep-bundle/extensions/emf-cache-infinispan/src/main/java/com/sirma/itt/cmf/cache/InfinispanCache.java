package com.sirma.itt.cmf.cache;

import java.io.Serializable;
import java.util.Collection;

import javax.enterprise.inject.Alternative;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;

import com.sirma.itt.emf.cache.SimpleCache;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;

/**
 * Represents a proxy class for infinispan cache implementation.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author BBonev
 */
@Alternative
public class InfinispanCache<K extends Serializable, V extends Object> implements SimpleCache<K, V> {

	/** Reference to the cache. */
	private Cache<K, V> cache;

	/** The with flags. */
	private AdvancedCache<K, V> withFlags;

	/**
	 * Instantiates a new infinispan cache.
	 *
	 * @param cache
	 *            the cache
	 */
	public InfinispanCache(Cache<K, V> cache) {
		if (cache == null) {
			throw new EmfConfigurationException(
					"Infinispan cache not cofigured correctly. Please check your configuration!");
		}
		this.cache = cache;
		withFlags = cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(K key) {
		return cache.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<K> getKeys() {
		return cache.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(K key) {
		return cache.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(K key, V value) {
		withFlags.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(K key) {
		withFlags.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		cache.clear();
	}

}
