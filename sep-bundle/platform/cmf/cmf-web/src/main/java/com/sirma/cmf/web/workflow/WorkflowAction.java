package com.sirma.cmf.web.workflow;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.emf.web.action.event.EmfImmediateActionEvent;

/**
 * {@link WorkflowAction} represent a class that will holds actions and observers for managing
 * workflows components.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class WorkflowAction extends WorkflowActionBase {

	private static final long serialVersionUID = -93723107884887337L;

	/** The workflow service. */
	@Inject
	private WorkflowService workflowService;

	/** The workflow cancel reason, received from workflow reason dialog box. */
	private String workflowCancelReason;

	/**
	 * Observer for starting workflow operation.
	 * 
	 * @param event
	 *            The event payload object.
	 */
	public void startWorkflowForCase(
			@Observes @EMFAction(value = ActionTypeConstants.START_WORKFLOW, target = CaseInstance.class) final EMFActionEvent event) {
		log.debug("CMFWeb: Executing observer WorkflowAction.startWorkflow");

		// clear workflow and task data stored in context before start a new one
		getDocumentContext().clearWorkflowData();
		getDocumentContext().clearSelectedAction();

		Instance instance = event.getInstance();
		initContextForInstanceEdit(instance);
		event.setNavigation(NavigationConstants.NAVIGATE_WORKFLOW_START);
	}

	/**
	 * Start workflow action observer.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void startWorkflowForDocument(
			@Observes @EMFAction(value = ActionTypeConstants.START_WORKFLOW, target = DocumentInstance.class) final EMFActionEvent event) {
		log.debug("CMFWeb: Executing observer WorkflowAction.startWorkflow");

		// clear workflow and task data stored in context before start a new one
		getDocumentContext().clearWorkflowData();
		getDocumentContext().clearSelectedAction();

		Instance instance = event.getInstance();
		initContextForInstanceEdit(instance);
		event.setNavigation(NavigationConstants.NAVIGATE_WORKFLOW_START);
	}

	/**
	 * Observer for cancel/stop workflow operation.
	 * 
	 * @param event
	 *            The event object
	 */
	public void stopWorkflow(
			@Observes @EMFAction(value = ActionTypeConstants.STOP, target = WorkflowInstanceContext.class) final EMFActionEvent event) {
		log.debug("CMFWeb: Executing observer WorkflowAction.stopWorkflow");
		if (event != null) {
			stopWorkflowAction((WorkflowInstanceContext) event.getInstance());
		}
	}

	/**
	 * (Immediate operation observer) Observer for cancel/stop workflow operation.
	 * 
	 * @param event
	 *            The event object
	 */
	public void stopWorkflowImmediate(
			@Observes @EMFAction(value = ActionTypeConstants.STOP, target = WorkflowInstanceContext.class) final EmfImmediateActionEvent event) {
		log.debug("CMFWeb: Executing observer WorkflowAction.stopWorkflowImmediate");
		event.setHandled(true);
	}

	/**
	 * Method that execute stop workflow operation and apply reason string if any.
	 * 
	 * @param workflowInstance
	 *            current workflow instance
	 */
	public void stopWorkflowAction(WorkflowInstanceContext workflowInstance) {
		if (workflowInstance != null) {
			log.debug("Executing action WorkflowAction.stopWorkflowAction");
			if (StringUtils.isNotNullOrEmpty(workflowCancelReason)) {
				workflowInstance.getProperties().put(WorkflowProperties.CANCEL_REASON,
						workflowCancelReason);
			}
			// TODO: can we just refresh the workflow?
			Instance freshInstance = fetchInstance(workflowInstance.getId(), workflowInstance
					.getClass().getSimpleName().toLowerCase());
			getDocumentContext().addInstance(freshInstance);
			workflowService.cancel((WorkflowInstanceContext) freshInstance);
		}
	}

	/**
	 * Getter for stop workflow reason.
	 * 
	 * @return stop reason string
	 */
	public String getWorkflowCancelReason() {
		return workflowCancelReason;
	}

	/**
	 * Setter for stop workflow reason.
	 * 
	 * @param workflowCancelReason
	 *            stop workflow reason text
	 */
	public void setWorkflowCancelReason(String workflowCancelReason) {
		this.workflowCancelReason = workflowCancelReason;
	}
}
