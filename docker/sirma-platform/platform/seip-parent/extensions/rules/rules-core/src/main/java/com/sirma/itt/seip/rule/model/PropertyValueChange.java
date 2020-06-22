package com.sirma.itt.seip.rule.model;

/**
 * Defines the modes a property change that can occur
 *
 * @author BBonev
 */
public enum PropertyValueChange {

	/** Property value has been added. */
	ADDED, /** Property value has been removed. */
	REMOVED, /** Property value has been changed. */
	CHANGED;

	/**
	 * Parses the.
	 *
	 * @param object
	 *            the object
	 * @return the property value change
	 */
	public static PropertyValueChange parse(Object object) {
		if (object instanceof PropertyValueChange) {
			return (PropertyValueChange) object;
		} else if (object instanceof String) {
			return valueOf(object.toString().toUpperCase());
		} else if (object instanceof Integer) {
			Integer index = (Integer) object;
			if (index.intValue() >= 0 && index.intValue() < 3) {
				return values()[index.intValue()];
			}
		}
		return null;
	}
}
