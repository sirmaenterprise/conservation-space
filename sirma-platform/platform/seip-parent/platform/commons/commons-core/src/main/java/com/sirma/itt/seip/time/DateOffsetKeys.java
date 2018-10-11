package com.sirma.itt.seip.time;

/**
 * Represents keys for date offsets used in {@link DateOffset}. If the need occurs, it can be extended with more
 * offsets.
 *
 * @author Mihail Radkov
 */
public interface DateOffsetKeys {

	/**
	 * Constant key for year offset.
	 */
	String YEAR = "yearOffset";

	/**
	 * Constant key for day offset.
	 */
	String DAY = "dayOffset";

	/**
	 * Constant key for hour offset.
	 */
	String HOUR = "hourOffset";

	/**
	 * Constant key for minute offset.
	 */
	String MINUTE = "minuteOffset";

	/**
	 * Constant key for second offset.
	 */
	String SECOND = "secondOffset";

	/**
	 * Constant key for millisecond offset.
	 */
	String MS = "msOffset";
}
