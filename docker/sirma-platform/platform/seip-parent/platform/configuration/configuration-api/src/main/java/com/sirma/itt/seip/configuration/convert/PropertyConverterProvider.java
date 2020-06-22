package com.sirma.itt.seip.configuration.convert;

import java.util.Optional;
import java.util.Set;

/**
 * Provider for the all registered property converters.
 *
 * @author BBonev
 * @see ConfigurationValueConverter
 */
public interface PropertyConverterProvider {

	/**
	 * Gets the converter for the requested type.
	 *
	 * @param <T>
	 *            the converter value type
	 * @param resultType
	 *            the expected converter result type
	 * @return the property value converter that is able to handle the given type if if any or <code>null</code> if not
	 *         such converter exists
	 */
	<T> ConfigurationValueConverter<T> getConverter(Class<T> resultType);

	/**
	 * Gets a converter by name if any.
	 *
	 * @param converterName
	 *            the converter name
	 * @return the converter
	 */
	Optional<ConfigurationValueConverter<?>> getConverter(String converterName);

	/**
	 * Returns the types of all supported converters
	 *
	 * @return the supported converter types
	 */
	Set<Class<?>> getSupportedTypes();
}
