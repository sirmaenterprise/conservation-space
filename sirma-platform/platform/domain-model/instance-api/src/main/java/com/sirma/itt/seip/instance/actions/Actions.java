package com.sirma.itt.seip.instance.actions;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.state.BeforeOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.time.TimeTracker;

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

	/**
	 * Calls an action identified for the given request. If no action is found then exception will be thrown.
	 *
	 * @param request
	 *            the request
	 * @return the action response
	 */
	@Transactional(TxType.REQUIRED)
	public Object callAction(ActionRequest request) {
		Objects.requireNonNull(request, "Request argument is required");
		Objects.requireNonNull(request.getOperation(), "Operation name is required");

		TimeTracker tracker = TimeTracker.createAndStart();
		Optional<Action<ActionRequest>> optional = actionInstances.get(request.getOperation());
		if (optional.isPresent()) {
			Action<ActionRequest> action = optional.get();
			LOGGER.trace("Executing [{}] action for instance - {}", action.getName(), request.getTargetId());
			// notify for the operation being executed
			BeforeOperationExecutedEvent event = beforeOperation(request);
			// invoke operation
			Object result = action.perform(request);
			// notify for the end
			afterOperation(event);
			LOGGER.debug("Operation {} took {} ms", request.getOperation(), tracker.stop());
			return result;
		}
		throw new UnsupportedOperationException("No operation found for " + request.getOperation());
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
