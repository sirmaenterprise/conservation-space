package com.sirma.itt.seip.permissions.role;

import com.sirma.itt.seip.permissions.PermissionModelType;

/**
 * Contains the assigned roles matched to their source (the way they are assigned). Provides a logic to compare the
 * currently active permissions based on the priority of the permission sources (special, inherited from parent,
 * library, etc.).
 *
 * @author Adrian Mitev
 */
public class RoleAssignments {

	private String special;
	private String inherited;
	private String library;
	private String active;

	private final String managerRole;

	/**
	 * Initializes the object.
	 * 
	 * @param managerRoleId
	 *            identifier of the manager role. It's used internally for performing business logic.
	 */
	public RoleAssignments(final String managerRoleId) {
		this.managerRole = managerRoleId;
	}

	/**
	 * Adds an assignment based on the permission model type.
	 * 
	 * @param role
	 *            role to assign.
	 * @param assignmentType
	 *            the way (source) the permissions are assigned.
	 */
	public void addAssignment(String role, PermissionModelType assignmentType) {
		if (assignmentType.isSpecial()) {
			special = role;
		} else if (assignmentType.isInherited()) {
			inherited = role;
		} else if (assignmentType.isLibrary()) {
			library = role;
		}

		calculateActivePermissions();
	}

	/**
	 * Calculates the currently active permissions based on the following algorithm rules. <br/>
	 * Manager role > Special role > Inherited role > Library role. <br/>
	 * The manager role may come from any place - special, inherited, library etc but it has the highest priority.
	 */
	private void calculateActivePermissions() {
		active = null;

		if (managerRole.equals(special) || managerRole.equals(inherited) || managerRole.equals(library)) {
			active = managerRole;
		} else if (special != null) {
			active = special;
		} else if (inherited != null) {
			active = inherited;
		} else if (library != null) {
			active = library;
		}
	}

	@Override
	public String toString() {
		return "RoleAssignments [special=" + special + ", inherited=" + inherited + ", library=" + library + ", active="
				+ active + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result + ((inherited == null) ? 0 : inherited.hashCode());
		result = prime * result + ((library == null) ? 0 : library.hashCode());
		result = prime * result + ((special == null) ? 0 : special.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoleAssignments other = (RoleAssignments) obj;
		if (active == null) {
			if (other.active != null)
				return false;
		} else if (!active.equals(other.active))
			return false;
		if (inherited == null) {
			if (other.inherited != null)
				return false;
		} else if (!inherited.equals(other.inherited))
			return false;
		if (library == null) {
			if (other.library != null)
				return false;
		} else if (!library.equals(other.library))
			return false;
		if (special == null) {
			if (other.special != null)
				return false;
		} else if (!special.equals(other.special))
			return false;
		return true;
	}

	/**
	 * Getter method for isManager.
	 *
	 * @return the isManager
	 */
	public boolean isManager() {
		return managerRole.equals(active);
	}

	/**
	 * Getter method for special.
	 *
	 * @return the special
	 */
	public String getSpecial() {
		return special;
	}

	/**
	 * Getter method for inherited.
	 *
	 * @return the inherited
	 */
	public String getInherited() {
		return inherited;
	}

	/**
	 * Getter method for library.
	 *
	 * @return the library
	 */
	public String getLibrary() {
		return library;
	}

	/**
	 * Getter method for active.
	 *
	 * @return the active
	 */
	public String getActive() {
		return active;
	}

}
