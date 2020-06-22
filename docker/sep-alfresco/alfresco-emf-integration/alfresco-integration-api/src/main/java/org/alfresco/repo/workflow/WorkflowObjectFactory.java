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

package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.tenant.MultiTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTimer;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;
import org.alfresco.util.collections.Function;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating WorkflowObject objects.
 *
 * @author Nick Smith
 * @since 3.4.e
 */
public class WorkflowObjectFactory {

	/** The Constant TITLE_LABEL. */
	private final static String TITLE_LABEL = "title";

	/** The Constant DESC_LABEL. */
	private final static String DESC_LABEL = "description";

	/** The q name converter. */
	private final WorkflowQNameConverter qNameConverter;

	/** The tenant service. */
	private final TenantService tenantService;

	/** The message service. */
	private final MessageService messageService;

	/** The dictionary service. */
	private final DictionaryService dictionaryService;

	/** The engine id. */
	private final String engineId;

	/** The default start task type. */
	private final QName defaultStartTaskType;

	/** The node service. */
	private NodeService nodeService;

	/**
	 * Instantiates a new workflow object factory.
	 *
	 * @param qNameConverter
	 *            the q name converter
	 * @param tenantService
	 *            the tenant service
	 * @param messageService
	 *            the message service
	 * @param dictionaryService
	 *            the dictionary service
	 * @param engineId
	 *            the engine id
	 * @param defaultStartTaskType
	 *            the default start task type
	 */
	public WorkflowObjectFactory(WorkflowQNameConverter qNameConverter, TenantService tenantService,
			MessageService messageService, DictionaryService dictionaryService, String engineId,
			QName defaultStartTaskType) {
		this.tenantService = tenantService;
		this.messageService = messageService;
		this.dictionaryService = dictionaryService;
		this.engineId = engineId;
		this.qNameConverter = qNameConverter;
		this.defaultStartTaskType = defaultStartTaskType;
	}

	/**
	 * Builds the global id.
	 *
	 * @param localId
	 *            the local id
	 * @return the string
	 */
	public String buildGlobalId(String localId) {
		return BPMEngineRegistry.createGlobalId(engineId, localId);
	}

	/**
	 * Gets the local engine id.
	 *
	 * @param globalId
	 *            the global id
	 * @return the local engine id
	 */
	public String getLocalEngineId(String globalId) {
		return BPMEngineRegistry.getLocalId(globalId);
	}

	/**
	 * Checks if is global id.
	 *
	 * @param globalId
	 *            the global id
	 * @return true, if is global id
	 */
	public boolean isGlobalId(String globalId) {
		return BPMEngineRegistry.isGlobalId(globalId, engineId);
	}

	/**
	 * Create a new {@link WorkflowDeployment}.
	 *
	 * @param wfDef
	 *            the wf def
	 * @param problems
	 *            the problems
	 * @return the workflow deployment
	 */
	public WorkflowDeployment createDeployment(WorkflowDefinition wfDef, String... problems) {
		WorkflowDeployment wfDeployment = new WorkflowDeployment(wfDef, problems);
		return wfDeployment;
	}

	/**
	 * Create a new {@link WorkflowDefinition}.
	 *
	 * @param defId
	 *            the def id
	 * @param defName
	 *            the def name
	 * @param version
	 *            the version
	 * @param defaultTitle
	 *            the default title
	 * @param defaultDescription
	 *            the default description
	 * @param startTaskDef
	 *            the start task def
	 * @return the workflow definition
	 */
	public WorkflowDefinition createDefinition(String defId, String defName, int version, String defaultTitle,
			String defaultDescription, WorkflowTaskDefinition startTaskDef) {
		checkDomain(defName);
		String actualDefName = buildGlobalId(tenantService.getBaseName(defName));
		String actualId = buildGlobalId(defId);

		String actualVersion = Integer.toString(version);

		String displayId = getProcessKey(defName) + ".workflow";
		String title = getLabel(displayId, TITLE_LABEL, defaultTitle);
		String description = getLabel(displayId, DESC_LABEL, defaultDescription, title);
		return new WorkflowDefinition(actualId, actualDefName, actualVersion, title, description, startTaskDef);
	}

