package com.sirma.itt.seip.configuration.convert;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;

/**
 * Represents a property configuration converter instance that is capable of providing a injectable value for a
 * particular type.<br>
 * A valid converter is a class that implements the {@link ConfigurationValueConverter} and is annotated with the
 * {@link ConfigurationConverter} annotation or a method that is accepts a {@link ConverterContext} or a
 * {@link GroupConverterContext}, has a non void return and is annotated with {@link ConfigurationConverter} annotation.
 *
 * @param <T>
 *            the expected converter result type
 * @author BBonev
 */
public interface ConfigurationValueConverter<T> extends Named {

	/**
	 * Gets the supported result converter type.
	 *
	 * @return the type
	 */
	Class<T> getType();

	/**
	 * Gets the name of the converter or null if no such is defined. The named converters that match the configuration
	 * property names are executed before any unnamed converters.
	 *
	 * @return the name of the converter or <code>null</code>.
	 */
	@Override
	default String getName() {
		return null;
	}

	/**
	 * Produces a injectable configuration value from the provided converter context.
	 *
	 * @param converterContext
	 *            the converter context
	 * @return the converted value or <code>null</code> if not valid
	 */
	T convert(TypeConverterContext converterContext);
}
