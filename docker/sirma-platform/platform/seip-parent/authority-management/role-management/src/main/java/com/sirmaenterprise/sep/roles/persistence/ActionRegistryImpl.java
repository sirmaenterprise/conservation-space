package com.sirmaenterprise.sep.roles.persistence;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAO;
import com.sirma.itt.seip.cache.lookup.ReadOnlyEntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.action.ActionRegistry;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Registry for user actions. To register new role/s a provider interface must be implemented
 * {@link com.sirma.itt.seip.permissions.action.ActionProvider}.
 * <p>
 *
 * @author BBonev
 */
@Singleton
public class ActionRegistryImpl extends BaseRegistryImpl<String, String, Action> implements ActionRegistry {

	@CacheConfiguration(eviction = @Eviction(maxEntries = -1), doc = @Documentation(""
			+ "Cache used to contain the unique actions for instance type. The cache once initialized it's preferred not to expire. The cache content does not change in time and is limited in numbers around 50+/-20. "
			+ "<br>Minimal value expression: -1"))
	private static final String ACTION_REGISTRY_CACHE = "ACTION_REGISTRY_CACHE";

	private RoleManagement roleManagement;
	private LabelProvider labelProvider;

	/**
	 * Initialize the action registry instance with the required services
	 *
	 * @param roleManagement
	 *            the role management service instance
	 * @param labelProvider
	 *            the label provider service instance
	 * @param cacheContext
	 *            the cache context instance
	 */
	@Inject
	public ActionRegistryImpl(RoleManagement roleManagement, LabelProvider labelProvider,
			EntityLookupCacheContext cacheContext) {
		super(cacheContext);
		this.roleManagement = roleManagement;
		this.labelProvider = labelProvider;
		configure();
	}

	@Override
	public Set<String> getKeys() {
		return roleManagement.getActions().filter(ActionDefinition::isEnabled).map(ActionDefinition::getId).collect(
				Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	protected String getCacheName() {
		return ACTION_REGISTRY_CACHE;
	}

	@Override
	protected String convertKey(String sourceKey) {
		return sourceKey;
	}

	@Override
	protected EntityLookupCallbackDAO<String, Action, Serializable> buildCacheCallback() {
		// do not leak the service scope to the cache store
		LabelProvider labels = labelProvider;
		RoleManagement roles = roleManagement;

		return ReadOnlyEntityLookupCallbackDAOAdaptor.from((String key) -> roles
				.getAction(key)
					.filter(ActionDefinition::isEnabled)
					.map(MappingUtil.defToAction(labels))
					.orElse(null));
	}

}
