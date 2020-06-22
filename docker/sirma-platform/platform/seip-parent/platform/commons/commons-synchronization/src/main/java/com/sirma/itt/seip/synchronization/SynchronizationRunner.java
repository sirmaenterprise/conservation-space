package com.sirma.itt.seip.synchronization;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.util.EqualsHelper.getMapComparison;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotation.DisableAudit;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.EqualsHelper.MapValueComparison;

/**
 * SynchronizationRunner realizes a synchronization algorithm and provides means of running named synchronizations
 * provided as plugin implementations of {@link SynchronizationConfiguration}
 *
 * @author BBonev
 */
@DisableAudit
@ApplicationScoped
public class SynchronizationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(SynchronizationConfiguration.PLUGIN_NAME)
	private Plugins<SynchronizationConfiguration<Object, Object>> configurations;

	/**
	 * Gets the available synchronization configurations.
	 *
	 * @return the available synchronizations
	 */
	public Collection<String> getAvailable() {
		return configurations.stream().map(SynchronizationConfiguration::getName).collect(Collectors.toList());
	}

	/**
	 * Run all synchronizations in the registered order and return a report of the found changes
	 *
	 * @return the result of the run synchronizations
	 * @throws SynchronizationException
	 *             thrown when any of the synchronizations failed
	 */
	public Collection<SynchronizationResultState> runAll() {
		return runAll(new SyncRuntimeConfiguration());
	}

	/**
	 * Run all synchronizations in the registered order and return a report of the found changes
	 *
	 * @param syncConfiguration
	 *            the runtime synchronization configuration to use during this synchronization session
	 * @return the result of the run synchronizations
	 * @throws SynchronizationException
	 *             thrown when any of the synchronizations failed
	 */
	public Collection<SynchronizationResultState> runAll(SyncRuntimeConfiguration syncConfiguration) {
		Collection<SynchronizationResultState> result = new ArrayList<>(configurations.count());
		for (SynchronizationConfiguration<Object, Object> config : configurations) {
			result.add(runSync(config, syncConfiguration));
		}
		return result;
	}

	/**
	 * Run synchronization identified by the given name. The possible {@link SynchronizationConfiguration} are search
	 * via CDI injection and traversing all {@link SynchronizationConfiguration#getName()}
	 *
	 * @param name
	 *            the name of the synchronization to run
	 * @return synchronization result object containing info about the added, removed and modified data or or one with
	 *         exception status if error occurred.
	 */
	public SynchronizationResultState runSynchronization(String name) {
		return runSynchronization(name, new SyncRuntimeConfiguration());
	}

	/**
	 * Run synchronization identified by the given name. The possible {@link SynchronizationConfiguration} are search
	 * via CDI injection and traversing all {@link SynchronizationConfiguration#getName()}
	 *
	 * @param name
	 *            the name of the synchronization to run
	 * @param syncConfiguration
	 *            the runtime synchronization configuration to use during this synchronization session
	 * @return synchronization result object containing info about the added, removed and modified data or or one with
	 *         exception status if error occurred.
	 */
	public SynchronizationResultState runSynchronization(String name, SyncRuntimeConfiguration syncConfiguration) {
		return configurations.get(name).map(cfg -> runSync(cfg, syncConfiguration)).orElseGet(
				() -> new SynchronizationResultState(name,
						new SynchronizationException("Synchronization " + name + " not found!")));
	}

	private static SynchronizationResultState runSync(SynchronizationConfiguration<Object, Object> config,
			SyncRuntimeConfiguration syncConfiguration) {
		try {
			LOGGER.info("Running synchronization: {}", config.getName());
			TimeTracker tracker = TimeTracker.createAndStart();
			SynchronizationResult<Object, Object> synchronizationResult = synchronize(config, syncConfiguration);
			return new SynchronizationResultState(config.getName(), synchronizationResult, tracker.stop());
		} catch (SynchronizationException e) {
			LOGGER.warn("Failed synchronization: {} with {}", config.getName(), e.getMessage(), e.getCause());
			LOGGER.trace("Failed synchronization: {} with {}", config.getName(), e.getMessage(), e);
			return new SynchronizationResultState(config.getName(), e);
		}
	}

	/**
	 * Run synchronization configured by the given {@link SynchronizationConfiguration}.
	 *
	 * @param <I>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param config
	 *            the configuration to run
	 * @return the synchronization result
	 * @throws SynchronizationException
	 *             a exception thrown in case of problem of reading the source data or problem during saving the result
	 */
	public static <I, E> SynchronizationResult<I, E> synchronize(SynchronizationConfiguration<I, E> config)
			throws SynchronizationException {
		return synchronize(config, new SyncRuntimeConfiguration());
	}

	/**
	 * Run synchronization configured by the given {@link SynchronizationConfiguration}.
	 *
	 * @param <I>
	 *            the generic type
	 * @param <E>
	 *            the element type
	 * @param config
	 *            the configuration to run
	 * @param syncConfiguration
	 *            the runtime synchronization configuration to use during this synchronization session
	 * @return the synchronization result
	 * @throws SynchronizationException
	 *             a exception thrown in case of problem of reading the source data or problem during saving the result
	 */
	public static <I, E> SynchronizationResult<I, E> synchronize(SynchronizationConfiguration<I, E> config,
			SyncRuntimeConfiguration syncConfiguration)
			throws SynchronizationException {
		SynchronizationDataProvider<I, E> source = config.getSource();
		SynchronizationDataProvider<I, E> destination = config.getDestination();

		Collection<? extends E> sourceData = source.provide();
		Collection<? extends E> destinationData = destination.provide();

		Map<I, E> sourceMapping = toMap(sourceData, source);
		Map<I, E> destinationMapping = toMap(destinationData, destination);

		BiPredicate<E, E> comparator = config.getComparator();
		Map<I, MapValueComparison> diff = getMapComparison(sourceMapping, destinationMapping, comparator);

		int mapSize = Math.max(sourceMapping.size(), destinationMapping.size());
		Map<I, E> toAdd = CollectionUtils.createLinkedHashMap(mapSize);
		Map<I, E> toRemove = CollectionUtils.createLinkedHashMap(mapSize);
		Map<I, E> modified = CollectionUtils.createLinkedHashMap(mapSize);

		Set<I> notEqual = new HashSet<>();

		SynchronizationResult<I, E> data = new SynchronizationResult<>(toAdd, toRemove, modified);
		for (Entry<I, MapValueComparison> entry : diff.entrySet()) {
			distribute(entry.getKey(), entry.getValue(), sourceMapping, destinationMapping, data, notEqual, syncConfiguration);
		}

		if (config.isMergeSupported()) {
			for (I key : notEqual) {
				E merged = config.merge(toRemove.remove(key), toAdd.remove(key));
				addNonNullValue(modified, key, merged);
			}
		}

		try {
			if (data.hasChanges()) {
				config.save(data, syncConfiguration);
			}
			return data;
		} catch (RuntimeException e) {
			throw new SynchronizationException("Failed saving synchronization result due to: " + e.getMessage(), e);
		}
	}

	private static <K, V> void distribute(K key, MapValueComparison comparison, Map<K, V> sourceMapping,
			Map<K, V> destinationMapping, SynchronizationResult<K, V> synchronization, Set<K> notEqual,
			SyncRuntimeConfiguration syncConfiguration) {
		switch (comparison) {
			// suppress warning for switch fall through rule
			// for equals case it intended
			case EQUAL: // NOSONAR
				// for equals and force synchronization this should fall through to next clause
				if (!syncConfiguration.isForceSynchronizationEnabled()) {
					break;
				}
			case NOT_EQUAL:
				notEqual.add(key);
				synchronization.getToAdd().put(key, sourceMapping.get(key));
				synchronization.getToRemove().put(key, destinationMapping.get(key));
				break;
			case LEFT_ONLY:
				synchronization.getToAdd().put(key, sourceMapping.get(key));
				break;
			case RIGHT_ONLY:
				synchronization.getToRemove().put(key, destinationMapping.get(key));
				break;
			default:
				break;
		}
	}

	private static <K, V> Map<K, V> toMap(Collection<? extends V> collection, SynchronizationDataProvider<K, V> side) {
		// for duplicate keys use the first value
		return collection.stream().collect(Collectors.toMap(side::getIdentity, Function.identity(), (v1, v2) -> v1));
	}

}
