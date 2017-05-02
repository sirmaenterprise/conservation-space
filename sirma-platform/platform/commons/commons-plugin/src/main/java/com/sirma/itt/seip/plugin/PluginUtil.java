package com.sirma.itt.seip.plugin;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Provides a utility function for working with plugins
 *
 * @author BBonev
 */
public class PluginUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Instantiates a new plugin util.
	 */
	private PluginUtil() {
		// utility class
	}

	/**
	 * Parses the supported objects and creates a mapping of them by class. If class is already defined as supported by
	 * other extension the method could thrown an exception if needed.
	 *
	 * @param <T>
	 *            the supportable element type
	 * @param <S>
	 *            the supportable instance type
	 * @param plugins
	 *            the plugin instances to iterate
	 * @param allowedDuplicates
	 *            the allowed duplicates is <code>true</code> the method will NOT throw an exception if a class is
	 *            defined by two or more extensions and will override the supported extension with the latest in the
	 *            list.
	 * @return a mapping of extensions by supported class.
	 */
	public static <T, S extends Supportable<T>> Map<T, S> parseSupportedObjects(Iterable<S> plugins,
			boolean allowedDuplicates) {
		Map<T, S> mapping = CollectionUtils.createLinkedHashMap(128);
		for (S supportable : plugins) {
			List<T> list = supportable.getSupportedObjects();
			if (isEmpty(list)) {
				LOGGER.warn("Provided invalid extension {}. Should return supported objects!", supportable.getClass());
				continue;
			}
			for (T supportedObject : list) {
				if (mapping.containsKey(supportedObject)) {
					String msg = "Overriding the handling extension for " + supportedObject + " with " + supportable;
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
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Parses the supported objects and creates a mapping of them by class. If class is already defined the new
	 * extension is added to the set for this class.
	 *
	 * @param <T>
	 *            the supportable element type
	 * @param <S>
	 *            the supportable instance type
	 * @param plugins
	 *            the plugin instances to iterate
	 * @return a map with set of extensions for supported class.
	 */
	public static <T, S extends Supportable<T>> Map<T, Set<S>> parseSupportedObjects(Iterable<S> plugins) {
		Map<T, Set<S>> mapping = CollectionUtils.createLinkedHashMap(128);
		for (S s : plugins) {
			List<T> list = s.getSupportedObjects();
			if (isEmpty(list)) {
				LOGGER.warn("Provided invalid extension {}. Should return supported objects!", s.getClass());
				continue;
			}
			for (T clazz : list) {
				if (!mapping.containsKey(clazz)) {
					mapping.put(clazz, new LinkedHashSet<S>());
				}
				if (mapping.get(clazz).contains(s)) {
					LOGGER.warn("Overriding {}", s);
				}
				mapping.get(clazz).add(s);
			}
		}
		return mapping;
	}
}
