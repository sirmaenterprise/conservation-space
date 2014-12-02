/*
 *
 */
package com.sirma.itt.cmf.security.evaluator;

import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.services.adapter.CMFUserService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.RoleService;
import com.sirma.itt.emf.security.evaluator.BaseRoleEvaluator;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.util.Documentation;

/**
 * Role provider based on external role evaluation as final decision about a resource role. If not
 * needed in some sub module, should be overriden
 *
 * @author bbanchev
 */
@Extension(target = RoleEvaluator.TARGET_NAME, order = 200)
@ApplicationScoped
public class CmfDMSExternalEvaluator extends BaseRoleEvaluator<Instance> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmfDMSExternalEvaluator.class);
	/** The Constant ROLE_EVALUATORS. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
			+ "Cache used external user roles. <br>Minimal value expression: users"))
	protected static final String ROLE_EVALUATORS = "ROLE_EVALUATORS";

	/** The Constant TIMEOUT. */
	private static final int TIMEOUT = 2 * 60 * 1000;

	/** The entity cache context. */
	@Inject
	private EntityLookupCacheContext entityCacheContext;

	/** The role service. */
	@Inject
	protected RoleService roleService;

	/** The user service. */
	@Inject
	protected CMFUserService userService;
	// TODO as config
	private boolean enabled = true;

	/**
	 * Initializes the role cache.
	 */
	@Override
	@PostConstruct
	public void init() {
		if (!entityCacheContext.containsCache(getCacheName())) {
			entityCacheContext.createCache(getCacheName(), new UserRoleLookup());
		}
	}

	/**
	 * Gets the dms user role.
	 *
	 * @param user
	 *            the user
	 * @param container
	 *            the container
	 * @return the dms user role
	 */
	protected String getDmsUserRole(String user, String container) {
		try {
			return userService.getUserRole(user, container);
		} catch (DMSException e) {
			LOGGER.error("User information retrieval failed!", e);
		}
		return null;
	}

	/**
	 * Internal method for role evaluation.
	 *
	 * @param target
	 *            the target
	 * @param resource
	 *            the user
	 * @param settings
	 *            are the runtime settings to use
	 * @return the user role and the corresponding role provider, that calculated it
	 */
	@Override
	protected Pair<Role, RoleEvaluator<Instance>> evaluateInternal(Instance target,
			Resource resource, final RoleEvaluatorRuntimeSettings settings) {
		if (enabled) {
			RoleIdentifier roleId = VIEWER;

			String container = getContainer(target);
			if (container == null) {
				return constructRoleModel(VIEWER);
			}

			String userRole = getCachedUserRole(resource.getIdentifier(), container);
			if (userRole == null) {
				return constructRoleModel(VIEWER);
			}

			roleId = roleService.getRoleIdentifier(userRole.toLowerCase());

			return new Pair<Role, RoleEvaluator<Instance>>(registry.find(roleId), this);
		}
		return null;
	}

	/**
	 * Gets the cache name.
	 *
	 * @return the cache name
	 */
	protected String getCacheName() {
		return ROLE_EVALUATORS;
	}

	/**
	 * Gets the cache.
	 *
	 * @return the cache
	 */
	protected EntityLookupCache<Pair<String, String>, Pair<String, Long>, Serializable> getCache() {
		return entityCacheContext.getCache(getCacheName());
	}

	/**
	 * Gets the cached user role. The cache contains the values only for
	 *
	 * @param userId
	 *            the user id
	 * @param container
	 *            the container
	 * @return the cached user role {@link CmfDMSExternalEvaluator#TIMEOUT} minutes
	 */
	private String getCachedUserRole(String userId, String container) {
		Pair<String, String> key = new Pair<String, String>(userId, container);
		EntityLookupCache<Pair<String, String>, Pair<String, Long>, Serializable> cache = getCache();
		Pair<Pair<String, String>, Pair<String, Long>> pair = cache.getByKey(key);
		if (pair == null) {
			return null;
		}
		// if we have created the record entry before the allowed time, we just
		// remove it and call the method again to fetch it a new.
		if ((System.currentTimeMillis() - pair.getSecond().getSecond()) > TIMEOUT) {
			cache.removeByKey(key);
			return getCachedUserRole(userId, container);
		}
		return pair.getSecond().getFirst();
	}

	/**
	 * The Class UserRoleLookup.
	 *
	 * @author BBonev
	 */
	public class UserRoleLookup extends
			EntityLookupCallbackDAOAdaptor<Pair<String, String>, Pair<String, Long>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, Pair<String, Long>> findByKey(Pair<String, String> key) {
			String role = getDmsUserRole(key.getFirst(), key.getSecond());

			if ((role == null) || role.equalsIgnoreCase("none")) {
				return new Pair<Pair<String, String>, Pair<String, Long>>(key,
						new Pair<String, Long>(null, System.currentTimeMillis()));
			}

			return new Pair<Pair<String, String>, Pair<String, Long>>(key, new Pair<String, Long>(
					role.toLowerCase(), System.currentTimeMillis()));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Pair<String, String>, Pair<String, Long>> createValue(Pair<String, Long> value) {
			throw new UnsupportedOperationException("Value cannot be created");
		}

	}

	@Override
	protected Boolean filterInternal(Instance target, Resource resource, Role role,
			Set<Action> actions) {
		return null;
	}

	@Override
	public List<Class<?>> getSupportedObjects() {
		return Arrays.asList(new Class<?>[] { Instance.class });
	}

	@Override
	public boolean canHandle(Object target) {
		return super.canHandle(target);
	}
}