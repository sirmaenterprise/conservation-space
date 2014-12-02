package com.sirma.itt.emf.plugin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Provides a utility function for working with plugins
 * 
 * @author BBonev
 */
public class PluginUtil {

	/** The Constant logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginUtil.class);

	/**
	 * Instantiates a new plugin util.
	 */
	private PluginUtil() {
		// utility class
	}

	/**
	 * Parses the supported objects and creates a mapping of them by class. If class is already
	 * defined as supported by other extension the method could thrown an exception if needed.
	 * 
	 * @param <T>
	 *            the supportable element type
	 * @param <S>
	 *            the supportable instance type
	 * @param plugins
	 *            the plugin instances to iterate
	 * @param allowedDuplicates
	 *            the allowed duplicates is <code>true</code> the method will NOT throw an exception
	 *            if a class is defined by two or more extensions and will override the supported
	 *            extension with the latest in the list.
	 * @return a mapping of extensions by supported class.
	 * @throws EmfConfigurationException
	 *             if the allowedDuplicates is <code>false</code> and is found a class that is
	 *             provided in more then one extension. The exception will be thrown even the same
	 *             extension provides the class more then once also.
	 */
	public static <T, S extends Supportable<T>> Map<T, S> parseSupportedObjects(
			Iterable<S> plugins, boolean allowedDuplicates) throws EmfConfigurationException {
		Map<T, S> mapping = CollectionUtils.createLinkedHashMap(128);
		for (S supportable : plugins) {
			List<T> list = supportable.getSupportedObjects();
			if ((list == null) || list.isEmpty()) {
				LOGGER.warn("Provided invalid extension " + supportable.getClass()
						+ ". Should return supported objects!");
				continue;
			}
			for (T supportedObject : list) {
				if (mapping.containsKey(supportedObject)) {
					String msg = "Overriding the handling extension for " + supportedObject
							+ " with " + supportable;
					handleDuplicates(allowedDuplicates, msg);
				}
				mapping.put(supportedObject, supportable);
			}
		}
		return mapping;
	}

	/**
	 * Handle duplicates.
	 * 
	 * @param allowedDuplicates
	 *            the allowed duplicates
	 * @param msg
	 *            the msg
	 */
	private static void handleDuplicates(boolean allowedDuplicates, String msg) {
		if (allowedDuplicates) {
			LOGGER.warn(msg);
		} else {
			throw new EmfConfigurationException(msg);
		}
	}

	/**
	 * Parses the supported objects and creates a mapping of them by class. If class is already
	 * defined the new extension is added to the set for this class.
	 * 
	 * @param <T>
	 *            the supportable element type
	 * @param <S>
	 *            the supportable instance type
	 * @param plugins
	 *            the plugin instances to iterate
	 * @return a map with set of extensions for supported class.
	 */
	public static <T, S extends Supportable<T>> Map<T, Set<S>> parseSupportedObjects(
			Iterable<S> plugins) {
		Map<T, Set<S>> mapping = CollectionUtils.createLinkedHashMap(128);
		for (S s : plugins) {
			List<T> list = s.getSupportedObjects();
			if ((list == null) || list.isEmpty()) {
				LOGGER.warn("Provided invalid extension " + s.getClass()
						+ ". Should return supported objects!");
				continue;
			}
			for (T clazz : list) {
				if (!mapping.containsKey(clazz)) {
					mapping.put(clazz, new LinkedHashSet<S>());
				}
				if (mapping.get(clazz).contains(s)) {
					LOGGER.warn("Overriding " + s);
				}
				mapping.get(clazz).add(s);
			}
		}
		return mapping;
	}
}
