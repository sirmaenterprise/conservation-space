/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of
 * Alfresco Alfresco is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. Alfresco is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.emf.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.cache.SimpleCache;

/**
 * A cache backed by a simple <code>HashMap</code>.
 * <p>
 * <b>Note:</b> This cache is not transaction- or thread-safe. Use it for
 * single-threaded tests only.
 * 
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 * @author Derek Hulley
 * @since 3.2
 */
@Alternative
public class MemoryCache<K extends Serializable, V extends Object> implements SimpleCache<K, V> {

	/** The map. */
	private Map<K, V> map;

	/**
	 * Instantiates a new memory cache.
	 */
	public MemoryCache() {
		map = new ConcurrentHashMap<K, V>(250);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(K key) {
		return map.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<K> getKeys() {
		return map.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public V get(K key) {
		return map.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(K key, V value) {
		map.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove(K key) {
		map.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		map.clear();
	}
}
