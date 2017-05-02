package com.sirma.itt.seip.configuration.build;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Configuration instance provider. Provides means to access configuration instances by name.
 *
 * @author BBonev
 */
public interface ConfigurationInstanceProvider {

	/**
	 * Gets the configuration instance by name. The method should return non <code>null</code> for name of defined
	 * configuration.
	 *
	 * @param name
	 *            the configuration name
	 * @return the configuration identified by the passed name.
	 */
	ConfigurationInstance getConfiguration(String name);

	/**
	 * Gets the names of all registered configurations.
	 *
	 * @return the registered configurations
	 */
	Set<String> getRegisteredConfigurations();

	/**
	 * Gets the all instances not filtered
	 *
	 * @return the all instances
	 */
	Collection<ConfigurationInstance> getAllInstances();

	/**
	 * Gets the filtered instances based on the given filter predicate.
	 *
	 * @param filter
	 *            the filter
	 * @return the filtered
	 */
	Collection<ConfigurationInstance> getFiltered(Predicate<ConfigurationInstance> filter);
}
