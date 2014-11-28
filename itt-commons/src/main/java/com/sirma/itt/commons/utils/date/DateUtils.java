/*
 * Created on 23.07.2008 11:12:19
 *
 * Author: Hristo Iliev
 * Company: Sirma-ITT
 * Email: hristo.iliev@sirma.bg
 */
package com.sirma.itt.commons.utils.date;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * 
 * @author Hristo Iliev
 */
public final class DateUtils {

	/**
	 * Hide default constructor.
	 */
	private DateUtils() {
		// No class should be instantiated
	}

	/** Message if for exceptions if null parameter is provided. */
	private static final String NOT_NULL_DATE_MESSAGE = "The date must not be null";

	/** Message if the range is smaller than the specified part of date. */
	private static final String DIFFERENT_RANGE_MESSAGE = "Range must be greated or equal to the part to change";

	/**
	 * Enumeration with parts of which date can be split.
	 * 
	 * @author Hristo Iliev
	 * 
	 */
	public enum DatePart {
		/** value specifying millisecond concreteness part of day. */
		MILLISECOND(Calendar.MILLISECOND, 0, 999, 999), // 14
		/** value specifying second concreteness part of day. */
		SECOND(Calendar.SECOND, 0, 59, 59), // 13
		/** value specifying minute concreteness part of day. */
		MINUTE(Calendar.MINUTE, 0, 59, 59), // 12
		/** value specifying hour concreteness part of day. */
		HOUR_OF_HALF_DAY(Calendar.HOUR, 0, 11, 11), // 10
		/**
		 * value specifying hour of the day concreteness part of day. Alias for
		 * HOUR_OF_DAY.
		 */
		HOUR(Calendar.HOUR_OF_DAY, 0, 23, 23), // 11
		/**
		 * value specifying hour of the day concreteness part of day. Alias for
		 * HOUR.
		 */
		HOUR_OF_DAY(Calendar.HOUR_OF_DAY, 0, 23, 23), // 11
		/** value specifying half_day concreteness part of day. */
		HALF_DAY(Calendar.AM_PM, 0, 1, 1), // 9
		/** value specifying day of week concreteness part of day. */
		DAY_OF_WEEK(Calendar.DAY_OF_WEEK, 1, 7, 7), // 7
		/** value specifying day concreteness part of day. */
		DAY_OF_YEAR(Calendar.DAY_OF_YEAR, 1, 365, 366), // 6
		/**
		 * value specifying day of month concreteness part of day. Alias for
		 * DAY_OF_MONTH.
		 */
		DAY(Calendar.DAY_OF_MONTH, 1, 28, 31), // 5
		/**
		 * value specifying day of month concreteness part of day. Alias for
		 * DAY.
		 */
		DAY_OF_MONTH(Calendar.DAY_OF_MONTH, 1, 28, 31), // 5
		/** value specifying day of week in month part of day. */
		DAY_OF_WEEK_IN_MONTH(Calendar.DAY_OF_WEEK_IN_MONTH, -1, 4, 6), // 8
		/** value specifying week of month concreteness part of day. */
		WEEK_OF_MONTH(Calendar.WEEK_OF_MONTH, 0, 4, 6), // 4
		/**
		 * value specifying week of year concreteness part of day. Alias for
		 * WEEK_OF_YEAR.
		 */
		WEEK(Calendar.WEEK_OF_YEAR, 1, 52, 53),
		/**
		 * value specifying week of year concreteness part of day. Alias for
		 * WEEK.
		 */
		WEEK_OF_YEAR(Calendar.WEEK_OF_YEAR, 1, 52, 53), // 3
		/** value specifying month concreteness part of day. */
		MONTH(Calendar.MONTH, 0, 11, 11), // 2
		/** value specifying year concreteness part of day. */
		YEAR(Calendar.YEAR, 1, 292269054, 292278994), // 1
		/** value specifying era concreteness part of day. */
		ERA(Calendar.ERA, 0, 1, 1); // 0
		/** mapped field in Calendar class. */
		private final int calendarValue;

		/** minimum value of the part of date. */
		private final int minValue;
		/** the smallest maximum value of the part of date. */
		private final int leastMaxValue;
		/** the gratest maximum value of the part of date. */
		private final int maxValue;

		/**
		 * Map enumeration to field in the Calendar.
		 * 
		 * @param calendarValue
		 *            int, value of the field in Calendar
		 * @param minValue
		 *            int, minimum value of the part of date
		 * @param leastMaxValue
		 *            int, the smallest maximum value of the part of date
		 * @param maxValue
		 *            int, the gratest maximum value of the part of date
		 */
		DatePart(int calendarValue, int minValue, int leastMaxValue,
				int maxValue) {
			this.calendarValue = calendarValue;
			this.minValue = minValue;
			this.leastMaxValue = leastMaxValue;
			this.maxValue = maxValue;
		}

