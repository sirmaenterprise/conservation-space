package com.sirma.itt.pm.schedule.util;

import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The Class DateUtilTest.
 * 
 * @author BBonev
 */
public class DateUtilTest {

	/**
	 * Test duration calculator.
	 */
	@Test
	public void testDurationCalculator() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2013, 5, 1);
		Date start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 6, 1);
		Date end = calendar.getTime();
		int duration = DateUtil.calculateDuration(start, end, false);
		Assert.assertEquals(30, duration);

		calendar.clear();
		calendar.set(2013, 5, 1);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 5, 2);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, true);
		Assert.assertEquals(1, duration);

		calendar.clear();
		calendar.set(2013, 6, 26);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 6, 28);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, true);
		Assert.assertEquals(1, duration);

		calendar.clear();
		calendar.set(2013, 6, 26);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 6, 27);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, true);
		Assert.assertEquals(1, duration);

		calendar.clear();
		calendar.set(2013, 5, 1);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 6, 1);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, true);
		Assert.assertEquals(21, duration);

		calendar.clear();
		calendar.set(2013, 5, 1);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 5, 30);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, true);
		Assert.assertEquals(20, duration);

		calendar.clear();
		calendar.set(2013, 5, 1);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 5, 29);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, true);
		Assert.assertEquals(20, duration);

		calendar.clear();
		calendar.set(2012, 5, 1);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 6, 1);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, false);
		Assert.assertEquals(394, duration);

		calendar.clear();
		calendar.set(2013, 5, 5);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 5, 24);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, true);
		Assert.assertEquals(14, duration);

		calendar.clear();
		calendar.set(2012, 5, 1);
		start = calendar.getTime();
		calendar.clear();
		calendar.set(2013, 6, 1);
		end = calendar.getTime();
		duration = DateUtil.calculateDuration(start, end, true);
		Assert.assertEquals(282, duration);

	}

}
