package com.sirma.itt.seip.permissions;

/**
 * The PermissionChangeSet class holds information about single authority/inheritance role change. <br>
 * <ul>
 * <li>If change is in inheritance - set or removed it the authorityId is null and the roleIdBefore and roleIdAfter
 * <li>If change in role for authorityId - if roleIdAfter is null the role is removed; if roleIdBefore is null it just
 * added; if both !=null change in model or reset
 * </ul>
 * <br>
 * The class is equals/hashCode implemented by the resourcerole entity db id
 */
public class PermissionAssignmentChange {

	private final String authorityId;
	private final String roleIdBefore;
	private final String roleIdAfter;

	/**
	 * Instantiates a new permission change set.
	 *
	 * @param authorityId
	 *            the authority id - the user/group change is related to
	 * @param roleIdBefore
	 *            the role id before - the role before set
	 * @param roleIdAfter
	 *            the role id after - the role after set
	 */
	public PermissionAssignmentChange(String authorityId, String roleIdBefore, String roleIdAfter) {
		this.authorityId = authorityId;
		this.roleIdBefore = roleIdBefore;
		this.roleIdAfter = roleIdAfter;
	}

	/**
	 * Checks is if the change introduces a new assignment.
	 *
	 * @return true if introduces, false otherwise.
	 */
	public boolean isNewAssignmentChange() {
		return roleIdBefore == null && roleIdAfter != null;
	}

	/**
	 * Checks is if the change removes an assignment.
	 *
	 * @return true if removes, false otherwise.
	 */
	public boolean isRemoveAssignmentChange() {
		return roleIdBefore != null && roleIdAfter == null;
	}

	/**
	 * Getter method for authorityId.
	 *
	 * @return the authorityId
	 */
	public String getAuthorityId() {
		return authorityId;
	}

	/**
	 * Getter method for roleIdBefore.
	 *
	 * @return the roleIdBefore
	 */
	public String getRoleIdBefore() {
		return roleIdBefore;
	}

	/**
	 * Getter method for roleIdAfter.
	 *
	 * @return the roleIdAfter
	 */
	public String getRoleIdAfter() {
		return roleIdAfter;
	}

	@Override
	public String toString() {
		return "PermissionAssignmentChange [authorityId=" + authorityId + ", roleIdBefore=" + roleIdBefore
				+ ", roleIdAfter=" + roleIdAfter + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorityId == null) ? 0 : authorityId.hashCode());
		result = prime * result + ((roleIdAfter == null) ? 0 : roleIdAfter.hashCode());
		result = prime * result + ((roleIdBefore == null) ? 0 : roleIdBefore.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PermissionAssignmentChange other = (PermissionAssignmentChange) obj;
		if (authorityId == null) {
			if (other.authorityId != null) {
				return false;
			}
		} else if (!authorityId.equals(other.authorityId)) {
			return false;
		}
		if (roleIdAfter == null) {
			if (other.roleIdAfter != null) {
				return false;
			}
		} else if (!roleIdAfter.equals(other.roleIdAfter)) {
			return false;
		}
		if (roleIdBefore == null) {
			if (other.roleIdBefore != null) {
				return false;
			}
		} else if (!roleIdBefore.equals(other.roleIdBefore)) {
			return false;
		}
		return true;
	}

}
