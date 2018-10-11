/*
 * Copyright (C) 2005-2011 Alfresco Software Limited. This file is part of Alfresco Alfresco is free
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

import org.alfresco.repo.cache.CacheInformationRegistry;
import org.alfresco.repo.cache.TransactionalCache;
import org.apache.log4j.Logger;

/**
 * A 2-level cache that mainains both a transaction-local cache and wraps a non-transactional
 * (shared) cache.
 * <p>
 * It uses the <b>Ehcache</b> <tt>Cache</tt> for it's per-transaction caches as these provide
 * automatic size limitations, etc.
 * <p>
 * Instances of this class <b>do not require a transaction</b>. They will work directly with the
 * shared cache when no transaction is present. There is virtually no overhead when running
 * out-of-transaction.
 * <p>
 * The first phase of the commit ensures that any values written to the cache in the current
 * transaction are not already superceded by values in the shared cache. In this case, the
 * transaction is failed for concurrency reasons and will have to retry. The second phase occurs
 * post-commit. We are sure that the transaction committed correctly, but things may have changed in
 * the cache between the commit and post-commit. If this is the case, then the offending values are
 * merely removed from the shared cache.
 * <p>
 * When the cache is {@link #clear() cleared}, a flag is set on the transaction. The shared cache,
 * instead of being cleared itself, is just ignored for the remainder of the tranasaction. At the
 * end of the transaction, if the flag is set, the shared transaction is cleared <i>before</i>
 * updates are added back to it.
 * <p>
 * Because there is a limited amount of space available to the in-transaction caches, when either of
 * these becomes full, the cleared flag is set. This ensures that the shared cache will not have
 * stale data in the event of the transaction-local caches dropping items. It is therefore important
 * to size the transactional caches correctly.
 *
 * @author Derek Hulley
 * @param <K> the key type
 * @param <V> the value type
 */
public class TransactionalCacheEMF<K extends Serializable, V extends Object> extends
		TransactionalCache<K, V> {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(CacheInformationRegistryImpl.class);

	/** The Constant TRACE_ENABLED. */
	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();

	/** The cache information registry. */
	private CacheInformationRegistry cacheInformationRegistry;

	/** The max cache size local. */
	private int maxCacheSizeLocal;

	/**
	 * Setter method for cacheInformationRegistry.
	 *
	 * @param cacheInformationRegistry
	 *            the cacheInformationRegistry to set
	 */
	public void setCacheInformationRegistry(CacheInformationRegistry cacheInformationRegistry) {
		this.cacheInformationRegistry = cacheInformationRegistry;
		if (cacheInformationRegistry != null) {
			cacheInformationRegistry.register(this, toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMaxCacheSize(int maxCacheSize) {
		maxCacheSizeLocal = maxCacheSize;
		super.setMaxCacheSize(maxCacheSize);
	}

	/**
	 * Gets the max cache size.
	 *
	 * @return the max cache size
	 */
	public int getMaxCacheSize() {
		return maxCacheSizeLocal;
	}

	/**
	 * Gets the cache information registry.
	 *
	 * @return the cache information registry
	 */
	public CacheInformationRegistry getCacheInformationRegistry() {
		return cacheInformationRegistry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put(K keyIn, V value) {
		super.put(keyIn, value);
		if (TRACE_ENABLED && this.cacheInformationRegistry != null) {
			cacheInformationRegistry.indicate(toString(), CacheInformationRegistry.CacheStatus.PUT,
					" key:" + keyIn + " and value:" + value);
		}
	}
}
