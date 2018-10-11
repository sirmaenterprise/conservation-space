package com.sirma.itt.seip.tenant.infinispan;

import static org.jboss.as.controller.client.helpers.ClientConstants.ADD;
import static org.jboss.as.controller.client.helpers.ClientConstants.COMPOSITE;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.REMOVE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.STEPS;
import static org.jboss.as.controller.client.helpers.ClientConstants.WRITE_ATTRIBUTE_OPERATION;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheRegister;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.Locking;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.wildfly.WildflyControllerService;

/**
 * {@link CacheRegister} implementation that register caches in Wildfly's server standalone.xml configuration file.
 *
 * @author BBonev
 */
public class WildflyInfinispanCacheRegister implements CacheRegister {
	/**
	 * The default cache container where all caches go
	 */
	public static final String CACHE_CONTAINER = SecurityContext.SYSTEM_TENANT;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String SUCCESS = "success";
	private static final String CACHE_CONTAINER_KEY = "cache-container";
	private static final String OUTCOME = "outcome";
	private static final String LOCKING = "locking";
	private static final String SUBSYSTEM = "subsystem";
	private static final String INFINISPAN = "infinispan";
	static final String DEFAULT_CACHE = "DEFAULT_CACHE_REGION";

	@Inject
	private WildflyControllerService controller;
	@Inject
	private SecurityContext securityContext;

	@Override
	public boolean registerCaches(Collection<CacheConfiguration> configuration) {
		try {
			addCaches(configuration);
		} catch (RollbackedException e) {
			LOGGER.warn("", e);
			return false;
		}
		return true;
	}

	private void addCaches(Collection<CacheConfiguration> values) throws RollbackedException {
		ModelNode root = new ModelNode();

		root.get(OP).set(COMPOSITE);

		ModelNode steps = root.get(STEPS);
		boolean changesToApply = false;

		if (!isCacheContainerPresent()) {
			LOGGER.debug("Default cache container {} not found. Going to add it", CACHE_CONTAINER);
			ModelNode container = steps.add();

			container.get(OP).set(ADD);
			ModelNode address = container.get(OP_ADDR);
			address.add(SUBSYSTEM, INFINISPAN);
			address.add(CACHE_CONTAINER_KEY, CACHE_CONTAINER);
			container.get("default-cache").set(DEFAULT_CACHE);

			addDefaultCache(steps::add);
			changesToApply = true;
		} else if (!isDefaultCachePresent()) {
			LOGGER.debug("Default cache {} not found. Going to add it", DEFAULT_CACHE);
			ModelNode container = steps.add();

			container.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
			ModelNode address = container.get(OP_ADDR);
			address.add(SUBSYSTEM, INFINISPAN);
			address.add(CACHE_CONTAINER_KEY, CACHE_CONTAINER);
			container.get("default-cache").set(DEFAULT_CACHE);

			addDefaultCache(steps::add);
			changesToApply = true;
		} else {
			LOGGER.debug("Cache container and default cache are present adding tenant caches..");
		}

		for (CacheConfiguration cacheConfiguration : values) {
			// we detect if there changes in the cache structure that need to be applied
			changesToApply |= defineCache(cacheConfiguration, steps::add);
		}

		if (changesToApply) {
			// cache changes require service restart to apply
			root.get(OPERATION_HEADERS, "allow-resource-service-restart").set(true);

			ModelNode result = controller.execute(root);
			if ("failed".equals(result.get(OUTCOME).asString())) {
				throw new TenantCreationException(result.get("failure-description").asString());
			}
		}
	}

	private boolean isCacheContainerPresent() throws RollbackedException {
		ModelNode root = new ModelNode();

		root.get(OP).set(READ_RESOURCE_OPERATION);
		ModelNode address = root.get(OP_ADDR);
		address.add(SUBSYSTEM, INFINISPAN);
		address.add(CACHE_CONTAINER_KEY, CACHE_CONTAINER);

		ModelNode result = controller.execute(root);
		return isSuccessful(result);
	}

	private static boolean isSuccessful(ModelNode result) {
		return SUCCESS.equals(result.get(OUTCOME).asString());
	}

	private boolean isDefaultCachePresent() throws RollbackedException {
		ModelNode root = new ModelNode();

		root.get(OP).set(READ_RESOURCE_OPERATION);
		ModelNode address = root.get(OP_ADDR);
		address.add(SUBSYSTEM, INFINISPAN);
		address.add(CACHE_CONTAINER_KEY, CACHE_CONTAINER);
		address.add("local-cache", DEFAULT_CACHE);

		ModelNode result = controller.execute(root);
		return isSuccessful(result);
	}

	private static void addDefaultCache(Supplier<ModelNode> nodeProvider) {
		String cacheName = DEFAULT_CACHE;

		ModelNode node = nodeProvider.get();
		node.get(OP).set(ADD);
		ModelNode address = node.get(OP_ADDR);
		appendCacheAddress(address, cacheName);

		ModelNode evictionNode = nodeProvider.get();
		evictionNode.get(OP).set(ADD);
		address = evictionNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);
		address.add("eviction", "EVICTION");

		evictionNode.get("strategy").set("NONE");
		evictionNode.get("max-entries").set(10000);

