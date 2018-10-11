package com.sirma.sep.instance.batch;

import static com.sirma.itt.seip.collections.CollectionUtils.createLinkedHashSet;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Default implementation of {@link BatchService} that stores in the written data in database table a single
 * row for each entry that should be processed.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/06/2017
 */
@ApplicationScoped
class BatchServiceImpl implements BatchService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final int JOB_ID_SIZE = 16;
	private static final int BATCH_SAVE_SIZE = 1024;

	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private SecurityContext securityContext;
	@Inject
	private BatchDataService batchDataService;
	@Inject
	private JobRunner jobRunner;

	@Override
	public long execute(BatchRequest request) {
		if (request instanceof StreamBatchRequest) {
			return execute((StreamBatchRequest) request);
		}

		String jobName = Objects.requireNonNull(request.getBatchName(), "Batch name is required");
		LOGGER.info("Starting batch job {}", jobName);
		return startJob(jobName, request);
	}

	@Override
	public long execute(StreamBatchRequest request) {
		String jobName = Objects.requireNonNull(request.getBatchName(), "Batch name is required");
		String jobId = buildJobId();
		try {
			int preparedData;
			try (Stream<Serializable> stream = request.getStreamSupplier().get()) {
				preparedData = writeStreamingData(jobName, jobId, stream);
			}

			Properties properties = request.getProperties();
			properties.put(BatchProperties.JOB_ID, jobId);

			if (preparedData > 0) {
				LOGGER.info("Starting batch job {} with instance id {}. Will process {} items", jobName, jobId,
						preparedData);
				return startJob(request.getBatchName(), request);
			}
			LOGGER.info("Tried to start job {}, but no data was found for it. Nothing will be started", jobName);
			return -1L;
		} catch (RuntimeException e) {
			// in case the current transaction is broken remove all data in new transaction
			transactionSupport.invokeInNewTx(() -> batchDataService.clearJobData(jobId));
			throw e;
		}
	}

	@Override
	public List<Long> getRunningExecutions(String jobName) {
		return jobRunner.getRunningExecutions(jobName);
	}

	@Override
	public Optional<JobExecution> getJobExecution(Long executionId) {
		return jobRunner.getJobExecution(executionId);
	}

	@Override
	public boolean hasJobInStatus(String jobName, List<BatchStatus> statuses) {
		List<Long> runningExecutions = getRunningExecutions(jobName);
		if (runningExecutions.isEmpty()) {
			return false;
		}
		return runningExecutions.stream()
				.map(this::getJobExecution)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(JobExecution::getBatchStatus)
				.anyMatch(statuses::contains);
	}

	@Override
	public void stopJobExecutions(String jobName) {
		jobRunner.stopJobExecutions(jobName);
	}

	@SuppressWarnings("unchecked")
	private int writeStreamingData(String jobName, String jobId, Stream<Serializable> dataStream) {
		// we cannot benefit from parallel streams so make sure it's sequential
		AtomicInteger written = new AtomicInteger();
		Set<Serializable> remaining = (Set<Serializable>) dataStream.sequential().reduce(
				(Serializable) createLinkedHashSet(BATCH_SAVE_SIZE), (accumulated, id) -> {
					Set<Serializable> current = (Set<Serializable>) accumulated;
					// when we reach enough items to trigger a transaction
					// write them and start collecting again
					if (current.size() == BATCH_SAVE_SIZE) {
						writeBatchData(jobName, jobId, current);
						written.addAndGet(current.size());
						current.clear();
					}
					current.add(id);
					return (Serializable) current;
				});

		// if the items are less than the persist batch or the last batch is not complete
		// we will have the remaining items and we need to write them
		writeBatchData(jobName, jobId, remaining);
		written.addAndGet(remaining.size());
		return written.get();
	}

	private static String buildJobId() {
		return RandomStringUtils.randomAlphanumeric(JOB_ID_SIZE);
	}

	private void writeBatchData(String jobName, String jobId, Collection<? extends Serializable> instanceIds) {
		if (isEmpty(instanceIds)) {
			return;
		}
		transactionSupport.invokeInNewTx(() -> {
			for (Serializable id : instanceIds) {
				batchDataService.addData(jobName, jobId, id.toString());
			}
		});
	}

	private long startJob(String jobName, BatchRequest request) {
		Properties properties = request.getProperties();
		fillSecurityInfo(properties);
		int chunk = request.getChunkSize();
		if (chunk <= 0) {
			chunk = BatchProperties.DEFAULT_CHUNK_SIZE;
		}
		// the chunk size should be a String otherwise the batch implementation does not read it properly
		properties.putIfAbsent(BatchProperties.CHUNK_SIZE, String.valueOf(chunk));
		properties.putIfAbsent(BatchProperties.PARTITIONS_COUNT, String.valueOf(request.getPartitionsCount()));
		return jobRunner.startJob(jobName, properties);
	}

	private void fillSecurityInfo(Properties properties) {
		properties.setProperty(BatchProperties.TENANT_ID, securityContext.getCurrentTenantId());
		properties.setProperty(BatchProperties.REQUEST_ID, securityContext.getRequestId());
		// if needed we can set authenticated user and/or request identifier
	}
}
