package com.sirma.sep.instance.batch;

import java.lang.invoke.MethodHandles;

import javax.batch.api.listener.JobListener;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Job listener that initialize security context using the initial job properties. The implementation depends on the
 * presence of the properties {@link BatchProperties#TENANT_ID} and {@link BatchProperties#REQUEST_ID} when the batch
 * job was started.<br>
 * If tenant identifier is not present the
 * {@link com.sirma.itt.seip.security.context.SecurityContext#SYSTEM_TENANT SecurityContext.SYSTEM_TENANT}
 * will be used
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
@Named
public class SecurityJobListener implements JobListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private JobContext context;
	@Inject
	private BatchProperties batchProperties;

	private TimeTracker tracker;

	@Override
	public void beforeJob() throws Exception {
		String tenantId = batchProperties.getTenantId(context.getExecutionId());
		String requestId = batchProperties.getRequestId(context.getExecutionId());
		securityContextManager.initializeTenantContext(tenantId, requestId);

		tracker = TimeTracker.createAndStart();
	}

	@Override
	public void afterJob() throws Exception {
		LOGGER.info("Batch job {} with id {} took {} s", context.getJobName(),
				batchProperties.getJobId(context.getExecutionId()), tracker.stopInSeconds());
		securityContextManager.endExecution();
	}
}
