package com.sirma.itt.commons.utils.date;

/**
 * Enumeration is used for stripping time periods from date.
 * 
 * @author SKostadinov
 */
public enum DateStripType {
	STRIP_MILLISECOND(1), STRIP_SECOND(2), STRIP_MINUTE(3), STRIP_HOUR(4), STRIP_DAY(
			5), STRIP_MONTH(6), STRIP_YEAR(7);
	private final int weight;

	/**
	 * Private constructor.
	 * 
	 * @param weight
	 *            is the weight of the stripping type
	 */
	private DateStripType(int weight) {
		this.weight = weight;
	}

	/**
	 * Getter method for weight.
	 * 
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}
}