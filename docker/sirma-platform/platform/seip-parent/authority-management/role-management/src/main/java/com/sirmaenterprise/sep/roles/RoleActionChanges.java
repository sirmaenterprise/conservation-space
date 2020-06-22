package com.sirmaenterprise.sep.roles;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represent a list of changes to role actions mapping. The data provided with the given changes will override the
 * current data for the same mapping. This includes if the mapping is enabled or nor and the applied filters. If no
 * filters are specified then the filters will be removed if exists
 *
 * @author BBonev
 */
public class RoleActionChanges {

	private List<RoleActionChange> changes = new ArrayList<>();

	/**
	 * Add the given change to the list of changes.
	 *
	 * @param change
	 *            the change to add
	 * @return the current instance to allow chaining
	 */
	public RoleActionChanges add(RoleActionChange change) {
		Objects.requireNonNull(change, "Cannot add null change");
		Objects.requireNonNull(change.getAction(), "Role id is required");
		Objects.requireNonNull(change.getRole(), "Action id is required");
		changes.add(change);
		return this;
	}

	/**
	 * Enable the given role action mapping so that it will allow users to execute that particular action if all other
	 * conditions are met
	 *
	 * @param role
	 *            the role id
	 * @param action
	 *            the action id
	 * @param filters
	 *            the filters changed value
	 * @return the current instance to allow chaining
	 */
	public RoleActionChanges enable(String role, String action, Collection<String> filters) {
		return add(new RoleActionChange(role, action, true, filters));
	}

	/**
	 * Disable the given role action mapping so that it the given action will stop to appear for the users even all
	 * other conditions are met
	 *
	 * @param role
	 *            the role id
	 * @param action
	 *            the action id
	 * @param filters
	 *            the filters changed value
	 * @return the current instance to allow chaining
	 */
	public RoleActionChanges disable(String role, String action, Collection<String> filters) {
		return add(new RoleActionChange(role, action, false, filters));
	}

	/**
	 * Get all changes
	 *
	 * @return the list of all added changes
	 */
	public Collection<RoleActionChange> getChanges() {
		return changes;
	}

	/**
	 * Represents a single role action change. Any data provided here will override the data in the database
	 *
	 * @author BBonev
	 */
	public static class RoleActionChange {
		private final String role;
		private final String action;
		private final boolean active;
		private final Set<String> filters;

		/**
		 * Instantiate new change instance.
		 *
		 * @param role
		 *            to assigned role
		 * @param action
		 *            the assigned action
		 * @param active
		 *            if the mapping role/action is active or not
		 * @param filters
		 *            any assigned filters to the mapping. Allowed {@code null} value
		 */
		public RoleActionChange(String role, String action, boolean active, Collection<String> filters) {
			this.role = role;
			this.action = action;
			this.active = active;
			this.filters = new HashSet<>(getOrDefault(filters, Collections.emptyList()));
		}

		public String getRole() {
			return role;
		}

		public String getAction() {
			return action;
		}

		public boolean isActive() {
			return active;
		}

		public Set<String> getFilters() {
			return filters;
		}
	}
}
