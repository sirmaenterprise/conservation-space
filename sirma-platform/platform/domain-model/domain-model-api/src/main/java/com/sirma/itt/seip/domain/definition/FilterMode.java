package com.sirma.itt.seip.domain.definition;

/**
 * Enum to specify the possible values for filter comparison modes.
 *
 * @author BBonev
 */
public enum FilterMode {
	/** The has value. */
	HAS_VALUE("hasValue"), /** The is empty. */
	IS_EMPTY("isEmpty"), /** The contains. */
	CONTAINS("contains"), /** The equals. */
	EQUALS("equals"), /** The in. */
	IN("in");

	/** The value. */
	private final String value;

	/**
	 * Instantiates a new filter mode.
	 *
	 * @param v
	 *            the value
	 */
	private FilterMode(String v) {
		value = v;
	}

	/**
	 * Value.
	 *
	 * @return the string
	 */
	public String value() {
		return value;
	}

	/**
	 * From value.
	 *
	 * @param v
	 *            the value key
	 * @return the check types
	 */
	public static FilterMode fromValue(String v) {
		for (FilterMode c : FilterMode.values()) {
			if (c.value.equalsIgnoreCase(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
