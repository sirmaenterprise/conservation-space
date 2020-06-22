package com.sirma.itt.seip.rule.model;

/**
 * Defines possible behavior when configuring what a rule should do when finds a duplicates.
 *
 * @author BBonev
 */
public enum OnDuplicateOperation {

	/** The concatenate values */
	CONCATENATE, /** The skip value */
	SKIP, /** The override value */
	OVERRIDE;

	/**
	 * Parses the.
	 *
	 * @param object
	 *            the object
	 * @return the on duplicate operation
	 */
	public static OnDuplicateOperation parse(Object object) {
		if (object == null) {
			return SKIP;
		}
		for (int i = 0; i < values().length; i++) {
			OnDuplicateOperation value = values()[i];
			if (value.toString().equalsIgnoreCase(object.toString())) {
				return value;
			}
		}
		return SKIP;
	}
}