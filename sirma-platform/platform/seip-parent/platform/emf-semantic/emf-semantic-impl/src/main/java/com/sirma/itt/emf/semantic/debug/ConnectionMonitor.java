package com.sirma.itt.emf.semantic.debug;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Collector for statistical information about semantic repository connections.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 06/03/2019
 */
@Singleton
public class ConnectionMonitor {

	private static final Metric CREATED_CONNECTIONS_COUNT = Metric.Builder.counter("semantic_created_connections_count",
			"The count of the created repository connections.").build();
	private static final Metric READ_QUERIES_COUNT = Metric.Builder.counter("semantic_read_queries_count",
			"The count of the read queries to the repository.").build();
	private static final Metric UPDATE_QUERIES_COUNT = Metric.Builder.counter("semantic_update_queries_count",
			"The count of the update queries to the repository").build();
	private static final Metric REMOVE_QUERIES_COUNT = Metric.Builder.counter("semantic_remove_queries_count",
			"The count of the remove queries to the repository").build();

	private ConcurrentMap<String, AtomicLong> onCreate = new ConcurrentHashMap<>();
	private ConcurrentMap<String, AtomicLong> onRead = new ConcurrentHashMap<>();
	private ConcurrentMap<String, AtomicLong> onUpdate = new ConcurrentHashMap<>();
	private ConcurrentMap<String, AtomicLong> onRemove = new ConcurrentHashMap<>();

	private final SecurityContext securityContext;
	private final Statistics statistics;

	@Inject
	public ConnectionMonitor(SecurityContext securityContext, Statistics statistics) {
		this.securityContext = securityContext;
		this.statistics = statistics;
	}

	void onCreate() {
		logAccess(onCreate);
		statistics.track(CREATED_CONNECTIONS_COUNT);
	}

	void onRead() {
		logAccess(onRead);
		statistics.track(READ_QUERIES_COUNT);
	}

	void onUpdate() {
		logAccess(onUpdate);
		statistics.track(UPDATE_QUERIES_COUNT);
	}

	void onRemove() {
		logAccess(onRemove);
		statistics.track(REMOVE_QUERIES_COUNT);
	}

	private void logAccess(ConcurrentMap<String, AtomicLong> store) {
		store.computeIfAbsent(securityContext.getCurrentTenantId(), k -> new AtomicLong()).incrementAndGet();
	}

	/**
	 * Wraps the given connection into connection that tracks the connection creation and interactions with the database
	 *
	 * @param connection the connection to wrap
	 * @return the monitored connection instance
	 */
	RepositoryConnection monitor(RepositoryConnection connection) {
		return new MonitoredRepositoryConnection(connection, this);
	}

	/**
	 * Returns a snapshot fo the current statistics information
	 *
	 * @return statistics snapshot
	 */
	Collection<ConnectionStats> getSnapshot() {
		Map<String, Long> onCreateSnapshot = new HashMap<>();
		Map<String, Long> onReadSnapshot = new HashMap<>();
		Map<String, Long> onUpdateSnapshot = new HashMap<>();
		Map<String, Long> onRemoveSnapshot = new HashMap<>();
		onCreate.forEach((tenant, count) -> onCreateSnapshot.put(tenant, count.get()));
		onRead.forEach((tenant, count) -> onReadSnapshot.put(tenant, count.get()));
		onUpdate.forEach((tenant, count) -> onUpdateSnapshot.put(tenant, count.get()));
		onRemove.forEach((tenant, count) -> onRemoveSnapshot.put(tenant, count.get()));

		Collection<ConnectionStats> connectionStats = new LinkedList<>();
		ConnectionStats total = new ConnectionStats();
		total.setId("total");
		total.setCreateCount(sumOf(onCreateSnapshot.values()));
		total.setReadCount(sumOf(onReadSnapshot.values()));
		total.setUpdateCount(sumOf(onUpdateSnapshot.values()));
		total.setRemoveCount(sumOf(onRemoveSnapshot.values()));
		connectionStats.add(total);

		Set<String> allTenants = new TreeSet<>();
		allTenants.addAll(onCreateSnapshot.keySet());
		allTenants.addAll(onReadSnapshot.keySet());

		for (String tenant : allTenants) {
			ConnectionStats stats = new ConnectionStats();
			stats.setId(tenant);
			stats.setCreateCount(onCreateSnapshot.getOrDefault(tenant, 0L));
			stats.setReadCount(onReadSnapshot.getOrDefault(tenant, 0L));
			stats.setUpdateCount(onUpdateSnapshot.getOrDefault(tenant, 0L));
			stats.setRemoveCount(onRemoveSnapshot.getOrDefault(tenant, 0L));
			connectionStats.add(stats);
		}
		return connectionStats;
	}

	private static long sumOf(Collection<Long> values) {
		return values.stream().reduce(0L, Long::sum);
	}
}
