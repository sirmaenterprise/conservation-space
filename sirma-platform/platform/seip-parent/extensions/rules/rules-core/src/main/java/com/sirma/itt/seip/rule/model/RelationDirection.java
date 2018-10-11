package com.sirma.itt.seip.rule.model;

/**
 * Defines relation possible directions.
 *
 * @author BBonev
 */
public enum RelationDirection {

	OUTGOING, INGOING;

	/**
	 * Parses the.
	 *
	 * @param object
	 *            the object
	 * @return the relation direction
	 */
	public static RelationDirection parse(Object object) {
		if (object instanceof RelationDirection) {
			return (RelationDirection) object;
		} else if (object instanceof String) {
			return valueOf(object.toString().toUpperCase());
		} else if (object instanceof Integer) {
			int index = ((Integer) object).intValue();
			if (index >= 0 && index < values().length) {
				return values()[index];
			}
		}
		return null;
	}
}
