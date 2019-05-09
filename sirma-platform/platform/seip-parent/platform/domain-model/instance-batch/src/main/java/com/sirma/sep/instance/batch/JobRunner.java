package com.sirma.sep.instance.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.batch.operations.JobOperator;
import javax.batch.operations.NoSuchJobException;
import javax.batch.operations.NoSuchJobExecutionException;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.JobExecution;

/**
 * Wrapper class for the {@link BatchRuntime}. Used to bridge the local api with the static batch api.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
class JobRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Start a batch job with the given name and runtime properties
	 *
	 * @param jobName the job name
	 * @param properties the runtime properties to pass to the job while executing
	 * @return the job execution id
	 */
	long startJob(String jobName, Properties properties) {
		return getJobOperator().start(jobName, properties);
	}

	/**
	 * Restarts a batch job with the given execution and runtime properties
	 *
	 * @param jobExecutionId the job execution id
	 * @param properties the runtime properties to pass to the job while executing
	 * @return the job execution id
	 */
	long restartJob(long jobExecutionId, Properties properties) {
		return getJobOperator().restart(jobExecutionId, properties);
	}

	/**
	 * Returns execution ids for job instances with the specified
	 * name that have running executions.
	 *
	 * @param jobName specifies the job name.
	 * @return a list of execution ids.
	 */
	List<Long> getRunningExecutions(String jobName) {
		JobOperator jobOperator = getJobOperator();
		try {
			return jobOperator.getRunningExecutions(jobName);
		} catch (NoSuchJobException e) {
			LOGGER.info("There is no running execution with name: {}", jobName);
		}
		return Collections.emptyList();
	}

	/**
	 * Return job execution for specified execution id
	 *
	 * @param executionId specifies the job execution.
	 * @return job execution
	 */
	Optional<JobExecution> getJobExecution(Long executionId) {
		JobOperator jobOperator = getJobOperator();
		try {
			return Optional.of(jobOperator.getJobExecution(executionId));
		} catch (NoSuchJobExecutionException e) {
			LOGGER.info("There is no execution with id: {}", executionId);
		}
		return Optional.empty();
	}

	/**
	 * Fetches all executions for job with name <code>jobName</code> and stops them.
	 *
	 * @param jobName - job name which executions have to be stopped.
	 */
	void stopJobExecutions(String jobName) {
		JobOperator jobOperator = getJobOperator();
		getRunningExecutions(jobName).forEach(executionId -> {
			try {
				jobOperator.stop(executionId);
			} catch (NoSuchJobExecutionException e) {
				LOGGER.info("There is no execution with id: {}", executionId);
			}
		});
	}

	/**
	 * The getJobOperator factory method returns
	 * an instance of the JobOperator interface.
	 *
	 * @return JobOperator instance.
	 */
	JobOperator getJobOperator() {
		return BatchRuntime.getJobOperator();
	}
}
