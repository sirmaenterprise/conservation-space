package com.sirma.sep.instance.batch;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.batch.operations.JobExecutionAlreadyCompleteException;
import javax.batch.operations.JobExecutionIsRunningException;
import javax.batch.operations.JobExecutionNotMostRecentException;
import javax.batch.operations.JobExecutionNotRunningException;
import javax.batch.operations.JobOperator;
import javax.batch.operations.JobRestartException;
import javax.batch.operations.JobSecurityException;
import javax.batch.operations.JobStartException;
import javax.batch.operations.NoSuchJobException;
import javax.batch.operations.NoSuchJobExecutionException;
import javax.batch.operations.NoSuchJobInstanceException;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.JobInstance;
import javax.batch.runtime.StepExecution;

/**
 * Mock instance for {@link JobOperator} for test purposes
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/07/2017
 */
public class JobOperatorMock implements JobOperator {

	private static ThreadLocal<JobExecution> jobExecutionThreadLocal = new ThreadLocal<>();
	@Override
	public Set<String> getJobNames() throws JobSecurityException {
		return null;
	}

	@Override
	public int getJobInstanceCount(String jobName) throws NoSuchJobException, JobSecurityException {
		return 0;
	}

	@Override
	public List<JobInstance> getJobInstances(String jobName, int start, int count)
			throws NoSuchJobException, JobSecurityException {
		return null;
	}

	@Override
	public List<Long> getRunningExecutions(String jobName) throws NoSuchJobException, JobSecurityException {
		return null;
	}

	@Override
	public Properties getParameters(long executionId) throws NoSuchJobExecutionException, JobSecurityException {
		return null;
	}

	@Override
	public long start(String jobXMLName, Properties jobParameters) throws JobStartException, JobSecurityException {
		return 0;
	}

	@Override
	public long restart(long executionId, Properties restartParameters)
			throws JobExecutionAlreadyCompleteException, NoSuchJobExecutionException,
				   JobExecutionNotMostRecentException, JobRestartException, JobSecurityException {
		return 0;
	}

	@Override
	public void stop(long executionId)
			throws NoSuchJobExecutionException, JobExecutionNotRunningException, JobSecurityException {

	}

	@Override
	public void abandon(long executionId)
			throws NoSuchJobExecutionException, JobExecutionIsRunningException, JobSecurityException {

	}

	@Override
	public JobInstance getJobInstance(long executionId) throws NoSuchJobExecutionException, JobSecurityException {
		return null;
	}

	@Override
	public List<JobExecution> getJobExecutions(JobInstance instance)
			throws NoSuchJobInstanceException, JobSecurityException {
		return null;
	}

	@Override
	public JobExecution getJobExecution(long executionId) throws NoSuchJobExecutionException, JobSecurityException {
		return jobExecutionThreadLocal.get();
	}

	/**
	 * Set thread local job execution
	 *
	 * @param jobExecution the execution to set
	 */
	public static void setJobExecution(JobExecution jobExecution) {
		JobOperatorMock.jobExecutionThreadLocal.set(jobExecution);
	}

	@Override
	public List<StepExecution> getStepExecutions(long jobExecutionId)
			throws NoSuchJobExecutionException, JobSecurityException {
		return null;
	}
}
