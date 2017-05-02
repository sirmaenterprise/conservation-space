package com.sirma.itt.seip.permissions.role;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.Filterable;
import com.sirma.itt.seip.provider.ProviderRegistry;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Defines the methods for a single user role. The class stores the actions and permissions in immutable collections.
 *
 * @author BBonev
 */
public class Role implements Sealable, Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final long serialVersionUID = -2783481764127201504L;

	private final RoleIdentifier roleId;

	private Set<Action> actions = new LinkedHashSet<>();

	private boolean sealed = false;

	/**
	 * Instantiates a new role.
	 *
	 * @param roleId
	 *            the role id
	 */
	public Role(RoleIdentifier roleId) {
		this.roleId = roleId;
	}

	/**
	 * Instantiates a new role.
	 *
	 * @param roleId
	 *            the role id
	 * @param actionsToSet
	 *            the actions to assign to the role
	 */
	public Role(RoleIdentifier roleId, Set<Action> actionsToSet) {
		this.roleId = roleId;
		addActions(actionsToSet);
	}

	/**
	 * Gets the allowed actions for the current role.<br>
	 * <b>Duplicate action for different target might be omitted</b>
	 *
	 * @param <A>
	 *            the generic type
	 * @return the allowed actions
	 */
	public <A extends Action> Set<A> getAllAllowedActions() {
		return getAllowedActions(null);
	}

	/**
	 * Gets the allowed actions for the current role.
	 *
	 * @param <A>
	 *            the generic type
	 * @param context
	 *            is the current evaluation context
	 * @return the allowed actions
	 */
	@SuppressWarnings("unchecked")
	public <A extends Action> Set<A> getAllowedActions(RoleActionEvaluatorContext context) {
		Set<Action> roleActions = getActions();
		if (roleActions == null) {
			return Collections.emptySet();
		}
		if (context == null) {
			return (Set<A>) roleActions;
		}
		return (Set<A>) context.filter(roleActions);
	}

	/**
	 * Adds the actions and the permission to the current role. If such permission already present then the actions will
	 * be merged
	 *
	 * @param actionsParam
	 *            the actions to add
	 */
	public void addActions(Collection<Action> actionsParam) {
		if (sealed || isEmpty(actionsParam)) {
			return;
		}
		getActions().addAll(actionsParam);
	}

	/**
	 * Creates a set of actions provided by their ids and retrieved the from the ProviderRegistry&lt;String, Action&gt;.
	 *
	 * @param actionRegistry
	 *            the action registry
	 * @param actionIds
	 *            the action ids
	 * @return linked hash set of the provided actions
	 */
	public static Set<Action> createActionsSet(ProviderRegistry<String, Action> actionRegistry, String... actionIds) {
		Set<Action> resultActions = newActionModel();
		for (String actionId : actionIds) {
			Action action = actionRegistry.find(actionId);
			if (action != null) {
				resultActions.add(action);
			} else {
				LOGGER.warn("Action not found: " + actionId);
			}
		}
		return resultActions;
	}

	/**
	 * Merge the role located in the mapping identified by the given identifier with the passed role.
	 *
	 * @param roles
	 *            the roles store to update
	 * @param roleIdToUpdate
	 *            the role id to update
	 * @param source
	 *            the source role to use
	 */
	public static void mergeRoles(Map<RoleIdentifier, Role> roles, RoleIdentifier roleIdToUpdate, Role source) {
		Role destintion = roles.get(roleIdToUpdate);
		if (destintion == null) {
			roles.put(roleIdToUpdate, source);
		} else {
			mergerRoles(source, destintion);
		}
	}

	/**
	 * Merger roles. The second role is enriched with roles from the first argument
	 *
	 * @param source
	 *            the source role
	 * @param destination
	 *            the destination to merge to
	 */
	public static void mergerRoles(Role source, Role destination) {
		Set<Action> newActions = source.getAllAllowedActions();
		boolean sameRole = source.getRoleId() == destination.getRoleId();
		if (!sameRole) {
			newActions = filterNonLocalActions(newActions);
		}
		destination.getActions().addAll(newActions);
	}

	/**
	 * Filter non local actions that should not be used in all roles
	 *
	 * @param value
	 *            the actions
	 * @return the set of non local actions
	 */
	public static Set<Action> filterNonLocalActions(Set<Action> value) {
		Set<Action> result = new LinkedHashSet<>();
		for (Action action : value) {
			if (!action.isLocal()) {
				result.add(action);
			}
		}
		return result;
	}

	/**
	 * Merge two roles internal. If the destination is missing is simple put at the end of merging
	 *
	 * @param chained
	 *            the current state permission model
	 * @param roleIdfrom
	 *            the role to get info from
	 * @param roleIdTo
	 *            the role id to add permissions to. if empty it is created
	 */
	public static void merge(Map<RoleIdentifier, Role> chained, RoleIdentifier roleIdfrom, RoleIdentifier roleIdTo) {
		Role roleFrom = chained.get(roleIdfrom);
		if (roleFrom == null) {
			return;
		}

		Role roleTo = chained.get(roleIdTo);
		if (roleTo == null) {
			roleTo = new Role(roleIdTo, null);
			chained.put(roleIdTo, roleTo);
		}

		mergerRoles(roleFrom, roleTo);
	}

	/**
	 * Gets the role id.
	 *
	 * @return the role id
	 */
	public RoleIdentifier getRoleId() {
		return roleId;
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void seal() {
		if (!sealed) {
			sealed = true;
			actions = Collections.unmodifiableSet(getActions());
		}
		LOGGER.debug("Role intialization completed! Value {}", toString());
	}

	/**
	 * Getter method for actions.
	 *
	 * @return the actions associated with this role
	 */
	private Set<Action> getActions() {
		return actions;
	}

	/**
	 * Creates the new internal role model for actions.
	 *
	 * @return the initial set
	 */
	public static Set<Action> newActionModel() {
		return new LinkedHashSet<>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (roleId == null ? 0 : roleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Role)) {
			return false;
		}

		Role other = (Role) obj;
		return EqualsHelper.nullSafeEquals(roleId, other.roleId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Role [roleId=");
		builder.append(roleId);
		builder.append(", mutable=");
		builder.append(sealed);

		for (Action action : getActions()) {
			List<String> filters = null;
			if (action instanceof Filterable) {
				filters = ((Filterable) action).getFilters();
			}
			builder
					.append("\t")
						.append(action != null ? action.getActionId() : "NULL!!!")
						.append(" ")
						.append(isEmpty(filters) ? "" : filters)
						.append("\n");
		}
		builder.append("]");
		return builder.toString();
	}
}
