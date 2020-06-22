package com.sirma.sep.instance.batch;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import javax.batch.runtime.JobExecution;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;

/**
 * Start up service that resumes not completed batch jobs.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 29/01/2019
 */
public class BatchJobRestarter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private BatchService batchService;

	/**
	 * Resume not completed jobs
	 */
	@Startup(async = true)
	@RunAsAllTenantAdmins(parallel = false)
	void startBatchJobsOnServerStart() {
		Collection<JobInfo> allJobs = batchService.getJobs();
		allJobs.stream().filter(validJobs()).filter(notCompletedJobs()).forEach(this::resumeJob);
	}

	private static Predicate<JobInfo> validJobs() {
		// the old jobs does not have any of the properties needed to restart them
		// so we process only these that have the needed information to be restarted
		return info -> info.getExecutionId() > 0;
	}

	private void resumeJob(JobInfo jobInfo) {
		String jobId = jobInfo.getId();
		LOGGER.info("Going to resume batch job {} for the remaining {}/{} items", jobId, jobInfo.getRemaining(),
				jobInfo.getRemaining() + jobInfo.getProcessed());
		Optional<JobExecution> jobExecution = batchService.resumeJob(jobId);
		jobExecution.ifPresent(job -> LOGGER.info("Successfully resumed batch job {}", jobId));
	}

	private static Predicate<JobInfo> notCompletedJobs() {
		return info -> info.getRemaining() > 0;
	}
}
