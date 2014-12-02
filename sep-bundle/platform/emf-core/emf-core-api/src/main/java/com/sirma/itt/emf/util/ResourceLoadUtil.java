package com.sirma.itt.emf.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * The ResourceLoadUtil loads resources by name.
 */
public class ResourceLoadUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoadUtil.class);

	/**
	 * Load resource by first try to load the resource by filename (full, or root the classpath) or
	 * as fallback based on classloader.
	 * 
	 * @param name
	 *            the name of resource
	 * @param fallbackLoader
	 *            the fallback loader
	 * @return the input stream or null on unavailability.
	 */
	public static InputStream loadResource(String name, Class<?> fallbackLoader) {
		if (name == null) {
			throw new EmfConfigurationException(
					"Invalid file name is provided! Loading is interrupted!");
		}
		File file = new File(name);
		try {
			if (file.exists()) {
				LOGGER.debug("Loading resoure from external file: " + file.getAbsolutePath());
				return new BufferedInputStream(new FileInputStream(file));
			} else if (file.isAbsolute()) {
				throw new EmfConfigurationException(file.getAbsolutePath()
						+ " file is not found! Loading is interrupted!");
			}
		} catch (Exception e) {
			throw new EmfConfigurationException(name + " file loading is interrupted!", e);
		}
		if (fallbackLoader != null) {
			try {
				LOGGER.debug("Loading resoure from classpath file: " + file.getName());
				InputStream inputStream = fallbackLoader.getResourceAsStream(file.getName());
				if (inputStream != null) {
					return new BufferedInputStream(inputStream);
				}
			} catch (Exception e1) {
				throw new EmfConfigurationException(name + " file loading is interrupted!", e1);
			}
		}
		throw new EmfConfigurationException(name + " file is not found! Loading is interrupted!");
	}

	/**
	 * Load properties from resource by using.
	 * 
	 * @param name
	 *            the name
	 * @param fallbackLoader
	 *            the fallback loader
	 * @return the properties loaded, or empty on error {@link #loadResource(String, Class)}
	 */
	public static Properties loadProperties(String name, Class<?> fallbackLoader) {
		InputStream loadResource = null;
		try {
			loadResource = loadResource(name, fallbackLoader);
			Properties properties = new Properties();
			properties.load(loadResource);
			return properties;
		} catch (EmfConfigurationException e) {
			throw e;
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		} finally {
			if (loadResource != null) {
				try {
					loadResource.close();
				} catch (Exception e) {
					LOGGER.warn("Failed to close resource stream", e);
				}
			}
		}
	}
}
