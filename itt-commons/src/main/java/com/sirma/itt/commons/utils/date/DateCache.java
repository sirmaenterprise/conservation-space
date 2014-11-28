package com.sirma.itt.commons.utils.date;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import com.sirma.itt.commons.utils.collections.LRUMap;

/**
 * Class contains {@link java.util.Date} instances the will be used for
 * minimization of the number of instances kept in the system.
 * 
 * @author SKostadinov
 */
public final class DateCache {
	/**
	 * Current time zone.
	 */
	public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
	/**
	 * This parameters is the number of milliseconds in a day.
	 */
	public static final long NUMBER_OF_MILLISECOND_IN_DAY = 24 * 60 * 60 * 1000;
	/**
	 * This is the capacity of the dates map.
	 */
	public static final int CAPACITY = 1000;
	/**
	 * The first year index that will be cached in the pool.
	 */
	public static final int START_YEAR = 1900;
	/**
	 * The last year index that will be cached in the pool.
	 */
	public static final int END_YEAR = 2100;
	/**
	 * Indexes of the days in non leap year.
	 */
	private static final int[] NON_LEAP_YEAR_DAYS_FROM_MONTH_RANGE = new int[] {
			0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 };
	/**
	 * Indexes of the days in leap year.
	 */
	private static final int[] LEAP_YEAR_DAYS_FROM_MONTH_RANGE = new int[] { 0,
			31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366 };
	/**
	 * Indexes of the years from {@link #START_YEAR} to {@link #END_YEAR}.
	 */
	private static final int[] DAYS_FROM_YEAR_RANGE;
	/**
	 * This is map with cached dates.
	 */
	private static final Map<Integer, Date> DATES = new LRUMap<Integer, Date>(
			DateCache.CAPACITY);

	/**
	 * Private constructor.
	 */
	private DateCache() {
		// Hides functionality.
	}

	static {
		DAYS_FROM_YEAR_RANGE = new int[DateCache.END_YEAR
				- DateCache.START_YEAR + 1];
		Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
		for (int i = DateCache.START_YEAR; i <= DateCache.END_YEAR; i++) {
			calendar.clear();
			calendar.set(i, 0, 1);
			DateCache.DAYS_FROM_YEAR_RANGE[i - DateCache.START_YEAR] = (int) (calendar
					.getTimeInMillis() / DateCache.NUMBER_OF_MILLISECOND_IN_DAY);
		}
	}

	/**
	 * Gets current date instance.
	 * 
	 * @return current date instance
	 */
	public static Date getCurrentDate() {
		Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
		return DateCache.getDate(calendar.getTimeInMillis());
	}

	/**
	 * Creates new date instance by stripping hours, minutes and milliseconds.
	 * 
	 * @param date
	 *            is the date that will be stripped
	 * @return stripped date instance
	 */
	public static Date getDate(Date date) {
		return DateCache.getDate(date.getTime());
	}

	/**
	 * Gets date by passing milliseconds.
	 * 
	 * @param dateMilliseconds
	 *            is the milliseconds parameter that is used for generation of
	 *            the date
	 * @return date instance
	 */
	public static Date getDate(long dateMilliseconds) {
		int yearRange = (int) (dateMilliseconds / DateCache.NUMBER_OF_MILLISECOND_IN_DAY);
		if (dateMilliseconds % DateCache.NUMBER_OF_MILLISECOND_IN_DAY < 0) {
			yearRange--;
		}
		int year = DateCache.getYearIndex(yearRange);
		if (year < 0) {
			return null;
		}
		boolean isLeapYear = DateCache.isLeapYear(year);
		int dayInYearIndex = yearRange
				- DateCache.DAYS_FROM_YEAR_RANGE[year - DateCache.START_YEAR];
		int month = DateCache.getMonthIndex(dayInYearIndex, isLeapYear);
		int date = DateCache.getDateIndex(dayInYearIndex, month, isLeapYear);
		return DateCache.getDate(year, month, date + 1);
	}

	/**
	 * Gets date by year, month and date. Key of the map is unique integer
	 * representation of date. The first 5 bits (data from 0 to 31) are used for
	 * unique date (the date variety is from 1 to 31), the next 4 bits (data
	 * from 0 to 15) are used for unique month (the month is from 1 to 12) and
	 * the other is for the year (the year is from 1 to 9999).
	 * 
	 * @param year
	 *            is the year of the date
	 * @param month
	 *            is the month of the date
	 * @param date
	 *            is the date of the date
	 * @return date instance
	 */
	public static Date getDate(int year, int month, int date) {
		Integer cacheKey = Integer.valueOf((year << 9) + (month << 5) + date);
		Date result = DateCache.DATES.get(cacheKey);
		if (result == null) {
			Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
			calendar.clear();
			calendar.set(year, month - 1, date);
			result = new Date(calendar.getTimeInMillis());
			DateCache.DATES.put(cacheKey, result);
		}
		return result;
	}

