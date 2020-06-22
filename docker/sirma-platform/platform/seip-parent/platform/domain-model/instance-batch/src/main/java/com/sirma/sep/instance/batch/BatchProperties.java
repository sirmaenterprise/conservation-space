package com.sirma.sep.instance.batch;

import java.util.Optional;

import javax.batch.runtime.BatchRuntime;

import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Defines common batch properties used in jobs started via the {@link BatchService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 14/06/2017
 */
public class BatchProperties {
	/**
	 * Property key that specifies the internal job identifier in the job execution context. This identifier is
	 * generated and set in the batch properties by the {@link BatchService}. It could be read only for started jobs
	 */
	public static final String JOB_ID = "jobId";

	/**
	 * Property that specifies the target tenant where the data is located in. It's used by the
	 * {@link SecurityJobListener} to initialize proper security context when executing the batch job. <br>This is
	 * automatically set by the {@link BatchService}
	 */
	public static final String TENANT_ID = "tenantId";
	/**
	 * Optional property that specifies the request identifier to use when processing the job data. It's used by the
	 * {@link SecurityJobListener} to initialize proper security context when executing the batch job. It can be used
	 * to link the batch processing with the originating request.<br> This is automatically set by the
	 * {@link BatchService}
	 */
	public static final String REQUEST_ID = "requestId";

	/**
	 * Property passed on job scheduling to specify the job chunk size used when processing job data.
	 */
	public static final String CHUNK_SIZE = "chunk_size";
	/**
	 * Property passed on job scheduling to specify the partitions count to use when processing job data.
	 */
	public static final String PARTITIONS_COUNT = "partitions";
	/**
	 * The default chunk size if nothing is specified
	 */
	public static final int DEFAULT_CHUNK_SIZE = 100;

	/**
	 * Helper method for fetching user defined job property.
	 *
	 * @param executionId the job execution id.
	 * @param key the property key.
	 * @return the property value or null if not defined
	 */
	public String getJobProperty(long executionId, String key) {
		return getJobProperty(executionId, key, null);
	}

	/**
	 * Helper method for fetching user defined job property.
	 *
	 * @param executionId the job execution id
	 * @param key the property key.
	 * @param defaultValue the default value to return if not found
	 * @return the property value or the {@code defaultValue} if not defined
	 */
	public String getJobProperty(long executionId, String key, String defaultValue) {
		return BatchRuntime.getJobOperator()
				.getJobExecution(executionId)
				.getJobParameters()
				.getProperty(key, defaultValue);
	}

	/**
	 * Helper method for fetching the internal job id by job execution id
	 *
	 * @param executionId the job execution id
	 * @return the found id or null
	 */
	public String getJobId(long executionId) {
		return getJobProperty(executionId, JOB_ID);
	}

	/**
	 * Helper method for fetching the tenant identifier by job execution id
	 *
	 * @param executionId the job execution id
	 * @return the found tenant or {@link SecurityContext#SYSTEM_TENANT}
	 */
	public String getTenantId(long executionId) {
		return getJobProperty(executionId, TENANT_ID, SecurityContext.SYSTEM_TENANT);
	}

	/**
	 * Helper method for fetching the request job id by job execution id
	 *
	 * @param executionId the job execution id
	 * @return the found id or null
	 */
	public String getRequestId(long executionId) {
		return getJobProperty(executionId, REQUEST_ID);
	}

	/**
	 * Get the configured chunk size if any
	 *
	 * @param executionId the job execution id
	 * @return the chunk size
	 */
	public Optional<Integer> getChunkSize(long executionId) {
		String property = getJobProperty(executionId, CHUNK_SIZE);
		if (property == null) {
			return Optional.empty();
		}
		return Optional.of(Integer.valueOf(property));
	}
}
