package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceCancelEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.event.instance.BeforeInstanceCancelEvent;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.instance.dao.BaseInstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Event provider for {@link WorkflowInstanceContext}.
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypesCmf.WORKFLOW)
public class WorkflowEventProvider extends BaseInstanceEventProvider<WorkflowInstanceContext> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceCreateEvent<WorkflowInstanceContext> createCreateEvent(
			WorkflowInstanceContext instance) {
		return new WorkflowCreateEvent(instance, ActionTypeConstants.START_WORKFLOW);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstancePersistedEvent<WorkflowInstanceContext> createPersistedEvent(
			WorkflowInstanceContext instance, WorkflowInstanceContext old, String operationId) {
		return new WorkflowPersistedEvent(instance, old, operationId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceChangeEvent<WorkflowInstanceContext> createChangeEvent(
			WorkflowInstanceContext instance) {
		return new WorkflowChangeEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceOpenEvent<WorkflowInstanceContext> createOpenEvent(
			WorkflowInstanceContext instance) {
		return new WorkflowOpenEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstancePersistEvent<WorkflowInstanceContext, ? extends AfterInstancePersistEvent<WorkflowInstanceContext, TwoPhaseEvent>> createBeforeInstancePersistEvent(
			WorkflowInstanceContext instance) {
		return new BeforeWorkflowPersistEvent(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceAttachedEvent<WorkflowInstanceContext> createAttachEvent(
			WorkflowInstanceContext instance, Instance child) {
		return new AttachedChildToWorkflowEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceDetachedEvent<WorkflowInstanceContext> createDetachEvent(
			WorkflowInstanceContext instance, Instance child) {
		return new DetachedChildToWorkflowEvent(instance, child);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceDeleteEvent<WorkflowInstanceContext, ? extends AfterInstanceDeleteEvent<WorkflowInstanceContext, TwoPhaseEvent>> createBeforeInstanceDeleteEvent(
			WorkflowInstanceContext instance) {
		return new BeforeWorkflowDeleteEvent(instance, ActionTypeConstants.DELETE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BeforeInstanceCancelEvent<WorkflowInstanceContext, ? extends AfterInstanceCancelEvent<WorkflowInstanceContext, TwoPhaseEvent>> createBeforeInstanceCancelEvent(
			WorkflowInstanceContext instance) {
		return new BeforeWorkflowCancelEvent(instance, TaskProperties.TRANSITION_CANCEL);
	}
}