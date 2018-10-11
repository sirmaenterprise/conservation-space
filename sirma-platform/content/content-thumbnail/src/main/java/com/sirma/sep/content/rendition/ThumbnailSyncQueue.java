package com.sirma.sep.content.rendition;

import static com.sirma.itt.seip.collections.CollectionUtils.createLinkedHashSet;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.context.RuntimeContext.CurrentRuntimeConfiguration;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.context.ThreadFactories;
import com.sirma.itt.seip.security.util.SecureRunnable;

/**
 * Processor for thumbnails synchronization that uses a Producer/Supplier pattern the process thumbnails
 * synchronization. The queue processed data in parallel using blocking deque for synchronization. The implementation
 * checks not to process identical requests at the same time by internal cache of thumbnail end points.
 * <p>
 * Note that different queues and threads are used for different tenants
 *
 * @author BBonev
 */
@ApplicationScoped
public class ThumbnailSyncQueue {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@ConfigurationPropertyDefinition(defaultValue = "10000", sensitive = true, type = Integer.class, label = "Property to define the maximum elements to wait for loading")
	static final String STORE_CAPACITY = "thumbnail.loader.storeCapacity";

	@ConfigurationPropertyDefinition(defaultValue = "2", sensitive = true, type = Integer.class, label = "Property to define thumbnail loader threads. ")
	static final String WORKER_THREADS = "thumbnail.loader.threads";

	@ConfigurationGroupDefinition(properties = { WORKER_THREADS, STORE_CAPACITY }, type = SyncQueueWorkers.class)
	private static final String WORKER_GROUP = "thumbnail.loader.group";

	@Inject
	@Configuration(WORKER_GROUP)
	private ConfigurationProperty<SyncQueueWorkers> workerGroup;

	private volatile boolean enabled = true;

	/**
	 * Builds a workers group that manages a private thread pool with own working store
	 *
	 * @param context
	 *            the converter context
	 * @param securityContextManager
	 *            the security context manager
	 * @param thumbnailLoader
	 *            the thumbnail loader
	 * @return a queue worker group to process sync requests per tenant
	 */
	@ConfigurationConverter(WORKER_GROUP)
	static SyncQueueWorkers buildWorkerGroup(GroupConverterContext context,
			SecurityContextManager securityContextManager, ThumbnailLoader thumbnailLoader) {
		Integer capacity = context.get(STORE_CAPACITY);
		Integer parallelism = context.get(WORKER_THREADS);
		return new SyncQueueWorkers(provider -> new SyncQueueWorkerGroup(provider, capacity, parallelism, securityContextManager,
				thumbnailLoader));
	}

	/**
	 * Initialize the queue.
	 */
	@PostConstruct
	void initQueue() {
		workerGroup.addValueDestroyListener(SyncQueueWorkers::stopWork);
	}

	/**
	 * Enable queue if not active. If already active the method does nothing. Starts all workers and any requests send
	 * will start processing.
	 */
	void enable() {
		if (enabled) {
			return;
		}
		try {
			workerGroup.get().startWorkers();
		} finally {
			enabled = true;
		}
	}

	/**
	 * Disable queue if active. If not active the method does nothing. All workers will be stopped and any add request
	 * will be ignored
	 */
	void disable() {
		if (!enabled) {
			return;
		}
		try {
			workerGroup.get().stopWork();
		} finally {
			enabled = false;
		}
	}

	/**
	 * Adds entry for execution
	 *
	 * @param id
	 *            the id
	 * @param endPoint
	 *            the end point
	 * @param providerName
	 *            the provider name
	 * @param retries
	 *            the retries
	 * @return true, if successful entry is not currently processing or <code>false</code> if the passed information is
	 *         currently processed or the queue is full.
	 */
	boolean add(String id, String endPoint, String providerName, Integer retries) {
		return scheduleForProcessing(id, endPoint, providerName, retries);
	}

	/**
	 * Adds all of the given items to the queue. The array elements must be in the same order as for the method
	 * {@link #add(String, String, String, Integer)}.
	 *
	 * @param entries
	 *            the entries
	 * @return the number of actually added items
	 */
	int addAll(Collection<Object[]> entries) {
		int added = 0;
		for (Object[] objects : entries) {
			if (scheduleForProcessing((String) objects[0], (String) objects[1], (String) objects[2], (Integer) objects[3])) {
				added++;
			}
		}
		return added;
	}

	/**
	 * The number of waiting items for processing
	 *
	 * @return the int
	 */
	int size() {
		return workerGroup.get().getStoreSize();
	}

	/**
	 * Removes all items currently processing
	 */
	synchronized void clear() {
		LOGGER.debug("Resetting queue");
		workerGroup.get().clear(null);
	}

