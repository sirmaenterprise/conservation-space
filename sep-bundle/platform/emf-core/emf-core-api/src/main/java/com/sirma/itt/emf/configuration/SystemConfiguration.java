package com.sirma.itt.emf.configuration;

import java.util.Set;

/**
 * The Interface SystemConfiguration for accessing the system's configuration directly.
 * 
 * @author BBonev
 */
public interface SystemConfiguration {

	/**
	 * Gets the configuration value or the default value if the key not found.
	 * 
	 * @param key
	 *            the key to look for
	 * @param defaultValue
	 *            the default value
	 * @return the property value or the default value if not found
	 */
	String getConfiguration(String key, String defaultValue);

	/**
	 * Gets the configuration value or <code>null</code> if the key not found.
	 * 
	 * @param key
	 *            the key to look for
	 * @return the property value or <code>null</code> if not found
	 */
	String getConfiguration(String key);

	/**
	 * Gets the configuration keys.
	 * 
	 * @return the configuration keys
	 */
	Set<String> getConfigurationKeys();

}