package com.sirma.itt.seip.tasks;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link SchedulerContext}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 19/09/2017
 */
public class SchedulerContextTest {

	private SchedulerContext ctx = new SchedulerContext();

	@Test
	public void test_getConfiguration() throws Exception {
		SchedulerEntry entry = new SchedulerEntry();
		entry.setConfiguration(
				new DefaultSchedulerConfiguration().setMaxRetryCount(10).setType(SchedulerEntryType.IMMEDIATE));
		ctx.put(SchedulerContext.SCHEDULER_ENTRY, entry);

		SchedulerConfiguration configuration = ctx.getConfiguration();
		assertEquals(10, configuration.getMaxRetryCount());
		assertEquals(SchedulerEntryType.IMMEDIATE, configuration.getType());
	}

	@Test(expected = IllegalStateException.class)
	public void test_getConfiguration_missingConfiguration() throws Exception {
		ctx.getConfiguration();
	}
}