package com.sirma.itt.seip.tasks;

import org.junit.Test;

/**
 * Tests for {@link SchedulerUtil}
 *
 * @author BBonev
 */
public class SchedulerUtilTest {

	@Test
	public void invokeSilently() throws Exception {
		SchedulerUtil.invokeSilently(() -> {
			return;
		});
		SchedulerUtil.invokeSilently(() -> {
			throw new RuntimeException();
		});
	}
}
