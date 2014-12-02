package com.sirma.itt.emf.security.registry;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.provider.MapProvider;
import com.sirma.itt.emf.provider.ProviderRegistry;
import com.sirma.itt.emf.provider.event.ProviderRegistryEventBinding;
import com.sirma.itt.emf.provider.event.RegisterEvent;
import com.sirma.itt.emf.security.model.Sealable;

/**
 * The Class BaseProvider.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
public abstract class BaseProviderRegistry<K extends Serializable, V> implements ProviderRegistry<K, V> {

	private static final long REFRESH_DATA_TIMEOUT = 2 * 60 * 1000;

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;

	/** The all roles. */
	private Set<K> keys;

	@Inject
	private EventService eventService;

	@Inject
	private Logger logger;

	private ProviderRegistryEventBinding registerEventBinding;

	/**
	 * Temporary timed cache
	 */
	private long lastRefreshTime = System.currentTimeMillis();
	private Map<K, V> lastRefreshData;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		if (!cacheContext.containsCache(getCacheName())) {
			cacheContext.createCache(getCacheName(), new ProviderCacheLookup<K, V>());
		}
		registerEventBinding = new ProviderRegistryEventBinding(getProviderSelector());

		reload();
	}

	/**
	 * Process provider data.
	 *
	 * @param data
	 *            the data
	 */
	private void processProviderData(Map<K, V> data) {
		EntityLookupCache<K, V, Serializable> cache = getCache();
		RegisterEvent event = new RegisterEvent();
		for (Entry<K, V> entry : data.entrySet()) {
			// fire event
			V value = entry.getValue();

			event.setPayload(value);
			eventService.fire(event, registerEventBinding);

			if (value instanceof Sealable) {
				Sealable sealable = (Sealable) value;
				if (!sealable.isSealed()) {
					sealable.seal();
				}
			}
			cache.setValue(entry.getKey(), value);
		}

		keys = Collections.unmodifiableSet(new LinkedHashSet<K>(data.keySet()));
	}

	/**
	 * Notify for registration of new value if mutable.
	 *
	 * @param value
	 *            the value
	 */
	private void notifyForRegistration(V value) {
		if (value instanceof Sealable) {
			Sealable sealable = (Sealable) value;
			if (sealable.isSealed()) {
				return;
			}
		}
		RegisterEvent event = new RegisterEvent(value);
		eventService.fire(event, registerEventBinding);

		if (value instanceof Sealable) {
			Sealable sealable = (Sealable) value;
			if (!sealable.isSealed()) {
				sealable.seal();
			}
		}
	}

	/**
	 * Gets the all roles.
	 *
	 * @return the all roles
	 */
	@Override
	public Set<K> getKeys() {
		return keys;
	}

	/**
	 * Find role by role Id.
	 *
	 * @param roleId
	 *            the role id
	 * @return the user role
	 */
	@Override
	public V find(K roleId) {
		if (roleId == null) {
			return null;
		}
		Pair<K, V> pair = getCache().getByKey(roleId);
		if (pair == null) {
			return null;
		}
		return pair.getSecond();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reload() {
		processProviderData(refreshProviderData());
	}

	/**
	 * Gets the user roles cache cache.
	 *
	 * @return the cache
	 */
	protected EntityLookupCache<K, V, Serializable> getCache() {
		return cacheContext.getCache(getCacheName());
	}

	/**
	 * Refresh user roles.
	 *
	 * @return the map
	 */
	protected Map<K, V> refreshProviderData() {
		if (lastRefreshData != null) {
			// cache the result for at least 2 minutes
			// this is because when initializing action and role providers the method for refresh if
			// called every time when there is an entry not found in the cache
			if ((System.currentTimeMillis() - lastRefreshTime) < REFRESH_DATA_TIMEOUT) {
				return lastRefreshData;
			}
			// clear data for GC
			lastRefreshData.clear();
			lastRefreshData = null;
		}

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (MapProvider<K, V> provider : getProviders()) {
			Map<K, V> data = provider.provide();
			if (logger.isTraceEnabled()) {
				logger.trace(provider.getClass() + " provided the following actions: "
						+ data.keySet());
			}
			if (data != null) {
				result.putAll(data);
			}
		}
		lastRefreshData = result;
		return result;
	}

	/**
	 * Gets the cache name.
	 *
	 * @return the cache name
	 */
	protected abstract String getCacheName();

	/**
	 * Gets the providers.
	 *
	 * @param <E>
	 *            the element type
	 * @return the providers
	 */
	protected abstract <E extends MapProvider<K, V>> Iterable<E> getProviders();

	/**
	 * Gets the provider selector used when firing events for registering.
	 *
	 * @return the provider selector
	 */
	protected abstract String getProviderSelector();

	/**
	 * The Class RoleCacheLookup.
	 *
	 * @param <KK>
	 *            the generic type
	 * @param <VV>
	 *            the generic type
	 * @author BBonev
	 */
	class ProviderCacheLookup<KK extends Serializable, VV> extends
			EntityLookupCallbackDAOAdaptor<KK, VV, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Pair<KK, VV> findByKey(KK key) {
			Map<K, V> data = refreshProviderData();
			if (data.containsKey(key)) {
				V value = data.get(key);
				notifyForRegistration(value);
				return new Pair<KK, VV>(key, (VV) value);
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<KK, VV> createValue(VV value) {
			throw new UnsupportedOperationException("Objects are created externally");
		}

	}
}