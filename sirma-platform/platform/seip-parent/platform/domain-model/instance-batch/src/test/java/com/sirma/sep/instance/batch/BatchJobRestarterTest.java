package com.sirma.sep.instance.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.batch.runtime.JobExecution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link BatchJobRestarter}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 29/01/2019
 */
public class BatchJobRestarterTest {

	@InjectMocks
	private BatchJobRestarter batchJobRestart;

	@Mock
	private BatchService batchService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		JobExecution jobExecution = mock(JobExecution.class);
		when(batchService.resumeJob(anyString())).thenReturn(Optional.of(jobExecution));
	}

	@Test
	public void startBatchJobsOnServerStart() throws Exception {
		when(batchService.getJobs()).thenReturn(Arrays.asList(createJob("notStarted", 12, 0, 100),
				createJob("stopped", 14, 15, 100),
				createJob("old_invalid", 0, 12, 100),
				createJob("completed", 15, 0, 0)));
		batchJobRestart.startBatchJobsOnServerStart();
		ArgumentCaptor<String> ids = ArgumentCaptor.forClass(String.class);
		verify(batchService, times(2)).resumeJob(ids.capture());
		List<String> values = ids.getAllValues();
		assertEquals(2, values.size());
		assertTrue(values.containsAll(Arrays.asList("notStarted", "stopped")));
	}

	private static JobInfo createJob(String jobId, long executionId, int processed, int remaining) {
		JobInfo info = new JobInfo(jobId);
		info.setExecutionId(executionId);
		info.setProcessed(processed);
		info.setRemaining(remaining);
		return info;
	}
}