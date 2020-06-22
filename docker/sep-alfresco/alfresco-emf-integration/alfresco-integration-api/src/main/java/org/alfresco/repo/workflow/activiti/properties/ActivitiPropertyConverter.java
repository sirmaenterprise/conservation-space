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

package org.alfresco.repo.workflow.activiti.properties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.workflow.WorkflowAuthorityManager;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowNodeConverter;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.WorkflowPropertyHandlerRegistry;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiTaskTypeManager;
import org.alfresco.repo.workflow.activiti.ActivitiUtil;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.EntryTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class ActivitiPropertyConverter.
 *
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiPropertyConverter {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ActivitiPropertyConverter.class);

	/** The Constant ERR_CONVERT_VALUE. */
	private static final String ERR_CONVERT_VALUE = "activiti.engine.convert.value.error";

	/** The Constant ERR_SET_TASK_PROPS_INVALID_VALUE. */
	private static final String ERR_SET_TASK_PROPS_INVALID_VALUE = "activiti.engine.set.task.properties.invalid.value";

	/** The Constant ERR_MANDATORY_TASK_PROPERTIES_MISSING. */
	private static final String ERR_MANDATORY_TASK_PROPERTIES_MISSING = "activiti.engine.mandatory.properties.missing";

	/** The type manager. */
	private final ActivitiTaskTypeManager typeManager;

	/** The message service. */
	private final MessageService messageService;

	/** The factory. */
	private final WorkflowObjectFactory factory;

	/** The authority manager. */
	private final WorkflowAuthorityManager authorityManager;

	/** The handler registry. */
	private final WorkflowPropertyHandlerRegistry handlerRegistry;

	/** The node converter. */
	private final WorkflowNodeConverter nodeConverter;

	/** The activiti util. */
	private final ActivitiUtil activitiUtil;

	/**
	 * Instantiates a new activiti property converter.
	 *
	 * @param activitiUtil
	 *            the activiti util
	 * @param factory
	 *            the factory
	 * @param handlerRegistry
	 *            the handler registry
	 * @param authorityManager
	 *            the authority manager
	 * @param messageService
	 *            the message service
	 * @param nodeConverter
	 *            the node converter
	 */
	public ActivitiPropertyConverter(ActivitiUtil activitiUtil, WorkflowObjectFactory factory,
			WorkflowPropertyHandlerRegistry handlerRegistry,
			WorkflowAuthorityManager authorityManager, MessageService messageService,
			WorkflowNodeConverter nodeConverter) {
		this.activitiUtil = activitiUtil;
		this.factory = factory;
		this.handlerRegistry = handlerRegistry;
		this.authorityManager = authorityManager;
		this.messageService = messageService;
		this.nodeConverter = nodeConverter;
		this.typeManager = new ActivitiTaskTypeManager(factory, activitiUtil.getFormService());
	}

	/**
	 * Gets the task properties.
	 *
	 * @param task
	 *            the task
	 * @return the task properties
	 */
	public Map<QName, Serializable> getTaskProperties(Task task) {
		if (task.getExecutionId() == null) {
			return collectPropertiesForStandaloneTask(task);
		}
		// retrieve type definition for task
		TypeDefinition taskDef = typeManager.getFullTaskDefinition(task);
		Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
		Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();

		TaskService taskService = activitiUtil.getTaskService();
		// Get all task variables including execution vars.
		Map<String, Object> variables = taskService.getVariables(task.getId());
		Map<String, Object> localVariables = taskService.getVariablesLocal(task.getId());

		// Map the arbitrary properties
		Map<QName, Serializable> properties = mapArbitraryProperties(variables, localVariables,
				taskProperties, taskAssociations);

		// Map activiti task instance fields to properties
		properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
		properties.put(WorkflowModel.PROP_DESCRIPTION, task.getDescription());

		// Since the task is never started explicitally, we use the create time
		properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());

		// Due date is present on the task
		properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());

		// Since this is a runtime-task, it's not completed yet
		properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
		properties.put(WorkflowModel.PROP_PRIORITY, task.getPriority());
		properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
		properties.put(ContentModel.PROP_OWNER, task.getAssignee());

		// Be sure to fetch the outcome
		String outcomeVarName = factory.mapQNameToName(WorkflowModel.PROP_OUTCOME);
		if (variables.get(outcomeVarName) != null) {
			properties
					.put(WorkflowModel.PROP_OUTCOME, (Serializable) variables.get(outcomeVarName));
		}

		List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
		mapPooledActors(links, properties);

		return filterTaskProperties(properties);
	}

	/**
	 * Gets the path properties.
	 *
	 * @param executionId
	 *            the execution id
	 * @return the path properties
	 */
	public Map<QName, Serializable> getPathProperties(String executionId) {
		Map<String, Object> variables = activitiUtil.getExecutionVariables(executionId);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>(variables.size());
		for (Entry<String, Object> entry : variables.entrySet()) {
			QName qNameKey = factory.mapNameToQName(entry.getKey());
			Serializable value = convertPropertyValue(entry.getValue());
			properties.put(qNameKey, value);
		}
		return properties;
	}

	/**
	 * Gets the pooled actors reference.
	 *
	 * @param links
	 *            the links
	 * @return the pooled actors reference
	 */
	public List<NodeRef> getPooledActorsReference(Collection<IdentityLink> links) {
		List<NodeRef> pooledActorRefs = new LinkedList<NodeRef>();
		if (links != null) {
			for (IdentityLink link : links) {
				if (IdentityLinkType.CANDIDATE.equals(link.getType())) {
					String id = link.getGroupId();
					if (id == null) {
						id = link.getUserId();
					}
					NodeRef pooledNodeRef = authorityManager.mapNameToAuthority(id);
					if (pooledNodeRef != null&&!pooledActorRefs.contains(pooledNodeRef)) {
						pooledActorRefs.add(pooledNodeRef);
					}
				}
			}
		}
		return pooledActorRefs;
	}

	/**
	 * Map pooled actors.
	 *
	 * @param links
	 *            the links
	 * @param properties
	 *            the properties
	 */
	private void mapPooledActors(Collection<IdentityLink> links, Map<QName, Serializable> properties) {
		Collection<NodeRef> pooledActorRefs = getPooledActorsReference(links);
		if (pooledActorRefs != null) {
			properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable) pooledActorRefs);
		}
	}

	/**
	 * Gets the task properties.
	 *
	 * @param task
	 *            the task
	 * @param typeDefinition
	 *            the type definition
	 * @param localOnly
	 *            the local only
	 * @return the task properties
	 */
	public Map<QName, Serializable> getTaskProperties(DelegateTask task,
			TypeDefinition typeDefinition, boolean localOnly) {
		Map<QName, PropertyDefinition> taskProperties = typeDefinition.getProperties();
		Map<QName, AssociationDefinition> taskAssociations = typeDefinition.getAssociations();

		// Get the local task variables
		Map<String, Object> localVariables = task.getVariablesLocal();
		Map<String, Object> variables = null;

		if (localOnly == false) {
			variables = new HashMap<String, Object>();
			variables.putAll(localVariables);

			// Execution-variables should also be added, if no value is present locally
			Map<String, Object> executionVariables = task.getExecution().getVariables();

			for (Entry<String, Object> entry : executionVariables.entrySet()) {
				String key = entry.getKey();
				if (localVariables.containsKey(key) == false) {
					variables.put(key, entry.getValue());
				}
			}
		} else {
			// Only local variables should be used.
			variables = localVariables;
		}
		// Map the arbitrary properties
		Map<QName, Serializable> properties = mapArbitraryProperties(variables, localVariables,
				taskProperties, taskAssociations);

		// Map activiti task instance fields to properties
		properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
		// Since the task is never started explicitally, we use the create time
		properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());

		// Due date
		properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());

		// Since this is a runtime-task, it's not completed yet
		properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
		properties.put(WorkflowModel.PROP_PRIORITY, task.getPriority());
		properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
		properties.put(ContentModel.PROP_OWNER, task.getAssignee());

		// TODO: Expose in DelegateTask
		Set<IdentityLink> links = ((TaskEntity) task).getCandidates();
		mapPooledActors(links, properties);

		return filterTaskProperties(properties);
	}

	/**
	 * Gets the task properties.
	 *
	 * @param historicTask
	 *            the historic task
	 * @param localVariables
	 *            the local variables
	 * @return the task properties
	 */
	@SuppressWarnings("unchecked")
	public Map<QName, Serializable> getTaskProperties(HistoricTaskInstance historicTask,
			Map<String, Object> localVariables) {
		// Retrieve type definition for task, based on taskFormKey variable
		String formKey = (String) localVariables.get(ActivitiConstants.PROP_TASK_FORM_KEY);
		TypeDefinition taskDef = typeManager.getFullTaskDefinition(formKey);

		Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
		Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();

		Map<String, Object> allVariables = getHistoricProcessVariables(historicTask
				.getProcessInstanceId());
		allVariables.putAll(localVariables);

		// Map the arbitrary properties
		Map<QName, Serializable> properties = mapArbitraryProperties(allVariables, localVariables,
				taskProperties, taskAssociations);

		// Map activiti task instance fields to properties
		properties.put(WorkflowModel.PROP_TASK_ID, historicTask.getId());
		properties.put(WorkflowModel.PROP_DESCRIPTION, historicTask.getDescription());

		// Since the task is never started explicitly, we use the create time
		properties.put(WorkflowModel.PROP_START_DATE, historicTask.getStartTime());

		properties.put(WorkflowModel.PROP_DUE_DATE, historicTask.getDueDate());
		properties.put(WorkflowModel.PROP_COMPLETION_DATE, historicTask.getEndTime());

		properties.put(WorkflowModel.PROP_PRIORITY, historicTask.getPriority());

		properties.put(ContentModel.PROP_CREATED, historicTask.getStartTime());
		properties.put(ContentModel.PROP_OWNER, historicTask.getAssignee());

		// Be sure to fetch the outcome
		String outcomeVarName = factory.mapQNameToName(WorkflowModel.PROP_OUTCOME);
		if (localVariables.get(outcomeVarName) != null) {
			properties.put(WorkflowModel.PROP_OUTCOME,
					(Serializable) localVariables.get(outcomeVarName));
		}

		// History of pooled actors is stored in task variable
		List<NodeRef> pooledActors = new ArrayList<NodeRef>();
		List<String> pooledActorRefIds = (List<String>) localVariables
				.get(ActivitiConstants.PROP_POOLED_ACTORS_HISTORY);
		if (pooledActorRefIds != null) {
			for (String nodeId : pooledActorRefIds) {
				pooledActors.add(new NodeRef(nodeId));
			}
		}
		// Add pooled actors. When no actors are found, set empty list
		properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable) pooledActors);

		return filterTaskProperties(properties);
	}

	// /**
	// * Sets all default workflow properties that should be set based on the given taskproperties.
	// * When the currentValues already contains a value for a certain key, this value is retained
	// * and the value in taskProperties is ignored.
	// *
	// * @param startTask
	// * start task instance
	// */
	// public void setDefaultWorkflowProperties(Map<String, Object> currentValues, Map<QName,
	// Serializable> taskProperties)
	// {
	// if(taskProperties != null)
	// {
	// String workflowDescriptionName =
	// factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
	// if (!currentValues.containsKey(workflowDescriptionName))
	// {
	// currentValues.put(workflowDescriptionName, taskProperties
	// .get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION));
	// }
	// String workflowDueDateName = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DUE_DATE);
	// if (!currentValues.containsKey(workflowDueDateName))
	// {
	// currentValues.put(workflowDueDateName,
	// taskProperties.get(WorkflowModel.PROP_WORKFLOW_DUE_DATE));
	// }
	// String workflowPriorityName = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_PRIORITY);
	// if (!currentValues.containsKey(workflowPriorityName))
	// {
	// currentValues.put(workflowPriorityName,
	// taskProperties.get(WorkflowModel.PROP_WORKFLOW_PRIORITY));
	// }
	// String workflowContextName = factory.mapQNameToName(WorkflowModel.PROP_CONTEXT);
	// if (!currentValues.containsKey(workflowContextName))
	// {
	// Serializable contextRef = taskProperties.get(WorkflowModel.PROP_CONTEXT);
	// currentValues.put(workflowContextName, convertNodeRefs(false, contextRef));
	// }
	// String pckgName = factory.mapQNameToName(WorkflowModel.ASSOC_PACKAGE);
	// if (!currentValues.containsKey(pckgName))
	// {
	// Serializable pckgNode = taskProperties.get(WorkflowModel.ASSOC_PACKAGE);
	// currentValues.put(pckgName, convertNodeRefs(false, pckgNode));
	// }
	// }
	// }

	/**
	 * Sets Default Properties of Task.
	 *
	 * @param task
	 *            the new default task properties
	 */
	public void setDefaultTaskProperties(DelegateTask task) {
		TypeDefinition typeDefinition = typeManager.getFullTaskDefinition(task);
		// Only local task properties should be set to default value
		Map<QName, Serializable> existingValues = getTaskProperties(task, typeDefinition, true);
		Map<QName, Serializable> defaultValues = new HashMap<QName, Serializable>();

		Map<QName, PropertyDefinition> propertyDefs = typeDefinition.getProperties();

		// for each property, determine if it has a default value
		for (Map.Entry<QName, PropertyDefinition> entry : propertyDefs.entrySet()) {
			QName key = entry.getKey();
			String defaultValue = entry.getValue().getDefaultValue();
			if (defaultValue != null && existingValues.get(key) == null) {
				defaultValues.put(key, defaultValue);
			}
		}

		// Special case for property priorities
		PropertyDefinition priorDef = propertyDefs.get(WorkflowModel.PROP_PRIORITY);
		Serializable existingValue = existingValues.get(WorkflowModel.PROP_PRIORITY);
		try {
			if (priorDef != null) {
				for (ConstraintDefinition constraintDef : priorDef.getConstraints()) {
					constraintDef.getConstraint().evaluate(existingValue);
				}
			}
		} catch (ConstraintException ce) {
			Integer defaultVal = Integer.valueOf(priorDef.getDefaultValue());
			if (logger.isDebugEnabled()) {
				logger.debug("Task priority value (" + existingValue
						+ ") was invalid so it was set to the default value of " + defaultVal
						+ ". Task:" + task.getName());
			}
			defaultValues.put(WorkflowModel.PROP_PRIORITY, defaultVal);
		}

		// Special case for task description default value
		String description = (String) existingValues.get(WorkflowModel.PROP_DESCRIPTION);
		if (description == null || description.length() == 0) {
			// Try the localised task description first
			String processDefinitionKey = ((ProcessDefinition) ((TaskEntity) task).getExecution()
					.getProcessDefinition()).getKey();
			description = factory.getTaskDescription(typeDefinition,
					factory.buildGlobalId(processDefinitionKey), null, task.getTaskDefinitionKey());
			if (description != null && description.length() > 0) {
				defaultValues.put(WorkflowModel.PROP_DESCRIPTION, description);
			} else {
				String descriptionKey = factory
						.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
				description = (String) task.getExecution().getVariable(descriptionKey);
				if (description != null && description.length() > 0) {
					defaultValues.put(WorkflowModel.PROP_DESCRIPTION, description);
				} else {
					defaultValues.put(WorkflowModel.PROP_DESCRIPTION, task.getName());
				}
			}

		}

		// Assign the default values to the task
		if (defaultValues.size() > 0) {
			setTaskProperties(task, defaultValues);
		}
	}

	/**
	 * Gets the start task properties.
	 *
	 * @param historicProcessInstance
	 *            the historic process instance
	 * @param taskDefId
	 *            the task def id
	 * @param completed
	 *            the completed
	 * @return the start task properties
	 */
	public Map<QName, Serializable> getStartTaskProperties(
			HistoricProcessInstance historicProcessInstance, String taskDefId, boolean completed) {
		TypeDefinition taskDef = typeManager.getStartTaskDefinition(taskDefId);
		Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
		Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();

		Map<String, Object> variables = getStartVariables(historicProcessInstance);

		// Map all the properties
		Map<QName, Serializable> properties = mapArbitraryProperties(variables, variables,
				taskProperties, taskAssociations);

		// Map activiti task instance fields to properties
		properties.put(WorkflowModel.PROP_TASK_ID, ActivitiConstants.START_TASK_PREFIX
				+ historicProcessInstance.getId());

		properties.put(WorkflowModel.PROP_START_DATE, historicProcessInstance.getStartTime());

		// Use workflow due-date at the time of starting the process
		String wfDueDateKey = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_DUE_DATE);
		String dueDateKey = factory.mapQNameToName(WorkflowModel.PROP_DUE_DATE);
		Serializable dueDate = (Serializable) variables.get(wfDueDateKey);
		if (dueDate == null) {
			dueDate = (Serializable) variables.get(dueDateKey);
		}
		properties.put(WorkflowModel.PROP_DUE_DATE, dueDate);

		// TODO: is it okay to use start-date?
		properties.put(WorkflowModel.PROP_COMPLETION_DATE, historicProcessInstance.getStartTime());

		// Use workflow priority at the time of starting the process
		String priorityKey = factory.mapQNameToName(WorkflowModel.PROP_PRIORITY);
		Serializable priority = (Serializable) variables.get(priorityKey);
		if (priority == null) {
			String wfPriorityKey = factory.mapQNameToName(WorkflowModel.PROP_WORKFLOW_PRIORITY);
			priority = (Serializable) variables.get(wfPriorityKey);
		}
		properties.put(WorkflowModel.PROP_PRIORITY, priority);

		properties.put(ContentModel.PROP_CREATED, historicProcessInstance.getStartTime());

		// Use initiator username as owner
		ActivitiScriptNode ownerNode = (ActivitiScriptNode) variables
				.get(WorkflowConstants.PROP_INITIATOR);
		if (ownerNode != null && ownerNode.exists()) {
			properties.put(ContentModel.PROP_OWNER,
					(Serializable) ownerNode.getProperties().get("userName"));
		}

		if (completed) {
			// Override default 'Not Yet Started' when start-task is completed
			properties.put(WorkflowModel.PROP_STATUS, WorkflowConstants.TASK_STATUS_COMPLETED);

			// Outcome is default transition
			properties.put(WorkflowModel.PROP_OUTCOME, ActivitiConstants.DEFAULT_TRANSITION_NAME);
		}

		return filterTaskProperties(properties);
	}

	/**
	 * Gets the start variables.
	 *
	 * @param historicProcessInstance
	 *            the historic process instance
	 * @return the start variables
	 */
	public Map<String, Object> getStartVariables(HistoricProcessInstance historicProcessInstance) {
		// Get historic variable values for start-event
		HistoricActivityInstance startEvent = activitiUtil.getHistoryService()
				.createHistoricActivityInstanceQuery()
				.processInstanceId(historicProcessInstance.getId())
				.activityId(historicProcessInstance.getStartActivityId()).singleResult();

		Map<String, Object> variables = getHistoricActivityVariables(startEvent.getId());
		return variables;
	}

	/**
	 * Get all variable updates for process instance, latest updates on top.
	 *
	 * @param processId
	 *            the process id
	 * @return the historic process variables
	 */
	public Map<String, Object> getHistoricProcessVariables(String processId) {
		HistoricDetailQuery query = activitiUtil.getHistoryService().createHistoricDetailQuery()
				.processInstanceId(processId).excludeTaskDetails();
		return getHistoricVariables(query);
	}

	/**
	 * Get all variable updates for task instance, latest updates on top.
	 *
	 * @param taskId
	 *            the task id
	 * @return the historic task variables
	 */
	public Map<String, Object> getHistoricTaskVariables(String taskId) {
		HistoricDetailQuery query = activitiUtil.getHistoryService().createHistoricDetailQuery()
				.taskId(taskId);
		return getHistoricVariables(query);
	}

	/**
	 * Get all variable updates for activity, latest updates on top.
	 *
	 * @param activityId
	 *            the activity id
	 * @return the historic activity variables
	 */
	public Map<String, Object> getHistoricActivityVariables(String activityId) {
		HistoricDetailQuery query = activitiUtil.getHistoryService().createHistoricDetailQuery()
				.activityInstanceId(activityId);
		return getHistoricVariables(query);
	}

	/**
	 * Gets the historic variables.
	 *
	 * @param query
	 *            the query
	 * @return the historic variables
	 */
	private Map<String, Object> getHistoricVariables(HistoricDetailQuery query) {
		List<HistoricDetail> historicDetails = query.variableUpdates().orderByVariableName().asc()
				.orderByTime().desc().orderByVariableRevision().desc().list();
		return convertHistoricDetails(historicDetails);
	}

	/**
	 * Map arbitrary properties.
	 *
	 * @param variables
	 *            the variables
	 * @param localVariables
	 *            the local variables
	 * @param taskProperties
	 *            the task properties
	 * @param taskAssociations
	 *            the task associations
	 * @return the map
	 */
	private Map<QName, Serializable> mapArbitraryProperties(Map<String, Object> variables,
			final Map<String, Object> localVariables,
			final Map<QName, PropertyDefinition> taskProperties,
			final Map<QName, AssociationDefinition> taskAssociations) {
		EntryTransformer<String, Object, QName, Serializable> transformer = new EntryTransformer<String, Object, QName, Serializable>() {
			@Override
			public Pair<QName, Serializable> apply(Entry<String, Object> entry) {
				String key = entry.getKey();
				QName qname = factory.mapNameToQName(key);
				// Add variable, only if part of task definition or locally defined
				// on task
				if (taskProperties.containsKey(qname) || taskAssociations.containsKey(qname)
						|| localVariables.containsKey(key)) {
					Serializable value = convertPropertyValue(entry.getValue());
					return new Pair<QName, Serializable>(qname, value);
				}
				return null;
			}
		};
		return CollectionUtils.transform(variables, transformer);
	}

	/**
	 * Convert an Activiti variable value to an Alfresco value.
	 *
	 * @param value
	 *            activti value
	 * @return alfresco value
	 */
	public Serializable convertPropertyValue(Object value) {
		if (value == null) {
			return null;
		}
		if (nodeConverter.isSupported(value)) {
			return nodeConverter.convert(value);
		}
		if (value instanceof Serializable) {
			return (Serializable) value;
		}
		String msg = messageService.getMessage(ERR_CONVERT_VALUE, value);
		throw new WorkflowException(msg);
	}

	/**
	 * Performs basic conversion from a property to a value that can be uses as activiti variable.
	 * If the type of the property is known, use
	 *
	 * @param property
	 *            the property to be converted
	 * @return the value {@link #convertValueToPropertyType(Task, Serializable, QName)}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object convertPropertyToValue(Object property) {
		if (property instanceof NodeRef) {
			return nodeConverter.convertNode((NodeRef) property);
		} else if (property instanceof Collection) {
			boolean allNodes = true;
			// Check if collection contains node-refs
			for (Object item : ((Collection) property)) {
				if (!(item instanceof NodeRef)) {
					allNodes = false;
					break;
				}
			}

			if (allNodes) {
				return nodeConverter.convertNodes((Collection<NodeRef>) property);
			} else {
				return property;
			}
		} else {
			// No conversion needed, property can be used.
			return property;
		}
	}

	/**
	 * Converts a {@link Serializable} value to the type of the specified property.
	 *
	 * @param task
	 *            the task
	 * @param value
	 *            the value
	 * @param propertyName
	 *            the property name
	 * @return the serializable
	 */
	public Serializable convertValueToPropertyType(Task task, Serializable value, QName propertyName) {
		TypeDefinition taskDef = typeManager.getFullTaskDefinition(task);
		PropertyDefinition propDef = taskDef.getProperties().get(propertyName);
		if (propDef != null) {
			return (Serializable) DefaultTypeConverter.INSTANCE.convert(propDef.getDataType(),
					value);
		}
		return value;
	}

	/**
	 * Gets the new task properties.
	 *
	 * @param task
	 *            the task
	 * @param properties
	 *            the properties
	 * @param add
	 *            the add
	 * @param remove
	 *            the remove
	 * @return the new task properties
	 */
	@SuppressWarnings("unchecked")
	private Map<QName, Serializable> getNewTaskProperties(Task task,
			Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add,
			Map<QName, List<NodeRef>> remove) {
		// create properties to set on task instance
		Map<QName, Serializable> newProperties = properties;

		if (add != null || remove != null) {
			if (newProperties == null) {
				newProperties = new HashMap<QName, Serializable>(10);
			}

			Map<QName, Serializable> existingProperties = getTaskProperties(task);

			if (add != null) {
				// add new associations
				for (Entry<QName, List<NodeRef>> toAdd : add.entrySet()) {
					// Retrieve existing list of noderefs for association OR single nodeRef
					Serializable existingAdd = newProperties.get(toAdd.getKey());
					if (existingAdd == null) {
						// Get the property values from the existing values, if any
						existingAdd = existingProperties.get(toAdd.getKey());
						newProperties.put(toAdd.getKey(), (Serializable) existingAdd);
					}

					// Add property values, if nessesairy
					if (existingAdd == null) {
						newProperties.put(toAdd.getKey(), (Serializable) toAdd.getValue());
					} else {
						if (existingAdd instanceof List<?>) {
							List<NodeRef> existingList = (List<NodeRef>) existingAdd;

							for (NodeRef nodeRef : toAdd.getValue()) {
								if (!(existingList.contains(nodeRef))) {
									existingList.add(nodeRef);
								}
							}
						} else {
							// Single valued property, add first value
							if (toAdd.getValue().size() > 0) {
								newProperties.put(toAdd.getKey(), (Serializable) toAdd.getValue()
										.get(0));
							}
						}
					}
				}
			}

			if (remove != null) {
				// add new associations
				for (Entry<QName, List<NodeRef>> toRemove : remove.entrySet()) {
					// retrieve existing list of noderefs for
					// association
					Serializable existingRemove = (Serializable) newProperties.get(toRemove
							.getKey());
					boolean isAlreadyNewProperty = existingRemove != null;

					if (existingRemove == null) {
						existingRemove = (Serializable) existingProperties.get(toRemove.getKey());
					}

					// Only if the current property value is set, remove makes sense
					if (existingRemove != null) {
						if (existingRemove instanceof List<?>) {
							existingRemove = new ArrayList<NodeRef>((List<NodeRef>) existingRemove);

							for (NodeRef nodeRef : toRemove.getValue()) {
								((List<NodeRef>) existingRemove).remove(nodeRef);
							}
							newProperties.put(toRemove.getKey(), existingRemove);
						} else {
							// It's a single-valued property and an "add" has been done. No need to
							// remove
							// previous value, since it's overridden by the new value.
							if (!isAlreadyNewProperty) {
								// Property is single-valued and should be removed
								newProperties.put(toRemove.getKey(), null);
							}
						}
					}
				}
			}
		}
		return newProperties;
	}

	/**
	 * Sets the task properties.
	 *
	 * @param task
	 *            the task
	 * @param properties
	 *            the properties
	 */
	public void setTaskProperties(DelegateTask task, Map<QName, Serializable> properties) {
		if (properties == null || properties.isEmpty())
			return;
		TypeDefinition type = typeManager.getFullTaskDefinition(task);
		Map<String, Object> variablesToSet = handlerRegistry.handleVariablesToSet(properties, type,
				task, DelegateTask.class);
		if (variablesToSet.size() > 0) {
			task.setVariablesLocal(variablesToSet);
		}

	}

	/**
	 * Sets the properties on the task, using Activiti API.
	 *
	 * @param task
	 *            the task
	 * @param properties
	 *            the properties
	 */
	public void setTaskProperties(Task task, Map<QName, Serializable> properties) {
		if (properties == null || properties.isEmpty())
			return;

		TypeDefinition type = typeManager.getFullTaskDefinition(task);
		Map<String, Object> variablesToSet = handlerRegistry.handleVariablesToSet(properties, type,
				task, Task.class);

		TaskService taskService = activitiUtil.getTaskService();
		// Will be set when an assignee is present in passed properties.
		taskService.saveTask(task);

		// Set the collected variables on the task
		taskService.setVariablesLocal(task.getId(), variablesToSet);

		setTaskOwner(task, properties);
	}

	/**
	 * Sets the properties on the task, using Activiti API.
	 *
	 * @param task
	 *            the task
	 * @param properties
	 *            the properties
	 */
	public void setSavedTaskProperties(Task task, Map<QName, Serializable> properties) {
		if (properties == null || properties.isEmpty())
			return;

		TypeDefinition type = typeManager.getFullTaskDefinition(task);
		Map<String, Object> variablesToSet = handlerRegistry.handleVariablesToSet(properties, type,
				task, Task.class);

		TaskService taskService = activitiUtil.getTaskService();

		// Set the collected variables on the task
		taskService.setVariables(task.getId(), variablesToSet);

		// setTaskOwner(task, properties);
	}

	/**
	 * Sets the task owner.
	 *
	 * @param task
	 *            the task
	 * @param properties
	 *            the properties
	 */
	private void setTaskOwner(Task task, Map<QName, Serializable> properties) {
		QName ownerKey = ContentModel.PROP_OWNER;
		if (properties.containsKey(ownerKey)) {
			Serializable owner = properties.get(ownerKey);
			if (owner != null && !(owner instanceof String)) {
				throw getInvalidPropertyValueException(ownerKey, owner);
			}
			String assignee = (String) owner;
			String currentAssignee = task.getAssignee();
			// Only set the assignee if the value has changes to prevent
			// triggering assignementhandlers when not needed
			if (ObjectUtils.equals(currentAssignee, assignee) == false) {
				activitiUtil.getTaskService().setAssignee(task.getId(), assignee);
			}
		}
	}

	/**
	 * Gets the invalid property value exception.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the invalid property value exception
	 */
	private WorkflowException getInvalidPropertyValueException(QName key, Serializable value) {
		String msg = messageService.getMessage(ERR_SET_TASK_PROPS_INVALID_VALUE, value, key);
		return new WorkflowException(msg);
	}

	/**
	 * Filter out all internal task-properties.
	 *
	 * @param properties
	 *            the properties
	 * @return filtered properties.
	 */
	private Map<QName, Serializable> filterTaskProperties(Map<QName, Serializable> properties) {
		if (properties != null) {
			properties
					.remove(QName.createQName(null, ActivitiConstants.PROP_POOLED_ACTORS_HISTORY));
			properties.remove(QName.createQName(null, ActivitiConstants.PROP_TASK_FORM_KEY));
		}
		return properties;
	}

	/**
	 * Convert a list of {@link HistoricDetail} to a map with key-value pairs.
	 *
	 * @param details
	 *            the histroicDetails. Should be a list of {@link HistoricVariableUpdate}s.
	 * @return the map
	 */
	public Map<String, Object> convertHistoricDetails(List<HistoricDetail> details) {
		// TODO: Sorting should be performed in query, not available right now.
		Collections.sort(details, new Comparator<HistoricDetail>() {
			@Override
			public int compare(HistoricDetail o1, HistoricDetail o2) {
				Long id1 = Long.valueOf(o1.getId());
				Long id2 = Long.valueOf(o2.getId());
				return -id1.compareTo(id2);
			}
		});
		Map<String, Object> variables = new HashMap<String, Object>();
		for (HistoricDetail detail : details) {
			HistoricVariableUpdate varUpdate = (HistoricVariableUpdate) detail;
			// First value for a single key is used
			if (!variables.containsKey(varUpdate.getVariableName())) {
				variables.put(varUpdate.getVariableName(), varUpdate.getValue());
			}
		}
		return variables;
	}

	/**
	 * Gets the start variables.
	 *
	 * @param processDefId
	 *            the process def id
	 * @param properties
	 *            the properties
	 * @return the start variables
	 */
	public Map<String, Object> getStartVariables(String processDefId,
			Map<QName, Serializable> properties) {
		ProcessDefinition procDef = activitiUtil.getProcessDefinition(processDefId);
		String startTaskTypeName = activitiUtil.getStartTaskTypeName(processDefId);
		TypeDefinition startTaskType = factory.getTaskFullTypeDefinition(startTaskTypeName, true);

		// Lookup type definition for the startTask
		Map<QName, PropertyDefinition> taskProperties = startTaskType.getProperties();

		// Get all default values from the definitions
		Map<QName, Serializable> defaultProperties = new HashMap<QName, Serializable>();
		for (Map.Entry<QName, PropertyDefinition> entry : taskProperties.entrySet()) {
			String defaultValue = entry.getValue().getDefaultValue();
			if (defaultValue != null) {
				defaultProperties.put(entry.getKey(), defaultValue);
			}
		}

		// Put all passed properties in map with defaults
		if (properties != null) {
			defaultProperties.putAll(properties);
		}

		// Special case for task description default value
		// Use the shared description set in the workflowinstance
		String description = (String) defaultProperties.get(WorkflowModel.PROP_DESCRIPTION);
		if (description == null) {
			String wfDescription = (String) defaultProperties
					.get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
			String procDefKey = procDef.getKey();
			ReadOnlyProcessDefinition deployedDef = activitiUtil
					.getDeployedProcessDefinition(processDefId);
			String startEventName = deployedDef.getInitial().getId();
			String wfDefKey = factory.buildGlobalId(procDefKey);
			description = factory.getTaskDescription(startTaskType, wfDefKey, wfDescription,
					startEventName);
			defaultProperties.put(WorkflowModel.PROP_DESCRIPTION, description);
		}

		// Special case for workflowDueDate.
		if (!defaultProperties.containsKey(WorkflowModel.PROP_WORKFLOW_DUE_DATE)
				&& taskProperties.containsKey(WorkflowModel.PROP_WORKFLOW_DUE_DATE)) {
			defaultProperties.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, null);
		}

		return handlerRegistry.handleVariablesToSet(defaultProperties, startTaskType, null,
				Void.class);
	}

	/**
	 * Check mandatory properties.
	 *
	 * @param task
	 *            the task
	 */
	public void checkMandatoryProperties(DelegateTask task) {
		// Check all mandatory properties are set. This is checked here instead of in
		// the completeTask() to allow taskListeners to set variable values before checking
		List<QName> missingProps = getMissingMandatoryTaskProperties(task);
		if (missingProps != null && missingProps.size() > 0) {
			String missingPropString = StringUtils.join(missingProps.iterator(), ", ");
			throw new WorkflowException(messageService.getMessage(
					ERR_MANDATORY_TASK_PROPERTIES_MISSING, missingPropString));
		}
	}

	/**
	 * Get missing mandatory properties on Task.
	 *
	 * @param task
	 *            task instance
	 * @return array of missing property names (or null, if none)
	 */
	private List<QName> getMissingMandatoryTaskProperties(DelegateTask task) {
		TypeDefinition typeDefinition = typeManager.getFullTaskDefinition(task);
		// retrieve properties of task
		Map<QName, Serializable> existingValues = getTaskProperties(task, typeDefinition, false);

		Map<QName, PropertyDefinition> propertyDefs = typeDefinition.getProperties();
		Map<QName, AssociationDefinition> assocDefs = typeDefinition.getAssociations();

		List<QName> missingProps = findMissingProperties(existingValues, propertyDefs);
		List<QName> missingAssocs = findMissingProperties(existingValues, assocDefs);
		missingProps.addAll(missingAssocs);
		return missingProps;
	}

	/**
	 * Find missing properties.
	 *
	 * @param existingValues
	 *            the existing values
	 * @param definitions
	 *            the definitions
	 * @return the list
	 */
	private List<QName> findMissingProperties(Map<QName, Serializable> existingValues,
			Map<QName, ? extends ClassAttributeDefinition> definitions) {
		// for each property, determine if it is mandatory
		List<QName> missingProps = new ArrayList<QName>();
		for (Map.Entry<QName, ? extends ClassAttributeDefinition> entry : definitions.entrySet()) {
			QName name = entry.getKey();
			// Skip System and CM properties. Why?
			if ((name.getNamespaceURI().equals(NamespaceService.CONTENT_MODEL_1_0_URI) || (name
					.getNamespaceURI().equals(NamespaceService.SYSTEM_MODEL_1_0_URI)))) {
				continue;
			}
			if (isMandatory(entry.getValue())) {
				Object value = existingValues.get(entry.getKey());
				if (value == null || isEmptyString(value)) {
					missingProps.add(entry.getKey());
				}
			}
		}
		return missingProps;
	}

	/**
	 * Checks if is mandatory.
	 *
	 * @param definition
	 *            the definition
	 * @return true, if is mandatory
	 */
	private boolean isMandatory(ClassAttributeDefinition definition) {
		if (definition instanceof PropertyDefinition) {
			PropertyDefinition propDef = (PropertyDefinition) definition;
			return propDef.isMandatory();
		}
		AssociationDefinition assocDSef = (AssociationDefinition) definition;
		return assocDSef.isTargetMandatory();
	}

	/**
	 * Checks if is empty string.
	 *
	 * @param value
	 *            the value
	 * @return true, if is empty string
	 */
	private boolean isEmptyString(Object value) {
		if (value instanceof String) {
			String str = (String) value;
			return str.isEmpty();
		}
		return false;
	}

	/**
	 * Update task.
	 *
	 * @param task
	 *            the task
	 * @param properties
	 *            the properties
	 * @param add
	 *            the add
	 * @param remove
	 *            the remove
	 * @return the task
	 */
	public Task updateTask(Task task, Map<QName, Serializable> properties,
			Map<QName, List<NodeRef>> add, Map<QName, List<NodeRef>> remove) {
		Map<QName, Serializable> newProperties = getNewTaskProperties(task, properties, add, remove);
		if (newProperties != null) {
			if (task.getExecutionId() == null) {
				try {
					if (properties != null && !properties.isEmpty()) {
						updateMetadataAttachment(task.getId(), properties, null);
					}
					return activitiUtil.getTaskInstance(task.getId());
				} catch (Exception e) {
					throw new java.lang.RuntimeException(e);
				}
			}
			setTaskProperties(task, newProperties);
			return activitiUtil.getTaskInstance(task.getId());
		}
		return task;
	}

	/**
	 * Collect properties for standalone task.
	 *
	 * @param task
	 *            the task
	 * @return the map
	 */
	public Map<QName, Serializable> collectPropertiesForStandaloneTask(Task task) {
		TypeDefinition taskDef = typeManager.getFullTaskDefinition(task);
		Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
		Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();

		TaskService taskService = activitiUtil.getTaskService();
		// Get all task variables including execution vars.
		Map<String, Object> variables = taskService.getVariables(task.getId());
		Map<String, Object> localVariables = taskService.getVariablesLocal(task.getId());

		// Map the arbitrary properties
		Map<QName, Serializable> properties = mapArbitraryProperties(variables, localVariables,
				taskProperties, taskAssociations);

		// Map activiti task instance fields to properties
		properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
		properties.put(WorkflowModel.PROP_DESCRIPTION, task.getDescription());

		// Since the task is never started explicitally, we use the create time
		properties.put(WorkflowModel.PROP_START_DATE, task.getCreateTime());

		// Due date is present on the task
		properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());

		// Since this is a runtime-task, it's not completed yet
		properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
		properties.put(WorkflowModel.PROP_PRIORITY, task.getPriority());
		properties.put(ContentModel.PROP_CREATED, task.getCreateTime());
		properties.put(ContentModel.PROP_OWNER, task.getAssignee());
		Map<QName, Serializable> metadataAttachment = getMetadataAttachment(task);
		if (metadataAttachment != null) {
			properties.putAll(metadataAttachment);
		}
		List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
		mapPooledActors(links, properties);
		properties.put(ContentModel.PROP_NAME, task.getName());
		return properties;
	}

	/**
	 * Collect properties for standalone task.
	 *
	 * @param task
	 *            the task
	 * @return the map
	 */
	public Map<QName, Serializable> collectPropertiesForStandaloneTask(HistoricTaskInstance task) {

		TypeDefinition taskDef = typeManager.getFullTaskDefinition(task.getTaskDefinitionKey());
		Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
		Map<QName, AssociationDefinition> taskAssociations = taskDef.getAssociations();

		// TaskService taskService = activitiUtil.getTaskService();
		// Get all task variables including execution vars.
		Map<String, Object> variables = new HashMap<String, Object>();
		Map<String, Object> localVariables = new HashMap<String, Object>(); // taskService.getVariablesLocal(task.getId());

		// Map the arbitrary properties
		Map<QName, Serializable> properties = mapArbitraryProperties(variables, localVariables,
				taskProperties, taskAssociations);

		// Map activiti task instance fields to properties
		properties.put(WorkflowModel.PROP_TASK_ID, task.getId());
		properties.put(WorkflowModel.PROP_DESCRIPTION, task.getDescription());

		// Since the task is never started explicitally, we use the create time
		properties.put(WorkflowModel.PROP_START_DATE, task.getStartTime());

		// Due date is present on the task
		properties.put(WorkflowModel.PROP_DUE_DATE, task.getDueDate());

		// Since this is a runtime-task, it's not completed yet
		properties.put(WorkflowModel.PROP_COMPLETION_DATE, null);
		properties.put(WorkflowModel.PROP_PRIORITY, task.getPriority());
		properties.put(ContentModel.PROP_CREATED, task.getStartTime());

		Map<QName, Serializable> metadataAttachment = getMetadataAttachment(task.getId());
		if (metadataAttachment != null) {
			properties.putAll(metadataAttachment);
		}
		properties.put(ContentModel.PROP_OWNER, task.getOwner());
		properties.put(WorkflowModel.ASSOC_ASSIGNEE, task.getAssignee());
		// List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
		// mapPooledActors(links, properties);
		properties.put(ContentModel.PROP_NAME, task.getName());
		return properties;
	}

	/**
	 * Gets the metadata attachment.
	 *
	 * @param task
	 *            the task
	 * @return the metadata attachment
	 */
	public Map<QName, Serializable> getMetadataAttachment(Task task) {
		return getMetadataAttachment(task.getId());
	}

	/**
	 * Gets the metadata attachment. Data is extracted either the noderef or the attachment content
	 *
	 * @param taskId
	 *            the task id
	 * @return the metadata for task
	 */
	public Map<QName, Serializable> getMetadataAttachment(String taskId) {
		TaskService taskService = activitiUtil.getTaskService();
		List<Attachment> taskAttachments = taskService.getTaskAttachments(taskId);
		for (Attachment attachment : taskAttachments) {
			if ("metadata".equals(attachment.getName())) {
				if (attachment.getUrl() != null) {
					NodeRef metadataNode = new NodeRef(attachment.getUrl());
					// if (!factory.getNodeService().exists(metadataNode)) {
					// logger.error(taskId + " metadata node is missing!");
					// return Collections.emptyMap();
					// }
					return factory.getNodeService().getProperties(metadataNode);
				}
				InputStream attachmentContent = taskService
						.getAttachmentContent(attachment.getId());
				try {
					@SuppressWarnings("unchecked")
					Map<QName, Serializable> data = (Map<QName, Serializable>) new ObjectInputStream(
							attachmentContent).readObject();
					return data;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return Collections.emptyMap();
	}

	/**
	 * Update metadata attachment. If not exits it lazily creates is with
	 * {@link #setMetadataAttachment(String, Map, String)}
	 *
	 * @param taskId
	 *            the task id
	 * @param metadata
	 *            the metadata to set
	 * @param url
	 *            the url the nodref id, to use to store data into
	 * @return the attachment
	 * @throws Exception
	 *             the exception
	 */
	public Attachment updateMetadataAttachment(String taskId, Map<QName, Serializable> metadata,
			String url) throws Exception {
		TaskService taskService = activitiUtil.getTaskService();
		List<Attachment> taskAttachments = taskService.getTaskAttachments(taskId);
		for (Attachment attachment : taskAttachments) {
			if ("metadata".equals(attachment.getName())) {
				if (attachment.getUrl() != null) {
					factory.getNodeService().addProperties(new NodeRef(attachment.getUrl()),
							metadata);
					return attachment;
				}
				taskService.deleteAttachment(attachment.getId());
				break;
			}
		}
		return setMetadataAttachment(taskId, metadata, url);
	}

	/**
	 * Sets the metadata attachment. If url is provided is expected to be noderef id and the data is
	 * set to it. If node is null, new attachment is created.
	 *
	 * @param taskId
	 *            the task to update
	 * @param metadata
	 *            the metadata to set
	 * @param url
	 *            the url the noderef to use as data
	 * @return the attachment the created attachment.
	 * @throws Exception
	 *             the exception on any error
	 */
	private Attachment setMetadataAttachment(String taskId, Map<QName, Serializable> metadata,
			String url) throws Exception {

		TaskService taskService = activitiUtil.getTaskService();
		if (url != null) {
			factory.getNodeService().addProperties(new NodeRef(url), metadata);
			return taskService.createAttachment("application/octet-stream", taskId, null,
					"metadata", taskId, url);
		}
		ByteArrayOutputStream bytesOutput = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bytesOutput);
		out.writeObject(metadata);
		IOUtils.closeQuietly(out);
		return taskService.createAttachment("application/octet-stream", taskId, null, "metadata",
				taskId, new ByteArrayInputStream(bytesOutput.toByteArray()));
	}
}