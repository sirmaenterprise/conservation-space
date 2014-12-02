package com.sirma.itt.emf.resources.model;

import java.io.Serializable;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * Defines a role for a particular resource to a target instance.
 *
 * @author BBonev
 */
public class ResourceRole implements Entity<Long>, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8827455611802521733L;

	/** The id. */
	private Long id;

	/** The role. */
	private RoleIdentifier role;

	/** The resource. */
	private Resource resource;

	/** The target role reference. */
	private InstanceReference targetRoleReference;

	/**
	* {@inheritDoc}
	*/
	@Override
	public Long getId() {
		return id;
	}

	/**
	* {@inheritDoc}
	*/
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
	public InstanceReference getTargetRoleReference() {
		return targetRoleReference;
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
	 * Setter method for targetRoleReference.
	 *
	 * @param targetRoleReference
	 *            the targetRoleReference to set
	 */
	public void setTargetRoleReference(InstanceReference targetRoleReference) {
		this.targetRoleReference = targetRoleReference;
	}

	/**
	 * Getter method for resource.
	 *
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Setter method for resource.
	 *
	 * @param resource
	 *            the resource to set
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
		result = (prime * result) + ((role == null) ? 0 : role.hashCode());
		result = (prime * result)
				+ ((targetRoleReference == null) ? 0 : targetRoleReference.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResourceRole other = (ResourceRole) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (resource == null) {
			if (other.resource != null) {
				return false;
			}
		} else if (!resource.equals(other.resource)) {
			return false;
		}
		if (role == null) {
			if (other.role != null) {
				return false;
			}
		} else if (!role.equals(other.role)) {
			return false;
		}
		if (targetRoleReference == null) {
			if (other.targetRoleReference != null) {
				return false;
			}
		} else if (!targetRoleReference.equals(other.targetRoleReference)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ResourceRole [id=" + id + ", role=" + role + ", resource=" + resource
				+ ", targetRoleReference=" + targetRoleReference + "]";
	}
}
