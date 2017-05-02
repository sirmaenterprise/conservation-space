package com.sirma.itt.seip.help;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.seip.help.exception.UserhelpException;

/**
 * The PropertyConfigsWrapper load the app properties from the root 'config.properties' and as addition stores them
 * after runtime change
 */
public class PropertyConfigsWrapper {
	/** Configuration instance. */
	public static final PropertyConfigsWrapper INSTANCE = new PropertyConfigsWrapper();

	/** The properties. */
	private Properties properties;

	/**
	 * Init and throw exception on file not found
	 */
	private PropertyConfigsWrapper() {
		properties = new Properties();

		File config = null;
		String externalConfig = System.getProperty("config.help.properties");
		String configFileName = "config.properties";
		if (externalConfig != null) {
			config = new File(externalConfig);
		} else {
			config = new File(configFileName);
		}
		InputStream inStream = null;
		try {

			if (!config.exists()) {
				inStream = PropertyConfigsWrapper.class.getResourceAsStream(configFileName);
			} else {
				inStream = new FileInputStream(config);
			}
			properties.load(inStream);
		} catch (Exception e) {
			throw new UserhelpException("Failed to read properties!", e);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
	}

	/**
	 * Gets the property from the main config.
	 *
	 * @param key
	 *            the key
	 * @return the property
	 */
	public String getProperty(String key) {
		Object value = properties.get(key);
		if (value != null) {
			return value.toString();
		}
		return "";
	}

	/**
	 * Gets the property from the main config.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the property
	 */
	public String put(String key, String value) {
		properties.put(key, value);
		return (String) properties.get(key);
	}

}