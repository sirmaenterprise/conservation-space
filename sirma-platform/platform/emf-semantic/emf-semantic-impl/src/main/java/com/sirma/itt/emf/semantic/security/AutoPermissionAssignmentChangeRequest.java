package com.sirma.itt.emf.semantic.security;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

/**
 * Represents a change triggered from automatic permission assignment functionality
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 26/04/2017
 */
class AutoPermissionAssignmentChangeRequest extends PermissionChangeRequest {
	private static final long serialVersionUID = -4418856784527939810L;

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
	public String toString() {
		return new StringBuilder(512)
				.append("PermissionChangeRequest [targetInstance=")
				.append(getTargetInstance())
				.append(", roleToAssign=")
				.append(roleToAssign)
				.append(", targetResource=")
				.append(getTargetResource())
				.append(", allowOverride=")
				.append(allowOverride)
				.append(", parentRoleToAssign=")
				.append(parentRoleToAssign)
				.append("]")
				.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (allowOverride ? 1231 : 1237);
		result = prime * result + (parentRoleToAssign == null ? 0 : parentRoleToAssign.hashCode());
		result = prime * result + (roleToAssign == null ? 0 : roleToAssign.hashCode());
		result = prime * super.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof AutoPermissionAssignmentChangeRequest)) {
			return false;
		}
		AutoPermissionAssignmentChangeRequest other = (AutoPermissionAssignmentChangeRequest) obj;
		return nullSafeEquals(roleToAssign, other.roleToAssign) && nullSafeEquals(parentRoleToAssign,
				other.parentRoleToAssign) && allowOverride == other.allowOverride;
	}
}
