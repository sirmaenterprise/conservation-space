package com.sirma.sep.instance.operation;

import java.util.Collections;
import java.util.Set;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.actions.AbstractOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Base implementation for delete operation.
 *
 * @author BBonev
 */
@Extension(target = InstanceOperation.TARGET_NAME, order = 99999)
public class BaseDeleteOperation extends AbstractOperation {

	private static final Set<String> SUPPORTED_OPERATIONS = Collections.singleton(ActionTypeConstants.DELETE);

	@Override
	public Set<String> getSupportedOperations() {
		return SUPPORTED_OPERATIONS;
	}

	@Override
	public Object execute(Context<String, Object> executionContext) {
		Instance instance = getTargetInstance(executionContext);
		Operation operation = getExecutedOperation(executionContext);
		delete(instance, operation);
		return null;
	}

	private void delete(Instance instance, Operation operation) {
		if (operation != null) {
			Options.CURRENT_OPERATION.set(operation);
		}

		try {
			instance.markAsDeleted();
			AbstractInstanceTwoPhaseEvent<?, ?> event = createBeforeEvent(instance);

			// first notify to update statutes
			notifyForStateChange(operation, instance);

			// if the operation require something to be executed before the operation
			// it should go here
			beforeOperation(instance, operation);

			// fire next part of the event
			eventService.fireNextPhase(event);
		} finally {
			if (operation != null) {
				Options.CURRENT_OPERATION.clear();
			}
		}
	}

	@Override
	protected AbstractInstanceTwoPhaseEvent<?, ?> createBeforeEvent(Instance instance) {
		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(instance);
		if (eventProvider == null) {
			return null;
		}

		AbstractInstanceTwoPhaseEvent<?, ?> event = eventProvider.createBeforeInstanceDeleteEvent(instance);
		eventService.fire(event);
		return event;
	}
}