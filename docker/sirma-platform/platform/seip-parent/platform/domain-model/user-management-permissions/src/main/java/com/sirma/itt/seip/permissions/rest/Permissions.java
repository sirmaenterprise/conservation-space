package com.sirma.itt.seip.permissions.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;

/**
 * Represents all assigned permissions to an instance.
 *
 * @author BBonev
 */
class Permissions implements Iterable<PermissionEntry> {

	private Map<Serializable, PermissionEntry> entries = new HashMap<>();
	private InstanceReference reference;
	private boolean inheritedPermissions;
	private boolean inheritedLibraryPermissions;
	private boolean isRoot;
	private boolean editAllowed;
	private boolean restoreAllowed;
	private boolean allowInheritParentPermissions;
	private boolean allowInheritLibraryPermissions;

	/**
	 * Gets the for authority.
	 *
	 * @param authorityId
	 *            the authority id
	 * @return the for authority
	 */
	public PermissionEntry getForAuthority(Serializable authorityId) {
		return entries.computeIfAbsent(authorityId, key -> new PermissionEntry());
	}

	/**
	 * @param roles
	 */
	public void addRolesWithSpecialPermissions(Collection<ResourceRole> roles) {
		addRoles(roles, PermissionEntry::setSpecial);
	}

	/**
	 * @param roles
	 */
	public void addRolesWithInteritedPermissions(Collection<ResourceRole> roles) {
		addRoles(roles, PermissionEntry::setInherited);
	}

	/**
	 * @param roles
	 */
	public void addRolesWithLibraryPermissions(Collection<ResourceRole> roles) {
		addRoles(roles, PermissionEntry::setLibrary);
	}

	/**
	 * @param roles
	 */
	public void addRolesWithCalculatedPermissions(Collection<ResourceRole> roles) {
		addRoles(roles, PermissionEntry::setCalculated);
	}

	/**
	 * @param roles
	 */
	public void addRolesWithManagerPermissions(Collection<ResourceRole> roles) {
		addRoles(roles, PermissionEntry::setManager);
	}

	private void addRoles(Collection<ResourceRole> assignments, BiConsumer<PermissionEntry, String> setter) {
		for (ResourceRole assignment : assignments) {
			String authorityId = assignment.getAuthorityId();
			if (authorityId == null) {
				continue;
			}

			PermissionEntry permissionEntry = getForAuthority(authorityId);
			setter.accept(permissionEntry, assignment.getRole().getIdentifier());

			permissionEntry.setAuthority(authorityId);
		}
	}

	/**
	 * Gets authority to special role mapping
	 *
	 * @param roleProvider
	 *            the role provider that can convert a role name to {@link RoleIdentifier}
	 * @return the role mapping
	 */
	public Map<String, RoleIdentifier> getRoleMapping(Function<String, RoleIdentifier> roleProvider) {
		Map<String, RoleIdentifier> result = new HashMap<>();
		for (Entry<Serializable, PermissionEntry> entry : entries.entrySet()) {
			addNonNullValue(result, entry.getKey().toString(), roleProvider.apply(entry.getValue().getSpecial()));
		}
		return result;
	}

	/**
	 * Returns the reference of the instance that has the current permissions
	 *
	 * @return the reference
	 */
	public InstanceReference getReference() {
		return reference;
	}

	/**
	 * Sets the reference of the instance that has the assigned permissions.
	 *
	 * @param reference
	 *            the reference to set
	 */
	public void setReference(InstanceReference reference) {
		this.reference = reference;
	}

	/**
	 * @return the inheritedPermissions
	 */
	public boolean isInheritedPermissions() {
		return inheritedPermissions;
	}

	/**
	 * @param inheritedPermissions
	 *            the inheritedPermissions to set
	 */
	public void setInheritedPermissions(boolean inheritedPermissions) {
		this.inheritedPermissions = inheritedPermissions;
	}

	/**
	 * Getter method for inheritedLibraryPermissions.
	 *
	 * @return the inheritedLibraryPermissions
	 */
	public boolean isInheritedLibraryPermissions() {
		return inheritedLibraryPermissions;
	}

	/**
	 * Setter method for inheritedLibraryPermissions.
	 *
	 * @param inheritedLibraryPermissions
	 *            the inheritedLibraryPermissions to set
	 */
	public void setInheritedLibraryPermissions(boolean inheritedLibraryPermissions) {
		this.inheritedLibraryPermissions = inheritedLibraryPermissions;
	}

	/**
	 * @return the isRoot
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * @param isRoot
	 *            the isRoot to set
	 */
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * @return the editAllowed
	 */
	public boolean isEditAllowed() {
		return editAllowed;
	}

	/**
	 * @param editAllowed
	 *            the editAllowed to set
	 */
	public void setEditAllowed(boolean editAllowed) {
		this.editAllowed = editAllowed;
	}

	/**
	 * @return the restoreAllowed
	 */
	public boolean isRestoreAllowed() {
		return restoreAllowed;
	}

	/**
	 * @param restoreAllowed
	 *            the restoreAllowed to set
	 */
	public void setRestoreAllowed(boolean restoreAllowed) {
		this.restoreAllowed = restoreAllowed;
	}

	/**
	 * Getter method for allowInheritParentPermissions.
	 *
	 * @return the allowInheritParentPermissions
	 */
	public boolean isAllowInheritParentPermissions() {
		return allowInheritParentPermissions;
	}

	/**
	 * Setter method for allowInheritParentPermissions.
	 *
	 * @param allowInheritParentPermissions
	 *            the allowInheritParentPermissions to set
	 */
	public void setAllowInheritParentPermissions(boolean allowInheritParentPermissions) {
		this.allowInheritParentPermissions = allowInheritParentPermissions;
	}

	/**
	 * Getter method for allowInheritLibraryPermissions.
	 *
	 * @return the allowInheritLibraryPermissions
	 */
	public boolean isAllowInheritLibraryPermissions() {
		return allowInheritLibraryPermissions;
	}

	/**
	 * Setter method for allowInheritLibraryPermissions.
	 *
	 * @param allowInheritLibraryPermissions
	 *            the allowInheritLibraryPermissions to set
	 */
	public void setAllowInheritLibraryPermissions(boolean allowInheritLibraryPermissions) {
		this.allowInheritLibraryPermissions = allowInheritLibraryPermissions;
	}

	@Override
	public Iterator<PermissionEntry> iterator() {
		return entries.values().iterator();
	}

}
