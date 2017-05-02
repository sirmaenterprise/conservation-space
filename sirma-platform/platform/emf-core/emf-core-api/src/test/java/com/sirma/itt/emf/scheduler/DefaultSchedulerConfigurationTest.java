package com.sirma.itt.emf.scheduler;

import static org.testng.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.date.DateUtils;
import com.sirma.itt.commons.utils.date.DateUtils.DatePart;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerEntryType;

/**
 * The Class DefaultSchedulerConfigurationTest.
 *
 * @author BBonev
 */
@Test
public class DefaultSchedulerConfigurationTest {

	/**
	 * Test next schedule time.
	 */
	public void testNextScheduleTime() {
		DefaultSchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(SchedulerEntryType.TIMED);
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.MINUTE, 1);
		Date scheduleTime = instance.getTime();
		configuration.setScheduleTime(scheduleTime);
		Assert.assertNotNull(configuration.getNextScheduleTime());
		Assert.assertEquals(scheduleTime, configuration.getNextScheduleTime());

		instance.add(Calendar.MINUTE, -2);
		scheduleTime = instance.getTime();
		configuration.setScheduleTime(scheduleTime);
		Assert.assertNull(configuration.getNextScheduleTime());

		configuration.setRetryCount(2).setRetryDelay(30L).setIncrementalDelay(false);

		Assert.assertNull(configuration.getNextScheduleTime());

		instance = Calendar.getInstance();
		configuration.setMaxRetryCount(5);
		Assert.assertNotNull(configuration.getNextScheduleTime());
		instance.add(Calendar.SECOND, 30);
		assertTrue(DateUtils.isSame(DatePart.SECOND, instance.getTime(), configuration.getNextScheduleTime()));

		configuration.setIncrementalDelay(true);
		instance.add(Calendar.SECOND, 30);
		assertTrue(DateUtils.isSame(DatePart.SECOND, instance.getTime(), configuration.getNextScheduleTime()));
	}

	/**
	 * Test cron expression scheduling.
	 */
	public void testCronExpressionScheduling() {
		DefaultSchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(SchedulerEntryType.CRON);
		configuration.setCronExpression("0 0/15 * ? * *");
		Assert.assertNotNull(configuration.getNextScheduleTime());
	}

}
