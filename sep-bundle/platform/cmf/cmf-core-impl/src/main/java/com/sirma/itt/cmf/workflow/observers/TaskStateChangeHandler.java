package com.sirma.itt.cmf.workflow.observers;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskCancelEvent;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskStartEvent;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskTransitionEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskActivateEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskChangeEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskOnHoldEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskOpenEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskActivateEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskChangeEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskOnHoldEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskOpenEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowCancelEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowStartEvent;
import com.sirma.itt.cmf.event.workflow.BeforeWorkflowTransitionEvent;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.cmf.workflows.TaskTransition;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Handler class for changing the task states when completing tasks or canceling workflow.
 *
 * @author BBonev
 */
@ApplicationScoped
public class TaskStateChangeHandler {

	/** The workflow service. */
	@Inject
	private WorkflowService workflowService;

	/** The task service. */
	@Inject
	private TaskService taskService;
	/** The current logged user. */
	@Inject
	private AuthenticationService authenticationService;

	/** The state service. */
	@Inject
	private StateService stateService;

	/** The codelist start task transition. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_START_TASK_OUTCOME, defaultValue = "RT0099")
	private String codelistStartTaskTransition;

	/** The event service. */
	@Inject
	private EventService eventService;

	/**
	 * When a task assigned to the current user is opened trough the UI and it is in initial state,
	 * the its status should be changed to be in progress automatically.
	 *
	 * @param event
	 *            the event
	 */
	public void onOpenTaskStatusChange(@Observes TaskOpenEvent event) {
		onTaskOpen(event);
	}

	/**
	 * On hold task status change.
	 *
	 * @param event
	 *            the event
	 */
	public void onHoldTaskStatusChange(@Observes TaskOnHoldEvent event) {
		onTaskHold(event);
	}

	/**
	 * On activate task status change.
	 *
	 * @param event
	 *            the event
	 */
	public void onActivateTaskStatusChange(@Observes TaskActivateEvent event) {
		onTaskActivate(event);
	}

	/**
	 * On workflow start udpate the start task status.
	 *
	 * @param transitionDto
	 *            the transition dto
	 */
	public void onWorkflowStartTaskStatusChange(
			@Observes @TaskTransition(event = TaskProperties.TRANSITION_WORKFLOW_START) BeforeWorkflowTransitionEvent transitionDto) {
		// clear the properties in order to send only the changed property
		Map<String, Serializable> properties = transitionDto.getTaskInstance().getProperties();
		// set completed state
		properties.put(TaskProperties.STATUS, getTaskStateCompleted());
	}

	/**
	 * On task completion set the next task state.
	 *
	 * @param transitionDto
	 *            the transition dto
	 */
	public void onTaskCompletion(@Observes BeforeWorkflowTransitionEvent transitionDto) {
		AbstractTaskInstance instance = transitionDto.getTaskInstance();
		setTaskState(instance, getTaskStateCompleted());
		HashMap<String, Serializable> map = new HashMap<String, Serializable>(1);
		map.put(TaskProperties.STATUS, getTaskStateOpen());
		instance.getProperties().put(TaskProperties.NEXT_STATE_PROP_MAP, map);
	}

