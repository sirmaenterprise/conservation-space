package com.sirma.itt.emf.cls.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceLoader;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.util.PropertiesEvaluationHelper;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Class with common methods for working with different instances.
 *
 * @author BBonev
 * @param <P>
 *            the primary key type
 * @param <K>
 *            the secondary key type
 * @deprecated This should be replaces with {@link InstanceLoader}
 */
@Deprecated
public abstract class OldBaseInstanceDao<P extends Serializable, K extends Serializable> {

	/**
	 * The query limit for most relational databases is 2^16, but to be safe we will do only 2^15.
	 */
	public static final int MAX_QUERY_ELEMENT_COUNT = 2 << 15;

	/** The properties dao. */
	@Inject
	protected PropertiesService propertiesService;

	/** The evaluator manager. */
	@Inject
	protected ExpressionsManager evaluatorManager;

	/** The dozer mapper. */
	@Inject
	protected ObjectMapper dozerMapper;

	@Inject
	protected SecurityContext securityContext;

	@Inject
	private DatabaseIdManager idManager;

	/**
	 * Converts the given source object to destination class. If the last argument is true then the conversion is done
	 * for the complete tree, otherwise only local copy is performed and relations are not followed.
	 *
	 * @param <S>
	 *            the generic source type
	 * @param <D>
	 *            the generic destination type
	 * @param source
	 *            the source object instance
	 * @param dest
	 *            the destination class type
	 * @param fullTreeConvert
	 *            the full tree convert
	 * @return the created and populated object
	 */
	protected <S, D> D convert(S source, Class<D> dest, boolean fullTreeConvert) {
		return convert(source, dest);
	}

	/**
	 * Converts the given source object to destination class. If the last argument is true then the conversion is done
	 * for the complete tree, otherwise only local copy is performed and relations are not followed.
	 *
	 * @param <S>
	 *            the generic source type
	 * @param <D>
	 *            the generic destination type
	 * @param source
	 *            the source object instance
	 * @param dest
	 *            the destination class type
	 * @return the created and populated object
	 */
	protected <S, D> D convert(S source, Class<D> dest) {
		return dozerMapper.map(source, dest);
	}

	/**
	 * Sets the current user to.
	 *
	 * @param model
	 *            the model
	 * @param key
	 *            the key
	 */
	protected void setCurrentUserTo(PropertyModel model, String key) {
		model.add(key, securityContext.getAuthenticated().getSystemId());
	}

	/**
	 * Populate properties.
	 *
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 * @param fields
	 *            the fields
	 */
	protected <E extends PropertyDefinition> void populateProperties(PropertyModel model, List<E> fields) {
		PropertiesEvaluationHelper.populateProperties(model, fields, evaluatorManager, false, idManager);
	}

	/**
	 * Convert entity.
	 *
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @param target
	 *            the target
	 * @param toLoadProps
	 *            the to load props
	 * @param forBatchLoad
	 *            <code>true</code> if the given instance has been converted for batch loading
	 * @param dao
	 *            the dao
	 * @return the e
	 */
	protected <E extends Instance> E convertEntity(Entity<?> entity, Class<E> target, boolean toLoadProps,
			boolean forBatchLoad, InstanceDao dao) {
		E instance = convert(entity, target);
		loadChildren(instance, forBatchLoad, dao);
		if (instance instanceof BidirectionalMapping) {
			((BidirectionalMapping) instance).initBidirection();
		}
		dao.synchRevisions(instance, instance.getRevision());
		if (toLoadProps) {
			dao.loadProperties(instance);
		}
		// optional load the extra data from DMS system

		// end DMS load
		return instance;
	}

	/**
	 * The method is called when an instance is being loaded, so that the instance children to be added before
	 * properties loading if required.
	 *
	 * @param <E>
	 *            the instance type that is being loaded
	 * @param instance
	 *            the instance to update
	 * @param forBatchLoad
	 *            <code>true</code> if the given instance has been converted for batch loading
	 * @param dao
	 *            the current dao instance
	 */
	protected abstract <E extends Instance> void loadChildren(E instance, boolean forBatchLoad, InstanceDao dao);

	/**
	 * Gets the current entity cache.
	 *
	 * @return the cache
	 */
	@SuppressWarnings("rawtypes")
	protected EntityLookupCache<Serializable, Entity, K> getCache() {
		return null;
	}

	/**
	 * Load instance internal by the given primary or secondary key.
	 *
	 * @param <E>
	 *            the element type
	 * @param id
	 *            the primary key
	 * @param otherId
	 *            the secondary key
	 * @param loadProperties
	 *            the load properties
	 * @return the loaded instance
	 */
	protected abstract <E extends Instance> E loadInstanceInternal(P id, K otherId, boolean loadProperties);

