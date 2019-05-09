package com.sirma.itt.seip.instance.actions;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Provides access for execution of {@link Action}s.
 * <p>
 * <b>Note that transactions are managed internally.</b> If the methods of this class are called inside transaction
 * context, exception will be thrown. Check {@link Transactional}({@link TxType#NEVER}).
 * </p>
 * <p>
 * The initialization of the transaction context depends on the method call, where {@link #callAction(ActionRequest)}
 * will start new transaction, where the action will be executed and {@link #callSlowAction(ActionRequest)} will not
 * start transaction.
 * </p>
 * The reason for internal transaction management is basically to ensure that the data is processed and committed,
 * before returning the results from the action execution and unlocking the resources, if the action requires locking.
 *
 * @author BBonev
 * @author A. Kunchev
 * @see TransactionalActionExecutor
 * @see NonTransactionalActionExecutor
 */
@Singleton
public class Actions {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(Action.TARGET_NAME)
	private Plugins<Action<ActionRequest>> actionInstances;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private LockService lockService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private TransactionManager transactionManager;

	@Inject
	private TransactionalActionExecutor transactionalActionExecutor;

	@Inject
	private NonTransactionalActionExecutor nonTransactionalActionExecutor;

	/**
	 * Calls an action identified for the given request. If no action is found then exception will be thrown. <p>
	 * The action is executed in transaction, which is managed internally. <br>
	 * <b>Note that this is method should not be invoked inside transaction context.</b>
	 *
	 * @param request that contains the data required for successful action execution
	 * @return the action response
	 */
	@Monitored({
		@MetricDefinition(name="action_duration_seconds",type=Type.TIMER,descr="Action execution duration in seconds."),
		@MetricDefinition(name="action_hit_count",type=Type.COUNTER,descr="Hit counter on the action service method.")
	})
	public Object callAction(ActionRequest request) {
		return callActionInternal(request, transactionalActionExecutor);
	}

	/**
	 * Calls an action identified for the given request. If no action is found then exception will be thrown.
	 * <p>
	 * <b>This is mainly intended for long running actions and the method does not wrap a transaction context when
	 * called.</b>
	 * </p>
	 *
	 * @param request that contains the data required for successful action execution
	 * @return the action response
	 */
	@Monitored({
		@MetricDefinition(name = "action_slow_duration_seconds", type = Type.TIMER, descr = "Slow action execution duration in seconds."),
		@MetricDefinition(name = "action_slow_hit_count", type = Type.COUNTER, descr = "Hit counter on the slow action service method.")
	})
	public Object callSlowAction(ActionRequest request) {
		return callActionInternal(request, nonTransactionalActionExecutor);
	}

	private Object callActionInternal(ActionRequest request, ActionExecutor executor) {
		requireNonNull(request, "Request argument is required!");
		String operationId = requireNonNull(request.getOperation(), "Operation name is required!");
		String targetId = requireNonNull(Objects.toString(request.getTargetId(), null), "Target id is required!");

		TimeTracker tracker = TimeTracker.createAndStart();
		Action<ActionRequest> action = actionInstances.get(operationId).orElseThrow(
				() -> new UnsupportedOperationException("No action found for " + operationId));

		validateTransactionalContext(request, action);

		ensureLoadedTargetReference(request);

		LOGGER.trace("Request: {}", request);
		action.validate(request);

		lockInstance(request, action);

		try {
			String actionName = action.getName();
			LOGGER.info("Executing {} action for instance {}", actionName, targetId);
			Object result = executor.execute(action, request);
			LOGGER.debug("Action {} execution took {} ms.", actionName, tracker.stop());
			return result;
		} finally {
			unlockInstance(request, action);
		}
	}

	private void validateTransactionalContext(ActionRequest request, Action<ActionRequest> action) {
		try {
			if (action.shouldLockInstanceBeforeAction(request) && transactionManager.getTransaction() != null) {
				throw new EmfRuntimeException("Invalid transaction. Action [" + action.getName()
						+ "] shouldn't be invoked inside transaction context.");
			}
		} catch (SystemException e) {
			throw new EmfRuntimeException(e);
		}
	}

	private void ensureLoadedTargetReference(ActionRequest request) {
		if (request.getTargetReference() == null) {
			Instance instance = domainInstanceService.loadInstance(request.getTargetId().toString());
			request.setTargetReference(instance.toReference());
		}
	}

	private void lockInstance(ActionRequest request, Action<ActionRequest> action) {
		if (!action.shouldLockInstanceBeforeAction(request)) {
			return;
		}
		boolean locked = transactionSupport.invokeInTx(() -> {
			String lockType = request.getUserOperation() + "-" + securityContext.getRequestId();
			LockInfo lock = lockService.tryLock(request.getTargetReference(), lockType);
			if (isLockedSuccessfully(lock, lockType)) {
				return true;
			}
			for (int i = 0; i < 2; i++) {
				lock = retryAfterDelay(request, lockType);
				if (isLockedSuccessfully(lock, lockType)) {
					return true;
				}
			}
			return false;
		});

		if (!locked) {
			throw new RollbackedRuntimeException(
					"Could not acquire lock on time to execute requested action. Please try again.");
		}
	}

	private LockInfo retryAfterDelay(ActionRequest request, String lockType) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RollbackedRuntimeException(e);
		}
		return lockService.tryLock(request.getTargetReference(), lockType);
	}

	private static boolean isLockedSuccessfully(LockInfo lock, String lockType) {
		return lock.isLocked() && lockType.equals(lock.getLockInfo());
	}

	private void unlockInstance(ActionRequest request, Action<ActionRequest> action) {
		if (!action.shouldLockInstanceBeforeAction(request)) {
			return;
		}
		transactionSupport.invokeInNewTx(() -> {
			if (lockService.tryUnlock(request.getTargetReference()).isLocked()) {
				LOGGER.warn("Instance {} shouldn't be locked after action {}", request.getTargetId(), action.getName());
			}
		});
	}
}