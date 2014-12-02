package com.sirma.itt.objects.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.configuration.ConfigProvider;

/**
 * Load the pm settings.Priority is higher than
 * {@link com.sirma.itt.cmf.config.DefaultConfigProvider} - 2, so to be processed later.
 * 
 * @author BBonev
 */
public class ObjectsConfigProvider implements ConfigProvider {
	private static final Logger LOGGER = Logger.getLogger(ObjectsConfigProvider.class);
	/** The config file path. */
	public static final String PROPERTIES_FILE_NAME = "config.properties";

	@Override
	public Properties getProperties() {
		Properties localProps = new Properties();
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = ObjectsConfigProvider.class
					.getResourceAsStream(PROPERTIES_FILE_NAME);
			localProps.load(resourceAsStream);
		} catch (Exception e) {
			LOGGER.warn("Failed to load properties file: " + PROPERTIES_FILE_NAME, e);
		} finally {
			if (resourceAsStream != null) {
				try {
					resourceAsStream.close();
				} catch (IOException e) {
					LOGGER.warn("Failed to close stream", e);
				}
			}
		}
		return localProps;
	}

	@Override
	public Integer getOrder() {
		return 4;
	}

}