		/**
		 * @return the calendarValue
		 */
		public int getCalendarValue() {
			return calendarValue;
		}

		/**
		 * Getter method for minValue.
		 * 
		 * @return the minValue
		 */
		public int getMinValue() {
			return minValue;
		}

		/**
		 * Getter method for leastMaxValue.
		 * 
		 * @return the leastMaxValue
		 */
		public int getLeastMaxValue() {
			return leastMaxValue;
		}

		/**
		 * Getter method for maxValue.
		 * 
		 * @return the maxValue
		 */
		public int getMaxValue() {
			return maxValue;
		}
	}

	/**
	 * Iterator for dates. On every iteration the date is changed with specified
	 * distance.
	 * 
	 * @author Hristo Iliev
	 */
	static class DateIterator implements Iterator<Date> {

		/** the limit of the iterator. */
		private Calendar limit;
		/** current value of the iterator. */
		private Calendar start;
		/** part of date which will be changed. */
		private final DatePart part;
		/** amount of date parts to be changed on iteration. */
		private final int distance;
		/** amount of date parts to be changed on current iteration. */
		private int currentDistance;
		/** cache the hasNext check. */
		private boolean hasNextValue;
		/**
		 * cache the next value. If null either {@link #hasNext()} is not called
		 * or there is no next value.
		 */
		private Calendar current;

		/**
		 * Creates iterator for dates.
		 * 
		 * @param start
		 *            {@link Date}, start of the iteration
		 * @param part
		 *            {@link DatePart}, part of date which will be changed
		 * @param distance
		 *            int, amount of date part specified by <code>part</code>
		 *            value to be changed
		 */
		DateIterator(Date start, DatePart part, int distance) {
			this.part = part;
			this.distance = distance;
			this.start = getCalendar(start);
			hasNextValue = true;
		}

		/**
		 * Creates iterator for dates.
		 * 
		 * @param start
		 *            {@link Date}, start of the iteration
		 * @param end
		 *            {@link Date}, end of the iteration
		 * @param part
		 *            {@link DatePart}, part of date which will be changed
		 * @param distance
		 *            int, amount of date part specified by <code>part</code>
		 *            value to be changed
		 */
		DateIterator(Date start, Date end, DatePart part, int distance) {
			this(start, part, distance);
			limit = getCalendar(end);
			hasNextValue = this.start.getTimeInMillis() <= limit
					.getTimeInMillis();
		}

		/**
		 * Creates iterator for dates.
		 * 
		 * @param start
		 *            {@link Date}, start of the iteration
		 * @param part
		 *            {@link DatePart}, part of date which will be changed
		 * @param distance
		 *            int, amount of datepart specified by <code>part</code>
		 *            value to be changed
		 */
		DateIterator(Calendar start, DatePart part, int distance) {
			/* Create new calendar. Prevent changes of start object. */
			this(start.getTime(), part, distance);
		}

