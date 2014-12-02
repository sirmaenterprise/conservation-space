package com.sirma.itt.emf.security.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Default user role implementations. The class stores the actions and permissions in immutable
 * collections
 *
 * @author BBonev
 */
public class RoleImpl implements Role, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1889725692968322608L;

	/** The role id. */
	private final RoleIdentifier roleId;

	/** The actions. */
	private Map<Class<?>, Set<Action>> actions;

	/** The permissions. */
	private Map<Permission, List<Pair<Class<?>, Action>>> permissions;

	/** The sealed. */
	private boolean sealed = false;

	/**
	 * Instantiates a new role.
	 *
	 * @param roleId
	 *            the role id
	 * @param permissions
	 *            the permissions
	 */
	public RoleImpl(RoleIdentifier roleId, Map<Permission, List<Pair<Class<?>, Action>>> permissions) {
		this.roleId = roleId;
		this.permissions = (permissions == null) ? new HashMap<Permission, List<Pair<Class<?>, Action>>>()
				: permissions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <A extends Action> Set<A> getAllAllowedActions() {
		return getAllowedActions(Object.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <A extends Action> Set<A> getAllowedActions(Class<?> targetClass) {
		if (targetClass == null) {
			if (sealed) {
				return Collections.emptySet();
			} else {
				Set<Action> set = new LinkedHashSet<Action>();
				getActions().put(Object.class, set);
				return (Set<A>) set;
			}
		}
		Set<Action> set = getActions().get(targetClass);
		if (set == null) {
			if (sealed) {
				return Collections.emptySet();
			} else {
				set = new LinkedHashSet<Action>();
				getActions().put(targetClass, set);
			}
		}
		return (Set<A>) set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <A extends Action, P extends Permission> void addPermission(P permission,
			List<Pair<Class<?>, Action>> actions) {
		if (sealed) {
			return;
		}

		for (Pair<Class<?>, Action> a : actions) {
			CollectionUtils.addValueToMap(permissions, permission, a);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Permission, List<Pair<Class<?>, Action>>> getPermissions() {
		return permissions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RoleIdentifier getRoleId() {
		return roleId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((roleId == null) ? 0 : roleId.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RoleImpl)) {
			return false;
		}
		RoleImpl other = (RoleImpl) obj;
		if (roleId == null) {
			if (other.roleId != null) {
				return false;
			}
		} else if (!roleId.equals(other.roleId)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Role [roleId=");
		builder.append(roleId);
		builder.append(", mutable=");
		builder.append(sealed);
		builder.append("").append("\n");

		Set<Entry<Permission, List<Pair<Class<?>, Action>>>> entrySet = permissions.entrySet();
		for (Entry<Permission, List<Pair<Class<?>, Action>>> entry : entrySet) {
			builder.append("\t").append(entry.getKey()).append("\n");
			List<Pair<Class<?>, Action>> value = entry.getValue();
			for (Pair<Class<?>, Action> pair : value) {
				builder.append("\t\t")
						.append(pair.getFirst())
						.append(" = ")
						.append(pair.getSecond() != null ? pair.getSecond().getActionId()
								: "NULL!!!").append("\n");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSealed() {
		return sealed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void seal() {
		if (!sealed) {
			sealed = true;
			for (Class<?> key : getActions().keySet()) {
				getActions().put(key, Collections.unmodifiableSet(getActions().get(key)));
			}
			actions = Collections.unmodifiableMap(getActions());
			for (Permission permission : permissions.keySet()) {
				permissions.put(permission,
						Collections.unmodifiableList(permissions.get(permission)));
			}
			permissions = Collections.unmodifiableMap(permissions);
		}
	}

	/**
	 * Getter method for actions.
	 *
	 * @return the actions
	 */
	private Map<Class<?>, Set<Action>> getActions() {
		if (actions == null) {
			Map<Class<?>, Set<Action>> mapping = new LinkedHashMap<Class<?>, Set<Action>>(50);
			for (Entry<Permission, List<Pair<Class<?>, Action>>> entry : this.permissions
					.entrySet()) {
				for (Pair<Class<?>, Action> pair : entry.getValue()) {
					CollectionUtils.addValueToSetMap(mapping, pair.getFirst(), pair.getSecond());
					CollectionUtils.addValueToSetMap(mapping, Object.class, pair.getSecond());
				}
			}
			actions = mapping;
		}
		return actions;
	}

}
