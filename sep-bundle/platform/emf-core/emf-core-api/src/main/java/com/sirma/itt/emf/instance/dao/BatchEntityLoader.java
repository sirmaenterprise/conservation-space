package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.concurrent.GenericAsyncTask;
import com.sirma.itt.emf.concurrent.TaskExecutor;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Helper class that implements optimized batch loading algorithms for entities and instances. Each
 * method requires a concrete callback implementation to work properly.
 * 
 * @author BBonev
 */
public class BatchEntityLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchEntityLoader.class);

	private static boolean trace = LOGGER.isTraceEnabled();

	/**
	 * Batch load of instances by secondary keys.
	 * 
	 * @param <P>
	 *            the generic type
	 * @param <E>
	 *            the concrete instance type
	 * @param ids
	 *            the needed to load
	 * @param cache
	 *            the cache
	 * @param callback
	 *            the callback
	 * @return the list fetched list
	 */
	public static <P extends Serializable, E extends Entity<?>> List<E> batchLoadByPrimaryKey(
			List<P> ids, EntityLookupCache<P, E, ?> cache,
			BatchPrimaryKeyEntityLoaderCallback<P, E> callback) {
		Set<P> secondPass = new LinkedHashSet<>();
		Map<P, E> result = new LinkedHashMap<>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		for (P dbId : ids) {
			if (cache != null) {
				// convert the cache entry to instance and schedule
				// properties loading instead of one by one loading
				E entity = cache.getValue(dbId);
				if (entity != null) {
					result.put(dbId, entity);
					continue;
				}
			}
			// no cache or not found in cache search later in DB
			secondPass.add(dbId);
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<E> list = callback.findEntitiesByPrimaryKey(secondPass);

			if (!list.isEmpty()) {
				for (E entity : list) {
					P primaryKey = callback.getPrimaryKey(entity);
					if (cache != null) {
						// update cache
						cache.setValue(primaryKey, entity);
					}
					result.put(primaryKey, entity);
				}
			}
		}

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
	 * Batch load of entities by secondary keys.
	 * 
	 * @param <P>
	 *            the generic type
	 * @param <E>
	 *            the concrete instance type
	 * @param <S>
	 *            the key type
	 * @param ids
	 *            the needed to load
	 * @param cache
	 *            the cache
	 * @param callback
	 *            the callback
	 * @return the list fetched list
	 */
	public static <P extends Serializable, E extends Entity<?>, S extends Serializable> List<E> batchLoadBySecondaryKey(
			List<S> ids, EntityLookupCache<P, E, S> cache,
			BatchSecondaryKeyEntityLoaderCallback<P, E, S> callback) {
		Set<S> secondPass = new LinkedHashSet<>();
		Map<S, E> result = new LinkedHashMap<>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		for (S dmsId : ids) {
			if (cache != null) {
				P key = cache.getKey(dmsId);
				if (key != null) {
					// convert the cache entry to instance and schedule
					// properties loading instead of one by one loading
					E entity = cache.getValue(key);
					// for some reason sometimes the value returned is null
					if (entity != null) {

						result.put(dmsId, entity);
						continue;
					}
				}
			}
			// no cache or not found in cache search later in DB
			secondPass.add(dmsId);
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<E> list = callback.findEntitiesBySecondaryKey(secondPass);
			if (!list.isEmpty()) {
				for (E entity : list) {
					if (cache != null) {
						// update cache
						cache.setValue(callback.getPrimaryKey(entity), entity);
					}
					result.put(callback.getSecondaryKey(entity), entity);
				}
			}
		}
		// sort the results
		List<E> sortedResult = new ArrayList<>(result.size());
		for (S key : ids) {
			E instance = result.get(key);
			if (instance != null) {
				sortedResult.add(instance);
			}
		}
		return sortedResult;
	}

	/**
	 * Load the given list of instance pairs. The pairs should contain the actual instance class and
	 * the primary id of the instance. If executor implementation is provided then the entries will
	 * be loaded the provided executor, otherwise will be loaded sequentially. The loaded elements
	 * has the same order as the original list.
	 * 
	 * @param list
	 *            the list of pairs to load
	 * @param serviceRegister
	 *            the service register
	 * @param executor
	 *            the executor to use to load the tasks (optional).
	 * @return the list
	 */
	public static List<Instance> load(List<Pair<Class<? extends Instance>, Serializable>> list,
			ServiceRegister serviceRegister, TaskExecutor executor) {

		// all results will appear here
		Map<Pair<Class<? extends Instance>, Serializable>, Instance> resultMapping = loadAsMapping(
				list, serviceRegister, executor);

		// order the final result
		List<Instance> result = new ArrayList<>(resultMapping.size());
		for (Pair<Class<? extends Instance>, Serializable> pair : list) {
			Instance instance = resultMapping.get(pair);
			if (instance != null) {
				result.add(instance);
			}
		}
		return result;
	}

	/**
	 * Load instances. The method reloads the given list of instances by batch loading and returning
	 * them in the same order.
	 * 
	 * @param instances
	 *            the instances to load/reload
	 * @param serviceRegister
	 *            the service register
	 * @param executor
	 *            the executor
	 * @return the list of refreshed instances
	 */
	public static List<Instance> loadInstances(Collection<Instance> instances,
			ServiceRegister serviceRegister, TaskExecutor executor) {
		if (instances == null || instances.isEmpty()) {
			return Collections.emptyList();
		}
		List<Pair<Class<? extends Instance>, Serializable>> toLoad = new ArrayList<>(
				instances.size());
		for (Instance instance : instances) {
			toLoad.add(new Pair<Class<? extends Instance>, Serializable>(instance.getClass(),
					instance.getId()));
		}
		return load(toLoad, serviceRegister, executor);
	}

	/**
	 * Load the given list of instance pairs. The pairs should contain the actual instance class and
	 * the primary id of the instance. If executor implementation is provided then the entries will
	 * be loaded the provided executor, otherwise will be loaded sequentially.
	 * 
	 * @param list
	 *            the list of pairs to load
	 * @param serviceRegister
	 *            the service register
	 * @param executor
	 *            the executor to use to load the tasks (optional).
	 * @return the mapping between the given list elements and the actual loaded instances
	 */
	public static Map<Pair<Class<? extends Instance>, Serializable>, Instance> loadAsMapping(
			List<Pair<Class<? extends Instance>, Serializable>> list,
			ServiceRegister serviceRegister, TaskExecutor executor) {
		if ((list == null) || list.isEmpty()) {
			return Collections.emptyMap();
		}
		TimeTracker tracker = null;
		if (trace) {
			tracker = TimeTracker.createAndStart();
		}
		// mapping for ids to load by type
		Map<Class<? extends Instance>, List<Serializable>> typeToIdMapping = new LinkedHashMap<>();

		// group results by type
		for (Pair<Class<? extends Instance>, Serializable> pair : list) {
			CollectionUtils.addValueToMap(typeToIdMapping, pair.getFirst(), pair.getSecond());
		}

		// all results will appear here
		Map<Pair<Class<? extends Instance>, Serializable>, Instance> resultMapping = CollectionUtils
				.createLinkedHashMap(list.size());

		// load the actual instances
		// for optimization we load the data in separate threads per object type if more then 1
		List<GenericAsyncTask> tasks = new ArrayList<>(typeToIdMapping.size());
		for (Entry<Class<? extends Instance>, List<Serializable>> entry : typeToIdMapping
				.entrySet()) {
			InstanceService<?, DefinitionModel> instanceService = serviceRegister
					.getInstanceService(entry.getKey());
			if (instanceService != null) {
				// no need to use parallel loading for a single type
				if ((executor != null) && (typeToIdMapping.size() > 1)) {
					tasks.add(new EntityLoaderAsyncTask(entry.getValue(), resultMapping,
							instanceService));
				} else {
					loadInstancesInternal(entry.getValue(), resultMapping, instanceService);
				}
			}
		}
		// if executor is provided we block to wait for the tasks to finish.
		if ((executor != null) && !tasks.isEmpty()) {
			executor.execute(tasks);
		}
		if (trace) {
			LOGGER.trace("Batch loading of {} instances took {} s", resultMapping.size(),
						tracker.stopInSeconds());
		}

		return resultMapping;
	}

	/**
	 * Load the instances that are represented by instance references. If executor implementation is
	 * provided then the entries will be loaded the provided executor, otherwise will be loaded
	 * sequentially. The method will keep the argument order.
	 * 
	 * @param list
	 *            the list of references to load
	 * @param serviceRegister
	 *            the service register
	 * @param executor
	 *            the executor to be used when loading the entries.
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static List<Instance> loadFromReferences(Collection<InstanceReference> list,
			ServiceRegister serviceRegister, TaskExecutor executor) {
		if (list == null) {
			return Collections.emptyList();
		}
		List<Pair<Class<? extends Instance>, Serializable>> args = new ArrayList<>(list.size());
		for (InstanceReference instanceReference : list) {
			args.add(new Pair<Class<? extends Instance>, Serializable>(
					(Class<? extends Instance>) instanceReference.getReferenceType().getJavaClass(),
					convertId(instanceReference.getIdentifier())));
		}
		return load(args, serviceRegister, executor);
	}

	/**
	 * Load the instances that are represented by instance references. If executor implementation is
	 * provided then the entries will be loaded the provided executor, otherwise will be loaded
	 * sequentially. The result mapping will have the original order of the arguments.
	 * 
	 * @param list
	 *            the list of references to load
	 * @param serviceRegister
	 *            the service register
	 * @param executor
	 *            the executor to be used when loading the entries.
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static Map<InstanceReference, Instance> loadAsMapFromReferences(
			Collection<InstanceReference> list, ServiceRegister serviceRegister,
			TaskExecutor executor) {
		if (list == null) {
			return Collections.emptyMap();
		}
		List<Pair<Class<? extends Instance>, Serializable>> args = new ArrayList<>(list.size());
		Map<Pair<Class<? extends Instance>, Serializable>, InstanceReference> resultMapping = CollectionUtils
				.createLinkedHashMap(list.size());
		for (InstanceReference instanceReference : list) {
			Pair<Class<? extends Instance>, Serializable> pair = new Pair<Class<? extends Instance>, Serializable>(
					(Class<? extends Instance>) instanceReference.getReferenceType().getJavaClass(),
					convertId(instanceReference.getIdentifier()));
			resultMapping.put(pair, instanceReference);
			args.add(pair);
		}
		// load the results
		Map<Pair<Class<? extends Instance>, Serializable>, Instance> mapping = loadAsMapping(args,
				serviceRegister, executor);
		Map<InstanceReference, Instance> result = CollectionUtils.createLinkedHashMap(mapping
				.size());

		// build the final mapping
		for (Entry<Pair<Class<? extends Instance>, Serializable>, Instance> entry : mapping
				.entrySet()) {
			InstanceReference reference = resultMapping.get(entry.getKey());
			result.put(reference, entry.getValue());
		}
		return result;
	}

	/**
	 * Convert id.
	 * 
	 * @param identifier
	 *            the identifier
	 * @return the serializable
	 */
	private static Serializable convertId(String identifier) {
		if (!Character.isDigit(identifier.charAt(0))) {
			// some optimization when converting the identifiers
			return identifier;
		}
		try {
			return Long.parseLong(identifier);
		} catch (NumberFormatException e) {
			return identifier;
		}
	}

	/**
	 * Load instances.
	 * 
	 * @param idsToLoad
	 *            the ids to load
	 * @param resultMapping
	 *            the result mapping
	 * @param instanceService
	 *            the instance service
	 */
	static void loadInstancesInternal(List<Serializable> idsToLoad,
			Map<Pair<Class<? extends Instance>, Serializable>, Instance> resultMapping,
			InstanceService<?, DefinitionModel> instanceService) {
		String name = "";
		if (trace) {
			name = instanceService
					.getInstanceDefinitionClass().getSimpleName().replace("Definition", "");
			LOGGER.trace("Loading instances for {} and ID={}", name,
					idsToLoad);
		}
		// batch load the instances
		List<?> instances = instanceService.loadByDbId(idsToLoad);
		if (trace) {
			LOGGER.trace("Loaded {} {} instances out of {}", instances.size(), name,
					idsToLoad.size());
		}
		for (Object object : instances) {
			Instance instance = (Instance) object;
			resultMapping.put(new Pair<Class<? extends Instance>, Serializable>(
					instance.getClass(), instance.getId()), instance);
		}
	}

	/**
	 * Callback interface to provide additional information when calling the method.
	 * 
	 * @param <P>
	 *            the primary key type
	 * @param <E>
	 *            the secondary key type
	 *            {@link BatchEntityLoader#batchLoadByPrimaryKey(List, EntityLookupCache, BatchPrimaryKeyEntityLoaderCallback)}
	 *            . The implemented class should provide implementation for all defined methods.
	 */
	public interface BatchPrimaryKeyEntityLoaderCallback<P extends Serializable, E extends Entity<?>> {

		/**
		 * Gets the primary key for the given entity.
		 * 
		 * @param entity
		 *            the entity
		 * @return the primary key
		 */
		P getPrimaryKey(E entity);

		/**
		 * The method should perform a search by list primary keys and return the results.
		 * 
		 * @param secondPass
		 *            the list of primary keys that have not been found into provided cache of the
		 *            batch loader method.
		 * @return the list of found entities
		 */
		List<E> findEntitiesByPrimaryKey(Set<P> secondPass);
	}

	/**
	 * Callback interface to provide additional information when calling the method.
	 * 
	 * @param <P>
	 *            the primary key type
	 * @param <E>
	 *            the concrete entity type
	 * @param <S>
	 *            the secondary key type
	 *            {@link BatchEntityLoader#batchLoadBySecondaryKey(List, EntityLookupCache, BatchSecondaryKeyEntityLoaderCallback)}
	 *            . The implemented class should provide implementation for all defined methods.
	 */
	public interface BatchSecondaryKeyEntityLoaderCallback<P extends Serializable, E extends Entity<?>, S extends Serializable> {

		/**
		 * Gets the primary key for the given entity.
		 * 
		 * @param entity
		 *            the entity
		 * @return the primary key
		 */
		P getPrimaryKey(E entity);

		/**
		 * Gets the secondary key for the given entity.
		 * 
		 * @param entity
		 *            the entity
		 * @return the secondary key
		 */
		S getSecondaryKey(E entity);

		/**
		 * The method should perform a search by list secondary keys and return the results.
		 * 
		 * @param secondPass
		 *            the list of secondary keys that have not been found into provided cache of the
		 *            batch loader method.
		 * @return the list of found entities
		 */
		List<E> findEntitiesBySecondaryKey(Set<S> secondPass);
	}

	/**
	 * Async task used to load a list of entities.
	 * 
	 * @author BBonev
	 */
	public static class EntityLoaderAsyncTask extends GenericAsyncTask {
		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -1085289827605058375L;
		/** The ids to load. */
		private final List<Serializable> idsToLoad;
		/** The result mapping. */
		private final Map<Pair<Class<? extends Instance>, Serializable>, Instance> resultMapping;
		/** The instance service. */
		private final InstanceService<?, DefinitionModel> instanceService;

		/**
		 * Instantiates a new entity loader async task.
		 * 
		 * @param idsToLoad
		 *            the ids to load
		 * @param resultMapping
		 *            the result mapping
		 * @param instanceService
		 *            the instance service
		 */
		public EntityLoaderAsyncTask(List<Serializable> idsToLoad,
				Map<Pair<Class<? extends Instance>, Serializable>, Instance> resultMapping,
				InstanceService<?, DefinitionModel> instanceService) {
			super();
			this.idsToLoad = idsToLoad;
			this.resultMapping = resultMapping;
			this.instanceService = instanceService;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean executeTask() {
			loadInstancesInternal(idsToLoad, resultMapping, instanceService);
			return true;
		}

	}
}
