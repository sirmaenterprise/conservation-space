package com.sirma.itt.seip.io;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The ResourceLoadUtil loads resources by name.
 */
public class ResourceLoadUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoadUtil.class);

	/**
	 * Load resource by first try to load the resource by filename (full, or root the classpath) or as fallback based on
	 * classloader.
	 *
	 * @param name
	 *            the name of resource
	 * @param fallbackLoader
	 *            the fallback loader
	 * @return the input stream or null on unavailability.
	 */
	public static InputStream loadResource(String name, Class<?> fallbackLoader) {
		Objects.requireNonNull(name, "Invalid file name is provided! Loading is interrupted!");
		File file = new File(name);
		try {
			if (file.exists()) {
				LOGGER.debug("Loading resoure from external file: " + file.getAbsolutePath());
				return new BufferedInputStream(new FileInputStream(file));
			} else if (file.isAbsolute()) {
				throw new EmfRuntimeException(file.getAbsolutePath() + " file is not found! Loading is interrupted!");
			}
		} catch (Exception e) {
			throw new EmfRuntimeException(name + " file loading is interrupted!", e);
		}
		if (fallbackLoader != null) {
			try {
				LOGGER.debug("Loading resoure from classpath file: " + file.getName());
				InputStream inputStream = fallbackLoader.getResourceAsStream(file.getName());
				if (inputStream != null) {
					return new BufferedInputStream(inputStream);
				}
			} catch (Exception e1) {
				throw new EmfRuntimeException(name + " file loading is interrupted!", e1);
			}
		}
		throw new EmfRuntimeException(name + " file is not found! Loading is interrupted!");
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
		try (InputStream loadResource = loadResource(name, fallbackLoader)) {
			Properties properties = new Properties();
			properties.load(loadResource);
			return properties;
		} catch (IOException e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Load scripts as resource located based on the given class.
	 *
	 * @param base
	 *            the base class for the resources to read
	 * @param resources
	 *            the resources the list of resources to read
	 * @return a collection of all resources read
	 */
	public static Collection<String> loadResources(Class<?> base, String... resources) {
		if (resources == null || resources.length == 0) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>(resources.length);
		for (int i = 0; i < resources.length; i++) {
			String path = resources[i];
			String loaded = loadResource(base, path);
			addNonNullValue(result, loaded);
		}
		return result;
	}

	/**
	 * Load scripts as resource located based on the given class.
	 *
	 * @param base
	 *            the base class for the resources to read
	 * @param path
	 *            the path of the resource to load
	 * @return a collection of all resources read
	 */
	@SuppressWarnings("resource")
	public static String loadResource(Class<?> base, String path) {
		if (StringUtils.isBlank(path)) {
			return null;
		}
		Class<?> baseClass = base;
		if (baseClass == null) {
			baseClass = ResourceLoadUtil.class;
		}
		InputStream stream = baseClass.getResourceAsStream(path);
		if (stream == null) {
			stream = baseClass.getClassLoader().getResourceAsStream(path);
		}
		if (stream == null) {
			return null;
		}
		try (Reader reader = new InputStreamReader(stream, UTF_8)) {
			return IOUtils.toString(reader);
		} catch (IOException e) {
			throw new EmfRuntimeException("Could not load resource " + path, e);
		}
	}
}
