package com.sirmaenterprise.sep.bpm.camunda.bpmn;

import java.util.Collection;
import java.util.Objects;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;

/**
 * The {@link CamundaBPMNService} is a specific service related to BPMN implementation of Camunda engine. Provides extra
 * methods over the {@link ProcessService}.
 */
public interface CamundaBPMNService {

	/**
	 * Check if instance is a process instance
	 * 
	 * @param activity
	 *            the instance to check
	 * @return true if it is a process instance (which might not be started)
	 */
	static boolean isProcess(Instance activity) {
		Objects.requireNonNull(activity, "Instance is a required parameter to check if it is process!");
		Objects.requireNonNull(activity.type(),
				"Instance type is a required parameter to check if instance is process!");
		return activity.type().is("workflowinstancecontext");
	}

	/**
	 * Gets the process definition for workflow instance.
	 *
	 * @param process
	 *            the process instance to check definition for
	 * @return the process definition - single entry, last revision. Throws runtime exception if multiple found
	 *         definitions
	 */
	ProcessDefinition getProcessDefinition(Instance process);

	/**
	 * Gets the process definition by start message id.
	 *
	 * @param messageId
	 *            the start message id to check definition for
	 * @return the process definition - single entry, last revision. Throws runtime exception if multiple found
	 *         definitions
	 */
	ProcessDefinition getProcessDefinitionByMessageId(String messageId);

	/**
	 * Gets the bpmn model instance for given activity .
	 *
	 * @param activity
	 *            the activity part of some existing process
	 * @return the {@link BpmnModelInstance} for that activity part of process or null if it not part of workflow
	 */
	BpmnModelInstance getBpmnModelInstance(Instance activity);

	/**
	 * Gets the bpmn model instance for given process definition id .
	 *
	 * @param processDefinitionId
	 *            the process definition id of some existing process
	 * @return the {@link BpmnModelInstance} for that activity part of process or null if it not part of workflow
	 */
	BpmnModelInstance getBpmnModelInstance(String processDefinitionId);

	/**
	 * Gets the camunda workflow - {@link ProcessInstance} based on the current activity.
	 *
	 * @param activity
	 *            the activity part of some existing process.
	 * @return the {@link ProcessInstance} for the given activity
	 */
	ProcessInstance getProcessInstance(Instance activity);

	/**
	 * Gets the camunda workflow properties - {@link VariableMap} based on the current activity.
	 *
	 * @param processInstance
	 *            the process instance to get variables for
	 * @param all
	 *            - whether to return all properties or only the local
	 * @return the {@link VariableMap} for the given process
	 */
	VariableMap getProcessInstanceVariables(ProcessInstance processInstance, boolean all);

	/**
	 * Collects the set of all active tasks ids for process
	 * 
	 * @param processInstanceId
	 *            the process instance to search for
	 * @return the set of active tasks ids
	 */
	Collection<String> listActiveTasksIds(String processInstanceId);

	/**
	 * Collects the list of all active tasks for process
	 *
	 * @param processInstanceId
	 *            the process instance to search for
	 * @return the list of active tasks
	 */
	Collection<Task> listActiveTasks(String processInstanceId);

	/**
	 * Gets the process instance id for particular activity. If instance is not part of process null is returned
	 *
	 * @param activity
	 *            the activity instance
	 * @return the process instance id by task id or null if not found. Throws runtime exception on invalid
	 *         request/response
	 */
	String getProcessInstanceId(Instance activity);

	/**
	 * Claim task and set current user as task assignee.
	 * 
	 * @param taskInstance
	 *            the current task instance
	 * @param userId
	 *            user who will be new assignee
	 */
	void claimTask(Instance taskInstance, String userId);

	/**
	 * Set current user as new task assignee.
	 *
	 * @param taskInstance
	 *            the current task instance
	 * @param userId
	 *            user who will be new assignee
	 */
	void reassignTask(Instance taskInstance, String userId);

	/**
	 * Removes an assignee of task.
	 * 
	 * @param taskInstance
	 *            the current task instance
	 */
	void releaseTask(Instance taskInstance);

	/**
	 * Check is the task pooled.
	 * 
	 * @param taskInstance
	 *            the current task instance
	 * @return true, if instance is pooled task
	 */
	boolean isTaskPooled(Instance taskInstance);

	/**
	 * Check can task be claimed from passed user id.
	 * 
	 * @param taskInstance
	 *            the current task instance
	 * @param userId
	 *            the current userId
	 * @return true, if instance can be claimed
	 */
	boolean isTaskClaimable(Instance taskInstance, String userId);

	/**
	 * Check can task be released from passed user id.
	 * 
	 * @param taskInstance
	 *            the current task instance
	 * @param userId
	 *            the current user id
	 * @return true, if instance can be released
	 */
	boolean isTaskReleasable(Instance taskInstance, String userId);

	/**
	 * Check can task assignee is equals to passed one.
	 * 
	 * @param taskInstance
	 *            the current task instance
	 * @param userId
	 *            the current user id
	 * @return true, if task assignee is equals to passed one
	 */
	boolean isTaskAssignee(Instance taskInstance, String userId);
}
