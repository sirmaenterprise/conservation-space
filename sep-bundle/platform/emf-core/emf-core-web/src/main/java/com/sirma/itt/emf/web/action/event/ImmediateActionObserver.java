package com.sirma.itt.emf.web.action.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.action.ActionTypeBinding;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Observer that monitors for immediate actions to perform the action. The observer will fire an
 * event of type {@link EmfImmediateActionEvent} for specific implementation of the event if
 * possible.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class ImmediateActionObserver {

	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	@Inject
	private EventService eventService;

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
		if ((action != null) && action.isImmediateAction()) {
			EmfImmediateActionEvent immediateEvent = new EmfImmediateActionEvent(
					event.getInstance(), event.getNavigation(), event.getActionId());

			ActionTypeBinding binding = new ActionTypeBinding(event.getActionId(), event
					.getInstance().getClass());
			eventService.fire(immediateEvent, binding);

			if (!immediateEvent.isHandled()) {
				Operation operation = new Operation(event.getActionId());
				// perform default operation
				instanceService.save(event.getInstance(), operation);
			}
		}
	}

}