	/**
	 * Gets the workflow definition name.
	 *
	 * @param defName
	 *            the def name
	 * @return the workflow definition name
	 */
	public String getWorkflowDefinitionName(String defName) {
		String baseName = tenantService.getBaseName(defName);
		String actualName = buildGlobalId(baseName);
		return actualName;
	}

	/**
	 * Creates a new WorkflowObject object.
	 *
	 * @param id
	 *            the id
	 * @param definition
	 *            the definition
	 * @param variables
	 *            the variables
	 * @param isActive
	 *            the is active
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @return the workflow instance
	 */
	public WorkflowInstance createInstance(String id, WorkflowDefinition definition, Map<String, Object> variables,
			boolean isActive, Date startDate, Date endDate) {
		String actualId = buildGlobalId(id);

		String description = (String) getVariable(variables, WorkflowModel.PROP_WORKFLOW_DESCRIPTION);

		NodeRef initiator = null;
		ScriptNode initiatorSN = (ScriptNode) getVariable(variables, WorkflowConstants.PROP_INITIATOR);
		if (initiatorSN != null) {
			initiator = initiatorSN.getNodeRef();
		}

		NodeRef context = getNodeVariable(variables, WorkflowModel.PROP_CONTEXT);
		NodeRef workflowPackage = getNodeVariable(variables, WorkflowModel.ASSOC_PACKAGE);

		WorkflowInstance workflowInstance = new WorkflowInstance(actualId, definition, description, initiator,
				workflowPackage, context, isActive, startDate, endDate);

		workflowInstance.priority = (Integer) getVariable(variables, WorkflowModel.PROP_WORKFLOW_PRIORITY);
		Date dueDate = (Date) getVariable(variables, WorkflowModel.PROP_WORKFLOW_DUE_DATE);
		if (dueDate != null) {
			workflowInstance.dueDate = dueDate;
		}

		return workflowInstance;
	}

	/**
	 * Creates a new WorkflowObject object.
	 *
	 * @param id
	 *            the id
	 * @param wfInstance
	 *            the wf instance
	 * @param node
	 *            the node
	 * @param isActive
	 *            the is active
	 * @return the workflow path
	 */
	public WorkflowPath createPath(String id, WorkflowInstance wfInstance, WorkflowNode node, boolean isActive) {
		String actualId = buildGlobalId(id);
		return new WorkflowPath(actualId, wfInstance, node, isActive);
	}

	/**
	 * Creates a new WorkflowObject object.
	 *
	 * @param name
	 *            the name
	 * @param definitionName
	 *            the definition name
	 * @param defaultTitle
	 *            the default title
	 * @param defaultDescription
	 *            the default description
	 * @param type
	 *            the type
	 * @param isTaskNode
	 *            the is task node
	 * @param transitions
	 *            the transitions
	 * @return the workflow node
	 */
	public WorkflowNode createNode(String name, String definitionName, String defaultTitle, String defaultDescription,
			String type, boolean isTaskNode, WorkflowTransition... transitions) {
		String displayId = definitionName + ".node." + name;
		String title = getLabel(displayId, TITLE_LABEL, defaultTitle);
		String description = getLabel(displayId, DESC_LABEL, defaultDescription, title);
		return new WorkflowNode(name, title, description, type, isTaskNode, transitions);
	}

	/**
	 * Creates a new WorkflowObject object.
	 *
	 * @param id
	 *            the id
	 * @param defaultTitle
	 *            the default title
	 * @param defaultDescription
	 *            the default description
	 * @param isDefault
	 *            the is default
	 * @param baseLabelKeys
	 *            the base label keys
	 * @return the workflow transition
	 */
	public WorkflowTransition createTransition(String id, String defaultTitle, String defaultDescription,
			boolean isDefault, String... baseLabelKeys) {
		String title = getLabel(baseLabelKeys, TITLE_LABEL, defaultTitle);
		String description = getLabel(baseLabelKeys, TITLE_LABEL, defaultDescription);
		return new WorkflowTransition(id, title, description, isDefault);
	}

