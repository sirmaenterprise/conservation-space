package com.sirma.itt.seip.permissions.model;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.permissions.action.external.Actions;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Represents an object that combines different action sets that have a specific/common purpose. Example: actions
 * associated with reading and accessing an object could be separated from the one used for object's modification.
 *
 * @author BBonev
 */
public class Permission extends PermissionId implements Copyable<Permission> {
	private static final long serialVersionUID = -1503543344106031077L;

	private List<Actions> actions;

	/**
	 * Needed by dozer default constructor
	 */
	public Permission() {
		super("");
	}

	/**
	 * Getter method for actions.
	 *
	 * @return the actions
	 */
	public List<Actions> getActions() {
		return actions;
	}

	@Override
	public void setPermissionId(String permissionId) {
		super.setPermissionId(permissionId);
	}

	/**
	 * Setter method for actions.
	 *
	 * @param actions
	 *            the actions to set
	 */
	public void setActions(List<Actions> actions) {
		this.actions = actions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getPermissionId() == null ? 0 : getPermissionId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof com.sirma.itt.seip.permissions.PermissionIdentifier)) {
			return false;
		}
		com.sirma.itt.seip.permissions.PermissionIdentifier other = (com.sirma.itt.seip.permissions.PermissionIdentifier) obj;
		return EqualsHelper.nullSafeEquals(getPermissionId(), other.getPermissionId());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		builder.append("Permission[").append(getPermissionId()).append(" -> ").append(actions).append("]");
		return builder.toString();
	}

	@Override
	public Permission createCopy() {
		Permission clone = new Permission();
		clone.setPermissionId(getPermissionId());

		if (actions != null) {
			List<Actions> actionsCopy = new ArrayList<>(actions.size());
			for (Actions action : actions) {
				actionsCopy.add(action.createCopy());
			}
			clone.setActions(actionsCopy);
		}
		return clone;
	}

}
