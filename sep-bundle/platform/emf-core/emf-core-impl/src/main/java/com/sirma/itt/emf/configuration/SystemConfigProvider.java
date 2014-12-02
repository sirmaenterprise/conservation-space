package com.sirma.itt.emf.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Load the java args <code>-Dconfig.path</code> provided config. Higher priority is set (100) so
 * loading to be processed as final step.
 *
 * @author bbanchev
 * @author BBonev
 */
public class SystemConfigProvider implements ConfigProvider {

	/** The logger. */
	private static final Logger LOGGER = Logger.getLogger(SystemConfigProvider.class);

	@Override
	public Properties getProperties() {
		Properties localProps = new Properties();
		String configPathProprty = getPropertyValue();
		if (configPathProprty != null) {
			FileInputStream configStream = null;
			try {
				configStream = new FileInputStream(configPathProprty);
				localProps.load(configStream);
			} catch (FileNotFoundException e) {
				LOGGER.error("The configuration file " + configPathProprty + " was not found!");
			} catch (IOException e) {
				LOGGER.error("Failed to read configuration file from " + configPathProprty, e);
			} finally {
				if (configStream != null) {
					try {
						configStream.close();
					} catch (IOException e) {
						LOGGER.warn("Failed to close stream " + configPathProprty, e);
					}
				}
			}
		}
		return localProps;
	}

	/**
	 * Gets the property value.
	 *
	 * @return the property value
	 */
	protected String getPropertyValue() {
		return System.getProperty("config.path");
	}

	@Override
	public Integer getOrder() {
		return 100;
	}
}