	/**
	 * Creates a new WorkflowObject object.
	 *
	 * @param id
	 *            the id
	 * @param node
	 *            the node
	 * @param typeName
	 *            the type name
	 * @param isStart
	 *            the is start
	 * @return the workflow task definition
	 */
	public WorkflowTaskDefinition createTaskDefinition(String id, WorkflowNode node, String typeName, boolean isStart) {
		TypeDefinition metaData = getTaskTypeDefinition(typeName, isStart);
		if (id == null) {
			id = qNameConverter.mapQNameToName(metaData.getName());
		}
		return new WorkflowTaskDefinition(id, node, metaData);
	}

	/**
	 * Creates a new WorkflowObject object.
	 *
	 * @param id
	 *            the id
	 * @param taskDef
	 *            the task def
	 * @param name
	 *            the name
	 * @param defaultTitle
	 *            the default title
	 * @param defaultDescription
	 *            the default description
	 * @param state
	 *            the state
	 * @param path
	 *            the path
	 * @param properties
	 *            the properties
	 * @return the workflow task
	 */
	public WorkflowTask createTask(String id, WorkflowTaskDefinition taskDef, String name, String defaultTitle,
			String defaultDescription, WorkflowTaskState state, WorkflowPath path,
			Map<QName, Serializable> properties) {
		String defName = path.getInstance().getDefinition().getName();
		String actualId = buildGlobalId(id);

		String processKey = getProcessKey(defName) + ".task." + name;
		TypeDefinition metadata = taskDef.getMetadata();
		String title = getLabel(processKey, TITLE_LABEL, metadata.getTitle(), defaultTitle, name);
		String description = getLabel(processKey, DESC_LABEL, metadata.getDescription(), defaultDescription, title);
		return new WorkflowTask(actualId, taskDef, name, title, description, state, path, properties);
	}

	/**
	 * Creates a new WorkflowObject object.
	 *
	 * @param id
	 *            the id
	 * @param taskDef
	 *            the task def
	 * @param name
	 *            the name
	 * @param defaultTitle
	 *            the default title
	 * @param defaultDescription
	 *            the default description
	 * @param state
	 *            the state
	 * @param defName
	 *            the def name
	 * @param properties
	 *            the properties
	 * @return the workflow task
	 */
	public WorkflowTask createTask(String id, WorkflowTaskDefinition taskDef, String name, String defaultTitle,
			String defaultDescription, WorkflowTaskState state, String defName, Map<QName, Serializable> properties) {
		String actualId = buildGlobalId(id);

		String processKey = getProcessKey(defName) + ".task." + name;
		TypeDefinition metadata = taskDef.getMetadata();
		String title = getLabel(processKey, TITLE_LABEL, metadata.getTitle(), defaultTitle, name);
		String description = getLabel(processKey, DESC_LABEL, metadata.getDescription(), defaultDescription, title);
		return new WorkflowTask(actualId, taskDef, name, title, description, state, null, properties);
	}

	/**
	 * Creates a new WorkflowObject object.
	 *
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param error
	 *            the error
	 * @param dueDate
	 *            the due date
	 * @param workflowPath
	 *            the workflow path
	 * @param workflowTask
	 *            the workflow task
	 * @return the workflow timer
	 */
	public WorkflowTimer createWorkflowTimer(String id, String name, String error, Date dueDate,
			WorkflowPath workflowPath, WorkflowTask workflowTask) {
		String actualId = buildGlobalId(id);
		return new WorkflowTimer(actualId, name, workflowPath, workflowTask, dueDate, error);
	}

	/**
	 * Gets the process key.
	 *
	 * @param defName
	 *            the def name
	 * @return the process key
	 */
	public String getProcessKey(String defName) {
		String processKey = defName;
		if (isGlobalId(defName)) {
			processKey = getLocalEngineId(defName);
		}
		return tenantService.getBaseName(processKey);
	}

