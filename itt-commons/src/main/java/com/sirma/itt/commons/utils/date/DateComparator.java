package com.sirma.itt.commons.utils.date;

import java.util.Calendar;
import java.util.Date;

/**
 * Compares dates by stripping time, milliseconds, etc.
 * 
 * @author SKostadinov
 */
public final class DateComparator {
	/**
	 * Private constructor.
	 */
	private DateComparator() {
		// Hides instantiating the class.
	}

	/**
	 * Checks if stripped dates are equal.
	 * 
	 * @param firstDate
	 *            the first date for the compare
	 * @param secondDate
	 *            the second date for the compare
	 * @param stripType
	 *            the type of stripping of dates
	 * @return true if the stripped dates are equal
	 */
	public static boolean isEqual(Date firstDate, Date secondDate,
			DateStripType stripType) {
		boolean result = false;
		if (firstDate == null && secondDate == null) {
			result = true;
		} else if (firstDate == null || secondDate == null) {
			// if one of the dates is null
			result = false;
		} else {
			// if both dates are not null
			Date firstStrippedDate = stripDate(firstDate, stripType);
			Date secondStrippedDate = stripDate(secondDate, stripType);
			result = firstStrippedDate.getTime() == secondStrippedDate
					.getTime();
		}

		return result;
	}

	/**
	 * Checks if two Date objects are equal according to date (yy-mm-dd).
	 * 
	 * @return true if they are equal, false otherwise.
	 */
	public static boolean areEqualDates(Date firstDate, Date secondDate) {
		return isEqual(firstDate, secondDate, DateStripType.STRIP_HOUR);
	}

	/**
	 * Checks if stripped dates are equal.
	 * 
	 * @param firstDate
	 *            the first date for the compare
	 * @param secondDate
	 *            the second date for the compare
	 * @param stripType
	 *            the type of stripping of dates
	 * @return true if the stripped dates are equal
	 */
	public static boolean isLess(Date firstDate, Date secondDate,
			DateStripType stripType) {
		if (firstDate == null) {
			throw new IllegalArgumentException("Invalid first date.");
		}
		if (secondDate == null) {
			throw new IllegalArgumentException("Invalid second date.");
		}
		Date firstStrippedDate = stripDate(firstDate, stripType);
		Date secondStrippedDate = stripDate(secondDate, stripType);
		return firstStrippedDate.getTime() < secondStrippedDate.getTime();
	}

	/**
	 * Checks if stripped dates are equal.
	 * 
	 * @param firstDate
	 *            the first date for the compare
	 * @param secondDate
	 *            the second date for the compare
	 * @param stripType
	 *            the type of stripping of dates
	 * @return true if the stripped dates are equal
	 */
	public static boolean isLessOrEqual(Date firstDate, Date secondDate,
			DateStripType stripType) {
		if (firstDate == null) {
			throw new IllegalArgumentException("Invalid first date.");
		}
		if (secondDate == null) {
			throw new IllegalArgumentException("Invalid second date.");
		}
		Date firstStrippedDate = stripDate(firstDate, stripType);
		Date secondStrippedDate = stripDate(secondDate, stripType);
		return firstStrippedDate.getTime() <= secondStrippedDate.getTime();
	}

	/**
	 * Checks if stripped dates are equal.
	 * 
	 * @param firstDate
	 *            the first date for the compare
	 * @param secondDate
	 *            the second date for the compare
	 * @param stripType
	 *            the type of stripping of dates
	 * @return true if the stripped dates are equal
	 */
	public static boolean isMore(Date firstDate, Date secondDate,
			DateStripType stripType) {
		if (firstDate == null) {
			throw new IllegalArgumentException("Invalid first date.");
		}
		if (secondDate == null) {
			throw new IllegalArgumentException("Invalid second date.");
		}
		Date firstStrippedDate = stripDate(firstDate, stripType);
		Date secondStrippedDate = stripDate(secondDate, stripType);
		return firstStrippedDate.getTime() > secondStrippedDate.getTime();
	}

	/**
	 * Checks if stripped dates are equal.
	 * 
	 * @param firstDate
	 *            the first date for the compare
	 * @param secondDate
	 *            the second date for the compare
	 * @param stripType
	 *            the type of stripping of dates
	 * @return true if the stripped dates are equal
	 */
	public static boolean isMoreOrEqual(Date firstDate, Date secondDate,
			DateStripType stripType) {
		if (firstDate == null) {
			throw new IllegalArgumentException("Invalid first date.");
		}
		if (secondDate == null) {
			throw new IllegalArgumentException("Invalid second date.");
		}
		Date firstStrippedDate = stripDate(firstDate, stripType);
		Date secondStrippedDate = stripDate(secondDate, stripType);
		return firstStrippedDate.getTime() >= secondStrippedDate.getTime();
	}

	/**
	 * Strips date.
	 * 
	 * @param currentDate
	 *            the date that will be stripped
	 * @param stripType
	 *            the type of strip
	 * @return newly stripped date
	 */
	public static Date stripDate(Date currentDate, DateStripType stripType) {
		if (currentDate == null) {
			throw new IllegalArgumentException("Invalid date.");
		}
		if (stripType == null) {
			return new Date(currentDate.getTime());
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.set(Calendar.MILLISECOND, 0);
		int year = 1;
		int month = 0;
		int day = 1;
		int hour = 0;
		int minute = 0;
		int second = 0;
		int millisecond = 0;
		if (stripType.getWeight() < DateStripType.STRIP_YEAR.getWeight()) {
			year = calendar.get(Calendar.YEAR);
		}
		if (stripType.getWeight() < DateStripType.STRIP_MONTH.getWeight()) {
			month = calendar.get(Calendar.MONTH);
		}
		if (stripType.getWeight() < DateStripType.STRIP_DAY.getWeight()) {
			day = calendar.get(Calendar.DAY_OF_MONTH);
		}
		if (stripType.getWeight() < DateStripType.STRIP_HOUR.getWeight()) {
			hour = calendar.get(Calendar.HOUR);
		}
		if (stripType.getWeight() < DateStripType.STRIP_MINUTE.getWeight()) {
			minute = calendar.get(Calendar.MINUTE);
		}
		if (stripType.getWeight() < DateStripType.STRIP_SECOND.getWeight()) {
			second = calendar.get(Calendar.SECOND);
		}
		if (stripType.getWeight() < DateStripType.STRIP_MILLISECOND.getWeight()) {
			millisecond = calendar.get(Calendar.MILLISECOND);
		}
		calendar.set(year, month, day, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, millisecond);
		return calendar.getTime();
	}
}