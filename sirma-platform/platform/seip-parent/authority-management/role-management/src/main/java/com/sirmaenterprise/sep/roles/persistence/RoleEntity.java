package com.sirmaenterprise.sep.roles.persistence;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;

/**
 * Entity that represent a single permission role
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@javax.persistence.Entity(name = "sep_role")
@Table(name = "sep_role")
@NamedQueries({ @NamedQuery(name = RoleEntity.QUERY_ALL_ROLES_KEY, query = RoleEntity.QUERY_ALL_ROLES),
		@NamedQuery(name = RoleEntity.QUERY_ROLES_BY_IDS_KEY, query = RoleEntity.QUERY_ROLES_BY_IDS) })
public class RoleEntity implements Entity<String>, Serializable {

	private static final long serialVersionUID = 5555196556415199855L;
	/**
	 * Query all roles sorted by ascending role order
	 */
	public static final String QUERY_ALL_ROLES_KEY = "QUERY_ALL_ROLES";
	static final String QUERY_ALL_ROLES = "select r from com.sirmaenterprise.sep.roles.persistence.RoleEntity r order by r.order ASC";

	/**
	 * Query roles by given list of identifiers (ids) sorted by ascending role order
	 */
	public static final String QUERY_ROLES_BY_IDS_KEY = "QUERY_ROLES_BY_IDS";
	static final String QUERY_ROLES_BY_IDS = "select r from com.sirmaenterprise.sep.roles.persistence.RoleEntity r where r.id in (:ids) order by r.order ASC";

	@Id
	@Column(name = "id", length = 100, unique = true, nullable = false, updatable = false)
	private String id;

	@Column(name = "role_order", nullable = false)
	private int order;

	@Column(name = "is_enabled", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean enabled;

	@Column(name = "can_write", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean canWrite;

	@Column(name = "can_read", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean canRead;

	@Column(name = "is_internal", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean isInternal;

	@Column(name = "is_user_defined", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean userDefined;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isInternal() {
		return isInternal;
	}

	public void setInternal(boolean isInternal) {
		this.isInternal = isInternal;
	}

	public boolean isUserDefined() {
		return userDefined;
	}

	public void setUserDefined(boolean userDefined) {
		this.userDefined = userDefined;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RoleIdentifier)) {
			return false;
		}
		RoleIdentifier other = (RoleIdentifier) obj;
		return nullSafeEquals(id, other.getIdentifier());
	}

	@Override
	public int hashCode() {
		return 31 + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		builder
				.append("RoleEntity [id=")
					.append(id)
					.append(", order=")
					.append(order)
					.append(", canWrite=")
					.append(canWrite)
					.append(", canRead=")
					.append(canRead)
					.append("]");
		return builder.toString();
	}

}
