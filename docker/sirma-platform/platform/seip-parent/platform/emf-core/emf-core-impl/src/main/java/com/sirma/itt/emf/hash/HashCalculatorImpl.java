package com.sirma.itt.emf.hash;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.definition.util.hash.HashCalculatorExtension;
import com.sirma.itt.seip.definition.util.hash.HashStatistics;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;

/**
 * Default {@link HashCalculator} implementation
 *
 * @author BBonev
 */
@ApplicationScoped
public class HashCalculatorImpl implements HashCalculator {

	private static final Integer ZERO = Integer.valueOf(0);

	/** The extension. */
	@Inject
	@ExtensionPoint(HashCalculatorExtension.TARGET_NAME)
	private Iterable<HashCalculatorExtension> extensions;

	/** The mapping. */
	private Map<Class<?>, HashCalculatorExtension> mapping;

	/**
	 * Initialize mapping.
	 */
	@PostConstruct
	public void initializeMapping() {
		mapping = PluginUtil.parseSupportedObjects(extensions, true);
	}

	@Override
	public Integer computeHash(Object object) {
		return computeInternal(object);
	}

	/**
	 * Compute the hash code for the given object.
	 *
	 * @param object
	 *            the object
	 * @return the integer
	 */
	private Integer computeInternal(Object object) {
		if (object == null) {
			return ZERO;
		}
		if (object instanceof Collection) {
			return computeCollectionHashCode((Collection<?>) object);
		} else if (object instanceof Map) {
			return computeMapHashCode((Map<?, ?>) object);
		}
		HashCalculatorExtension extension = findSupportedExtension(object.getClass());
		if (extension != null) {
			return extension.computeHash(this, object);
		}
		// if we can't handle it, return the default
		return object.hashCode();
	}

	/**
	 * Compute map hash code.
	 *
	 * @param map
	 *            the map
	 * @return the integer
	 */
	private Integer computeMapHashCode(Map<?, ?> map) {
		if (map.isEmpty()) {
			return map.hashCode();
		}
		int result = 0;
		for (Entry<?, ?> o : map.entrySet()) {
			result += computeInternal(o.getKey()) + computeInternal(o.getValue());
		}
		return result;
	}

	/**
	 * Compute collection hash code.
	 *
	 * @param object
	 *            the object
	 * @return the integer
	 */
	private Integer computeCollectionHashCode(Collection<?> collection) {
		if (collection.isEmpty()) {
			return collection.hashCode();
		}
		// if there is an extension that can handle the element objects we calculate the
		// hash code by the sum of the hash codes of all elements.
		int result = 0;
		for (Object o : collection) {
			result += computeInternal(o);
		}
		return result;
	}

	/**
	 * Find supported extension in the hierarchy of the given class
	 *
	 * @param clazz
	 *            the clazz
	 * @return the hash calculator extension or <code>null</code> if not found proper mapping
	 */
	private HashCalculatorExtension findSupportedExtension(Class<?> clazz) {
		Class<?> currentClass = clazz;
		// direct mapping for current class
		HashCalculatorExtension extension = getExtension(currentClass);
		if (extension != null) {
			return extension;
		}

		// find via supported interfaces of value
		do {
			Class<?>[] ifClasses = clazz.getInterfaces();
			for (Class<?> ifClasse : ifClasses) {
				extension = getExtension(ifClasse);
				if (extension != null) {
					return extension;
				}
			}
			currentClass = currentClass.getSuperclass();
		} while (currentClass != null);
		return extension;
	}

	/**
	 * Gets the extension for the given class if registered.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the extension
	 */
	private HashCalculatorExtension getExtension(Class<?> clazz) {
		return mapping.get(clazz);
	}

	@Override
	public boolean equalsByHash(Object object1, Object object2) {
		if (object1 == object2) {
			// the same reference no need to check anything
			return true;
		}
		Integer hash1 = computeInternal(object1);
		Integer hash2 = computeInternal(object2);
		return hash1 != null && hash2 != null && hash1.compareTo(hash2) == 0;
	}

	@Override
	public void setStatisticsEnabled(boolean enabled) {
		HashStatistics.setStatisticsEnabled(enabled);
	}

	@Override
	public List<String> getStatistics() {
		return HashStatistics.getStatisticsAndReset();
	}

}
