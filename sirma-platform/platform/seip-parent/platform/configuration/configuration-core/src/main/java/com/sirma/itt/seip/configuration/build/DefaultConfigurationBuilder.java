package com.sirma.itt.seip.configuration.build;

import java.util.Optional;

import javax.enterprise.inject.Vetoed;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.Property;
import com.sirma.itt.seip.configuration.convert.ConfigurationValueConverter;
import com.sirma.itt.seip.configuration.convert.PropertyConverterProvider;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;
import com.sirma.itt.seip.configuration.sync.ReadWriteSynchronizer;

/**
 * Default configuration builder that creates property instance with enabled read/write access. The class is not
 * considered as a CDI bean by default
 *
 * @author BBonev
 */
@Vetoed
public class DefaultConfigurationBuilder implements ConfigurationBuilder {

	@Override
	public <T> ConfigurationProperty<T> buildProperty(ConfigurationInstance configuration,
			RawConfigurationAccessor configurationValueProvider, PropertyConverterProvider converterProvider,
			ConfigurationProvider configurationProvider, ConfigurationInstanceProvider configurationInstanceProvider)
					throws ConfigurationException {
		return build(configuration, configurationValueProvider, converterProvider, configurationProvider,
				configurationInstanceProvider);
	}

	/**
	 * Builds a single configuration property using the given providers. The property value will be evaluated lazily on
	 * first request.
	 *
	 * @param <T>
	 *            the generic type
	 * @param configuration
	 *            the configuration
	 * @param configurationValueProvider
	 *            the configuration value provider
	 * @param converterProvider
	 *            the converter provider
	 * @param configurationProvider
	 *            the configuration provider
	 * @param configurationInstanceProvider
	 *            the configuration instance provider
	 * @return the configuration property
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	public static <T> ConfigurationProperty<T> build(ConfigurationInstance configuration,
			RawConfigurationAccessor configurationValueProvider, PropertyConverterProvider converterProvider,
			ConfigurationProvider configurationProvider, ConfigurationInstanceProvider configurationInstanceProvider)
					throws ConfigurationException {

		return new Property<>(configuration, new ReadWriteSynchronizer(), () -> {
			ConfigurationValueConverter<T> valueConverter = resolveConverter(configuration, converterProvider);

			TypeConverterContext converterContext = configuration.createConverterContext(configurationInstanceProvider,
					configurationValueProvider, configurationProvider);

			return valueConverter.convert(converterContext);
		});
	}

	/**
	 * Resolve converter for the given {@link ConfigurationInstance}.
	 *
	 * @param <T>
	 *            the generic type
	 * @param configuration
	 *            the configuration
	 * @param converterProvider
	 *            the converter provider
	 * @return the configuration value converter
	 */
	@SuppressWarnings("unchecked")
	public static <T> ConfigurationValueConverter<T> resolveConverter(ConfigurationInstance configuration,
			PropertyConverterProvider converterProvider) {
		Optional<ConfigurationValueConverter<?>> namedConverter = converterProvider
				.getConverter(configuration.getConverter());
		// if converter is present and the types are the same
		// then we return it otherwise we use normal converter resolving
		if (namedConverter.isPresent()) {
			boolean validEnumConverter = (configuration.getType().isEnum() && namedConverter.get().getType()
					.isAssignableFrom(configuration.getType()));
			if (validEnumConverter || configuration.getType().isAssignableFrom(namedConverter.get().getType())) {
				return (ConfigurationValueConverter<T>) namedConverter.get();
			}
		}
		return converterProvider.getConverter(configuration.getType());
	}

}
