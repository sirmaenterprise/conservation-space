package com.sirma.itt.seip.permissions.model;

import java.io.Serializable;

import com.sirma.itt.seip.permissions.PermissionIdentifier;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default implementation for {@link PermissionIdentifier} interface
 *
 * @author BBonev
 */
public class PermissionId implements PermissionIdentifier, Serializable {
	private static final long serialVersionUID = -7966151673964416799L;
	/** The permission id. */
	private String permissionId;

	/**
	 * Instantiates a new permission model.
	 *
	 * @param permissionId
	 *            the permission id
	 */
	public PermissionId(String permissionId) {
		this.permissionId = permissionId;
		if (permissionId == null) {
			throw new IllegalArgumentException("Permission Id cannot be null");
		}
	}

	@Override
	public String getPermissionId() {
		return permissionId;
	}

	protected void setPermissionId(String permissionId) {
		this.permissionId = permissionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (permissionId == null ? 0 : permissionId.hashCode());
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
		if (!(obj instanceof PermissionId)) {
			return false;
		}
		PermissionId other = (PermissionId) obj;
		return EqualsHelper.nullSafeEquals(permissionId, other.permissionId, true);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Permission [");
		builder.append(permissionId);
		builder.append("]");
		return builder.toString();
	}

}
