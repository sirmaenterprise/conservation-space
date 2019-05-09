package com.sirma.sep.instance.batch.rest;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.JobExecution;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.sep.instance.batch.BatchService;
import com.sirma.sep.instance.batch.JobInfo;

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

	@Inject
	private BatchService batchService;

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
	 * Lists all registered jobs.
	 *
	 * @param filter optional wildcard filter by job id, name and alias
	 * @param activeOnly a boolean property that if has value of true the endpoint will return only the non completed jobs
	 * @return the list of {@link JobInfo}s for all jobs at the moment of calling the rest endpoint
	 */
	@GET
	@Path("/all")
	public Collection<JobInfo> getAllJobs(@QueryParam("filter") String filter,
			@DefaultValue("false") @QueryParam("activeOnly") Boolean activeOnly) {
		Collection<JobInfo> jobs = batchService.getJobs();
		Predicate<JobInfo> jobFilters = completedJobs(activeOnly);
		if (StringUtils.isNotBlank(filter)) {
			jobFilters = jobFilters.and(filterJobs(filter));
		}
		return jobs.stream().filter(jobFilters).collect(Collectors.toList());
	}

	private Predicate<JobInfo> completedJobs(Boolean includeCompleted) {
		if (Boolean.FALSE.equals(includeCompleted)) {
			return job -> true;
		}
		return job -> job.getRemaining() > 0;
	}

	private Predicate<? super JobInfo> filterJobs(String filter) {
		String filterPattern = filter.replace("*", ".*");
		Pattern pattern = Pattern.compile(filterPattern);
		Predicate<JobInfo> filterNonFilterableItems = job -> job.getId() != null && job.getName() != null
				&& job.getAlias() != null;
		Predicate<JobInfo> filteredItems = job -> pattern.matcher(job.getId()).matches()
				|| pattern.matcher(job.getName()).matches()
				|| pattern.matcher(job.getAlias()).matches();
		return filterNonFilterableItems.and(filteredItems);
	}

	/**
	 * Tries to restart or resume failed batch job.
	 *
	 * @param jobId the job id to resume
	 * @return the new job execution
	 */
	@POST
	@Transactional
	@Path("restart/{jobId}")
	public JobExecution resumeJob(@PathParam("jobId") String jobId) {
		return batchService.resumeJob(jobId).map(JobExecutionStatus::new)
				.orElseThrow(() -> new ResourceNotFoundException("Batch job with id " + jobId + " was not found"));
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
