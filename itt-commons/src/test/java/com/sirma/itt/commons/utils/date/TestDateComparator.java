package com.sirma.itt.commons.utils.date;

import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test functionality of DateComparator.
 * 
 * @author SKostadinov
 */
@Test
public class TestDateComparator {
	/**
	 * Test isEqual method.
	 */
	public void testIsEqual() {
		// test when one of the dates is null
		Assert.assertTrue(DateComparator.isEqual(null, null, null));

		Assert.assertFalse(DateComparator.isEqual(new Date(), null, null));
		Assert.assertFalse(DateComparator.isEqual(null, new Date(), null));

		// test for stripped dates
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 10, 10, 10, 10, 10);
		Date one = calendar.getTime();
		Date two = null;

		calendar.set(2000, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isEqual(one, two, null));

		calendar.set(2000, 10, 10, 10, 10, 0);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isEqual(one, two, null));
		calendar.set(2000, 10, 10, 10, 10, 0);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isEqual(one, two,
				DateStripType.STRIP_SECOND));

		calendar.set(2000, 10, 10, 10, 0, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isEqual(one, two,
				DateStripType.STRIP_SECOND));
		calendar.set(2000, 10, 10, 10, 0, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isEqual(one, two,
				DateStripType.STRIP_MINUTE));

		calendar.set(2000, 10, 10, 0, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isEqual(one, two,
				DateStripType.STRIP_MINUTE));
		calendar.set(2000, 10, 10, 0, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isEqual(one, two,
				DateStripType.STRIP_HOUR));

		calendar.set(2000, 10, 1, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isEqual(one, two,
				DateStripType.STRIP_HOUR));
		calendar.set(2000, 10, 1, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isEqual(one, two,
				DateStripType.STRIP_DAY));

		calendar.set(2000, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isEqual(one, two,
				DateStripType.STRIP_DAY));
		calendar.set(2000, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isEqual(one, two,
				DateStripType.STRIP_MONTH));

		calendar.set(0, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isEqual(one, two,
				DateStripType.STRIP_MONTH));
		calendar.set(0, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isEqual(one, two,
				DateStripType.STRIP_YEAR));
	}

	/**
	 * Test isLessOrEqual method.
	 */
	public void testIsLessOrEqual() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 10, 10, 10, 10, 10);
		Date one = calendar.getTime();
		Date two = null;

		calendar.set(2000, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLessOrEqual(one, two, null));

		calendar.set(2000, 10, 10, 10, 10, 0);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLessOrEqual(one, two, null));
		calendar.set(2000, 10, 10, 10, 10, 0);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_SECOND));

		calendar.set(2000, 10, 10, 10, 0, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_SECOND));
		calendar.set(2000, 10, 10, 10, 0, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_MINUTE));

		calendar.set(2000, 10, 10, 0, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_MINUTE));
		calendar.set(2000, 10, 10, 0, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_HOUR));

		calendar.set(2000, 10, 1, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_HOUR));
		calendar.set(2000, 10, 1, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_DAY));

		calendar.set(2000, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_DAY));
		calendar.set(2000, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_MONTH));

		calendar.set(0, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_MONTH));
		calendar.set(0, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLessOrEqual(one, two,
				DateStripType.STRIP_YEAR));
	}

	/**
	 * Test isMore method.
	 */
	public void testIsMore() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 10, 10, 10, 10, 10);
		Date one = calendar.getTime();
		Date two = null;

		calendar.set(2000, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMore(one, two, null));

		calendar.set(2000, 10, 10, 10, 10, 0);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMore(one, two, null));
		calendar.set(2000, 10, 10, 10, 10, 0);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMore(one, two,
				DateStripType.STRIP_SECOND));

		calendar.set(2000, 10, 10, 10, 0, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMore(one, two,
				DateStripType.STRIP_SECOND));
		calendar.set(2000, 10, 10, 10, 0, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMore(one, two,
				DateStripType.STRIP_MINUTE));

		calendar.set(2000, 10, 10, 0, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMore(one, two,
				DateStripType.STRIP_MINUTE));
		calendar.set(2000, 10, 10, 0, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMore(one, two,
				DateStripType.STRIP_HOUR));

		calendar.set(2000, 10, 1, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMore(one, two,
				DateStripType.STRIP_HOUR));
		calendar.set(2000, 10, 1, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMore(one, two,
				DateStripType.STRIP_DAY));

		calendar.set(2000, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMore(one, two,
				DateStripType.STRIP_DAY));
		calendar.set(2000, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMore(one, two,
				DateStripType.STRIP_MONTH));

		calendar.set(0, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMore(one, two,
				DateStripType.STRIP_MONTH));
		calendar.set(0, 10, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMore(one, two,
				DateStripType.STRIP_YEAR));
	}

	/**
	 * Test isLess method.
	 */
	public void testIsLess() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 10, 10, 10, 10, 10);
		Date one = calendar.getTime();
		Date two = null;

		calendar.set(2000, 10, 10, 10, 10, 11);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLess(one, two, null));

		calendar.set(2000, 10, 10, 10, 10, 0);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLess(one, two, null));
		calendar.set(2000, 10, 10, 10, 11, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLess(one, two,
				DateStripType.STRIP_SECOND));

		calendar.set(2000, 10, 10, 10, 0, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLess(one, two,
				DateStripType.STRIP_SECOND));
		calendar.set(2000, 10, 10, 11, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLess(one, two,
				DateStripType.STRIP_MINUTE));

		calendar.set(2000, 10, 10, 0, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLess(one, two,
				DateStripType.STRIP_MINUTE));
		calendar.set(2000, 10, 11, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLess(one, two,
				DateStripType.STRIP_HOUR));

		calendar.set(2000, 10, 1, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLess(one, two,
				DateStripType.STRIP_HOUR));
		calendar.set(2000, 11, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLess(one, two,
				DateStripType.STRIP_DAY));

		calendar.set(2000, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isLess(one, two,
				DateStripType.STRIP_DAY));
		calendar.set(2001, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isLess(one, two,
				DateStripType.STRIP_MONTH));
	}

	/**
	 * Test isMoreOrEqual method.
	 */
	public void testIsMoreOrEqual() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 10, 10, 10, 10, 10);
		Date one = calendar.getTime();
		Date two = null;

		calendar.set(2000, 10, 10, 10, 10, 11);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMoreOrEqual(one, two, null));

		calendar.set(2000, 10, 10, 10, 10, 0);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMoreOrEqual(one, two, null));
		calendar.set(2000, 10, 10, 10, 11, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_SECOND));

		calendar.set(2000, 10, 10, 10, 0, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_SECOND));
		calendar.set(2000, 10, 10, 11, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_MINUTE));

		calendar.set(2000, 10, 10, 0, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_MINUTE));
		calendar.set(2000, 10, 11, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_HOUR));

		calendar.set(2000, 10, 1, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_HOUR));
		calendar.set(2000, 11, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_DAY));

		calendar.set(2000, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertTrue(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_DAY));
		calendar.set(2001, 1, 10, 10, 10, 10);
		two = calendar.getTime();
		Assert.assertFalse(DateComparator.isMoreOrEqual(one, two,
				DateStripType.STRIP_MONTH));
	}
}