	/**
	 * Removes all items currently processing
	 */
	synchronized void clear(String provider) {
		LOGGER.debug("Resetting queue {}", provider);
		workerGroup.get().clear(provider);
	}

	/**
	 * Gets the worker info.
	 *
	 * @return the worker info
	 */
	Collection<JSONObject> getWorkerInfo() {
		return workerGroup.get().getWorkerInfo();
	}

	private boolean scheduleForProcessing(String id, String endPoint, String providerName, Integer retries) {
		if (id == null || endPoint == null || providerName == null) {
			LOGGER.trace(
					"Adding to queue is skipped. Missing required parameted - id: {}, endPoint: {}, providerName:{}.",
					id, endPoint, providerName);
			return false;
		}
		// skip currently processing
		if (!workerGroup.get().getWorkerGroup(providerName).markForProcessing(endPoint)) {
			LOGGER.trace("The endPoint: [{}] already exists in the waiting set. Adding to queue is skipped.", endPoint);
			return false;
		}
		return addToQueue(new ThumbnailSyncEntry(id, endPoint, providerName, retries));
	}

	private boolean addToQueue(ThumbnailSyncEntry entry) {
		SyncQueueWorkerGroup queueWorkerGroup = workerGroup.get().getWorkerGroup(entry.providerName);
		try {
			// if could not add the entry for execution it should be removed from the waiting list too
			if (!queueWorkerGroup.offer(entry)) {
				LOGGER.trace("Enrty {} could not be added. It is removed from the waiting set.", entry);
				queueWorkerGroup.markDoneProcessing(entry.endPoint);
				return false;
			}
			return true;
		} catch (Exception e) {
			queueWorkerGroup.markDoneProcessing(entry.endPoint);
			LOGGER.warn("", e);
		}
		return false;
	}

	/**
	 * Wrapper class for SyncQueueWorkerGroup that groups workers per provider. so that if one of the providers hand it
	 * does not affect the rest of the workers
	 */
	private static class SyncQueueWorkers {
		private final Map<String, SyncQueueWorkerGroup> workers = new HashMap<>();
		private final Function<String, SyncQueueWorkerGroup> workerGroupBuilder;

		SyncQueueWorkers(Function<String, SyncQueueWorkerGroup> workerGroupBuilder) {
			this.workerGroupBuilder = workerGroupBuilder;
		}

		SyncQueueWorkerGroup getWorkerGroup(String provider) {
			return workers.computeIfAbsent(provider, workerGroupBuilder);
		}

		void stopWork() {
			workers.values().forEach(SyncQueueWorkerGroup::stopWork);
		}

		void startWorkers() {
			workers.values().forEach(SyncQueueWorkerGroup::startWorkers);
		}

		int getStoreSize() {
			return workers.values().stream().mapToInt(worker -> worker.getStore().size()).sum();
		}

		void clear(String provider) {
			if (StringUtils.isBlank(provider)) {
				workers.values().forEach(SyncQueueWorkerGroup::clear);
			} else {
				SyncQueueWorkerGroup workerGroup = workers.get(provider);
				if (workerGroup != null) {
					workerGroup.clear();
				}
			}
		}

		Collection<JSONObject> getWorkerInfo() {
			return workers.values()
					.stream()
					.flatMap(worker -> worker.getWorkerInfo().stream())
					.collect(Collectors.toList());
		}
	}

	/**
	 * Worker group for the queue to contain the worker executor server and his workers and the data sync. All workers
	 * operate on the data sync and process the waiting data.
	 * <p>
	 * The class implement equals and hash code methods that checks for workersCount and storeCapacity. These are
	 * checked when comparing for changes in the configuration
	 *
	 * @author BBonev
	 */
	private static class SyncQueueWorkerGroup {
		private final Set<String> waiting = new ConcurrentSkipListSet<>();
		private final Supplier<ExecutorService> executorFactory;
		private final Set<ThumbnailSyncWorker> workers;
		private final BlockingDeque<ThumbnailSyncEntry> store;
		private final Supplier<ThumbnailSyncWorker> workerFactory;
		private final int workersCount;
		private final int storeCapacity;

		/**
		 * Instantiates a new sync queue worker group.
		 *
		 * @param provider
		 *            the assigned provider
		 * @param storeCapacity
		 *            the store capacity
		 * @param workersCount
		 *            the workers count
		 * @param securityContextManager
		 *            the security context manager
		 * @param thumbnailLoader
		 *            the thumbnail loader
		 */
		SyncQueueWorkerGroup(String provider, int storeCapacity, int workersCount,
				SecurityContextManager securityContextManager, ThumbnailLoader thumbnailLoader) {
			this.storeCapacity = storeCapacity;
			this.workersCount = workersCount;
			store = new LinkedBlockingDeque<>(storeCapacity);
			workers = createLinkedHashSet(workersCount);

			executorFactory = new CachingSupplier<>(() -> createExecutorService(provider, workersCount, securityContextManager));
			workerFactory = () -> buildWorker(provider, securityContextManager, waiting, thumbnailLoader);
		}

