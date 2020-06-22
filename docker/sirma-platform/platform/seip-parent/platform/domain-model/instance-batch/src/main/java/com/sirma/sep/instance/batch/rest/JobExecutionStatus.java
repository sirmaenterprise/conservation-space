package com.sirma.sep.instance.batch.rest;

import java.util.Date;
import java.util.Properties;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

/**
 * Proxy object for {@link JobExecution} instances used for writing to REST endpoints. The default implementation is
 * not suitable as produces stack overflow used as is.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/07/2017
 */
public class JobExecutionStatus implements JobExecution {

	private final JobExecution execution;

	/**
	 * Instantiate new instance using the given delegate
	 *
	 * @param execution the execution to wrap
	 */
	public JobExecutionStatus(JobExecution execution) {
		this.execution = execution;
	}

	@Override
	public long getExecutionId() {
		return execution.getExecutionId();
	}

	@Override
	public String getJobName() {
		return execution.getJobName();
	}

	@Override
	public BatchStatus getBatchStatus() {
		return execution.getBatchStatus();
	}

	@Override
	public Date getStartTime() {
		return execution.getStartTime();
	}

	@Override
	public Date getEndTime() {
		return execution.getEndTime();
	}

	@Override
	public String getExitStatus() {
		return execution.getExitStatus();
	}

	@Override
	public Date getCreateTime() {
		return execution.getCreateTime();
	}

	@Override
	public Date getLastUpdatedTime() {
		return execution.getLastUpdatedTime();
	}

	@Override
	public Properties getJobParameters() {
		return execution.getJobParameters();
	}
}
