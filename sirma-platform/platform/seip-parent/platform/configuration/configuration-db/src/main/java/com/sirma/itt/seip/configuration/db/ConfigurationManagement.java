/**
 *
 */
package com.sirma.itt.seip.configuration.db;

import java.util.Collection;

/**
 * Provides access to persisted configurations.
 * <p>
 * Note that update methods could modify only the configuration value and nothing more.
 *
 * @author BBonev
 */
public interface ConfigurationManagement {

	/**
	 * Gets the all configurations this include all system and all configurations for all tenants.
	 * <p>
	 * <b>This method could be accessed only by system administrator.</b>
	 *
	 * @return the all configurations
	 */
	Collection<Configuration> getAllConfigurations();

	/**
	 * Gets the system configurations.
	 * <p>
	 * <b>This method could be accessed only by system administrator.</b>
	 *
	 * @return the system configurations
	 */
	Collection<Configuration> getSystemConfigurations();

	/**
	 * Gets the current tenant configurations. This method retrieves all possible configurations that are available to
	 * the user from the current tenant.
	 * <p>
	 * <b> The method could be executed only if logged as tenant administrator.</b>
	 *
	 * @return the current tenant configurations
	 */
	Collection<Configuration> getCurrentTenantConfigurations();

	/**
	 * Adds system or tenant configurations depending on the configuration object. If
	 * {@link Configuration#getTenantId()} returns <code>null</code> that configurations will be added as system
	 * property otherwise in a tenant with id equal to the returned value.<br>
	 * This method is intended to be called during tenant provisioning so that tenant configurations could be inserted
	 * for the new tenant.
	 * <p>
	 * Note that if configuration with such id for the given tenant exists nothing will be updated!
	 * <p>
	 * Note also that this method will not trigger configuration property notification for value change.
	 * <p>
	 * <b>This method could be accessed only by system or tenant administrator.</b>
	 *
	 * @param configurations
	 *            the configurations to add
	 * @return the actual added configurations
	 */
	Collection<Configuration> addConfigurations(Collection<Configuration> configurations);

	/**
	 * Update system configuration value.
	 * <p>
	 * <b>This method could be accessed only by system administrator.</b>
	 *
	 * @param configuration
	 *            the configuration
	 */
	void updateSystemConfiguration(Configuration configuration);

	/**
	 * Update multiple system configurations.
	 * <p>
	 * <b>This method could be accessed only by system administrator.</b>
	 *
	 * @param configuration
	 *            the configuration
	 */
	void updateSystemConfigurations(Collection<Configuration> configuration);

	/**
	 * Update configuration.
	 * <p>
	 * <b> The method could be executed only if logged as tenant administrator.</b>
	 *
	 * @param configuration
	 *            the configuration
	 */
	void updateConfiguration(Configuration configuration);

	/**
	 * Update multiple configurations.
	 * <p>
	 * <b> The method could be executed only if logged as tenant administrator.</b>
	 *
	 * @param configuration
	 *            the configuration
	 */
	void updateConfigurations(Collection<Configuration> configuration);

	/**
	 * Removes the system configuration. Calling this method should remove any persisted values for configuration with
	 * the given key. If no configuration with such id exists nothing will be removed! <b>This method could be accessed
	 * only by system administrator.</b>
	 *
	 * @param key
	 *            the key to remove
	 */
	void removeSystemConfiguration(String key);

	/**
	 * Removes the configuration. Calling this method should remove any persisted values for configuration with the
	 * given key. If no configuration with such id exists nothing will be removed! <b> The method could be executed only
	 * if logged as tenant administrator.</b>
	 *
	 * @param key
	 *            the key to remove for the current tenant
	 */
	void removeConfiguration(String key);

	/**
	 * Removes all configurations for the currently set tenant.
	 */
	void removeAllConfigurations();
}