package com.sirma.itt.seip.permissions.action;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.permissions.FilterActionBinding;
import com.sirma.itt.seip.permissions.PermissionIdentifier;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorManagerService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default implementation for the {@link AuthorityService}. The implementation collects the registered
 * {@link RoleEvaluator}s and navigates to the correct evaluator on each different instance. If the instance is not
 * supported then {@link EmfRuntimeException} is thrown
 *
 * @author BBonev
 */
@Named("authorityService")
@ApplicationScoped
public class AuthorityServiceImpl implements AuthorityService, Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6491452577004813734L;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorityServiceImpl.class);

	/** The evaluate actions. */
	@Inject
	private EventService eventService;

	/** The people service. */
	@Inject
	private ResourceService resourceService;

	/** The action registry. */
	@Inject
	private ActionRegistry actionRegistry;

	/** The role evaluator manager service. */
	@Inject
	private RoleEvaluatorManagerService roleEvaluatorManagerService;

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private SecurityContext securityContext;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Action> getAllowedActions(Instance instance, String placeholder) {
		return filterActions(getRoleEvaluatorForInstance(instance), instance, placeholder);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Action> getAllowedActions(String user, Instance instance, String placeholder) {
		return filterActions(getRoleEvaluatorForInstance(instance), instance, user, placeholder);
	}

	/**
	 * Gets the role evaluator that can handle the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the role evaluator for instance
	 */
	protected RoleEvaluator<Instance> getRoleEvaluatorForInstance(Instance instance) {
		if (instance == null) {
			throw new EmfRuntimeException("Cannot get role evaluator for NULL instance");
		}
		RoleEvaluator<Instance> evaluator = roleEvaluatorManagerService.getRootEvaluator(instance);
		if (evaluator != null) {
			return evaluator;
		}
		throw new EmfRuntimeException("Failed to find a valid " + RoleEvaluator.class
				+ " implementation that supports instances of type " + instance.getClass());
	}

	/**
	 * Filter actions.
	 *
	 * @param <E>
	 *            the element type
	 * @param evaluator
	 *            the evaluator
	 * @param target
	 *            the target
	 * @param placeholder
	 *            the placeholder
	 * @return the sets the
	 */
	private <E extends Instance> Set<Action> filterActions(RoleEvaluator<E> evaluator, E target, String placeholder) {
		Resource user = getCurrentUser();
		return filterActions(evaluator, target, user, placeholder);
	}

	/**
	 * Gets the current user.
	 *
	 * @return the current user
	 */
	private Resource getCurrentUser() {
		return (Resource) securityContext.getAuthenticated();
	}

	/**
	 * Filter actions.
	 *
	 * @param <E>
	 *            the element type
	 * @param evaluator
	 *            the evaluator
	 * @param target
	 *            the target
	 * @param userId
	 *            the user id
	 * @param placeholder
	 *            the placeholder
	 * @return the sets the
	 */
	private <E extends Instance> Set<Action> filterActions(RoleEvaluator<E> evaluator, E target, String userId,
			String placeholder) {
		Resource user = resourceService.getResource(userId, ResourceType.USER);
		return filterActions(evaluator, target, user, placeholder);
	}

	/**
	 * Filter actions.
	 *
	 * @param <E>
	 *            the element type
	 * @param evaluator
	 *            the evaluator
	 * @param target
	 *            the target
	 * @param user
	 *            the user
	 * @param placeholder
	 *            the placeholder
	 * @return the sets the
	 */
	private <E extends Instance> Set<Action> filterActions(RoleEvaluator<E> evaluator, E target, Resource user,
			String placeholder) {
		Pair<Role, RoleEvaluator<E>> role = evaluator.evaluate(target, user, null);
		if (role == null || role.getFirst() == null) {
			LOGGER.warn("Missing role for [" + target.getIdentifier() + "/" + target.getId() + "] on user "
					+ user.getName() + " and placeholder " + placeholder);
			return CollectionUtils.emptySet();
		}
		Set<Action> filterActions = evaluator.filterActions(target, user, role.getFirst());
		boolean traceEnabled = LOGGER.isTraceEnabled();
		if (traceEnabled) {
			LOGGER.trace("Actions for [" + target.getIdentifier() + "/" + target.getId() + "] before filtering are "
					+ filterActions);
		}
		String type = roleEvaluatorManagerService.getEvaluatorType(target);
		if (placeholder != null) {
			eventService.fire(new ActionEvaluatedEvent(target, filterActions, placeholder),
					new FilterActionBinding(type, placeholder));
		}
		if (traceEnabled) {
			LOGGER.trace("Actions for [" + target.getIdentifier() + "/" + target.getId() + "] after filtering are "
					+ filterActions);
		}
		return filterActions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Role getUserRole(Instance instance, Resource user) {
		return getRoleEvaluatorForInstance(instance).evaluate(instance, user, null).getFirst();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActionAllowed(Instance instance, String operation, Resource user, String placeholder) {
		Set<Action> actions = filterActions(getRoleEvaluatorForInstance(instance), instance, user, placeholder);
		Action action = actionRegistry.find(buildActionKey(instance, operation));
		if (action == null) {
			LOGGER.warn("Action {} not registered for instance {}", operation, instance.getClass().getSimpleName());
			return false;
		}
		return actions.contains(action);
	}

	/**
	 * Builds the action key.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the pair
	 */
	private static String buildActionKey(Instance instance, String operation) {
		return operation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActionAllowed(Instance instance, String operation, String placeholder) {
		Set<Action> actions = filterActions(getRoleEvaluatorForInstance(instance),
				instance,
				getCurrentUser(),
				placeholder);
		Action action = actionRegistry.find(buildActionKey(instance, operation));
		if (action == null) {
			LOGGER.warn("Action {} not registered for instance {}", operation, instance.getClass().getSimpleName());
			return false;
		}
		return actions.contains(action);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Action> filterAllowedActions(Instance instance, String placeholder, String... actions) {
		if (actions == null || actions.length == 0) {
			return CollectionUtils.emptySet();
		}
		Set<Action> filtered = filterActions(getRoleEvaluatorForInstance(instance),
				instance,
				getCurrentUser(),
				placeholder);
		// no need to calculate operations if nothing is allowed on the first place
		if (filtered.isEmpty()) {
			return filtered;
		}
		Set<Action> toFilter = CollectionUtils.createLinkedHashSet(actions.length);
		for (int i = 0; i < actions.length; i++) {
			String actionId = actions[i];
			Action action = actionRegistry.find(buildActionKey(instance, actionId));
			if (action == null) {
				LOGGER.warn("Action {} not registered for instance {}", actionId, instance.getClass().getSimpleName());
				continue;
			}
			toFilter.add(action);
		}
		// leave only needed actions
		filtered.retainAll(toFilter);
		return filtered;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Action getAllowedAction(Instance instance, String actionId, String placeholder) {
		Set<Action> filteredAllowedActions = filterAllowedActions(instance, actionId);
		if (!filteredAllowedActions.isEmpty()) {
			return filteredAllowedActions.iterator().next();
		}
		return null;
	}

	@Override
	public boolean hasPermission(PermissionIdentifier permission, Instance target, Resource resource) {
		Role userRole = getUserRole(target, resource);
		return checkPermissionInternal(permission, userRole);
	}

	@Override
	public Map<Instance, Role> getUserRole(Collection<Instance> instances, Resource resource) {
		if (instances == null || instances.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Instance, Role> rolesAssigned = new LinkedHashMap<>(instances.size());
		RoleEvaluator<Instance> currentRootEvaluator = null;
		for (Instance target : instances) {
			if (currentRootEvaluator == null || !currentRootEvaluator.canHandle(target)) {
				currentRootEvaluator = getRoleEvaluatorForInstance(target);
			}
			Role roleToResource = currentRootEvaluator.evaluate(target, resource, null).getFirst();
			rolesAssigned.put(target, roleToResource);
		}
		return rolesAssigned;
	}

	@Override
	@SuppressWarnings("boxing")
	public Map<Instance, Boolean> hasPermission(PermissionIdentifier permission, Collection<Instance> instances,
			Resource resource) {

		if (instances == null || instances.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Instance, Boolean> permissionsAssigned = new LinkedHashMap<>(instances.size());
		RoleEvaluator<Instance> currentRootEvaluator = null;
		// TODO: add parallel evaluation of permissions
		for (Instance target : instances) {
			if (currentRootEvaluator == null || !currentRootEvaluator.canHandle(target)) {
				currentRootEvaluator = getRoleEvaluatorForInstance(target);
			}
			Pair<Role, RoleEvaluator<Instance>> evaluatedPermission = currentRootEvaluator.evaluate(target,
					resource,
					null);
			if (evaluatedPermission != null) {
				Role roleToResource = evaluatedPermission.getFirst();
				permissionsAssigned.put(target, checkPermissionInternal(permission, roleToResource));
			} else {
				LOGGER.error("Missing result for " + target + " evalauted through: " + currentRootEvaluator);
			}
		}
		return permissionsAssigned;
	}

	/**
	 * Evaluate has permission with the given role.
	 *
	 * @param permission
	 *            is the permission to check
	 * @param userRole
	 *            for an instance
	 * @return true if it has, false if unknown or dont have it
	 */
	private static boolean checkPermissionInternal(PermissionIdentifier permission, Role userRole) {
		if (userRole == null) {
			// don't return null
			return false;
		}
		if (SecurityModel.PERMISSION_READ.equals(permission)) {
			return userRole.getRoleId().canRead();
		} else if (SecurityModel.PERMISSION_EDIT.equals(permission)) {
			return userRole.getRoleId().canWrite();
		}
		return false;
	}

	@Override
	public boolean isAdminOrSystemUser(Resource user) {
		if (!(user instanceof User) || user.getName() == null) {
			return false;
		}
		String userName = user.getName().toLowerCase();
		boolean isAdminUser = securityConfiguration.getAdminUserName().isSet()
				&& (userName.equalsIgnoreCase(securityConfiguration.getAdminUserName().get())
						|| userName.equalsIgnoreCase(securityConfiguration.getSystemUser().get().getIdentityId()));
		if (isAdminUser) {
			return true;
		}
		if (securityConfiguration.getAdminGroup().isSet()) {
			Resource adminGroup = resourceService.getResource(securityConfiguration.getAdminGroup().get(),
					ResourceType.GROUP);
			List<Instance> containedResources = resourceService.getContainedResources(adminGroup, ResourceType.USER);
			// get the user, to be well known type
			Resource theUser = resourceService.getResource(user.getName(), ResourceType.USER);
			return containedResources.contains(theUser);
		}
		return isAdminUser;
	}

	@Override
	public boolean isAdminOrSystemUser() {
		return isAdminOrSystemUser(getCurrentUser());
	}

}
