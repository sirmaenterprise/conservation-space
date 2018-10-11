package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * The Class TaskExecutorTest.
 *
 * @author BBonev
 */
public class TaskExecutorTest {

	@Mock
	private SecurityContextManager securityContextManager;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContextManager.createTransferableContext()).thenReturn(mock(SecurityContext.class));
	}

	/**
	 * Test executor call.
	 *
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("boxing")
	public void testExecutorCall() throws Exception {

		long fixedDelay = -1;
		SchedulerEntry entry = buildEntry();

		SchedulerTaskCallback callback = mock(SchedulerTaskCallback.class);
		when(callback.onTimeout(any())).thenReturn(Boolean.TRUE, Boolean.FALSE);


		TaskExecutor executor = new TaskExecutor(entry, callback, fixedDelay, securityContextManager);

		executor.run();
		assertEquals(SchedulerEntryStatus.COMPLETED, entry.getStatus());
		verify(callback).onExecuteSuccess(executor);

		executor = new TaskExecutor(entry, callback, 2, securityContextManager);

		executor.run();
		assertEquals(SchedulerEntryStatus.RUN_WITH_ERROR, entry.getStatus());
		verify(callback).onExecuteFail(executor);
	}

	private static SchedulerEntry buildEntry() {
		SchedulerEntry entry = new SchedulerEntry();

		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration
				.setType(SchedulerEntryType.EVENT)
					.setScheduleTime(new Date())
					.setMaxRetryCount(5)
					.setIncrementalDelay(true)
					.setRetryDelay(2L);

		entry.setAction(mock(SchedulerAction.class));

		entry.setId(1L);
		entry.setConfiguration(configuration);
		return entry;
	}

	@Test
	@SuppressWarnings("boxing")
	public void test_canceledTask() throws Exception {

		long fixedDelay = -1;
		SchedulerEntry entry = buildEntry();

		SchedulerTaskCallback callback = mock(SchedulerTaskCallback.class);
		when(callback.onTimeout(any())).then(a -> {
			a.getArgumentAt(0, SchedulerTask.class).cancel();
			return true;
		});

		TaskExecutor executor = new TaskExecutor(entry, callback, fixedDelay, securityContextManager);

		executor.run();
		verify(callback).onExecuteCanceled(executor);
	}
}
