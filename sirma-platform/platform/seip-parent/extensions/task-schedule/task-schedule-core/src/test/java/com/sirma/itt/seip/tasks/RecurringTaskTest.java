package com.sirma.itt.seip.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link RecurringTask}
 *
 * @author BBonev
 */
public class RecurringTaskTest {
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private SchedulerTaskCallback callback;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContextManager.createTransferableContext()).thenReturn(mock(SecurityContext.class));
	}
	@Test
	public void successfullExecution() throws Exception {
		SchedulerEntry entry = buildEntry();
		when(callback.onTimeout(any())).then(a -> {
			a.getArgumentAt(0, SchedulerTask.class).cancel();
			return Boolean.TRUE;
		});
		RecurringTask task = new RecurringTask(entry, callback, securityContextManager);
		task.run();

		verify(callback).onExecuteCanceled(task);
	}

	@Test
	public void tooLongToWait() throws Exception {
		SchedulerEntry entry = buildEntry();
		SchedulerConfiguration configuration = mock(SchedulerConfiguration.class);
		when(configuration.getType()).thenReturn(SchedulerEntryType.CRON);
		when(configuration.getNextScheduleTime()).thenReturn(new Date(), new Date(System.currentTimeMillis() + 1000));
		entry.setConfiguration(configuration);

		when(callback.onTimeout(any())).then(a -> {
			a.getArgumentAt(0, SchedulerTask.class).cancel();
			return Boolean.TRUE;
		});

		RecurringTask task = new RecurringTask(entry, callback, 10L, TimeUnit.MILLISECONDS, securityContextManager);
		task.run();

		verify(callback).onExecuteSuccess(
				argThat(CustomMatcher.of((SchedulerTask t) -> t.getEntry().getStatus() == SchedulerEntryStatus.PENDING,
						"The entry status should be set as PENDING")));
	}

	private static SchedulerEntry buildEntry() {
		SchedulerEntry entry = new SchedulerEntry();

		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setType(SchedulerEntryType.CRON).setScheduleTime(new Date());

		entry.setAction(mock(SchedulerAction.class));

		entry.setId(1L);
		entry.setConfiguration(configuration);
		return entry;
	}
}
