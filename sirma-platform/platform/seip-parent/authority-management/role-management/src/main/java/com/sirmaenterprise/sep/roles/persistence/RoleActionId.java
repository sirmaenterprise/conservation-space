package com.sirmaenterprise.sep.roles.persistence;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Defines composite identifier that combines a role id and action id. The entity is used as primary key object for the
 * {@link RoleActionEntity}
 *
 * @since 2017-03-23
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Embeddable
public class RoleActionId implements Serializable {

	private static final long serialVersionUID = -1108434333012900866L;

	@Column(name = "action_id", nullable = false, updatable = false)
	private String action;

	@Column(name = "role_id", nullable = false, updatable = false)
	private String role;

	/**
	 * Default constructor
	 */
	public RoleActionId() {
		// nothing to do
	}

	/**
	 * Instantiate and fill all id properties
	 *
	 * @param role
	 *            the role identifier
	 * @param action
	 *            the action identifier
	 */
	public RoleActionId(String role, String action) {
		this.role = role;
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((action == null) ? 0 : action.hashCode());
		result = PRIME * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RoleActionId)) {
			return false;
		}
		RoleActionId other = (RoleActionId) obj;
		return nullSafeEquals(action, other.action) && nullSafeEquals(role, other.role);
	}
}
