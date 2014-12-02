/**
 *
 */
package com.sirma.itt.cmf.security.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * Basic implementation for {@link RoleProviderExtension} holding some common code.
 * 
 * @author bbanchev
 */
public abstract class AbstractRoleProviderExtension implements RoleProviderExtension, SecurityModel {

	/**
	 * Merger roles. The second role is enriched with roles from the first argument
	 * 
	 * @param source
	 *            the source role
	 * @param destination
	 *            the destination to merge to
	 */
	protected void mergerRoles(Role source, Role destination) {
		for (Entry<Permission, List<Pair<Class<?>, Action>>> permissionEntry : source
				.getPermissions().entrySet()) {
			List<Pair<Class<?>, Action>> actions = destination.getPermissions().get(
					permissionEntry.getKey());
			if (actions == null) {
				destination.getPermissions().put(permissionEntry.getKey(),
						new ArrayList<Pair<Class<?>, Action>>(permissionEntry.getValue()));
			} else {
				actions.addAll(permissionEntry.getValue());
			}
		}
	}

	/**
	 * Gets the model for some {@link RoleIdentifier} it exists in the current permission model<br>
	 * <strong>This is not a snapshot but actual model!</strong>
	 * 
	 * @param permissionModel
	 *            is the model with all roles
	 * @param roleId
	 *            is the role to get
	 * @return the found role model or new map if the permission model has no such key or associated
	 *         value
	 */
	protected Map<Permission, List<Pair<Class<?>, Action>>> getPermissions(
			Map<RoleIdentifier, Role> permissionModel, RoleIdentifier roleId) {
		Role roleImpl = permissionModel.get(roleId);
		if (roleImpl != null) {
			Map<Permission, List<Pair<Class<?>, Action>>> permissionsCurrent = roleImpl
					.getPermissions();
			return permissionsCurrent;
		}
		return new HashMap<Permission, List<Pair<Class<?>, Action>>>();
	}

	/**
	 * Adds a permission with its action the a model holding the permission for some role.
	 * 
	 * @param permissions
	 *            the permissions is the current state model to be updated
	 * @param permission
	 *            the permission to set
	 * @param actions
	 *            the actions for this permission created by
	 *            {@link com.sirma.itt.emf.security.SecurityUtil#createActionsList(Class, com.sirma.itt.emf.provider.ProviderRegistry, String...)}
	 */
	protected void addPermission(Map<Permission, List<Pair<Class<?>, Action>>> permissions,
			Permission permission, List<Pair<Class<?>, Action>> actions) {
		if (permissions.containsKey(permission)) {
			permissions.get(permission).addAll(actions);
		} else {
			permissions.put(permission, actions);
		}
	}

	/**
	 * Create a new role based on existing one.
	 * 
	 * @param chain
	 *            is the current model data
	 * @param roleId
	 *            is the role to base on
	 * @return the permission model as copy for the role id
	 */
	protected Map<Permission, List<Pair<Class<?>, Action>>> initBasedOn(
			Map<RoleIdentifier, Role> chain, RoleIdentifier roleId) {

		Map<Permission, List<Pair<Class<?>, Action>>> basedOn = getPermissions(chain, roleId);
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>(
				basedOn.size());
		Set<Entry<Permission, List<Pair<Class<?>, Action>>>> basedOnSet = basedOn.entrySet();
		for (Entry<Permission, List<Pair<Class<?>, Action>>> entry : basedOnSet) {
			addPermission(permissions, entry.getKey(),
					new ArrayList<Pair<Class<?>, Action>>(entry.getValue()));
		}
		return permissions;
	}
}
