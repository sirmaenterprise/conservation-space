package com.sirma.itt.commons.utils.date;

import java.util.Date;

/**
 * Represents a date range.
 * 
 * @author Adrian Mitev
 */
public class DateRange {

	/**
	 * Date from.
	 */
	private Date from;

	/**
	 * Date to
	 */
	private Date to;

	/**
	 * Initializes from and to dates.
	 * 
	 * @param from
	 *            date "from" to set.
	 * @param to
	 *            date "to" to set;
	 */
	public DateRange(Date from, Date to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * Creates a DateRange using the current date truncated to yy-MM-dd.
	 * 
	 * @return created DateRange.
	 */
	public static DateRange forCurrentDate() {
		Date date = DateComparator.stripDate(new Date(),
				DateStripType.STRIP_HOUR);

		return new DateRange(date, date);
	}

	/**
	 * Getter method for from.
	 * 
	 * @return the from
	 */
	public Date getFrom() {
		return from;
	}

	/**
	 * Setter method for from.
	 * 
	 * @param from
	 *            the from to set
	 */
	public void setFrom(Date from) {
		this.from = from;
	}

	/**
	 * Getter method for to.
	 * 
	 * @return the to
	 */
	public Date getTo() {
		return to;
	}

	/**
	 * Setter method for to.
	 * 
	 * @param to
	 *            the to to set
	 */
	public void setTo(Date to) {
		this.to = to;
	}

}
