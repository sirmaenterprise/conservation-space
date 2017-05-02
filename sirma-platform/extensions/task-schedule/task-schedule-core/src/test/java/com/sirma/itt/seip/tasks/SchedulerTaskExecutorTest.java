package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.CancellationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Test for {@link SchedulerTaskExecutor}
 *
 * @author BBonev
 */
public class SchedulerTaskExecutorTest {

	@InjectMocks
	private SchedulerTaskExecutor executor;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private SchedulerTaskCallback callback;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContextManager.createTransferableContext()).then(a -> mock(SecurityContext.class));
		executor.setMaxConcurrentTasks(3);
	}

	@Test
	public void testTimedTask() throws Exception {
		SchedulerEntry entry = new SchedulerEntry();
		DefaultSchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(SchedulerEntryType.TIMED).setScheduleTime(new Date()).setIdentifier("test");

		entry.setConfiguration(configuration);
		entry.setAction(mock(SchedulerAction.class));

		when(callback.onTimeout(any())).thenReturn(Boolean.TRUE);

		SchedulerTask task = executor.submit(entry, 0, callback);
		assertNotNull(task);
		// wait for the task to complete
		task.getFuture().get();

		verify(callback).onTimeout(task);
		verify(callback).onExecuteSuccess(task);
	}

	@Test(expected = CancellationException.class)
	public void testCronTask() throws Exception {
		SchedulerEntry entry = new SchedulerEntry();
		DefaultSchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(SchedulerEntryType.CRON).setScheduleTime(new Date()).setIdentifier("test");

		entry.setConfiguration(configuration);
		entry.setAction(mock(SchedulerAction.class));

		when(callback.onTimeout(any())).then(a -> {
			a.getArgumentAt(0, SchedulerTask.class).cancel();
			return Boolean.TRUE;
		});

		SchedulerTask task = executor.submit(entry, 0, callback);
		assertNotNull(task);
		// the task is cancelled when the onTimeout is called
		task.getFuture().get();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotSupportedTask() throws Exception {
		SchedulerEntry entry = new SchedulerEntry();
		DefaultSchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(SchedulerEntryType.EVENT).setIdentifier("test");

		entry.setConfiguration(configuration);
		entry.setAction(mock(SchedulerAction.class));

		executor.submit(entry, 0, callback);
	}

	@After
	public void afterMethod() {
		executor.shutdown();
	}
}
