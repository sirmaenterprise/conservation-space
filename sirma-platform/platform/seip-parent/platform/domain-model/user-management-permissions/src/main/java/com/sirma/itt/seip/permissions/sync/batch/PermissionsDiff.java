package com.sirma.itt.seip.permissions.sync.batch;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Entity used to carry the detected permission during permission synchronization
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 15/06/2017
 */
class PermissionsDiff {

	private final String targetId;
	private Set<PermissionsDiffEntry> toAdd = new HashSet<>();
	private Set<PermissionsDiffEntry> toRemove = new HashSet<>();
	private Set<String> removeParentInheritanceFrom;
	private String addParentInheritanceTo;
	private Set<String> removeLibraryInheritanceFrom;
	private String addLibraryInheritanceTo;

	/**
	 * Represents a mapping of created permission roles in the semantic for the current instance.
	 * <br>Map key is the role type (conc:SecurityRoleTypes-Read-Write) and value is the role instance id
	 * (emf:instance-id_SecurityRoleTypes-Read-Write)
	 */
	private Map<String, String> instanceRoles = createHashMap(5);

	/**
	 * Instantiate permissions different entity for the given affected instance
	 *
	 * @param targetId the affected instance id
	 */
	PermissionsDiff(String targetId) {
		this.targetId = targetId;
	}

	/**
	 * Add permission role change for the given authority
	 *
	 * @param authority the affected authority
	 * @param newRole the new role value or null if the role is removed
	 * @param oldRole the old role value or null if the role is added
	 * @return current instance for chaining
	 */
	PermissionsDiff addRoleChange(String authority, String newRole, String oldRole) {
		if (newRole != null) {
			toAdd.add(new PermissionsDiffEntry(authority, newRole));
		}
		if (oldRole != null) {
			toRemove.add(new PermissionsDiffEntry(authority, oldRole));
		}
		return this;
	}

	/**
	 * Set changes in the parent inheritance
	 *
	 * @param parentsToRemove the parent entries to remove, if empty then no parents will be removed
	 * @param parentToSet the parent to set, if empty no parent will be set
	 */
	void parentInheritanceChanged(Set<String> parentsToRemove, String parentToSet) {
		this.removeParentInheritanceFrom = parentsToRemove;
		this.addParentInheritanceTo = parentToSet;
	}

	/**
	 * Set changes in the library inheritance
	 *
	 * @param librariesToRemove the library entries to remove, if empty nothing will be removed as library inheritance
	 * @param libraryToSet the library inheritance to enable, if empty no librery inheritance will be added
	 */
	void libraryInheritanceChanged(Set<String> librariesToRemove, String libraryToSet) {
		this.removeLibraryInheritanceFrom = librariesToRemove;
		this.addLibraryInheritanceTo = libraryToSet;
	}

	/**
	 * Checks if there are cny changes in the parent inheritance
	 *
	 * @return true if there are changes
	 */
	boolean isParentInheritanceChanged() {
		return hasChanges(removeParentInheritanceFrom, addParentInheritanceTo);
	}

	/**
	 * Checks if there are cny changes in the library inheritance
	 *
	 * @return true if there are changes
	 */
	boolean isLibraryPermissionsChanged() {
		return hasChanges(removeLibraryInheritanceFrom, addLibraryInheritanceTo);
	}

	private static boolean hasChanges(Set<String> currentValues, String newValue) {
		return isNotEmpty(currentValues) || newValue != null;
	}

	Set<String> getParentInheritanceToRemove() {
		return removeParentInheritanceFrom;
	}

	String getParentInheritanceToAdd() {
		return addParentInheritanceTo;
	}

	Set<String> getLibraryInheritanceToRemove() {
		return removeLibraryInheritanceFrom;
	}

	String getLibraryInheritanceToAdd() {
		return addLibraryInheritanceTo;
	}

	/**
	 * The affected instance id
	 *
	 * @return the instance id
	 */
	String getTargetId() {
		return targetId;
	}

	Stream<PermissionsDiffEntry> getToAdd() {
		return toAdd.stream();
	}

	Stream<PermissionsDiffEntry> getToRemove() {
		return toRemove.stream();
	}

	/**
	 * Checks if there are any changes for processing in the item writer
	 *
	 * @return true if there are any changes
	 */
	boolean hasChanges() {
		return !toAdd.isEmpty() || !toRemove.isEmpty() || isLibraryPermissionsChanged() || isParentInheritanceChanged();
	}

	/**
	 * Set the current instance role in the semantic database, will be used as reference when writing the permission
	 * changes
	 *
	 * @param instanceRoles the roles to set
	 */
	void setInstanceRoles(Map<String, String> instanceRoles) {
		this.instanceRoles.putAll(instanceRoles);
	}

	Map<String, String> getInstanceRoles() {
		return instanceRoles;
	}

	/**
	 * Represents a single authority/role mapping change
	 *
	 * @author BBonev
	 */
	static class PermissionsDiffEntry {
		final String authority;
		final String roleType;

		PermissionsDiffEntry(String authority, String roleType) {
			this.authority = authority;
			this.roleType = roleType;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			PermissionsDiffEntry that = (PermissionsDiffEntry) o;
			return (authority != null ? authority.equals(that.authority) : that.authority == null) && roleType.equals(
					that.roleType);
		}

		@Override
		public int hashCode() {
			int result = +(authority != null ? authority.hashCode() : 0);
			result = 31 * result + roleType.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return new StringBuilder(90)
					.append("PermissionsDiffEntry{")
					.append("authority='").append(authority).append('\'')
					.append(", roleType='").append(roleType).append('\'')
					.append('}')
					.toString();
		}
	}
}
