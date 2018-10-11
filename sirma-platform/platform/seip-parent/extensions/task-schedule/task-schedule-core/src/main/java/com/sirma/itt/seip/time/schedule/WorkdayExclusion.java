package com.sirma.itt.seip.time.schedule;

import java.util.Date;

/**
 * Immutable class representing a workday exclusion
 * 
 * @author Valeri Tishev
 *
 */
public class WorkdayExclusion {
	
	private final Date date;
	private final Boolean isWorkday;
	
	/**
	 * Instantiates a new workday exclusion
	 * 
	 * @param date the day
	 * @param isWorkday denotes whether the given day is working or not
	 */
	public WorkdayExclusion(Date date, Boolean isWorkday) {
		this.date = date;
		this.isWorkday = isWorkday;
	}

	/**
	 * Gets the date
	 * 
	 * @return date of the workday exclusion
	 */
	public Date getDate() {
		return this.date;
	}

	/**
	 * Denotes whether the given day is working or not 
	 * 
	 * @return true if is working
	 */
	public Boolean isWorkday() {
		return isWorkday;
	}

}