	/**
	 * Find entities in the DB.
	 *
	 * @param <E>
	 *            the element type
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	protected abstract <E extends Entity<P>> List<E> findEntities(Set<K> ids);

	/**
	 * Find entities by primary key.
	 *
	 * @param <E>
	 *            the element type
	 * @param ids
	 *            the ids
	 * @return the list
	 */
	protected abstract <E extends Entity<P>> List<E> findEntitiesByPrimaryKey(Collection<P> ids);

	/**
	 * Batch fetch properties for the given list of instances.
	 *
	 * @param <E>
	 *            the element type
	 * @param toLoadProps
	 *            the to load props
	 * @param loadAll
	 *            the load all properties for the given list of instances
	 */
	protected <E extends Instance> void batchFetchProperties(List<E> toLoadProps, boolean loadAll) {
		if (toLoadProps == null || toLoadProps.isEmpty()) {
			return;
		}
		propertiesService.loadProperties(toLoadProps, loadAll);
	}

	/**
	 * Gets the secondary key for the given instance, used for grouping of retrieved entities.
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @return the secondary key
	 */
	protected abstract <E extends Instance> K getSecondaryKey(E instance);

	/**
	 * Gets the primary key.
	 *
	 * @param <E>
	 *            the element type
	 * @param entity
	 *            the entity
	 * @return the primary key
	 */
	protected abstract <E extends Entity<?>> P getPrimaryKey(E entity);

	/**
	 * Batch load of instances by secondary keys.
	 *
	 * @param <E>
	 *            the concrete instance type
	 * @param <T>
	 *            the concrete entity type
	 * @param ids
	 *            the needed to load
	 * @param instanceClass
	 *            the instance class
	 * @param instanceDao
	 *            the invoking instance DAO
	 * @param loadAll
	 *            the load all properties for the loaded instances
	 * @return the list fetched list
	 */
	@SuppressWarnings("rawtypes")
	protected <E extends Instance, T extends Entity<P>> List<E> batchLoad(List<K> ids, Class<E> instanceClass,
			InstanceDao instanceDao, boolean loadAll) {
		Set<K> secondPass = new LinkedHashSet<>();
		Map<K, E> result = new LinkedHashMap<>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, Entity, K> cache = getCache();
		List<E> toLoadProps = new ArrayList<>(ids.size());
		preFetchFromCache(ids, instanceClass, instanceDao, secondPass, result, cache, toLoadProps);

		if (!secondPass.isEmpty()) {
			doSecondPass(instanceClass, instanceDao, secondPass, result, cache, toLoadProps);
		}

		// load properties of all entries with a single call
		batchFetchProperties(toLoadProps, loadAll);

		// sort the results
		List<E> sortedResult = new ArrayList<>(result.size());
		for (K key : ids) {
			E instance = result.get(key);
			if (instance != null) {
				sortedResult.add(instance);
			}
		}
		return sortedResult;
	}

