package com.sirma.sep.instance.batch;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Integration service for processing instance data via Batch API. The service provides means to store selected
 * instance identifiers that need to be processed by a batch job. The given identifiers are stored in persistent
 * storage and could be retrieved back via {@link BatchDataService#getBatchData(long, int, int)}. For each new job an
 * unique identifier
 * is generated that can be used to fetch, update as processed or clear data. The identifier could be retrieved via
 * <pre><code>
 * &#64;Inject
 * private JobContext context;
 * ...
 * JobOperator jobOperator = BatchRuntime.getJobOperator();
 * JobExecution jobExecution = jobOperator.getJobExecution(context.getExecutionId());
 * Properties properties = jobExecution.getJobParameters();
 * String jobInstanceId = properties.getProperty(BatchProperties.JOB_ID);</code>
 * </pre>
 * Or using one of the methods {@link BatchProperties#getJobProperty(long, String)} or
 * {@link BatchProperties#getJobId(long)}
 * <p>The service will pass the current security context to the executed jobs, but in order to be applied for all
 * steps in the batch job a listener should be added in the job definition like one shown in the listing bellow<pre>
 * &lt;job id="myJobName" xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="1.0"&gt;
 *   &lt;listeners&gt;
 *       &lt;listener ref="securityJobListener"/&gt;
 *   &lt;/listeners&gt;
 *   &lt;step id="myStepName"&gt;
 *   .....
 * </pre></p>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/06/2017
 */
public interface BatchService {
	/**
	 * Execute generic batch job that does not store any data. It will initialize the security context and start the
	 * job. If the passed request instance is one of the supported requests it will be processed as if any of the
	 * other methods are called.
	 * @param request the request to process and start
	 * @return the batch runtime execution id returned from the {@code BatchRuntime.getJobOperator().start(jobName,
	 * properties)}
	 */
	long execute(BatchRequest request);

	/**
	 * Reads the data from the given instance id stream, persists all provide identifiers and starts the
	 * specified batch job.
	 * <br>The method is optimized to handle huge amounts of data in multiple transactions. In case of error all
	 * written data will be removed
	 * @param request to process and start
	 * @return the batch runtime execution id returned from the {@code BatchRuntime.getJobOperator().start(jobName,
	 * properties)}
	 */
	long execute(StreamBatchRequest request);

	/**
	 * Returns execution ids for job instances with the specified
	 * name that have running executions.
	 *
	 * @param jobName
	 *            specifies the job name.
	 * @return a list of execution ids.
	 */
	List<Long> getRunningExecutions(String jobName);

	/**
	 * Return job execution for specified execution id
	 *
	 * @param executionId
	 *            specifies the job execution.
	 * @return job execution
	 */
	Optional<JobExecution> getJobExecution(Long executionId);

	/**
	 * Checks there is a execution for job with name <code>jobName</code> and status listed in <code>statuses</code>.
	 *
	 * @param jobName
	 *         - job name.
	 * @param statuses
	 *         - list with statuses.
	 * @return true if there is at least one execution in status listed in <code>statuses</code>.
	 */
	boolean hasJobInStatus(String jobName, List<BatchStatus> statuses);

	/**
	 * Fetches all executions for job with name <code>jobName</code> and stops them.
	 * BatchService cannot guarantee that all executions stops See {@link javax.batch.operations.JobOperator#stop(long)}.
	 *
	 * @param jobName
	 *         - job name which executions have to be stopped.
	 */
	void stopJobExecutions(String jobName);

	/**
	 * Return information about all known jobs in the database.
	 *
	 * @return info for all jobs.
	 */
	Collection<JobInfo> getJobs();

	/**
	 * Resume a batch job that was stopped or starts new job if the previous job is no longer available.
	 *
	 * @param jobId the job id to restart
	 * @return the job execution of the new job
	 */
	Optional<JobExecution> resumeJob(String jobId);
}
