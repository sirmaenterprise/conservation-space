package com.sirma.itt.seip.time.schedule;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.joda.time.LocalDate;

/**
 * Deadline calculator.
 *
 * <P>Calculates a deadline by given start date and duration [days]</P>
 *
 * @author Valeri Tishev
 *
 */
@ApplicationScoped
public class DeadlineCalculator {

	/**
	 * Workday exclusion cache. <B>NOTE:</B> the cache is not used so far, but is considered for future development
	 */
	private Map<LocalDate, WorkdayExclusion> workdayExclusionsCache = Collections.emptyMap();

	/**
	 * Calculate the deadline by given start date and duration [days],
	 * considering or not workday exclusions.
	 *
	 * Considering the calendar listed below:
	 *
	 * <PRE>
	 *       June 2015             July 2015
	 * Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su
	 *  1  2  3  4  5  6  7         1  2  3  4  5
	 *  8  9 10 11 12 13 14   6  7  8  9 10 11 12
	 * 15 16 17 18 19 20 21  13 14 15 16 17 18 19
	 * 22 23 24 25 26 27 28  20 21 22 23 24 25 26
	 * 29 30                 27 28 29 30 31
	 * </PRE>
	 *
	 * <UL>
	 * <LI>
	 * Given start date <B>26.06.2015</B>, duration <B>7</B> days
	 * and mindWorkdayExclusions set to <B>false</B>
	 * the calculated deadline should be 06.07.2015
	 *
	 * <LI>
	 * Given start date <B>26.06.2015</B>, duration <B>7</B> days,
	 * mindWorkdayExclusions set to <B>true</B> and no workday
	 * exclusions defined (except for standard holidays - Saturday and Sunday)
	 * the calculated deadline should be 07.07.2015
	 * </UL>
	 *
	 * @param startDate the start date of the time period to be calculated
	 * @param duration duration [days] of the time period to be calculated
	 * @param mindWorkdayExclusions consider or not workday exclusions
	 * @return the deadline date
	 */
	public Date calculateDeadLine(Date startDate, int duration, boolean mindWorkdayExclusions) {
		return calculateDeadLine(startDate, duration, mindWorkdayExclusions, false, false);
	}

	/**
	 * Calculates the date by adding/subtracting the given number of days to/from the starting date, considering or not
	 * workday exclusions.
	 *
	 * @param startDate
	 *            the start date of the time period to be calculated
	 * @param numberOfDays
	 *            number of days of the time period to be calculated
	 * @param mindWorkdayExclusions
	 *            consider or not workday exclusions
	 * @param operation
	 *            <B>ADD<B> for adding the days or <B>SUB<B> for subtracting the days
	 * @return the calculated date
	 */
	public Date calculateDate(Date startDate, int numberOfDays, boolean mindWorkdayExclusions,
			OperationType operation) {
		return calculateDate(startDate, numberOfDays, mindWorkdayExclusions, false, false, operation);
	}

	/**
	 * Calculate the deadline by given start date and duration [days],
	 * considering or not workday exclusions.
	 *
	 * @param startDate the start date of the time period to be calculated
	 * @param duration duration [days] of the time period to be calculated
	 * @param mindWorkdayExclusions consider or not workday exclusions
	 * @param startOnWorkdayExclusion start or not on a workday exclusion
	 * @param endOnWorkdayExclusion end or not on a workday exclusion
	 * @return the deadline date
	 */
	public Date calculateDeadLine(
			Date startDate,
			int duration,
			boolean mindWorkdayExclusions,
			boolean startOnWorkdayExclusion,
			boolean endOnWorkdayExclusion) {
		return calculateDate(startDate, duration, mindWorkdayExclusions, startOnWorkdayExclusion,
				endOnWorkdayExclusion, OperationType.ADD);
	}

	/**
	 * Calculates the date by adding/subtracting the given number of days to/from the starting date, considering or not
	 * workday exclusions.
	 *
	 * @param startDate
	 *            the start date of the time period to be calculated
	 * @param numberOfDays
	 *            number of days of the time period to be calculated
	 * @param mindWorkdayExclusions
	 *            consider or not workday exclusions
	 * @param startOnWorkdayExclusion
	 *            start or not on a workday exclusion
	 * @param endOnWorkdayExclusion
	 *            end or not on a workday exclusion
	 * @param operation
	 *            <B>ADD<B> for adding the days or <B>SUB<B> for subtracting the days
	 * @return the calculated date
	 */
	public Date calculateDate(Date startDate, int numberOfDays, boolean mindWorkdayExclusions,
			boolean startOnWorkdayExclusion, boolean endOnWorkdayExclusion, OperationType operation) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		// start counting on next day
		calendar = calculateDay(calendar, operation);

		if (!startOnWorkdayExclusion) {
			while (isWorkday(calendar)) {
				calendar = calculateDay(calendar, operation);
			}
		}

		for (int i = 0; i < (numberOfDays - 1); i++) {
			if (mindWorkdayExclusions) {
				do {
					calendar = calculateDay(calendar, operation);
				} while (isWorkday(calendar));
			} else {
				calendar = calculateDay(calendar, operation);
			}
		}

		if (!endOnWorkdayExclusion) {
			while (isWorkday(calendar)) {
				calendar = calculateDay(calendar, operation);
			}
		}

		return calendar.getTime();
	}

	/**
	 * Adds or subtracts a day from the calendar. With default operation - add.
	 *
	 * @param calendar
	 *            instance
	 * @param operation
	 *            <B>ADD<B> for adding the days or <B>SUB<B> for subtracting the days
	 * @return the new calendar
	 */
	private Calendar calculateDay(Calendar calendar, OperationType operation) {
		if (OperationType.SUB.equals(operation)) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		} else {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		return calendar;
	}

	/**
	 * Check whether a given date is workday or not.
	 *
	 * @param date
	 *            date to check whether is workday or not
	 * @return true if given date is workday
	 */
	private boolean isWorkday(Calendar date) {
		WorkdayExclusion workdayExclusion = workdayExclusionsCache.get(new LocalDate(date.getTime()));

		if (workdayExclusion != null) {
			return !workdayExclusion.isWorkday();
		}

		return (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
				(date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) ;
	}

}
