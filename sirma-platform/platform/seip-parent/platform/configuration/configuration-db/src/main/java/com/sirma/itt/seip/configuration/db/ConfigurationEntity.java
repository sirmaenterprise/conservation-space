/**
 *
 */
package com.sirma.itt.seip.configuration.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Configuration entity for a single configuration property.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.CORE)
@javax.persistence.Entity
@Table(name = "seip_configurations")
@NamedQueries({
		@NamedQuery(name = ConfigurationEntity.QUERY_CONFIG_BY_ID_TENANT_KEY, query = ConfigurationEntity.QUERY_CONFIG_BY_ID_TENANT),
		@NamedQuery(name = ConfigurationEntity.QUERY_CONFIG_VALUE_BY_ID_TENANT_KEY, query = ConfigurationEntity.QUERY_CONFIG_VALUE_BY_ID_TENANT),
		@NamedQuery(name = ConfigurationEntity.QUERY_CONFIG_FOR_TENANT_KEY, query = ConfigurationEntity.QUERY_CONFIG_FOR_TENANT),
		@NamedQuery(name = ConfigurationEntity.QUERY_CONFIG_BY_TENANT_KEY, query = ConfigurationEntity.QUERY_CONFIG_BY_TENANT),
		@NamedQuery(name = ConfigurationEntity.QUERY_ALL_CONFIG_KEY, query = ConfigurationEntity.QUERY_ALL_CONFIG),
		@NamedQuery(name = ConfigurationEntity.DELETE_ALL_CONFIGS_FOR_TENANT_KEY, query = ConfigurationEntity.DELETE_ALL_CONFIGS_FOR_TENANT) })
class ConfigurationEntity implements Entity<ConfigurationId>, Serializable {

	private static final long serialVersionUID = 5265354365031969406L;

	/**
	 * Query all {@link ConfigurationEntity}s
	 */
	public static final String QUERY_ALL_CONFIG_KEY = "QUERY_ALL_CONFIG";
	static final String QUERY_ALL_CONFIG = "from ConfigurationEntity";

	/**
	 * Query {@link ConfigurationEntity} by configuration id and tenant id. Params: id and tenantId
	 */
	public static final String QUERY_CONFIG_BY_ID_TENANT_KEY = "QUERY_CONFIG_BY_ID_TENANT";
	static final String QUERY_CONFIG_BY_ID_TENANT = "from ConfigurationEntity where id.key in (:id) and id.tenantId=:tenantId";

	/**
	 * Query {@link ConfigurationEntity}s for tenant id. Params: tenantId
	 */
	public static final String QUERY_CONFIG_BY_TENANT_KEY = "QUERY_CONFIG_BY_TENANT";
	static final String QUERY_CONFIG_BY_TENANT = "from ConfigurationEntity where id.tenantId=:tenantId";

	/**
	 * Query {@link ConfigurationEntity#getValue()} by configuration id and tenant id. Params: id
	 * and tenantId
	 */
	public static final String QUERY_CONFIG_VALUE_BY_ID_TENANT_KEY = "QUERY_CONFIG_VALUE_BY_ID_TENANT";
	static final String QUERY_CONFIG_VALUE_BY_ID_TENANT = "select value from ConfigurationEntity where id.key=:id and id.tenantId=:tenantId";

	/**
	 * Query {@link ConfigurationEntity}s for a single tenant. The query will return all system
	 * properties and the one specific for the tenant. Params: systemTenant, tenantId
	 */
	public static final String QUERY_CONFIG_FOR_TENANT_KEY = "QUERY_CONFIG_FOR_TENANT";
	static final String QUERY_CONFIG_FOR_TENANT = "from ConfigurationEntity where id.tenantId =:systemTenant OR id.tenantId=:tenantId order by id.tenantId desc";

	public static final String DELETE_ALL_CONFIGS_FOR_TENANT_KEY = "DELETE_ALL_CONFIGS_FOR_TENANT";
	static final String DELETE_ALL_CONFIGS_FOR_TENANT = "delete from ConfigurationEntity where id.tenantId=:tenantId skip locked";

	@EmbeddedId
	private ConfigurationId id;

	@Column(name = "config_value", length = 4098, nullable = true)
	private String value;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@Override
	public ConfigurationId getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	@Override
	public void setId(ConfigurationId id) {
		this.id = id;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(256);
		return builder.append(id).append("=").append(value).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ConfigurationEntity)) {
			return false;
		}
		ConfigurationEntity other = (ConfigurationEntity) obj;
		return EqualsHelper.nullSafeEquals(id, other.id);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (id == null ? 0 : id.hashCode());
		return result;
	}
	
}
