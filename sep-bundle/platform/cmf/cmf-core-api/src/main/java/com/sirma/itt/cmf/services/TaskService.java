package com.sirma.itt.cmf.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.beans.model.WorkLogEntry;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Service for common access to both workflow and standalone tasks.
 * 
 * @author BBonev
 */
public interface TaskService extends InstanceService<AbstractTaskInstance, DefinitionModel> {

	/**
	 * Cancels the task instance. The instance may not be started to be canceled.
	 * 
	 * @param instance
	 *            the instance
	 * @return the updated task instance
	 */
	@Override
	AbstractTaskInstance cancel(AbstractTaskInstance instance);

	/**
	 * {@inheritDoc}
	 */
	@Override
	AbstractTaskInstance save(AbstractTaskInstance instance, Operation operation);

	/**
	 * Checks if the given user has tasks for the given reference instance with the given state.
	 * 
	 * @param instance
	 *            the reference instance
	 * @param userid
	 *            the userid
	 * @param taskState
	 *            the task state
	 * @return true, if has at least one task
	 */
	boolean hasUserTasks(Instance instance, String userid, TaskState taskState);

	/**
	 * Checks if the given user has pool task assigned to him as individual user or as part of
	 * group. Only tasks that are not assigned to any owner are included in check.
	 * 
	 * @param instance
	 *            the reference instance
	 * @param userid
	 *            the userid
	 * @param taskState
	 *            the task state
	 * @return true, if it is part of at least one task
	 */
	boolean hasUserPooledTasks(Instance instance, String userid, TaskState taskState);

	/**
	 * Check if this is pool task - has no owner and has a list of pool resources
	 * 
	 * @param instance
	 *            the task to check
	 * @return true if this is considered pool task
	 */
	boolean isPooledTask(AbstractTaskInstance instance);

	/**
	 * Get the current list of pool users for task either this is pool user task or pool group task.
	 * If it is pool group task, all users from the group are returned.
	 * 
	 * @param instance
	 *            the task to get for
	 * @return the list of pool users or null if this is not a pool task
	 */
	Collection<String> getPoolUsers(AbstractTaskInstance instance);

	/**
	 * Get the current list of resources for task either this is pool user task or pool group task.
	 * If it is pool group task, the group is returned.
	 * 
	 * @param instance
	 *            the task to get for
	 * @return the list of <code>GROUP,USERS</code> or null if this is not a pool task. One of the
	 *         pair is always null, depending on the task model
	 */
	Pair<Set<String>, Set<String>> getPoolResources(AbstractTaskInstance instance);

	/**
	 * Prepare task instance. Fills the properties that need to be filled in order the data can be
	 * saved.
	 * 
	 * @param instance
	 *            the instance
	 */
	void prepareTaskInstance(AbstractTaskInstance instance);

	/**
	 * Gets the users for a reference instance that have tasks with the given state.
	 * 
	 * @param instance
	 *            the instance
	 * @param taskState
	 *            the task state
	 * @return the active users for case
	 */
	Set<String> getUsersWithTasksForInstance(Instance instance, TaskState taskState);

	/**
	 * Gets the pool resources for a reference instance that have tasks with the given state.
	 * Results does not depend if there is owner
	 * 
	 * @param instance
	 *            the instance
	 * @param taskState
	 *            the task state
	 * @return the set of pool users and pool groups joined for all found tasks
	 */
	Pair<Set<String>, Set<String>> getPoolResourcesWithTasksForInstance(Instance instance,
			TaskState taskState);

	/**
	 * Adds the tasks for owning instance and in a given parent context.
	 * 
	 * @param <T>
	 *            the Task type
	 * @param owningInstance
	 *            the owning instance of the tasks: case, project
	 * @param tasks
	 *            the list of tasks to add
	 * @param context
	 *            the direct parent context of the tasks. For workflow tasks this is the workflow
	 *            instance. For sub tasks this is the parent task.
	 * @param active
	 *            If the list of tasks is active or inactive tasks
	 */
	<T extends AbstractTaskInstance> void attachTaskToInstance(Instance owningInstance,
			List<T> tasks, Instance context, boolean active);

	/**
	 * Removes tasks from owning context. The method should be called when standalone tasks are
	 * moved to new parent.
	 * 
	 * @param <T>
	 *            the Task type
	 * @param owningInstance
	 *            the owning instance of the tasks: case, project
	 * @param tasks
	 *            the list of tasks to remove
	 */
	<T extends AbstractTaskInstance> void dettachTaskFromInstance(Instance owningInstance,
			List<T> tasks);

