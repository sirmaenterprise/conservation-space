package com.sirma.itt.emf.semantic.security;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

import java.util.Objects;

/**
 * Represents a change triggered from the automatic permission assignment functionality
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 26/04/2017
 */
class AutoPermissionAssignmentChangeRequest extends PermissionChangeRequest {

	@TaggedFieldSerializer.Tag(2)
	private String roleToAssign;
	@TaggedFieldSerializer.Tag(4)
	private boolean allowOverride;
	@TaggedFieldSerializer.Tag(5)
	private String parentRoleToAssign;

	/**
	 * Instantiates a new permission change request.
	 */
	AutoPermissionAssignmentChangeRequest() {
		// needed by Kryo to instantiate the instance
	}

	/**
	 * Instantiates a new permission change request.
	 *
	 * @param targetInstance the target instance
	 * @param roleToAssign the role to assign
	 * @param targetResource the target resource
	 * @param allowOverride the allow override
	 * @param parentRoleToAssign the parent role to assign
	 */
	AutoPermissionAssignmentChangeRequest(String targetInstance, String roleToAssign, String targetResource,
			boolean allowOverride, String parentRoleToAssign) {
		super(targetInstance, targetResource);
		this.roleToAssign = roleToAssign;
		this.allowOverride = allowOverride;
		this.parentRoleToAssign = parentRoleToAssign;
	}

	public String getRoleToAssign() {
		return roleToAssign;
	}

	public boolean isAllowOverride() {
		return allowOverride;
	}

	public String getParentRoleToAssign() {
		return parentRoleToAssign;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AutoPermissionAssignmentChangeRequest)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		AutoPermissionAssignmentChangeRequest that = (AutoPermissionAssignmentChangeRequest) o;
		return allowOverride == that.allowOverride
				&& Objects.equals(parentRoleToAssign, that.parentRoleToAssign)
				&& Objects.equals(roleToAssign, that.roleToAssign);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (roleToAssign != null ? roleToAssign.hashCode() : 0);
		result = 31 * result + (allowOverride ? 1231 : 1237);
		result = 31 * result + (parentRoleToAssign != null ? parentRoleToAssign.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("AutoPermissionAssignmentChangeRequest{");
		sb.append("roleToAssign='").append(roleToAssign).append('\'');
		sb.append(", allowOverride=").append(allowOverride);
		sb.append(", parentRoleToAssign='").append(parentRoleToAssign).append('\'');
		sb.append(", targetInstance='").append(getTargetInstance()).append('\'');
		sb.append(", targetResource='").append(getTargetResource()).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
