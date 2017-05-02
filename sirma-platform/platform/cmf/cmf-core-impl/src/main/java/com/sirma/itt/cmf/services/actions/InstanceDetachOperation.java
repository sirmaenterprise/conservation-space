package com.sirma.itt.cmf.services.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.actions.AbstractOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default implementation for detach operation
 *
 * @author BBonev
 */
@Extension(target = InstanceOperation.TARGET_NAME, order = 99970)
public class InstanceDetachOperation extends AbstractOperation {

	private static final Set<String> SUPPORTED_OPERATIONS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(ActionTypeConstants.DETACH,
					ActionTypeConstants.DETACH_DOCUMENT, ActionTypeConstants.DETACH_OBJECT)));

	@Inject
	protected LinkService linkService;

	@Override
	public Set<String> getSupportedOperations() {
		return SUPPORTED_OPERATIONS;
	}

	@Override
	public Object execute(Context<String, Object> executionContext) {
		Instance instance = executionContext.getIfSameType(InstanceOperationProperties.INSTANCE, Instance.class);
		Operation operation = executionContext.getIfSameType(InstanceOperationProperties.OPERATION, Operation.class);
		Instance[] instances = executionContext.getIfSameType(InstanceOperationProperties.INSTANCE_ARRAY,
				Instance[].class);
		detach(instance, operation, instances);
		return null;
	}

	/**
	 * Performs the detach operation
	 *
	 * @param targetInstance
	 *            the target instance
	 * @param operation
	 *            the operation
	 * @param children
	 *            the children
	 */
	public void detach(Instance targetInstance, Operation operation, Instance... children) {

		beforeOperation(targetInstance, operation);

		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(targetInstance);
		for (Instance instance : children) {
			InstanceDetachedEvent<Instance> event = eventProvider.createDetachEvent(targetInstance, instance);
			event.setOperationId(Operation.getUserOperationId(operation));
			eventService.fire(event);
			afterDetach(targetInstance, operation, instance);
		}
	}

	/**
	 * Method called after the instance has been detached. Override method to provide additional functions.
	 *
	 * @param instance
	 *            the instance to attach to
	 * @param operation
	 *            the operation
	 * @param newChild
	 *            the new child to attach
	 */
	protected void afterDetach(Instance instance, Operation operation, Instance newChild) {
		linkService.unlinkSimple(instance.toReference(), newChild.toReference(), LinkConstants.HAS_ATTACHMENT,
				LinkConstants.IS_ATTACHED_TO);

		linkService.unlinkSimple(newChild.toReference(), instance.toReference(), LinkConstants.PART_OF_URI,
				LinkConstants.HAS_CHILD_URI);
		linkService.unlink(instance.toReference(), newChild.toReference(), LinkConstants.HAS_CHILD_URI,
				LinkConstants.PART_OF_URI);
	}

}
