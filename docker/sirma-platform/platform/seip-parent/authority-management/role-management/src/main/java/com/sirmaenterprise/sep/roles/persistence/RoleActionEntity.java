package com.sirmaenterprise.sep.roles.persistence;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.db.customtype.StringSetCustomType;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Mapping entity that links the defined roles and actions. The class represent which action to which role is assigned
 * with what filters and if the mapping is enabled or not.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@javax.persistence.Entity(name = "sep_role_actions")
@Table(name = "sep_role_actions")
@NamedQueries({
		@NamedQuery(name = RoleActionEntity.QUERY_ALL_ROLE_ACTIONS_KEY, query = RoleActionEntity.QUERY_ALL_ROLE_ACTIONS),
		@NamedQuery(name = RoleActionEntity.DELETE_ALL_ROLE_ACTIONS_KEY, query = RoleActionEntity.DELETE_ALL_ROLE_ACTIONS) })
public class RoleActionEntity implements Entity<RoleActionId>, Serializable {

	private static final long serialVersionUID = -2248629758607484337L;

	/**
	 * Query all {@link RoleActionEntity} entries
	 */
	public static final String QUERY_ALL_ROLE_ACTIONS_KEY = "QUERY_ALL_ROLE_ACTIONS";
	static final String QUERY_ALL_ROLE_ACTIONS = "from com.sirmaenterprise.sep.roles.persistence.RoleActionEntity";

	public static final String DELETE_ALL_ROLE_ACTIONS_KEY = "DELETE_ALL_ROLE_ACTIONS";
	static final String DELETE_ALL_ROLE_ACTIONS = "delete from com.sirmaenterprise.sep.roles.persistence.RoleActionEntity";

	@EmbeddedId
	private RoleActionId id;

	@Column(name = "is_enabled", nullable = false)
	@Type(type = BooleanCustomType.TYPE_NAME)
	private boolean enabled;

	@Column(name = "active_filters", nullable = true)
	@Type(type = StringSetCustomType.TYPE_NAME)
	private Set<String> filters;

	/**
	 * Instantiate empty entity
	 */
	public RoleActionEntity() {
		// default constructor
	}

	/**
	 * Instantiate and id
	 *
	 * @param id
	 *            the id to set
	 */
	public RoleActionEntity(RoleActionId id) {
		this.id = id;
	}

	/**
	 * Instantiate and set role and action ids
	 *
	 * @param role
	 *            the role id to set
	 * @param action
	 *            the action id to set
	 */
	public RoleActionEntity(String role, String action) {
		id = new RoleActionId(role, action);
	}

	@Override
	public RoleActionId getId() {
		return id;
	}

	@Override
	public void setId(RoleActionId id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean isActive) {
		enabled = isActive;
	}

	public Set<String> getFilters() {
		return filters;
	}

	public void setFilters(Set<String> filters) {
		this.filters = filters;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RoleActionEntity)) {
			return false;
		}
		RoleActionEntity other = (RoleActionEntity) obj;
		return nullSafeEquals(id, other.id);
	}

	@Override
	public int hashCode() {
		return 31 + ((id == null) ? 0 : id.hashCode());
	}
}
