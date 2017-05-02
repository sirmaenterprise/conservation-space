/*
 *
 */
package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Default implementation of {@link InstanceLoader} interface that uses a provided {@link InstancePersistCallback}
 * .Single place for loading an instance or instances. This is valid when all instances are loaded for a single source.
 *
 * @author BBonev
 */
public class DefaultInstanceLoader implements InstanceLoader {

	private final InstanceConverter instanceConverter;
	private final InstancePersistCallback instancePersistCallback;

	private final InstanceLoadCallback primaryIdLoadCallback;
	private final InstanceLoadCallback secondaryIdLoadCallback;

	/**
	 * Instantiates a new default instance loader.
	 *
	 * @param instancePersistCallback
	 *            the instance persist callback
	 */
	public DefaultInstanceLoader(InstancePersistCallback instancePersistCallback) {
		this.instancePersistCallback = instancePersistCallback;
		instanceConverter = instancePersistCallback.getInstanceConverter();
		primaryIdLoadCallback = instancePersistCallback.getPrimaryIdLoadHandler();
		secondaryIdLoadCallback = instancePersistCallback.getSecondaryIdLoadHandler();
	}

	@Override
	public <I extends Instance> I find(Serializable id) {
		return toInstance(getEntityCached(id, primaryIdLoadCallback), true);
	}

	@Override
	public <I extends Instance> I findBySecondaryId(Serializable dmsId) {
		return toInstance(getEntityCached(dmsId, secondaryIdLoadCallback), true);
	}

	/**
	 * To instance.
	 *
	 * @param <I>
	 *            the generic type
	 * @param entity
	 *            the entity
	 * @param loadProperties
	 *            the load properties
	 * @return the i
	 */
	@SuppressWarnings("unchecked")
	protected <I extends Instance> I toInstance(Entity<? extends Serializable> entity, boolean loadProperties) {
		if (entity == null) {
			return null;
		}
		if (instanceConverter == null) {
			if (entity instanceof Instance) {
				return (I) Instance.class.cast(entity);
			}
			throw new EmfRuntimeException("Enabled to convert entity to Instance: no converter provided!");
		}
		I convert = (I) instanceConverter.convertToInstance(entity);
		if (loadProperties && convert != null) {
			instancePersistCallback.onInstanceConverted(entity, convert);
		}
		return convert;
	}

	@Override
	public <I extends Instance> Collection<I> load(Collection<? extends Serializable> ids) {
		return batchLoad(ids, primaryIdLoadCallback);
	}

	@Override
	public <I extends Instance> Collection<I> loadBySecondaryId(Collection<? extends Serializable> ids) {
		return batchLoad(ids, secondaryIdLoadCallback);
	}

	@Override
	public InstancePersistCallback getPersistCallback() {
		return instancePersistCallback;
	}

	/**
	 * Gets the entity cache mapped by primary key and DMS secondary key.
	 *
	 * @return the cache
	 */
	protected EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> getCache() {
		return instancePersistCallback.getCache();
	}

	/**
	 * Gets a cached entity instance by primary or secondary key.
	 *
	 * @param dbId
	 *            the id
	 * @param loadCallback
	 *            the load callback
	 * @return the case entity cache
	 */
	protected Entity<? extends Serializable> getEntityCached(Serializable dbId, InstanceLoadCallback loadCallback) {
		if (dbId != null) {
			return loadCallback.lookupById(dbId, getCache());
		}
		return null;
	}

