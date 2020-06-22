package com.sirma.itt.emf.scheduler;


import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerEntryType;

public class DefaultSchedulerConfigurationTest {

	@Test
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

		DateUtils.isSameInstant(instance.getTime(), configuration.getNextScheduleTime());

		configuration.setIncrementalDelay(true);
		instance.add(Calendar.SECOND, 30);
		DateUtils.isSameInstant(instance.getTime(), configuration.getNextScheduleTime());
	}

	@Test
	public void testCronExpressionScheduling() {
		DefaultSchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(SchedulerEntryType.CRON);
		configuration.setCronExpression("0 0/15 * ? * *");
		Assert.assertNotNull(configuration.getNextScheduleTime());
	}

	@Test
	public void should_schedule_next_with_cron_expression_with_timezone() {
		DefaultSchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setTimeZoneID("GMT");
		configuration.setType(SchedulerEntryType.CRON);
		configuration.setCronExpression("0 1 5 1/1 * ? *");
		Assert.assertNotNull(configuration.getNextScheduleTime());
	}

}
