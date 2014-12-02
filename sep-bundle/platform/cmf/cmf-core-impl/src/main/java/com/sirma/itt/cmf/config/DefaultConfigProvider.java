package com.sirma.itt.cmf.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.ConfigProvider;

/**
 * Load the default settings. Priority is lowest.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class DefaultConfigProvider implements ConfigProvider {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigProvider.class);
	/** The config file path. */
	public static final String PROPERTIES_FILE_NAME = "config.properties";

	@Override
	public Properties getProperties() {
		Properties localProps = new Properties();
		InputStream resourceAsStream = DefaultConfigProvider.class
				.getResourceAsStream(PROPERTIES_FILE_NAME);
		try {
			localProps.load(resourceAsStream);
		} catch (Exception e) {
			LOGGER.warn("Failed to load the default internal {} configuration file",
					PROPERTIES_FILE_NAME, e);
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
		return 0;
	}

}
