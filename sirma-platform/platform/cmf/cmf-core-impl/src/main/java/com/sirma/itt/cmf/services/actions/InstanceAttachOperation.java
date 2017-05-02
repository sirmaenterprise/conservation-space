package com.sirma.itt.cmf.services.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.actions.AbstractOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.MethodNotAllowedException;

/**
 * Default implementation for attach operation
 *
 * @author BBonev
 */
@Extension(target = InstanceOperation.TARGET_NAME, order = 99980)
public class InstanceAttachOperation extends AbstractOperation {

	private static final Set<String> SUPPORTED_OPERATIONS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(ActionTypeConstants.ATTACH, ActionTypeConstants.ATTACH_OBJECT,
					ActionTypeConstants.ATTACH_DOCUMENT, ActionTypeConstants.ADD_LIBRARY)));

	@Inject
	private InstanceContextInitializer instanceContextInitializer;

	@Override
	public Set<String> getSupportedOperations() {
		return SUPPORTED_OPERATIONS;
	}

	@Inject
	protected LinkService linkService;

	@Inject
	protected InstanceService instanceService;

	@Override
	public Object execute(Context<String, Object> executionContext) {
		Instance instance = getTargetInstance(executionContext);
		Operation operation = getExecutedOperation(executionContext);

		Instance[] instances = executionContext.getIfSameType(InstanceOperationProperties.INSTANCE_ARRAY,
				Instance[].class);
		attach(instance, operation, instances);
		return null;
	}

	/**
	 * Performs the attach operation
	 *
	 * @param targetInstance
	 *            the target instance
	 * @param operation
	 *            the operation
	 * @param children
	 *            the children
	 */
	public void attach(Instance targetInstance, Operation operation, Instance... children) {

		beforeOperation(targetInstance, operation);

		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(targetInstance);
		for (Instance child : children) {
			if (!isAttachAllowed(targetInstance, child)) {
				throw new MethodNotAllowedException("The instance " + child.getId() + " couldn't be attached to "
						+ targetInstance.getId() + " because that will create a circular dependency.");
			}
			InstanceAttachedEvent<Instance> event = eventProvider.createAttachEvent(targetInstance, child);
			event.setOperationId(Operation.getUserOperationId(operation));
			onAfterAttach(targetInstance, operation, child);
			eventService.fire(event);
			OwnedModel.setOwnedModel(child, targetInstance);
		}
	}

	/**
	 * Determine if the child can be attached to it's new target instance. If the target has a parent that has the same
	 * id as the child that is to be attached, it can't be attached because it will create a circular relation
	 * dependency.
	 *
	 * @param target
	 *            the target instance to which the child instance is going to be attached
	 * @param child
	 *            the child instance that is going to be attached to the target instance
	 * @return true if it can be attached, false otherwise
	 */
	private boolean isAttachAllowed(Instance target, Instance child) {
		instanceContextInitializer.restoreHierarchy(target);
		InstanceReference current = target.toReference();
		while (current.getParent() != null) {
			current = current.getParent();
			if (child.getId().equals(current.getIdentifier())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method called after the instance has been attached. Override method to provide additional functions.
	 *
	 * @param instance
	 *            the instance to attach to
	 * @param operation
	 *            the operation
	 * @param newChild
	 *            the new child to attach
	 */
	protected void onAfterAttach(Instance instance, Operation operation, Instance newChild) {
		InstanceReference parentRef = instance.toReference();
		InstanceReference childRef = newChild.toReference();

		// Recreate partOf relation after move operation
		if (Operation.isUserOperationAs(operation, ActionTypeConstants.MOVE)) {
			OwnedModel ownedModel = (OwnedModel) newChild;
			ownedModel.setOwningInstance(instance);
			ownedModel.setOwningReference(instance.toReference());
			instanceService.save(newChild, operation);
		}

		if (shouldCreateAttachLink(operation)) {
			linkService.linkSimple(parentRef, childRef, LinkConstants.HAS_ATTACHMENT, LinkConstants.IS_ATTACHED_TO);
		}

		linkService.linkSimple(childRef, parentRef, LinkConstants.PART_OF_URI);
	}

	/**
	 * Should create attach link.
	 *
	 * @param operation
	 *            the operation
	 * @return true, if successful
	 */
	protected boolean shouldCreateAttachLink(Operation operation) {
		return Operation.isUserOperationAs(operation, ActionTypeConstants.ATTACH_DOCUMENT,
				ActionTypeConstants.ATTACH_OBJECT, ActionTypeConstants.ADD_LIBRARY);
	}
}
