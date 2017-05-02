package com.sirma.itt.emf.web.action.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.security.action.ActionTypeBinding;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.actions.OperationInvoker;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Observer that monitors for immediate actions to perform the action. The observer will fire an event of type
 * {@link EmfImmediateActionEvent} for specific implementation of the event if possible.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ImmediateActionObserver {

	@Inject
	private InstanceService instanceService;

	@Inject
	private EventService eventService;

	@Inject
	private OperationInvoker operationInvoker;

	/**
	 * Observer method that handles all immediate actions.
	 *
	 * @param event
	 *            the event
	 */
	public void onUserOperation(@Observes EMFActionEvent event) {
		if (event.getInstance() == null) {
			return;
		}
		Action action = event.getAction();
		if (action != null && action.isImmediateAction()) {
			EmfImmediateActionEvent immediateEvent = new EmfImmediateActionEvent(event.getInstance(),
					event.getNavigation(), event.getActionId());

			ActionTypeBinding binding = new ActionTypeBinding(event.getActionId(), event.getInstance().getClass());
			eventService.fire(immediateEvent, binding);

			if (!immediateEvent.isHandled()) {
				// all actions with the default purpose are executed the same way
				if (DefinitionUtil.isStandardAction(action)) {
					// perform default operation
					instanceService.save(event.getInstance(), new Operation(event.getActionId(), true));
				} else {
					// all actions that have a specific purpose will invoke an action for that
					// purpose
					Context<String, Object> context = operationInvoker.createDefaultContext(event.getInstance(),
							new Operation(action.getPurpose(), event.getActionId(), true));
					operationInvoker.invokeOperation(context);
				}
			}
		}
	}
}