		/**
		 * Creates a executor service for the given parallelism.
		 *
		 * @param provider
		 *            provider name used to update the thread names
		 * @param parallelism
		 *            the needed parallelism for processing
		 * @return the executor service
		 */
		private static ExecutorService createExecutorService(String provider, int parallelism,
				SecurityContextManager securityContextManager) {
			return Executors.newFixedThreadPool(parallelism,
					ThreadFactories.createSystemThreadFactory(securityContextManager, thread -> {
						thread.setName(thread.getName() + "-" + provider);
						thread.setDaemon(true);
						return thread;
					}));
		}

		private ThumbnailSyncWorker buildWorker(String provider, SecurityContextManager securityManager, Set<String> waitingQueue,
				ThumbnailLoader loader) {
			return new ThumbnailSyncWorker(provider, store, securityManager, entry -> loadThumbnail(entry, waitingQueue, loader),
					onWorkerCancellation());
		}

		private Consumer<ThumbnailSyncWorker> onWorkerCancellation() {
			return worker -> {
				// we may have parallel removal so ensure single access here
				synchronized (workers) {
					if (workers.remove(worker)) {
						LOGGER.debug("Removed worker..");
					}
				}
			};
		}

		private static void loadThumbnail(ThumbnailSyncEntry entry, Set<String> waitingQueue, ThumbnailLoader loader) {
			try {
				loader.load(entry.id, entry.endPoint, entry.providerName, entry.retries);
			} finally {
				waitingQueue.remove(entry.endPoint);
			}
		}

		/**
		 * Stop any previous workers and fill the worker pool with new workers
		 */
		void startWorkers() {
			stopWorkers();
			for (int i = 0; i < workersCount; i++) {
				ThumbnailSyncWorker worker = workerFactory.get();
				Future<?> future = executorFactory.get().submit(worker);
				worker.setFuture(future);
				workers.add(worker);
			}
		}

		/**
		 * Stop workers and shutdown the thread pool
		 */
		void stopWork() {
			stopWorkers();
			executorFactory.get().shutdown();
			Resettable.reset(executorFactory);
			waiting.clear();
		}

		private void stopWorkers() {
			// first copy the workers as the cancel method will also trigger remove from the original worker list
			new ArrayList<>(workers).forEach(worker -> worker.cancel(true));
			workers.clear();
		}

		Collection<JSONObject> getWorkerInfo() {
			List<JSONObject> list = new ArrayList<>(workers.size());
			// to prevent concurrent modification exception with workers reset
			synchronized (workers) {
				for (ThumbnailSyncWorker worker : workers) {
					list.add(worker.toJSONObject());
				}
			}
			return list;
		}

		boolean offer(ThumbnailSyncEntry entry) throws InterruptedException {
			ensureActiveWorkers();
			return getStore().offerLast(entry, 500, TimeUnit.MILLISECONDS);
		}

		private void ensureActiveWorkers() {
			if (isEmpty(workers)) {
				startWorkers();
			}
		}

		/**
		 * Get the store that is operated by the workers
		 *
		 * @return the store instance
		 */
		BlockingDeque<ThumbnailSyncEntry> getStore() {
			return store;
		}

		/**
		 * Clear the store and the current processing list
		 */
		void clear() {
			store.clear();
			waiting.clear();
		}

		/**
		 * Add the given element identifier as processing
		 *
		 * @param id
		 *            the id to add
		 * @return <code>true</code> if added successfully
		 */
		boolean markForProcessing(String id) {
			return waiting.add(id);
		}

