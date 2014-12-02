package com.sirma.cmf.web.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.workflow.transition.TaskTransitionAction;
import com.sirma.cmf.web.workflow.transition.TransitionsBuilder;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.emf.definition.DefinitionUtil;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class WorkflowActionBase.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class WorkflowActionBase extends EntityAction implements Serializable {

	private static final long serialVersionUID = 1966951418039806033L;

	/** The workflow service. */
	@Inject
	protected WorkflowService workflowService;

	/** The operations builder. */
	@Inject
	protected TransitionsBuilder operationsBuilder;

	/** The selected workflow instance id. */
	private Serializable selectedWorkflowInstanceId;

	/** The transition actions. */
	private List<TaskTransitionAction> transitionActions;

	/**
	 * Load task lists.
	 * 
	 * @param workflowInstance
	 *            the workflow instance
	 * @return the pair with the two task lists
	 */
	protected Pair<List<TaskInstance>, List<TaskInstance>> loadTaskLists(
			WorkflowInstanceContext workflowInstance) {

		log.debug("CMFWeb: Executing WorkflowActionBase.loadTaskLists");

		List<TaskInstance> workflowTasks = workflowService.getWorkflowTasks(workflowInstance,
				TaskState.ALL);

		List<TaskInstance> activeTasks = new ArrayList<TaskInstance>();
		List<TaskInstance> completedTasks = new ArrayList<TaskInstance>();

		for (TaskInstance taskInstance : workflowTasks) {
			TaskState state = taskInstance.getState();
			if (TaskState.IN_PROGRESS == state) {
				activeTasks.add(taskInstance);
			} else if (TaskState.COMPLETED == state) {
				completedTasks.add(taskInstance);
			}
		}

		Pair<List<TaskInstance>, List<TaskInstance>> tasks = new Pair<List<TaskInstance>, List<TaskInstance>>(
				activeTasks, completedTasks);
		return tasks;
	}

	/**
	 * Load tasks by workflow.
	 * 
	 * @param workflowInstance
	 *            the workflow instance
	 * @return the list
	 */
	protected List<TaskInstance> loadTasksByWorkflow(WorkflowInstanceContext workflowInstance) {

		log.debug("CMFWeb: Executing WorkflowTaskAction.loadTasksByWorkflow");

		List<TaskInstance> completedTasks = workflowService.getWorkflowTasks(workflowInstance,
				TaskState.COMPLETED);

		return completedTasks;
	}

	/**
	 * Render workflow form.
	 * 
	 * @param taskDefinition
	 *            the task definition
	 * @param startTaskInstance
	 *            the start task instance (this is actually a WorkflowInstanceContext)
	 * @param formViewMode
	 *            the form view mode
	 * @param panel
	 *            the panel
	 * @param rootInstanceName
	 *            the root instance name
	 */
	protected void renderStartTaskForm(TaskDefinitionRef taskDefinition,
			Instance startTaskInstance, FormViewMode formViewMode, UIComponent panel,
			String rootInstanceName) {

		log.debug("CMFWeb: Executing WorkflowActionBase.renderWorkflowForm");

		// - pass the task definition to the definition reader to render the
		// form
		if (taskDefinition != null) {

			getDocumentContext().addDefinition(TaskDefinitionRef.class, taskDefinition);

			mergeFields(taskDefinition);

			// - build the form
			// - for workflow details form we should pass the workflowInstance
			invokeReader(taskDefinition, startTaskInstance, panel, formViewMode, rootInstanceName);
		}
	}

	/**
	 * Gets the landing page navigation string.
	 * 
	 * @param instance
	 *            the instance
	 * @return the landing page navigation string
	 */
	public String getLandingPageNavigationString(Instance instance) {
		if (instance != null) {
			return instance.getClass().getSimpleName().toLowerCase();
		}
		return null;
	}

	/**
	 * Getter method for selectedWorkflowInstanceId.
	 * 
	 * @return the selectedWorkflowInstanceId
	 */
	public Serializable getSelectedWorkflowInstanceId() {
		return selectedWorkflowInstanceId;
	}

	/**
	 * Setter method for selectedWorkflowInstanceId.
	 * 
	 * @param workflowInstanceId
	 *            the selectedWorkflowInstanceId to set
	 */
	public void setSelectedWorkflowInstanceId(Serializable workflowInstanceId) {
		this.selectedWorkflowInstanceId = workflowInstanceId;
	}

	/**
	 * Load transitions.
	 * 
	 * @param taskDefinition
	 *            the task definition
	 * @return the list
	 */
	protected List<TransitionDefinition> loadTransitions(TaskDefinitionRef taskDefinition) {
		return DefinitionUtil.getDefaultTransitions(taskDefinition);
	}

	/**
	 * Pass the transitions to transitions builder to generate the operation buttons.
	 * 
	 * @param taskDefinition
	 *            the task definition
	 * @return Transition actions list
	 */
	protected List<TaskTransitionAction> buildTransitionActions(TaskDefinitionRef taskDefinition) {
		List<TransitionDefinition> transitions = loadTransitions(taskDefinition);
		return operationsBuilder.build(transitions);
	}

	/**
	 * Getter method for transitionActions.
	 * 
	 * @return the transitionActions
	 */
	public List<TaskTransitionAction> getTransitionActions() {
		return transitionActions;
	}

	/**
	 * Setter method for transitionActions.
	 * 
	 * @param actions
	 *            the transitionActions to set
	 */
	public void setTransitionActions(List<TaskTransitionAction> actions) {
		this.transitionActions = actions;
	}

}
