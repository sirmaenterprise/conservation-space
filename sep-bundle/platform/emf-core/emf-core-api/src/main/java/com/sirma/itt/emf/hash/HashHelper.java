package com.sirma.itt.emf.hash;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;

/**
 * Helper class for performing hash computations over lists and sets
 * 
 * @author BBonev
 */
public class HashHelper {

	/** The Constant prime. */
	public static final int PRIME = 31;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(HashHelper.class);
	/** The Constant trace. */
	private static final boolean TRACE = LOGGER.isTraceEnabled();

	/**
	 * Instantiates a new hash helper.
	 */
	private HashHelper() {
		// utility class
	}

	/**
	 * Computes hash for string values.
	 * 
	 * @param current
	 *            the current hash code
	 * @param value
	 *            the value
	 * @param name
	 *            the name of the field or some other custom string used for logging purposes
	 * @return the int
	 */
	public static int computeHash(int current, String value, String name) {
		int result = (PRIME * current) + (StringUtils.isNullOrEmpty(value) ? 0 : value.hashCode());
		if (TRACE) {
			log(name, "#" + name + "(" + value + ")=" + result);
		}
		return result;
	}

	/**
	 * Computes hash for any other simple Objects.
	 * 
	 * @param current
	 *            the current hash code
	 * @param value
	 *            the value
	 * @param name
	 *            the name of the field or some other custom string used for logging purposes
	 * @return the int
	 */
	public static int computeHash(int current, Object value, String name) {
		if (value instanceof String) {
			return computeHash(current, (String) value, name);
		} else if (value instanceof Boolean) {
			return computeHash(current, (Boolean) value, name);
		}
		int result = (PRIME * current) + (value == null ? 0 : value.hashCode());
		if (TRACE) {
			log(name, "#" + name + "(" + value + ")=" + result);
		}
		return result;
	}

	/**
	 * Compute hash.
	 * 
	 * @param current
	 *            the current
	 * @param value
	 *            the value
	 * @param name
	 *            the name
	 * @return the int
	 */
	public static int computeHash(int current, Boolean value, String name) {
		if (value == null) {
			if (TRACE) {
				log(name, "#" + name + "(null)=0");
			}
			return 0;
		}
		int result = (PRIME * current) + (value ? 1231 : 1237);
		if (TRACE) {
			log(name, "#" + name + "(" + value + ")=" + result);
		}
		return result;
	}

	/**
	 * Compute hash for the given map and logs the result
	 * 
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param current
	 *            the current
	 * @param map
	 *            the collection
	 * @param calculator
	 *            the calculator
	 * @param name
	 *            the name
	 * @return the int
	 */
	public static <K, V> int computeHash(int current, Map<K, V> map, HashCalculator calculator,
			String name) {
		int result = current;
		if ((map != null) && !map.isEmpty()) {
			result += calculator.computeHash(map);
		}
		result = PRIME * result;
		if (TRACE) {
			log(name, "#" + name + "(MAP)=" + result);
		}
		return result;
	}

	/**
	 * Compute hash for the given collection and logs the result
	 * 
	 * @param current
	 *            the current
	 * @param collection
	 *            the collection
	 * @param calculator
	 *            the calculator
	 * @param name
	 *            the name
	 * @return the int
	 */
	public static int computeHash(int current, Collection<?> collection,
			HashCalculator calculator, String name) {
		int result = current;
		if ((collection != null) && !collection.isEmpty()) {
			result += calculator.computeHash(collection);
		}
		result = PRIME * result;
		if (TRACE) {
			log(name, "#" + name + "(COLLECTION)=" + result);
		}
		return result;
	}

	/**
	 * Logs the given message.
	 * 
	 * @param string
	 *            the string
	 */
	protected static void log(String string) {
		if (TRACE) {
			LOGGER.trace(string);
		}
	}

	/**
	 * Logs the given message.
	 * 
	 * @param fieldName
	 *            the field name
	 * @param string
	 *            the string
	 */
	protected static void log(String fieldName, String string) {
		log(string);
		if (Boolean.TRUE.equals(HashStatistics.isStatisticsEnabled())) {
			HashStatistics.getStatistics(false).add(new Pair<>(fieldName, string));
		}
	}

}
