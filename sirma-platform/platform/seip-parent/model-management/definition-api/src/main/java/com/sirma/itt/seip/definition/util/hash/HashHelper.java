package com.sirma.itt.seip.definition.util.hash;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.definition.AllowedChildrenModel;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.StateTransitionalModel;
import com.sirma.itt.seip.definition.Transitional;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.Controllable;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;

/**
 * Helper class for performing hash computations over lists and sets
 *
 * @author BBonev
 */
public class HashHelper {

	private static final String ALLOWED_CHILDREN = "/AllowedChildren";
	private static final String STATE_TRANSITIONS = "/StateTransitions";
	private static final String TRANSITIONS = "/Transitions";
	private static final String REGIONS = "/Regions";
	private static final String FIELDS = "/Fields";
	private static final String CONFIGURATIONS = "/Configurations";
	private static final String CONDITIONS = "/Conditions";

	/** The Constant prime. */
	public static final int PRIME = 31;

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
		int result = PRIME * current + (StringUtils.isBlank(value) ? 0 : value.hashCode());
		log(name, value, result);
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
		int result = PRIME * current + (value == null ? 0 : value.hashCode());
		log(name, value, result);
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
			log(name, "null", current);
			return current;
		}
		int result = PRIME * current + (value.booleanValue() ? 1231 : 1237);
		log(name, value, result);
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
	public static <K, V> int computeHash(int current, Map<K, V> map, HashCalculator calculator, String name) {
		int result = current;
		if (map != null && !map.isEmpty()) {
			result += calculator.computeHash(map).intValue();
		}
		result = PRIME * result;
		log(name, "MAP", result);
		return result;
	}

	/**
	 * Compute hash for the given collection and logs the result. The method does not take into account the order of the
	 * given collection.
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
	public static int computeHash(int current, Collection<?> collection, HashCalculator calculator, String name) {
		return computeHash(current, collection, calculator, name, false);
	}

	/**
	 * Compute hash for the given collection and logs the result. The method takes into account the collection order if
	 * the last argument considerOrder=true
	 *
	 * @param current
	 *            the current
	 * @param collection
	 *            the collection
	 * @param calculator
	 *            the calculator
	 * @param name
	 *            the name
	 * @param considerOrder
	 *            the consider order
	 * @return the int
	 */
	public static int computeHash(int current, Collection<?> collection, HashCalculator calculator, String name,
			boolean considerOrder) {
		int result = current;
		int multiplyer = considerOrder ? PRIME : 1;
		if (collection != null && !collection.isEmpty()) {
			result = multiplyer * result + calculator.computeHash(collection).intValue();
		}
		result = PRIME * result;
		log(name, "COLLECTION", result);
		return result;
	}

	/**
	 * Logs a string constructed from the field, the value and the corresponding hash.
	 *
	 * @param field
	 *            the field
	 * @param value
	 *            the value
	 * @param hash
	 *            the hash
	 */
	public static void log(String field, Object value, int hash) {
		if (isLoggingEnabled()) {
			StringBuilder builder = new StringBuilder(128);
			builder.append("#").append(field).append("(").append(value).append(")=").append(hash);
			log(builder.toString());
		}
	}

	/**
	 * Logs the given message.
	 * @param string
	 *            the string
	 */
	private static void log(String string) {
		if (HashStatistics.isStatisticsEnabled()) {
			HashStatistics.getStatistics().add(string);
		}
	}

	/**
	 * Checks if is logging enabled.
	 *
	 * @return true, if is logging enabled
	 */
	private static boolean isLoggingEnabled() {
		return HashStatistics.isStatisticsEnabled();
	}

	/**
	 * Compute generic definition interfaces.
	 *
	 * @param current
	 *            the current
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the updated hash if source object is any of the checked interfaces
	 */
	public static int computeGenericDefinitionInterfaces(int current, Object definition, HashCalculator calculator) {
		int result = current;
		String path = "";
		if (definition instanceof PathElement) {
			path = PathHelper.getPath((PathElement) definition);
		}

		if (definition instanceof GenericDefinition) {
			result = HashHelper.computeHash(result, ((GenericDefinition) definition).getConfigurations(), calculator,
					path + CONFIGURATIONS);
		}
		if (definition instanceof DefinitionModel) {
			result = HashHelper.computeHash(result, ((DefinitionModel) definition).getFields(), calculator,
					path + FIELDS);
		}
		if (definition instanceof RegionDefinitionModel) {
			result = HashHelper.computeHash(result, ((RegionDefinitionModel) definition).getRegions(), calculator,
					path + REGIONS);
		}
		if (definition instanceof Transitional) {
			result = HashHelper.computeHash(result, ((Transitional) definition).getTransitions(), calculator,
					path + TRANSITIONS);
		}
		if (definition instanceof StateTransitionalModel) {
			result = HashHelper.computeHash(result, ((StateTransitionalModel) definition).getStateTransitions(),
					calculator, path + STATE_TRANSITIONS, true);
		}
		if (definition instanceof AllowedChildrenModel) {
			result = HashHelper.computeHash(result, ((AllowedChildrenModel) definition).getAllowedChildren(),
					calculator, path + ALLOWED_CHILDREN);
		}
		if (definition instanceof Conditional) {
			result = HashHelper.computeHash(result, ((Conditional) definition).getConditions(), calculator,
					path + CONDITIONS);
		}
		if (definition instanceof Controllable) {
			result = result * PRIME
					+ calculator.computeHash(((Controllable) definition).getControlDefinition()).intValue();
			log(path + "/CONTROL", "CONTROL", result);
		}
		return result;
	}

}