		/**
		 * Remove the given identifier as done processing
		 *
		 * @param id
		 *            to remove
		 */
		void markDoneProcessing(String id) {
			waiting.remove(id);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + storeCapacity;
			result = prime * result + workersCount;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof SyncQueueWorkerGroup)) {
				return false;
			}
			SyncQueueWorkerGroup other = (SyncQueueWorkerGroup) obj;
			return storeCapacity == other.storeCapacity && workersCount == other.workersCount;
		}
	}

	/**
	 * Secure runnable worker that will consume items from the given store suppler and provide them to the given
	 * consumer
	 *
	 * @author BBonev
	 */
	private static class ThumbnailSyncWorker extends SecureRunnable implements JsonRepresentable {

		private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		private final BlockingDeque<ThumbnailSyncEntry> storeRef;
		private final Consumer<ThumbnailSyncEntry> entryConsumer;
		private final String assignedTo;
		private final Consumer<ThumbnailSyncWorker> cancellationListener;
		private String name;
		private WorkerStatus status;
		private ThumbnailSyncEntry entry;

		/**
		 * Instantiates a new thumbnail sync worker.
		 *
		 * @param provider
		 *            the assigned provider
		 * @param storeRef
		 *            the store ref
		 * @param securityManager
		 *            the security manager
		 * @param entryConsumer
		 *            the entry consumer
		 * @param cancellationListener
		 *            listener to be notified in case the worker is stopped or cancelled
		 */
		ThumbnailSyncWorker(String provider, BlockingDeque<ThumbnailSyncEntry> storeRef,
				SecurityContextManager securityManager, Consumer<ThumbnailSyncEntry> entryConsumer,
				Consumer<ThumbnailSyncWorker> cancellationListener) {
			super(securityManager);
			this.assignedTo = provider;
			this.cancellationListener = Objects.requireNonNull(cancellationListener,
					"Cancellation listener is required");
			this.storeRef = Objects.requireNonNull(storeRef, "Store reference is required");
			this.entryConsumer = Objects.requireNonNull(entryConsumer, "Entity consumer is required");
			status = WorkerStatus.NOT_RUN;
		}

		@Override
		protected void doRun() {
			name = Thread.currentThread().getName();
			LOG.debug("Started worker {}({})", getClass().getSimpleName(), name);
			while (!Thread.currentThread().isInterrupted()) {
				try {
					status = WorkerStatus.WAITING_FOR_DATA;
					entry = storeRef.takeFirst();
					status = WorkerStatus.WORKING;
					entryConsumer.accept(entry);
					entry = null;
				} catch (InterruptedException e) {
					// the exception does not carry any information we have a proper logging after that
					LOG.trace("", e);
					if (isCanceled()) {
						cancellationListener.accept(this);
						status = WorkerStatus.CANCELED;
						LOG.debug("Worker {}({}) canceled", getClass().getSimpleName(), name);
						return;
					}
				}
			}
			status = WorkerStatus.STOPPED;
			LOG.debug("Worker {}({}) stopped", getClass().getSimpleName(), name);
		}

		@Override
		protected void afterCall(CurrentRuntimeConfiguration oldConfiguration) {
			// if the worker is stopped or cancelled for some reason, notify for it's end
			cancellationListener.accept(this);
			super.afterCall(oldConfiguration);
		}

		@Override
		public JSONObject toJSONObject() {
			JSONObject object = new JSONObject();
			JsonUtil.addToJson(object, "name", name);
			JsonUtil.addToJson(object, "assignedTo", assignedTo);
			JsonUtil.addToJson(object, "status", status);
			// create local copy to the reference no prevent race condition
			ThumbnailSyncEntry entryRef = entry;
			if (entryRef != null) {
				JsonUtil.addToJson(object, "workingOn", entryRef.toJSONObject());
			}
			return object;
		}

		@Override
		public void fromJSONObject(JSONObject jsonObject) {
			throw new UnsupportedOperationException();
		}

		/**
		 * Worker status
		 *
		 * @author BBonev
		 */
		private enum WorkerStatus {
			NOT_RUN, WAITING_FOR_DATA, CANCELED, STOPPED, WORKING
		}
	}

	/**
	 * Entity object that is stored in the queue for processing
	 *
	 * @author BBonev
	 */
	private static class ThumbnailSyncEntry implements Serializable, JsonRepresentable {
		private static final long serialVersionUID = -2081803031511745570L;

		final String id;
		final String endPoint;
		final String providerName;
		final Integer retries;

		/**
		 * Instantiates a new thumbnail sync entry.
		 *
		 * @param id
		 *            the id
		 * @param endPoint
		 *            the end point
		 * @param providerName
		 *            the provider name
		 * @param retries
		 *            the retries
		 */
		ThumbnailSyncEntry(String id, String endPoint, String providerName, Integer retries) {
			this.id = id;
			this.endPoint = endPoint;
			this.providerName = providerName;
			this.retries = retries;
		}

		@Override
		public JSONObject toJSONObject() {
			JSONObject workingOn = new JSONObject();
			JsonUtil.addToJson(workingOn, "id", id);
			JsonUtil.addToJson(workingOn, "endPoint", endPoint);
			JsonUtil.addToJson(workingOn, "providerName", providerName);
			JsonUtil.addToJson(workingOn, "retries", retries);
			return workingOn;
		}

		@Override
		public void fromJSONObject(JSONObject jsonObject) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return new StringBuilder()
					.append("ThumbnailSyncEntry [id=")
						.append(id)
						.append(", endPoint=")
						.append(endPoint)
						.append(", providerName=")
						.append(providerName)
						.append(", retries=")
						.append(retries)
						.append("]")
						.toString();
		}

	}
}
