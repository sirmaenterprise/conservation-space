package com.sirma.itt.emf.semantic;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.util.IntMap;
import com.sirma.itt.seip.collections.FixedSizeMap;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Collects and stores information about active connections mapped per thread. The purpose of this class is to help
 * catch leaking connections problem and monitor connections
 *
 * @author BBonev
 */
@ApplicationScoped
public class RepositoryConnectionMonitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// the stores are not synchronized as it may become a performance bottleneck, the iteration code will handle
	// concurrent modifications or not
	private Map<Thread, ConnectionInfoGroup> connections = new WeakHashMap<>(256, 0.75f);
	private Map<Long, ConnectionInfoGroup> failed = new FixedSizeMap<>(2048);

	@Inject
	private SecurityContext securityContext;

	/**
	 * Should be called when new connection is opened
	 *
	 * @param connectionId
	 *            a connection identifier (system identity hash code)
	 * @param inTransaction
	 *            if the connection is in transaction (a.k.a open for write)
	 */
	void onNewConnection(int connectionId, boolean inTransaction) {
		Thread currentThread = Thread.currentThread();
		ConnectionInfoGroup groupInfo = connections.computeIfAbsent(currentThread,
				t -> new ConnectionInfoGroup(securityContext));
		// the connection add was rejected for non matching tenants
		if (!groupInfo.newConnection(connectionId, inTransaction, securityContext)) {

			addToBroken(groupInfo);
			// replace the old info with new for the current tenant
			ConnectionInfoGroup newInfoGroup = new ConnectionInfoGroup(securityContext);
			newInfoGroup.newConnection(connectionId, inTransaction, securityContext);
			connections.put(currentThread, newInfoGroup);
		}
	}

	@SuppressWarnings("boxing")
	private void addToBroken(ConnectionInfoGroup info) {
		failed.put(System.currentTimeMillis(), info);
	}

	/**
	 * Should be called when existing read connection is upgraded to write (on transaction begin)
	 *
	 * @param connectionId
	 *            a connection identifier (system identity hash code)
	 */
	void upgreadConnectionToWrite(int connectionId) {
		Thread currentThread = Thread.currentThread();
		ConnectionInfoGroup groupInfo = connections.computeIfAbsent(currentThread,
				t -> new ConnectionInfoGroup(securityContext));
		// the connection add was rejected for non matching tenants
		if (!groupInfo.changeTxMode(connectionId, securityContext)) {

			addToBroken(groupInfo);
			// replace the old info with new for the current tenant
			ConnectionInfoGroup newInfoGroup = new ConnectionInfoGroup(securityContext);
			newInfoGroup.changeTxMode(connectionId, securityContext);
			connections.put(currentThread, newInfoGroup);
		}
	}

	/**
	 * Should be called on transaction close (normal (on transaction end or close method) or abnormal (on mixed
	 * tenants))
	 *
	 * @param connectionId
	 *            a connection identifier (system identity hash code)
	 */
	void onConnectionClose(int connectionId) {

		Thread currentThread = Thread.currentThread();
		ConnectionInfoGroup groupInfo = connections.remove(currentThread);
		// the connection add was rejected for non matching tenants
		if (groupInfo != null && !groupInfo.onConnectionClose(connectionId, securityContext)) {
			addToBroken(groupInfo);
		}
	}

	/**
	 * Provide the internal state as JSON
	 *
	 * @return the state as JSON
	 */
	JsonObject getInfo() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonObjectBuilder active = Json.createObjectBuilder();
		Set<Thread> keys = new HashSet<>(connections.keySet());
		for (Thread key : keys) {
			ConnectionInfoGroup info = connections.get(key);
			// the key may be destroyed during the traversal
			if (info != null) {
				active.add(key.getName(), info.toJson());
			}
		}
		builder.add("active", active);

		JsonArrayBuilder broken = Json.createArrayBuilder();
		for (Entry<Long, ConnectionInfoGroup> entry : failed.entrySet()) {
			// the key may be destroyed during the traversal
			broken.add(Json
					.createObjectBuilder()
						.add("when", ISO8601DateFormat.format(new Date(entry.getKey().longValue())))
						.add("info", entry.getValue().toJson()));
		}
		builder.add("broken", broken);

		return builder.build();
	}

	private static class ConnectionInfoGroup extends IntMap<ConnectionInfo> {

		private final String originalTenant;
		private final String originalThread;
		private final String requestId;
		private String user;

		ConnectionInfoGroup(SecurityContext securityContext) {
			// create relatively small. most of the cases we do not expect huge counts here
			super(5);
			originalTenant = securityContext.getCurrentTenantId();
			requestId = securityContext.getRequestId();
			user = securityContext.getAuthenticated().getSystemId().toString();
			originalThread = Thread.currentThread().getName();
		}

		boolean newConnection(int id, boolean inTx, SecurityContext securityContext) {
			if (!isSameTenant(securityContext)) {
				return false;
			}
			ConnectionInfo info = new ConnectionInfo();
			info.inTransaction = inTx;
			put(id, info);
			return true;
		}

		@SuppressWarnings("boxing")
		boolean onConnectionClose(int id, SecurityContext securityContext) {
			if (!isSameTenant(securityContext)) {
				return false;
			}
			if (remove(id) == null) {
				LOGGER.trace("No such connection to close for id: {}", id);
			}
			return true;
		}

		boolean changeTxMode(int id, SecurityContext securityContext) {
			if (!isSameTenant(securityContext)) {
				return false;
			}
			ConnectionInfo info = get(id);
			if (info == null) {
				return newConnection(id, true, securityContext);
			}
			info.inTransaction = true;
			info.upgreaded = true;
			return true;
		}

		private boolean isSameTenant(SecurityContext securityContext) {
			return nullSafeEquals(originalTenant, securityContext.getCurrentTenantId());
		}

		@Override
		public String toString() {
			String entries = StreamSupport.stream(values().spliterator(), false).map(ConnectionInfo::toString).collect(
					Collectors.joining("\n\t\t"));
			return String.format("Tenant: %s%n\tUser: %s%n\tRequestId: %s%n\tConnections: [%n%s]", originalTenant, user,
					requestId, entries);
		}

		JsonObject toJson() {
			JsonArrayBuilder entriesBuilder = Json.createArrayBuilder();
			StreamSupport
					.stream(values().spliterator(), false)
						.map(ConnectionInfo::toJson)
						.forEach(entriesBuilder::add);
			return Json
					.createObjectBuilder()
						.add("tenant", originalTenant)
						.add("thread-name", originalThread)
						.add("request-id", requestId)
						.add("user", user)
						.add("connections", entriesBuilder)
						.build();
		}
	}

	private static class ConnectionInfo {

		private boolean inTransaction;
		private boolean upgreaded;

		private StackTraceElement[] stack;
		private long timestamp;

		ConnectionInfo() {
			stack = Thread.currentThread().getStackTrace();
			timestamp = System.currentTimeMillis();
		}

		String getStack() {
			return streamStack().collect(Collectors.joining("\n\t", "\n", "\n"));
		}

		private Stream<String> streamStack() {
			return Arrays.stream(stack).map(StackTraceElement::toString).filter(item -> item.contains("sirma"));
		}

		@Override
		@SuppressWarnings("boxing")
		public String toString() {
			return String.format("In transaction: %s%n\tUpgreaded: %s%n\tCreated: %s%n\tOriginal stacktrace: %s",
					inTransaction, upgreaded, ISO8601DateFormat.format(new Date(timestamp)), getStack());
		}

		JsonObject toJson() {
			JsonArrayBuilder stackBuilder = Json.createArrayBuilder();
			streamStack().forEach(stackBuilder::add);
			return Json
					.createObjectBuilder()
						.add("upgraded", upgreaded)
						.add("isTransaction", inTransaction)
						.add("timestamp", ISO8601DateFormat.format(new Date(timestamp)))
						.add("stack", stackBuilder)
						.build();
		}
	}
}
