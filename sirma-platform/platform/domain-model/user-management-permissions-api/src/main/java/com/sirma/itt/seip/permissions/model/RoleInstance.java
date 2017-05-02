package com.sirma.itt.seip.permissions.model;

import java.util.List;

/**
 * Object that represents an user role in the system.
 *
 * @author BBonev
 */
public class RoleInstance {

	/** The role identifier */
	private RoleId roleId;
	/**
	 * The include section. If this role should be constructed from multiple roles. The values could be a path to sub
	 * section in the role in the format <code> &lt;roleId&gt;/&lt;permissionId&gt; </code>
	 */
	private List<String> include;
	/** The list of permissions that are specific for the current role. */
	private List<Permission> permissions;
	/**
	 * Defines if the role should be public and visible to the user <code>true</code> or should be used only for
	 * internal references <code>false</code>
	 */
	private boolean external = true;

	/**
	 * Gets the role id.
	 *
	 * @return the role id
	 */
	public RoleId getRoleId() {
		return roleId;
	}

	/**
	 * Setter method for roleId.
	 *
	 * @param roleId
	 *            the roleId to set
	 */
	public void setRoleId(RoleId roleId) {
		this.roleId = roleId;
	}

	/**
	 * Getter method for include.
	 *
	 * @return the include
	 */
	public List<String> getInclude() {
		return include;
	}

	/**
	 * Setter method for include.
	 *
	 * @param include
	 *            the include to set
	 */
	public void setInclude(List<String> include) {
		this.include = include;
	}

	/**
	 * Getter method for permissions.
	 *
	 * @return the permissions
	 */
	public List<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * Setter method for permissions.
	 *
	 * @param permissions
	 *            the permissions to set
	 */
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}

	/**
	 * Getter method for external.
	 *
	 * @return the external
	 */
	public boolean isExternal() {
		return external;
	}

	/**
	 * Setter method for external.
	 *
	 * @param external
	 *            the external to set
	 */
	public void setExternal(boolean external) {
		this.external = external;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
				.append(roleId)
					.append("[external=")
					.append(external)
					.append(", include=")
					.append(include)
					.append(", permissions=")
					.append(permissions)
					.append("]");
		return builder.toString();
	}

}