		ModelNode expirationNode = nodeProvider.get();
		expirationNode.get(OP).set(ADD);
		address = expirationNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);
		address.add("expiration", "EXPIRATION");

		expirationNode.get("interval").set(60000L);
		expirationNode.get("lifespan").set(-1L);
		expirationNode.get("max-idle").set(-1L);

		ModelNode transactionNode = nodeProvider.get();
		transactionNode.get(OP).set(ADD);
		address = transactionNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);
		address.add("transaction", "TRANSACTION");

		transactionNode.get(LOCKING).set("PESSIMISTIC");
		transactionNode.get("mode").set("NONE");
		transactionNode.get("stop-timeout").set(30000);

		ModelNode lockingNode = nodeProvider.get();
		lockingNode.get(OP).set(ADD);
		address = lockingNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);
		address.add(LOCKING, "LOCKING");

		lockingNode.get("isolation").set("NONE");
		lockingNode.get("concurrency-level").set(10);
		lockingNode.get("striping").set(false);
	}

	private boolean defineCache(CacheConfiguration cacheConfiguration, Supplier<ModelNode> nodeProvider)
			throws RollbackedException {
		String cacheName = buildCacheName(cacheConfiguration.name());
		if (checkCachePresent(cacheName)) {
			return false;
		}
		LOGGER.debug("Building cache {}", cacheName);

		ModelNode cacheNode = nodeProvider.get();
		cacheNode.get(OP).set(ADD);
		ModelNode address = cacheNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);

		ModelNode evictionNode = nodeProvider.get();
		evictionNode.get(OP).set(ADD);
		address = evictionNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);
		address.add("eviction", "EVICTION");

		Eviction eviction = cacheConfiguration.eviction();
		evictionNode.get("strategy").set(eviction.strategy());
		evictionNode.get("max-entries").set(eviction.maxEntries());

		ModelNode expirationNode = nodeProvider.get();
		expirationNode.get(OP).set(ADD);
		address = expirationNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);
		address.add("expiration", "EXPIRATION");

		Expiration expiration = cacheConfiguration.expiration();
		expirationNode.get("interval").set(expiration.interval());
		expirationNode.get("lifespan").set(expiration.lifespan());
		expirationNode.get("max-idle").set(expiration.maxIdle());

		ModelNode transactionNode = nodeProvider.get();
		transactionNode.get(OP).set(ADD);
		address = transactionNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);
		address.add("transaction", "TRANSACTION");

		Transaction transaction = cacheConfiguration.transaction();
		transactionNode.get(LOCKING).set(transaction.locking().toString());
		transactionNode.get("mode").set(transaction.mode().toString());
		transactionNode.get("stop-timeout").set(transaction.stopTimeout());

		ModelNode lockingNode = nodeProvider.get();
		lockingNode.get(OP).set(ADD);
		address = lockingNode.get(OP_ADDR);
		appendCacheAddress(address, cacheName);
		address.add(LOCKING, "LOCKING");

		Locking locking = cacheConfiguration.locking();
		lockingNode.get("isolation").set(locking.isolation().toString());
		lockingNode.get("acquire-timeout").set(locking.acquireTimeout());
		lockingNode.get("concurrency-level").set(locking.concurrencyLevel());
		lockingNode.get("striping").set(locking.striping());
		return true;
	}

	private boolean checkCachePresent(String cacheName) throws RollbackedException {
		ModelNode node = new ModelNode();
		node.get(OP).set(ClientConstants.READ_RESOURCE_OPERATION);
		ModelNode address = node.get(OP_ADDR);
		appendCacheAddress(address, cacheName);

		ModelNode result = controller.execute(node);
		return isSuccessful(result);
	}

	private static void appendCacheAddress(ModelNode address, String cacheName) {
		address.add(SUBSYSTEM, INFINISPAN);
		address.add(CACHE_CONTAINER_KEY, CACHE_CONTAINER);
		address.add("local-cache", cacheName);
	}

	private String buildCacheName(String name) {
		String prefix = "";
		String tenantId = securityContext.getCurrentTenantId();
		if (!SecurityContext.isDefaultTenant(tenantId)) {
			prefix = tenantId + "_";
		}
		return prefix + name + "_REGION";
	}

	@Override
	public boolean unregisterCaches(Collection<String> names) {
		try {
			removeCaches(names);
		} catch (RollbackedException e) {
			LOGGER.warn("", e);
			return false;
		}
		return true;
	}

	private void removeCaches(Collection<String> names) throws RollbackedException {
		ModelNode root = new ModelNode();

		root.get(OP).set(COMPOSITE);

		ModelNode steps = root.get(STEPS);

		for (String name : names) {
			ModelNode cacheNode = steps.add();
			String cacheName = buildCacheName(name);

			cacheNode.get(OP).set(REMOVE_OPERATION);
			ModelNode address = cacheNode.get(OP_ADDR);
			appendCacheAddress(address, cacheName);
		}

		root.get(OPERATION_HEADERS, "allow-resource-service-restart").set(true);

		ModelNode result = controller.execute(root);
		if ("failed".equals(result.get(OUTCOME).asString())) {
			throw new TenantCreationException(result.get("failure-description").asString());
		}
	}
}
