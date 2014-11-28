package com.sirma.itt.commons.utils.date;

import java.util.Calendar;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests methods of {@link DateCache} class.
 * 
 * @author SKostadinov
 */
@Test
public class DateCacheTest {
	/**
	 * Tests getDateIndex method.
	 */
	public void getDateIndexTest() {
		Assert.assertTrue(31 == DateCache.getDateIndex(366, 12, true));
		Assert.assertTrue(31 == DateCache.getDateIndex(365, 12, false));
		Assert.assertTrue(1 == DateCache.getDateIndex(336, 12, true));
		Assert.assertTrue(1 == DateCache.getDateIndex(335, 12, false));
		Assert.assertTrue(1 == DateCache.getDateIndex(1, 1, true));
		Assert.assertTrue(1 == DateCache.getDateIndex(1, 1, false));
		Assert.assertTrue(29 == DateCache.getDateIndex(60, 2, true));
		Assert.assertTrue(1 == DateCache.getDateIndex(60, 3, false));
	}

	/**
	 * Tests getMonthIndex method.
	 */
	public void getMonthIndexTest() {
		Assert.assertTrue(12 == DateCache.getMonthIndex(366, true));
		Assert.assertTrue(12 == DateCache.getMonthIndex(365, false));
		Assert.assertTrue(12 == DateCache.getMonthIndex(336, true));
		Assert.assertTrue(12 == DateCache.getMonthIndex(335, false));
		Assert.assertTrue(1 == DateCache.getMonthIndex(1, true));
		Assert.assertTrue(1 == DateCache.getMonthIndex(1, false));
		Assert.assertTrue(2 == DateCache.getMonthIndex(60, true));
		Assert.assertTrue(3 == DateCache.getMonthIndex(60, false));
	}

	/**
	 * Tests getYearIndex method.
	 */
	public void getYearIndexTest() {
		Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
		for (int i = 1; i < 12; i++) {
			for (int j = DateCache.START_YEAR; j < DateCache.END_YEAR; j++) {
				calendar.clear();
				calendar.set(j, i, i);
				Assert.assertTrue(calendar.get(Calendar.YEAR) == DateCache
						.getYearIndex((int) (calendar.getTimeInMillis() / DateCache.NUMBER_OF_MILLISECOND_IN_DAY)));
			}
		}
	}

	/**
	 * Tests isLeapYear method.
	 */
	public void isLeapYearTest() {
		Assert.assertTrue(DateCache.isLeapYear(0));
		Assert.assertTrue(DateCache.isLeapYear(400));
		Assert.assertTrue(DateCache.isLeapYear(800));
		Assert.assertTrue(DateCache.isLeapYear(1200));
		Assert.assertTrue(DateCache.isLeapYear(1600));
		Assert.assertTrue(DateCache.isLeapYear(2000));
		Assert.assertFalse(DateCache.isLeapYear(100));
		Assert.assertFalse(DateCache.isLeapYear(200));
		Assert.assertFalse(DateCache.isLeapYear(300));
		Assert.assertFalse(DateCache.isLeapYear(500));
		Assert.assertFalse(DateCache.isLeapYear(600));
		Assert.assertFalse(DateCache.isLeapYear(700));
		Assert.assertTrue(DateCache.isLeapYear(4));
		Assert.assertTrue(DateCache.isLeapYear(8));
		Assert.assertTrue(DateCache.isLeapYear(16));
		Assert.assertTrue(DateCache.isLeapYear(20));
		Assert.assertTrue(DateCache.isLeapYear(24));
		Assert.assertTrue(DateCache.isLeapYear(28));
		Assert.assertFalse(DateCache.isLeapYear(1));
		Assert.assertFalse(DateCache.isLeapYear(2));
		Assert.assertFalse(DateCache.isLeapYear(3));
		Assert.assertFalse(DateCache.isLeapYear(5));
		Assert.assertFalse(DateCache.isLeapYear(6));
		Assert.assertFalse(DateCache.isLeapYear(7));
	}

	/**
	 * Tests getDate method.
	 */
	public void getDateTest() {
		Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
		for (int i = 1; i < 12; i++) {
			for (int j = DateCache.START_YEAR; j < DateCache.END_YEAR; j++) {
				calendar.clear();
				calendar.set(j, i, i);
				Assert.assertEquals(calendar.getTime(),
						DateCache.getDate(j, i + 1, i));
			}
		}
	}

	/**
	 * Tests getDate method.
	 */
	public void getDateTestTwo() {
		Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
		Calendar calendarTwo = Calendar.getInstance(DateCache.TIME_ZONE);
		for (int k = 1; k < 12; k++) {
			for (int i = 1; i < 12; i++) {
				for (int j = DateCache.START_YEAR; j < DateCache.END_YEAR; j++) {
					calendar.clear();
					calendar.set(j, i, i);
					calendarTwo.clear();
					calendarTwo.set(j, i, i, k, k, k);
					Assert.assertEquals(calendar.getTime(),
							DateCache.getDate(calendarTwo.getTimeInMillis()));
				}
			}
		}
	}

	/**
	 * Tests getDate method.
	 */
	public void getDateTestThree() {
		Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
		Calendar calendarTwo = Calendar.getInstance(DateCache.TIME_ZONE);
		for (int k = 1; k < 12; k++) {
			for (int i = 1; i < 12; i++) {
				for (int j = DateCache.START_YEAR; j < DateCache.END_YEAR; j++) {
					calendar.clear();
					calendar.set(j, i, i);
					calendarTwo.clear();
					calendarTwo.set(j, i, i, k, k, k);
					Assert.assertEquals(calendar.getTime(),
							DateCache.getDate(calendarTwo.getTime()));
				}
			}
		}
	}

	/**
	 * Tests getCurrentDate method.
	 */
	public void getCurrentDateTwo() {
		Calendar calendarTwo = Calendar.getInstance(DateCache.TIME_ZONE);
		Calendar calendar = Calendar.getInstance(DateCache.TIME_ZONE);
		calendar.clear();
		calendar.set(calendarTwo.get(Calendar.YEAR),
				calendarTwo.get(Calendar.MONTH), calendarTwo.get(Calendar.DATE));
		Assert.assertEquals(calendar.getTime(),
				DateCache.getDate(calendarTwo.getTimeInMillis()));
	}
}