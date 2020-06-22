package com.sirma.itt.seip.time;

import java.util.Date;

import com.sirma.itt.seip.Pair;

/**
 * Helper class that represents a date range. It has start and end date and means of checking if a date is in the range.
 *
 * @author BBonev
 */
public class DateRange extends Pair<Date, Date> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3203526554805113786L;

	/**
	 * Instantiates a new date range.
	 *
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 */
	public DateRange(Date first, Date second) {
		super(first, second);
	}

	/**
	 * Check if date is the range.
	 *
	 * @param date
	 *            the date to test.
	 * @return true if is in range.
	 */
	public boolean isInRange(Date date) {

		if (getFirst() != null) {
			boolean before = getFirst().before(date);
			if (getSecond() != null) {
				return before && getSecond().after(date);
			}
			return before;
		}
		if (getSecond() != null) {
			return getSecond().after(date);
		}
		// range is not defined
		return true;
	}

}
