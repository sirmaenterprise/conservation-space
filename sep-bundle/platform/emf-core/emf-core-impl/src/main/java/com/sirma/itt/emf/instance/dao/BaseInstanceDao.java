package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.dozer.DozerMapper;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.SecurityUtil;

/**
 * Class with common methods for working with different instances.
 * 
 * @author BBonev
 * @param <P>
 *            the primary key type
 * @param <K>
 *            the secondary key type
 */
public abstract class BaseInstanceDao<P extends Serializable, K extends Serializable> {

	/** The properties dao. */
	@Inject
	protected PropertiesService propertiesService;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseInstanceDao.class);

	/** The evaluator manager. */
	@Inject
	protected ExpressionsManager evaluatorManager;
	/** The authentication service. */
	@Inject
	protected javax.enterprise.inject.Instance<AuthenticationService> authenticationService;

	/** The dozer mapper. */
	@Inject
	protected DozerMapper dozerMapper;

	/**
	 * Converts the given source object to destination class. If the last
	 * argument is true then the conversion is done for the complete tree,
	 * otherwise only local copy is performed and relations are not followed.
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
	 * Converts the given source object to destination class. If the last
	 * argument is true then the conversion is done for the complete tree,
	 * otherwise only local copy is performed and relations are not followed.
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
		return dozerMapper.getMapper().map(source, dest);
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
		SecurityUtil.setCurrentUserTo(model, key, authenticationService);
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
	protected <E extends PropertyDefinition> void populateProperties(PropertyModel model,
			List<E> fields) {
		PropertiesUtil.populateProperties(model, fields, evaluatorManager, false);
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
	protected <E extends Instance> E convertEntity(Entity<?> entity, Class<E> target,
			boolean toLoadProps, boolean forBatchLoad, InstanceDao<E> dao) {
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
	 * The method is called when an instance is being loaded, so that the instance children to be
	 * added before properties loading if required.
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
	protected abstract <E extends Instance> void loadChildren(E instance, boolean forBatchLoad, InstanceDao<E> dao);

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
	protected abstract <E extends Instance> E loadInstanceInternal(P id, K otherId,
			boolean loadProperties);

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
	protected abstract <E extends Entity<P>> List<E> findEntitiesByPrimaryKey(Set<P> ids);

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
		if ((toLoadProps == null) || toLoadProps.isEmpty()) {
			return;
		}
		propertiesService.loadProperties(toLoadProps, loadAll);
	}

	/**
	 * Gets the secondary key for the given instance, used for grouping of
	 * retrieved entities.
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
	 * @param entityClass
	 *            the entity class, this is the entity class that is retrieved
	 *            from the DB
	 * @param instanceDao
	 *            the invoking instance DAO
	 * @param loadAll
	 *            the load all properties for the loaded instances
	 * @return the list fetched list
	 */
	@SuppressWarnings("rawtypes")
	protected <E extends Instance, T extends Entity<P>> List<E> batchLoad(List<K> ids,
			Class<E> instanceClass, Class<T> entityClass, InstanceDao<E> instanceDao,
			boolean loadAll) {
		Set<K> secondPass = new LinkedHashSet<K>();
		Map<K, E> result = new LinkedHashMap<K, E>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, Entity, K> cache = getCache();
		List<E> toLoadProps = new ArrayList<E>(ids.size());
		preFetchFromCache(ids, instanceClass, instanceDao, secondPass, result, cache, toLoadProps);

		if (!secondPass.isEmpty()) {
			doSecondPass(instanceClass, instanceDao, secondPass, result, cache, toLoadProps);
		}

		// load properties of all entries with a single call
		batchFetchProperties(toLoadProps, loadAll);

		// sort the results
		List<E> sortedResult = new ArrayList<E>(result.size());
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
			InstanceDao<E> instanceDao, Set<K> secondPass, Map<K, E> result,
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
	private <E extends Instance> void preFetchFromCache(List<K> ids, Class<E> instanceClass,
			InstanceDao<E> instanceDao, Set<K> secondPass, Map<K, E> result,
			EntityLookupCache<Serializable, Entity, K> cache, List<E> toLoadProps) {
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
	 * @param entityClass
	 *            the entity class, this is the entity class that is retrieved
	 *            from the DB
	 * @param instanceDao
	 *            the invoking instance DAO
	 * @param loadAll
	 *            the load all properties for the loaded instances
	 * @return the list fetched list
	 */
	@SuppressWarnings("rawtypes")
	protected <E extends Instance, T extends Entity<P>> List<E> batchLoadByPrimaryId(List<P> ids,
			Class<E> instanceClass, Class<T> entityClass, InstanceDao<E> instanceDao,
			boolean loadAll) {
		Set<P> secondPass = new LinkedHashSet<P>();
		Map<P, E> result = new LinkedHashMap<P, E>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		EntityLookupCache<Serializable, Entity, K> cache = getCache();
		List<E> toLoadProps = new ArrayList<E>(ids.size());

		preFetchFromCacheForPrimaryKey(ids, instanceClass, instanceDao, secondPass, result, cache,
				toLoadProps);

		if (!secondPass.isEmpty()) {
			doSecondPassByPrimaryKey(instanceClass, instanceDao, secondPass, result, cache, toLoadProps);
		}

		// load properties of all entries with a single call
		batchFetchProperties(toLoadProps, loadAll);

		// sort the results
		List<E> sortedResult = new ArrayList<E>(result.size());
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
	private <E extends Instance, T extends Entity<P>> void doSecondPassByPrimaryKey(
			Class<E> instanceClass, InstanceDao<E> instanceDao, Set<P> secondPass,
			Map<P, E> result, EntityLookupCache<Serializable, Entity, K> cache, List<E> toLoadProps) {
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
	private <E extends Instance> void preFetchFromCacheForPrimaryKey(List<P> ids,
			Class<E> instanceClass, InstanceDao<E> instanceDao, Set<P> secondPass,
			Map<P, E> result, EntityLookupCache<Serializable, Entity, K> cache, List<E> toLoadProps) {
		if (cache == null) {
			secondPass.addAll(ids);
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
