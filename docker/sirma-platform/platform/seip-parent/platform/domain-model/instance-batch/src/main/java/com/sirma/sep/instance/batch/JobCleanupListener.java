package com.sirma.sep.instance.batch;

import java.lang.invoke.MethodHandles;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job listener that automatically clears any data written from the job if the job completes successfully
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
@Named
public class JobCleanupListener implements JobListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private BatchDataService runtimeService;
	@Inject
	private JobContext jobContext;
	@Inject
	private BatchProperties batchProperties;

	@Override
	public void beforeJob() throws Exception {
		// nothing to do
	}

	@Override
	public void afterJob() throws Exception {
		BatchStatus batchStatus = jobContext.getBatchStatus();
		// because we are in the active job we cannot receive completed status, but get STARTED
		// but if there is an error in the processing we will get FAILED
		// all other states are ignored
		if (batchStatus == BatchStatus.STARTED) {
			LOGGER.info("Cleaning job {} data", batchProperties.getJobId(jobContext.getExecutionId()));
			runtimeService.clearJobData(jobContext.getExecutionId());
		}
	}
}
