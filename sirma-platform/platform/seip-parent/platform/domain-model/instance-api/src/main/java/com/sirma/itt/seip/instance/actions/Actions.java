package com.sirma.itt.seip.instance.actions;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.state.BeforeOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Provides access for execution of {@link Action}s
 *
 * @author BBonev
 */
@ApplicationScoped
public class Actions {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@ExtensionPoint(Action.TARGET_NAME)
	private Plugins<Action<ActionRequest>> actionInstances;

	@Inject
	private InstanceTypeResolver resolver;

	@Inject
	private EventService eventService;

	@Inject
	private LockService lockService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Calls an action identified for the given request. If no action is found then exception will be thrown.
	 *
	 * @param request
	 *            the request
	 * @return the action response
	 */
	@Transactional
	public Object callAction(ActionRequest request) {
		return callSlowAction(request);
	}

	/**
	 * Calls an action identified for the given request. If no action is found then exception will be thrown.
	 * <p>
	 * <b>This is mainly intended for long running actions and the method does not wrap a transaction context when called.</b>
	 * </p>
	 *
	 * @param request
	 *            the request
	 * @return the action response
	 */
	public Object callSlowAction(ActionRequest request) {
		Objects.requireNonNull(request, "Request argument is required");
		Objects.requireNonNull(request.getOperation(), "Operation name is required");

		TimeTracker tracker = TimeTracker.createAndStart();
		Optional<Action<ActionRequest>> optional = actionInstances.get(request.getOperation());
		if (!optional.isPresent()) {
			throw new UnsupportedOperationException("No action found for " + request.getOperation());
		}

		Action<ActionRequest> action = optional.get();
		String actionName = action.getName();
		Serializable targetId = request.getTargetId();
		LOGGER.trace("Validating action {} for instance {}, before execution,", actionName, targetId);
		// check if the action could be executed
		action.validate(request);
		ensureLoadedTargetReference(request);

		lockInstance(request, action);

		try {
			LOGGER.info("Executing {} action for instance {}", actionName, targetId);
			// notify for the operation being executed
			BeforeOperationExecutedEvent event = beforeOperation(request);
			// invoke operation
			Object result = action.perform(request);
			// notify for the end
			afterOperation(event);
			LOGGER.debug("Operation {} took {} ms", request.getOperation(), tracker.stop());
			return result;
		} finally {
			unlockInstance(request, action);
		}
	}

	private void unlockInstance(ActionRequest request, Action<ActionRequest> action) {
		if (request.getTargetReference() == null || !action.shouldLockInstanceBeforeAction(request)) {
			// instance not locked as the action does not need it
			return;
		}
		transactionSupport.invokeInNewTx(() -> {
			if (lockService.tryUnlock(request.getTargetReference()).isLocked()) {
				LOGGER.warn("Instance {} should not be locked after action {}", request.getTargetId(),
						action.getName());
			}
		});
	}

	private void ensureLoadedTargetReference(ActionRequest request) {
		if (request.getTargetId() == null && request.getTargetReference() == null) {
			return;
		}
		if (request.getTargetReference() == null) {
			request.setTargetReference(resolver.resolveReference(request.getTargetId()).orElse(null));
		}
	}

	private void lockInstance(ActionRequest request, Action<ActionRequest> action) {
		if (request.getTargetReference() == null || !action.shouldLockInstanceBeforeAction(request)) {
			return;
		}
		boolean locked = transactionSupport.invokeInNewTx(() -> {
			String lockType = request.getUserOperation() + "-" + securityContext.getRequestId();
			LockInfo lock = lockService.tryLock(request.getTargetReference(), lockType);
			if (isLockedSuccessfully(lockType, lock)) {
				return true;
			}
			for (int i = 0; i < 2; i++) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RollbackedRuntimeException(e);
				}
				lock = lockService.tryLock(request.getTargetReference(), lockType);
				if (isLockedSuccessfully(lockType, lock)) {
					return true;
				}
			}
			return false;
		});

		if (!locked) {
			throw new RollbackedRuntimeException("Could not acquire lock on time to execute requested action. Please try again.");
		}
	}

	private static boolean isLockedSuccessfully(String lockType, LockInfo lock) {
		return lock.isLocked() && lockType.equals(lock.getLockInfo());
	}

	private BeforeOperationExecutedEvent beforeOperation(ActionRequest request) {
		if (request.getTargetId() == null && request.getTargetReference() == null) {
			return null;
		}
		if (request.getTargetReference() == null) {
			request.setTargetReference(resolver.resolveReference(request.getTargetId()).orElse(null));
		}
		if (request.getTargetReference() == null || request.getTargetReference().toInstance() == null) {
			return null;
		}

		BeforeOperationExecutedEvent operationEvent = new BeforeOperationExecutedEvent(
				new Operation(request.getOperation(), request.getUserOperation(), true),
				request.getTargetReference().toInstance());

		eventService.fire(operationEvent);
		return operationEvent;
	}

	private void afterOperation(BeforeOperationExecutedEvent event) {
		eventService.fireNextPhase(event);
	}
}
