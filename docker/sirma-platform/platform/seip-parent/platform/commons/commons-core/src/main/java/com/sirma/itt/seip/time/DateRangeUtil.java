package com.sirma.itt.seip.time;

import java.util.Calendar;
import java.util.Date;

/**
 * Helper class that creates a {@link DateRange} instance of predefined periods of time
 *
 * @author BBonev
 */
public final class DateRangeUtil {

	/**
	 * Prevent instantiation of utility class
	 */
	private DateRangeUtil() {
	}

	/**
	 * Gets a {@link DateRange} object for today.
	 *
	 * @return the today interval
	 */
	public static DateRange getToday() {
		DateRange nDaysRange = getNDaysRange(0, 0, false);
		return new TodayDateRange(nDaysRange.getFirst(), nDaysRange.getSecond());
	}

	/**
	 * Gets a {@link DateRange} object for this week.
	 *
	 * @return the this week
	 */
	public static DateRange getThisWeek() {
		Calendar calendar = getTodayCalendar();
		// beginning of the week
		// this could be customized
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0,
				0, 1);
		return new DateRange(calendar.getTime(), new Date());
	}

	/**
	 * Gets a {@link DateRange} object for the last 7 days.
	 *
	 * @return the last7 days
	 */
	public static DateRange getLast7Days() {
		return getNDaysRange(7, 0, false);
	}

	/**
	 * Gets a {@link DateRange} object for last 30 days.
	 *
	 * @return the last30 days
	 */
	public static DateRange getLast30Days() {
		return getNDaysRange(30, 0, false);
	}

	/**
	 * Gets a {@link DateRange} object for this month.
	 *
	 * @return the this month
	 */
	public static DateRange getThisMonth() {
		Calendar calendar = getTodayCalendar();
		// beginning of the week
		// this could be customized
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0,
				0, 1);
		return new DateRange(calendar.getTime(), new Date());
	}

	/**
	 * Gets a {@link DateRange} object that spans from -infinity to today.
	 * <p>
	 * <b>NOTE:</b> The beginning of the interval is 1970.
	 *
	 * @return the all
	 */
	public static DateRange getAll() {
		return new DateRange(new Date(0), new Date());
	}

	/**
	 * Returns the specified number of days back from today.
	 *
	 * @param offset
	 *            is the offset from today to substract days. 30 will give 1 month earlier roughly
	 * @param zeroBasedTime
	 *            whether to set to 0 time and use only date precision for end date
	 * @return the range for the specified gap
	 */
	public static DateRange getLastNDays(int offset, boolean zeroBasedTime) {
		return getNDaysRange(offset, 0, zeroBasedTime);
	}

	/**
	 * Returns a range with specified time gap.
	 *
	 * @param offset
	 *            is the offset from today to substract days. 30 will give 1 month earlier roughly
	 * @param endOffset
	 *            home many days from today to set end date
	 * @param zeroBasedTime
	 *            whether to set to 0 time and use only date precision for end date
	 * @return the specified date range
	 */
	public static DateRange getNDaysRange(final int offset, int endOffset, boolean zeroBasedTime) {
		int negativeOffset = offset;
		if (offset > 0) {
			negativeOffset = -offset;
		}
		// create reference calendar
		Calendar reference = Calendar.getInstance();
		Calendar startDate = Calendar.getInstance();
		Calendar endDate = Calendar.getInstance();
		// initialize the start and end calendars with the reference date
		Date referanceDate = reference.getTime();
		startDate.setTime(referanceDate);
		endDate.setTime(referanceDate);
		startDate.add(Calendar.DAY_OF_YEAR, negativeOffset);
		startDate.set(Calendar.HOUR_OF_DAY, reference.getActualMinimum(Calendar.HOUR_OF_DAY));
		startDate.set(Calendar.MINUTE, reference.getActualMinimum(Calendar.MINUTE));
		startDate.set(Calendar.SECOND, reference.getActualMinimum(Calendar.SECOND));

		endDate.add(Calendar.DAY_OF_YEAR, endOffset);
		if (zeroBasedTime) {
			endDate.set(Calendar.HOUR_OF_DAY, reference.getActualMinimum(Calendar.HOUR_OF_DAY));
			endDate.set(Calendar.MINUTE, reference.getActualMinimum(Calendar.MINUTE));
			endDate.set(Calendar.SECOND, reference.getActualMinimum(Calendar.SECOND));
		}
		return new DateRange(startDate.getTime(), endDate.getTime());
	}

	/**
	 * Checks if the given {@link DateRange} instance points range for today.
	 * <p>
	 * <b>IMPORTANT: </b> In order this method to return <code>true</code> the instance of the argument should be
	 * produced from the method {@link #getToday()}. All other instances will return <code>false</code>
	 *
	 * @param dateRange
	 *            the date range to check
	 * @return true, if is today
	 */
	public static boolean isToday(DateRange dateRange) {
		return dateRange instanceof TodayDateRange;
	}

	/**
	 * Gets the today calendar.
	 *
	 * @return the today calendar
	 */
	private static Calendar getTodayCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		return calendar;
	}

	/**
	 * Maker class to indicate that the returned instance of the method {@link DateRangeUtil#getToday()} is distinct
	 * from the other methods.
	 *
	 * @author BBonev
	 */
	private static class TodayDateRange extends DateRange {

		private static final long serialVersionUID = 6068954304089822763L;

		/**
		 * Instantiates a new today date range.
		 *
		 * @param first
		 *            the first
		 * @param second
		 *            the second
		 */
		public TodayDateRange(Date first, Date second) {
			super(first, second);
		}

	}
}
