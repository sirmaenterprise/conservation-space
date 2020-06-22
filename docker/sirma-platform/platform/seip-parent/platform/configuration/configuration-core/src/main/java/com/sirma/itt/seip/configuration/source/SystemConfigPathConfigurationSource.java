package com.sirma.itt.seip.configuration.source;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.build.ConfigurationSource;
import com.sirma.itt.seip.configuration.event.ConfigurationReloadRequest;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Configuration provider that reads JVM configuration property to read an external properties file.
 * <p>
 * The search property is <code>config.path</code>
 *
 * @author BBonev
 */
@Extension(target = ConfigurationSource.NAME, order = 1000)
public class SystemConfigPathConfigurationSource implements ConfigurationSource {

	static final String CONFIG_PATH = "config.path";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private Properties localProps;

	@Override
	public Properties getConfigurations() {
		if (localProps == null) {
			localProps = new Properties();
			String configPathProprty = getPropertyValue();
			if (configPathProprty != null) {
				try (FileInputStream configStream = new FileInputStream(configPathProprty)) {
					localProps.load(configStream);
				} catch (FileNotFoundException e) {
					LOGGER.error("The configuration file {} was not found!", configPathProprty, e);
				} catch (IOException e) {
					LOGGER.error("Failed to read configuration file from {}", configPathProprty, e);
				}
			}
		}
		return localProps;
	}

	/**
	 * On configuration reload request clear the loaded properties so that they will be loaded again
	 *
	 * @param reloadRequest
	 *            the reload request
	 */
	public void onConfigurationReload(@Observes ConfigurationReloadRequest reloadRequest) {
		localProps = null;
	}

	/**
	 * Gets the property value.
	 *
	 * @return the property value
	 */
	@SuppressWarnings("static-method")
	protected String getPropertyValue() {
		String property = System.getProperty(CONFIG_PATH);
		if (property == null) {
			property = System.getProperty("cmf.config.path");
		}
		return property;
	}

	@Override
	public String getConfigurationValue(String key) {
		return getConfigurations().getProperty(key);
	}

	@Override
	public Properties getConfigurations(String tenantId) {
		Properties filtered = new Properties();
		getConfigurations()
				.entrySet()
					.stream()
					.filter((e) -> filterTenantKeys(tenantId, e.getKey().toString()))
					.forEach((e) -> filtered.put(e.getKey(), e.getValue()));
		return filtered;
	}

	private static boolean filterTenantKeys(String tenantId, String key) {
		if (key.indexOf('-') < 0) {
			return true;
		}
		return key.startsWith(tenantId);
	}

	@Override
	public String getConfigurationValue(String key, String tenantId) {
		String value = getConfigurations().getProperty(tenantId + '-' + key);
		if (value == null && (SecurityContext.isSystemTenant(tenantId) || SecurityContext.isDefaultTenant(tenantId))) {
			value = getConfigurations().getProperty(key);
		}
		return value;
	}

}
