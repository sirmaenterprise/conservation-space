package com.sirma.itt.seip.tasks;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.CompletableOperation;
import com.sirma.itt.seip.concurrent.SimpleFuture;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Default implementation of the {@link SchedulerExecuter}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SchedulerExecuterImpl implements SchedulerExecuter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerExecuterImpl.class);

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private EventService eventService;

	@Inject
	private Instance<SchedulerService> schedulerServiceInstance;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private UserStore userStore;

	@Override
	public Future<SchedulerEntryStatus> execute(SchedulerEntry schedulerEntry, boolean allowAsync,
			boolean allowPersist) {
		SchedulerConfiguration configuration = getConfiguration(schedulerEntry);

		if (allowAsync && !configuration.isSynchronous()) {
			return runAsynchronously(schedulerEntry, allowPersist);
		}

		TransactionMode transactionMode = configuration.getTransactionMode();
		SchedulerEntryStatus newStatus;
		try {
			newStatus = runSynchronously(schedulerEntry, transactionMode);
		} catch (Exception e) {
			LOGGER.error("Failed to execute action {} in transaction with {} mode due to: {}",
				     schedulerEntry.getIdentifier(),transactionMode, e.getMessage(), e);
			newStatus = SchedulerEntryStatus.FAILED;
		}

		finalizeExecution(schedulerEntry, newStatus, allowPersist);

		return new SimpleFuture<>(newStatus);
	}

	private void finalizeExecution(SchedulerEntry schedulerEntry, SchedulerEntryStatus status, boolean allowPersist) {
		if (allowPersist && getConfiguration(schedulerEntry).isPersistent()) {
			schedulerEntry.setStatus(status);
			schedulerServiceInstance.get().save(schedulerEntry);
		}
	}

	private static SchedulerConfiguration getConfiguration(SchedulerEntry schedulerEntry) {
		if (schedulerEntry == null) {
			throw new EmfRuntimeException("The schedule instance is required argument!");
		}
		SchedulerConfiguration configuration = schedulerEntry.getConfiguration();
		if (configuration == null) {
			throw new EmfRuntimeException("Configuration instance is required!");
		}
		return configuration;
	}

	@SuppressWarnings("boxing")
	private SchedulerEntryStatus runSynchronously(SchedulerEntry schedulerEntry, TransactionMode transactionMode) {
		Supplier<Boolean> toExecute;
		switch (transactionMode) {
			case REQUIRED:
				toExecute = () -> transactionSupport.invokeFunctionInTx(this::executeImmediate, schedulerEntry);
				break;
			case REQUIRES_NEW:
				toExecute = () -> transactionSupport.invokeFunctionInNewTx(this::executeImmediate, schedulerEntry);
				break;
			case NOT_SUPPORTED:
			default:
				toExecute = () -> executeImmediate(schedulerEntry);
		}
		ExecuteAsBinding filter = getFilter(schedulerEntry);
		Response response = new Response();
		RunAs runAs = schedulerEntry.getConfiguration().getRunAs();
		String runUserId = schedulerEntry.getConfiguration().getRunUserId();
		if (runAs == RunAs.SYSTEM) {
			SchedulerExecuterEvent event = new SchedulerExecuterEvent(toExecute, response);
			fireEvent(event, filter);
		} else if (runAs == RunAs.USER && runUserId != null) {
			SchedulerExecuterEvent event = new SchedulerExecuterEvent(toExecute, response);
			User user = userStore.loadBySystemId(runUserId);
			if (user == null) {
				LOGGER.warn("Requested custom user execution but user {} was not found. Executing as default", runUserId);
				fireEvent(event, filter);
			} else {
				LOGGER.debug("Performing custom execution for user {}({}) of action {} with id {}",
						user.getDisplayName(), runUserId, schedulerEntry.getActionName(),
						schedulerEntry.getIdentifier());
				securityContextManager.executeAsUser(user).biConsumer(this::fireEvent, event, filter);
			}
		} else {
			SchedulerAllTenantsExecuterEvent event = new SchedulerAllTenantsExecuterEvent(toExecute, response);
			fireEvent(event, filter);
		}
		return response.isSuccessful() ? SchedulerEntryStatus.COMPLETED : SchedulerEntryStatus.FAILED;
	}

	private Future<SchedulerEntryStatus> runAsynchronously(SchedulerEntry schedulerEntry, boolean allowPersist) {
		ExecuteAsBinding filter = getFilter(schedulerEntry);
		Supplier<SchedulerEntryStatus> toExecute = () -> executeAsyncInternal(schedulerEntry, allowPersist);
		SimpleFuture<SchedulerEntryStatus> future = new SimpleFuture<>();
		if (schedulerEntry.getConfiguration().getTransactionMode() == TransactionMode.NOT_SUPPORTED) {
			// asynchronous actions are executed on another thread
			fireEvent(new AsyncNoTxSchedulerExecuterEvent(toExecute, future), filter);
		} else {
			// asynchronous actions are executed on another thread
			fireEvent(new AsyncSchedulerExecuterEvent(toExecute, future), filter);
		}
		return future;
	}

	private void fireEvent(EmfEvent event, Annotation annotation) {
		eventService.fire(event, annotation);
	}

	private static ExecuteAsBinding getFilter(SchedulerEntry schedulerEntry) {
		RunAs runAs = schedulerEntry.getConfiguration().getRunAs();
		if (runAs == RunAs.USER) {
			runAs = RunAs.DEFAULT;
		}
		return new ExecuteAsBinding(runAs);
	}

	private SchedulerEntryStatus executeAsyncInternal(SchedulerEntry entry, boolean allowPersist) {
		SchedulerEntryStatus status = SchedulerEntryStatus.COMPLETED;
		if (!executeImmediate(entry)) {
			status = SchedulerEntryStatus.FAILED;
		}

		finalizeExecution(entry, status, allowPersist);
		return status;
	}

	@Override
	public boolean executeImmediate(SchedulerEntry entry) {
		if (entry == null) {
			throw new EmfRuntimeException("Scheduler entry is required!");
		}
		if (entry.getAction() == null) {
			throw new EmfRuntimeException("Missing required action for scheduler entry: " + entry.getIdentifier());
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
	@SuppressWarnings("static-method")
	protected Throwable invokeAction(SchedulerEntry entry) { // NOSONAR
		SchedulerAction action = getAction(entry);
		SchedulerContext context = entry.getContext();
		LOGGER.trace("Executing entry id={}, context={}", entry.getId(), entry.getContext());
		try {
			// ensure that the context contains the current entry
			context.put(SchedulerContext.SCHEDULER_ENTRY, entry);
			try {
				action.beforeExecute(context);
			} catch (EmfRuntimeException e) {
				LOGGER.error("Exception during pre execute.", e);
				return e;
			} catch (Exception e) {
				LOGGER.error("Generic failure during pre execute. ", e);
				return e;
			}

			try {
				action.execute(context);
			} catch (SchedulerRetryException e) { // NOSONAR
				LOGGER.warn("Action {} failed with {}. Will try again later", entry.getIdentifier(), e.getMessage());
				return e;
			} catch (EmfRuntimeException e) {
				LOGGER.error("Exception during execute.", e);
				return e;
			} catch (Exception e) {
				LOGGER.error("Generic failure during action execute.", e);
				return e;
			}

			try {
				action.afterExecute(context);
			} catch (EmfRuntimeException e) {
				LOGGER.error("Exception during post execute.", e);
			} catch (Exception e) {
				LOGGER.error("Generic failure during post execute. ", e);
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
	private static SchedulerAction getAction(SchedulerEntry entry) {
		SchedulerAction action = entry.getAction();
		if (action == null) {
			throw new EmfRuntimeException("Action instance is required to execute a schedule entry");
		}
		return action;
	}

	/**
	 * Binding for {@link ExecuteAs}
	 *
	 * @author BBonev
	 */
	private static final class ExecuteAsBinding extends AnnotationLiteral<ExecuteAs>implements ExecuteAs {
		private static final long serialVersionUID = 3623405173758779840L;
		private final RunAs value;

		/**
		 * Instantiates a new execute as binding.
		 *
		 * @param value
		 *            the value
		 */
		public ExecuteAsBinding(RunAs value) {
			this.value = value;
		}

		@Override
		public RunAs value() {
			return value;
		}
	}

	/**
	 * The Class Response.
	 *
	 * @author BBonev
	 */
	private static class Response implements CompletableOperation<Object> {

		private List<Boolean> result = new LinkedList<>();
		private Collection<Exception> exception = new LinkedList<>();

		@Override
		public boolean completed(Object arg) {
			result.add(Boolean.valueOf(String.valueOf(arg)));
			return true;
		}

		/**
		 * Checks if is successful.
		 *
		 * @return true, if is successful
		 */
		public boolean isSuccessful() {
			if (!exception.isEmpty() || result.isEmpty()) {
				return false;
			}
			if (result.size() == 1) {
				return result.iterator().next().booleanValue();
			}
			return result.stream().allMatch(Boolean.TRUE::equals);
		}

		@Override
		public boolean failed(Exception exc) {
			exception.add(exc);
			return true;
		}

		@Override
		public boolean cancel() {
			return true;
		}

	}

}
