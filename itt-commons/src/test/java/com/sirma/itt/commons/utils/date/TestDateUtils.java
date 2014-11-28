/*
 * Created on 23.07.2008 15:47:55 Author: Hristo Iliev Company: Sirma-ITT Email:
 * hristo.iliev@sirma.bg
 */
package com.sirma.itt.commons.utils.date;

import static org.testng.Assert.assertEquals;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.date.DateUtils.DatePart;

/**
 * Test class of DateUtils class.
 * 
 * @author Hristo Iliev
 */
public class TestDateUtils {

	private TimeZone defaultZone;

	/**
	 * Sets the default timezone to Sofia for the purpose of the tests.
	 */
	@BeforeClass
	public void init() {
		defaultZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Sofia"));
	}

	/**
	 * Rolls back the default timezone.
	 */
	@AfterClass
	public void after() {
		TimeZone.setDefault(defaultZone);
	}

	/**
	 * Convert list of arrays of objects to array of arrays of objects.
	 * 
	 * @param list
	 *            list to be converted to array
	 * @return Object[][] result 2-dimensional Object array
	 */
	private Object[][] listToArray(List<Object[]> list) {
		Object[][] result = new Object[list.size()][];
		int i = 0;
		for (Object[] array : list) {
			result[i++] = array;
		}
		return result;
	}

