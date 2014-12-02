/*
 *
 */
package com.sirma.itt.emf.security.evaluator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorManagerService;
import com.sirma.itt.emf.security.RoleEvaluatorRuntimeSettings;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.ActionRegistry;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleRegistry;
import com.sirma.itt.emf.state.PrimaryStateFactory;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Common implementation for {@link RoleEvaluator}.
 * <p>
 * <b>NOTE: </b>The subclasses should override the
 * {@link #getRoleFromExternalSource(Object, com.sirma.itt.emf.security.model.User)} method if they
 * don't like the default implementation that checks in DMS for the user role
 * 
 * @param <E>
 *            the element type
 */
public abstract class BaseRoleEvaluator<E> implements RoleEvaluator<E> {

	/** The Constant NO_ACTIONS. */
	static final Set<Action> NO_ACTIONS = new HashSet<Action>(5);

	/** The registry. */
	@Inject
	protected RoleRegistry registry;

	/** The state service. */
	@Inject
	protected StateService stateService;

	/** The instance service. */
	@Inject
	@Proxy
	protected InstanceService<Instance, DefinitionModel> instanceService;

	@Inject
	protected AuthorityService authorityService;

	/** The transition manager. */
	@Inject
	protected StateTransitionManager transitionManager;

	/** The action registry. */
	@Inject
	protected ActionRegistry actionRegistry;

	/** The role evaluator manager service. */
	@Inject
	protected javax.enterprise.inject.Instance<RoleEvaluatorManagerService> roleEvaluatorManagerService;

	/** The resource service. */
	@Inject
	protected ResourceService resourceService;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseRoleEvaluator.class);

	/** The trace. */
	private static final boolean TRACE = LOGGER.isTraceEnabled();
	/** The chain. */
	protected Deque<RoleEvaluator<E>> roleEvaluatorsChain;

	/** The sealed. */
	private boolean sealed;

	protected RoleEvaluatorRuntimeSettings chainRuntimeSettings;

	@Inject
	protected PrimaryStateFactory stateFactory;

	private PrimaryStateType deleted;
	private PrimaryStateType completed;
	private PrimaryStateType canceled;

	/**
	 * Initializes the role cache.
	 */
	@PostConstruct
	public void init() {
		chainRuntimeSettings = new RoleEvaluatorRuntimeSettings();
		chainRuntimeSettings.setIrrelevantRoles(new ArrayList<RoleIdentifier>(Collections
				.singletonList(BaseRoles.CREATOR)));

		// no need to worry for multiple creations of the action objects - the factory uses a
		// synchronized cache
		deleted = stateFactory.create(PrimaryStateType.DELETED);
		completed = stateFactory.create(PrimaryStateType.COMPLETED);
		canceled = stateFactory.create(PrimaryStateType.CANCELED);
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * @return true if role is contained in settigns as irrelevant
	 */
	protected boolean isRoleIrrelevant(final RoleEvaluatorRuntimeSettings settings,
			RoleIdentifier skipped) {
		// FIXME: check if the implementation is correct and uncomment
		// for now the checks are disabled
		return (settings == null)
				|| ((settings.getIrrelevantRoles() != null) && !settings.getIrrelevantRoles()
						.contains(skipped));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Role, RoleEvaluator<E>> evaluate(E target, Resource resource,
			RoleEvaluatorRuntimeSettings settings) {
		if ((target == null) || ((resource == null) || (resource.getId() == null))) {
			return null;
		}
		if (isAdminOrSystemUser(resource)) {
			return constructRoleModel(SecurityModel.BaseRoles.ADMINISTRATOR);
		}
		Pair<Role, RoleEvaluator<E>> role = evaluateInternal(target, resource, settings);
		Pair<Role, RoleEvaluator<E>> highestRole = calculateHighestRoleFromChain(target, resource,
				role, settings, getPreferredHighPriorityRoles());
		return highestRole;
	}

	/**
	 * Calculate highest role for the given instance and user from the current role evaluators
	 * chain. Advance calculation could be used if the current role matches the one of the specified
	 * roles in the last argument.
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
	protected Pair<Role, RoleEvaluator<E>> calculateHighestRoleFromChain(E target,
			Resource resource, Pair<Role, RoleEvaluator<E>> role,
			RoleEvaluatorRuntimeSettings settings, RoleIdentifier... preferredRoles) {
		if (TRACE) {
			LOGGER.trace("Calculating role for {} with chain {}",
					(target instanceof Entity) ? ((Entity<?>) target).getId() : "null",
					roleEvaluatorsChain);
		}
		if ((role == null) || (role.getFirst() == null)) {
			return role;
		}
		if ((preferredRoles != null) && (preferredRoles.length > 0)) {
			for (int i = 0; i < preferredRoles.length; i++) {
				RoleIdentifier roleIdentifier = preferredRoles[i];
				// role identifiers should be compared only the identifiers, not by objects itself
				// due to that could be of different types
				if (roleIdentifier.getIdentifier().equals(
						role.getFirst().getRoleId().getIdentifier())) {
					if (TRACE) {
						LOGGER.trace("Returned role from the preferred list or roles "
								+ roleIdentifier.getIdentifier());
					}
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
					Role highestRoleCalculated = evaluatorManagerService.getHighestRole(
							currentRole.getFirst(), highestRole.getFirst());
					highestRole.setFirst(highestRoleCalculated);
				}
			}
			return highestRole;
		}
		if (TRACE) {
			LOGGER.trace("Role on instance: {} for {} is {}",
					(target instanceof Instance) ? ((Instance) target).getId() : target.toString(),
					resource.getIdentifier(), currentRole.getFirst() == null ? "NULL" : currentRole
							.getFirst().getRoleId());
		}
		return currentRole;
	}

	/**
	 * High priority roles for the current evaluator. When evaluating roles the list of roles
	 * returned from the method will be considered with higher priority than the ones returned from
	 * the role evaluator chain.
	 *
	 * @return the preferred roles
	 */
	protected RoleIdentifier[] getPreferredHighPriorityRoles() {
		return null;
	}

	/**
	 * {@inheritDoc}<br>
	 * The chain of evaluators is invoked in reverse order ( from highest to lowest priority) to
	 * filter actions.<strong>Client code may override to stop chain behavior for invoking
	 * {@link #filterInternal(Object, Resource, Role, Set)} on each evaluator</strong>
	 */
	@Override
	public Set<Action> filterActions(E target, Resource resource, Role role) {

		if ((target == null) || (resource == null)) {
			return NO_ACTIONS;
		}
		// check for cached actions
		// REVIEW: NOTE that this optimization is valid only if the same instance is evaluated again
		// it should be removed when all evaluation is done from REST services.
		if (target instanceof Instance) {
			Instance instance = (Instance) target;
			Serializable serializable = instance.getProperties().get(
					DefaultProperties.EVALUATED_ACTIONS);
			if (serializable instanceof Pair) {
				Pair<String, Set<Action>> pair = (Pair<String, Set<Action>>) serializable;
				String state = stateService.getPrimaryState(instance);
				if (EqualsHelper.nullSafeEquals(state, pair.getFirst())) {
					return new LinkedHashSet<>(pair.getSecond());
				}
			}
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
		if (target instanceof Instance) {
			Instance instance = (Instance) target;
			String state = stateService.getPrimaryState(instance);
			instance.getProperties().put(DefaultProperties.EVALUATED_ACTIONS,
					new Pair<String, Set<Action>>(state, new LinkedHashSet<>(actions)));
		}
		return actions;
	}

	/**
	 * Factory method to return the actions for some instance. Should be override to provide
	 * specific retrieval of actions
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
		return getAllowedActions((Instance) target, role);
	}

	/**
	 * Invoked by the {@link #filterActions(Object, Resource, Role)} method to filter the current
	 * instance with the current evaluator.
	 *
	 * @param target
	 *            is the target instance
	 * @param resource
	 *            is the user currently used
	 * @param role
	 *            is the evaluated role to get actions for
	 * @param actions
	 *            are the current actions, initially produced by
	 * @return null on unknown object/state. false when decision should not be considered final,
	 *         true when decision for filtered action is final and iteration should be interrupted
	 *         {@link #getCalculatedActions(Object, Resource, Role)}
	 */
	protected abstract Boolean filterInternal(E target, Resource resource, Role role,
			Set<Action> actions);

	/**
	 * Construct role model based on {@link RoleIdentifier} that is mapped to role by the.
	 *
	 * @param roleId
	 *            the role id
	 * @return the pair of role and current evaluator {@link #registry}
	 */
	protected Pair<Role, RoleEvaluator<E>> constructRoleModel(RoleIdentifier roleId) {
		return new Pair<Role, RoleEvaluator<E>>(registry.find(roleId), this);
	}

	/**
	 * Constructs role model based on the evaluated role from the provided instance and current
	 * evaluator.
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
			BaseRoles defaultRole, RoleEvaluatorRuntimeSettings settings) {
		if (instance == null) {
			return constructRoleModel(defaultRole);
		}
		RoleEvaluator<Instance> rootEvaluator = roleEvaluatorManagerService.get().getRootEvaluator(
				instance);
		if (rootEvaluator == null) {
			return constructRoleModel(defaultRole);
		}
		// XXX this here enters infinite cycle!
		Pair<Role, RoleEvaluator<Instance>> evaluate = rootEvaluator.evaluate(instance, resource,
				settings);
		if ((evaluate != null) && (evaluate.getFirst() != null)) {
			return constructRoleModel(evaluate.getFirst().getRoleId());
		}
		return constructRoleModel(defaultRole);
	}

	/**
	 * Checks if the given instance is in primary status.
	 *
	 * @param instance
	 *            the instance to check
	 * @return true, if instance is the desired status, false if any parameter is null or instance
	 *         is not in the status
	 */
	protected boolean isInInactiveState(com.sirma.itt.emf.instance.model.Instance instance) {
		return isInstanceInStates(instance, deleted, completed, canceled);
	}

	/**
	 * Checks if is deleted.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if is deleted
	 */
	protected boolean isDeleted(com.sirma.itt.emf.instance.model.Instance instance) {
		return isInstanceInStates(instance, deleted);
	}

	/**
	 * Checks if the given instance is in primary status.
	 *
	 * @param instance
	 *            the instance to check
	 * @param states
	 *            the states
	 * @return true, if instance is the desired status, false if any parameter is null or instance
	 *         is not in the status
	 */
	protected boolean isInstanceInStates(com.sirma.itt.emf.instance.model.Instance instance,
			PrimaryStateType... states) {
		boolean result = false;
		if ((instance != null) && (states != null)) {
			result = stateService.isInStates(instance, states);
		}
		return result;
	}

	/**
	 * Gets the container for the given instance
	 *
	 * @param target
	 *            the target
	 * @return the container or <code>null</code> if not supported or in no container
	 */
	protected String getContainer(E target) {
		TenantAware parent = InstanceUtil.getParent(TenantAware.class, (Instance) target);
		if (parent != null) {
			return parent.getContainer();
		}
		return null;
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
	 * @param localRole
	 *            the local role
	 * @return the allowed actions
	 */
	protected Set<Action> getAllowedActions(Instance target, Role localRole) {
		if ((localRole == null) || (target == null)) {
			return new HashSet<Action>(5);
		}
		return transitionManager.getAllowedActions(target, getInstanceStatus(target),
				localRole.getAllowedActions(target.getClass()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Deque<RoleEvaluator<E>> addChainInOrder(
			Collection<RoleEvaluator<E>> evaluators) {
		if ((evaluators == null) || evaluators.isEmpty() || isSealed()) {
			return roleEvaluatorsChain;
		}
		if (roleEvaluatorsChain == null) {
			roleEvaluatorsChain = new LinkedList<RoleEvaluator<E>>();
		}

		roleEvaluatorsChain.addAll(evaluators);
		seal();
		return roleEvaluatorsChain;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void seal() {
		sealed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean isSealed() {
		return sealed;
	}

}