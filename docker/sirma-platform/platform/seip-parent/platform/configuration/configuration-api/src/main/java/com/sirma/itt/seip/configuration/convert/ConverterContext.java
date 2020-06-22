package com.sirma.itt.seip.configuration.convert;

import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;

/**
 * Type converter context for single property converters. Provides a single raw value for the converted configuration
 * and the default value if any. Method that accepts this interface should be annotated with
 * {@link ConfigurationConverter} and should have a non void return.
 *
 * @author BBonev
 */
public interface ConverterContext extends TypeConverterContext {

	/**
	 * Gets the raw value to convert or the default value if the value is <code>null</code> .
	 *
	 * @return the raw value, the default value or <code>null</code>
	 */
	String getRawValue();

	/**
	 * Gets the default value or <code>null</code> if not specified.
	 *
	 * @return the default value or <code>null</code>
	 */
	String getDefaultValue();

}
