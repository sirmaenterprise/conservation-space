package com.sirma.sep.instance.batch.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.JobExecution;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST resource to provide access to the running batch {@link JobExecution}s
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/07/2017
 */
@ApplicationScoped
@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
public class JobExecutionResource {

	/**
	 * List the current active job execution context for all registered jobs.
	 *
	 * @return the list of {@link JobExecution}s for all active jobs at the moment of calling the rest endpoint
	 */
	@GET
	public List<JobExecution> getCurrentJobs() {
		JobOperator jobOperator = BatchRuntime.getJobOperator();
		return jobOperator
				.getJobNames().stream()
				.flatMap(jobName -> jobOperator.getRunningExecutions(jobName).stream())
				.map(jobOperator::getJobExecution)
				.map(JobExecutionStatus::new)
				.collect(Collectors.toList());
	}

	/**
	 * Get a status for a running or completed job
	 *
	 * @param jobExecutionId the requested job execution id
	 * @return the job execution instance
	 */
	@GET
	@Path("{executionId}")
	public JobExecution getStatus(@PathParam("executionId") long jobExecutionId) {
		return new JobExecutionStatus(BatchRuntime.getJobOperator().getJobExecution(jobExecutionId));
	}
}
