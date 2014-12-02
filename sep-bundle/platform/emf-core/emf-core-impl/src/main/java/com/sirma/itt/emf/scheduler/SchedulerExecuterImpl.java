/*
 *
 */
package com.sirma.itt.emf.scheduler;

import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.scheduler.event.AsyncSchedulerExecuterEvent;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * Default implementation of the {@link SchedulerExecuter}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SchedulerExecuterImpl implements SchedulerExecuter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerExecuterImpl.class);
	/** The dao. */
	@Inject
	private DbDao dao;

	/** The event service. */
	@Inject
	private EventService eventService;

	/** The scheduler service instance. */
	@Inject
	private Instance<SchedulerService> schedulerServiceInstance;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(SchedulerEntry schedulerEntry) {
		if (schedulerEntry == null) {
			throw new EmfRuntimeException("The schedule instance is required argument!");
		}
		SchedulerConfiguration configuration = schedulerEntry.getConfiguration();
		if (configuration == null) {
			throw new EmfRuntimeException("Configuration instance is required!");
		}

		boolean synchronous = configuration.isSynchronous();
		if (!synchronous) {
			// asynchronous actions are executed on another thread
			eventService.fire(new AsyncSchedulerExecuterEvent(schedulerEntry,
					SecurityContextManager.getCurrentSecurityContext()));
			return;
		}

		boolean sameTransaction = configuration.inSameTransaction();
		Boolean result = null;
		SchedulerEntryStatus newStatus = SchedulerEntryStatus.COMPLETED;
		try {
			Callable<Boolean> callable = new InTransactionCallable(schedulerEntry);
			if (sameTransaction) {
				result = dao.invokeInTx(callable);
			} else {
				result = dao.invokeInNewTx(callable);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to execute action in {} transaction due to: {}",
					sameTransaction ? "same" : "new", e.getMessage(), e);
			newStatus = SchedulerEntryStatus.FAILED;
		}
		if (!Boolean.TRUE.equals(result)) {
			// action execution failed
			newStatus = SchedulerEntryStatus.FAILED;
		}
		schedulerEntry.setStatus(newStatus);

		if (configuration.isPersistent()) {
			schedulerServiceInstance.get().save(schedulerEntry);
		}
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public boolean executeImmediate(SchedulerEntry entry) {
		if (entry == null) {
			throw new EmfRuntimeException("Scheduler entry is required argument!");
		}
		return invokeAction(entry) == null;
	}

	/**
	 * Invoke action.
	 * 
	 * @param entry
	 *            the entry
	 * @return the throwable exception object if error occurred during execution
	 */
	protected Throwable invokeAction(SchedulerEntry entry) {
		SchedulerAction action = getAction(entry);
		SchedulerContext context = entry.getContext();
		try {
			// ensure that the context contains the current entry
			context.put(SchedulerContext.SCHEDULER_ENTRY, entry);
			try {
				action.beforeExecute(context);
			} catch (EmfRuntimeException e) {
				LOGGER.error(e.getMessage());
				LOGGER.trace("Exeception on pre execute", e);
				return e;
			} catch (Exception e) {
				LOGGER.error("Failed to pre execute action: " + e.getMessage(), e);
				return e;
			}

			try {
				action.execute(context);
			} catch (EmfRuntimeException e) {
				LOGGER.error(e.getMessage());
				LOGGER.trace("Exeception on execute", e);
				return e;
			} catch (Exception e) {
				LOGGER.error("Failed to execute action: " + e.getMessage(), e);
				return e;
			}

			try {
				action.afterExecute(context);
			} catch (EmfRuntimeException e) {
				LOGGER.error(e.getMessage());
				LOGGER.trace("Exeception on post execute", e);
			} catch (Exception e) {
				LOGGER.error("Failed to post execute action: " + e.getMessage(), e);
			}
		} finally {
			// make sure to remove the entry and not to serialize it with it
			context.remove(SchedulerContext.SCHEDULER_ENTRY);
		}
		return null;
	}

	/**
	 * Gets the action.
	 * 
	 * @param entry
	 *            the entry
	 * @return the action
	 */
	private SchedulerAction getAction(SchedulerEntry entry) {
		SchedulerAction action = entry.getAction();
		if (action == null) {
			throw new EmfRuntimeException("Action instance is required to execute a schedule entry");
		}
		return action;
	}


	/**
	 * Callable class that is used to execute the action in a transaction.
	 *
	 * @author BBonev
	 */
	private final class InTransactionCallable implements Callable<Boolean> {

		/** The entry. */
		private final SchedulerEntry entry;

		/**
		 * Instantiates a new in transaction callable.
		 * 
		 * @param entry
		 *            the entry
		 */
		private InTransactionCallable(SchedulerEntry entry) {
			this.entry = entry;
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		public Boolean call() throws Exception {
			Throwable exception = invokeAction(entry);
			return exception == null;
		}
	}

}