	/**
	 * Batch load of instances by secondary keys.
	 *
	 * @param <E>
	 *            the concrete instance type
	 * @param <S>
	 *            the generic type
	 * @param <T>
	 *            the concrete entity type
	 * @param ids
	 *            the needed to load
	 * @param loadCallback
	 *            the load callback
	 * @return the list fetched list
	 */
	protected <E extends Instance, S extends Serializable, T extends Entity<? extends Serializable>> List<E> batchLoad(
			Collection<S> ids, InstanceLoadCallback loadCallback) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}
		Set<S> secondPass = new LinkedHashSet<>();
		Map<S, E> result = new LinkedHashMap<>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache = getCache();
		List<E> toLoadProps = new ArrayList<>(ids.size());
		preFetchFromCache(ids, secondPass, result, cache, toLoadProps, loadCallback);

		if (!secondPass.isEmpty()) {
			doFragmentedLoading(secondPass, result, cache, toLoadProps, loadCallback, getMaxQueryElementParamSize());
		}

		// load properties of all entries with a single call
		batchFetchProperties(toLoadProps);

		return buildResultFromMapping(ids, result);
	}

	/**
	 * Builds the result from mapping.
	 *
	 * @param <E>
	 *            the element type
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param result
	 *            the result
	 * @return the list
	 */
	private static <E extends Instance, S extends Serializable> List<E> buildResultFromMapping(Collection<S> ids,
			Map<S, E> result) {
		// sort the results
		List<E> sortedResult = new ArrayList<>(result.size());
		for (Serializable key : ids) {
			E instance = result.get(key);
			if (instance != null) {
				sortedResult.add(instance);
			}
		}
		return sortedResult;
	}

	/**
	 * Batch fetch properties for the given list of instances.
	 *
	 * @param <E>
	 *            the element type
	 * @param toLoadProps
	 *            the to load props
	 */
	protected <E extends Instance> void batchFetchProperties(List<E> toLoadProps) {
		if (CollectionUtils.isEmpty(toLoadProps)) {
			return;
		}
		instancePersistCallback.onBatchConvertedInstances(toLoadProps);
	}

	/**
	 * Pre fetch from cache.
	 *
	 * @param <E>
	 *            the element type
	 * @param ids
	 *            the ids
	 * @param instanceClass
	 *            the instance class
	 * @param instanceDao
	 *            the instance dao
	 * @param secondPass
	 *            the second pass
	 * @param result
	 *            the result
	 * @param cache
	 *            the cache
	 * @param toLoadProps
	 *            the to load props
	 */
	@SuppressWarnings("unchecked")
	private <E extends Instance, K extends Serializable> void preFetchFromCache(Collection<K> ids, Set<K> secondPass,
			Map<K, E> result, EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache,
			Collection<E> toLoadProps, InstanceLoadCallback loadCallback) {
		if (cache == null) {
			secondPass.addAll(ids);
			return;
		}
		for (Serializable id : ids) {
			Entity<? extends Serializable> entity = loadCallback.getFromCacheById(id, cache);
			// for some reason sometimes the value returned is null
			if (entity != null) {
				E instance = (E) toInstance(entity, false);
				toLoadProps.add(instance);

				result.put((K) id, instance);
				continue;
			}
			// no cache or not found in cache search later in DB
			secondPass.add((K) id);
		}
	}

	/**
	 * Perform actual external (database) loading of the data. The loading is done in chunks.
	 *
	 * @param <E>
	 *            the element type
	 * @param secondPass
	 *            the second pass
	 * @param result
	 *            the result
	 * @param cache
	 *            the cache
	 * @param toLoadProps
	 *            the to load props
	 * @param loadCallback
	 *            the load callback
	 * @param chunkSize
	 *            the chunk size
	 */
	private <E extends Instance, S extends Serializable> void doFragmentedLoading(Collection<S> secondPass,
			final Map<S, E> result,
			final EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache,
			final Collection<E> toLoadProps, final InstanceLoadCallback loadCallback, int chunkSize) {

		FragmentedWork.doWork(secondPass, chunkSize, f -> doSecondPass(f, result, cache, toLoadProps, loadCallback));
	}

	/**
	 * Do second pass.
	 *
	 * @param <E>
	 *            the element type
	 * @param instanceClass
	 *            the instance class
	 * @param instanceDao
	 *            the instance dao
	 * @param secondPass
	 *            the second pass
	 * @param result
	 *            the result
	 * @param cache
	 *            the cache
	 * @param toLoadProps
	 *            the to load props
	 */
	@SuppressWarnings("unchecked")
	private <E extends Instance, S extends Serializable> void doSecondPass(Collection<S> secondPass, Map<S, E> result,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache,
			Collection<E> toLoadProps, InstanceLoadCallback loadCallback) {
		// fetch everything else from DB and update cache
		Collection<Entity<? extends Serializable>> list = loadCallback.loadPersistedEntities(secondPass);

		if (!list.isEmpty()) {
			for (Entity<? extends Serializable> entity : list) {
				// update cache
				loadCallback.addEntityToCache(entity, cache);

				E instance = toInstance(entity, false);
				toLoadProps.add(instance);

				result.put((S) loadCallback.getId(instance), instance);
			}
		}
	}

	/**
	 * Gets the max query element param size.
	 *
	 * @return the max query element param size
	 */
	private int getMaxQueryElementParamSize() {
		return Math.min(Math.max(instancePersistCallback.getMaxBatchSize(), 512), Integer.MAX_VALUE);
	}
}
