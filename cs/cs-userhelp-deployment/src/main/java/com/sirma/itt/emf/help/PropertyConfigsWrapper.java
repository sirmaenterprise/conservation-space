package com.sirma.itt.emf.help;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * The PropertyConfigsWrapper load the app properties from the root 'config.properties' and as
 * addition stores them after runtime change
 */
public class PropertyConfigsWrapper {

	private static PropertyConfigsWrapper PROPERTIES;
	/** The properties. */
	private Properties properties;

	/**
	 * Init and throw exception on file not found
	 *
	 * @throws Exception
	 */
	private PropertyConfigsWrapper() throws Exception {
		properties = new Properties();
		File config = new File("config.properties");
		InputStream inStream = null;
		if (!config.exists()) {
			inStream = PropertyConfigsWrapper.class.getResourceAsStream("config.properties");
		} else {
			inStream = new FileInputStream(config);
		}
		properties.load(inStream);
		IOUtils.closeQuietly(inStream);
	}

	/**
	 * Gets the configured instance.
	 *
	 * @return the instance of wrapper initialized
	 */
	public static PropertyConfigsWrapper getInstance() {
		if (PROPERTIES == null) {
			try {
				PROPERTIES = new PropertyConfigsWrapper();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return PROPERTIES;
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