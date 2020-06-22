/*
 * Copyright (C) 2005-2011 Alfresco Software Limited. This file is part of Alfresco Alfresco is free
 * software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.workflow.activiti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.repo.workflow.activiti.ActivitiConstants.DEFAULT_TRANSITION_NAME;
import static org.alfresco.repo.workflow.activiti.ActivitiConstants.DEFAULT_TRANSITION_DESCRIPTION;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.Function;

// TODO: Auto-generated Javadoc
/**
 * The Class ActivitiTypeConverter.
 *
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ActivitiTypeConverter {

	/** The Constant TRANSITION_SUFFIX. */
	private static final String TRANSITION_SUFFIX = ".transition";

	/** The Constant DEFAULT_TRANSITION_KEY. */
	private static final String DEFAULT_TRANSITION_KEY = "bpm_businessprocessmodel.transition";

	/** The repo service. */
	private final RepositoryService repoService;

	/** The runtime service. */
	private final RuntimeService runtimeService;

	/** The form service. */
	private final FormService formService;

	/** The history service. */
	private final HistoryService historyService;

	/** The property converter. */
	private final ActivitiPropertyConverter propertyConverter;

	/** The factory. */
	private final WorkflowObjectFactory factory;

	/** The activiti util. */
	private final ActivitiUtil activitiUtil;

	/**
	 * Instantiates a new activiti type converter.
	 *
	 * @param processEngine
	 *            the process engine
	 * @param factory
	 *            the factory
	 * @param propertyConverter
	 *            the property converter
	 */
	public ActivitiTypeConverter(ProcessEngine processEngine, WorkflowObjectFactory factory,
			ActivitiPropertyConverter propertyConverter) {
		repoService = processEngine.getRepositoryService();
		runtimeService = processEngine.getRuntimeService();
		formService = processEngine.getFormService();
		historyService = processEngine.getHistoryService();
		this.factory = factory;
		this.propertyConverter = propertyConverter;
		activitiUtil = new ActivitiUtil(processEngine);
	}

	/**
	 * Filter by domain and convert.
	 *
	 * @param <F>
	 *            the generic type
	 * @param <T>
	 *            the generic type
	 * @param values
	 *            the values
	 * @param processKeyGetter
	 *            the process key getter
	 * @return the list
	 */
	public <F, T> List<T> filterByDomainAndConvert(List<F> values,
			Function<F, String> processKeyGetter) {
		List<F> filtered = factory.filterByDomain(values, processKeyGetter);
		return convert(filtered);
	}

	/**
	 * Convert a {@link Deployment} into a {@link WorkflowDeployment}.
	 *
	 * @param deployment
	 *            the deployment
	 * @return the workflow deployment
	 */
	public WorkflowDeployment convert(Deployment deployment) {
		if (deployment == null) {
			return null;
		}

		List<ProcessDefinition> processDefs = repoService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).list();
		ProcessDefinition processDef = processDefs.get(0);
		WorkflowDefinition wfDef = convert(processDef);
		return factory.createDeployment(wfDef);
	}

	/**
	 * Convert a {@link ProcessDefinition} into a {@link WorkflowDefinition}.
	 *
	 * @param definition
	 *            the definition
	 * @return the workflow definition
	 */
	public WorkflowDefinition convert(ProcessDefinition definition) {
		if (definition == null) {
			return null;
		}

		String defId = definition.getId();
		String defName = definition.getKey();
		int version = definition.getVersion();
		String defaultTitle = definition.getName();

		String startTaskName = null;
		StartFormData startFormData = formService.getStartFormData(definition.getId());
		if (startFormData != null) {
			startTaskName = startFormData.getFormKey();
		}

		ReadOnlyProcessDefinition def = activitiUtil.getDeployedProcessDefinition(defId);
		PvmActivity startEvent = def.getInitial();
		WorkflowTaskDefinition startTask = getTaskDefinition(startEvent, startTaskName,
				definition.getKey(), true);

		return factory.createDefinition(defId, defName, version, defaultTitle, null, startTask);
	}

	/**
	 * Gets the task definition.
	 *
	 * @param activity
	 *            the activity
	 * @param taskFormKey
	 *            the task form key
	 * @param processKey
	 *            the process key
	 * @param isStart
	 *            the is start
	 * @return the task definition
	 */
	public WorkflowTaskDefinition getTaskDefinition(PvmActivity activity, String taskFormKey,
			String processKey, boolean isStart) {
		WorkflowNode node = getNode(activity, processKey, true);
		String taskDefId = taskFormKey == null ? node.getName() : taskFormKey;
		return factory.createTaskDefinition(taskDefId, node, taskFormKey, isStart);
	}

	/**
	 * Gets the default transition.
	 *
	 * @param processDefKey
	 *            the process def key
	 * @param nodeId
	 *            the node id
	 * @return the default transition
	 */
	private WorkflowTransition getDefaultTransition(String processDefKey, String nodeId) {
		String processKey = processDefKey + TRANSITION_SUFFIX;
		String nodeKey = processDefKey + ".node." + nodeId + TRANSITION_SUFFIX;
		String transitionId = DEFAULT_TRANSITION_NAME;
		String title = DEFAULT_TRANSITION_NAME;
		String description = DEFAULT_TRANSITION_DESCRIPTION;
		return factory.createTransition(transitionId, title, description, true, nodeKey,
				processKey, DEFAULT_TRANSITION_KEY);
	}

	/**
	 * Convert.
	 *
	 * @param instance
	 *            the instance
	 * @return the workflow instance
	 */
	public WorkflowInstance convert(ProcessInstance instance) {
		return convertAndSetVariables(instance, (Map<String, Object>) null);
	}

	/**
	 * Convert and set variables.
	 *
	 * @param instance
	 *            the instance
	 * @param collectedvariables
	 *            the collectedvariables
	 * @return the workflow instance
	 */
	public WorkflowInstance convertAndSetVariables(ProcessInstance instance,
			Map<String, Object> collectedvariables) {
		if (instance == null) {
			return null;
		}

		HistoricProcessInstance historicInstance = historyService
				.createHistoricProcessInstanceQuery().processInstanceId(instance.getId())
				.singleResult();

		return convertToInstanceAndSetVariables(historicInstance, collectedvariables);
	}

	/**
	 * Convert.
	 *
	 * @param instance
	 *            the instance
	 * @param collectedvariables
	 *            the collectedvariables
	 * @return the workflow instance
	 */
	public WorkflowInstance convert(HistoricProcessInstance instance,
			Map<String, Object> collectedvariables) {
		if (instance == null) {
			return null;
		}

		HistoricProcessInstance historicInstance = historyService
				.createHistoricProcessInstanceQuery().processInstanceId(instance.getId())
				.singleResult();

		return convertToInstanceAndSetVariables(historicInstance, collectedvariables);
	}

	/**
	 * Convert.
	 *
	 * @param execution
	 *            the execution
	 * @return the workflow path
	 */
	public WorkflowPath convert(Execution execution) {
		String instanceId = execution.getProcessInstanceId();
		ProcessInstance instance = activitiUtil.getProcessInstance(instanceId);
		return convert(execution, instance);
	}

	/**
	 * Convert.
	 *
	 * @param execution
	 *            the execution
	 * @param instance
	 *            the instance
	 * @return the workflow path
	 */
	public WorkflowPath convert(Execution execution, ProcessInstance instance) {
		if (execution == null) {
			return null;
		}

		boolean isActive = !execution.isEnded();

		// Convert workflow and collect variables
		Map<String, Object> workflowInstanceVariables = new HashMap<String, Object>();
		WorkflowInstance wfInstance = convertAndSetVariables(instance, workflowInstanceVariables);

		WorkflowNode node = null;
		// Get active node on execution
		List<String> nodeIds = runtimeService.getActiveActivityIds(execution.getId());

		if ((nodeIds != null) && (nodeIds.size() >= 1)) {
			ReadOnlyProcessDefinition procDef = activitiUtil.getDeployedProcessDefinition(instance
					.getProcessDefinitionId());
			PvmActivity activity = procDef.findActivity(nodeIds.get(0));
			node = convert(activity);
		}

		return factory.createPath(execution.getId(), wfInstance, node, isActive);
	}

	/**
	 * Convert.
	 *
	 * @param activity
	 *            the activity
	 * @param forceIsTaskNode
	 *            the force is task node
	 * @return the workflow node
	 */
	public WorkflowNode convert(PvmActivity activity, boolean forceIsTaskNode) {
		String procDefId = activity.getProcessDefinition().getId();
		String key = activitiUtil.getProcessDefinition(procDefId).getKey();
		return getNode(activity, key, forceIsTaskNode);
	}

	/**
	 * Gets the node.
	 *
	 * @param activity
	 *            the activity
	 * @param key
	 *            the key
	 * @param forceIsTaskNode
	 *            the force is task node
	 * @return the node
	 */
	private WorkflowNode getNode(PvmActivity activity, String key, boolean forceIsTaskNode) {
		String name = activity.getId();
		String defaultTitle = (String) activity.getProperty(ActivitiConstants.NODE_NAME);
		String defaultDescription = (String) activity
				.getProperty(ActivitiConstants.NODE_DESCRIPTION);
		String type = (String) activity.getProperty(ActivitiConstants.NODE_TYPE);
		boolean isTaskNode = forceIsTaskNode || ActivitiConstants.USER_TASK_NODE_TYPE.equals(type);

		if (defaultTitle == null) {
			defaultTitle = name;
		}
		if (defaultDescription == null) {
			defaultDescription = name;
		}
		WorkflowTransition transition = getDefaultTransition(key, name);
		return factory.createNode(name, key, defaultTitle, defaultDescription, type, isTaskNode,
				transition);
	}

	/**
	 * Convert.
	 *
	 * @param activity
	 *            the activity
	 * @return the workflow node
	 */
	public WorkflowNode convert(PvmActivity activity) {
		return convert(activity, false);
	}

	/**
	 * Convert execution.
	 *
	 * @param executions
	 *            the executions
	 * @return the list
	 */
	public List<WorkflowPath> convertExecution(List<Execution> executions) {
		ArrayList<WorkflowPath> results = new ArrayList<WorkflowPath>(executions.size());
		for (Execution execution : executions) {
			results.add(convert(execution));
		}
		return results;
	}

	/**
	 * Convert.
	 *
	 * @param <T>
	 *            the generic type
	 * @param inputs
	 *            the inputs
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> convert(List<?> inputs) {
		ArrayList<T> results = new ArrayList<T>(inputs.size());
		for (Object in : inputs) {
			T out = (T) convert(in);
			if (out != null) {
				results.add(out);
			}
		}
		return results;
	}

	/**
	 * Converts an Activiti object to an Alresco Workflow object. Determines the exact conversion
	 * method to use by checking the class of object.
	 *
	 * @param obj
	 *            The object to be converted.
	 * @return the converted object.
	 */
	private Object convert(Object obj) {
		if (obj == null) {
			return null;
		}

		if (obj instanceof Deployment) {
			return convert((Deployment) obj);
		}
		if (obj instanceof ProcessDefinition) {
			return convert((ProcessDefinition) obj);
		}
		if (obj instanceof ProcessInstance) {
			return convert((ProcessInstance) obj);
		}
		if (obj instanceof Execution) {
			return convert((Execution) obj);
		}
		if (obj instanceof ActivityImpl) {
			return convert((ActivityImpl) obj);
		}
		if (obj instanceof Task) {
			return convert((Task) obj);
		}
		if (obj instanceof HistoricTaskInstance) {
			return convert((HistoricTaskInstance) obj);
		}
		if (obj instanceof HistoricProcessInstance) {
			return convert((HistoricProcessInstance) obj);
		}
		throw new WorkflowException("Cannot convert object: " + obj + " of type: " + obj.getClass());
	}

	/**
	 * Convert.
	 *
	 * @param task
	 *            the task
	 * @return the workflow task
	 */
	public WorkflowTask convert(Task task) {
		if (task == null) {
			return null;
		}
		if (task.getExecutionId() == null) {
			return convertStandalone(task);
		}
		String id = task.getId();
		String defaultTitle = task.getName();
		String defaultDescription = task.getDescription();

		WorkflowTaskState state = WorkflowTaskState.IN_PROGRESS;
		Execution execution = activitiUtil.getExecution(task.getExecutionId());
		WorkflowPath path = convert(execution);

		// Since the task is active, it's safe to use the active node on
		// the execution path
		WorkflowNode node = path.getNode();

		TaskFormData taskFormData = formService.getTaskFormData(task.getId());
		String taskDefId = null;
		if (taskFormData != null) {
			taskDefId = taskFormData.getFormKey();
		}
		WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, node, taskDefId,
				false);

		// All task-properties should be fetched, not only local
		Map<QName, Serializable> properties = propertyConverter.getTaskProperties(task);

		return factory.createTask(id, taskDef, taskDef.getId(), defaultTitle, defaultDescription,
				state, path, properties);
	}

	/**
	 * Convert standalone.
	 *
	 * @param task
	 *            the task
	 * @return the workflow task
	 */
	public WorkflowTask convertStandalone(Task task) {
		if (task == null) {
			return null;
		}
		String id = task.getId();
		String defaultName = task.getName();
		String defaultTitle = defaultName;
		String defaultDescription = task.getDescription();

		String taskDefId = task.getName();
		if (task.getProcessInstanceId() == null) {
			taskDefId = task.getTaskDefinitionKey();
		}
		if (defaultName == null) {
			defaultName = taskDefId;
		}
		WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, null, taskDefId,
				false);

		// All task-properties should be fetched, not only local
		Map<QName, Serializable> properties = propertyConverter
				.collectPropertiesForStandaloneTask(task);
		if (properties.get(ContentModel.PROP_TITLE) != null) {
			defaultTitle = properties.get(ContentModel.PROP_TITLE).toString();
		}
		if (properties.get(ContentModel.PROP_DESCRIPTION) != null) {
			defaultDescription = DefaultTypeConverter.INSTANCE.convert(String.class,
					properties.get(ContentModel.PROP_DESCRIPTION));
		}
		return factory.createTask(id, taskDef, defaultName, defaultTitle, defaultDescription,
				WorkflowTaskState.IN_PROGRESS, "standaloneTask", properties);
	}

	/**
	 * Gets the virtual start task.
	 *
	 * @param processInstanceId
	 *            the process instance id
	 * @param inProgress
	 *            the in progress
	 * @return the virtual start task
	 */
	public WorkflowTask getVirtualStartTask(String processInstanceId, Boolean inProgress) {
		ProcessInstance processInstance = activitiUtil.getProcessInstance(processInstanceId);
		if (processInstance != null) {
			if (null == inProgress) {
				inProgress = isStartTaskActive(processInstanceId);
			}
			return getVirtualStartTask(processInstance, inProgress);
		}
		HistoricProcessInstance historicProcessInstance = activitiUtil
				.getHistoricProcessInstance(processInstanceId);
		return getVirtualStartTask(historicProcessInstance);
	}

	/**
	 * Checks if is start task active.
	 *
	 * @param processInstanceId
	 *            the process instance id
	 * @return true, if is start task active
	 */
	public boolean isStartTaskActive(String processInstanceId) {
		Object endDate = runtimeService.getVariable(processInstanceId,
				ActivitiConstants.PROP_START_TASK_END_DATE);
		return endDate == null;
	}

	/**
	 * Gets the virtual start task.
	 *
	 * @param processInstance
	 *            the process instance
	 * @param inProgress
	 *            the in progress
	 * @return the virtual start task
	 */
	private WorkflowTask getVirtualStartTask(ProcessInstance processInstance, boolean inProgress) {
		String processInstanceId = processInstance.getId();
		String id = ActivitiConstants.START_TASK_PREFIX + processInstanceId;

		WorkflowTaskState state = null;
		if (inProgress) {
			state = WorkflowTaskState.IN_PROGRESS;
		} else {
			state = WorkflowTaskState.COMPLETED;
		}

		WorkflowPath path = convert((Execution) processInstance);

		// Convert start-event to start-task Node
		ReadOnlyProcessDefinition procDef = activitiUtil
				.getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
		WorkflowNode startNode = convert(procDef.getInitial(), true);

		StartFormData startFormData = formService.getStartFormData(processInstance
				.getProcessDefinitionId());
		String taskDefId = null;
		if (startFormData != null) {
			taskDefId = startFormData.getFormKey();
		}
		WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, startNode,
				taskDefId, true);

		// Add properties based on HistoricProcessInstance
		HistoricProcessInstance historicProcessInstance = historyService
				.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId())
				.singleResult();

		Map<QName, Serializable> properties = propertyConverter.getStartTaskProperties(
				historicProcessInstance, taskDefId, !inProgress);

		// TODO: Figure out what name/description should be used for the start-task, start event's
		// name?
		String defaultTitle = null;
		String defaultDescription = null;

		return factory.createTask(id, taskDef, taskDef.getId(), defaultTitle, defaultDescription,
				state, path, properties);
	}

	/**
	 * Gets the virtual start task.
	 *
	 * @param historicProcessInstance
	 *            the historic process instance
	 * @return the virtual start task
	 */
	private WorkflowTask getVirtualStartTask(HistoricProcessInstance historicProcessInstance) {
		if (historicProcessInstance == null) {
			return null;
		}

		String processInstanceId = historicProcessInstance.getId();
		String id = ActivitiConstants.START_TASK_PREFIX + processInstanceId;

		// Since the process instance is complete the Start Task must be complete!
		WorkflowTaskState state = WorkflowTaskState.COMPLETED;

		// We use the process-instance ID as execution-id. It's ended anyway
		WorkflowPath path = buildCompletedPath(processInstanceId, processInstanceId);
		if (path == null) {
			return null;
		}

		// Convert start-event to start-task Node
		ReadOnlyProcessDefinition procDef = activitiUtil
				.getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());
		WorkflowNode startNode = convert(procDef.getInitial(), true);

		String taskDefId = activitiUtil.getStartFormKey(historicProcessInstance
				.getProcessDefinitionId());
		WorkflowTaskDefinition taskDef = factory.createTaskDefinition(taskDefId, startNode,
				taskDefId, true);

		boolean completed = historicProcessInstance.getEndTime() != null;
		Map<QName, Serializable> properties = propertyConverter.getStartTaskProperties(
				historicProcessInstance, taskDefId, completed);

		// TODO: Figure out what name/description should be used for the start-task, start event's
		// name?
		String defaultTitle = null;
		String defaultDescription = null;

		return factory.createTask(id, taskDef, taskDef.getId(), defaultTitle, defaultDescription,
				state, path, properties);
	}

	/**
	 * Convert.
	 *
	 * @param historicTaskInstance
	 *            the historic task instance
	 * @return the workflow task
	 */
	public WorkflowTask convert(HistoricTaskInstance historicTaskInstance) {
		if (historicTaskInstance == null) {
			return null;
		}
		WorkflowPath path = null;
		if (historicTaskInstance.getExecutionId() == null) {
			return convertStandalone(historicTaskInstance);
		}
		// Check to see if the instance is still running
		Execution execution = activitiUtil.getExecution(historicTaskInstance.getExecutionId());

		if (execution != null) {
			// Process execution still running
			path = convert(execution);
		} else {
			// Process execution is historic
			path = buildCompletedPath(historicTaskInstance.getExecutionId(),
					historicTaskInstance.getProcessInstanceId());
		}

		if (path == null) {
			// When path is null, workflow is deleted or cancelled. Task should
			// not be used
			return null;
		}

		// Extract node from historic task
		WorkflowNode node = buildHistoricTaskWorkflowNode(historicTaskInstance);

		WorkflowTaskState state = WorkflowTaskState.COMPLETED;

		String taskId = historicTaskInstance.getId();
		// Get the local task variables from the history
		Map<String, Object> variables = propertyConverter.getHistoricTaskVariables(taskId);
		Map<QName, Serializable> historicTaskProperties = propertyConverter.getTaskProperties(
				historicTaskInstance, variables);

		// Get task definition from historic variable
		String formKey = (String) variables.get(ActivitiConstants.PROP_TASK_FORM_KEY);
		WorkflowTaskDefinition taskDef = factory
				.createTaskDefinition(formKey, node, formKey, false);
		String title = historicTaskInstance.getName();
		String description = historicTaskInstance.getDescription();
		String taskName = taskDef.getId();

		return factory.createTask(taskId, taskDef, taskName, title, description, state, path,
				historicTaskProperties);
	}

	/**
	 * Convert standalone.
	 *
	 * @param historicTaskInstance
	 *            the historic task instance
	 * @return the workflow task
	 */
	public WorkflowTask convertStandalone(HistoricTaskInstance historicTaskInstance) {
		if (historicTaskInstance == null) {
			return null;
		}

		WorkflowTaskState state = WorkflowTaskState.COMPLETED;

		String taskId = historicTaskInstance.getId();
		// Get the local task variables from the history
		Map<QName, Serializable> historicTaskProperties = propertyConverter
				.collectPropertiesForStandaloneTask(historicTaskInstance);
		// Get task definition from historic variable
		String formKey = historicTaskInstance.getTaskDefinitionKey();
		WorkflowTaskDefinition taskDef = factory
				.createTaskDefinition(formKey, null, formKey, false);
		String title = historicTaskInstance.getName();
		String description = historicTaskInstance.getDescription();
		String taskName = taskDef.getId();

		return factory.createTask(taskId, taskDef, taskName, title, description, state,
				"standaloneTask", historicTaskProperties);
	}

	/**
	 * Builds the historic task workflow node.
	 *
	 * @param historicTaskInstance
	 *            the historic task instance
	 * @return the workflow node
	 */
	private WorkflowNode buildHistoricTaskWorkflowNode(HistoricTaskInstance historicTaskInstance) {
		ReadOnlyProcessDefinition procDef = activitiUtil
				.getDeployedProcessDefinition(historicTaskInstance.getProcessDefinitionId());
		PvmActivity taskActivity = procDef
				.findActivity(historicTaskInstance.getTaskDefinitionKey());
		return convert(taskActivity);
	}

	/**
	 * Builds the completed path.
	 *
	 * @param executionId
	 *            the execution id
	 * @param processInstanceId
	 *            the process instance id
	 * @return the workflow path
	 */
	public WorkflowPath buildCompletedPath(String executionId, String processInstanceId) {
		WorkflowInstance wfInstance = null;
		ProcessInstance processInstance = activitiUtil.getProcessInstance(processInstanceId);
		if (processInstance != null) {
			wfInstance = convert(processInstance);
		} else {
			HistoricProcessInstance historicProcessInstance = activitiUtil
					.getHistoricProcessInstance(processInstanceId);
			if (historicProcessInstance != null) {
				wfInstance = convert(historicProcessInstance);
			}
		}
		if (wfInstance == null) {
			// When workflow is cancelled or deleted, WorkflowPath should not be returned
			return null;
		}
		WorkflowNode node = null;
		return factory.createPath(executionId, wfInstance, node, false);
	}

	/**
	 * Convert to instance and set variables.
	 *
	 * @param historicProcessInstance
	 *            the historic process instance
	 * @param collectedVariables
	 *            the collected variables
	 * @return the workflow instance
	 */
	public WorkflowInstance convertToInstanceAndSetVariables(
			HistoricProcessInstance historicProcessInstance, Map<String, Object> collectedVariables) {
		String processInstanceId = historicProcessInstance.getId();
		String id = processInstanceId;
		ProcessDefinition procDef = activitiUtil.getProcessDefinition(historicProcessInstance
				.getProcessDefinitionId());
		WorkflowDefinition definition = convert(procDef);

		// Set process variables based on historic detail query
		Map<String, Object> variables = propertyConverter
				.getHistoricProcessVariables(processInstanceId);

		Date startDate = historicProcessInstance.getStartTime();
		Date endDate = historicProcessInstance.getEndTime();

		// Copy all variables to map, if not null
		if (collectedVariables != null) {
			collectedVariables.putAll(variables);
		}
		boolean isActive = endDate == null;
		return factory.createInstance(id, definition, variables, isActive, startDate, endDate);
	}

	/**
	 * Convert.
	 *
	 * @param historicProcessInstance
	 *            the historic process instance
	 * @return the workflow instance
	 */
	public WorkflowInstance convert(HistoricProcessInstance historicProcessInstance) {
		return convertToInstanceAndSetVariables(historicProcessInstance, null);
	}

}
