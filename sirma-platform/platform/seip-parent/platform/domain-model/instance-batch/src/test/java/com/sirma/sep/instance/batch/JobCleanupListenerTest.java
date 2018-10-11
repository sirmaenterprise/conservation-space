package com.sirma.sep.instance.batch;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link JobCleanupListener}
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
public class JobCleanupListenerTest {
	@InjectMocks
	private JobCleanupListener cleanupListener;

	@Mock
	private BatchDataService runtimeService;
	@Mock
	private JobContext jobContext;
	@Mock
	private BatchProperties batchProperties;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(jobContext.getExecutionId()).thenReturn(1L);
		when(batchProperties.getJobId(1L)).thenReturn("jobId");
	}

	@Test
	public void afterJob_doNothing_ifNotInCorrectState() throws Exception {
		when(jobContext.getBatchStatus()).thenReturn(BatchStatus.FAILED);

		cleanupListener.afterJob();

		verify(runtimeService, never()).clearJobData(anyLong());
	}

	@Test
	public void afterJob_clearData_ifCompleted() throws Exception {
		when(jobContext.getBatchStatus()).thenReturn(BatchStatus.STARTED);

		cleanupListener.afterJob();

		verify(runtimeService).clearJobData(1L);
	}

}
