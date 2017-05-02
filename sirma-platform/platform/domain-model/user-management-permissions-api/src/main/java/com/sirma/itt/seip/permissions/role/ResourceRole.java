package com.sirma.itt.seip.permissions.role;

import java.io.Serializable;

import com.sirma.itt.seip.Entity;

/**
 * Defines a role for a particular resource to a target instance. <br>
 * bbanchev TODO check equals, hashCode
 *
 * @author BBonev
 */
public class ResourceRole implements Entity<Long>, Serializable {

	private static final long serialVersionUID = -8827455611802521733L;

	private Long id;

	private RoleIdentifier role;

	private String authorityId;

	private String targetReference;

	private String inheritedFromReference;

	private RoleAssignments roleAssignments;

	private String systemInfo;

	@Override
	public String toString() {
		return "ResourceRole [id=" + id + ", role=" + role + ", authorityId=" + authorityId + ", targetReference="
				+ targetReference + ", inheritedFromReference=" + inheritedFromReference + ", roleAssignments="
				+ roleAssignments + ", systemInfo=" + systemInfo + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorityId == null) ? 0 : authorityId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((inheritedFromReference == null) ? 0 : inheritedFromReference.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((roleAssignments == null) ? 0 : roleAssignments.hashCode());
		result = prime * result + ((systemInfo == null) ? 0 : systemInfo.hashCode());
		result = prime * result + ((targetReference == null) ? 0 : targetReference.hashCode());
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
		ResourceRole other = (ResourceRole) obj;
		if (authorityId == null) {
			if (other.authorityId != null) {
				return false;
			}
		} else if (!authorityId.equals(other.authorityId)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (inheritedFromReference == null) {
			if (other.inheritedFromReference != null) {
				return false;
			}
		} else if (!inheritedFromReference.equals(other.inheritedFromReference)) {
			return false;
		}
		if (role == null) {
			if (other.role != null) {
				return false;
			}
		} else if (!role.equals(other.role)) {
			return false;
		}
		if (roleAssignments == null) {
			if (other.roleAssignments != null) {
				return false;
			}
		} else if (!roleAssignments.equals(other.roleAssignments)) {
			return false;
		}
		if (systemInfo == null) {
			if (other.systemInfo != null) {
				return false;
			}
		} else if (!systemInfo.equals(other.systemInfo)) {
			return false;
		}
		if (targetReference == null) {
			if (other.targetReference != null) {
				return false;
			}
		} else if (!targetReference.equals(other.targetReference)) {
			return false;
		}
		return true;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets the role.
	 *
	 * @return the role
	 */
	public RoleIdentifier getRole() {
		return role;
	}

	/**
	 * Gets the target role reference.
	 *
	 * @return the target role reference
	 */
	public String getTargetReference() {
		return targetReference;
	}

	/**
	 * Setter method for role.
	 *
	 * @param role
	 *            the role to set
	 */
	public void setRole(RoleIdentifier role) {
		this.role = role;
	}

	/**
	 * Setter method for targetReference.
	 *
	 * @param targetReference
	 *            the targetReference to set
	 */
	public void setTargetReference(String targetReference) {
		this.targetReference = targetReference;
	}

	/**
	 * Gets the inherited from reference.
	 *
	 * @return the inherited from reference
	 */
	public String getInheritedFromReference() {
		return inheritedFromReference;
	}

	/**
	 * Sets the inherited from reference.
	 *
	 * @param inheritedFromReference
	 *            the new inherited from reference
	 */
	public void setInheritedFromReference(String inheritedFromReference) {
		this.inheritedFromReference = inheritedFromReference;
	}

	/**
	 * Gets the system info.
	 *
	 * @return the system info
	 */
	public String getSystemInfo() {
		return systemInfo;
	}

	/**
	 * Sets the system info.
	 *
	 * @param systemInfo
	 *            the new system info
	 */
	public void setSystemInfo(String systemInfo) {
		this.systemInfo = systemInfo;
	}

	/**
	 * Getter method for roleAssignments.
	 *
	 * @return the roleAssignments
	 */
	public RoleAssignments getRoleAssignments() {
		return roleAssignments;
	}

	/**
	 * Setter method for roleAssignments.
	 *
	 * @param roleAssignments
	 *            the roleAssignments to set
	 */
	public void setRoleAssignments(RoleAssignments roleAssignments) {
		this.roleAssignments = roleAssignments;
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
	 * Setter method for authorityId.
	 *
	 * @param authorityId
	 *            the authorityId to set
	 */
	public void setAuthorityId(String authorityId) {
		this.authorityId = authorityId;
	}

}
