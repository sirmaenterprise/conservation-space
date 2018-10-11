package com.sirma.itt.cmf.cache;

import java.io.Serializable;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.Alternative;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.commons.util.CloseableIteratorSet;
import org.infinispan.context.Flag;

import com.sirma.itt.seip.cache.SimpleCache;
import com.sirma.itt.seip.configuration.ConfigurationException;

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
			throw new ConfigurationException(
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
	@SuppressWarnings("resource")
	public Stream<K> getKeys() {
		CloseableIteratorSet<K> keySet = cache.keySet();
		CloseableIterator<K> iterator = keySet.iterator();
		// close the iterator on stream close
		return StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.CONCURRENT), false)
					.onClose(iterator::close);
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