	/**
	 * Checks if the year is leap or not.
	 * 
	 * @param yearNumber
	 *            is the year number
	 * @return true if the year is leap
	 */
	public static boolean isLeapYear(int yearNumber) {
		if (yearNumber % 400 == 0) {
			return true;
		}
		if (yearNumber % 100 == 0) {
			return false;
		}
		if (yearNumber % 4 == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the index of the month in the year.
	 * 
	 * @param daysFromYear
	 *            is the day number in the year
	 * @return the index of the month in the year
	 */
	public static int getYearIndex(int daysFromYear) {
		int size = DateCache.DAYS_FROM_YEAR_RANGE.length - 1;
		for (int i = 0; i < size; i++) {
			if (DateCache.DAYS_FROM_YEAR_RANGE[i] < daysFromYear
					&& daysFromYear <= DateCache.DAYS_FROM_YEAR_RANGE[i + 1]) {
				return DateCache.START_YEAR + i;
			}
		}
		return -1;
	}

	/**
	 * Gets the index of the month in the year.
	 * 
	 * @param dayInYearIndex
	 *            is the day number in the year
	 * @param isLeapYear
	 *            is parameter that keeps information if the year is leap
	 * @return the index of the month in the year
	 */
	public static int getMonthIndex(int dayInYearIndex, boolean isLeapYear) {
		if (isLeapYear) {
			for (int i = 0; i < 12; i++) {
				if (DateCache.LEAP_YEAR_DAYS_FROM_MONTH_RANGE[i] < dayInYearIndex
						&& dayInYearIndex <= DateCache.LEAP_YEAR_DAYS_FROM_MONTH_RANGE[i + 1]) {
					return i + 1;
				}
			}
		} else {
			for (int i = 0; i < 12; i++) {
				if (DateCache.NON_LEAP_YEAR_DAYS_FROM_MONTH_RANGE[i] < dayInYearIndex
						&& dayInYearIndex <= DateCache.NON_LEAP_YEAR_DAYS_FROM_MONTH_RANGE[i + 1]) {
					return i + 1;
				}
			}
		}
		return -1;
	}

	/**
	 * Gets the date of the month.
	 * 
	 * @param dayInYearIndex
	 *            is the day number in the year
	 * @param monthNumber
	 *            is the month number
	 * @param isLeapYear
	 *            is parameter that keeps information if the year is leap
	 * @return the date of the month
	 */
	public static int getDateIndex(int dayInYearIndex, int monthNumber,
			boolean isLeapYear) {
		if (monthNumber < 1 || monthNumber > 12) {
			return -1;
		}
		if (isLeapYear) {
			if (DateCache.LEAP_YEAR_DAYS_FROM_MONTH_RANGE[monthNumber - 1] < dayInYearIndex
					&& dayInYearIndex <= DateCache.LEAP_YEAR_DAYS_FROM_MONTH_RANGE[monthNumber]) {
				return dayInYearIndex
						- DateCache.LEAP_YEAR_DAYS_FROM_MONTH_RANGE[monthNumber - 1];
			}
			return -1;
		}
		if (DateCache.NON_LEAP_YEAR_DAYS_FROM_MONTH_RANGE[monthNumber - 1] < dayInYearIndex
				&& dayInYearIndex <= DateCache.NON_LEAP_YEAR_DAYS_FROM_MONTH_RANGE[monthNumber]) {
			return dayInYearIndex
					- DateCache.NON_LEAP_YEAR_DAYS_FROM_MONTH_RANGE[monthNumber - 1];
		}
		return -1;
	}

	/**
	 * Gets number of days till now.
	 * 
	 * @param year
	 *            is the year of the date
	 * @param month
	 *            is the month of the date
	 * @param date
	 *            is the date of the date
	 * @return number of days till now
	 */
	public static long getNumberOfDaysTillNow(int year, int month, int date) {
		Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
		calendar.clear();
		calendar.set(year, month - 1, date);
		long difference = System.currentTimeMillis();
		difference -= difference % DateCache.NUMBER_OF_MILLISECOND_IN_DAY;
		difference -= calendar.getTimeInMillis();
		return difference / DateCache.NUMBER_OF_MILLISECOND_IN_DAY;
	}
}