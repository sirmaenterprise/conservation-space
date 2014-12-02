/*
 *
 */
package com.sirma.itt.emf.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.event.ApplicationStartupEvent;
import com.sirma.itt.emf.scheduler.event.SchedulerEntryAddedEvent;
import com.sirma.itt.emf.scheduler.event.SchedulerEntryUpdatedEvent;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Schedule executor that executes the timed scheduled actions using a ForkJoinPool.
 *
 * @author BBonev
 */
@ApplicationScoped
public class TimedScheduleExecutor {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimedScheduleExecutor.class);
	/** The trace. */
	private static final boolean TRACE = LOGGER.isTraceEnabled();
	/** The debug. */
	private static final boolean DEBUG = LOGGER.isDebugEnabled();

	/** The Constant ENTRY_TYPE. */
	private static final Set<SchedulerEntryType> ENTRY_TYPE = CollectionUtils
			.createLinkedHashSet(3);

	/** The Constant ENTRY_STASUS. */
	private static final Set<SchedulerEntryStatus> ACTIVE_STASUS = CollectionUtils
			.createLinkedHashSet(5);

	private static final Set<SchedulerEntryStatus> COMPLETED_STASUS = CollectionUtils
			.createLinkedHashSet(4);

	/** The main executor feature. */
	private ScheduledFuture<?> mainExecutorFeature;

	/** The main executor. */
	private ScheduledExecutorService mainExecutor;

	/** The scheduled executor. */
	private ExecutorService scheduledExecutor;

	/** The running tasks. */
	private Map<Long, Pair<Future<Boolean>, TaskExecutor>> runningTasks;

	/** The timed executor delay in seconds. */
	@Inject
	@Config(name = EmfConfigurationProperties.TIME_SCHEDULER_EXECUTOR_CHECK_INTERVAL, defaultValue = "60")
	private Long timedExecutorDelay;
	/** The default retry delay. */
	@Inject
	@Config(name = EmfConfigurationProperties.TIME_SCHEDULER_EXECUTOR_RETRY_INTERVAL, defaultValue = "60")
	private Long defaultRetryDelay;

	/** The current executor delay. */
	private long currentExecutorDelay;

	/** The db dao. */
	@Inject
	private DbDao dbDao;
	/** The entity manager. */
	@PersistenceContext(unitName = DbDao.PERSISTENCE_UNIT_NAME, type = PersistenceContextType.TRANSACTION)
	private EntityManager entityManager;

	/** The scheduler service. */
	@Inject
	private SchedulerService schedulerService;

	/** The scheduler executer. */
	@Inject
	private SchedulerExecuter schedulerExecuter;

	/** The main execution command. */
	private Runnable mainExecutionCommand;

	/** The lock used to synchronize the access to the running tasks map */
	private ReadWriteLock runningLock = new ReentrantReadWriteLock();

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		currentExecutorDelay = timedExecutorDelay;

		ENTRY_TYPE.add(SchedulerEntryType.TIMED);
		ENTRY_TYPE.add(SchedulerEntryType.CRON);

		ACTIVE_STASUS.add(SchedulerEntryStatus.NOT_RUN);
		ACTIVE_STASUS.add(SchedulerEntryStatus.RUNNING);
		ACTIVE_STASUS.add(SchedulerEntryStatus.RUN_WITH_ERROR);
		ACTIVE_STASUS.add(SchedulerEntryStatus.PENDING);

		COMPLETED_STASUS.add(SchedulerEntryStatus.COMPLETED);
		COMPLETED_STASUS.add(SchedulerEntryStatus.CANCELED);
		COMPLETED_STASUS.add(SchedulerEntryStatus.FAILED);

		int cores = Runtime.getRuntime().availableProcessors();
		runningTasks = new ConcurrentHashMap<Long, Pair<Future<Boolean>, TaskExecutor>>(100, 0.9f,
				cores);

		ThreadFactory threadFactory = new DaemonThreadFactory();
		mainExecutor = Executors.newSingleThreadScheduledExecutor(threadFactory);
		scheduledExecutor = Executors.newFixedThreadPool(cores, threadFactory);

		mainExecutionCommand = new MainTaskChecker(this);

		scheduleMainChecker(1L);
	}

	/**
	 * On application start.
	 * 
	 * @param event
	 *            the event
	 */
	public void onApplicationStart(@Observes ApplicationStartupEvent event) {
		// creates the instance on application start and schedules checking for tasks for execution
	}

	/**
	 * On shutdown stops all tasks and executors.
	 */
	@PreDestroy
	public void onShutdown() {
		LOGGER.info("Shutting down executor threads");
		mainExecutorFeature.cancel(true);

		runningLock.readLock().lock();
		try {
			for (Entry<Long, Pair<Future<Boolean>, TaskExecutor>> entry : runningTasks.entrySet()) {
				entry.getValue().getFirst().cancel(true);
			}
		} finally {
			runningLock.readLock().unlock();
		}
		try {
			mainExecutor.shutdownNow();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
		try {
			scheduledExecutor.shutdownNow();
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
	}

	/**
	 * Schedule main checker.
	 *
	 * @param delay
	 *            the delay
	 */
	void scheduleMainChecker(long delay) {
		ScheduledFuture<?> oldSchedule = mainExecutorFeature;
		if (DEBUG) {
			LOGGER.debug("Scheduling Main timed scheduler checker to run in {} seconds", delay);
		}
		// schedule to run one check before the next time
		mainExecutorFeature = mainExecutor.scheduleAtFixedRate(mainExecutionCommand, delay,
				timedExecutorDelay, TimeUnit.SECONDS);
		if (oldSchedule != null) {
			oldSchedule.cancel(false);
		}
	}

	/**
	 * Listens for new added entries and notifies the main thread if needed.
	 *
	 * @param event
	 *            the event
	 */
	public void onTaskAdded(
			@Observes(during = TransactionPhase.AFTER_SUCCESS) SchedulerEntryAddedEvent event) {
		SchedulerEntry entry = event.getEntry();
		onEvent(entry);
	}

	/**
	 * Listens for updated entries and notifies the main thread if needed.
	 *
	 * @param event
	 *            the event
	 */
	public void onTaskUpdated(
			@Observes(during = TransactionPhase.AFTER_SUCCESS) SchedulerEntryUpdatedEvent event) {
		onEvent(event.getNewEntry());
	}

	/**
	 * On event.
	 *
	 * @param entry
	 *            the entry
	 */
	private void onEvent(SchedulerEntry entry) {
		if (COMPLETED_STASUS.contains(entry.getStatus())) {
			runningLock.writeLock().lock();
			try {
				// if the task is scheduled for execution we should cancel if possible
				Pair<Future<Boolean>, TaskExecutor> pair = runningTasks.remove(entry.getId());
				if (pair != null) {
					pair.getFirst().cancel(false);
				}
			} finally {
				runningLock.writeLock().unlock();
			}
			// nothing to do with completed entries
			return;
		}

		// if the task is scheduled for execution we should return because we cannot do more
		if (isTaskScheduledAndRunning(entry)) {
			return;
		}

		rescheduleEntry(entry);
	}

	/**
	 * Reschedule entry.
	 * 
	 * @param entry
	 *            the entry
	 */
	private void rescheduleEntry(SchedulerEntry entry) {
		SchedulerEntryType type = entry.getConfiguration().getType();
		if ((type == SchedulerEntryType.TIMED) || (type == SchedulerEntryType.CRON)) {
			Date nextScheduleDate = entry.getConfiguration().getNextScheduleTime();
			// ensure the next check for time difference is enough not to be in the past
			long nextScheduleTime = System.currentTimeMillis() + 50;
			if (nextScheduleDate != null) {
				nextScheduleTime = nextScheduleDate.getTime();
			}
			// get the remaining wait time for the next wake up of the checker
			long delay = mainExecutorFeature.getDelay(TimeUnit.MILLISECONDS);
			// how much time is needed to pass before we need to execute the entry
			long diff = nextScheduleTime - System.currentTimeMillis();
			if (diff < (timedExecutorDelay * 1000)) {
				// scheduled time is in the limit of the checker interval we schedule the task write
				// away. when picked up by the query it will be skipped when called the schedule
				// method again
				scheduleTask(entry, -1);
				return;
			}
			// check when we should wake up the executor when the delay is more then the default
			// wait time

			// check if the current delay is more then one run interval and the delay is bigger that
			// the delay for the given task
			if (((delay / (timedExecutorDelay * 1000)) > 1L) && (delay > diff)) {
				// if the entry should be run in the next up to 5 seconds then we run it now (we
				// will collect the entry from the DB and it will stay in the queue for waiting for
				// execution)
				long startIn = (diff - 5000L) / 1000L;
				if (startIn < 0L) {
					// if we a getting late then run it right away
					startIn = 1L;
				}
				scheduleMainChecker(startIn);
			}
		}
	}

	/**
	 * Gets the tasks for execution.
	 *
	 * @return the tasks for execution
	 */
	List<SchedulerEntry> getTasksForExecution() {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(3);
		args.add(new Pair<String, Object>("type", ENTRY_TYPE));
		args.add(new Pair<String, Object>("status", ACTIVE_STASUS));
		Date next = new Date(System.currentTimeMillis() + (currentExecutorDelay * 1000L));
		args.add(new Triplet<String, Object, TemporalType>("next", next, TemporalType.TIMESTAMP));
		List<Long> list = dbDao.fetchWithNamed(
				EmfQueries.QUERY_SCHEDULER_TASKS_FOR_TIMED_EXECUTION_KEY, args);
		Set<Long> set = new LinkedHashSet<Long>(list);
		// remove all currently running tasks
		runningLock.readLock().lock();
		try {
			set.removeAll(runningTasks.keySet());
		} finally {
			runningLock.readLock().unlock();
		}
		// load the actual entries and update their state right away
		List<SchedulerEntry> entries = schedulerService.loadAndActivateByDbId(new ArrayList<Long>(
				set));
		return entries;
	}

	/**
	 * Schedule task for execution to wait in the execution queue in memory.
	 * 
	 * @param schedulerEntry
	 *            the scheduler entry
	 * @param fixedDelay
	 *            the fixed delay
	 */
	protected void scheduleTask(SchedulerEntry schedulerEntry, long fixedDelay) {
		if (isTaskScheduledAndRunning(schedulerEntry)) {
			// ignore request still in execution
			return;
		}
		// schedule the task for execution
		TaskExecutor task = new TaskExecutor(schedulerEntry, this, fixedDelay);
		Future<Boolean> future = scheduledExecutor.submit(task);

		runningLock.writeLock().lock();
		try {
			runningTasks.remove(schedulerEntry.getId());
			runningTasks.put(schedulerEntry.getId(), new Pair<Future<Boolean>, TaskExecutor>(
					future, task));
		} finally {
			runningLock.writeLock().unlock();
		}
	}

	/**
	 * Checks if this task is scheduled and running.
	 * 
	 * @param schedulerEntry
	 *            the scheduler entry
	 * @return true, if is task scheduled and running
	 */
	private boolean isTaskScheduledAndRunning(SchedulerEntry schedulerEntry) {
		runningLock.readLock().lock();
		try {
			// check if the task is scheduled for execution and check it's status to be sure.
			// note if the task is in the running tasks it should not have a status different than
			// the one checked bellow
			if (runningTasks.containsKey(schedulerEntry.getId())) {
				Pair<Future<Boolean>, TaskExecutor> pair = runningTasks.get(schedulerEntry.getId());
				SchedulerEntryStatus status = pair.getSecond().getStatus();
				// if the task is already scheduled we ignore the request
				switch (status) {
					case NOT_RUN:
					case RUNNING:
					case PENDING:
						LOGGER.trace(
								"Skipped request for multiple add to schedule a task with ID={}",
								schedulerEntry.getId());
						return true;
					default:
						break;
				}
			}
		} finally {
			runningLock.readLock().unlock();
		}
		return false;
	}

	/**
	 * Gets the next probable run time.
	 *
	 * @return the next run time
	 */
	@SuppressWarnings("unchecked")
	Date getNextProbableRunTime() {
		Query query = entityManager.createNamedQuery(EmfQueries.QUERY_NEXT_EXECUTION_TIME_KEY);
		query.setMaxResults(1);
		query.setParameter("type", ENTRY_TYPE);
		query.setParameter("status", ACTIVE_STASUS);
		Date next = new Date(System.currentTimeMillis() + (currentExecutorDelay * 1000L));
		query.setParameter("next", next, TemporalType.TIMESTAMP);
		List<Date> list = query.getResultList();
		if ((list == null) || list.isEmpty()) {
			// if there are no task for execution after currentExecutorDelay (60 sec)) from now then
			// we schedule the next check for 11 times currentExecutorDelay. We schedule for 11
			// times because the decision when to be the next execution will check if there at least
			// 10 more empty activations
			return new Date(System.currentTimeMillis() + (currentExecutorDelay * 1000L * 11L));
		}
		// if we have tasks for execution in the near future after the currentExecutorDelay we
		// schedule for the first task that need to be executed.
		return list.get(0);
	}

	/**
	 * Execute action.
	 *
	 * @param entry
	 *            the entry
	 * @return true, if successful
	 */
	boolean executeAction(final SchedulerEntry entry) {
		return SecurityContextManager.callAsSystem(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return dbDao.invokeInTx(new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return schedulerExecuter.executeImmediate(entry);
					}
				});
			}
		});
	}

	/**
	 * Main task that checks for timed tasks for execution.
	 *
	 * @author BBonev
	 */
	class MainTaskChecker implements Runnable {

		/** The executor. */
		private TimedScheduleExecutor executor;

		/**
		 * Instantiates a new main task checker.
		 *
		 * @param executor
		 *            the timed schedule executor
		 */
		public MainTaskChecker(TimedScheduleExecutor executor) {
			this.executor = executor;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				List<SchedulerEntry> tasksForExecution = executor.getTasksForExecution();
				if (TRACE) {
					LOGGER.trace("Collected {} tasks for execution", tasksForExecution.size());
				}
				for (SchedulerEntry schedulerEntry : tasksForExecution) {
					executor.scheduleTask(schedulerEntry, -1L);
				}

				// reschedule only if there are no current tasks, due to current tasks may be
				// rescheduled in some time so we need to check again for such.
				if (hasActiveTasks()) {
					// rescheduling of the task if the next task is way ahead
					Date nextRunTime = executor.getNextProbableRunTime();
					long diff = nextRunTime.getTime() - System.currentTimeMillis();
					long times = (diff / 1000L) / executor.timedExecutorDelay;
					// if we have time to fire more then 10 times before we have some work to do
					// when we
					// will reschedule otherwise we will just keep checking for tasks
					if (times > 10L) {
						// reschedule
						executor.scheduleMainChecker((diff - executor.timedExecutorDelay) / 1000L);
					}
				}
			} catch (Exception e) {
				LOGGER.warn("Error when collecting tasks for execution: {}", e.getMessage());
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("", e);
				}
			}
		}

	}

	/**
	 * Checks for active tasks.
	 *
	 * @return true, if there is at least one running task
	 */
	public boolean hasActiveTasks() {
		runningLock.readLock().lock();
		try {
			return runningTasks.isEmpty();
		} finally {
			runningLock.readLock().unlock();
		}
	}

	/**
	 * Removes the running task.
	 *
	 * @param taskId
	 *            the task id
	 */
	public void removeRunningTask(Long taskId) {
		runningLock.writeLock().lock();
		try {
			runningTasks.remove(taskId);
		} finally {
			runningLock.writeLock().unlock();
		}
	}

	/**
	 * Gets the default retry delay.
	 *
	 * @return the default retry delay
	 */
	public long getDefaultRetryDelay() {
		return defaultRetryDelay;
	}

	/**
	 * Gets the timed executor delay in seconds.
	 *
	 * @return the timed executor delay in seconds
	 */
	public long getTimedExecutorDelay() {
		return timedExecutorDelay;
	}

	/**
	 * Method called when the {@link TaskExecutor} finishes the execution of the specified entry.
	 * The method will persist the entry changes.
	 *
	 * @param entry
	 *            the entry
	 */
	public void finishedExecution(SchedulerEntry entry) {
		try {
			schedulerService.save(entry);
		} finally {
			removeRunningTask(entry.getId());
		}
	}
}
