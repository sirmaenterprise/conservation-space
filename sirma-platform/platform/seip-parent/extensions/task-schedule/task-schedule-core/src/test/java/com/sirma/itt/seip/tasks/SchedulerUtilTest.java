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
		SchedulerUtil.executeSilently(() -> {
			return;
		});
		SchedulerUtil.supplySilently(() -> {
			throw new RuntimeException();
		});
	}

	@Test
	public void should_invokeSilently_withSupplier() throws Exception {
		SchedulerUtil.executeSilently(() -> {
			return;
		});
		SchedulerUtil.supplySilently(() -> {
			throw new RuntimeException();
		});
	}
}
