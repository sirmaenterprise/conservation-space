package com.sirma.itt.seip.configuration.convert;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;

/**
 * {@link ConverterContext} for {@link ConfigurationGroupDefinition}s
 *
 * @author BBonev
 */
public class GroupContext implements GroupConverterContext {

	private final ConfigurationInstance instance;
	private final ConfigurationGroupDefinition groupDefinition;
	private final ConfigurationInstanceProvider configurationInstanceProvider;
	private final RawConfigurationAccessor rawConfigurationAccessor;
	private final ConfigurationProvider configurationProvider;

	/**
	 * Instantiates a new group context.
	 *
	 * @param instance
	 *            the instance
	 * @param groupDefinition
	 *            the group definition
	 * @param configurationInstanceProvider
	 *            the configuration instance provider
	 * @param rawConfigurationAccessor
	 *            the raw configuration accessor
	 * @param configurationProvider
	 *            the configuration provider
	 */
	public GroupContext(ConfigurationInstance instance, ConfigurationGroupDefinition groupDefinition,
			ConfigurationInstanceProvider configurationInstanceProvider,
			RawConfigurationAccessor rawConfigurationAccessor, ConfigurationProvider configurationProvider) {
		this.instance = instance;
		this.groupDefinition = groupDefinition;
		this.configurationInstanceProvider = configurationInstanceProvider;
		this.rawConfigurationAccessor = rawConfigurationAccessor;
		this.configurationProvider = configurationProvider;
	}

	@Override
	public ConfigurationInstance getConfiguration() {
		return instance;
	}

	@Override
	public String[] getNames() {
		return groupDefinition.properties();
	}

	@Override
	public ConfigurationInstance getConfiguration(String name) {
		return configurationInstanceProvider.getConfiguration(name);
	}

	@Override
	public <T> ConfigurationProperty<T> getValue(String name) throws ConfigurationException {
		return configurationProvider.getProperty(name);
	}

	@Override
	public ConverterContext getRawValue(String name) {
		ConfigurationInstance configuration = getConfiguration(name);
		return (ConverterContext) configuration.createConverterContext(configurationInstanceProvider,
				rawConfigurationAccessor, configurationProvider);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(String name) throws ConfigurationException {
		return (T) getValue(name).get();
	}

}
