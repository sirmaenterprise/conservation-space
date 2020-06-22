package com.sirma.itt.seip.tasks;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.ContextualSet;
import com.sirma.itt.seip.security.annotation.OnTenantRemove;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.context.ThreadFactories;
import com.sirma.itt.seip.security.util.SecureRunnable;

/**
 * Provides means for scheduling tasks for registering a trigger that will initiate scheduler tasks checks and execution
 *
 * @author BBonev
 */
@ApplicationScoped
public class MainSchedulerTrigger {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ContextualSet<SecureRunnable> tasks;
	@Inject
	private SecurityContext securityContext;
	@Inject
	private SecurityContextManager securityContextManager;

	private ScheduledExecutorService mainExecutor;

	@PostConstruct
	void initialize() {
		int cores = Runtime.getRuntime().availableProcessors();

		tasks.onDestroy(set -> set.forEach(SecureRunnable::cancel));

		ThreadFactory threadFactory = ThreadFactories.createSystemThreadFactory(securityContextManager,
				ThreadFactories::asDaemon);
		ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(cores, threadFactory);
		// enable the policy so that canceled task to be removed
		scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
		scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

		mainExecutor = scheduledThreadPoolExecutor;
	}

	/**
	 * Schedule an {@link Executable} that need to be run at the given interval in seconds.
	 *
	 * @param executable
	 *            the executable to run
	 * @param checkInterval
	 *            the check interval in seconds
	 * @param timeUnit
	 *            the time unit for the check interval parameter
	 * @return the future that represents the scheduled executable
	 */
	@SuppressWarnings("boxing")
	Future<Object> scheduleMainChecker(Executable executable, long checkInterval, TimeUnit timeUnit) {
		LOGGER.debug("Scheduling main a scheduler checker to run in {} seconds for tenant {}", checkInterval,
				securityContext.getCurrentTenantId());

		SecureRunnable wrapped = SecureRunnable.wrap(securityContextManager, executable::execute);
		// the initial delay is set to zero so that immediately to start checking for tasks
		// this is set in conjunction with the removed of the initial loading before calling this method
		// that is causing transaction problems during tenant creation. The resulting behaviour should not be changed
		wrapped.setFuture(mainExecutor.scheduleAtFixedRate(wrapped, 0L, checkInterval, timeUnit));
		// schedule to run one check before the next time
		tasks.add(wrapped);
		return wrapped.getFuture();
	}

	/**
	 * On shutdown stops all tasks and executors.
	 */
	@PreDestroy
	void onShutdown() {
		LOGGER.info("Shutting down cron executor threads");

		SchedulerUtil.executeSilently(tasks::destroy);
		SchedulerUtil.supplySilently(mainExecutor::shutdownNow);
	}

	/**
	 * On tenant remove, stop all running tasks.
	 */
	@OnTenantRemove
	void stopTasks() {
		Set<SecureRunnable> runningTasks = SchedulerUtil.supplySilently(tasks::clearContextValue);
		if (runningTasks != null) {
			runningTasks.forEach(SecureRunnable::cancel);
		}
	}
}
