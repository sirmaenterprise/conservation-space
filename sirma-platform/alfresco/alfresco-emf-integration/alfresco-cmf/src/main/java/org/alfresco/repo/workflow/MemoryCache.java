/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.alfresco.repo.cache.SimpleCache;
import org.apache.commons.collections.map.LRUMap;

/**
 * A cache backed by {@link LRUMap}. Default size is provided as parameter
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author bbanchev
 */
public class MemoryCache<K extends Serializable, V extends Object> implements SimpleCache<K, V> {

	/** The map. */
	private Map<K, V> map;

	/** The max size. */
	private int maxSize;

	/**
	 * Instantiates a new memory cache.
	 *
	 * @param size
	 *            the size
	 */
	@SuppressWarnings("unchecked")
	public MemoryCache(int size) {

		this.maxSize = size;
		map = new LRUMap(maxSize);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.repo.cache.SimpleCache#contains(java.io.Serializable)
	 */
	@Override
	public boolean contains(K key) {
		return map.containsKey(key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.repo.cache.SimpleCache#getKeys()
	 */
	@Override
	public Collection<K> getKeys() {
		return map.keySet();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.repo.cache.SimpleCache#get(java.io.Serializable)
	 */
	@Override
	public V get(K key) {
		return map.get(key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.repo.cache.SimpleCache#put(java.io.Serializable,
	 * java.lang.Object)
	 */
	@Override
	public void put(K key, V value) {
		map.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.repo.cache.SimpleCache#remove(java.io.Serializable)
	 */
	@Override
	public void remove(K key) {
		map.remove(key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.repo.cache.SimpleCache#clear()
	 */
	@Override
	public void clear() {
		map.clear();
	}

}
