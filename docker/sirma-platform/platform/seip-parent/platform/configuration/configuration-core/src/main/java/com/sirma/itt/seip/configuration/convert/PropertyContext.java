package com.sirma.itt.seip.configuration.convert;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;

/**
 * {@link ConverterContext} for single property definition that is passed to configuration value converters.
 *
 * @author BBonev
 */
public class PropertyContext implements ConverterContext {

	/** The instance. */
	private final ConfigurationInstance instance;
	/** The default value. */
	private final String defaultValue;
	/** The raw configuration accessor. */
	private final RawConfigurationAccessor rawConfigurationAccessor;

	/**
	 * Instantiates a new property converter context.
	 *
	 * @param instance
	 *            the instance
	 * @param defaultValue
	 *            the default value
	 * @param rawConfigurationAccessor
	 *            the raw configuration accessor
	 */
	public PropertyContext(ConfigurationInstance instance, String defaultValue,
			RawConfigurationAccessor rawConfigurationAccessor) {
		this.instance = instance;
		this.defaultValue = StringUtils.trimToNull(defaultValue);
		this.rawConfigurationAccessor = rawConfigurationAccessor;
	}

	/**
	 * Gets the configuration.
	 *
	 * @return the configuration
	 */
	@Override
	public ConfigurationInstance getConfiguration() {
		return instance;
	}

	/**
	 * Gets the raw value.
	 *
	 * @return the raw value
	 */
	@Override
	public String getRawValue() {
		String configurationValue = rawConfigurationAccessor.getRawConfigurationValue(getConfiguration().getName());
		if (configurationValue == null) {
			return getDefaultValue();
		}
		return configurationValue;
	}

	/**
	 * Gets the default value.
	 *
	 * @return the default value
	 */
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

}
