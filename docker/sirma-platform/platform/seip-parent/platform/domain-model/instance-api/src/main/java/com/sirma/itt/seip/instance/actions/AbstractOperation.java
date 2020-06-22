package com.sirma.itt.seip.instance.actions;

import javax.inject.Inject;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateService;

/**
 * Abstract class for creating operations. The class provides some common methods.
 *
 * @author BBonev
 */
public abstract class AbstractOperation implements InstanceOperation {

	@Inject
	protected EventService eventService;
	@Inject
	protected ServiceRegistry serviceRegistry;
	@Inject
	protected DefinitionService definitionService;
	@Inject
	private StateService stateService;

	@Override
	public boolean isApplicable(Instance instance, Operation operation) {
		return operation != null && getSupportedOperations().contains(operation.getOperation());
	}

	/**
	 * Fire persisted event.
	 *
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operation
	 *            the operation
	 */
	protected void firePersistedEvent(Instance instance, Instance old, Operation operation) {
		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(instance);
		if (eventProvider != null) {
			eventService.fire(eventProvider.createPersistedEvent(instance, old, Operation.getOperationId(operation)));
		}
	}

	/**
	 * Fire change event.
	 *
	 * @param instance
	 *            the instance
	 */
	protected void fireChangeEvent(Instance instance) {
		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(instance);
		if (eventProvider != null) {
			eventService.fire(eventProvider.createChangeEvent(instance));
		}
	}

	/**
	 * Creates the before event.
	 *
	 * @param instance
	 *            the instance
	 * @return the abstract instance two phase event
	 */
	protected AbstractInstanceTwoPhaseEvent<?, ?> createBeforeEvent(Instance instance) {
		// nothing to do by default
		return null;
	}

	/**
	 * Override the method if something should be executed before the operation
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 */
	protected void beforeOperation(Instance instance, Operation operation) {
		// if the operation require something to be executed before
		// the operation it should go here
	}

	/**
	 * Change state.
	 *
	 * @param operation
	 *            the operation
	 * @param instance
	 *            the instance
	 */
	protected void notifyForStateChange(Operation operation, Instance instance) {
		stateService.changeState(instance, operation);
	}

	/**
	 * Gets the definition from the given instance.
	 *
	 * @param <E>
	 *            the definition type
	 * @param instance
	 *            the instance
	 * @return the definition
	 */
	protected <E extends DefinitionModel> E getDefinition(Instance instance) {
		return definitionService.getInstanceDefinition(instance);
	}

	/**
	 * Gets the executed operation from the context
	 *
	 * @param executionContext
	 *            the execution context
	 * @return the executed operation
	 */
	protected Operation getExecutedOperation(Context<String, Object> executionContext) {
		return executionContext.getIfSameType(InstanceOperationProperties.OPERATION, Operation.class);
	}

	/**
	 * Gets the target instance from the context
	 *
	 * @param executionContext
	 *            the execution context
	 * @return the target instance
	 */
	protected Instance getTargetInstance(Context<String, Object> executionContext) {
		return executionContext.getIfSameType(InstanceOperationProperties.INSTANCE, Instance.class);
	}
}