	/**
	 * Removes the tasks for the given context. This should be called when the context is
	 * deleted/cancelled/archived. This effectively deletes the association between the
	 * tasks-context-owning instance.
	 * 
	 * @param context
	 *            the context to delete his tasks
	 * @param getExisting
	 *            if <code>true</code> the method will return the task identifiers of the tasks
	 *            before deletion. If <code>false</code> empty set will be returned.
	 * @return the sets of identifiers for the current tasks before deletion or empty list depending
	 *         on the <b>getExisting</b> parameter.
	 */
	Set<String> removeContextTasks(Instance context, boolean getExisting);

	/**
	 * Change task status. Updates the tasks status
	 * 
	 * @param owningInstance
	 *            the owning instance of the task
	 * @param context
	 *            the task context
	 * @param instance
	 *            the task instance
	 * @param active
	 *            <code>true</code> to set it as active and <code>false</code> to inactive
	 */
	void updateTaskStateAndAssignment(Instance owningInstance, Instance context,
			AbstractTaskInstance instance, Boolean active);

	/**
	 * Gets the tasks for the given context if any. The method returns the instance ids of the tasks
	 * assigned to the given context. To fetch the actual tasks call {@link #load(List)} with the
	 * result.
	 * 
	 * @param context
	 *            the context
	 * @param active
	 *            if <code>true</code> the method should return active tasks, if <code>false</code>
	 *            the inactive. If <code>null</code> the method should return all of them.
	 * @return the tasks instance ids
	 */
	Set<String> getContextTasks(Instance context, Boolean active);

	/**
	 * Checks if is claimable. Task is claimable if user is contained in the pool and the other
	 * constraints are fulfilled
	 * 
	 * @param instance
	 *            is the task to check
	 * @param userId
	 *            the user to check
	 * @return true, if is claimable
	 */
	boolean isClaimable(AbstractTaskInstance instance, String userId);

	/**
	 * Checks if is releaseable. Task is releaseable if the pool is not empty, user is current owner
	 * or creator of task
	 * 
	 * @param instance
	 *            is the task to check
	 * @param userId
	 *            the user to check
	 * @return true, if is releaseable
	 */
	boolean isReleasable(AbstractTaskInstance instance, String userId);

	/**
	 * Gets the sub tasks for the given task.
	 * 
	 * @param <A>
	 *            the task type
	 * @param currentInstance
	 *            the current instance
	 * @return the sub tasks
	 */
	<A extends AbstractTaskInstance> List<A> getSubTasks(AbstractTaskInstance currentInstance);

	/**
	 * Gets the sub tasks for the given task filtered by the given task state. If passed
	 * {@link TaskState#ALL} it's the same as calling the method
	 * {@link #getSubTasks(AbstractTaskInstance)}.
	 * 
	 * @param <A>
	 *            the task type
	 * @param currentInstance
	 *            the current instance
	 * @param state
	 *            the state to filter
	 * @param includeCurrent
	 *            if should the current task be included in the returned list or not.
	 * @return the sub tasks
	 */
	<A extends AbstractTaskInstance> List<A> getSubTasks(AbstractTaskInstance currentInstance,
			TaskState state, boolean includeCurrent);

	/**
	 * Checks for sub tasks for the given task and state. If passed {@link TaskState#ALL} the check
	 * will check if there are subtask at all.
	 * 
	 * @param currentInstance
	 *            the current instance
	 * @param state
	 *            the state
	 * @return true, if found more then one task with the given state
	 */
	boolean hasSubTasks(AbstractTaskInstance currentInstance, TaskState state);

	/**
	 * Log work for the given task reference for the given user.
	 * 
	 * @param task
	 *            the task reference
	 * @param userId
	 *            the user id to log the work to
	 * @param loggedData
	 *            the logged data
	 * @return the generated id for the logged entry.
	 */
	Serializable logWork(InstanceReference task, String userId, Map<String, Serializable> loggedData);

	/**
	 * Update logged work data for the given id.
	 * 
	 * @param id
	 *            the id
	 * @param loggedData
	 *            the logged data
	 * @return true, if successful
	 */
	boolean updateLoggedWork(Serializable id, Map<String, Serializable> loggedData);

	/**
	 * Delete logged work.
	 * 
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	boolean deleteLoggedWork(Serializable id);

	/**
	 * Gets the logged data for the given task instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return the logged data
	 */
	List<WorkLogEntry> getLoggedData(AbstractTaskInstance instance);

	/**
	 * Gets the tasks for the given owning instance if any. The method returns the instance ids of
	 * the tasks assigned to the given instance. To fetch the actual tasks call {@link #load(List)}
	 * on the result.
	 * 
	 * @param owningInstance
	 *            the owning instance
	 * @param taskState
	 *            is the state that is desired - all should skip state check
	 * @param type
	 *            the type of the tasks to fetch, if <code>null</code> standalone and workflow tasks
	 *            will be fetched.
	 * @return the tasks instance ids
	 */
	List<String> getOwnedTaskInstances(Instance owningInstance, TaskState taskState, TaskType type);

}
