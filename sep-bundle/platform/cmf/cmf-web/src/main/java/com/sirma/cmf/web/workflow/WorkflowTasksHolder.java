package com.sirma.cmf.web.workflow;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.itt.cmf.beans.model.TaskInstance;

/**
 * Holder of workflow task lists: active and completed if any at all. This should be initialized
 * trough appropriate landing page and the tasks tables to be binded to the lists in this holder.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class WorkflowTasksHolder implements Serializable {

	private static final long serialVersionUID = 4296497583412858350L;

	/** The active tasks. */
	private List<TaskInstance> activeTasks;

	/** The completed tasks. */
	private List<TaskInstance> completedTasks;

	/**
	 * Getter method for activeTasks.
	 * 
	 * @return the activeTasks
	 */
	public List<TaskInstance> getActiveTasks() {
		return activeTasks;
	}

	/**
	 * Setter method for activeTasks.
	 * 
	 * @param activeTasks
	 *            the activeTasks to set
	 */
	public void setActiveTasks(List<TaskInstance> activeTasks) {
		this.activeTasks = activeTasks;
	}

	/**
	 * Getter method for completedTasks.
	 * 
	 * @return the completedTasks
	 */
	public List<TaskInstance> getCompletedTasks() {
		return completedTasks;
	}

	/**
	 * Setter method for completedTasks.
	 * 
	 * @param completedTasks
	 *            the completedTasks to set
	 */
	public void setCompletedTasks(List<TaskInstance> completedTasks) {
		this.completedTasks = completedTasks;
	}

	/**
	 * Getter method for completedTasksByWorkflow.
	 * 
	 * @return the completedTasksByWorkflow
	 */
	public List<TaskInstance> getCompletedTasksByWorkflow() {
		return getCompletedTasks();
	}

	/**
	 * Setter method for completedTasksByWorkflow.
	 * 
	 * @param completedTasksByWorkflow
	 *            the completedTasksByWorkflow to set
	 */
	public void setCompletedTasksByWorkflow(List<TaskInstance> completedTasksByWorkflow) {
		setCompletedTasks(completedTasksByWorkflow);
	}

}
