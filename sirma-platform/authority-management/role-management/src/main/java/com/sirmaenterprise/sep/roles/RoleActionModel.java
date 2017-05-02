package com.sirmaenterprise.sep.roles;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * DTO that represents the mappings between roles and the actions associated with them
 *
 * @since 2017-03-22
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class RoleActionModel {

	private Map<String, ActionDefinition> actions = new LinkedHashMap<>();
	private Map<String, RoleDefinition> roles = new LinkedHashMap<>();
	private List<RoleActionMapping> roleActionEntries = new LinkedList<>();

	/**
	 * Register action definition to the model
	 *
	 * @param action
	 *            an action to register
	 * @return current instance for chaining
	 */
	public RoleActionModel add(ActionDefinition action) {
		actions.put(action.getId(), action);
		return this;
	}

	/**
	 * Register role definition to the model
	 *
	 * @param role
	 *            a role to register
	 * @return current instance for chaining
	 */
	public RoleActionModel add(RoleDefinition role) {
		roles.put(role.getId(), role);
		return this;
	}

	/**
	 * Add new mapping entry. Note that this method uses already registered roles and actions to the model. If no such
	 * role is present then new entry will be added to roles and action models
	 *
	 * @param role
	 *            the role to map
	 * @param action
	 *            the action to map to the role
	 * @param enabled
	 *            if the mapping is enabled
	 * @param filters
	 *            optional filters to assign to the mapping
	 * @return current instance for chaining
	 */
	public RoleActionModel add(String role, String action, boolean enabled,
			Collection<String> filters) {
		Objects.requireNonNull(role, "Role defininion is required");
		Objects.requireNonNull(action, "Action defininion is required");

		roleActionEntries.add(new RoleActionMapping(
				roles.computeIfAbsent(role, id -> new RoleDefinition().setId(id).setUserDefined(true)),
				actions.computeIfAbsent(action, id -> new ActionDefinition().setId(id).setUserDefined(true)), enabled,
				filters));
		return this;
	}

	/**
	 * Gets a stream role action mappings that match the given role identifier
	 *
	 * @param roleId
	 *            the role identifier to filter by
	 * @return a stream of filtered mappings
	 */
	public Stream<RoleActionMapping> getActionsForRole(String roleId) {
		return roleActions().filter(entry -> nullSafeEquals(roleId, entry.role.getId()));
	}

	/**
	 * Gets all registered actions as stream
	 *
	 * @return all known by this model actions
	 */
	public Stream<ActionDefinition> actions() {
		return actions.values().stream();
	}

	/**
	 * Gets all registered roles as stream
	 *
	 * @return all known by this model roles
	 */
	public Stream<RoleDefinition> roles() {
		return roles.values().stream();
	}

	/**
	 * Gets a stream of all mappings
	 *
	 * @return stream of all mappings contained in the current instance
	 */
	public Stream<RoleActionMapping> roleActions() {
		return roleActionEntries.stream();
	}

	/**
	 * Represent a single role and action mapping
	 *
	 * @since 2017-03-27
	 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
	 */
	public class RoleActionMapping {
		final RoleDefinition role;
		private final ActionDefinition action;
		private final boolean enabled;
		private Collection<String> filters;

		RoleActionMapping(RoleDefinition role, ActionDefinition action, boolean enabled, Collection<String> filters) {
			this.role = role;
			this.action = action;
			this.enabled = enabled;
			this.filters = getOrDefault(filters, Collections.<String>emptySet());
		}

		public boolean isEnabled() {
			return enabled;
		}

		public ActionDefinition getAction() {
			return action;
		}

		public Collection<String> getFilters() {
			return filters;
		}

		public RoleDefinition getRole() {
			return role;
		}
	}
}