	/**
	 * Gets the domain process key.
	 *
	 * @param defName
	 *            the def name
	 * @return the domain process key
	 */
	public String getDomainProcessKey(String defName) {
		String processKey = defName;
		if (isGlobalId(defName)) {
			processKey = getLocalEngineId(defName);
		}
		return tenantService.getName(processKey);
	}

	/**
	 * Gets the task title.
	 *
	 * @param typeDefinition
	 *            the type definition
	 * @param defName
	 *            the def name
	 * @param defaultTitle
	 *            the default title
	 * @param name
	 *            the name
	 * @return the task title
	 */
	public String getTaskTitle(TypeDefinition typeDefinition, String defName, String defaultTitle, String name) {
		String displayId = getProcessKey(defName) + ".task." + name;
		return getLabel(displayId, TITLE_LABEL, defaultTitle, typeDefinition.getTitle(), name);
	}

	/**
	 * Gets the task description.
	 *
	 * @param typeDefinition
	 *            the type definition
	 * @param defName
	 *            the def name
	 * @param defaultDescription
	 *            the default description
	 * @param title
	 *            the title
	 * @return the task description
	 */
	public String getTaskDescription(TypeDefinition typeDefinition, String defName, String defaultDescription,
			String title) {
		String displayId = getProcessKey(defName) + ".task." + title;
		return getLabel(displayId, DESC_LABEL, defaultDescription);
	}

	/**
	 * Get an I18N Label for a workflow item.
	 *
	 * @param displayId
	 *            message resource id lookup
	 * @param labelKey
	 *            label to lookup (title or description)
	 * @param defaults
	 *            the defaults
	 * @return the label
	 */
	private String getLabel(String displayId, String labelKey, String... defaults) {
		String keyBase = displayId.replace(":", "_");
		String key = keyBase + "." + labelKey;
		String label = messageService.getMessage(key);
		return getDefaultLabel(label, defaults);
	}

	/**
	 * Gets the label.
	 *
	 * @param locations
	 *            the locations
	 * @param labelKey
	 *            the label key
	 * @param defaults
	 *            the defaults
	 * @return the label
	 */
	private String getLabel(String[] locations, String labelKey, String... defaults) {
		String label = null;
		int i = 0;
		while ((label == null) && (i < locations.length)) {
			label = getLabel(locations[i], labelKey);
			i++;
		}
		return getDefaultLabel(label, defaults);
	}

	/**
	 * Gets the default label.
	 *
	 * @param label
	 *            the label
	 * @param defaults
	 *            the defaults
	 * @return the default label
	 */
	private String getDefaultLabel(String label, String... defaults) {
		int i = 0;
		while ((label == null) && (i < defaults.length)) {
			label = defaults[i];
			i++;
		}
		return label;
	}

	/**
	 * Gets the node variable.
	 *
	 * @param variables
	 *            the variables
	 * @param qName
	 *            the q name
	 * @return the node variable
	 */
	private NodeRef getNodeVariable(Map<String, Object> variables, QName qName) {
		Object obj = getVariable(variables, qName);
		if (obj == null) {
			return null;
		}
		if (obj instanceof ScriptNode) {
			ScriptNode scriptNode = (ScriptNode) obj;
			return scriptNode.getNodeRef();
		}
		String message = "Variable " + qName + " should be of type ScriptNode but was " + obj.getClass();
		throw new WorkflowException(message);
	}

	/**
	 * Gets the variable.
	 *
	 * @param variables
	 *            the variables
	 * @param qName
	 *            the q name
	 * @return the variable
	 */
	private Object getVariable(Map<String, Object> variables, QName qName) {
		if ((variables == null) || (qName == null)) {
			return null;
		}
		String varName = qNameConverter.mapQNameToName(qName);
		return variables.get(varName);
	}

	/**
	 * Gets the variable.
	 *
	 * @param variables
	 *            the variables
	 * @param key
	 *            the key
	 * @return the variable
	 */
	private Object getVariable(Map<String, Object> variables, String key) {
		if ((variables == null) || (key == null)) {
			return null;
		}
		return variables.get(key);
	}

