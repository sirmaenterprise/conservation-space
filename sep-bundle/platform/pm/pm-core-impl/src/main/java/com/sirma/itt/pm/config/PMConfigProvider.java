package com.sirma.itt.pm.config;

import java.io.InputStream;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.configuration.ConfigProvider;

/**
 * Load the pm settings.Priority is higher than
 * {@link com.sirma.itt.cmf.config.DefaultConfigProvider} - 2, so to be processed later.
 * 
 * @author bbanchev
 */
public class PMConfigProvider implements ConfigProvider {
	/** The config file path. */
	public static final String PROPERTIES_FILE_NAME = "config.properties";
	@Inject
	private Logger logger;

	@Override
	public Properties getProperties() {
		Properties localProps = new Properties();
		try {
			InputStream resourceAsStream = PMConfigProvider.class
					.getResourceAsStream(PROPERTIES_FILE_NAME);
			localProps.load(resourceAsStream);
		} catch (Exception e) {
			logger.error("PM config could not be loaded!", e);
		}
		return localProps;
	}

	@Override
	public Integer getOrder() {
		return 2;
	}

}
