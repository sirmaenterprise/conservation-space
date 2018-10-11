package com.sirma.itt.seip.configuration.build;

import java.util.Set;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Producer for any registered configuration property.
 *
 * @author BBonev
 */
public interface ConfigurationProvider {

	/**
	 * Gets a single property for the provide name or <code>null</code> if not valid.
	 *
	 * @param <T>
	 *            the configuration value type
	 * @param name
	 *            the name
	 * @return the configuration property or <code>null</code> if not registered.
	 * @throws ConfigurationException
	 *             if there is problem while building configuration.
	 */
	<T> ConfigurationProperty<T> getProperty(String name) throws ConfigurationException;

	/**
	 * Gets the registered configurations.
	 *
	 * @return the registered configurations
	 */
	Set<String> getInstantiatedConfigurations();

}