	/**
	 * Throws exception if domain mismatch.
	 *
	 * @param defName
	 *            the def name
	 */
	public void checkDomain(String defName) {
		if (tenantService.isEnabled()) {
			String processKey = defName;
			if (isGlobalId(defName)) {
				processKey = getLocalEngineId(defName);
			}
			tenantService.checkDomain(processKey);
		}
	}

	/**
	 * Filter by domain.
	 *
	 * @param <T>
	 *            the generic type
	 * @param values
	 *            the values
	 * @param processKeyGetter
	 *            the process key getter
	 * @return the list
	 */
	public <T extends Object> List<T> filterByDomain(Collection<T> values, final Function<T, String> processKeyGetter) {
		final String currentDomain = tenantService.getCurrentUserDomain();
		return CollectionUtils.filter(values, new Filter<T>() {
			@Override
			public Boolean apply(T value) {
				String key = processKeyGetter.apply(value);
				String domain = MultiTServiceImpl.getMultiTenantDomainName(key);
				return (currentDomain.equals(domain));
			}
		});
	}

	/**
	 * Gets the full domain task id.
	 *
	 * @param taskId
	 *            the task id
	 * @param domain
	 *            the domain
	 * @return the full domain task id
	 */
	public String getFullDomainTaskId(String taskId, String domain) {
		StringBuilder taskIdBuilder = new StringBuilder();
		taskIdBuilder.append("@");
		taskIdBuilder.append(domain);
		taskIdBuilder.append("@");
		taskIdBuilder.append(taskId);
		return taskIdBuilder.toString();
	}

	/**
	 * Clear full domain task id - get the actual task id without domain info.
	 *
	 * @param fullTaskId
	 *            the full task id
	 * @param domain
	 *            the domain
	 * @return the string
	 */
	public String clearFullDomainTaskId(String fullTaskId, String domain) {
		return fullTaskId.replace("@" + domain + "@", "");
	}

	/**
	 * Returns an anonymous {@link TypeDefinition} for the given name with all
	 * the mandatory aspects applied.
	 *
	 * @param name
	 *            the name of the task definition.
	 * @param isStart
	 *            is theis a start task?
	 * @return the task {@link TypeDefinition}.
	 */
	public TypeDefinition getTaskFullTypeDefinition(String name, boolean isStart) {
		TypeDefinition typeDef = getTaskTypeDefinition(name, isStart);
		return dictionaryService.getAnonymousType(typeDef.getName());
	}

	/**
	 * Gets the Task {@link TypeDefinition} for the given name.
	 *
	 * @param name
	 *            the name of the task definition.
	 * @param isStart
	 *            is theis a start task?
	 * @return the task {@link TypeDefinition}.
	 */
	public TypeDefinition getTaskTypeDefinition(String name, boolean isStart) {
		TypeDefinition typeDef = null;
		if (name != null) {
			QName typeName = qNameConverter.mapNameToQName(name);
			typeDef = dictionaryService.getType(typeName);
		}
		if (typeDef == null) {
			QName defaultTypeName = isStart ? defaultStartTaskType : WorkflowModel.TYPE_WORKFLOW_TASK;
			typeDef = dictionaryService.getType(defaultTypeName);
			if (typeDef == null) {
				String msg = messageService.getMessage("workflow.get.task.definition.metadata.error", name);
				throw new WorkflowException(msg);
			}
		}
		return typeDef;
	}

	/**
	 * Map QName to jBPM variable name.
	 *
	 * @param name
	 *            QName
	 * @return jBPM variable name
	 */
	public String mapQNameToName(QName name) {
		return qNameConverter.mapQNameToName(name);
	}

	/**
	 * Map QName to jBPM variable name.
	 *
	 * @param name
	 *            QName
	 * @return jBPM variable name
	 */
	public QName mapNameToQName(String name) {
		return qNameConverter.mapNameToQName(name);
	}

	/**
	 * Clear q name cache.
	 */
	public void clearQNameCache() {
		qNameConverter.clearCache();
	}

	/**
	 * Gets the node service.
	 *
	 * @return the node service
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * Sets the node service.
	 *
	 * @param nodeService
	 *            the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

}