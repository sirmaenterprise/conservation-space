package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after workflow instance context creation, before to be opened for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired after workflow instance context creation, before to be opened for the first time.")
public class WorkflowCreateEvent extends InstanceCreateEvent<WorkflowInstanceContext> implements
		OperationEvent {

	/** The operation id. */
	private final String operationId;

	/**
	 * Instantiates a new workflow create event.
	 * 
	 * @param instance
	 *            the instance
	 * @param operationId
	 *            the operation id
	 */
	public WorkflowCreateEvent(WorkflowInstanceContext instance, String operationId) {
		super(instance);
		this.operationId = operationId;
	}

	/**
	 * Getter method for operationId.
	 *
	 * @return the operationId
	 */
	@Override
	public String getOperationId() {
		return operationId;
	}

}
