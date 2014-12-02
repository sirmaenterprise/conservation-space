/**
 *
 */
package com.sirma.itt.cmf.services.adapter;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * Adapter for workflows and tasks in DMS.
 * 
 * @author bbanchev
 */
public interface CMFWorkflowAdapterService {

	/**
	 * Transition the task to next state.
	 * 
	 * @param <T>
	 *            is the type for task
	 * @param transition
	 *            the transition to execute
	 * @param task
	 *            the task the update.
	 * @return the list of remaining tasks
	 * @throws DMSException
	 *             on some exception in dms
	 */
	<T extends AbstractTaskInstance> List<T> transition(String transition, T task)
			throws DMSException;

	/**
	 * Updates the task with the new properties.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param task
	 *            the task the update.
	 * @param toRemove
	 *            with properties to remove from the task.
	 * @return the list of remaining tasks
	 * @throws DMSException
	 *             on some exception in dms
	 */
	<T extends AbstractTaskInstance> List<T> updateTask(T task, Map<String, Serializable> toRemove)
			throws DMSException;

	/**
	 * Start workflow process.
	 * 
	 * @param startTask
	 *            the starting task
	 * @param workflowContext
	 *            the start workflow context
	 * @return the list of created task instances
	 * @throws DMSException
	 *             if some error occurs during update
	 */
	List<TaskInstance> startWorkflow(TaskInstance startTask, WorkflowInstanceContext workflowContext)
			throws DMSException;

	/**
	 * Starts single task, relating on properties set on the instance.
	 * 
	 * @param task
	 *            the task to start in dms activiti
	 * @return the started task with updated data.
	 * @throws DMSException
	 *             on any error
	 */
	StandaloneTaskInstance startTask(StandaloneTaskInstance task) throws DMSException;

	/**
	 * Cancel workflow process.
	 * 
	 * @param workflowContext
	 *            the start workflow context
	 * @throws DMSException
	 *             if some error occurs during update and workflow is not cancelled.
	 */
	void cancelWorkflow(WorkflowInstanceContext workflowContext) throws DMSException;

	/**
	 * Delete workflow process.
	 * 
	 * @param workflowContext
	 *            the start workflow context
	 * @param permanent
	 *            the permanent
	 * @throws DMSException
	 *             if some error occurs during update and workflow is not cancelled.
	 */
	void deleteWorkflow(WorkflowInstanceContext workflowContext, boolean permanent)
			throws DMSException;

	/**
	 * Cancel standalone task instance in dms in the mean of deleting active state
	 * 
	 * @param taskInstance
	 *            is the task instance to cancel
	 * @throws DMSException
	 *             on error or cancellation failure
	 */
	void cancelTask(StandaloneTaskInstance taskInstance) throws DMSException;

	/**
	 * Delete standalone task instance in dms in the mean of canceling and archiving so not to be
	 * searchable again
	 * 
	 * @param taskInstance
	 *            is the task instance to cancel
	 * @throws DMSException
	 *             on error or cancellation failure
	 */
	void deleteTask(StandaloneTaskInstance taskInstance) throws DMSException;

	/**
	 * Search tasks based on provided query. Query may have one of the following accepted arguments:<br>
	 * <code>
	 * ?authority={authority?}&amp;state={state?}&amp;priority={priority?}&amp;pooledTasks={pooledTasks?}&amp;dueBefore={dueBefore?}&amp;dueAfter={dueAfter?}&amp;properties={properties?}&amp;exclude={exclude?}
	 * </code>
	 * 
	 * @param args
	 *            the args to search with
	 * @return the search arguments with populated result
	 * @throws DMSException
	 *             on dms error
	 */
	SearchArguments<TaskInstance> searchTasks(SearchArguments<TaskInstance> args)
			throws DMSException;

	/**
	 * Search tasks based on provided query.Return pairs of <taskId,wfId> without constructing
	 * entities
	 * 
	 * @param args
	 *            the args to search with
	 * @return the search arguments with populated result
	 * @throws DMSException
	 *             on dms error
	 */
	SearchArguments<Pair<String, String>> searchTasksLight(
			SearchArguments<Pair<String, String>> args) throws DMSException;

	/**
	 * Search tasks based on provided query for the provided workflow. Query may have one of the
	 * following accepted arguments:<br>
	 * <code>
	 * ?authority={authority?}&amp;state={state?}&amp;priority={priority?}&amp;dueBefore={isoDate?}&amp;dueAfter={isoDate?}&amp;properties={prop1, prop2, prop3...?}&amp;exclude={exclude?}
	 * </code>
	 * 
	 * @param workflowContext
	 *            is the workflowContext
	 * @param args
	 *            the args to search with
	 * @return the search arguments with populated result
	 * @throws DMSException
	 *             on dms error
	 */
	SearchArguments<TaskInstance> searchWorkflowTasks(WorkflowInstanceContext workflowContext,
			SearchArguments<TaskInstance> args) throws DMSException;

	/**
	 * Gets the tasks for the given workflow instance. The task a filtered by task state
	 * 
	 * @param context
	 *            the workflow instance cotext
	 * @param state
	 *            the required state
	 * @return the tasks or empty list if none
	 * @throws DMSException
	 *             the dMS exception
	 */
	List<TaskInstance> getTasks(WorkflowInstanceContext context, TaskState state)
			throws DMSException;

	/**
	 * Returns the process diagram for the flow with bold current state.
	 * 
	 * @param workflowInstance
	 *            is the forkflow to process
	 * @return the image read from stream.
	 * @throws DMSException
	 *             on error
	 */
	BufferedImage getProcessDiagram(String workflowInstance) throws DMSException;

	/**
	 * Filter task properties for tasks, that have passed the cmf loading process. This method is
	 * done for optimization
	 * 
	 * @param currentTask
	 *            the current task to filter properties of
	 * @return the map of filtered task properties
	 */
	Map<String, Serializable> filterTaskProperties(AbstractTaskInstance currentTask);
}
