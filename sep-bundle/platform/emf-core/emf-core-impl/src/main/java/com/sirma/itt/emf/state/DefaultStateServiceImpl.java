package com.sirma.itt.emf.state;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.state.event.StateChangedEvent;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Default {@link StateService} implementation that acts as a proxy to concrete service extension
 * points.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DefaultStateServiceImpl implements StateService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStateServiceImpl.class);

	/** The services extension points. */
	@Inject
	@ExtensionPoint(StateServiceExtension.TARGET_NAME)
	private Iterable<StateServiceExtension<Instance>> services;

	@Inject
	private EventService eventService;

	@Override
	public <I extends Instance> void changeState(I instance, Operation operation) {
		String oldState = getPrimaryState(instance);
		boolean changeState = getSupportedServiceOrError(instance).changeState(instance, operation);
		if (changeState) {
			String newState = getPrimaryState(instance);
			// REVIEW: we could add a method in extension point-a that return the concrete event
			// implementation and to fire it instead of one event type

			// fire event for state change
			StateChangedEvent event = new StateChangedEvent(oldState,
					operation != null ? operation.getOperation() : null, newState, instance);
			LOGGER.debug("Detected state change: " + event);
			eventService.fire(event);
		}
	}

	@Override
	public <I extends Instance> String getPrimaryState(I instance) {
		return getSupportedServiceOrError(instance).getPrimaryState(instance);
	}

	@Override
	public <I extends Instance> String getState(PrimaryStateType stateType, Class<I> target) {
		return getSupportedServiceOrError(target).getState(stateType);
	}

	@Override
	public <I extends Instance> boolean isState(PrimaryStateType stateType, Class<I> target,
			String state) {
		return getSupportedServiceOrError(target).isState(stateType, state);
	}

	@Override
	public <I extends Instance> boolean isStateAs(Class<I> target, String state,
			PrimaryStateType... stateType) {
		if (stateType == null) {
			return false;
		}
		StateServiceExtension<Instance> extension = getSupportedServiceOrError(target);
		for (PrimaryStateType primaryStateType : stateType) {
			if (extension.isState(primaryStateType, state)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <I extends Instance> boolean isInState(PrimaryStateType stateType, I instance) {
		return getSupportedServiceOrError(instance).isInState(stateType, instance);
	}

	@Override
	public <I extends Instance> boolean isInStates(I instance, PrimaryStateType... stateTypes) {
		if (stateTypes == null) {
			return false;
		}
		StateServiceExtension<Instance> extension = getSupportedServiceOrError(instance);
		String state = getPrimaryState(instance);
		for (PrimaryStateType primaryStateType : stateTypes) {
			if (extension.isState(primaryStateType, state)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <I extends Instance> int getPrimaryStateCodelist(Class<I> target) {
		return getSupportedServiceOrError(target).getPrimaryStateCodelist();
	}

	@Override
	public <I extends Instance> Integer getSecondaryStateCodelist(Class<I> target) {
		return getSupportedServiceOrError(target).getSecondaryStateCodelist();
	}

	/**
	 * Gets the supported service. If not found error is thrown
	 * 
	 * @param instance
	 *            the instance
	 * @return the supported service
	 */
	protected StateServiceExtension<Instance> getSupportedServiceOrError(Object instance) {
		StateServiceExtension<Instance> stateService = getSupportedService(instance);
		if (stateService == null) {
			throw new EmfRuntimeException("Failed to find a valid " + StateServiceExtension.class
					+ " implementation that supports instances of type " + instance.getClass());
		}
		return stateService;
	}

	/**
	 * Gets the supported service.
	 * 
	 * @param instance
	 *            the instance
	 * @return the supported service
	 */
	protected StateServiceExtension<Instance> getSupportedService(Object instance) {
		for (StateServiceExtension<Instance> service : services) {
			if (service.canHandle(instance)) {
				return service;
			}
		}
		return null;
	}

}