	/**
	 * Do second pass.
	 *
	 * @param <E>
	 *            the element type
	 * @param <T>
	 *            the generic type
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
	@SuppressWarnings("rawtypes")
	private <E extends Instance, T extends Entity<P>> void doSecondPass(Class<E> instanceClass,
			InstanceDao instanceDao, Set<K> secondPass, Map<K, E> result,
			EntityLookupCache<Serializable, Entity, K> cache, List<E> toLoadProps) {
		// fetch everything else from DB and update cache
		List<T> list = findEntities(secondPass);

		if (!list.isEmpty()) {
			for (T entity : list) {
				if (cache != null) {
					// update cache
					cache.setValue(getPrimaryKey(entity), entity);
				}

				E instance = convertEntity(entity, instanceClass, false, true, instanceDao);
				toLoadProps.add(instance);

				result.put(getSecondaryKey(instance), instance);
			}
		}
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
	@SuppressWarnings("rawtypes")
	private <E extends Instance> void preFetchFromCache(List<K> ids, Class<E> instanceClass, InstanceDao instanceDao,
			Set<K> secondPass, Map<K, E> result, EntityLookupCache<Serializable, Entity, K> cache,
			List<E> toLoadProps) {
		if (cache == null) {
			secondPass.addAll(ids);
			return;
		}
		for (K dmsId : ids) {
			Serializable key = cache.getKey(dmsId);
			if (key != null) {
				// convert the cache entry to instance and schedule
				// properties loading instead of one by one loading
				Entity entity = cache.getValue(key);
				// for some reason sometimes the value returned is null
				if (entity != null) {
					E instance = convertEntity(entity, instanceClass, false, true, instanceDao);
					toLoadProps.add(instance);

					result.put(dmsId, instance);
					continue;
				}
			}
			// no cache or not found in cache search later in DB
			secondPass.add(dmsId);
		}
	}

	/**
	 * Batch load of instances by secondary keys.
	 *
	 * @param <E>
	 *            the concrete instance type
	 * @param <T>
	 *            the concrete entity type
	 * @param ids
	 *            the needed to load
	 * @param instanceClass
	 *            the instance class
	 * @param instanceDao
	 *            the invoking instance DAO
	 * @param loadAll
	 *            the load all properties for the loaded instances
	 * @return the list fetched list
	 */
	@SuppressWarnings("rawtypes")
	protected <E extends Instance, T extends Entity<P>> List<E> batchLoadByPrimaryId(List<P> ids,
			Class<E> instanceClass, InstanceDao instanceDao, boolean loadAll) {
		Set<P> secondPass = new LinkedHashSet<>();
		Map<P, E> result = new LinkedHashMap<>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, Entity, K> cache = getCache();
		List<E> toLoadProps = new ArrayList<>(ids.size());

		preFetchFromCacheForPrimaryKey(ids, instanceClass, instanceDao, secondPass, result, cache, toLoadProps);

		if (!secondPass.isEmpty()) {
			doSecondPassByPrimaryKey(instanceClass, instanceDao, secondPass, result, cache, toLoadProps);
		}

		// load properties of all entries with a single call
		batchFetchProperties(toLoadProps, loadAll);

		// sort the results
		List<E> sortedResult = new ArrayList<>(result.size());
		for (P key : ids) {
			E instance = result.get(key);
			if (instance != null) {
				sortedResult.add(instance);
			}
		}
		return sortedResult;
	}

	/**
	 * Do second pass by primary key.
	 *
	 * @param <E>
	 *            the element type
	 * @param <T>
	 *            the generic type
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
	@SuppressWarnings("rawtypes")
	private <E extends Instance, T extends Entity<P>> void doSecondPassByPrimaryKey(Class<E> instanceClass,
			InstanceDao instanceDao, Set<P> secondPass, Map<P, E> result,
			EntityLookupCache<Serializable, Entity, K> cache, List<E> toLoadProps) {
		FragmentedWork.doWork(secondPass, getMaxQueryElementParamSize(),
				fragment -> executeFragmentedPrimaryKeyLoad(instanceClass, instanceDao, fragment, result, cache,
						toLoadProps));
	}

	/**
	 * Execute fragmented primary key load.
	 *
	 * @param <T>
	 *            the generic type
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
	@SuppressWarnings("rawtypes")
	private <T extends Entity<P>, E extends Instance> void executeFragmentedPrimaryKeyLoad(Class<E> instanceClass,
			InstanceDao instanceDao, Collection<P> secondPass, Map<P, E> result,
			EntityLookupCache<Serializable, Entity, K> cache, List<E> toLoadProps) {
		// fetch everything else from DB and update cache
		List<T> list = findEntitiesByPrimaryKey(secondPass);

		if (!list.isEmpty()) {
			for (T entity : list) {

				if (cache != null) {
					// update cache
					cache.setValue(getPrimaryKey(entity), entity);
				}

				E instance = convertEntity(entity, instanceClass, false, true, instanceDao);
				toLoadProps.add(instance);

				result.put(getPrimaryKey(instance), instance);
			}
		}
	}

	/**
	 * Gets the max query element parameter size. This is the size of a single parameter when it involves multiple
	 * elements for IN clause. Some databases has a limitation on the IN clause.
	 *
	 * @return the max query element param size
	 */
	protected int getMaxQueryElementParamSize() {
		return MAX_QUERY_ELEMENT_COUNT;
	}

	/**
	 * Pre fetch from cache for primary key.
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
	@SuppressWarnings("rawtypes")
	private <E extends Instance> void preFetchFromCacheForPrimaryKey(List<P> ids, Class<E> instanceClass,
			InstanceDao instanceDao, Set<P> secondPass, Map<P, E> result,
			EntityLookupCache<Serializable, Entity, K> cache, List<E> toLoadProps) {
		if (cache == null) {
			secondPass.addAll(ids);
			return;
		}
		for (P dbId : ids) {
			// convert the cache entry to instance and schedule
			// properties loading instead of one by one loading
			Entity entity = cache.getValue(dbId);
			if (entity != null) {
				E instance = convertEntity(entity, instanceClass, false, true, instanceDao);
				toLoadProps.add(instance);

				result.put(dbId, instance);
				continue;
			}
			// no cache or not found in cache search later in DB
			secondPass.add(dbId);
		}
	}
}
