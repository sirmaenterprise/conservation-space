package com.sirma.itt.seip.tenant.infinispan;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheConfigurationProvider;
import com.sirma.itt.seip.cache.CacheRegister;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;

/**
 * Infinispan provisioning for local cache, configured via Jboss CLI.
 *
 * @author BBonev
 */
@ApplicationScoped
public class LocalInfinispanProvisioning {

	private static final String ADDED_CACHES = "$addedCaches$";
	@Inject
	private CacheConfigurationProvider configurationExtension;

	@Inject
	private CacheRegister cacheRegister;

	@Inject
	private SecurityContextManager contextManager;

	/**
	 * Provision.
	 *
	 * @param properties
	 *            the properties
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void provision(Map<String, Serializable> properties, TenantInfo tenantInfo) {
		Collection<CacheConfiguration> configurations = configurationExtension.getConfigurations().values();

		boolean success = contextManager
				.executeAsTenant(tenantInfo.getTenantId())
					.predicate(cacheRegister::registerCaches, configurations);
		if (success) {
			properties.put(ADDED_CACHES, Boolean.TRUE);
		}
	}

	/**
	 * Rollback.
	 *
	 * @param properties
	 *            the properties
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void rollback(Map<String, Serializable> properties, TenantInfo tenantInfo) {
		// does not remove default cache configurations
		if (properties.containsKey(ADDED_CACHES) && !SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			contextManager.executeAsTenant(tenantInfo.getTenantId()).consumer(cacheRegister::unregisterCaches,
					getCacheNames());
		}
	}

	private List<String> getCacheNames() {
		return configurationExtension
				.getConfigurations()
					.values()
					.stream()
					.map(CacheConfiguration::name)
					.collect(Collectors.toList());
	}
}
