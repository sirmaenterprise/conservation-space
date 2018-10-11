package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.NO_PERMISSION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.PrimaryStateFactory;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.RoleActionFilterService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorManagerService;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Common implementation for {@link RoleEvaluator}.
 *
 * @param <E>
 *            the element type
 */
public abstract class BaseRoleEvaluator<E> implements RoleEvaluator<E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseRoleEvaluator.class);

	private static final Set<Action> NO_ACTIONS = new HashSet<>(5);

	private static final RoleIdentifier[] ROLE_IDENTIFIERS_EMPTY_ARRAY = new RoleIdentifier[0];

	@Inject
	protected RoleRegistry registry;

	@Inject
	protected StateService stateService;

	@Inject
	protected InstanceService instanceService;

	@Inject
	protected AuthorityService authorityService;

	@Inject
	protected StateTransitionManager transitionManager;

	@Inject
	protected javax.enterprise.inject.Instance<RoleEvaluatorManagerService> roleEvaluatorManagerService;

	@Inject
	protected PrimaryStateFactory stateFactory;

	@Inject
	protected ResourceService resourceService;

	@Inject
	protected PermissionService permissionService;

	@Inject
	protected RoleActionFilterService actionEvaluatorService;

	protected Deque<RoleEvaluator<E>> roleEvaluatorsChain;

	private boolean sealed;

	protected RoleEvaluatorRuntimeSettings chainRuntimeSettings;

	private PrimaryStates deleted;
	private PrimaryStates completed;
	private PrimaryStates canceled;

	/**
	 * Initializes the role cache.
	 */
	@PostConstruct
	public void init() {
		chainRuntimeSettings = new RoleEvaluatorRuntimeSettings();
		chainRuntimeSettings
				.setIrrelevantRoles(new ArrayList<>(Collections.singletonList(SecurityModel.BaseRoles.CREATOR)));

		// no need to worry for multiple creations of the action objects - the factory uses a
		// synchronized cache
		deleted = stateFactory.create(PrimaryStates.DELETED_KEY);
		completed = stateFactory.create(PrimaryStates.COMPLETED_KEY);
		canceled = stateFactory.create(PrimaryStates.CANCELED_KEY);
	}

	@Override
	public boolean canHandle(Object target) {
		if (target == null) {
			return false;
		}
		// by provided class
		if (target.getClass() == Class.class) {
			return getSupportedObjects().contains(target);
		}
		// by instance
		return getSupportedObjects().contains(target.getClass());
	}

	/**
	 * Check if role is irrelevant
	 *
	 * @param settings
	 *            is the current settings to use
	 * @param skipped
	 *            is the role that is irrelevant
	 * @return true if role is contained in settings as irrelevant
	 */
	protected boolean isRoleIrrelevant(final RoleEvaluatorRuntimeSettings settings, RoleIdentifier skipped) {
		return settings == null
				|| settings.getIrrelevantRoles() != null && !settings.getIrrelevantRoles().contains(skipped);
	}

	@Override
	public Pair<Role, RoleEvaluator<E>> evaluate(E target, Resource resource, RoleEvaluatorRuntimeSettings settings) {
		if (target == null || resource == null || resource.getId() == null) {
			return null;
		}
		if (isAdminOrSystemUser(resource)) {
			return constructRoleModel(SecurityModel.BaseRoles.ADMINISTRATOR);
		}
		Pair<Role, RoleEvaluator<E>> role = evaluateInternal(target, resource, settings);
		return calculateHighestRoleFromChain(target, resource, role, settings, getPreferredHighPriorityRoles());
	}

	/**
	 * Calculate highest role for the given instance and user from the current role evaluators chain. Advance
	 * calculation could be used if the current role matches the one of the specified roles in the last argument.
	 *
	 * @param target
	 *            the target instance on that we calculate the role of the user
	 * @param resource
	 *            the resource fir witch we calculate role
	 * @param role
	 *            the current calculated role
	 * @param settings
	 *            are the runtime settings to use
	 * @param preferredRoles
	 *            the preferred roles if any.
	 * @return the role pair with the highest priority
	 */
	protected Pair<Role, RoleEvaluator<E>> calculateHighestRoleFromChain(E target, Resource resource,
			Pair<Role, RoleEvaluator<E>> role, RoleEvaluatorRuntimeSettings settings,
			RoleIdentifier... preferredRoles) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Calculating role for {} with chain {}",
					target instanceof Entity ? ((Entity<?>) target).getId() : "null", roleEvaluatorsChain);
		}
		if (role == null || role.getFirst() == null) {
			return role;
		}
		if (preferredRoles != null && preferredRoles.length > 0) {
			for (RoleIdentifier roleIdentifier : preferredRoles) {
				// role identifiers should be compared only the identifiers, not by objects itself
				// due to that could be of different types
				if (roleIdentifier.getIdentifier().equals(role.getFirst().getRoleId().getIdentifier())) {
					LOGGER.trace("Returned role from the preferred list or roles " + roleIdentifier.getIdentifier());
					return role;
				}
			}
		}
		Pair<Role, RoleEvaluator<E>> currentRole = role;
		if (roleEvaluatorsChain != null) {
			Pair<Role, RoleEvaluator<E>> highestRole = currentRole;
			RoleEvaluatorManagerService evaluatorManagerService = roleEvaluatorManagerService.get();
			for (RoleEvaluator<E> roleEvaluator : roleEvaluatorsChain) {
				currentRole = roleEvaluator.evaluate(target, resource, settings);
				if (currentRole != null) {
					Role highestRoleCalculated = evaluatorManagerService.getHighestRole(currentRole.getFirst(),
							highestRole.getFirst());
					highestRole.setFirst(highestRoleCalculated);
				}
			}
			return highestRole;
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Role on instance: {} for {} is {}",
					target instanceof Instance ? ((Instance) target).getId() : target.toString(), resource.getName(),
					currentRole.getFirst() == null ? "NULL" : currentRole.getFirst().getRoleId());
		}
		return currentRole;
	}

	/**
	 * High priority roles for the current evaluator. When evaluating roles the list of roles returned from the method
	 * will be considered with higher priority than the ones returned from the role evaluator chain.
	 *
	 * @return the preferred roles
	 */
	protected RoleIdentifier[] getPreferredHighPriorityRoles() {
		return ROLE_IDENTIFIERS_EMPTY_ARRAY;
	}

	/**
	 * {@inheritDoc}<br>
	 * The chain of evaluators is invoked in reverse order ( from highest to lowest priority) to filter actions.
	 * <strong>Client code may override to stop chain behavior for invoking
	 * {@link #filterInternal(Object, Resource, Role, Set)} on each evaluator</strong>
	 */
	@Override
	public Set<Action> filterActions(E target, Resource resource, Role role) {
		if (target == null || resource == null) {
			return NO_ACTIONS;
		}
		Set<Action> actions = getCalculatedActions(target, resource, role);
		Boolean filterInternal = filterInternal(target, resource, role, actions);
		if (roleEvaluatorsChain != null) {
			// iterate in reverse order for walk back the chain
			Iterator<RoleEvaluator<E>> listIterator = roleEvaluatorsChain.descendingIterator();
			while (listIterator.hasNext() && !Boolean.TRUE.equals(filterInternal)) {
				BaseRoleEvaluator<E> roleEvaluator = (BaseRoleEvaluator<E>) listIterator.next();
				filterInternal = roleEvaluator.filterInternal(target, resource, role, actions);
			}
		}
		return actions;
	}

	/**
	 * Factory method to return the actions for some instance. Should be override to provide specific retrieval of
	 * actions
	 *
	 * @param target
	 *            is the target instance
	 * @param resource
	 *            is the user currently used
	 * @param role
	 *            is the evaluated role to get actions for
	 * @return the set of actions
	 */
	protected Set<Action> getCalculatedActions(E target, Resource resource, Role role) {
		return getAllowedActions((Instance) target, resource, role);
	}

	/**
	 * Invoked by the {@link #filterActions(Object, Resource, Role)} method to filter the current instance with the
	 * current evaluator.
	 *
	 * @param target
	 *            is the target instance
	 * @param resource
	 *            is the user currently used
	 * @param role
	 *            is the evaluated role to get actions for
	 * @param actions
	 *            are the current actions, initially produced by
	 * @return null on unknown object/state. false when decision should not be considered final, true when decision for
	 *         filtered action is final and iteration should be interrupted
	 *         {@link #getCalculatedActions(Object, Resource, Role)}
	 */
	protected abstract Boolean filterInternal(E target, Resource resource, Role role, Set<Action> actions);

	/**
	 * Construct role model based on {@link RoleIdentifier} that is mapped to role by the.
	 *
	 * @param roleId
	 *            the role id
	 * @return the pair of role and current evaluator {@link #registry}
	 */
	protected Pair<Role, RoleEvaluator<E>> constructRoleModel(RoleIdentifier roleId) {
		return new Pair<>(registry.find(roleId), this);
	}

	/**
	 * Constructs role model based on the evaluated role from the provided instance and current evaluator.
	 *
	 * @param instance
	 *            is the instance to check
	 * @param resource
	 *            is the resource to check role for
	 * @param defaultRole
	 *            on fail on missing role return it
	 * @param settings
	 *            are the runtime settings to use
	 * @return the pair of role and current evaluator
	 */
	protected Pair<Role, RoleEvaluator<E>> constructRoleModel(Instance instance, Resource resource,
			RoleIdentifier defaultRole, RoleEvaluatorRuntimeSettings settings) {
		if (instance == null) {
			return constructRoleModel(defaultRole);
		}
		RoleEvaluator<Instance> rootEvaluator = roleEvaluatorManagerService.get().getRootEvaluator(instance);
		if (rootEvaluator == null) {
			return constructRoleModel(defaultRole);
		}
		Pair<Role, RoleEvaluator<Instance>> evaluate = rootEvaluator.evaluate(instance, resource, settings);
		if (evaluate != null && evaluate.getFirst() != null) {
			return constructRoleModel(evaluate.getFirst().getRoleId());
		}
		return constructRoleModel(defaultRole);
	}

	/**
	 * Finds the assigned permission if there is any or returns {@link SecurityModel.BaseRoles#NO_PERMISSION} model.
	 *
	 * @param target
	 *            is the instance to check
	 * @param resource
	 *            the resource to check for
	 * @return the found role or NO_PERMISSION if could
	 */
	protected Pair<Role, RoleEvaluator<E>> getAssignedPermission(Instance target, Resource resource) {
		return getAssignedPermission(target, resource, NO_PERMISSION);
	}

	/**
	 * Finds the assigned permission if there is any or returns the given defaultRole model.
	 *
	 * @param target
	 *            is the instance to check
	 * @param resource
	 *            the resource to check for
	 * @param defaultRole
	 *            the default role to use
	 * @return the found role or the defaultRole
	 */
	protected Pair<Role, RoleEvaluator<E>> getAssignedPermission(Instance target, Resource resource,
			RoleIdentifier defaultRole) {
		ResourceRole resourceRole = permissionService.getPermissionAssignment(target.toReference(),
				resource.getId());
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(resource.getName() + " has role " + resourceRole + " on [" + target.getIdentifier() + "/"
					+ target.getId() + "]");
		}
		return constructRoleModel(resourceRole == null ? defaultRole : resourceRole.getRole());
	}

	/**
	 * Checks if the given instance is in primary status.
	 *
	 * @param instance
	 *            the instance to check
	 * @return true, if instance is the desired status, false if any parameter is null or instance is not in the status
	 */
	protected boolean isInInactiveState(Instance instance) {
		return isInstanceInStates(instance, deleted, completed, canceled);
	}

	/**
	 * Checks if is deleted.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if is deleted
	 */
	protected boolean isDeleted(Instance instance) {
		return instance.isDeleted();
	}

	/**
	 * Checks if the given instance is in primary status.
	 *
	 * @param instance
	 *            the instance to check
	 * @param states
	 *            the states
	 * @return true, if instance is the desired status, false if any parameter is null or instance is not in the status
	 */
	protected boolean isInstanceInStates(Instance instance, PrimaryStates... states) {
		boolean result = false;
		if (instance != null && states != null) {
			result = stateService.isInStates(instance, states);
		}
		return result;
	}

	/**
	 * Check if user has manager role (on root level of the instance) no matter what is the role assigned.
	 *
	 * @param instance
	 *            is the instance to check for
	 * @param resource
	 *            is the resource to check
	 * @return if the role is manager on root level
	 */
	protected boolean isRootManager(Instance instance, Resource resource) {
		Map<String, ResourceRole> assignments = permissionService.getPermissionAssignments(instance.toReference());
		ResourceRole role = assignments.get(resource.getId());
		if (role != null) {
			return role.getRoleAssignments().isManager();
		}

		return false;
	}

	/**
	 * Internal method for role evaluation.
	 *
	 * @param target
	 *            the target
	 * @param user
	 *            the user
	 * @param settings
	 *            are the runtime settings to use
	 * @return the user role and the corresponding role provider, that calculated it
	 */
	protected abstract Pair<Role, RoleEvaluator<E>> evaluateInternal(E target, Resource user,
			RoleEvaluatorRuntimeSettings settings);

	/**
	 * Checks if the given user is admin or system user.
	 *
	 * @param user
	 *            the user
	 * @return true, if is admin or system user
	 */
	protected boolean isAdminOrSystemUser(Resource user) {
		return authorityService.isAdminOrSystemUser(user);
	}

	/**
	 * Gets the instance status.
	 *
	 * @param instance
	 *            the instance
	 * @return the instance status
	 */
	protected String getInstanceStatus(Instance instance) {
		if (instance != null) {
			return stateService.getPrimaryState(instance);
		}
		return null;
	}

	/**
	 * Gets the allowed actions based on the given role and targets allowed actions per state.
	 *
	 * @param target
	 *            the target
	 * @param authority
	 *            is the current evaluating authority
	 * @param localRole
	 *            the local role
	 * @return the allowed actions
	 */
	protected Set<Action> getAllowedActions(Instance target, Resource authority, Role localRole) {
		if (localRole == null || target == null) {
			return new HashSet<>(5);
		}
		return transitionManager.getAllowedActions(target, getInstanceStatus(target),
				localRole.getAllowedActions(buildContext(target, localRole, authority)));
	}

	/**
	 * Builds a {@link RoleActionEvaluatorContext} based on input data
	 *
	 * @param target
	 *            is the current instance under evaluation
	 * @param localRole
	 *            is the current role under evaluation
	 * @param resource
	 *            is the current user under evaluation
	 * @return a new instance of {@link RoleActionEvaluatorContext}
	 */
	protected RoleActionEvaluatorContext buildContext(Instance target, Role localRole, Resource resource) {
		return new RoleActionEvaluatorContext(actionEvaluatorService, target, resource, localRole);
	}

	@Override
	public Deque<RoleEvaluator<E>> addChainInOrder(Collection<RoleEvaluator<E>> evaluators) {
		if (evaluators == null || evaluators.isEmpty() || isSealed()) {
			return roleEvaluatorsChain;
		}
		if (roleEvaluatorsChain == null) {
			roleEvaluatorsChain = new LinkedList<>();
		}

		roleEvaluatorsChain.addAll(evaluators);
		seal();
		return roleEvaluatorsChain;
	}

	@Override
	public synchronized void seal() {
		sealed = true;
	}

	@Override
	public synchronized boolean isSealed() {
		return sealed;
	}

}
