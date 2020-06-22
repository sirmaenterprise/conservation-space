package com.sirma.itt.seip.configuration.db;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Object that represents a single configuration with it's value and definition. The object supports JSON conversion if
 * both directions.
 *
 * @author BBonev
 */
public class Configuration implements Serializable {

	private static final long serialVersionUID = -7594445729005789262L;

	private String configurationKey;
	private String tenantId;
	private String rawValue;
	private Supplier<Object> value;
	private Supplier<ConfigurationInstance> definition;

	/**
	 * Instantiates a new configuration.
	 */
	public Configuration() {
		// default constructor
	}

	/**
	 * Instantiates a new configuration.
	 *
	 * @param configurationKey
	 *            the configuration key
	 * @param value
	 *            the value
	 */
	public Configuration(String configurationKey, String value) {
		this.configurationKey = configurationKey;
		this.value = () -> value;
	}

	/**
	 * Instantiates a new configuration.
	 *
	 * @param configurationKey
	 *            the configuration key
	 * @param value
	 *            the value
	 * @param tenantId
	 *            the tenant id
	 */
	public Configuration(String configurationKey, String value, String tenantId) {
		this.configurationKey = configurationKey;
		this.value = () -> value;
		this.tenantId = tenantId;
	}

	/**
	 * Instantiates a new configuration.
	 *
	 * @param definition
	 *            the definition
	 */
	Configuration(ConfigurationInstance definition) {
		configurationKey = definition.getName();
		this.definition = () -> definition;
		// for configurations initialized from definition only use the default value as value
		value = this::getDefaultValue;
    }

	/**
	 * Instantiates a new configuration.
	 *
	 * @param id
	 *            the id
	 */
	Configuration(ConfigurationId id) {
		configurationKey = id.getKey();
		tenantId = id.getTenantId();
		value = () -> null;
	}

	/**
	 * Instantiates a new configuration.
	 *
	 * @param configurationKey
	 *            the configuration key
	 * @param tenantId
	 *            the tenant id
	 * @param value
	 *            the value
	 * @param definition
	 *            the definition
	 */
	Configuration(String configurationKey, String tenantId, Supplier<Object> value,
			Supplier<ConfigurationInstance> definition) {
		this.configurationKey = configurationKey;
		this.tenantId = tenantId;
		this.value = value;
		this.definition = definition;
	}

	/**
	 * Gets the configuration key.
	 *
	 * @return the configuration key
	 */
	public String getConfigurationKey() {
		return configurationKey;
	}

	/**
	 * Sets the configuration key.
	 *
	 * @param configurationKey
	 *            the new configuration key
	 */
	public void setConfigurationKey(String configurationKey) {
		this.configurationKey = configurationKey;
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
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value.get();
	}

	/**
	 * Gets the default value or null
	 *
	 * @return the default value
	 */
	public Object getDefaultValue() {
		ConfigurationInstance instance = getDefinition();
		if (instance != null) {
			Annotation annotation = instance.getAnnotation();
			if (annotation instanceof ConfigurationPropertyDefinition) {
				return ((ConfigurationPropertyDefinition) annotation).defaultValue();
			}
		}
		return null;
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	public void setValue(Object value) {
		this.value = () -> value;
	}

	/**
	 * Gets the definition.
	 *
	 * @return the definition
	 */
	public ConfigurationInstance getDefinition() {
		return definition.get();
	}

	/**
	 * @param definition
	 *            the definition to set
	 */
	public void setDefinition(ConfigurationInstance definition) {
		this.definition = () -> definition;
	}

	/**
	 * Sub system id. Identifier that can be used to group configurations in logical groups. Default value is empty
	 * string.
	 *
	 * @return configuration sub system
	 */
	public String getSubSystem() {
		return getDefinition().getSubSystem();
	}

	/**
	 * Configuration name alias. If leaved empty the configuration alias is the
	 * {@link #getSubSystem()}.{@link #getConfigurationKey()}. Configuration could not be inserted by alias. It's used
	 * only for filtering.
	 *
	 * @return configuration alias.
	 */
	public String getAlias() {
		return getDefinition().getAlias();
	}

	/**
	 * Checks if a configuration's value as sensitive. This configuration values will not be displayed as plain text
	 * anywhere and will not be viewable by non administrative account. This is mainly for passwords and other keys.
	 *
	 * @return true, if configuration value is sensitive
	 */
	public boolean isSensitive() {
		return getDefinition().isSensitive();
	}

	/**
	 * Checks if a configuration's value is password. This configuration values will not be returned.
	 *
	 * @return true, if configuration value is password
	 */
	public boolean isPassword() {
		return getDefinition().isPassword();
	}

	/**
	 * Checks if the configuration is for the system scope.
	 *
	 * @return <code>true</code> if the configuration is for the system and is not shared between tenants
	 */
	public boolean isSystem() {
		return getDefinition().isSystemConfiguration();
	}

	/**
	 * Returns the configuration's value before any conversion or modification - the same as in the database.
	 *
	 * @return the raw value
	 */
    public String getRawValue() {
        return rawValue;
    }

	/**
	 * Sets the configurations's before any conversion or modification. It should represent the value from the database.
	 *
	 * @param rawValue - the raw value of the configuration
	 */
	public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    /**
	 * To id.
	 *
	 * @return the configuration id
	 */
	ConfigurationId toId() {
		return new ConfigurationId(getConfigurationKey(), getTenantId());
	}

	@Override
	public String toString() {
		return new StringBuilder(128)
				.append("Configuration [configurationKey=")
					.append(configurationKey)
					.append(", tenantId=")
					.append(tenantId)
					.append(", value=")
					.append(isSensitive() ? "SENSITIVE VALUE" : value.get())
					.append("]")
					.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (configurationKey == null ? 0 : configurationKey.hashCode());
		result = prime * result + (tenantId == null ? 0 : tenantId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Configuration)) {
			return false;
		}
		Configuration other = (Configuration) obj;
		return EqualsHelper.nullSafeEquals(configurationKey, other.configurationKey)
				&& EqualsHelper.nullSafeEquals(tenantId, other.tenantId);
	}

}
