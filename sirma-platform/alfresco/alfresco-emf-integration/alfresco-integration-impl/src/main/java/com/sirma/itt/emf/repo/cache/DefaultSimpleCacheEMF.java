/*
 * Copyright (C) 2005-2012 Alfresco Software Limited. This file is part of Alfresco Alfresco is free
 * software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.emf.repo.cache;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;

import org.alfresco.repo.cache.CacheInformationRegistry;
import org.alfresco.repo.cache.SimpleCache;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

/**
 * {@link SimpleCache} implementation backed by a {@link ConcurrentLinkedHashMap}.
 *
 * @author Matt Ward
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
public final class DefaultSimpleCacheEMF<K extends Serializable, V extends Object> implements
		SimpleCache<K, V>, BeanNameAware {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(CacheInformationRegistryImpl.class);

	/** The Constant TRACE_ENABLED. */
	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

	/** The map. */
	private ConcurrentLinkedHashMap<K, AbstractMap.SimpleImmutableEntry<K, V>> map;

	/** The bean name. */
	private String beanName;

	/** The cache information registry. */
	private CacheInformationRegistry cacheInformationRegistry;

	/** The max items. */
	private int maxItems = -1;

	/**
	 * Construct a cache using the specified capacity and name.
	 *
	 * @param maxItemsVal
	 *            the max items val
	 * @param cacheName
	 *            the cache name
	 */
	public DefaultSimpleCacheEMF(String maxItemsVal, String cacheName) {
		if (maxItemsVal == null) {
			throw new IllegalArgumentException("maxItems must be a positive integer, but was "
					+ maxItemsVal);
		}
		maxItems = Integer.parseInt(maxItemsVal);
		setBeanName(cacheName);

		// The map will have a bounded size determined by the maxItems member variable.
		map = new ConcurrentLinkedHashMap.Builder<K, AbstractMap.SimpleImmutableEntry<K, V>>()
				.maximumWeightedCapacity(maxItems).concurrencyLevel(32)
				.weigher(Weighers.singleton()).build();
	}

	/**
	 * Instantiates a new default simple cache emf.
	 */
	public DefaultSimpleCacheEMF() {
		this("100000", null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(K key) {
		return map.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<K> getKeys() {
		return map.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(K key) {
		AbstractMap.SimpleImmutableEntry<K, V> kvp = map.get(key);
		if (kvp == null) {
			return null;
		}
		return kvp.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(K key, V value) {
		AbstractMap.SimpleImmutableEntry<K, V> kvp = new AbstractMap.SimpleImmutableEntry<K, V>(
				key, value);
		if (TRACE_ENABLED && this.cacheInformationRegistry != null) {
			if (map.size() >= maxItems) {
				cacheInformationRegistry.indicate(beanName,
						CacheInformationRegistry.CacheStatus.FULL, " key:" + key + " and value:"
								+ value);
			} else {
				cacheInformationRegistry.indicate(beanName,
						CacheInformationRegistry.CacheStatus.PUT, " key:" + key + " and value:"
								+ value);
			}
		}
		map.put(key, kvp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(K key) {
		map.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		map.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "DefaultSimpleCache[maxItems=" + map.capacity() + ", cacheName=" + beanName + "]";
	}

	/**
	 * Sets the maximum number of items that the cache will hold.
	 *
	 * @param maxItems
	 *            the new max items
	 */
	public void setMaxItems(int maxItems) {
		map.setCapacity(maxItems);
	}

	/**
	 * Gets the max items.
	 *
	 * @return the max items
	 */
	public int getMaxItems() {
		return maxItems;
	}

	/**
	 * Since there are many cache instances, it is useful to be able to associate a name with each
	 * one.
	 *
	 * @param cacheName
	 *            Set automatically by Spring, but can be set manually if required.
	 */
	@Override
	public void setBeanName(String cacheName) {
		this.beanName = cacheName;
	}

	/**
	 * Setter method for cacheInformationRegistry.
	 *
	 * @param cacheInformationRegistry
	 *            the cacheInformationRegistry to set
	 */
	public void setCacheInformationRegistry(CacheInformationRegistry cacheInformationRegistry) {
		this.cacheInformationRegistry = cacheInformationRegistry;
		if (this.cacheInformationRegistry != null && beanName != null) {
			this.cacheInformationRegistry.register(this, beanName);
		}
	}
}
