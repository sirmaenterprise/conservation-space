package com.sirma.itt.cmf.services;

import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Service for working with standalone tasks.
 * 
 * @author BBonev
 */
public interface StandaloneTaskService extends
		InstanceService<StandaloneTaskInstance, TaskDefinition> {

	/**
	 * Starts the given task instance into Activiti engine and assigns the task to the filled
	 * assignee. <br>
	 * NOTE: before calling the method the assignee/s should be set.
	 * 
	 * @param instance
	 *            the instance to start
	 * @param operation
	 *            the operation used to start the task
	 * @return the updated task instance
	 */
	StandaloneTaskInstance start(StandaloneTaskInstance instance, Operation operation);

	/**
	 * Completes a task instance. To call this method the instance should be started first.
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the updated task instance
	 */
	StandaloneTaskInstance complete(StandaloneTaskInstance instance, Operation operation);

	/**
	 * Cancels the task instance. The instance may not be started to be canceled.
	 * 
	 * @param instance
	 *            the instance
	 * @return the updated task instance
	 */
	@Override
	StandaloneTaskInstance cancel(StandaloneTaskInstance instance);

	/**
	 * {@inheritDoc}
	 * <p>
	 * The provided task instance should be started before calling this method. If an operations is
	 * not provided then the method only saves the tasks properties and reassigns the task if
	 * necessary. If operations is provided then the method is equal to calling
	 * {@link #complete(StandaloneTaskInstance, Operation)} method.
	 */
	@Override
	StandaloneTaskInstance save(StandaloneTaskInstance instance, Operation operation);
}
