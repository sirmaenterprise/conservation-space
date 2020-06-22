package com.sirmaenterprise.sep.bpm.bpmn;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 * The {@link ProcessService} is the business logic service that executes all operation to underlying process engine.
 *
 * @author Borislav Banchev
 */
public interface ProcessService {

	/**
	 * Start a corresponding process for given instance and returns the updated instance. The operation has the
	 * following characteristics:<br>
	 * <strong> Operation does update the provided process instance in SEIP with additional metadata wihtout saving
	 * it.<br>
	 * Operation might trigger creation of additional process activities in SEIP.<br>
	 * Single BPMN process in the process engine is related to the provided workflow instance.<br>
	 * </strong>
	 *
	 * @param process
	 *            the workflow instance to start process on.
	 * @param transitionData
	 *            is the data to be transformed and transfered to the next activities. Data is in format:
	 *            acitivityId=map of properties
	 * @return the updated workflow instance with the process engine metadata, or null if no workflow is started.
	 * @throws BPMException
	 *             on any error during process starting
	 */
	Instance startProcess(Instance process, Map<String, Serializable> transitionData) throws BPMException;


	/**
	 * Start a corresponding process for given message id and returns the new created process instance. The operation has the
	 * following characteristics:<br>
	 * <strong> Operation creates process instance in SEP.<br>
	 * Single BPMN process is created in the Camunda process engine which is related to the newly process instance. <br>
	 * Those BPMN Process will eventually trigger creation of additional SEP activity instances.<br>
	 * </strong>
	 *
	 * @param messageId
	 *            the workflow message start event id to signal and start process on
	 * @param parentId the new workflow parent instance id
	 * @param transitionData
	 *            is the data to be transformed and transfered to the next activities. Data is in format:
	 *            acitivityId=map of properties
	 * @return the updated workflow instance with the process engine metadata, or null if no workflow is started.
	 * @throws BPMException
	 *             on any error during process starting
	 */
	Instance startProcess(String messageId, String parentId, Map<String, Serializable> transitionData) throws BPMException;

	/**
	 * Cancel a process related to given instance and returns the updated instance. The operation has the following
	 * characteristics:<br>
	 * <strong> Operation does nothing to the provided process instance in SEIP.<br>
	 * Operation might trigger cancellation of related to the process activities in SEIP.<br>
	 * BPMN process in the process engine should be soft deleted and later should be possible to be retrieved from
	 * history.<br>
	 * </strong>
	 * 
	 * @param process
	 *            the workflow instance to cancel process for.
	 * @return the updated workflow instance with the process engine metadata
	 * @throws BPMException
	 *             on any error during process starting
	 */
	Instance cancelProcess(Instance process) throws BPMException;

	/**
	 * Invokes an operation on activity instance that is a possible transition.The operation has the following
	 * characteristics:<br>
	 * <strong> Operation does not update the provided activity instance in SEIP.<br>
	 * Operation might trigger creation or/and update of additional process activities in SEIP.<br>
	 * Operation uses the 'operation' to map a transition for execution - the outcome.<br>
	 * </strong>
	 *
	 * @param activity
	 *            any instance part of process.
	 * @param operation
	 *            is one of the operations contained in the transition matrix of the instance
	 * @param activityData
	 *            is the data to be transformed and transfered to the completed activity
	 * @param transitionData
	 *            is the data to be transformed and transfered to the next activities. Data is in format:
	 *            acitivityId=map of properties
	 * @return the processed activity if operation is executed in the process engine or null if no operation is executed
	 */
	Instance transition(Instance activity, String operation, Map<String, Serializable> activityData,
			Map<String, Serializable> transitionData);

	/**
	 * Sends a notification to workflow based on event name (Signal or Message Event Name).
	 *
	 * @param eventName is the name used in camunda signal or message event
	 * @param processVariables are the variables to add to execution
	 */
	void notify(String eventName, Map<String,Object> processVariables);

	/**
	 * Sends a notification to workflow based on event name (Signal or Message Event Name).
	 *
	 * @param eventName is the name used in camunda signal or message event
	 * @param executionId is the current workflow execution id
	 * @param processVariables are the variables to add to execution
	 */
	void notify(String eventName, String executionId, Map<String,Object> processVariables);

}