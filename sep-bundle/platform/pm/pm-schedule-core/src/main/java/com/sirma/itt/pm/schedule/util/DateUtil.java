package com.sirma.itt.pm.schedule.util;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

/**
 * Date utility class for calculating durations
 * 
 * @author BBonev
 */
public class DateUtil {

	public static final String UNIT_SECOND = "s";
	public static final String UNIT_MILLISECOND = "mi";
	public static final String UNIT_MINUTE = "m";
	public static final String UNIT_DAY = "d";
	public static final String UNIT_HOUR = "h";
	
	private static final int MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

	/**
	 * Calculate duration between the given two dates. If needed the method could remove the
	 * weekends from the returned count
	 * 
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 * @param removeWeekends
	 *            the remove weekends
	 * @return the int
	 */
	public static int calculateDuration(Date start, Date end, boolean removeWeekends) {
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(start);
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(end);
		int startYear = startCalendar.get(Calendar.YEAR);
		int startDay = startCalendar.get(Calendar.DAY_OF_YEAR);
		int endYear = endCalendar.get(Calendar.YEAR);
		int endDay = endCalendar.get(Calendar.DAY_OF_YEAR);
		int result;
		result = ((endYear - startYear) * 365) + (endDay - startDay);
		if (result == 0) {
			// we have the same day then the duration is the same day
			int dayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK);
			if (!removeWeekends && (dayOfWeek != Calendar.SATURDAY)
					&& (dayOfWeek != Calendar.SUNDAY)) {
				result = 1;
			}
		}
		if (removeWeekends && (result > 1)) {
			// calculate the working days in the first week
			int sdayOfweek = startCalendar.get(Calendar.DAY_OF_WEEK);
			int daysInStartWeek = 0;
			if ((sdayOfweek != Calendar.SUNDAY) && (sdayOfweek != Calendar.SATURDAY)) {
				daysInStartWeek = Calendar.SATURDAY - sdayOfweek;
			}

			// calculate the work days in the last week
			int edayOfweek = endCalendar.get(Calendar.DAY_OF_WEEK);
			int daysInEndWeek = 0;
			if ((edayOfweek != Calendar.SUNDAY) && (edayOfweek != Calendar.SATURDAY)) {
				daysInEndWeek = edayOfweek - Calendar.SUNDAY;
			}

			int startWeek = startCalendar.get(Calendar.WEEK_OF_YEAR);
			int endWeek = endCalendar.get(Calendar.WEEK_OF_YEAR);
			if (startWeek == endWeek) {
				// implement when the start end end date are in the same week
				if (edayOfweek == Calendar.SATURDAY) {
					result--;
				} else if (edayOfweek == Calendar.SUNDAY) {
					result -= 2;
				}
				if (result < 0) {
					result = 0;
				}
			} else {
				int weeks = result / 7;
				result = weeks * 5;
				if (((result % 7) != 0) || ((daysInStartWeek > 0) || (daysInEndWeek > 0))) {
					// if not even then we add the days that are remaining
					result += daysInStartWeek + daysInEndWeek;
				}
			}
		}
		return result;
	}

	/**
	 * Convert the given duration to milliseconds using as source format the given unit type.
	 * 
	 * @param duration
	 *            the duration in the specified unit
	 * @param unit
	 *            the source unit
	 * @return the long
	 * @see #UNIT_DAY
	 * @see #UNIT_HOUR
	 * @see #UNIT_MINUTE
	 * @see #UNIT_SECOND
	 * @see #UNIT_MILLISECOND
	 */
	public static long convertToMilliseconds(Integer duration, String unit) {
		if (duration == null) {
			return 0;
		}
		long result = duration.longValue();
		if (UNIT_HOUR.equals(unit)) {
			result *= 3600000; // 60 * 60 * 1000;
		} else if (UNIT_DAY.equals(unit)) {
			result *= 86400000; // 24 * 60 * 60 * 1000;
		} else if (UNIT_MINUTE.equals(unit)) {
			result *= 60000; // 60 * 1000;
		} else if (UNIT_SECOND.equals(unit)) {
			result *= 1000;
		} // else if mi is the same as the unput value
		return result;
	}
	
	/**
	 * Calculates days between two dates.
	 *
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param removeWeekends a boolean flag indicating that weekends should be removed from the calculation
	 * @return days between the two dates with high precision
	 */
	public static double daysBetween(Date startDate, Date endDate,  boolean removeWeekends) {
		long duration = 0;
		DateTime startDateTime = new DateTime(startDate.getTime(), DateTimeZone.getDefault());
		DateTime endDateTime = new DateTime(endDate.getTime(), DateTimeZone.getDefault());
		if (removeWeekends) {
			while (startDateTime.isBefore(endDateTime)) {
				int startDatePassedTime = startDateTime.getMillisOfDay();
				int restOfStartDay = MILLIS_IN_A_DAY - startDatePassedTime; 
				
				if (startDateTime.getDayOfWeek() != DateTimeConstants.SATURDAY && startDateTime.getDayOfWeek() != DateTimeConstants.SUNDAY) {
					long diff = endDateTime.getMillis() - startDateTime.getMillis();
					if (diff > restOfStartDay) {
						diff = restOfStartDay;
					}
					duration += diff;
					startDateTime = startDateTime.plusMillis((int)diff);
				} else {
					startDateTime = startDateTime.plusMillis(restOfStartDay);
				}
			}
		} else {
			if (startDateTime.isBefore(endDateTime)) {
				duration = endDateTime.getMillis() - startDateTime.getMillis();
			}
		}
		return (double) duration/MILLIS_IN_A_DAY*1.0;
	}

}
