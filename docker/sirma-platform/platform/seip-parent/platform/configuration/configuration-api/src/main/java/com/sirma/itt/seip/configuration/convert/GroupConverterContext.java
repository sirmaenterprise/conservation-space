package com.sirma.itt.seip.configuration.convert;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;

/**
 * Type converter context for group property converters. Provides access to all configuration properties defined in a
 * group configuration. Method that accepts this interface should be annotated with {@link ConfigurationConverter} and
 * should have a non void return.
 *
 * @author BBonev
 */
public interface GroupConverterContext extends TypeConverterContext {

	/**
	 * Gets the configuration names to all properties that are defined for the produces complex configuration object.
	 *
	 * @return the names
	 */
	String[] getNames();

	/**
	 * Gets the configuration definition instance for the given configuration name that is part of the group
	 * configuration.
	 *
	 * @param name
	 *            the name
	 * @return the configuration instance for the name.
	 */
	ConfigurationInstance getConfiguration(String name);

	/**
	 * Gets the configuration value for the given configuration name that is part of the group configuration. Provides
	 * access to the converted configuration value.
	 *
	 * @param <T>
	 *            the expected configuration type
	 * @param name
	 *            the name
	 * @return the configuration value.
	 */
	<T> ConfigurationProperty<T> getValue(String name);

	/**
	 * Gets the value from the configuration property identified by the given name. The method may return
	 * <code>null</code> if the configuration is not defined. Note that this method will produce
	 * {@link ConfigurationException} if the expected value is primitive and the value is <code>null</code> .<br>
	 * This method is short form of of <code>groupConverterContext.getValue(name).get();</code>
	 *
	 * @param <T>
	 *            the expected type
	 * @param name
	 *            the name
	 * @return the configuration value or <code>null</code>.
	 */
	<T> T get(String name);

	/**
	 * Provides access to the raw values that was used to build the complex configuration object.
	 *
	 * @param name
	 *            the name
	 * @return the raw values represented by the given {@link ConverterContext}.
	 */
	ConverterContext getRawValue(String name);
}
