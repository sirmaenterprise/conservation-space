package com.sirma.itt.seip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * The PropertyConfigsWrapper load the app properties from the root 'config.properties' and as addition stores them
 * after runtime change
 */
public class PropertyConfigsWrapper {

	/** The Constant CONFIG_INPUT_LAST_SITEID. */
	public static final String CONFIG_INPUT_LAST_SITEID = "input.last.siteid";
	/** The Constant CONFIG_INPUT_EMF_HOST. */
	public static final String CONFIG_INPUT_EMF_HOST = "input.emf.host";
	/** The Constant CONFIG_INPUT_EMF_HOST. */
	public static final String CONFIG_INPUT_SOLR_HOST = "solr.host";
	/** The Constant CONFIG_INPUT_LAST_SITEID. */
	public static final String CONFIG_INPUT_LAST_CHECKS = "input.last.checks";
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
		FileInputStream inStream = new FileInputStream("config.properties");
		properties.load(inStream);
		inStream.close();
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
	 * @param defaultValue
	 *            the default value
	 * @return the property
	 */
	public String getProperty(String key, String defaultValue) {
		Object value = properties.get(key);
		if (value != null) {
			return value.toString();
		}
		return defaultValue;
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

	/**
	 * Store.
	 */
	public void store() {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("config.properties");
			properties.store(fileOutputStream, "Updated according to user preferences");
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the version from maven artifact.
	 *
	 * @return the version
	 */
	public String getVersion() {
		try {
			InputStream resource = PropertyConfigsWrapper.class.getClassLoader().getResourceAsStream(
					"META-INF/maven/com.sirma.itt.emf/sep-admin-tool/pom.properties");
			if (resource != null) {
				Properties properties = new Properties();
				properties.load(resource);
				Object version = properties.get("version");
				return version.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
