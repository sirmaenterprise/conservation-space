package com.sirma.itt.seip.permissions.sync;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.batch.operations.JobExecutionNotRunningException;
import javax.batch.runtime.BatchRuntime;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.permissions.sync.batch.CompletedDryRunJobProcessingEvent;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.sep.instance.batch.BatchRequest;
import com.sirma.sep.instance.batch.BatchRequestBuilder;
import com.sirma.sep.instance.batch.BatchService;

/**
 * Synchronization service responsible for triggering permission synchronizations. <br>
 * The service supports per tenant custom two-phase synchronization. In the first phase the synchronization only returns
 * the affected instances with found changes and in the second phase the changes committed to the database. <br>
 * The intermediate data is stored in memory and is one per tenant. To trigger new synchronization the old
 * synchronization need to canceled or committed via the methods {@link #applySynchronizationChanges()} or
 * {@link #cancelSynchronization()}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/07/2017
 */
@Singleton
public class PermissionSynchronizationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String ALL_INSTANCES_QUERY = "select ?instance where {\n"
			+ "    ?instance emf:isDeleted \"false\"^^xsd:boolean.\n"
			+ "    ?instance emf:instanceType ?type.\n"
			+ "    FILTER( \n"
			+ "        ?type = \"objectinstance\"\n"
			+ "    ).\n"
			+ "}";
	private static final int CHUNK_SIZE = 100;

	@Inject
	private BatchService batchService;
	@Inject
	private SearchService searchService;

	@Inject
	private Contextual<SyncExecution> execution;

	@PostConstruct
	void init() {
		execution.initializeWith(SyncExecution::new);
	}

	/**
	 * Trigger the initial synchronization of the two phase process over all instances
	 *
	 * @return the batch job execution identifier that processes the current request.
	 * @throws SynchronizationAlreadyRunningException if other synchronization is already running
	 */
	public SyncExecution triggerPermissionChecking() throws SynchronizationAlreadyRunningException {
		if (execution.getContextValue().isRunning) {
			throw new SynchronizationAlreadyRunningException("There is other synchronization running. "
					+ "Wait for the first to finish or cancel it");
		}
		BatchRequest searchRequest = BatchRequestBuilder.fromSearch(
				"permissionSynchronizationDryRunJob", ALL_INSTANCES_QUERY, null, "instance", searchService);
		searchRequest.setChunkSize(CHUNK_SIZE);

		long jobExecutionId = batchService.execute(searchRequest);
		execution.getContextValue().start(jobExecutionId);

		return execution.getContextValue();
	}

	/**
	 * Get the synchronization data if available. The method will not wait for the data to be available
	 *
	 * @return the optional that may contains the synchronization data
	 * @throws NoSynchronizationException if no synchronization was triggered before calling this method
	 */
	public SyncExecution getCurrentExecution() throws NoSynchronizationException {
		checkForConcurrentSync();
		return execution.getContextValue();
	}

	private void checkForConcurrentSync() throws NoSynchronizationException {
		if (!execution.getContextValue().isRunning) {
			throw new NoSynchronizationException("There is no running synchronization.");
		}
	}

	/**
	 * Cancel any running two phase synchronization.
	 *
	 * @throws NoSynchronizationException if no synchronization was triggered before calling this method
	 */
	public void cancelSynchronization() throws NoSynchronizationException {
		checkForConcurrentSync();
		NoSynchronizationException error = null;
		try {
			BatchRuntime.getJobOperator().stop(execution.getContextValue().executionId);
		} catch (JobExecutionNotRunningException e) {
			LOGGER.trace("", e);
			error = new NoSynchronizationException("There is no running synchronization to cancel: " + e
					.getMessage());
		}
		// notify waiting requests for end and
		// reset the execution to allow new job starting
		execution.getContextValue().cancel();
		execution.clearContextValue();
		if (error != null) {
			throw error;
		}
	}

	/**
	 * Triggers the second phase of the synchronization that will apply the changes to the database. If the first
	 * phase is not completed, the method will block and wait for the data to get available
	 *
	 * @return the batch job execution identifier that processes the second phase of the synchronization
	 * @throws NoSynchronizationException if no synchronization was triggered before calling this method
	 */
	public long applySynchronizationChanges() throws NoSynchronizationException {
		checkForConcurrentSync();
		// if the execution is not ready we will wait until ready
		SyncExecution contextValue = execution.getContextValue();
		List<String> data = contextValue.waitForData().orElse(Collections.emptyList());
		if (isEmpty(data) || contextValue.isCancelled) {
			// reset the execution to allow new job starting
			execution.clearContextValue();
			return -1;
		}
		BatchRequest batchRequest = BatchRequestBuilder.fromCollection("permissionSynchronizationJob", data);
		batchRequest.setChunkSize(CHUNK_SIZE);

		long jobExecutionId = batchService.execute(batchRequest);
		// update the execution so we can cancel it if needed
		contextValue.executionId = jobExecutionId;

		// reset the execution to allow new job starting
		execution.clearContextValue();

		return jobExecutionId;
	}

	/**
	 * Perform synchronization for the given list of instance identifiers.
	 *
	 * @param ids the identifiers to process
	 * @return the batch job execution identifier that processes the current request
	 */
	public long syncGivenInstances(List<String> ids) {
		if (isEmpty(ids)) {
			return -1;
		}
		BatchRequest searchRequest = BatchRequestBuilder.fromCollection("permissionSynchronizationJob", ids);
		searchRequest.setChunkSize(CHUNK_SIZE);

		return batchService.execute(searchRequest);
	}

	void onFinishedDryRunJob(@Observes CompletedDryRunJobProcessingEvent event) {
		if (execution.getContextValue().isRunning
				&& execution.getContextValue().executionId == event.getExecutionId()
				&& !execution.getContextValue().isDone()) {
			if (event.isDone()) {
				// we first set the data then we notify for finished processing
				execution.getContextValue().done(event.getData());
			} else {
				// append the currently processed data
				execution.getContextValue().data.addAll(event.getData());
			}
		}
		//else we got event when no execution is processed here
	}

	/**
	 * Represent information about the current permission synchronization
	 *
	 * @author BBonev
	 */
	public static class SyncExecution {
		private CountDownLatch wait;
		private List<String> data = new LinkedList<>();
		private long executionId;
		private boolean isRunning;
		private boolean isCancelled;
		private boolean done;

		private void start(long jobExecutionId) {
			executionId = jobExecutionId;
			wait = new CountDownLatch(1);
			isRunning = true;
		}

		private void done(List<String> data) {
			this.data = data;
			done = true;
			wait.countDown();
		}

		/**
		 * Blocks and waits for the synchronization to complete. If the job is cancelled the method will return
		 * {@link Optional#empty()}
		 *
		 * @return the synchronization data
		 */
		public Optional<List<String>> waitForData() {
			try {
				wait.await();
			} catch (InterruptedException e) {
				LOGGER.warn("Interrupted waiting for synchronization data");
				LOGGER.trace("", e);
				return Optional.empty();
			}
			return Optional.of(data);
		}

		private void cancel() {
			isCancelled = true;
			wait.countDown();
		}

		/**
		 * Check if the currnet execution is cancelled or no
		 *
		 * @return if it's cancelled
		 */
		public boolean isCancelled() {
			return isCancelled;
		}

		/**
		 * Checks if the synchronization data collection is completed or not. This means the first phase of the
		 * synchronization is complete.
		 *
		 * @return if the first phase is finished or not
		 */
		public boolean isDone() {
			return done;
		}

		/**
		 * Get the current context execution id
		 *
		 * @return the batch job execution id that is currently running the synchronization phase
		 */
		public long getExecutionId() {
			return executionId;
		}

		/**
		 * Get the intermediate synchronization data. If not completed this method will return the currently
		 * processed and affected instances.
		 *
		 * @return the intermediate data
		 */
		public List<String> getData() {
			return data;
		}
	}
}
