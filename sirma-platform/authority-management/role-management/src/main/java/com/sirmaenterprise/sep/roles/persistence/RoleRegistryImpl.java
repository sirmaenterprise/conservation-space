package com.sirmaenterprise.sep.roles.persistence;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAO;
import com.sirma.itt.seip.cache.lookup.ReadOnlyEntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirmaenterprise.sep.roles.RoleActionModel.RoleActionMapping;
import com.sirmaenterprise.sep.roles.RoleDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Registry for user roles. The returned roles are only enabled active roles
 * <p>
 *
 * @author BBonev
 */
@Singleton
public class RoleRegistryImpl extends BaseRegistryImpl<RoleIdentifier, String, Role> implements RoleRegistry {

	@CacheConfiguration(eviction = @Eviction(maxEntries = -1), doc = @Documentation(""
			+ "Cache used to contain the unique roles in the system for instance type. The cache once initialized it's preferred not to expire. The cache content does not change in time and is limited in numbers around 20+/-10. "
			+ "<br>Minimal value expression: -1"))
	private static final String ROLE_REGISTRY_CACHE = "ROLE_REGISTRY_CACHE";

	private RoleManagement roleManagement;
	private LabelProvider labelProvider;

	/**
	 * Initialize the role registry instance with the required services
	 *
	 * @param roleManagement
	 *            the role management service instance
	 * @param labelProvider
	 *            the label provider service instance
	 * @param cacheContext
	 *            the cache context instance
	 */
	@Inject
	protected RoleRegistryImpl(RoleManagement roleManagement, LabelProvider labelProvider,
			EntityLookupCacheContext cacheContext) {
		super(cacheContext);
		this.roleManagement = roleManagement;
		this.labelProvider = labelProvider;
		configure();
	}

	@Override
	public Set<RoleIdentifier> getKeys() {
		return roleManagement.getRoles().filter(RoleDefinition::isEnabled).map(MappingUtil.defToRoleId()).collect(
				Collectors.toCollection(LinkedHashSet::new));
	}


	@Override
	protected String convertKey(RoleIdentifier sourceKey) {
		return sourceKey.getIdentifier();
	}

	@Override
	protected String getCacheName() {
		return ROLE_REGISTRY_CACHE;
	}

	@Override
	protected EntityLookupCallbackDAO<String, Role, Serializable> buildCacheCallback() {
		// do not leak the service scope to the cache store
		RoleManagement roles = roleManagement;
		LabelProvider labels = labelProvider;

		return ReadOnlyEntityLookupCallbackDAOAdaptor.from((String key) -> roles
				.getRole(key)
					.filter(RoleDefinition::isEnabled)
					.map(MappingUtil.defToRole(labels, getActiveActionMappings(roles)))
					.orElse(null));
	}

	private static Function<String, Stream<RoleActionMapping>> getActiveActionMappings(RoleManagement roles) {
		return roleId -> roles.getRoleActionModel().getActionsForRole(roleId).filter(
				entry -> entry.isEnabled() && entry.getAction().isEnabled());
	}
}
