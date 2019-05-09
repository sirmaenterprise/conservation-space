package com.sirma.sep.instance.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.AdditionalPackages;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

import javax.batch.operations.NoSuchJobExecutionException;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Test for {@link BatchServiceImpl}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ DatabaseBatchDataService.class, DbDaoFake.class, BatchPropertiesFake.class })
@AdditionalPackages({ TypeConverterImpl.class })
@AdditionalClasspaths({})
@ActivatedAlternatives({ BatchPropertiesFake.class })
public class BatchServiceImplTest {
	@Inject
	private BatchServiceImpl batchService;
	@Spy
	@Produces
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	@Produces
	private SecurityContext securityContext;
	@Mock
	@Produces
	private JobRunner jobRunner;

	@Inject
	private DbDaoFake daoFake;

	@Inject
	private BatchProperties batchProperties;

	private AtomicLong jobIndex = new AtomicLong(0);

	@Before
	public void setUp() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn("test.tenant");
		when(securityContext.getRequestId()).thenReturn("request-id");
		when(jobRunner.startJob(anyString(), any())).thenAnswer(a -> {
			long jobExecutionId = jobIndex.incrementAndGet();
			BatchPropertiesFake.addProperties(jobExecutionId, a.getArgumentAt(1, Properties.class));
			return jobExecutionId;
		});
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
		assertEquals("test.tenant", properties.getProperty(BatchPropertiesFake.TENANT_ID));
		assertEquals("request-id", properties.getProperty(BatchPropertiesFake.REQUEST_ID));
	}

	@Test
	public void executeReturnJobData() throws Exception {
		StreamBatchRequest request = createStreamingRequest(1024);

		batchService.execute(request);

		Collection<JobInfo> jobs = batchService.getJobs();
		assertFalse(jobs.isEmpty());
		JobInfo jobInfo = jobs.iterator().next();
		assertEquals("jobName", jobInfo.getName());
		assertNotNull("The job should have job id", jobInfo.getId());
		assertEquals(1024, jobInfo.getRemaining());
		assertEquals(0, jobInfo.getProcessed());
		assertNotNull(jobInfo.getProperties());
	}

	private StreamBatchRequest createStreamingRequest(int numberOfItems) {
		StreamBatchRequest request = new StreamBatchRequest(
				() -> IntStream.range(0, numberOfItems).boxed().map(String::valueOf));
		request.setBatchName("jobName");
		return request;
	}

	@Test
	public void executeGenericBatch_shouldExecuteStreamingBatch_IfPassed() throws Exception {
		StreamBatchRequest request = createStreamingRequest(1024);

		batchService.execute(request);

		ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
		verify(jobRunner).startJob(eq("jobName"), propertiesCaptor.capture());
		Properties properties = propertiesCaptor.getValue();
		assertEquals("test.tenant", properties.getProperty(BatchPropertiesFake.TENANT_ID));
		assertEquals("request-id", properties.getProperty(BatchPropertiesFake.REQUEST_ID));
		verify(transactionSupport, times(1)).invokeInNewTx(any(Executable.class));
		verifyJobDataCount(request, 1024);
	}

	@Test
	public void executeStreamingBatch() throws Exception {
		StreamBatchRequest request = createStreamingRequest(1500);

		batchService.execute(request);

		ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
		verify(jobRunner).startJob(eq("jobName"), propertiesCaptor.capture());
		Properties properties = propertiesCaptor.getValue();
		assertEquals("test.tenant", properties.getProperty(BatchPropertiesFake.TENANT_ID));
		assertEquals("request-id", properties.getProperty(BatchPropertiesFake.REQUEST_ID));
		verify(transactionSupport, times(2)).invokeInNewTx(any(Executable.class));

		verifyJobDataCount(request, 1500);
	}

	@Test(expected = RuntimeException.class)
	public void executeStreamingJob_cleanDataIfFails() throws Exception {
		StreamBatchRequest request = createStreamingRequest(1);
		doThrow(RuntimeException.class).when(jobRunner).startJob(anyString(), any());

		try {
			batchService.execute(request);
		} finally {
			verifyJobDataCount(request, 0);
		}
	}

	@Test
	public void resumeJob_shouldRestartJob() {
		StreamBatchRequest request = createStreamingRequest(99);
		long execute = batchService.execute(request);
		Optional<String> jobId = batchService.getJobs()
				.stream()
				.filter(job -> job.getExecutionId() == execute)
				.findFirst()
				.map(JobInfo::getId);
		assertTrue("The job should be resolvable by execution id", jobId.isPresent());
		batchService.resumeJob(jobId.get());
		verify(jobRunner).restartJob(eq(execute), any());
	}


	@Test
	public void resumeJob_shouldStartJobIfOldDoesNotExists() {
		StreamBatchRequest request = createStreamingRequest(99);
		long execute = batchService.execute(request);
		Optional<String> jobId = batchService.getJobs()
				.stream()
				.filter(job -> job.getExecutionId() == execute)
				.findFirst()
				.map(JobInfo::getId);
		assertTrue("The job should be resolvable by execution id", jobId.isPresent());

		when(jobRunner.restartJob(eq(execute), any())).thenThrow(NoSuchJobExecutionException.class);

		batchService.resumeJob(jobId.get());

		// one for the initial run and then for the resume
		verify(jobRunner, times(2)).startJob(anyString(), any());

		ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		verify(jobRunner).getJobExecution(captor.capture());

		// get the new job execution id so we can retrieve the job id to check the stored data
		Long jobExecutionId = captor.getValue();
		String job = batchProperties.getJobId(jobExecutionId);
		request.getProperties().put(BatchProperties.JOB_ID, job);

		verifyJobDataCount(request, 99);
	}


	private void verifyJobDataCount(BatchRequest request, int expectedCount) {
		String jobId = request.getProperties().getProperty(BatchPropertiesFake.JOB_ID);
		assertEquals(expectedCount, daoFake.getJobData(jobId).size());
	}

	private void setupJobRunner(String jobName, Long executionId, BatchStatus statusOfExecution) {
		JobExecution jobExecution = Mockito.mock(JobExecution.class);
		Mockito.when(jobExecution.getBatchStatus()).thenReturn(statusOfExecution);
		Mockito.when(jobRunner.getRunningExecutions(jobName)).thenReturn(Collections.singletonList(executionId));
		Mockito.when(jobRunner.getJobExecution(executionId)).thenReturn(Optional.of(jobExecution));
	}
}
