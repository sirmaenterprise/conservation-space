package com.sirma.itt.seip.configuration.build;

import java.util.Properties;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Configuration source for raw configuration values.
 *
 * @author BBonev
 */
public interface ConfigurationSource extends Plugin {

	String NAME = "ConfigurationSource";

	/**
	 * Gets the configurations.
	 *
	 * @return the configurations
	 */
	Properties getConfigurations();

	/**
	 * Gets all configurations that are accessible for tenant with specified id. If tenant id is <code>null</code>
	 * nothing will be returned.
	 *
	 * @param tenantId
	 *            the tenant id
	 * @return the configurations
	 */
	Properties getConfigurations(String tenantId);

	/**
	 * Gets a property that does not have any tenant context.
	 *
	 * @param key
	 *            the key
	 * @return the property
	 */
	String getConfigurationValue(String key);

	/**
	 * Gets the property that is specific for the given tenant.
	 *
	 * @param key
	 *            the key
	 * @param tenantId
	 *            the tenant id
	 * @return the property
	 */
	String getConfigurationValue(String key, String tenantId);
}