	/** format of test strings. */
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"G yyyy-MM-dd HH:mm:ss:SSS Z", new Locale("EN")); //$NON-NLS-1$ //$NON-NLS-2$

	/** changed dates according the changed field. */
	private static HashMap<DatePart, String> changedDates = new HashMap<DatePart, String>();

	/** basic date. */
	private static String staticDate = "AD 2001-01-01 00:00:00:000 +0200"; //$NON-NLS-1$

	static {
		changedDates.put(DatePart.MILLISECOND, "AD 2001-01-01 00:00:00:001 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.SECOND, "AD 2001-01-01 00:00:01:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.MINUTE, "AD 2001-01-01 00:01:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.HOUR, "AD 2001-01-01 21:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.HALF_DAY, "AD 2001-01-01 11:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.DAY, "AD 2001-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.WEEK, "AD 2001-01-04 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.MONTH, "AD 2001-02-01 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.YEAR, "AD 2002-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.ERA, "BC 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$

		changedDates.put(DatePart.HOUR_OF_HALF_DAY, "AD 2001-01-01 11:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.HOUR_OF_DAY, "AD 2001-01-01 21:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.DAY_OF_MONTH, "AD 2001-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.DAY_OF_WEEK, "AD 2001-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.DAY_OF_WEEK_IN_MONTH, "AD 2001-01-08 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.DAY_OF_YEAR, "AD 2001-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.WEEK_OF_MONTH, "AD 2001-01-08 00:00:00:000 +0200"); //$NON-NLS-1$
		changedDates.put(DatePart.WEEK_OF_YEAR, "AD 2001-01-08 00:00:00:000 +0200"); //$NON-NLS-1$
	}

	/**
	 * Provider of tests for isSame() function.
	 * 
	 * @return tests for isSame() function
	 */
	@DataProvider(name = "isSame-provider")
	public Object[][] providerIsSame() {
		ArrayList<Object[]> arrayResult = new ArrayList<Object[]>();

		for (DatePart part : DatePart.values()) {
			arrayResult.add(new Object[] { part, staticDate, staticDate });
		}

		// for (DatePart first : DatePart.values()) {
		// for (DatePart second : DatePart.values()) {
		// if (first.ordinal() > second.ordinal()) {
		// arrayResult.add(new Object[] { first, staticDate,
		// changedDates.get(second) });
		// }
		// }
		// }

		arrayResult.add(new Object[] { DatePart.SECOND, staticDate,
				changedDates.get(DatePart.MILLISECOND) });

		arrayResult.add(new Object[] { DatePart.MINUTE, staticDate,
				changedDates.get(DatePart.MILLISECOND) });
		arrayResult.add(new Object[] { DatePart.MINUTE, staticDate,
				changedDates.get(DatePart.SECOND) });

		arrayResult.add(new Object[] { DatePart.HOUR_OF_HALF_DAY, staticDate,
				changedDates.get(DatePart.MILLISECOND) });
		arrayResult.add(new Object[] { DatePart.HOUR_OF_HALF_DAY, staticDate,
				changedDates.get(DatePart.SECOND) });
		arrayResult.add(new Object[] { DatePart.HOUR_OF_HALF_DAY, staticDate,
				changedDates.get(DatePart.MINUTE) });

		arrayResult.add(new Object[] { DatePart.HOUR, staticDate,
				changedDates.get(DatePart.MILLISECOND) });
		arrayResult
				.add(new Object[] { DatePart.HOUR, staticDate, changedDates.get(DatePart.SECOND) });
		arrayResult
				.add(new Object[] { DatePart.HOUR, staticDate, changedDates.get(DatePart.MINUTE) });

		arrayResult.add(new Object[] { DatePart.HALF_DAY, staticDate,
				changedDates.get(DatePart.MILLISECOND) });
		arrayResult.add(new Object[] { DatePart.HALF_DAY, staticDate,
				changedDates.get(DatePart.SECOND) });
		arrayResult.add(new Object[] { DatePart.HALF_DAY, staticDate,
				changedDates.get(DatePart.MINUTE) });
		arrayResult.add(new Object[] { DatePart.HALF_DAY, staticDate,
				changedDates.get(DatePart.HOUR_OF_HALF_DAY) });

		arrayResult.add(new Object[] { DatePart.DAY, staticDate,
				changedDates.get(DatePart.MILLISECOND) });
		arrayResult
				.add(new Object[] { DatePart.DAY, staticDate, changedDates.get(DatePart.SECOND) });
		arrayResult
				.add(new Object[] { DatePart.DAY, staticDate, changedDates.get(DatePart.MINUTE) });
		arrayResult.add(new Object[] { DatePart.DAY, staticDate, changedDates.get(DatePart.HOUR) });
		arrayResult.add(new Object[] { DatePart.DAY, staticDate,
				changedDates.get(DatePart.HOUR_OF_DAY) });
		arrayResult.add(new Object[] { DatePart.DAY, staticDate,
				changedDates.get(DatePart.HOUR_OF_HALF_DAY) });
		arrayResult.add(new Object[] { DatePart.DAY, staticDate,
				changedDates.get(DatePart.HALF_DAY) });

		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.MILLISECOND) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.SECOND) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.MINUTE) });
		arrayResult
				.add(new Object[] { DatePart.MONTH, staticDate, changedDates.get(DatePart.HOUR) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.HOUR_OF_DAY) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.HOUR_OF_HALF_DAY) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.HALF_DAY) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.DAY_OF_MONTH) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.DAY_OF_WEEK_IN_MONTH) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.DAY_OF_WEEK) });
		arrayResult.add(new Object[] { DatePart.MONTH, staticDate,
				changedDates.get(DatePart.WEEK_OF_MONTH) });

		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.MILLISECOND) });
		arrayResult
				.add(new Object[] { DatePart.YEAR, staticDate, changedDates.get(DatePart.SECOND) });
		arrayResult
				.add(new Object[] { DatePart.YEAR, staticDate, changedDates.get(DatePart.MINUTE) });
		arrayResult
				.add(new Object[] { DatePart.YEAR, staticDate, changedDates.get(DatePart.HOUR) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.HOUR_OF_DAY) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.HOUR_OF_HALF_DAY) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.HALF_DAY) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate, changedDates.get(DatePart.DAY) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.DAY_OF_MONTH) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.DAY_OF_WEEK) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.DAY_OF_WEEK_IN_MONTH) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.DAY_OF_YEAR) });
		arrayResult
				.add(new Object[] { DatePart.YEAR, staticDate, changedDates.get(DatePart.WEEK) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.WEEK_OF_MONTH) });
		arrayResult.add(new Object[] { DatePart.YEAR, staticDate,
				changedDates.get(DatePart.WEEK_OF_YEAR) });

		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.MILLISECOND) });
		arrayResult
				.add(new Object[] { DatePart.ERA, staticDate, changedDates.get(DatePart.SECOND) });
		arrayResult
				.add(new Object[] { DatePart.ERA, staticDate, changedDates.get(DatePart.MINUTE) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate, changedDates.get(DatePart.HOUR) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.HOUR_OF_DAY) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.HOUR_OF_HALF_DAY) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.HALF_DAY) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate, changedDates.get(DatePart.DAY) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.DAY_OF_MONTH) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.DAY_OF_WEEK) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.DAY_OF_WEEK_IN_MONTH) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.DAY_OF_YEAR) });
		arrayResult
				.add(new Object[] { DatePart.ERA, staticDate, changedDates.get(DatePart.MONTH) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate, changedDates.get(DatePart.WEEK) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.WEEK_OF_MONTH) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate,
				changedDates.get(DatePart.WEEK_OF_YEAR) });
		arrayResult.add(new Object[] { DatePart.ERA, staticDate, changedDates.get(DatePart.YEAR) });
		return listToArray(arrayResult);
	}

	/**
	 * Test the isSame function according the provided.
	 * 
	 * @param part
	 *            part argument of isSame method
	 * @param date1
	 *            first date
	 * @param date2
	 *            second date
	 * @throws ParseException
	 *             if the provided test date is with different syntax
	 * @see DateUtils#isSame(DatePart, Date, Date)
	 */
	@Test(dataProvider = "isSame-provider", groups = { "isSame" })
	public void testIsSame(DatePart part, String date1, String date2) throws ParseException {
		Assert.assertEquals(
				DateUtils.isSame(part, dateFormat.parse(date1), dateFormat.parse(date2)), true);
	}

	/**
	 * Provider for test for iterator method.
	 * 
	 * @return iterator tests
	 */
	@DataProvider(name = "iterator-provider")
	public Object[][] providerIterator() {
		ArrayList<Object[]> arrayResult = new ArrayList<Object[]>();
		String staticIteratorDate = "AD 2001-01-01 00:00:00:000 +0200"; //$NON-NLS-1$
		ArrayList<String> miliseconds = new ArrayList<String>();
		miliseconds.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:010 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:020 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:030 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:040 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:050 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:060 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:070 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:080 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:090 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:100 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:110 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:120 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:130 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:140 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:150 +0200"); //$NON-NLS-1$
		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MILLISECOND,
				Integer.valueOf(10), miliseconds });

		ArrayList<String> seconds = new ArrayList<String>();
		seconds.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:03:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:06:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:09:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:12:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:15:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:18:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:21:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:24:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:27:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:30:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:33:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:36:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:39:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:42:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:45:000 +0200"); //$NON-NLS-1$
		arrayResult.add(new Object[] { staticIteratorDate, DatePart.SECOND, Integer.valueOf(3),
				seconds });

		ArrayList<String> minutes = new ArrayList<String>();
		minutes.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:02:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:04:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:06:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:08:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:10:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:12:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:14:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:16:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:18:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:20:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:22:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:24:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:26:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:28:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:30:00:000 +0200"); //$NON-NLS-1$
		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MINUTE, Integer.valueOf(2),
				minutes });

		ArrayList<String> hours = new ArrayList<String>();
		hours.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 12:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 14:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 16:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 18:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 20:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 22:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-02 02:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-02 04:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-02 06:00:00:000 +0200"); //$NON-NLS-1$
		arrayResult
				.add(new Object[] { staticIteratorDate, DatePart.HOUR, Integer.valueOf(2), hours });

		ArrayList<String> halfDay = new ArrayList<String>();
		halfDay.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-01 12:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-02 12:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-03 00:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-03 12:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-04 00:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-04 12:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-05 00:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-05 12:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-06 00:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-06 12:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-07 00:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-07 12:00:00:000 +0200"); //$NON-NLS-1$
		halfDay.add("AD 2001-01-08 00:00:00:000 +0200"); //$NON-NLS-1$
		arrayResult.add(new Object[] { staticIteratorDate, DatePart.HALF_DAY, Integer.valueOf(1),
				halfDay });

		ArrayList<String> days = new ArrayList<String>();
		days.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-03 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-05 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-07 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-09 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-11 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-13 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-15 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-17 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-19 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-21 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-23 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-25 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-27 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-29 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-31 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-02-02 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-02-04 00:00:00:000 +0200"); //$NON-NLS-1$
		arrayResult
				.add(new Object[] { staticIteratorDate, DatePart.DAY, Integer.valueOf(2), days });

		ArrayList<String> months = new ArrayList<String>();
		months.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2001-03-31 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2001-06-30 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2001-09-30 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2002-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2002-03-31 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2002-06-30 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2002-09-30 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2003-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2003-03-31 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2003-06-30 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months.add("AD 2003-09-30 23:00:00:000 +0200"); //$NON-NLS-1$
		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MONTH, Integer.valueOf(3),
				months });

		ArrayList<String> months2 = new ArrayList<String>();
		months2.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2001-03-01 00:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months2.add("AD 2001-04-30 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months2.add("AD 2001-06-30 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months2.add("AD 2001-08-31 23:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2001-11-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2002-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2002-03-01 00:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months2.add("AD 2002-04-30 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months2.add("AD 2002-06-30 23:00:00:000 +0200"); //$NON-NLS-1$
		// due daylight saving of SimpleDateFormat
		months2.add("AD 2002-08-31 23:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2002-11-01 00:00:00:000 +0200"); //$NON-NLS-1$
		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MONTH, Integer.valueOf(2),
				months2 });

		ArrayList<String> years = new ArrayList<String>();
		years.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2002-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2003-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2004-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2005-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2006-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2007-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2008-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2009-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2010-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2011-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2012-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2013-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2014-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("AD 2015-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		arrayResult
				.add(new Object[] { staticIteratorDate, DatePart.YEAR, Integer.valueOf(1), years });

		ArrayList<String> years2 = new ArrayList<String>();
		years2.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("AD 1501-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("AD 1001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("AD 0501-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("AD 0001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("BC 0500-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("BC 1000-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("BC 1500-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("BC 2000-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("BC 2500-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years2.add("BC 3000-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		arrayResult.add(new Object[] { staticIteratorDate, DatePart.YEAR, Integer.valueOf(-500),
				years2 });

		return listToArray(arrayResult);
	}

	/**
	 * Test the iterator method.
	 * 
	 * @param start
	 *            initial date
	 * @param part
	 *            part to change
	 * @param distance
	 *            distance of iteration
	 * @param expected
	 *            expected value
	 * @throws ParseException
	 *             thrown if the date is not valid formated
	 * @see {@link DateUtils#iterator(Date, DatePart, int)
	 */
	@Test(dataProvider = "iterator-provider", groups = { "iterator" }, dependsOnGroups = { "isSame" })
	public void testIterator(String start, DatePart part, Integer distance, List<String> expected)
			throws ParseException {
		Iterator<Date> it = DateUtils.iterator(dateFormat.parse(start), part, distance.intValue());
		for (String expectedValue : expected) {
			Assert.assertEquals(
					DateUtils.isSame(DatePart.MILLISECOND, it.next(),
							dateFormat.parse(expectedValue)), true);
		}
	}

	/**
	 * Provider of test for rangeIterator tests.
	 * 
	 * @return Object[][], tests
	 */
	@DataProvider(name = "rangeIterator-provider")
	public Object[][] providerRangeIterator() {
		ArrayList<Object[]> arrayResult = new ArrayList<Object[]>();
		String staticIteratorDate = "AD 2001-01-01 00:00:00:000 +0200"; //$NON-NLS-1$
		ArrayList<String> miliseconds = new ArrayList<String>();
		miliseconds.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:100 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:200 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:300 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:400 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:500 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:600 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:700 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:800 +0200"); //$NON-NLS-1$
		miliseconds.add("AD 2001-01-01 00:00:00:900 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.SECOND, DatePart.MILLISECOND,
				Integer.valueOf(100), miliseconds });

		ArrayList<String> miliseconds2 = new ArrayList<String>();
		miliseconds2.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:111 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:222 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:333 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:444 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:555 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:666 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:777 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:888 +0200"); //$NON-NLS-1$
		miliseconds2.add("AD 2001-01-01 00:00:00:999 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.SECOND, DatePart.MILLISECOND,
				Integer.valueOf(111), miliseconds2 });

		ArrayList<String> seconds = new ArrayList<String>();
		seconds.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:06:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:12:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:18:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:24:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:30:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:36:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:42:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:48:000 +0200"); //$NON-NLS-1$
		seconds.add("AD 2001-01-01 00:00:54:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MINUTE, DatePart.SECOND,
				Integer.valueOf(6), seconds });

		ArrayList<String> seconds2 = new ArrayList<String>();
		seconds2.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:05:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:10:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:15:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:20:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:25:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:30:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:35:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:40:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:45:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:50:000 +0200"); //$NON-NLS-1$
		seconds2.add("AD 2001-01-01 00:00:55:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MINUTE, DatePart.SECOND,
				Integer.valueOf(5), seconds2 });

		ArrayList<String> seconds3 = new ArrayList<String>();
		seconds3.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		seconds3.add("AD 2001-01-01 00:00:07:000 +0200"); //$NON-NLS-1$
		seconds3.add("AD 2001-01-01 00:00:14:000 +0200"); //$NON-NLS-1$
		seconds3.add("AD 2001-01-01 00:00:21:000 +0200"); //$NON-NLS-1$
		seconds3.add("AD 2001-01-01 00:00:28:000 +0200"); //$NON-NLS-1$
		seconds3.add("AD 2001-01-01 00:00:35:000 +0200"); //$NON-NLS-1$
		seconds3.add("AD 2001-01-01 00:00:42:000 +0200"); //$NON-NLS-1$
		seconds3.add("AD 2001-01-01 00:00:49:000 +0200"); //$NON-NLS-1$
		seconds3.add("AD 2001-01-01 00:00:56:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MINUTE, DatePart.SECOND,
				Integer.valueOf(7), seconds3 });

		ArrayList<String> minutes = new ArrayList<String>();
		minutes.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:07:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:14:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:21:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:28:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:35:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:42:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:49:00:000 +0200"); //$NON-NLS-1$
		minutes.add("AD 2001-01-01 00:56:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.HOUR_OF_DAY, DatePart.MINUTE,
				Integer.valueOf(7), minutes });

		ArrayList<String> minutes2 = new ArrayList<String>();
		minutes2.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:06:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:12:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:18:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:24:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:30:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:36:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:42:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:48:00:000 +0200"); //$NON-NLS-1$
		minutes2.add("AD 2001-01-01 00:54:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.HOUR_OF_DAY, DatePart.MINUTE,
				Integer.valueOf(6), minutes2 });

		ArrayList<String> minutes3 = new ArrayList<String>();
		minutes3.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:05:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:10:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:15:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:20:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:25:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:30:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:35:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:40:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:45:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:50:00:000 +0200"); //$NON-NLS-1$
		minutes3.add("AD 2001-01-01 00:55:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.HOUR_OF_DAY, DatePart.MINUTE,
				Integer.valueOf(5), minutes3 });

		ArrayList<String> hours = new ArrayList<String>();
		hours.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 01:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 03:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 05:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 07:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 09:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 11:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 12:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 13:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 14:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 15:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 16:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 17:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 18:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 19:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 20:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 21:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 22:00:00:000 +0200"); //$NON-NLS-1$
		hours.add("AD 2001-01-01 23:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.DAY_OF_MONTH,
				DatePart.HOUR_OF_DAY, Integer.valueOf(1), hours });

		ArrayList<String> hours2 = new ArrayList<String>();
		hours2.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 12:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 14:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 16:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 18:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 20:00:00:000 +0200"); //$NON-NLS-1$
		hours2.add("AD 2001-01-01 22:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.DAY_OF_MONTH,
				DatePart.HOUR_OF_DAY, Integer.valueOf(2), hours2 });

		ArrayList<String> hours3 = new ArrayList<String>();
		hours3.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 01:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 03:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 05:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 07:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 09:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$
		hours3.add("AD 2001-01-01 11:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.HALF_DAY,
				DatePart.HOUR_OF_HALF_DAY, Integer.valueOf(1), hours3 });

		ArrayList<String> hours4 = new ArrayList<String>();
		hours4.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours4.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours4.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours4.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours4.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours4.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.HALF_DAY,
				DatePart.HOUR_OF_HALF_DAY, Integer.valueOf(2), hours4 });

		ArrayList<String> hours5 = new ArrayList<String>();
		hours5.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 01:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 03:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 05:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 07:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 09:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 11:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 12:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 13:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 14:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 15:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 16:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 17:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 18:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 19:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 20:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 21:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 22:00:00:000 +0200"); //$NON-NLS-1$
		hours5.add("AD 2001-01-01 23:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.DAY_OF_WEEK,
				DatePart.HOUR_OF_DAY, Integer.valueOf(1), hours5 });

		ArrayList<String> hours6 = new ArrayList<String>();
		hours6.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 12:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 14:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 16:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 18:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 20:00:00:000 +0200"); //$NON-NLS-1$
		hours6.add("AD 2001-01-01 22:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.DAY_OF_WEEK,
				DatePart.HOUR_OF_DAY, Integer.valueOf(2), hours6 });

		ArrayList<String> hours9 = new ArrayList<String>();
		hours9.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 01:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 03:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 05:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 07:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 09:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 11:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 12:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 13:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 14:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 15:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 16:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 17:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 18:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 19:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 20:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 21:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 22:00:00:000 +0200"); //$NON-NLS-1$
		hours9.add("AD 2001-01-01 23:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.DAY_OF_YEAR,
				DatePart.HOUR_OF_DAY, Integer.valueOf(1), hours9 });

		ArrayList<String> hours10 = new ArrayList<String>();
		hours10.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 02:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 04:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 06:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 08:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 10:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 12:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 14:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 16:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 18:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 20:00:00:000 +0200"); //$NON-NLS-1$
		hours10.add("AD 2001-01-01 22:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.DAY_OF_YEAR,
				DatePart.HOUR_OF_DAY, Integer.valueOf(2), hours10 });

		ArrayList<String> days = new ArrayList<String>();
		days.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-03 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-04 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-05 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-06 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-07 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-08 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-09 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-10 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-11 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-12 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-13 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-14 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-15 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-16 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-17 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-18 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-19 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-20 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-21 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-22 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-23 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-24 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-25 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-26 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-27 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-28 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-29 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-30 00:00:00:000 +0200"); //$NON-NLS-1$
		days.add("AD 2001-01-31 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MONTH, DatePart.DAY_OF_MONTH,
				Integer.valueOf(1), days });

		ArrayList<String> days2 = new ArrayList<String>();
		days2.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-03 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-05 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-07 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-09 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-11 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-13 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-15 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-17 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-19 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-21 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-23 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-25 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-27 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-29 00:00:00:000 +0200"); //$NON-NLS-1$
		days2.add("AD 2001-01-31 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MONTH, DatePart.DAY_OF_MONTH,
				Integer.valueOf(2), days2 });

		ArrayList<String> days3 = new ArrayList<String>();
		days3.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-04 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-07 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-10 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-13 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-16 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-19 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-22 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-25 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-28 00:00:00:000 +0200"); //$NON-NLS-1$
		days3.add("AD 2001-01-31 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MONTH, DatePart.DAY_OF_MONTH,
				Integer.valueOf(3), days3 });

		ArrayList<String> days4 = new ArrayList<String>();
		days4.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days4.add("AD 2001-01-05 00:00:00:000 +0200"); //$NON-NLS-1$
		days4.add("AD 2001-01-09 00:00:00:000 +0200"); //$NON-NLS-1$
		days4.add("AD 2001-01-13 00:00:00:000 +0200"); //$NON-NLS-1$
		days4.add("AD 2001-01-17 00:00:00:000 +0200"); //$NON-NLS-1$
		days4.add("AD 2001-01-21 00:00:00:000 +0200"); //$NON-NLS-1$
		days4.add("AD 2001-01-25 00:00:00:000 +0200"); //$NON-NLS-1$
		days4.add("AD 2001-01-29 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.MONTH, DatePart.DAY_OF_MONTH,
				Integer.valueOf(4), days4 });

		ArrayList<String> days5 = new ArrayList<String>();
		days5.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days5.add("AD 2001-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		days5.add("AD 2001-01-03 00:00:00:000 +0200"); //$NON-NLS-1$
		days5.add("AD 2001-01-04 00:00:00:000 +0200"); //$NON-NLS-1$
		days5.add("AD 2001-01-05 00:00:00:000 +0200"); //$NON-NLS-1$
		days5.add("AD 2001-01-06 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.WEEK_OF_MONTH,
				DatePart.DAY_OF_WEEK, Integer.valueOf(1), days5 });

		ArrayList<String> days6 = new ArrayList<String>();
		days6.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days6.add("AD 2001-01-04 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.WEEK_OF_MONTH,
				DatePart.DAY_OF_WEEK, Integer.valueOf(3), days6 });

		ArrayList<String> days7 = new ArrayList<String>();
		days7.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days7.add("AD 2001-01-06 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.WEEK_OF_MONTH,
				DatePart.DAY_OF_WEEK, Integer.valueOf(5), days7 });

		ArrayList<String> days8 = new ArrayList<String>();
		days8.add("AD 2001-02-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days8.add("AD 2001-02-04 00:00:00:000 +0200"); //$NON-NLS-1$
		days8.add("AD 2001-02-07 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { "AD 2001-02-01 00:00:00:000 +0200", //$NON-NLS-1$
				DatePart.DAY_OF_WEEK_IN_MONTH, DatePart.DAY_OF_WEEK, Integer.valueOf(3), days8 });

		/* Week start from Sunday */
		ArrayList<String> days9 = new ArrayList<String>();
		days9.add("AD 2001-01-28 00:00:00:000 +0200"); //$NON-NLS-1$
		days9.add("AD 2001-01-31 00:00:00:000 +0200"); //$NON-NLS-1$
		days9.add("AD 2001-02-03 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { "AD 2001-01-28 00:00:00:000 +0200", //$NON-NLS-1$
				DatePart.WEEK_OF_YEAR, DatePart.DAY_OF_YEAR, Integer.valueOf(3), days9 });

		/* Week start from Sunday */
		ArrayList<String> days10 = new ArrayList<String>();
		days10.add("AD 2001-01-28 00:00:00:000 +0200"); //$NON-NLS-1$
		days10.add("AD 2001-01-29 00:00:00:000 +0200"); //$NON-NLS-1$
		days10.add("AD 2001-01-30 00:00:00:000 +0200"); //$NON-NLS-1$
		days10.add("AD 2001-01-31 00:00:00:000 +0200"); //$NON-NLS-1$
		days10.add("AD 2001-02-01 00:00:00:000 +0200"); //$NON-NLS-1$
		days10.add("AD 2001-02-02 00:00:00:000 +0200"); //$NON-NLS-1$
		days10.add("AD 2001-02-03 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { "AD 2001-01-28 00:00:00:000 +0200", //$NON-NLS-1$
				DatePart.WEEK_OF_YEAR, DatePart.DAY_OF_YEAR, Integer.valueOf(1), days10 });

		ArrayList<String> months = new ArrayList<String>();
		months.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-02-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-03-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-03-31 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-04-30 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-05-31 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-06-30 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-07-31 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-08-31 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-09-30 23:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-11-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months.add("AD 2001-12-01 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.YEAR, DatePart.MONTH,
				Integer.valueOf(1), months });

		ArrayList<String> months2 = new ArrayList<String>();
		months2.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2001-03-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2001-04-30 23:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2001-06-30 23:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2001-08-31 23:00:00:000 +0200"); //$NON-NLS-1$
		months2.add("AD 2001-11-01 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.YEAR, DatePart.MONTH,
				Integer.valueOf(2), months2 });

		ArrayList<String> months3 = new ArrayList<String>();
		months3.add("AD 2001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		months3.add("AD 2001-07-31 23:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { staticIteratorDate, DatePart.YEAR, DatePart.MONTH,
				Integer.valueOf(7), months3 });

		ArrayList<String> years = new ArrayList<String>();
		years.add("BC 0005-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("BC 0004-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("BC 0003-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("BC 0002-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		years.add("BC 0001-01-01 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { "BC 0005-01-01 00:00:00:000 +0200", //$NON-NLS-1$
				DatePart.ERA, DatePart.YEAR, Integer.valueOf(1), years });

		ArrayList<String> yearsDays = new ArrayList<String>();
		yearsDays.add("AD 2000-01-01 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-02 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-03 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-04 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-05 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-06 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-07 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-08 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-09 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-10 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-11 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-12 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-13 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-14 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-15 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-16 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-17 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-18 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-19 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-20 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-21 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-22 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-23 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-24 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-25 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-26 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-27 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-28 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-29 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-30 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-01-31 00:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-02-01 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-02 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-03 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-04 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-05 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-06 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-07 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-08 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-09 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-10 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-11 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-12 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-13 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-14 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-15 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-16 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-17 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-18 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-19 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-20 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-21 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-22 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-23 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-24 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-25 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-26 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-27 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-28 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-02-29 00:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-03-01 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-02 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-03 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-04 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-05 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-06 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-07 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-08 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-09 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-10 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-11 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-12 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-13 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-14 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-15 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-16 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-17 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-18 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-19 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-20 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-21 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-22 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-23 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-24 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-25 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-26 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-26 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-27 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-28 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-29 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-30 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-03-31 23:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-04-01 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-02 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-03 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-04 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-05 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-06 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-07 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-08 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-09 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-10 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-11 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-12 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-13 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-14 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-15 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-16 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-17 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-18 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-19 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-20 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-21 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-22 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-23 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-24 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-25 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-26 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-27 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-28 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-29 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-04-30 23:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-05-01 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-02 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-03 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-04 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-05 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-06 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-07 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-08 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-09 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-10 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-11 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-12 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-13 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-14 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-15 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-16 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-17 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-18 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-19 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-20 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-21 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-22 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-23 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-24 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-25 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-26 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-27 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-28 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-29 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-30 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-05-31 23:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-06-01 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-02 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-03 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-04 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-05 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-06 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-07 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-08 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-09 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-10 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-11 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-12 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-13 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-14 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-15 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-16 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-17 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-18 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-19 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-20 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-21 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-22 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-23 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-24 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-25 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-26 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-27 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-28 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-29 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-06-30 23:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-07-01 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-02 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-03 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-04 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-05 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-06 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-07 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-08 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-09 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-10 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-11 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-12 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-13 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-14 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-15 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-16 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-17 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-18 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-19 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-20 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-21 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-22 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-23 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-24 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-25 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-26 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-27 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-28 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-29 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-30 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-07-31 23:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-08-01 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-02 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-03 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-04 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-05 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-06 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-07 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-08 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-09 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-10 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-11 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-12 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-13 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-14 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-15 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-16 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-17 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-18 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-19 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-20 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-21 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-22 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-23 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-24 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-25 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-26 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-27 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-28 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-29 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-30 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-08-31 23:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-09-01 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-02 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-03 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-04 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-05 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-06 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-07 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-08 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-09 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-10 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-11 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-12 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-13 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-14 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-15 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-16 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-17 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-18 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-19 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-20 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-21 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-22 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-23 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-24 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-25 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-26 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-27 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-28 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-29 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-09-30 23:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-10-01 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-02 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-03 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-04 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-05 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-06 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-07 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-08 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-09 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-10 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-11 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-12 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-13 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-14 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-15 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-16 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-17 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-18 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-19 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-20 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-21 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-22 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-23 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-24 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-25 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-26 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-27 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-28 23:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-30 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-10-31 00:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-11-01 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-02 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-03 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-04 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-05 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-06 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-07 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-08 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-09 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-10 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-11 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-12 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-13 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-14 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-15 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-16 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-17 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-18 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-19 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-20 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-21 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-22 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-23 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-24 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-25 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-26 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-27 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-28 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-29 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-11-30 00:00:00:000 +0200"); //$NON-NLS-1$

		yearsDays.add("AD 2000-12-01 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-02 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-03 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-04 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-05 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-06 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-07 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-08 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-09 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-10 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-11 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-12 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-13 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-14 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-15 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-16 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-17 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-18 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-19 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-20 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-21 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-22 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-23 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-24 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-25 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-26 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-27 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-28 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-29 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-30 00:00:00:000 +0200"); //$NON-NLS-1$
		yearsDays.add("AD 2000-12-31 00:00:00:000 +0200"); //$NON-NLS-1$

		arrayResult.add(new Object[] { "AD 2000-01-01 00:00:00:000 +0200", //$NON-NLS-1$
				DatePart.YEAR, DatePart.DAY_OF_YEAR, Integer.valueOf(1), yearsDays });

		return listToArray(arrayResult);
	}

	/**
	 * Test {@link DateUtils#rangeIterator(java.util.Calendar, DatePart, DatePart, int)} method.
	 * 
	 * @param start
	 *            date to start iterator
	 * @param bound
	 *            limit
	 * @param part
	 *            change part
	 * @param distance
	 *            amount of changed part
	 * @param expected
	 *            expected value
	 * @throws ParseException
	 *             if the test data date is incorrect
	 */
	@Test(dataProvider = "rangeIterator-provider", groups = { "iterator" }, dependsOnGroups = { "isSame" })
	public void testRangeIterator(String start, DatePart bound, DatePart part, Integer distance,
			List<String> expected) throws ParseException {
		Iterator<Date> it = DateUtils.rangeIterator(dateFormat.parse(start), bound, part,
				distance.intValue());
		Date next;
		Date expectedDate;
		for (String expectedValue : expected) {
			next = it.next();
			expectedDate = dateFormat.parse(expectedValue);
			Assert.assertEquals(DateUtils.isSame(DatePart.MILLISECOND, next, expectedDate), true);
		}
		if (it.hasNext()) {
			Assert.fail("Iterator has next element."); //$NON-NLS-1$
		}
	}

	/**
	 * Check if the year is leap.
	 * 
	 * @return tests
	 */
	@DataProvider(name = "isLeapYear-provider")
	public Object[][] providerIsLeapYear() {
		ArrayList<Object[]> arrayResult = new ArrayList<Object[]>();

		arrayResult.add(new Object[] { "AD 2000-01-01 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2001-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2002-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2003-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2004-01-01 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2005-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2006-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2007-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2008-01-01 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 2100-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 0700-02-29 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 1600-02-29 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 1700-02-28 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "AD 1800-02-28 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "BC 0001-01-01 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "BC 0002-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "BC 0003-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "BC 0004-01-01 00:00:00:000 +0200", Boolean.FALSE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "BC 0005-01-01 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "BC 16373-02-29 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$
		arrayResult.add(new Object[] { "BC 17001-02-29 00:00:00:000 +0200", Boolean.TRUE }); //$NON-NLS-1$

		return listToArray(arrayResult);
	}

	/**
	 * Test is an year is leap or not according the Gregorian calendar.
	 * 
	 * @param year
	 *            date
	 * @param expected
	 *            expected result
	 * @throws ParseException
	 *             thrown if the date is not specified correctly
	 */
	@Test(dataProvider = "isLeapYear-provider", groups = { "iterator" }, dependsOnGroups = { "isSame" })
	public void testIsLeapYear(String year, Boolean expected) throws ParseException {
		Date date = dateFormat.parse(year);
		Assert.assertEquals(DateUtils.isLeapYear(date), expected.booleanValue());
	}

	/**
	 * Provider for method for retrieving the value from date.
	 * 
	 * @return tests
	 */
	@DataProvider(name = "providerGetDateValue_String_List_BigInteger")
	public Iterator<Object[]> providerGetDateValue_String_List_BigInteger() {
		List<Object[]> result = new ArrayList<Object[]>();
		List<DatePart> parts;

		/* Date is in the summer so +1 hour */
		parts = new ArrayList<DatePart>();
		parts.add(DatePart.YEAR);
		parts.add(DatePart.DAY_OF_YEAR);
		parts.add(DatePart.HOUR);
		parts.add(DatePart.MILLISECOND);
		result.add(new Object[] {
				"AD 2005-09-21 13:05:41:087 +0200", parts, new BigInteger("17609462087") }); //$NON-NLS-1$ //$NON-NLS-2$

		parts = new ArrayList<DatePart>();
		parts.add(DatePart.DAY_OF_YEAR);
		parts.add(DatePart.HOUR);
		parts.add(DatePart.MINUTE);
		parts.add(DatePart.SECOND);
		result.add(new Object[] {
				"AD 2008-10-28 21:03:01:123 +0200", parts, new BigInteger("26082181") }); //$NON-NLS-1$ //$NON-NLS-2$

		parts = new ArrayList<DatePart>();
		parts.add(DatePart.DAY_OF_YEAR);
		parts.add(DatePart.HOUR);
		parts.add(DatePart.MINUTE);
		parts.add(DatePart.SECOND);
		result.add(new Object[] { "AD 2008-01-01 00:00:00:000 +0200", parts, new BigInteger("0") }); //$NON-NLS-1$ //$NON-NLS-2$

		return result.iterator();
	}

	/**
	 * Test getValueFromDate
	 * 
	 * @param date
	 *            date for which will be returned.
	 * @param parts
	 *            Parts of the day which will be used
	 * @param expected
	 *            expected result
	 * @throws ParseException
	 *             thrown if date is invalid
	 */
	@Test(dataProvider = "providerGetDateValue_String_List_BigInteger", groups = { "GetDateValue" })
	public void testGetDateValue_String_List_BigInteger(String date, List<DatePart> parts,
			BigInteger expected) throws ParseException {
		Date test = dateFormat.parse(date);
		assertEquals(DateUtils.getDateValue(test, parts), expected);
	}

}
