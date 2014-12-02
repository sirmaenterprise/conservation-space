package com.sirma.itt.emf.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.emf.util.CollectionUtils;

/**
 * A factory for creating Configuration objects.
 */
@ApplicationScoped
public class ConfigurationFactory implements SystemConfiguration {

	/** The Constant PROP_SPLIT_PATTERN. */
	private static final Pattern PROP_SPLIT_PATTERN = Pattern.compile("\\s*[,;]\\s*");

	/** The config properties. */
	private Properties configProperties;

	@Inject
	private Logger LOGGER;

	@Inject
	private Instance<ConfigProvider> configurations;

	/**
	 * Gets the properties from all providers.
	 * 
	 * @return the properties
	 */
	public synchronized Properties getProperties() {
		if (configProperties == null) {
			configProperties = new Properties();
			LOGGER.info("Loading configurations for: " + configurations);
			if (configurations == null) {
				return configProperties;
			}
			// iterate all providers
			ArrayList<ConfigProvider> providers = new ArrayList<ConfigProvider>();
			Iterator<ConfigProvider> iterator = configurations.iterator();
			while (iterator.hasNext()) {
				ConfigProvider configProvider = iterator.next();
				providers.add(configProvider);
			}
			// sort by order
			Collections.sort(providers, new Comparator<ConfigProvider>() {
				@Override
				public int compare(ConfigProvider o1, ConfigProvider o2) {
					return o1.getOrder().compareTo(o2.getOrder());
				}
			});
			for (ConfigProvider configProvider : providers) {
				LOGGER.info("Loading configurations from: " + configProvider.getClass().getName());
				configProperties.putAll(configProvider.getProperties());
			}
			providers = null;
		}
		return configProperties;
	}

	/**
	 * Gets the configuration for string
	 * 
	 * @param injectionPoint
	 *            the injection point
	 * @return the configuration
	 */
	@Produces
	@Config
	public String getConfiguration(InjectionPoint injectionPoint) {
		Properties config = getProperties();
		Config annotation = injectionPoint.getAnnotated().getAnnotation(Config.class);
		String key = annotation.name();
		String defaultValue = annotation.defaultValue();
		if (StringUtils.isEmpty(defaultValue)) {
			defaultValue = null;
		}
		if (StringUtils.isNotEmpty(key)) {
			String value = config.getProperty(key);
			// ensure that the value is not an empty string if so we consider it
			// for not set
			if (StringUtils.isEmpty(value)) {
				return defaultValue;
			}
			return value;
		}
		// if the key is not specified we will search for other options
		String configKey = injectionPoint.getMember().getDeclaringClass().getName() + "."
				+ injectionPoint.getMember().getName();
		if (config.getProperty(configKey) == null) {
			configKey = injectionPoint.getMember().getDeclaringClass().getSimpleName() + "."
					+ injectionPoint.getMember().getName();
			if (config.getProperty(configKey) == null) {
				configKey = injectionPoint.getMember().getName();
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Config key= " + configKey + " value = " + config.getProperty(configKey));
		}
		return config.getProperty(configKey, defaultValue);
	}

	@Override
	public String getConfiguration(String key, String defaultValue) {
		return getProperties().getProperty(key, defaultValue);
	}

	@Override
	public String getConfiguration(String key) {
		return getProperties().getProperty(key);
	}

	/**
	 * Gets the configuration for integer
	 * 
	 * @param p
	 *            the injection point
	 * @return the configuration int
	 */
	@Produces
	@Config
	public int getConfigurationInt(InjectionPoint p) {
		return Integer.parseInt(getConfiguration(p));
	}

	/**
	 * Gets the configuration for Long
	 * 
	 * @param p
	 *            the injection point
	 * @return the configuration long
	 */
	@Produces
	@Config
	public long getConfigurationLong(InjectionPoint p) {
		return Long.parseLong(getConfiguration(p));
	}

	/**
	 * Gets the configuration for double.
	 * 
	 * @param p
	 *            the injection point
	 * @return the configuration double
	 */
	@Produces
	@Config
	public Double getConfigurationDouble(InjectionPoint p) {
		String val = getConfiguration(p);
		return Double.parseDouble(val);
	}

	/**
	 * Gets a directory file based on the configured path
	 * 
	 * @param p
	 *            the injection point
	 * @return the file detonated by the configured path or <code>null</code> if not directory
	 */
	@Produces
	@Config
	public File getConfigurationFile(InjectionPoint p) {
		String configuration = getConfiguration(p);
		File directory = null;
		if (configuration != null) {
			directory = new File(configuration);
			if (!directory.isDirectory()) {
				directory = null;
			}
		}
		return directory;
	}

	/**
	 * Gets a configuration property that has a list of values
	 * 
	 * @param p
	 *            the injection point
	 * @return the list configuration
	 */
	@Produces
	@Config
	public Set<String> getListConfiguration(InjectionPoint p) {
		String string = getConfiguration(p);
		if (string == null) {
			return Collections.emptySet();
		}
		String[] split = PROP_SPLIT_PATTERN.split(string);
		Set<String> result = new LinkedHashSet<String>();
		for (String prop : split) {
			if (!prop.isEmpty()) {
				result.add(prop);
			}
		}
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Gets the boolean configuration parameter.
	 * 
	 * @param p
	 *            the p
	 * @return the boolean configuration
	 */
	@Produces
	@Config
	public Boolean getBooleanConfig(InjectionPoint p) {
		String conf = getConfiguration(p);
		return Boolean.valueOf(conf);
	}

	@Override
	public Set<String> getConfigurationKeys() {
		Set<Object> keySet = getProperties().keySet();
		Set<String> keys = CollectionUtils.createHashSet(keySet.size());
		for (Object key : keySet) {
			keys.add(key.toString());
		}
		return Collections.unmodifiableSet(keys);
	}
}