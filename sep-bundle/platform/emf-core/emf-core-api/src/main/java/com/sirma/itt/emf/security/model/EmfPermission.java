package com.sirma.itt.emf.security.model;

import java.io.Serializable;


/**
 * Default implementation for {@link Permission} interface
 *
 * @author BBonev
 */
public class EmfPermission implements Permission, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7966151673964416799L;
	/** The permission id. */
	private final String permissionId;

	/**
	 * Instantiates a new cmf permission.
	 *
	 * @param permissionId
	 *            the permission id
	 */
	public EmfPermission(String permissionId) {
		this.permissionId = permissionId;
		if (permissionId == null) {
			throw new IllegalArgumentException("Permission Id cannot be null");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPermissionId() {
		return permissionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((permissionId == null) ? 0 : permissionId.hashCode());
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
		if (!(obj instanceof EmfPermission)) {
			return false;
		}
		EmfPermission other = (EmfPermission) obj;
		if (permissionId == null) {
			if (other.permissionId != null) {
				return false;
			}
		} else if (!permissionId.equals(other.permissionId)) {
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
		builder.append("CmfPermission [permissionId=");
		builder.append(permissionId);
		builder.append("]");
		return builder.toString();
	}

}