		/**
		 * Creates iterator for dates.
		 * 
		 * @param start
		 *            {@link Calendar}, start of the iteration
		 * @param end
		 *            {@link Calendar}, end of the iteration
		 * @param part
		 *            {@link DatePart}, part of date which will be changed
		 * @param distance
		 *            int, amount of date part specified by <code>part</code>
		 *            value to be changed
		 */
		DateIterator(Calendar start, Calendar end, DatePart part, int distance) {
			/* Create new calendar. Prevent changes of start and end objects. */
			this(start.getTime(), end.getTime(), part, distance);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext() {
			if (current != null) {
				return hasNextValue;
			}
			current = (Calendar) start.clone();
			add(part, current, currentDistance);
			currentDistance += distance;
			hasNextValue = ((limit == null) || ((distance > 0) ? (current
					.getTimeInMillis() <= limit.getTimeInMillis()) : (current
					.getTimeInMillis() >= limit.getTimeInMillis())));
			return hasNextValue;
		}

		/**
		 * {@inheritDoc}
		 */
		public Date next() {
			if (hasNext()) {
				Date result = current.getTime();
				current = null;
				return result;
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Check if the specified dates are the same until specified part of date.
	 * For example: if <code>part</code> is <code>{@link DatePart#YEAR}</code>
	 * then the dates are checked if they are in the same era and year. If
	 * <code>part</code> is <code>{@link DatePart#HOUR}</code> method will
	 * return true if the dates are in the same day and hour, no matter if the
	 * minutes, seconds or milliseconds are different.
	 * 
	 * @param part
	 *            {@link DatePart}, part of the day until which should be
	 *            checked for equality
	 * @param date1
	 *            {@link Date}, first date to check
	 * @param date2
	 *            {@link Date}, second date to check
	 * @return true if the dates are equal until specified part of the day and
	 *         false if the dates have difference until specified
	 *         <code>part</code>
	 * @see #isSame(DatePart, Calendar, Calendar)
	 */
	public static boolean isSame(DatePart part, Date date1, Date date2) {
		if ((date1 == null) || (date2 == null)) {
			throw new IllegalArgumentException(NOT_NULL_DATE_MESSAGE);
		}
		return isSame(part, getCalendar(date1), getCalendar(date2));
	}

	/**
	 * Check if the specified dates represented by the calendars are the same
	 * until specified part of date. For example: if <code>part</code> is
	 * <code>{@link DatePart#YEAR}</code> then the dates are checked if they are
	 * in the same era and year. If <code>part</code> is
	 * <code>{@link DatePart#HOUR}</code> method will return true if the dates
	 * are in the same day and hour, no matter if the minutes, seconds or
	 * milliseconds are different.
	 * 
	 * @param part
	 *            {@link DatePart}, part of the day until which should be
	 *            checked for equality
	 * @param calendar1
	 *            {@link Calendar}, first date to check
	 * @param calendar2
	 *            {@link Calendar}, second date to check
	 * @return true if the dates are equal until specified part of the day and
	 *         false if the dates have difference until specified
	 *         <code>part</code>
	 * @see #isSame(DatePart, Date, Date)
	 */
	@SuppressWarnings("fallthrough")
	public static boolean isSame(DatePart part, Calendar calendar1,
			Calendar calendar2) {
		if ((calendar1 == null) || (calendar2 == null)) {
			throw new IllegalArgumentException(NOT_NULL_DATE_MESSAGE);
		}
		switch (part) {
		case MILLISECOND:
			if (calendar1.get(Calendar.MILLISECOND) != calendar2
					.get(Calendar.MILLISECOND)) {
				return false;
			}
		case SECOND:
			if (calendar1.get(Calendar.SECOND) != calendar2
					.get(Calendar.SECOND)) {
				return false;
			}
		case MINUTE:
			if (calendar1.get(Calendar.MINUTE) != calendar2
					.get(Calendar.MINUTE)) {
				return false;
			}
		case HOUR:
		case HOUR_OF_HALF_DAY:
		case HOUR_OF_DAY:
			if (calendar1.get(Calendar.HOUR_OF_DAY) != calendar2
					.get(Calendar.HOUR_OF_DAY)) {
				return false;
			}
		case DAY_OF_YEAR:
			if (calendar1.get(Calendar.DAY_OF_YEAR) != calendar2
					.get(Calendar.DAY_OF_YEAR)) {
				return false;
			}
		case YEAR:
			if (calendar1.get(Calendar.YEAR) != calendar2.get(Calendar.YEAR)) {
				return false;
			}
		case ERA:
			if (calendar1.get(Calendar.ERA) != calendar2.get(Calendar.ERA)) {
				return false;
			}
			return true;
		case DAY_OF_WEEK:
			if (calendar1.get(Calendar.DAY_OF_WEEK) != calendar2
					.get(Calendar.DAY_OF_WEEK)) {
				return false;
			}
		case WEEK:
		case WEEK_OF_YEAR:
			int week = calendar1.get(Calendar.WEEK_OF_YEAR);
			// Check if the dates are in the same week
			if (week != calendar2.get(Calendar.WEEK_OF_YEAR)) {
				return false;
			}
			// If the week is different than the first week of the year, then
			// the year and era should be checked
			if (week != 1) {
				return isSame(DatePart.YEAR, calendar1, calendar2);
			}

			// The code bellow is in case two dates return first week

			Calendar cal1;
			Calendar cal2;
			if (calendar1.getTimeInMillis() > calendar2.getTimeInMillis()) {
				cal1 = calendar2;
				cal2 = calendar1;
			} else {
				cal1 = calendar1;
				cal2 = calendar2;
			}

			/*
			 * If the week is 1, then it can be the last partial week of the old
			 * year and the first (partial) week of the new year It is possible
			 * to check for days in the first and last weeks in the first and
			 * last year of the eras
			 */
			if (cal1.get(Calendar.ERA) != cal2.get(Calendar.ERA)) {
				int year1 = cal1.get(Calendar.YEAR);
				/*
				 * If the years are different (and in different eras) than they
				 * definitely can't be in one week
				 */
				if (year1 != cal2.get(Calendar.YEAR)) {
					return false;
				}
				/*
				 * If the years are same then the year should be 1 (first and
				 * last years in the eras), so to be in the same week the date
				 * should be in last month of the BC year and the first in AC
				 * year
				 */
				if (year1 == 1) {
					return ((cal1.get(Calendar.MONTH) == 12) && (cal1
							.get(Calendar.MONTH) == 1));
				}
				/*
				 * If the years are equal but not equal to 1 then the dates
				 * cannot be in the same week
				 */
				return false;
			}
			/*
			 * If the eras are equal then it should be checked if the years are
			 * different, and the smaller date is exactly one year before bigger
			 * or are in the same year and month
			 */
			int year1 = cal1.get(Calendar.YEAR);
			int year2 = cal2.get(Calendar.YEAR);
			if (year1 == year2) {
				return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
			} else if (year2 - year1 == 1) {
				return (cal1.get(Calendar.MONTH) == 12)
						&& (cal2.get(Calendar.MONTH) == 1);
			}
			return false;
		case DAY:
		case DAY_OF_MONTH:
			if (calendar1.get(Calendar.DAY_OF_MONTH) != calendar2
					.get(Calendar.DAY_OF_MONTH)) {
				return false;
			}
		case MONTH:
			if (calendar1.get(Calendar.MONTH) != calendar2.get(Calendar.MONTH)) {
				return false;
			}
			return isSame(DatePart.YEAR, calendar1, calendar2);
		case WEEK_OF_MONTH:
			if (calendar1.get(Calendar.WEEK_OF_MONTH) != calendar2
					.get(Calendar.WEEK_OF_MONTH)) {
				return false;
			}
			return isSame(DatePart.MONTH, calendar1, calendar2);
		case DAY_OF_WEEK_IN_MONTH:
			if (calendar1.get(Calendar.DAY_OF_WEEK_IN_MONTH) != calendar2
					.get(Calendar.DAY_OF_WEEK_IN_MONTH)) {
				return false;
			}
			return isSame(DatePart.MONTH, calendar1, calendar2);
		case HALF_DAY:
			if (calendar1.get(Calendar.AM_PM) != calendar2.get(Calendar.AM_PM)) {
				return false;
			}
			return isSame(DatePart.DAY_OF_YEAR, calendar1, calendar2);
		default:
			throw new AbstractMethodError(
					"DateUtils.isSame(DatePart, Calendar, Calendar) method is not implemented for "
							+ part.name() + " part of date");
		}
	}

	/**
	 * Check if first date is before second taking until specified DatePart
	 * (inclusive).
	 * 
	 * @param datePart
	 *            {@link DatePart}, date part until which will be compared
	 * @param first
	 *            {@link Date}, first date
	 * @param second
	 *            {@link Date}, first date
	 * @return {@code true} if first date is before second
	 */
	public static boolean isBefore(DatePart datePart, Date first, Date second) {
		return first.before(second) && !isSame(datePart, first, second);
	}

	/**
	 * Check if first date is before second taking until specified DatePart
	 * (inclusive).
	 * 
	 * @param datePart
	 *            {@link DatePart}, date part until which will be compared
	 * @param first
	 *            {@link Calendar}, first date
	 * @param second
	 *            {@link Calendar}, first date
	 * @return {@code true} if first date is before second
	 */
	public static boolean isBefore(DatePart datePart, Calendar first,
			Calendar second) {
		return first.before(second) && !isSame(datePart, first, second);
	}

	/**
	 * Check if first date is before or the same as second taking until
	 * specified DatePart (inclusive).
	 * 
	 * @param datePart
	 *            {@link DatePart}, date part until which will be compared
	 * @param first
	 *            {@link Date}, first date
	 * @param second
	 *            {@link Date}, first date
	 * @return {@code true} if first date is before second or if there are same
	 */
	public static boolean isBeforeOrSame(DatePart datePart, Date first,
			Date second) {
		return first.before(second) || isSame(datePart, first, second);
	}

	/**
	 * Check if first date is before or the same as second taking until
	 * specified DatePart (inclusive).
	 * 
	 * @param datePart
	 *            {@link DatePart}, date part until which will be compared
	 * @param first
	 *            {@link Calendar}, first date
	 * @param second
	 *            {@link Calendar}, first date
	 * @return {@code true} if first date is before second or if there are same
	 */
	public static boolean isBeforeOrSame(DatePart datePart, Calendar first,
			Calendar second) {
		return first.before(second) || isSame(datePart, first, second);
	}

	/**
	 * Check if first date is after second taking until specified DatePart
	 * (inclusive).
	 * 
	 * @param datePart
	 *            {@link DatePart}, date part until which will be compared
	 * @param first
	 *            {@link Date}, first date
	 * @param second
	 *            {@link Date}, first date
	 * @return {@code true} if first date is after second
	 */
	public static boolean isAfter(DatePart datePart, Date first, Date second) {
		return first.after(second) && !isSame(datePart, first, second);
	}

	/**
	 * Check if first date is after second taking until specified DatePart
	 * (inclusive).
	 * 
	 * @param datePart
	 *            {@link DatePart}, date part until which will be compared
	 * @param first
	 *            {@link Calendar}, first date
	 * @param second
	 *            {@link Calendar}, first date
	 * @return {@code true} if first date is after second
	 */
	public static boolean isAfter(DatePart datePart, Calendar first,
			Calendar second) {
		return first.after(second) && !isSame(datePart, first, second);
	}

	/**
	 * Check if first date is after or the same as second taking until specified
	 * DatePart (inclusive).
	 * 
	 * @param datePart
	 *            {@link DatePart}, date part until which will be compared
	 * @param first
	 *            {@link Date}, first date
	 * @param second
	 *            {@link Date}, first date
	 * @return {@code true} if first date is after second or if there are same
	 */
	public static boolean isAfterOrSame(DatePart datePart, Date first,
			Date second) {
		return first.after(second) || isSame(datePart, first, second);
	}

	/**
	 * Check if first date is after or the same as second taking until specified
	 * DatePart (inclusive).
	 * 
	 * @param datePart
	 *            {@link DatePart}, date part until which will be compared
	 * @param first
	 *            {@link Calendar}, first date
	 * @param second
	 *            {@link Calendar}, first date
	 * @return {@code true} if first date is before second or if there are same
	 */
	public static boolean isAfterOrSame(DatePart datePart, Calendar first,
			Calendar second) {
		return first.after(second) || isSame(datePart, first, second);
	}

	/**
	 * Check if the specified dates represented by the calendars are the same
	 * until specified part of date, and the calendars are from same type.
	 * 
	 * @param part
	 *            {@link DatePart}, part of the day until which should be
	 *            checked for equality
	 * @param calendar1
	 *            {@link Date}, first date to check
	 * @param calendar2
	 *            {@link Date}, second date to check
	 * @return true if the dates are equal until specified part of the day and
	 *         false if the dates have difference until specified
	 *         <code>part</code>
	 * @see #isSame(DatePart, Calendar, Calendar)
	 */
	public static boolean isSameLocalTime(DatePart part, Calendar calendar1,
			Calendar calendar2) {
		if ((calendar1 == null) || (calendar2 == null)) {
			throw new IllegalArgumentException(NOT_NULL_DATE_MESSAGE);
		}
		return (calendar1.getClass() == calendar2.getClass())
				&& isSame(part, calendar1, calendar2);
	}

	/**
	 * Add to specified date <code>count</code> parts of date. If the part is
	 * set to {@link DatePart#DAY} then to date will be added count days. If
	 * part is second then to date will be added count seconds.
	 * 
	 * @param part
	 *            {@link DatePart}, part of date to add
	 * @param date
	 *            {@link Date}, base date on which will be added
	 * @param count
	 *            int, number of {@link DatePart}s to add
	 * @return new Date instance with added parts of date
	 */
	public static Date add(DatePart part, Date date, int count) {
		if (date == null) {
			throw new IllegalArgumentException(NOT_NULL_DATE_MESSAGE);
		}
		return add(part, getCalendar(date), count).getTime();
	}

	/**
	 * Add to specified date <code>count</code> parts of date. If the part is
	 * set to {@link DatePart#DAY} then to date will be added count days. If
	 * part is second then to date will be added count seconds.
	 * 
	 * @param part
	 *            {@link DatePart}, part of date to add
	 * @param calendar
	 *            {@link Calendar}, base date on which will be added
	 * @param count
	 *            int, number of {@link DatePart}s to add
	 * @return the calendar object
	 */
	public static Calendar add(DatePart part, Calendar calendar, int count) {
		if (calendar == null) {
			throw new IllegalArgumentException(NOT_NULL_DATE_MESSAGE);
		}
		calendar.add(part.getCalendarValue(), count);
		return calendar;
	}

	/**
	 * Iterate dates with specified amount of time. The amount of time is
	 * specified according the part and distance values. If part is
	 * {@link DatePart#HOUR} and distance is 2 then, the iterator will return
	 * {@link Date}s with 2 hour difference.
	 * 
	 * @param start
	 *            {@link Date}, start date. This date will be the first returned
	 *            date
	 * @param part
	 *            {@link DatePart}, part of date which will be changed
	 * @param distance
	 *            int, amount of date parts between two dates
	 * @return {@link Iterator}<{@link Date}>, iterator for dates. Iterators
	 *         method <code>hasNext()</code> always return <code>true</code>.
	 *         Method <code>remove()</code> is not supported and always throws
	 *         {@link UnsupportedOperationException}
	 */
	public static Iterator<Date> iterator(Date start, DatePart part,
			int distance) {
		return new DateIterator(start, part, distance);
	}

	/**
	 * Iterate dates with specified amount of time. The amount of time is
	 * specified according the part and distance values. If part is
	 * {@link DatePart#HOUR} and distance is 2 then, the iterator will return
	 * {@link Date}s with 2 hour difference. The iterator end when the date
	 * reach or exceed the end value.
	 * 
	 * @param start
	 *            {@link Date}, start date. This date will be the first returned
	 *            date
	 * @param end
	 *            {@link Date}, end date. This date will be the last returned
	 *            date, if it is reachable by the iterator
	 * @param part
	 *            {@link DatePart}, part of date which will be changed
	 * @param distance
	 *            int, amount of date parts between two dates
	 * @return {@link Iterator}<{@link Date}>, iterator for dates. Iterators
	 *         method <code>remove()</code> is not supported and always throws
	 *         {@link UnsupportedOperationException}
	 */
	public static Iterator<Date> iterator(Date start, Date end, DatePart part,
			int distance) {
		return new DateIterator(start, end, part, distance);
	}

	/**
	 * Iterate dates with specified amount of time. The amount of time is
	 * specified according the part and distance values. If part is
	 * {@link DatePart#HOUR} and distance is 2 then, the iterator will return
	 * {@link Date}s with 2 hour difference.
	 * 
	 * @param start
	 *            {@link Calendar}, start date. The dates value of the calendar
	 *            will be the first returned date. The calendar is copied and
	 *            not used, so changing the provided parameter does not affect
	 *            the iteration
	 * @param part
	 *            {@link DatePart}, part of date which will be changed
	 * @param distance
	 *            int, amount of date parts between two dates
	 * @return {@link Calendar}<{@link Date}>, iterator for dates. Iterators
	 *         method <code>hasNext()</code> always return <code>true</code>.
	 *         Method <code>remove()</code> is not supported and always throws
	 *         {@link UnsupportedOperationException}
	 */
	public static Iterator<Date> iterator(Calendar start, DatePart part,
			int distance) {
		return new DateIterator(start, part, distance);
	}

	/**
	 * Iterate dates with specified amount of time. The amount of time is
	 * specified according the part and distance values. If part is
	 * {@link DatePart#HOUR} and distance is 2 then, the iterator will return
	 * {@link Date}s with 2 hour difference. The iterator end when the date
	 * reach or exceed the end value.
	 * 
	 * @param start
	 *            {@link Calendar}, start date. The dates value of the calendar
	 *            will be the first returned date. The calendar is copied and
	 *            not used, so changing the provided parameter does not affect
	 *            the iteration
	 * @param end
	 *            {@link Calendar}, end date. The dates value of the calendar
	 *            will be the last returned date, if it is reachable by the
	 *            iterator. The calendar is copied and not used, so changing the
	 *            provided parameter does not affect the iteration
	 * @param part
	 *            {@link DatePart}, part of date which will be changed
	 * @param distance
	 *            int, amount of date parts between two dates
	 * @return {@link Iterator}<{@link Date}>, iterator for dates. Iterators
	 *         method <code>remove()</code> is not supported and always throws
	 *         {@link UnsupportedOperationException}
	 */
	public static Iterator<Date> iterator(Calendar start, Calendar end,
			DatePart part, int distance) {
		return new DateIterator(start, end, part, distance);
	}

	/**
	 * Returned iterator will iterate until the part of date specified by range
	 * For example: if start is 2008-07-30 17:22:30:222 and range is set to
	 * HOUR, then the last possible value which the iterator will reach is
	 * 2008-07-30 17:59:59:999 (if part to change and distance are set so
	 * iterator can reach this value). Values of range and partToChange must set
	 * so the partToChange should not exceed the range value.
	 * 
	 * @param start
	 *            {@link Date}, start of the iterator
	 * @param partToChange
	 *            {@link DatePart}, part of the day which will be changed by the
	 *            iterator
	 * @param range
	 *            {@link DatePart}, the least significant part of the day which
	 *            should be the same for every values returned by the iterator.
	 * @param distance
	 *            int, amount of parts to be changed on iteration
	 * @return {@link Iterator}<{@link Date}> iterator for dates
	 */
	public static Iterator<Date> rangeIterator(Date start, DatePart range,
			DatePart partToChange, int distance) {
		return rangeIterator(getCalendar(start), range, partToChange, distance);
	}

	/**
	 * Returned iterator will iterate until the part of date specified by range
	 * For example: if start is 2008-07-30 17:22:30:222 and range is set to
	 * HOUR, then the last possible value which the iterator will reach is
	 * 2008-07-30 17:59:59:999 (if part to change and distance are set so
	 * iterator can reach this value). Values of range and partToChange must set
	 * so the partToChange should not exceed the range value.
	 * 
	 * @param start
	 *            {@link Calendar}, start of the iterator
	 * @param partToChange
	 *            {@link DatePart}, part of the day which will be changed by the
	 *            iterator
	 * @param range
	 *            {@link DatePart}, the least significant part of the day which
	 *            should be the same for every values returned by the iterator.
	 * @param distance
	 *            int, amount of parts to be changed on iteration
	 * @return {@link Iterator}<{@link Date}> iterator for dates
	 */
	@SuppressWarnings("fallthrough")
	public static Iterator<Date> rangeIterator(Calendar start, DatePart range,
			DatePart partToChange, int distance) {
		if (partToChange.ordinal() > range.ordinal()) {
			throw new IllegalArgumentException(DIFFERENT_RANGE_MESSAGE);
		}
		Calendar newCal = (Calendar) start.clone();
		DatePart setRange = range;
		int startWeek;
		iteration: do {
			switch (setRange) {
			case ERA:
				if (newCal.get(Calendar.ERA) == 0) {
					newCal.set(DatePart.YEAR.getCalendarValue(), 1);
				} else {
					newCal.setTime(getLastDate());
				}
			case YEAR:
				newCal.set(DatePart.MONTH.getCalendarValue(), 11);
			case MONTH:
				newCal.set(DatePart.DAY_OF_MONTH.getCalendarValue(),
						getMaximumDaysInMonth(newCal));
			case DAY:
			case DAY_OF_MONTH:
			case DAY_OF_WEEK:
			case DAY_OF_YEAR:
				newCal.set(DatePart.HOUR_OF_DAY.getCalendarValue(), 23);
			case HOUR:
			case HOUR_OF_DAY:
			case HOUR_OF_HALF_DAY:
				newCal.set(DatePart.MINUTE.getCalendarValue(), 59);
			case MINUTE:
				newCal.set(DatePart.SECOND.getCalendarValue(), 59);
			case SECOND:
				newCal.set(DatePart.MILLISECOND.getCalendarValue(), 999);
			case MILLISECOND:
				break;
			case WEEK:
			case WEEK_OF_MONTH:
			case DAY_OF_WEEK_IN_MONTH:
			case WEEK_OF_YEAR:
				startWeek = newCal.get(setRange.getCalendarValue());
				do {
					add(DatePart.DAY_OF_YEAR, newCal, 1);
				} while (newCal.get(setRange.getCalendarValue()) == startWeek);
				add(DatePart.DAY_OF_YEAR, newCal, -1);
				setRange = DatePart.HOUR_OF_DAY;
				continue iteration;
			case HALF_DAY:
				newCal.set(DatePart.HOUR_OF_HALF_DAY.getCalendarValue(), 11);
				setRange = DatePart.HOUR_OF_DAY;
				continue iteration;
			default:
				throw new UnsupportedOperationException(
						"DateUtils.rangeIterator(Calendar, DatePart, DatePart, int) method is not implemented for "
								+ range.name() + " range");
			}
			break;
		} while (true);
		return new DateIterator(start, newCal, partToChange, distance);
	}

	/**
	 * Retrieve calendar set to specified date.
	 * 
	 * @param date
	 *            {@link Date}, date for setting the calendar
	 * @return {@link Calendar}, the calendar set to provided date
	 */
	public static Calendar getCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * Retrieve Gregorian calendar set to specified date.
	 * 
	 * @param date
	 *            {@link Date}, date for setting the calendar
	 * @return {@link GregorianCalendar}, the calendar set to provided date
	 */
	public static GregorianCalendar getGregorianCalendar(Date date) {
		GregorianCalendar result = new GregorianCalendar();
		result.setTime(date);
		return result;
	}

	/**
	 * Retrieve the maximum days in the current month specified by the
	 * <code>date</code>parameter.
	 * 
	 * @param date
	 *            {@link Date}, date for which month to get number of days
	 * @return int, the number of days in the month
	 */
	public static int getMaximumDaysInMonth(Date date) {
		return getMaximumDaysInMonth(getCalendar(date));
	}

	/**
	 * Retrieve the maximum days in the current month specified by the
	 * <code>date</code>parameter.
	 * 
	 * @param cal
	 *            {@link Calendar}, date for which month to get number of days
	 * @return int, the number of days in the month
	 */
	public static int getMaximumDaysInMonth(Calendar cal) {
		int month = cal.get(Calendar.MONTH);
		switch (month) {
		case 0:
		case 2:
		case 4:
		case 6:
		case 7:
		case 9:
		case 11:
			return 31;
		case 1:
			if (isLeapYear(cal)) {
				return 29;
			}
			return 28;
		default:
			return 30;
		}
	}

	/**
	 * Check if the year is leap or not.
	 * 
	 * @param date
	 *            {@link Date}, date from which to take the year
	 * @return boolean, true if the year is leap, false if the year is not leap
	 */
	public static boolean isLeapYear(Date date) {
		GregorianCalendar cal = getGregorianCalendar(date);
		if (cal.get(Calendar.ERA) == 0) {
			return cal.isLeapYear(1 - cal.get(Calendar.YEAR));
		}
		return cal.isLeapYear(cal.get(Calendar.YEAR));
	}

	/**
	 * Check if the year is leap or not.
	 * 
	 * @param cal
	 *            {@link Calendar}, calendar from which to take the year
	 * @return boolean, true if the year is leap, false if the year is not leap
	 */
	public static boolean isLeapYear(Calendar cal) {
		GregorianCalendar gCal;
		if (cal instanceof GregorianCalendar) {
			gCal = (GregorianCalendar) cal;
		} else {
			gCal = getGregorianCalendar(cal.getTime());
		}
		return isLeapYear(gCal);
	}

	/**
	 * Check if the year is leap or not. This method handle the BCA era
	 * requirements.
	 * 
	 * @param cal
	 *            {@link GregorianCalendar}, calendar from which to take the
	 *            year
	 * @return boolean, true if the year is leap, false if the year is not leap
	 */
	public static boolean isLeapYear(GregorianCalendar cal) {
		if (cal.get(Calendar.ERA) == 0) {
			return cal.isLeapYear(1 - cal.get(Calendar.YEAR));
		}
		return cal.isLeapYear(cal.get(Calendar.YEAR));
	}

	/**
	 * Retrieve the last possible date which can be represented by the
	 * {@link Date} object.
	 * 
	 * @return {@link Date}, last possible date
	 */
	public static Date getLastDate() {
		return new Date(Long.MAX_VALUE);
	}

	/**
	 * Set field of date to specified value.
	 * 
	 * @param date
	 *            Date, date to be used as basic date
	 * @param part
	 *            DatePart, field to be set
	 * @param value
	 *            int, value to be set
	 * @return new Date object with changed field
	 */
	public static Date set(Date date, DatePart part, int value) {
		Calendar cal = getCalendar(date);
		cal.set(part.getCalendarValue(), value);
		return cal.getTime();
	}

	/**
	 * Get value of field of date. The field is selected with part.
	 * 
	 * @param date
	 *            Date, date, which part will be retrieved
	 * @param part
	 *            DatePart, part to be retrieved
	 * @return int value for the specified part of date
	 */
	public static int get(Date date, DatePart part) {
		Calendar cal = getCalendar(date);
		return cal.get(part.getCalendarValue());
	}

	/**
	 * Convert {@link Date} to {@link java.sql.Date}.
	 * 
	 * @param date
	 *            {@link Date}, date to be converted
	 * @return {@link java.sql.Date}, converted date
	 */
	public static java.sql.Date toSQLDate(Date date) {
		return new java.sql.Date(date.getTime());
	}

	/**
	 * Convert {@link Date} to {@link java.sql.Date}.
	 * 
	 * @param date
	 *            {@link Date}, date to be converted
	 * @return {@link java.sql.Date}, converted date
	 */
	public static Date fromSQLDate(java.sql.Date date) {
		return new Date(date.getTime());
	}

	/**
	 * Create value from specified date. The value is representation of the date
	 * calculated by the parts in the calendar instance. Note that even some of
	 * parts overlap with other(s) they are not ignored and are included in the
	 * generated value.
	 * 
	 * @param date
	 *            {@link Date}, date for which will be calculated the value
	 * @param parts
	 *            List<DatePart>, parts which should be included in the
	 *            calculated value
	 * @return {@link BigInteger}, the calculated value
	 */
	public static BigInteger getDateValue(Date date, List<DatePart> parts) {
		return getDateValue(getCalendar(date), parts);
	}

	/**
	 * Create value from specified date. The value is representation of the date
	 * calculated by the parts in the calendar instance. Note that even some of
	 * parts overlap with other(s) they are not ignored and are included in the
	 * generated value.
	 * 
	 * @param cal
	 *            {@link Calendar}, date for which will be calculated the value
	 * @param parts
	 *            List<DatePart>, parts which should be included in the
	 *            calculated value
	 * @return {@link BigInteger}, the calculated value
	 */
	public static BigInteger getDateValue(Calendar cal, List<DatePart> parts) {
		BigInteger currentValue = BigInteger.ZERO;
		long currentDistance;
		Iterator<DatePart> it = parts.iterator();
		if (!it.hasNext()) {
			return BigInteger.ZERO;
		}
		DatePart part = it.next();
		int calValue = cal.get(part.getCalendarValue()) - part.getMinValue();
		currentValue = BigInteger.valueOf(calValue);
		for (; it.hasNext();) {
			part = it.next();
			/* x = (x+1)dist - 1 */
			currentDistance = part.getMaxValue() - part.getMinValue() + 1;
			calValue = cal.get(part.getCalendarValue()) - part.getMinValue();
			currentValue = currentValue.multiply(
					BigInteger.valueOf(currentDistance)).add(
					BigInteger.valueOf(calValue));
		}
		return currentValue;
	}

	/**
	 * Creates new date based on the given arguments.
	 * 
	 * @param year
	 *            is the year to set
	 * @param month
	 *            is the month to set
	 * @param day
	 *            is the day to set
	 * @return the date instance.
	 */
	public static Date createDate(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month - 1, day);
		return calendar.getTime();
	}

	/**
	 * @return {@link Calendar} instance initialized with current time zone and
	 *         time.
	 * @see #getCalendar()
	 */
	public static Calendar getCurrentDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		return calendar;
	}

	/**
	 * Returns a calendar object initialized with the current date (year, month,
	 * day) only.
	 * 
	 * @return today.
	 */
	public static Calendar getToday() {
		Calendar currentDate = getCurrentDate();
		Date today = createDate(currentDate.get(Calendar.YEAR),
				currentDate.get(Calendar.MONTH) + 1,
				currentDate.get(Calendar.DAY_OF_MONTH));
		currentDate.clear();
		currentDate.setTime(today);
		return currentDate;
	}
}
