package com.sirma.itt.seip.instance.state;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * An observer class that listens for {@link OperationExecutedEvent} to perform a state change if needed.
 *
 * @author BBonev
 */
@ApplicationScoped
public class OperationHandlerImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private StateService stateService;

	/**
	 * Handle event operation.
	 *
	 * @param event
	 *            the event
	 */
	public void handleOperation(@Observes OperationExecutedEvent event) {
		stateService.changeState(event.getInstance(), event.getOperation());
		String state = stateService.getPrimaryState(event.getInstance());
		Boolean isActive = stateService.isInActiveState(event.getInstance());
		LOGGER.debug("{} with id = {} in state {} -> isActive = {}", event.getInstance().getClass().getSimpleName(),
				event.getInstance().getId(), state, isActive);
		event.getInstance().getProperties().put(DefaultProperties.IS_ACTIVE, isActive);
	}
}
