package com.sirma.itt.cmf.services.observers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.event.task.standalone.BeforeStandaloneTaskDeleteEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskChangeEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskClaimEvent;
import com.sirma.itt.cmf.event.task.standalone.StandaloneTaskReleaseEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskChangeEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskClaimEvent;
import com.sirma.itt.cmf.event.task.workflow.TaskReleaseEvent;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.state.StateService;

/**
 * An asynchronous update interface for receiving notifications about TaskOperations information as
 * the TaskOperations is constructed.
 */
@ApplicationScoped
public class TaskOperationsObserver {

	/** The authentication service. */
	@Inject
	private AuthenticationService authenticationService;

	/** The task service. */
	@Inject
	private TaskService taskService;

	/** The state service. */
	@Inject
	private StateService stateService;

	/**
	 * This method is called when a {@link StandaloneTaskClaimEvent} is observed. Owner is set to
	 * current user
	 *
	 * @param event
	 *            the event to process
	 */
	public void onClaim(@Observes StandaloneTaskClaimEvent event) {
		claimTask(event.getInstance());
	}

	/**
	 * This method is called when a {@link StandaloneTaskReleaseEvent} is observed. Owner is set to
	 * null
	 *
	 * @param event
	 *            the event to process
	 */
	public void onRelease(@Observes StandaloneTaskReleaseEvent event) {
		releaseToPool(event.getInstance());
	}

	/**
	 * This method is called when a {@link TaskClaimEvent} is observed. Owner is set to current user
	 *
	 * @param event
	 *            the event to process
	 */
	public void onClaim(@Observes TaskClaimEvent event) {
		claimTask(event.getInstance());
	}

	/**
	 * This method is called when a {@link BeforeStandaloneTaskDeleteEvent} is observed. State of
	 * task is set to deleted
	 *
	 * @param event
	 *            the event to process
	 */
	public void onDelete(@Observes BeforeStandaloneTaskDeleteEvent event) {
		StandaloneTaskInstance standaloneTaskInstance = event.getInstance();
		standaloneTaskInstance.getProperties().put(TaskProperties.STATUS,
				stateService.getState(PrimaryStates.DELETED, TaskInstance.class));
	}

	/**
	 * This method is called when a {@link TaskReleaseEvent} is observed. Owner is set to null
	 *
	 * @param event
	 *            the event to process
	 */
	public void onRelease(@Observes TaskReleaseEvent event) {
		releaseToPool(event.getInstance());
	}

	/**
	 * Release to pool the task instance. Task owner and assignee are set to null
	 *
	 * @param instance
	 *            to update
	 */
	private void releaseToPool(AbstractTaskInstance instance) {
		String userId = authenticationService.getCurrentUserId();
		if (taskService.isReleasable(instance, userId)) {
			Map<String, Serializable> nullable = new HashMap<>(2);
			nullable.put(TaskProperties.TASK_OWNER, null);
			nullable.put(TaskProperties.TASK_ASSIGNEE, null);
			instance.getProperties().putAll(nullable);
			instance.getProperties().put(TaskProperties.NULLABLE_PROPS,
					new ArrayList<>(nullable.keySet()));
		}
	}

	/**
	 * Internal method to claim task, owner and assignee are set to the current user as properties
	 * of task.
	 *
	 * @param instance
	 *            the instance to set
	 */
	private void claimTask(AbstractTaskInstance instance) {

		String userId = authenticationService.getCurrentUserId();
		if (taskService.isClaimable(instance, userId)) {
			instance.getProperties().put(TaskProperties.TASK_OWNER, userId);
			instance.getProperties().put(TaskProperties.TASK_ASSIGNEE, userId);
		}
	}

	/**
	 * Listens for standalone task change and updates the active state
	 *
	 * @param event
	 *            the event holding the changed task
	 */
	public void onStandaloneTaskChange(@Observes StandaloneTaskChangeEvent event) {
		setIsActiveState(event.getInstance());
	}

	/**
	 * Listens for task change and updates the active state
	 *
	 * @param event
	 *            the event holding the changed task
	 */
	public void onTaskChange(@Observes TaskChangeEvent event) {
		setIsActiveState(event.getInstance());
	}

	/**
	 * Changes {@link TaskProperties#TASK_ACTIVE_STATE} depending on the
	 * {@link AbstractTaskInstance#getState()} value
	 *
	 * @param task
	 *            the task instance to update properties for
	 */
	public void setIsActiveState(AbstractTaskInstance task) {
		if (task.getProperties() != null && task.getState() != TaskState.ALL) {
			task.getProperties().put(TaskProperties.TASK_ACTIVE_STATE,
					(task.getState() == TaskState.COMPLETED) ? Boolean.FALSE : Boolean.TRUE);
		}
	}
}
