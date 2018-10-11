package com.sirma.sep.instance.batch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

/**
 * Test for {@link BatchServiceImpl}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
public class BatchServiceImplTest {
	@InjectMocks
	private BatchServiceImpl batchService;
	@Mock
	private TransactionSupport transactionSupport;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private BatchDataService batchRuntimeService;
	@Mock
	private JobRunner jobRunner;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		doAnswer(a -> {
			a.getArgumentAt(0, Executable.class).execute();
			return null;
		}).when(transactionSupport).invokeInNewTx(any(Executable.class));
		when(securityContext.getCurrentTenantId()).thenReturn("test.tenant");
		when(securityContext.getRequestId()).thenReturn("request-id");
		when(jobRunner.startJob(anyString(), any())).thenReturn(1L);
	}

	@Test
	public void should_StopJobExecutions() {
		String jobName = "job-name";
		batchService.stopJobExecutions(jobName);
		Mockito.verify(jobRunner).stopJobExecutions(jobName);
	}

	@Test
	public void should_ReturnTrue_When_ThereIsExecutionInStatus() {
		String jobName = "jobWithExecutionInStatus";
		BatchStatus executionStatus = BatchStatus.STARTING;
		setupJobRunner(jobName, 2L, executionStatus);
		Assert.assertTrue(batchService.hasJobInStatus(jobName, Collections.singletonList(executionStatus)));
	}

	@Test
	public void should_ReturnFalse_When_StatusIsDifferentThanRequested() {
		String jobName = "jobWithExecutionStatusDifferentThanRequested";
		BatchStatus executionStatus = BatchStatus.STARTING;
		setupJobRunner(jobName, 2L, executionStatus);
		Assert.assertFalse(batchService.hasJobInStatus(jobName, Collections.singletonList(BatchStatus.STARTED)));
	}

	@Test
	public void should_ReturnFalse_When_ThereIsNotExecutionsForJob() {
		String jobName = "jobNameWithoutExecution";
		Mockito.when(jobRunner.getRunningExecutions(jobName)).thenReturn(Collections.emptyList());
		Assert.assertFalse(batchService.hasJobInStatus(jobName, Collections.emptyList()));
	}

	@Test
	public void executeGenericBatch() throws Exception {
		BatchRequest request = new BatchRequest();
		request.setBatchName("jobName");

		batchService.execute(request);

		ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
		verify(jobRunner).startJob(eq("jobName"), propertiesCaptor.capture());
		Properties properties = propertiesCaptor.getValue();
		assertEquals("test.tenant", properties.getProperty(BatchProperties.TENANT_ID));
		assertEquals("request-id", properties.getProperty(BatchProperties.REQUEST_ID));
	}

	@Test
	public void executeGenericBatch_shouldExecuteStreamingBatch_IfPassed() throws Exception {
		BatchRequest request = new StreamBatchRequest(() -> {
			List<Serializable> data = new ArrayList<>();
			for (int i = 0; i < 1024; i++) {
				data.add(String.valueOf(i));
			}
			return data.stream();
		});
		request.setBatchName("jobName");

		batchService.execute(request);

		ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
		verify(jobRunner).startJob(eq("jobName"), propertiesCaptor.capture());
		Properties properties = propertiesCaptor.getValue();
		assertEquals("test.tenant", properties.getProperty(BatchProperties.TENANT_ID));
		assertEquals("request-id", properties.getProperty(BatchProperties.REQUEST_ID));
		verify(batchRuntimeService, times(1024)).addData(eq("jobName"), anyString(), anyString());
		verify(transactionSupport, times(1)).invokeInNewTx(any(Executable.class));
	}

	@Test
	public void executeStreamingBatch() throws Exception {
		StreamBatchRequest request = new StreamBatchRequest(() -> {
			List<Serializable> data = new ArrayList<>();
			for (int i = 0; i < 1500; i++) {
				data.add(String.valueOf(i));
			}
			return data.stream();
		});
		request.setBatchName("jobName");

		batchService.execute(request);

		ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
		verify(jobRunner).startJob(eq("jobName"), propertiesCaptor.capture());
		Properties properties = propertiesCaptor.getValue();
		assertEquals("test.tenant", properties.getProperty(BatchProperties.TENANT_ID));
		assertEquals("request-id", properties.getProperty(BatchProperties.REQUEST_ID));
		verify(batchRuntimeService, times(1500)).addData(eq("jobName"), anyString(), anyString());
		verify(transactionSupport, times(2)).invokeInNewTx(any(Executable.class));
	}

	@Test(expected = RuntimeException.class)
	public void executeStreamingJob_cleanDataIfFails() throws Exception {
		StreamBatchRequest request = new StreamBatchRequest(() -> Stream.of("1"));
		request.setBatchName("jobName");
		doThrow(RuntimeException.class).when(batchRuntimeService).addData(anyString(), anyString(), anyString());

		try {
			batchService.execute(request);
		} finally {
			verify(batchRuntimeService).clearJobData(anyString());
		}
	}

	private void setupJobRunner(String jobName, Long executionId, BatchStatus statusOfExecution) {
		JobExecution jobExecution = Mockito.mock(JobExecution.class);
		Mockito.when(jobExecution.getBatchStatus()).thenReturn(statusOfExecution);
		Mockito.when(jobRunner.getRunningExecutions(jobName)).thenReturn(Arrays.asList(executionId));
		Mockito.when(jobRunner.getJobExecution(executionId)).thenReturn(Optional.of(jobExecution));
	}
}
