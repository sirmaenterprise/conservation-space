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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.alfresco.repo.cache.CacheInformationRegistry;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.TransactionalCache;
import org.apache.log4j.Logger;

/**
 * The CacheInformationRegistryImpl hold the registered caches and prints at fixed rate the cache
 * statuses.
 */
public class CacheInformationRegistryImpl implements CacheInformationRegistry {
	/** The Constant TIMER. */
	private static final Timer TIMER = new Timer();
	/** The Constant LOGGER. */
	public static final Logger LOGGER = Logger.getLogger(CacheInformationRegistryImpl.class);

	/** The transactional cache. */
	public static Map<String, TransactionalCache<?, ?>> transactionalCacheRegistry = new HashMap<String, TransactionalCache<?, ?>>(
			50);

	/** The simple cache. */
	public static Map<String, SimpleCache<?, ?>> simpleCacheRegistry = new HashMap<String, SimpleCache<?, ?>>(
			50);

	/** The info timeout - 10 min. */
	private Long infoTimeout = new Long(600000L);

	/**
	 * The Class TimerStatistic.
	 */
	private class CacheStatisticTimer extends TimerTask {

		/** The id. */
		private String id;

		/** The transactional. */
		private boolean transactional;

		/**
		 * Instantiates a new timer statistic.
		 *
		 * @param id
		 *            the id
		 * @param transactional
		 *            the transactional
		 */
		private CacheStatisticTimer(String id, boolean transactional) {
			this.id = id;
			this.transactional = transactional;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			String additional = null;
			if (transactional) {
				TransactionalCache<?, ?> transactional = transactionalCacheRegistry.get(id);
				additional = " Current size: " + transactional.getKeys().size();
				if (transactional instanceof TransactionalCacheEMF) {
					additional += " of "
							+ ((TransactionalCacheEMF<?, ?>) transactional).getMaxCacheSize();
				}
			} else {
				SimpleCache<?, ?> simple = simpleCacheRegistry.get(id);
				additional = " Current size: " + simple.getKeys().size();
				if (simple instanceof DefaultSimpleCacheEMF) {
					additional += " of " + ((DefaultSimpleCacheEMF<?, ?>) simple).getMaxItems();
				}
			}
			indicate(id, CacheInformationRegistry.CacheStatus.INFO, additional);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void register(TransactionalCache<?, ?> cache, String cacheName) {
		if (cache == null || cacheName == null) {
			throw new RuntimeException(cache == null ? "null : " + cacheName : cache.toString()
					+ " : " + cacheName);
		}
		if (transactionalCacheRegistry.containsKey(cacheName)) {
			throw new RuntimeException(cacheName);
		}
		transactionalCacheRegistry.put(cacheName, cache);
		TIMER.scheduleAtFixedRate(new CacheStatisticTimer(cacheName, true), infoTimeout,
				infoTimeout);
		if (cache instanceof TransactionalCacheEMF<?, ?>) {
			LOGGER.info("Registered EMF TransactionalCache '" + cacheName + "' with size: "
					+ ((TransactionalCacheEMF<?, ?>) cache).getMaxCacheSize());
		} else {
			LOGGER.info("Registered TransactionalCache '" + cacheName + "'");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void register(SimpleCache<?, ?> cache, String cacheName) {
		if (cache == null || cacheName == null) {
			throw new RuntimeException(cache == null ? "null : " + cacheName : cache.toString()
					+ " : " + cacheName);
		}
		if (simpleCacheRegistry.containsKey(cacheName)) {
			throw new RuntimeException(cache.toString());
		}
		simpleCacheRegistry.put(cacheName, cache);
		TIMER.scheduleAtFixedRate(new CacheStatisticTimer(cacheName, false), infoTimeout,
				infoTimeout);
		if (cache instanceof DefaultSimpleCacheEMF) {
			LOGGER.info("Registered EMF DefaultSimpleCache '" + cacheName + "' with size: "
					+ ((DefaultSimpleCacheEMF<?, ?>) cache).getMaxItems());
		} else {
			LOGGER.info("Registered SimpleCache '" + cacheName + "'");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void indicate(String id, CacheStatus status, String message) {
		if (id == null) {
			return;
		}
		LOGGER.info(status + (message != null ? message : "") + " for " + id);

	}

	/**
	 * Getter method for infoTimeout.
	 *
	 * @return the infoTimeout
	 */
	public Long getInfoTimeout() {
		return infoTimeout;
	}

	/**
	 * Setter method for infoTimeout.
	 *
	 * @param infoTimeout
	 *            the infoTimeout to set
	 */
	public void setInfoTimeout(Long infoTimeout) {
		this.infoTimeout = infoTimeout;
	}

}
