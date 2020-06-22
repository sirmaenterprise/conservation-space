package com.sirma.itt.seip.instance.actions;

import javax.inject.Inject;

import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.state.AfterOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.BeforeOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Provides default implementation for action execution. Notifies any listeners before and after action execution.
 *
 * @author A. Kunchev
 */
abstract class ActionExecutor {

	@Inject
	private EventService eventService;

	/**
	 * Executes given action. Additionally notifies the listeners before and after action execution via specific events.
	 *
	 * @param action that should be executed
	 * @param request contains the required data for action execution
	 * @return the result from the action execution
	 * @see BeforeOperationExecutedEvent
	 * @see AfterOperationExecutedEvent
	 */
	Object execute(Action<ActionRequest> action, ActionRequest request) {
		// notify for the operation being executed
		BeforeOperationExecutedEvent event = beforeOperation(request);
		// invoke operation
		Object result = action.perform(request);
		// notify for the end
		afterOperation(event);
		return result;
	}

	private BeforeOperationExecutedEvent beforeOperation(ActionRequest request) {
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