	/**
	 * On workflow cancellation.
	 *
	 * @param transitionDto
	 *            the transition dto
	 */
	public void onWorkflowCancellation(@Observes BeforeWorkflowCancelEvent transitionDto) {

		WorkflowInstanceContext context = transitionDto.getInstance();

		HashMap<String, Serializable> currentState = getCancellationProperties();
		context.getProperties().put(TaskProperties.CURRENT_STATE_PROP_MAP, currentState);
		// get all active tasks and set the status to cancel and save them
		List<TaskInstance> tasks = workflowService.getWorkflowTasks(context, TaskState.IN_PROGRESS);
		// get the old value
		Operation operation = new Operation(ActionTypeConstants.STOP);
		boolean configurationNotSet = !RuntimeConfiguration
				.isConfigurationSet(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		if (configurationNotSet) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.DO_NO_CALL_DMS,
					Boolean.TRUE);
		}
		for (TaskInstance instance : tasks) {
			instance.getProperties().putAll(currentState);
			instance.setState(TaskState.COMPLETED);

			// should save the changes to CMF only
			taskService.save(instance, operation);
		}
		if (configurationNotSet) {
			RuntimeConfiguration.clearConfiguration(RuntimeConfigurationProperties.DO_NO_CALL_DMS);
		}
	}

	/**
	 * Internal collect of cancellation properties for task.
	 *
	 * @return the properties to update for task
	 */
	private HashMap<String, Serializable> getCancellationProperties() {
		HashMap<String, Serializable> currentState = new HashMap<String, Serializable>(1);
		currentState.put(TaskProperties.TRANSITION_OUTCOME, "");
		currentState.put(TaskProperties.SEARCH_STATE, TaskState.COMPLETED);
		currentState.put(TaskProperties.ACTUAL_END_DATE, new Date());
		currentState.put(TaskProperties.STATUS, getTaskStateCanceled());
		return currentState;
	}

	/**
	 * On workflow start.
	 *
	 * @param transitionDto
	 *            the transition dto
	 */
	public void onWorkflowStart(@Observes BeforeWorkflowStartEvent transitionDto) {
		WorkflowInstanceContext context = transitionDto.getInstance();
		if (context != null) {
			context.getProperties().put(TaskProperties.TRANSITION_OUTCOME,
					codelistStartTaskTransition);
			// set current status and next state task's status

			HashMap<String, Serializable> map = new HashMap<String, Serializable>(1);
			HashMap<String, Serializable> currentState = new HashMap<String, Serializable>(1);
			map.put(TaskProperties.STATUS, getTaskStateOpen());
			currentState.put(TaskProperties.STATUS, getTaskStateCompleted());
			context.getProperties().put(TaskProperties.NEXT_STATE_PROP_MAP, map);
			context.getProperties().put(TaskProperties.CURRENT_STATE_PROP_MAP, currentState);
		}
	}

	/**
	 * Sets the task state.
	 *
	 * @param instance
	 *            the instance
	 * @param value
	 *            the value
	 */
	private void setTaskState(AbstractTaskInstance instance, String value) {
		instance.getProperties().put(TaskProperties.STATUS, value);
	}

	/**
	 * On activate task status change.
	 *
	 * @param event
	 *            the event
	 */
	public void onActivateTaskStatusChange(@Observes StandaloneTaskActivateEvent event) {
		onTaskActivate(event);
	}

	/**
	 * On standalone task cancellation.
	 *
	 * @param transitionDto
	 *            the transition dto
	 */
	public void onStandaloneTaskCancellation(@Observes BeforeStandaloneTaskCancelEvent transitionDto) {
		StandaloneTaskInstance instance = transitionDto.getInstance();
		setTaskState(instance, getTaskStateCanceled());
		instance.getProperties().putAll(getCancellationProperties());
	}

	/**
	 * On standalone task completion.
	 *
	 * @param transitionDto
	 *            the transition dto
	 */
	public void onStandaloneTaskCompletion(
			@Observes BeforeStandaloneTaskTransitionEvent transitionDto) {
		AbstractTaskInstance instance = transitionDto.getInstance();
		setTaskState(instance, getTaskStateCompleted());
	}

	/**
	 * On hold task status change.
	 *
	 * @param event
	 *            the event
	 */
	public void onHoldStandaloneTaskStatusChange(@Observes StandaloneTaskOnHoldEvent event) {
		// clear the properties in order to send only the changed property
		Map<String, Serializable> properties = event.getInstance().getProperties();
		properties.put(TaskProperties.STATUS, getTaskStateOnHold());
	}

	/**
	 * On open standalone task status change.
	 *
	 * @param event
	 *            the event
	 */
	public void onOpenStandaloneTaskStatusChange(@Observes StandaloneTaskOpenEvent event) {
		onTaskOpen(event);
	}

	/**
	 * On task open.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	private <I extends Instance> void onTaskOpen(AbstractInstanceEvent<I> event) {
		Map<String, Serializable> properties = event.getInstance().getProperties();
		String state = (String) properties.get(TaskProperties.STATUS);
		String ownerId = (String) properties.get(TaskProperties.TASK_OWNER);

		User currentUser = getCurrentUser();
		if (EqualsHelper.nullSafeEquals(getTaskStateOpen(), state, true)
				&& (currentUser.getIdentifier().equals(ownerId) || taskService.isClaimable(
						(AbstractTaskInstance) event.getInstance(), currentUser.getIdentifier()))) {
			event.getInstance().getProperties().put(TaskProperties.STATUS, getOpenState());
			Instance instance = event.getInstance();
			if (instance instanceof TaskInstance) {
				eventService.fire(new TaskChangeEvent((TaskInstance) instance));
			} else {
				eventService.fire(new StandaloneTaskChangeEvent((StandaloneTaskInstance) instance));
			}
		}
	}

	/**
	 * Gets the current user.
	 * 
	 * @return the current user
	 */
	private User getCurrentUser() {
		return SecurityContextManager.getCurrentUser(authenticationService);
	}

	/**
	 * On task hold for any task.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	private <I extends Instance> void onTaskHold(AbstractInstanceEvent<I> event) {
		// clear the properties in order to send only the changed property
		Map<String, Serializable> properties = event.getInstance().getProperties();
		properties.put(TaskProperties.STATUS, getTaskStateOnHold());
	}

	/**
	 * On task activate for any task.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param event
	 *            the event
	 */
	private <I extends Instance> void onTaskActivate(AbstractInstanceEvent<I> event) {
		// clear the properties in order to send only the changed property
		Map<String, Serializable> properties = event.getInstance().getProperties();
		properties.put(TaskProperties.STATUS, getOpenState());
	}

	/**
	 * On standalone task event start.
	 *
	 * @param transitionDto
	 *            the transition dto
	 */
	public void onWorkflowStart(@Observes BeforeStandaloneTaskStartEvent transitionDto) {
		// TODO change to codelist
		StandaloneTaskInstance context = transitionDto.getInstance();
		context.getProperties().put(TaskProperties.TRANSITION_OUTCOME, "");
		// set current status and next state task's status
		context.getProperties().put(TaskProperties.STATUS, getTaskStateOpen());

	}

	/**
	 * Gets the open state.
	 * 
	 * @return the open state
	 */
	private String getOpenState() {
		return stateService.getState(PrimaryStates.OPENED, TaskInstance.class);
	}

	/**
	 * Getter method for taskStateOpen.
	 * 
	 * @return the taskStateOpen
	 */
	private String getTaskStateOpen() {
		return stateService.getState(PrimaryStates.APPROVED, TaskInstance.class);
	}

	/**
	 * Getter method for taskStateOnHold.
	 * 
	 * @return the taskStateOnHold
	 */
	private String getTaskStateOnHold() {
		return stateService.getState(PrimaryStates.ON_HOLD, TaskInstance.class);
	}

	/**
	 * Getter method for taskStateCompleted.
	 * 
	 * @return the taskStateCompleted
	 */
	private String getTaskStateCompleted() {
		return stateService.getState(PrimaryStates.COMPLETED, TaskInstance.class);
	}

	/**
	 * Getter method for taskStateCanceled.
	 * 
	 * @return the taskStateCanceled
	 */
	private String getTaskStateCanceled() {
		return stateService.getState(PrimaryStates.CANCELED, TaskInstance.class);
	}
}
