/**
 *
 */
package com.sirma.itt.seip.tenant.context;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.Entity;

/**
 * Persistent entity to represent a single tenant information.
 *
 * @author BBonev
 */
@javax.persistence.Entity
@Table(name = "seip_tenants")
@NamedQueries({
		@NamedQuery(name = TenantEntity.QUERY_ALL_TENANTS_INFO_KEY, query = TenantEntity.QUERY_ALL_TENANTS_INFO),
		@NamedQuery(name = TenantEntity.QUERY_ALL_TENANTS_IDS_KEY, query = TenantEntity.QUERY_ALL_TENANTS_IDS),
		@NamedQuery(name = TenantEntity.QUERY_ACTIVE_TENANTS_INFO_KEY, query = TenantEntity.QUERY_ACTIVE_TENANTS_INFO),
		@NamedQuery(name = TenantEntity.QUERY_ACTIVE_TENANT_IDS_KEY, query = TenantEntity.QUERY_ACTIVE_TENANT_IDS),
		@NamedQuery(name = TenantEntity.QUERY_ACTIVE_TENANT_ID_BY_ID_KEY, query = TenantEntity.QUERY_ACTIVE_TENANT_ID_BY_ID),
		@NamedQuery(name = TenantEntity.QUERY_CHECK_TENANT_EXISTS_BY_ID_KEY, query = TenantEntity.QUERY_CHECK_TENANT_EXISTS_BY_ID),
		@NamedQuery(name = TenantEntity.CHANGE_TENANT_STATE_KEY, query = TenantEntity.CHANGE_TENANT_STATE) })
class TenantEntity implements Entity<String> {

	/** Query that returns all {@link TenantEntity}s. */
	public static final String QUERY_ALL_TENANTS_INFO_KEY = "QUERY_ALL_TENANTS_INFO";

	/** The Constant QUERY_ALL_TENANTS_INFO. */
	static final String QUERY_ALL_TENANTS_INFO = "from TenantEntity";

	/** Query that returns all {@link TenantEntity#getId()}s. */
	public static final String QUERY_ALL_TENANTS_IDS_KEY = "QUERY_ALL_TENANTS_IDS";
	static final String QUERY_ALL_TENANTS_IDS = "select tenantId from TenantEntity";

	/** Query that returns all active {@link TenantEntity}s. */
	public static final String QUERY_ACTIVE_TENANTS_INFO_KEY = "QUERY_ACTIVE_TENANTS_INFO";

	/** The Constant QUERY_ACTIVE_TENANTS_INFO. */
	static final String QUERY_ACTIVE_TENANTS_INFO = "from TenantEntity where active = 1";

	/** Query that returns {@link TenantEntity#getId()} of all active tenants. */
	public static final String QUERY_ACTIVE_TENANT_IDS_KEY = "QUERY_ACTIVE_TENANT_IDS";

	/** The Constant QUERY_ACTIVE_TENANT_IDS. */
	static final String QUERY_ACTIVE_TENANT_IDS = "select tenantId from TenantEntity where active = 1";

	/**
	 * Query that returns {@link TenantEntity#getId()} if there is such tenant and that tenant is active.
	 */
	public static final String QUERY_ACTIVE_TENANT_ID_BY_ID_KEY = "QUERY_ACTIVE_TENANT_ID_BY_ID";
	static final String QUERY_ACTIVE_TENANT_ID_BY_ID = "select tenantId from TenantEntity where active = 1 and tenantId = :tenantId";

	/** Query that returns {@link TenantEntity#getId()} if there is such tenant. */
	public static final String QUERY_CHECK_TENANT_EXISTS_BY_ID_KEY = "QUERY_CHECK_TENANT_EXISTS_BY_ID";
	static final String QUERY_CHECK_TENANT_EXISTS_BY_ID = "select tenantId from TenantEntity where tenantId = :tenantId";

	/** Update query that sets new tenant state. Updates the active field */
	public static final String CHANGE_TENANT_STATE_KEY = "CHANGE_TENANT_STATE";
	static final String CHANGE_TENANT_STATE = "update TenantEntity set active = :active where tenantId = :tenantId";

	/** The tenant id. */
	@Id
	@Column(name = "id", length = 128, updatable = false)
	private String tenantId;

	/** The active. */
	@Column(name = "active", nullable = false)
	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
	private Boolean active;

	/** The tenant admin. */
	@Column(name = "tenant_admin", length = 128, nullable = false)
	private String tenantAdmin;

	/** The display name. */
	@Column(name = "display_name", length = 512)
	private String displayName;

	/** The description. */
	@Column(name = "description", length = 2048)
	private String description;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Override
	public String getId() {
		return tenantId;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	@Override
	public void setId(String id) {
		tenantId = id;
	}

	/**
	 * Gets the active.
	 *
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * Sets the active.
	 *
	 * @param active
	 *            the new active
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * Gets the tenant admin.
	 *
	 * @return the tenant admin
	 */
	public String getTenantAdmin() {
		return tenantAdmin;
	}

	/**
	 * Sets the tenant admin.
	 *
	 * @param tenantAdmin
	 *            the new tenant admin
	 */
	public void setTenantAdmin(String tenantAdmin) {
		this.tenantAdmin = tenantAdmin;
	}

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name.
	 *
	 * @param displayName
	 *            the new display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
