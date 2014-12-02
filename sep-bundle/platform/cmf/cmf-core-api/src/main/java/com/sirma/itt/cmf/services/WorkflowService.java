package com.sirma.itt.cmf.services;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;

import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Defines methods for working with workflows.
 * 
 * @author BBonev
 */
public interface WorkflowService extends
		InstanceService<WorkflowInstanceContext, WorkflowDefinition> {

	/**
	 * Starts the given workflow. Executes the handler for the start task and creates the workflow
	 * into the JBPM system.
	 * 
	 * @param context
	 *            the context
	 * @param startTaskInstance
	 *            the start task instance
	 * @return the list of started tasks
	 */
	List<TaskInstance> startWorkflow(WorkflowInstanceContext context, TaskInstance startTaskInstance);

	/**
	 * Update workflow and executes the operation to move the workflow to the next transition.
	 * 
	 * @param context
	 *            the context
	 * @param taskInstance
	 *            the task instance
	 * @param operation
	 *            the operation
	 * @return the list of started tasks
	 */
	List<TaskInstance> updateWorkflow(WorkflowInstanceContext context, TaskInstance taskInstance,
			String operation);

	/**
	 * Updates the given task instance without performing a transition. The method should also
	 * update the attached workflow context.
	 * 
	 * @param instance
	 *            the task instance to update
	 */
	void updateTaskInstance(TaskInstance instance);

	/**
	 * TODO: replace with method {@link #load(String)} Loads the context for the given workflow
	 * instance ID.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @return the workflow instance context
	 */
	WorkflowInstanceContext loadContext(String instanceId);

	/**
	 * TODO: replace with method {@link #load(List)} Load the contexts for the given list of
	 * workflow instance IDs.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param instanceId
	 *            the instance id
	 * @return the list
	 */
	<S extends Serializable> List<WorkflowInstanceContext> loadContexts(List<S> instanceId);

	/**
	 * Gets the workflow tasks.
	 * 
	 * @param context
	 *            the instance id
	 * @param state
	 *            the state
	 * @return the workflow tasks
	 */
	List<TaskInstance> getWorkflowTasks(WorkflowInstanceContext context, TaskState state);

	/**
	 * Gets the workflow context an active context for the given.
	 * 
	 * @param instance
	 *            the case instance to get the workflow context for
	 * @return the context or <code>null</code> if no active workflow was found.
	 */
	List<WorkflowInstanceContext> getCurrentWorkflow(Instance instance);

	/**
	 * Gets the diagram that represents the given workflow process instance.
	 * 
	 * @param context
	 *            the workflow context
	 * @return the workflow process diagram or <code>null</code> if failed to retrieve the diagram
	 */
	BufferedImage getWorkflowProcessDiagram(WorkflowInstanceContext context);

	/**
	 * Gets the past workflows for the given instance.
	 * 
	 * @param caseInstance
	 *            the instance
	 * @return the workflows history
	 */
	List<WorkflowInstanceContext> getWorkflowsHistory(Instance caseInstance);

	@Override
	WorkflowInstanceContext save(WorkflowInstanceContext instance, Operation operation);

	/**
	 * Cancel the given workflow instance.
	 * 
	 * @param instance
	 *            the instance to cancel
	 * @return the updated instance
	 */
	@Override
	WorkflowInstanceContext cancel(WorkflowInstanceContext instance);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void delete(WorkflowInstanceContext instance, Operation operation, boolean permanent);

}
