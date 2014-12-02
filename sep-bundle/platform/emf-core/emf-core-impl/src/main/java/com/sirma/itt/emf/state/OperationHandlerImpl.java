package com.sirma.itt.emf.state;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.state.operation.OperationHandler;
import com.sirma.itt.emf.state.operation.event.OperationExecutedEvent;

/**
 * An observer class that listens for {@link OperationExecutedEvent} to perform a state change if
 * needed.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class OperationHandlerImpl implements OperationHandler {

	@Inject
	private StateService stateService;

	@Override
	public void handleOperation(@Observes OperationExecutedEvent event) {
		stateService.changeState(event.getInstance(), event.getOperation());
	}
}
