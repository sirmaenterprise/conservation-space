package com.sirma.itt.emf.security.registry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleProvider;
import com.sirma.itt.emf.security.model.RoleRegistry;
import com.sirma.itt.emf.util.Documentation;

/**
 * Registry for user roles. To register new role/s a provider interface must be implemented
 * {@link RoleProvider}.
 * <p>
 * 
 * @author BBonev
 */
@ApplicationScoped
public class RoleRegistryImpl extends BaseProviderRegistry<RoleIdentifier, Role> implements RoleRegistry {

	/** The Constant ROLE_REGISTRY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = -1), doc = @Documentation(""
			+ "Cache used to contain the unique roles in the system for instance type. The cache once initialized it's preferred not to expire. The cache content does not change in time and is limited in numbers around 20+/-10. "
			+ "<br>Minimal value expression: -1"))
	private static final String ROLE_REGISTRY_CACHE = "ROLE_REGISTRY_CACHE";

	/** The Constant SELECTOR. */
	public static final String SELECTOR = "userRole";

	/** The providers. */
	@Inject
	@Any
	private Instance<RoleProvider> providers;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheName() {
		return ROLE_REGISTRY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Iterable<RoleProvider> getProviders() {
		return providers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getProviderSelector() {
		return SELECTOR;
	}
}
