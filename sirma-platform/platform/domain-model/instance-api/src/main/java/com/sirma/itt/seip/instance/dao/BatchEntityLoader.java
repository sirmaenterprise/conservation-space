package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.collections.CollectionUtils.addValueToSetMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Helper class that implements optimized batch loading algorithms for entities and instances. Each method requires a
 * concrete callback implementation to work properly.
 *
 * @author BBonev
 */
public class BatchEntityLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static boolean TRACE = LOGGER.isTraceEnabled();

	/**
	 * Batch load of instances by secondary keys.
	 *
	 * @param
	 * 			<P>
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
	public static <P extends Serializable, E extends Entity<?>> List<E> batchLoadByPrimaryKey(List<P> ids,
			EntityLookupCache<P, E, ?> cache, BatchPrimaryKeyEntityLoaderCallback<P, E> callback) {
		Set<P> secondPass = new LinkedHashSet<>();
		Map<P, E> result = new LinkedHashMap<>((int) (ids.size() * 1.5));
		// first we check for hits in the cache and filter the cache misses
		if (cache == null) {
			secondPass.addAll(ids);
		} else {
			ids.forEach((id) -> {
				// convert the cache entry to instance and schedule
				// properties loading instead of one by one loading
				E entity = cache.getValue(id);
				if (entity != null) {
					result.put(id, entity);
				} else {
					// no cache or not found in cache search later in DB
					secondPass.add(id);
				}
			});
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<E> list = callback.findEntitiesByPrimaryKey(secondPass);
			for (E entity : list) {
				P primaryKey = callback.getPrimaryKey(entity);
				addToCache(cache, entity, primaryKey);
				result.put(primaryKey, entity);
			}
		}

		// sort the results
		List<E> sortedResult = new ArrayList<>(result.size());
		for (P key : ids) {
			CollectionUtils.addNonNullValue(sortedResult, result.get(key));
		}
		return sortedResult;
	}

	private static <E extends Entity<?>, P extends Serializable> void addToCache(EntityLookupCache<P, E, ?> cache,
			E entity, P primaryKey) {
		if (cache != null) {
			// update cache
			cache.setValue(primaryKey, entity);
		}
	}

	/**
	 * Batch load of entities by secondary keys.
	 *
	 * @param
	 * 			<P>
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
			List<S> ids, EntityLookupCache<P, E, S> cache, BatchSecondaryKeyEntityLoaderCallback<P, E, S> callback) {
		Set<S> secondPass = new LinkedHashSet<>();
		Map<S, E> result = CollectionUtils.createLinkedHashMap(ids.size());
		// first we check for hits in the cache and filter the cache misses

		if (cache == null) {
			secondPass.addAll(ids);
		} else {
			ids.forEach(id -> {
				P key = cache.getKey(id);
				// convert the cache entry to instance and schedule
				// properties loading instead of one by one loading
				// for some reason sometimes the value returned is null
				if (key != null && CollectionUtils.addNonNullValue(result, id, cache.getValue(key))) {
					return;
				}
				// no cache or not found in cache search later in DB
				secondPass.add(id);
			});
		}

		// fetch everything else from DB and update cache
		if (!secondPass.isEmpty()) {
			List<E> list = callback.findEntitiesBySecondaryKey(secondPass);
			for (E entity : list) {
				if (cache != null) {
					// update cache
					cache.setValue(callback.getPrimaryKey(entity), entity);
				}
				result.put(callback.getSecondaryKey(entity), entity);
			}
		}
		// sort the results
		List<E> sortedResult = new ArrayList<>(result.size());
		ids.forEach(key -> CollectionUtils.addNonNullValue(sortedResult, result.get(key)));
		return sortedResult;
	}

	/**
	 * Load the given list of instance pairs. The pairs should contain the actual instance class and the primary id of
	 * the instance. If executor implementation is provided then the entries will be loaded the provided executor,
	 * otherwise will be loaded sequentially. The loaded elements has the same order as the original list.
	 *
	 * @param list
	 *            the list of pairs to load
	 * @param serviceRegistry
	 *            the service register
	 * @param executor
	 *            the executor to use to load the tasks (optional).
	 * @return the list
	 */
	public static List<Instance> load(Collection<Pair<Object, Serializable>> list, ServiceRegistry serviceRegistry,
			TaskExecutor executor) {

		// all results will appear here
		Map<Pair<Object, Serializable>, Instance> resultMapping = loadAsMapping(list, serviceRegistry, executor);

		// order the final result
		return list.stream().map(pair -> resultMapping.get(pair)).filter(Objects::nonNull).collect(
				CollectionUtils.toList(resultMapping.size()));
	}

	/**
	 * Load instances. The method reloads the given list of instances by batch loading and returning them in the same
	 * order.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instances
	 *            the instances to load/reload
	 * @param serviceRegistry
	 *            the service register
	 * @param executor
	 *            the executor
	 * @return the list of refreshed instances
	 */
	public static <I extends Instance> List<I> loadInstances(Collection<I> instances, ServiceRegistry serviceRegistry,
			TaskExecutor executor) {
		return loadInternal(instances, serviceRegistry, executor, () -> new ArrayList<>(instances.size()));
	}

	/**
	 * Load instances. The method reloads the given list of instances by batch loading and returning them in the same
	 * order. The method will filter out any duplicate instances.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instances
	 *            the instances to load/reload
	 * @param serviceRegistry
	 *            the service register
	 * @param executor
	 *            the executor
	 * @return the list of refreshed instances
	 */
	public static <I extends Instance> List<I> loadDistinctInstances(Collection<I> instances,
			ServiceRegistry serviceRegistry, TaskExecutor executor) {
		return loadInternal(instances, serviceRegistry, executor,
				() -> CollectionUtils.createLinkedHashSet(instances.size()));
	}

	/**
	 * Perform loading of the given instances. The intermediate store to copy the instance before passing for actual
	 * loading should be provided as last argument. That store will be used to store the instance ids before passing
	 * them to {@link #load(Collection, ServiceRegistry, TaskExecutor)} method.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instances
	 *            the instances
	 * @param serviceRegistry
	 *            the service registry
	 * @param executor
	 *            the executor
	 * @param intermediateStore
	 *            the intermediate store
	 * @return the list result
	 */
	@SuppressWarnings("unchecked")
	private static <I extends Instance> List<I> loadInternal(Collection<I> instances, ServiceRegistry serviceRegistry,
			TaskExecutor executor, Supplier<Collection<Pair<Object, Serializable>>> intermediateStore) {
		if (isEmpty(instances)) {
			return Collections.emptyList();
		}
		Collection<Pair<Object, Serializable>> toLoad = intermediateStore.get();
		for (Instance instance : instances) {
			toLoad.add(instanceToPair(instance));
		}
		return (List<I>) load(toLoad, serviceRegistry, executor);
	}

	/**
	 * Load new instance copies from the given instance list. The method reloads the given list of instances by batch
	 * loading and returning them as a mapping.
	 *
	 * @param <I>
	 *            the generic type
	 * @param instances
	 *            the instances to load/reload
	 * @param serviceRegistry
	 *            the service register
	 * @param executor
	 *            the executor
	 * @return a mapping of refreshed instances
	 */
	public static <I extends Instance> Map<Pair<Object, Serializable>, Instance> loadInstancesAsMapping(
			Collection<I> instances, ServiceRegistry serviceRegistry, TaskExecutor executor) {
		if (isEmpty(instances)) {
			return Collections.emptyMap();
		}
		List<Pair<Object, Serializable>> toLoad = new ArrayList<>(instances.size());
		for (Instance instance : instances) {
			toLoad.add(instanceToPair(instance));
		}
		return loadAsMapping(toLoad, serviceRegistry, executor);
	}

	/**
	 * Create a pair object that contains information for the given instance used in mappings by the load methods.
	 *
	 * @param instance
	 *            the instance
	 * @return a pair of the instance type and instance id
	 */
	public static Pair<Object, Serializable> instanceToPair(Instance instance) {
		// FIXME TYPE RESOLVING
		return new Pair<>(instance.getClass(), instance.getId());
	}

	/**
	 * Load the given list of instance pairs. The pairs should contain the actual instance class and the primary id of
	 * the instance. If executor implementation is provided then the entries will be loaded the provided executor,
	 * otherwise will be loaded sequentially.
	 *
	 * @param list
	 *            the list of pairs to load
	 * @param serviceRegistry
	 *            the service register
	 * @param executor
	 *            the executor to use to load the tasks (optional).
	 * @return the mapping between the given list elements and the actual loaded instances
	 */
	@SuppressWarnings("boxing")
	public static Map<Pair<Object, Serializable>, Instance> loadAsMapping(Collection<Pair<Object, Serializable>> list,
			ServiceRegistry serviceRegistry, TaskExecutor executor) {
		if (isEmpty(list)) {
			return Collections.emptyMap();
		}
		TimeTracker tracker = null;
		if (TRACE) {
			tracker = TimeTracker.createAndStart();
		}
		// mapping for ids to load by type
		Map<Object, Set<Serializable>> typeToIdMapping = new LinkedHashMap<>();

		// group results by type
		for (Pair<Object, Serializable> pair : list) {
			addValueToSetMap(typeToIdMapping, pair.getFirst(), pair.getSecond());
		}

		// all results will appear here
		Map<Pair<Object, Serializable>, Instance> resultMapping = CollectionUtils.createLinkedHashMap(list.size());

		typeToIdMapping.forEach((type, ids) -> {
			InstanceService instanceService = serviceRegistry.getInstanceService(type);
			if (instanceService == null) {
				LOGGER.warn("No service found for loading of type {}", type);
				return;
			}
			loadInstancesInternal(ids, resultMapping, instanceService);
		});

		if (tracker != null) {
			LOGGER.trace("Batch loading of {} instances took {} ms", resultMapping.size(), tracker.stop());
		}

		return resultMapping;
	}

	/**
	 * Load the instances that are represented by instance references. If executor implementation is provided then the
	 * entries will be loaded the provided executor, otherwise will be loaded sequentially. The method will keep the
	 * argument order.
	 *
	 * @param list
	 *            the list of references to load
	 * @param serviceRegistry
	 *            the service register
	 * @param executor
	 *            the executor to be used when loading the entries.
	 * @return the list
	 */
	public static List<Instance> loadFromReferences(Collection<InstanceReference> list, ServiceRegistry serviceRegistry,
			TaskExecutor executor) {
		if (list == null) {
			return Collections.emptyList();
		}
		List<Pair<Object, Serializable>> args = new ArrayList<>(list.size());
		for (InstanceReference instanceReference : list) {
			// FIXME TYPE RESOLVING
			args.add(new Pair<>(instanceReference.getReferenceType().getJavaClass(),
					convertId(instanceReference.getIdentifier())));
		}
		return load(args, serviceRegistry, executor);
	}

	/**
	 * Load the instances that are represented by instance references. If executor implementation is provided then the
	 * entries will be loaded the provided executor, otherwise will be loaded sequentially. The result mapping will have
	 * the original order of the arguments.
	 *
	 * @param list
	 *            the list of references to load
	 * @param serviceRegistry
	 *            the service register
	 * @param executor
	 *            the executor to be used when loading the entries.
	 * @return the list
	 */
	public static Map<InstanceReference, Instance> loadAsMapFromReferences(Collection<InstanceReference> list,
			ServiceRegistry serviceRegistry, TaskExecutor executor) {
		if (list == null) {
			return Collections.emptyMap();
		}
		List<Pair<Object, Serializable>> orderedKeys = new ArrayList<>(list.size());
		Map<Pair<Object, Serializable>, InstanceReference> resultMapping = CollectionUtils
				.createLinkedHashMap(list.size());
		for (InstanceReference instanceReference : list) {
			// FIXME TYPE RESOLVING
			Pair<Object, Serializable> pair = new Pair<>(instanceReference.getReferenceType().getJavaClass(),
					convertId(instanceReference.getIdentifier()));
			resultMapping.put(pair, instanceReference);
			orderedKeys.add(pair);
		}
		// load the results
		Map<Pair<Object, Serializable>, Instance> mapping = loadAsMapping(orderedKeys, serviceRegistry, executor);
		Map<InstanceReference, Instance> result = CollectionUtils.createLinkedHashMap(mapping.size());

		// build the final mapping
		mapping.entrySet().forEach(entry -> result.put(resultMapping.get(entry.getKey()), entry.getValue()));
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
			return Long.valueOf(identifier);
		} catch (NumberFormatException e) {
			LOGGER.trace("", e);
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
	@SuppressWarnings("boxing")
	static void loadInstancesInternal(Set<Serializable> idsToLoad,
			Map<Pair<Object, Serializable>, Instance> resultMapping, InstanceService instanceService) {
		// batch load the instances
		List<?> instances = instanceService.loadByDbId((List<Serializable>) new ArrayList<>(idsToLoad));
		if (isEmpty(instances)) {
			return;
		}

		LOGGER.trace("Loaded {} instances out of {}", instances.size(), idsToLoad.size());

		for (Object object : instances) {
			Instance instance = (Instance) object;
			resultMapping.put(instanceToPair(instance), instance);
		}
	}

	/**
	 * Callback interface to provide additional information when calling the method.
	 *
	 * @param <R>
	 *            the primary key type
	 * @param <E>
	 *            the secondary key type
	 *            {@link BatchEntityLoader#batchLoadByPrimaryKey(List, EntityLookupCache, BatchPrimaryKeyEntityLoaderCallback)}
	 *            . The implemented class should provide implementation for all defined methods.
	 */
	public interface BatchPrimaryKeyEntityLoaderCallback<R extends Serializable, E extends Entity<? extends Serializable>> {

		/**
		 * Gets the primary key for the given entity.
		 *
		 * @param entity
		 *            the entity
		 * @return the primary key
		 */
		R getPrimaryKey(E entity);

		/**
		 * The method should perform a search by list primary keys and return the results.
		 *
		 * @param secondPass
		 *            the list of primary keys that have not been found into provided cache of the batch loader method.
		 * @return the list of found entities
		 */
		List<E> findEntitiesByPrimaryKey(Set<R> secondPass);
	}

	/**
	 * Callback interface to provide additional information when calling the method.
	 *
	 * @param <R>
	 *            the primary key type
	 * @param <E>
	 *            the concrete entity type
	 * @param <S>
	 *            the secondary key type
	 *            {@link BatchEntityLoader#batchLoadBySecondaryKey(List, EntityLookupCache, BatchSecondaryKeyEntityLoaderCallback)}
	 *            . The implemented class should provide implementation for all defined methods.
	 */
	public interface BatchSecondaryKeyEntityLoaderCallback<R extends Serializable, E extends Entity<? extends Serializable>, S extends Serializable> {

		/**
		 * Gets the primary key for the given entity.
		 *
		 * @param entity
		 *            the entity
		 * @return the primary key
		 */
		R getPrimaryKey(E entity);

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
		 *            the list of secondary keys that have not been found into provided cache of the batch loader
		 *            method.
		 * @return the list of found entities
		 */
		List<E> findEntitiesBySecondaryKey(Set<S> secondPass);
	}
}
