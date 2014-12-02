package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowStartEvent;

/**
 * The listener interface for receiving QVIAlfrescoWorkflow events. The class that is interested in
 * processing a QVIAlfrescoWorkflow event implements this interface, and the object created with
 * that class is registered with a component using the component's
 * <code>addQVIAlfrescoWorkflowListener<code> method. When
 * the QVIAlfrescoWorkflow event occurs, that object's appropriate
 * method is invoked.
 * TODO move in qvi module, it is not part of core functionality
 * 
 * @see QVIAlfrescoWorkflowEvent
 */
@ApplicationScoped
public class QVIAlfrescoWorkflowListener {

	/**
	 * On start of workflow, do metadata update.
	 * 
	 * @param event
	 *            the event
	 */
	public void onStart(@Observes BeforeWorkflowStartEvent event) {
		WorkflowInstanceContext workflowContext = event.getInstance();
		if ((workflowContext != null) && (workflowContext.getProperties() != null)
				&& "activiti$cmfRandomActiviti".equals(workflowContext.getIdentifier())) {
			Serializable value = workflowContext.getProperties().get("qviAssignedTo");
			if ((value == null) || value.toString().isEmpty()) {
				return;
			}
			workflowContext.getProperties().put(TaskProperties.TASK_ASSIGNEE, value);
		}
	}
}
