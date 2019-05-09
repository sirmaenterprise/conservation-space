package com.sirma.sep.model.management.definition;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds mapping between type option and type validation regex
 *
 * @author Stella Djulgerova
 */
class TypeOptions {

	private static final Map<String, String> TYPE_REGEX_MAP = new HashMap<>(11);

	static {
		TYPE_REGEX_MAP.put("ALPHA_NUMERIC_FIXED_TYPE", "AN\\d+");
		TYPE_REGEX_MAP.put("ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE", "\\bAN..\\b\\d+");
		TYPE_REGEX_MAP.put("ALPHA_NUMERIC_TYPE", "ANY");
		TYPE_REGEX_MAP.put("FLOATING_POINT_FIXED_TYPE", "N\\d{1,2}[\\,\\.]\\d{1,2}");
		TYPE_REGEX_MAP.put("FLOATING_POINT_TYPE", "N..\\d{1,2}[\\,\\.]\\d{1,2}");
		TYPE_REGEX_MAP.put("NUMERIC_FIXED_TYPE", "N\\d+");
		TYPE_REGEX_MAP.put("NUMERIC_TYPE", "\\bN..\\b\\d+");
		TYPE_REGEX_MAP.put("DATETIME_TYPE", "DATETIME");
		TYPE_REGEX_MAP.put("DATE_TYPE", "DATE");
		TYPE_REGEX_MAP.put("BOOLEAN", "BOOLEAN");
		TYPE_REGEX_MAP.put("URI", "URI");
	}

	/**
	 * Resolve type option by given type
	 * 
	 * @param type the type value (can be an..180, n20, etc..)
	 * @param codeList codeList value or null if such attribute is missing
	 * @return corresponding type option (can be ALPHA_NUMERIC_TYPE, NUMERIC_FIXED_TYPE, etc..)
	 */
	public static String resolveTypeOption(String type, Integer codeList) {
		if (type == null) {
			return null;
		}
		if (codeList != null) {
			return "CODELIST";
		}
		return TYPE_REGEX_MAP.entrySet()
				.stream()
				.filter(e -> type.toUpperCase().matches(e.getValue()))
				.map(Map.Entry::getKey)
				.findFirst().orElse(null);
	}

	private TypeOptions() {
		// Preventing instantiation.
	}

}
