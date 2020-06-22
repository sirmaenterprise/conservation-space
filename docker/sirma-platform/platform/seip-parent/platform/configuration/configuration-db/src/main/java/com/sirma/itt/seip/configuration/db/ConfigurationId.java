/**
 *
 */
package com.sirma.itt.seip.configuration.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Configuration database id that includes a configuration name and tenant id.
 *
 * @author BBonev
 */
@Embeddable
@PersistenceUnitBinding(PersistenceUnits.CORE)
class ConfigurationId implements Serializable {
	private static final long serialVersionUID = -6648382754177905704L;

	/** The id. */
	@Column(name = "id", length = 128, nullable = false, updatable = false)
	private String key;

	/** The tenant id. */
	@Column(name = "tenant_id", length = 128, nullable = true, updatable = false)
	private String tenantId;

	/**
	 * Instantiates a new configuration id.
	 */
	public ConfigurationId() {
		// default constructor
	}

	/**
	 * Instantiates a new configuration id.
	 *
	 * @param key
	 *            the key
	 * @param tenantId
	 *            the tenant id
	 */
	public ConfigurationId(String key, String tenantId) {
		this.key = key;
		this.tenantId = tenantId;
	}

	/**
	 * Instantiates a new configuration id.
	 *
	 * @param configuration
	 *            the configuration
	 */
	ConfigurationId(Configuration configuration) {
		java.util.Objects.requireNonNull(configuration, "Cannot create ConfiguratioId from null configuration!");
		key = configuration.getConfigurationKey();
		tenantId = configuration.getTenantId();
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key
	 *            the new key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Gets the tenant id.
	 *
	 * @return the tenant id
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Sets the tenant id.
	 *
	 * @param tenantId
	 *            the new tenant id
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (key == null ? 0 : key.hashCode());
		result = PRIME * result + (tenantId == null ? 0 : tenantId.hashCode());
		return result;
	}

	/**
	 * Equals.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ConfigurationId)) {
			return false;
		}
		ConfigurationId other = (ConfigurationId) obj;
		return EqualsHelper.nullSafeEquals(key, other.key) && EqualsHelper.nullSafeEquals(tenantId, other.tenantId);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		builder.append("(").append(tenantId).append(")").append(key);
		return builder.toString();
	}

}
