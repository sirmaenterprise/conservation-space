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
import java.util.Collections;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.cache.SimpleCache;

/**
 * A cache that does nothing - always.
 * <P/>
 * There are conditions under which code that expects to be caching, should not
 * be. Using this cache, it becomes possible to configure a valid cache in
 * whilst still ensuring that the actual caching is not performed.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Derek Hulley
 */
@Alternative
public class NullCache<K extends Serializable, V extends Object> implements SimpleCache<K, V> {
	
	/**
	 * Instantiates a new null cache.
	 */
	public NullCache() {
	}

	/**
	 * NO-OP.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	public boolean contains(K key) {
		return false;
	}

	/**
	* {@inheritDoc}
	*/
	public Collection<K> getKeys() {
		return Collections.<K> emptyList();
	}

	/**
	 * NO-OP.
	 *
	 * @param key the key
	 * @return the v
	 */
	public V get(K key) {
		return null;
	}

	/**
	 * NO-OP.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void put(K key, V value) {
		return;
	}

	/**
	 * NO-OP.
	 *
	 * @param key the key
	 */
	public void remove(K key) {
		return;
	}

	/**
	 * NO-OP.
	 */
	public void clear() {
		return;
	}
}
