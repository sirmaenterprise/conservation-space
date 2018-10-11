package com.sirma.itt.seip.configuration.build;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.convert.PropertyConverterProvider;

/**
 * Builder that is responsible for building concrete configuration objects. The concrete implementation class will be
 * loaded via Java Service API but will be instantiated as a CDI bean if it's valid one. This is the main class that
 * need to be provided for the configuration extension to replace the default build configuration implementation.
 *
 * @author BBonev
 */
public interface ConfigurationBuilder {

	/**
	 * Builds the property instance for the given configuration instance using the provided providers.
	 *
	 * @param <T>
	 *            the configuration value type
	 * @param configuration
	 *            the configuration to build property for
	 * @param configurationValueProvider
	 *            the configuration value provider that is responsible for the raw value provisioning
	 * @param converterProvider
	 *            the converter provider responsible for property converter provisioning
	 * @param configurationProvider
	 *            the configuration provider that is responsible for provisioning other configuration property values.
	 * @param configurationInstanceProvider
	 *            the configuration instance provider
	 * @return the created configuration property.
	 * @throws ConfigurationException
	 *             when there is a problem with configuration building
	 */
	<T> ConfigurationProperty<T> buildProperty(ConfigurationInstance configuration,
			RawConfigurationAccessor configurationValueProvider, PropertyConverterProvider converterProvider,
			ConfigurationProvider configurationProvider, ConfigurationInstanceProvider configurationInstanceProvider)
					throws ConfigurationException;
}
