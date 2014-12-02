package com.sirma.itt.cmf.event.workflow;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before workflow to be canceled in Activiti and then deleted. <br>
 * <b>NOTE:</b> if the workflow is deleted indirectly (for example the holding case is deleted) then
 * the Activiti engine will be called in advance and after the event is finished Activiti may not be
 * called for this workflow anymore. To check if Activiti is going to be called <br>
 * <code>
 * if (!event.getInstance().getProperties().containsKey(com.sirma.itt.cmf.constants.WorkflowProperties.DO_NO_CALL_DMS)) {
		// activiti will not be called
		.....
	}
 * </code>
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation(" * Event fired before workflow to be canceled in Activiti and then deleted. <br>"
		+ " <b>NOTE:</b> if the workflow is deleted indirectly (for example the holding case is"
		+ " deleted) then the Activiti engine will be called in advance and after the event is finished"
		+ " Activiti may not be called for this workflow anymore. To check if Activiti is going to be called <br>"
		+ "<pre><code>\n "
		+ "if (!event.getInstance().getProperties().containsKey(com.sirma.itt.cmf.constants.WorkflowProperties.DO_NO_CALL_DMS)) {\n		"
		+ "// activiti will not be called\n		.....\n	}\n  </code></pre>")
public class BeforeWorkflowDeleteEvent extends
		BeforeInstanceDeleteEvent<WorkflowInstanceContext, AfterWorkflowDeleteEvent> implements
		OperationEvent {

	/** The operation id. */
	private final String operationId;

	/**
	 * Instantiates a new before workflow cancel event.
	 * 
	 * @param instance
	 *            the instance
	 * @param operationId
	 *            the operation id
	 */
	public BeforeWorkflowDeleteEvent(WorkflowInstanceContext instance, String operationId) {
		super(instance);
		this.operationId = operationId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterWorkflowDeleteEvent createNextEvent() {
		return new AfterWorkflowDeleteEvent(getInstance(), getOperationId());
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
