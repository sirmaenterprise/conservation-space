package com.sirma.itt.emf.security.registry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.util.Documentation;

/**
 * Registry for user roles. To register new role/s a provider interface must be implemented
 * {@link com.sirma.itt.emf.security.model.RoleProvider}.
 * <p>
 * 
 * @author BBonev
 */
@ApplicationScoped
public class ActionRegistryImpl extends BaseProviderRegistry<Pair<Class<?>, String>, Action> implements ActionRegistry {

	/** The Constant ROLE_REGISTRY_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = -1), doc = @Documentation(""
			+ "Cache used to contain the unique actions for instance type. The cache once initialized it's preferred not to expire. The cache content does not change in time and is limited in numbers around 50+/-20. "
			+ "<br>Minimal value expression: -1"))
	private static final String ACTION_REGISTRY_CACHE = "ACTION_REGISTRY_CACHE";

	/** The Constant SELECTOR. */
	public static final String SELECTOR = "action";

	/** The providers. */
	@Inject
	@ExtensionPoint(ActionProvider.TARGET_NAME)
	private Iterable<ActionProvider> providers;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getCacheName() {
		return ACTION_REGISTRY_CACHE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Iterable<ActionProvider> getProviders() {
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
