package com.sirma.itt.seip.instance.state;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_ACTIVE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Default {@link StateService} implementation that acts as a proxy to concrete service extension points.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefaultStateServiceImpl implements StateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStateServiceImpl.class);

	@Inject
	@ExtensionPoint(StateServiceExtension.TARGET_NAME)
	private Plugins<StateServiceExtension> services;

	@Inject
	private EventService eventService;

	@Override
	public <I extends Instance> void changeState(I instance, Operation operation) {
		String oldState = getPrimaryState(instance);
		boolean isActive = isInActiveState(instance);
		boolean changeState = getSupportedServiceOrError(instance.type().getCategory()).changeState(instance,
				operation);
		if (changeState) {
			String newState = getPrimaryState(instance);
			// fire event for state change
			StateChangedEvent event = new StateChangedEvent(oldState, operation, newState,
					instance);
			LOGGER.debug("Detected state change: {}", event);
			eventService.fire(event);

			boolean isActiveAfterChange = isInActiveState(instance);

			LOGGER.debug("{} with id = {} in state {} -> isActive = {}", instance.getClass().getSimpleName(),
					instance.getId(), newState, isActive);
			instance.add(IS_ACTIVE, isActiveAfterChange);

			// fire proper event if there is a transition of states
			if (isActive && !isActiveAfterChange) {
				eventService.fire(new InstanceDeactivatedEvent(instance));
			} else if (!isActive && isActiveAfterChange) {
				eventService.fire(new InstanceActivatedEvent(instance));
			}
		}
	}

	@Override
	public <I extends Instance> String getPrimaryState(I instance) {
		return getSupportedServiceOrError(instance.type().getCategory()).getPrimaryState(instance);
	}

	@Override
	public String getState(PrimaryStates stateType, String targetType) {
		return getSupportedServiceOrError(targetType).getState(stateType);
	}

	@Override
	public boolean isState(PrimaryStates stateType, String targetType, String state) {
		return getSupportedServiceOrError(targetType).isState(stateType, state);
	}

	@Override
	public boolean isStateAs(String targetType, String state, PrimaryStates... stateType) {
		if (stateType == null) {
			return false;
		}
		StateServiceExtension extension = getSupportedServiceOrError(targetType);
		for (PrimaryStates primaryStates : stateType) {
			if (extension.isState(primaryStates, state)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <I extends Instance> boolean isInState(PrimaryStates stateType, I instance) {
		return getSupportedServiceOrError(instance.type().getCategory()).isInState(stateType, instance);
	}

	@Override
	public <I extends Instance> boolean isInStates(I instance, PrimaryStates... stateTypes) {
		if (stateTypes == null) {
			return false;
		}
		StateServiceExtension extension = getSupportedServiceOrError(instance.type().getCategory());
		String state = getPrimaryState(instance);
		for (PrimaryStates primaryStates : stateTypes) {
			if (extension.isState(primaryStates, state)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <I extends Instance> boolean isInActiveState(I instance) {
		return getSupportedServiceOrError(instance.type().getCategory()).isInActiveState(instance);
	}

	@Override
	public int getPrimaryStateCodelist(String targetType) {
		return getSupportedServiceOrError(targetType).getPrimaryStateCodelist();
	}

	@Override
	public Integer getSecondaryStateCodelist(String targetType) {
		return getSupportedServiceOrError(targetType).getSecondaryStateCodelist();
	}

	/**
	 * Gets the supported service. If not found error is thrown
	 *
	 * @param type
	 *            the instance type
	 * @return the supported service
	 */
	private StateServiceExtension getSupportedServiceOrError(String type) {
		StateServiceExtension stateService = getSupportedService(type);
		if (stateService == null) {
			throw new EmfRuntimeException("Failed to find a valid " + StateServiceExtension.class
					+ " implementation that supports instances of type " + type);
		}
		return stateService;
	}

	/**
	 * Gets the supported service.
	 *
	 * @param type
	 *            the type
	 * @return the supported service
	 */
	private StateServiceExtension getSupportedService(String type) {
		for (StateServiceExtension service : services) {
			if (service.canHandle(type)) {
				return service;
			}
		}
		return null;
	}

}
