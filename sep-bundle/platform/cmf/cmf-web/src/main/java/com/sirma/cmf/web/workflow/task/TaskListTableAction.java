package com.sirma.cmf.web.workflow.task;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.TableAction;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class TaskListTableAction.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class TaskListTableAction extends TableAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1404542453335520183L;

	/** The workflow service. */
	@Inject
	private TaskService taskService;

	/**
	 * Open selected task.
	 * 
	 * @param taskInstance
	 *            the task instance
	 * @return Navigation string.
	 */
	// TODO: Move in WorkflowTaskAction
	public String open(TaskInstance taskInstance) {

		log.debug("CMFWeb: Executing TaskListTableAction.open. Task ID ["
				+ taskInstance.getTaskInstanceId() + "]");

		// create default instance objects if not created
		taskService.prepareTaskInstance(taskInstance);

		WorkflowInstanceContext workflowInstance = taskInstance.getContext();

		// this is case in CMF and/or project in PMF
		Instance owningInstance = workflowInstance.getOwningInstance();

		String workflowDefinitionId = taskInstance.getWorkflowDefinitionId();

		WorkflowDefinition workflowDefinition = dictionaryService.getDefinition(
				WorkflowDefinition.class, workflowDefinitionId, workflowInstance.getRevision());

		TaskDefinitionRef taskDefinition = WorkflowHelper.getTaskById(workflowDefinition,
				taskInstance.getIdentifier());

		getDocumentContext().addInstance(owningInstance);
		getDocumentContext().addInstance(workflowInstance);
		// set the task instance to be the current instance
		// getDocumentContext().setTaskDefinition(taskDefinition);
		// getDocumentContext().setCurrentInstance(taskInstance);
		getDocumentContext().populateContext(taskInstance, TaskDefinitionRef.class, taskDefinition);

		return NavigationConstants.NAVIGATE_TASK_DETAILS_PAGE;
	}

}
