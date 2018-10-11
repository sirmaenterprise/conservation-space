package com.sirma.itt.seip.configuration.build;

import java.util.Set;

/**
 * Provider for raw configuration values. The concrete implementation will be loaded via Java Service API and will be
 * instantiated as a CDI bean. This should allow implementing extension that provides multi-tenant configuration
 * property values if needed. The implementation could use the {@link ConfigurationSource} extension as extension to
 * provide different means of raw value configuration loading.
 *
 * @author BBonev
 */
public interface RawConfigurationAccessor {

	/**
	 * Gets the raw configuration value for the given key.
	 *
	 * @param name
	 *            the name
	 * @return the raw configuration value
	 */
	String getRawConfigurationValue(String name);

	/**
	 * Gets the available configurations names
	 *
	 * @return the available configurations
	 */
	Set<String> getAvailableConfigurations();

}
