/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.cmf.integration.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.SEIPTenantIntegration;
import org.alfresco.repo.tenant.MultiTServiceImpl;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowReportConstants;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.bean.repository.Node;
import org.springframework.extensions.webscripts.WebScriptException;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;

/**
 * Bean implementation for the Start Task Wizard. Every process should have
 * single instance as it is stateful.
 *
 * @author bbanchev
 */
public class StandaloneTaskWizard extends ActivitiWizards {

	/** The start task node. */
	protected Node startTaskNode;
	/** The item to workflow. */
	private NodeRef itemToTask;
	/** The workflow definition. */
	private WorkflowTaskDefinition taskDefinition;

	/** The selected task id. */
	private String selectedTaskId;

	/** The parent id. */
	private String parentId;

	/**
	 * Instantiates a new start task wizard.
	 *
	 * @param registry
	 *            the registry
	 * @param caseService
	 *            the case service
	 */
	public StandaloneTaskWizard(ServiceRegistry registry, CMFService caseService) {
		super(registry, caseService);
	}

	/**
	 * Inits the workflow metadata.
	 *
	 * @param selectedTaskId
	 *            the selected workflow
	 * @param itemToTaskId
	 *            the item to task noderef
	 * @param parentId
	 *            the parent id
	 */
	public void init(String selectedTaskId, String itemToTaskId, String parentId) {

		this.parentId = parentId;
		startTaskNode = null;
		this.selectedTaskId = selectedTaskId;
		String multiTenantDomainName = MultiTServiceImpl.getMultiTenantDomainName(selectedTaskId);
		String domain = SEIPTenantIntegration.getTenantId();
		if (!domain.equals(multiTenantDomainName)) {
			throw new WebScriptException(401, "Task provided is not part of the '" + domain + "' domain!");
		}
		this.selectedTaskId = getObjectFactory().clearFullDomainTaskId(this.selectedTaskId, domain);
		taskDefinition = new WorkflowTaskDefinition(null, null,
				getObjectFactory().getTaskTypeDefinition(this.selectedTaskId, false));
		if (taskDefinition == null) {
			throw new WebScriptException(404, "No task definition found for: " + this.selectedTaskId);
		}
		populateItemToTaskId(itemToTaskId);
		createModel();
	}

	/**
	 * Internal method to evaluate the items attache to the task. Throws
	 * Exception on initialization error ( wrong type, id, ...)
	 *
	 * @param itemToTaskId
	 *            is the noderef id
	 */
	private void populateItemToTaskId(String itemToTaskId) {
		if (itemToTaskId != null) {
			itemToTask = caseService.getNodeRef(itemToTaskId);
			if (itemToTask == null || !getNodeService().exists(itemToTask)) {
				throw new WebScriptException(404, "Element to attach to task is not found!");
			}
			QName type = getNodeService().getType(itemToTask);
			if (!getDictionaryService().isSubClass(type, ContentModel.TYPE_FOLDER)) {
				throw new WebScriptException(404, "Object to attach to workflow is not the required type!");
			}
		}
	}

	/**
	 * Creates the model.
	 */
	public void createModel() {

		debug("Selected task: ", selectedTaskId);

		if (taskDefinition != null) {
			debug("Start task definition: ", taskDefinition);
			// create an instance of a task from the data dictionary
			startTaskNode = TransientNode.createNew(registry, taskDefinition.getMetadata(),
					"task_" + System.currentTimeMillis(), null);
		} else {
			throw new WebScriptException(404, "No start task found for: " + taskDefinition);
		}

	}

	/**
	 * Prepare data.
	 *
	 * @param properties
	 *            the properties
	 * @return the map
	 */
	public Map<QName, Serializable> prepareData(Map<QName, Serializable> properties) {
		// TODO: Deal with workflows that don't require any data

		debug("Starting task: ", selectedTaskId);

		// prepare the parameters from the current state of the property sheet
		Map<QName, Serializable> params = WorkflowUtil.prepareTaskParams(startTaskNode);
		// first set initial properties and then continue set them
		params.putAll(properties);
		debug("Starting task with parameters: ", params);

		// create a workflow package for the attached items and add them
		NodeRef workflowPackage = getWorkflowService().createPackage(null);
		params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
		params.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME, selectedTaskId);
		attachItemsToTaskPackage(workflowPackage, false);
		// setup the context for the workflow (this is the space the workflow
		// was launched from)
		// TODO
		// SiteInfo workflowContext =
		// registry.getSiteService().getSite(itemToWorkflow);
		// if (workflowContext != null) {
		// params.put(WorkflowModel.PROP_CONTEXT, workflowContext.getNodeRef());
		// }

		params.putAll(properties);

		debug("Task Params ", params);
		return params;
	}

	/**
	 * Attach the items to the task as metadata.
	 *
	 * @param workflowPackage
	 *            is the package to use
	 * @param removeOld
	 *            is whether to remove all old items.
	 */
	private void attachItemsToTaskPackage(NodeRef workflowPackage, boolean removeOld) {
		if (removeOld) {
			List<ChildAssociationRef> childAssocs = getUnprotectedNodeService().getChildAssocs(workflowPackage);
			for (ChildAssociationRef childAssociationRef : childAssocs) {
				if (WorkflowModel.ASSOC_PACKAGE_CONTAINS.equals(childAssociationRef.getTypeQName())) {
					getUnprotectedNodeService().removeChildAssociation(childAssociationRef);
				}
			}
		}
		if (itemToTask != null) {

			QName childName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName
					.createValidLocalName((String) getNodeService().getProperty(itemToTask, ContentModel.PROP_NAME)));
			getUnprotectedNodeService().addChild(workflowPackage, itemToTask, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
					childName);
		}
	}

	/**
	 * Start task.
	 *
	 * @param variablesDefault
	 *            the variables default
	 * @return the workflow task
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public WorkflowTask startTask(Map<QName, Serializable> variablesDefault) throws Exception {

		debug("Starting task of type: ", selectedTaskId, "with assignee: ",
				variablesDefault.get(WorkflowModel.ASSOC_ASSIGNEE));
		// new task with internal id
		TaskService taskService = getActivitiUtil().getTaskService();
		TaskEntity task = (TaskEntity) taskService.newTask();
		task.setTaskDefinitionKeyWithoutCascade(selectedTaskId);
		if (parentId != null) {
			task.setParentTaskId(parentId);
		}
		// map the metadata
		updateTaskMetadata(variablesDefault, task);
		// save task to persist it
		taskService.saveTask(task);
		// load the instnace again
		TaskEntity taskInstance = (TaskEntity) getActivitiUtil().getTaskInstance(task.getId());
		Serializable multiAssignees = variablesDefault.get(CMFModel.ASSOC_TASK_MULTI_ASSIGNEES);
		if (multiAssignees != null) {
			Pair<List<String>, List<String>> extractMultiAssignees = extractMultiAssignees(multiAssignees);
			variablesDefault.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable) extractMultiAssignees.getFirst());
			variablesDefault.put(WorkflowModel.ASSOC_GROUP_ASSIGNEES, (Serializable) extractMultiAssignees.getSecond());
		}
		// set as pooled task if data is provided
		Serializable users = variablesDefault.get(WorkflowModel.ASSOC_ASSIGNEES);
		if (users != null) {

			Collection<String> convertedUsers = DefaultTypeConverter.INSTANCE.convert(Collection.class, users);
			for (String userId : convertedUsers) {
				taskService.addCandidateUser(taskInstance.getId(), userId);
				// taskService.addCandidateGroup(taskId, groupId);
			}
		}
		Serializable groups = variablesDefault.get(WorkflowModel.ASSOC_GROUP_ASSIGNEES);
		if (groups != null) {
			// assign to group
			Collection<?> convertedGroups = (Collection<?>) groups;
			for (Object actor : convertedGroups) {
				String groupId = null;
				if (actor instanceof NodeRef) {
					groupId = getUnprotectedNodeService().getProperty((NodeRef) actor, ContentModel.PROP_AUTHORITY_NAME)
							.toString();
				} else {
					groupId = actor.toString();
				}
				taskService.addCandidateGroup(taskInstance.getId(), groupId);
			}

		} else if (variablesDefault.get(WorkflowModel.ASSOC_GROUP_ASSIGNEE) != null) {
			// assign to group
			Serializable group = variablesDefault.get(WorkflowModel.ASSOC_GROUP_ASSIGNEE);
			if (group != null) {
				taskService.addCandidateGroup(taskInstance.getId(), (String) getUnprotectedNodeService()
						.getProperty((NodeRef) group, ContentModel.PROP_AUTHORITY_NAME));
			}
		}

		taskService.saveTask(taskInstance);
		WorkflowTask convertStandalone = getTypeConverter().convertStandalone(task);
		NodeRef addTask = getWorkflowReportService().addStandaloneTask(convertStandalone.getId(), variablesDefault);
		String url = null;
		if (addTask != null) {
			url = addTask.toString();
		}
		// add the attachment info with dummy map
		getPropertyConverter().updateMetadataAttachment(task.getId(), new HashMap<QName, Serializable>(), url);
		// load it again
		task = (TaskEntity) getActivitiUtil().getTaskInstance(task.getId());
		// convert and return the started task
		convertStandalone = getTypeConverter().convertStandalone(task);
		return convertStandalone;
	}

	/**
	 * Update metadata for task, by extracting properties from map and set them
	 * to corresponding fields.
	 *
	 * @param variablesDefault
	 *            the variables default
	 * @param task
	 *            the task
	 * @return the task updated
	 * @throws Exception
	 *             the exception
	 */
	private Task updateTaskMetadata(Map<QName, Serializable> variablesDefault, Task task) throws Exception {
		if (task == null || variablesDefault == null) {
			return null;
		}
		updateUserData(variablesDefault);
		// remove all props, as stored in task itself
		Serializable assignee = variablesDefault.get(WorkflowModel.ASSOC_ASSIGNEE);
		if (assignee != null) {
			task.setAssignee(assignee.toString());
		}
		Serializable owner = variablesDefault.get(ContentModel.PROP_OWNER);
		if (owner != null) {
			task.setOwner(DefaultTypeConverter.INSTANCE.convert(String.class, owner));
		}
		Serializable description = variablesDefault.get(WorkflowModel.PROP_DESCRIPTION);
		if (description != null) {
			task.setDescription(DefaultTypeConverter.INSTANCE.convert(String.class, description));
		}
		Serializable priority = variablesDefault.get(WorkflowModel.PROP_PRIORITY);
		if (priority != null) {
			task.setPriority(DefaultTypeConverter.INSTANCE.convert(Integer.class, priority));
		}
		Serializable dueDate = variablesDefault.get(WorkflowModel.PROP_DUE_DATE);
		if (dueDate != null) {
			task.setDueDate((Date) dueDate);
		}
		task.setName(
				DefaultTypeConverter.INSTANCE.convert(String.class, variablesDefault.remove(ContentModel.PROP_NAME)));
		return task;
	}

	/**
	 * Update user data for single task.
	 *
	 * @param variablesDefault
	 *            the variables default
	 */
	private void updateUserData(Map<QName, Serializable> variablesDefault) {
		if (variablesDefault.containsKey(WorkflowModel.ASSOC_ASSIGNEE)) {
			NodeRef person = getPerson(variablesDefault.get(WorkflowModel.ASSOC_ASSIGNEE));
			String assignee = getNodeService().getProperty(person, ContentModel.PROP_USERNAME).toString();
			variablesDefault.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
		}
		if (variablesDefault.containsKey(ContentModel.PROP_OWNER)) {
			NodeRef person = getPerson(variablesDefault.get(ContentModel.PROP_OWNER));
			String assignee = null;
			if (person != null) {
				assignee = getNodeService().getProperty(person, ContentModel.PROP_USERNAME).toString();
			}
			variablesDefault.put(ContentModel.PROP_OWNER, assignee);
		}

	}

	/**
	 * Gets the person.
	 *
	 * @param value
	 *            the value
	 * @return the person
	 */
	private NodeRef getPerson(Object value) {
		if (value == null) {
			return null;
		}
		Serializable userName = value.toString();
		if (NodeRef.isNodeRef(value.toString())) {
			NodeRef ref = new NodeRef(value.toString());
			if (getNodeService().exists(ref)) {
				userName = getNodeService().getProperty(ref, ContentModel.PROP_USERNAME);
			}
		}
		if (registry.getPersonService().personExists(userName.toString())) {
			return registry.getPersonService().getPerson(userName.toString());
		}
		throw new RuntimeException("Unrecognized user: " + value);
	}

	/**
	 * Update task metadata. Updates assignees, outcome and any other related
	 * metadata
	 *
	 * @param taskId
	 *            the task id
	 * @param props
	 *            the props to use
	 * @param itemToTaskId
	 *            is the context to attach task to
	 * @return the workflow task
	 * @throws Exception
	 *             any expected/unexpected event
	 */
	public WorkflowTask updateTask(String taskId, Map<QName, Serializable> props, String itemToTaskId)
			throws Exception {
		WorkflowTask updateTask = updateTask(taskId, props, itemToTaskId, false);

		return updateTask;
	}

	/**
	 * Internal update of metadata.
	 *
	 * @param taskId
	 *            the task id
	 * @param props
	 *            the props to use
	 * @param itemToTaskId
	 *            is the context to update if set or null if no change in
	 *            context is needed
	 * @param finalState
	 *            whether outcome should be set
	 * @return the workflow task to
	 * @throws Exception
	 *             the exception
	 */
	private WorkflowTask updateTask(String taskId, Map<QName, Serializable> props, String itemToTaskId,
			boolean finalState) throws Exception {
		String localId = WorkflowUtil.getActivitiWorkflowManager().getWorkflowEngine().createLocalId(taskId);
		TaskEntity taskInstance = (TaskEntity) getActivitiUtil().getTaskInstance(localId);
		Task task = updateTaskMetadata(props, taskInstance);
		TaskService taskService = getActivitiUtil().getTaskService();
		if (finalState) {
			// set the custom outcome
			setOutcome(task, null, props);
		}

		WorkflowTask converted = getTypeConverter().convertStandalone(task);
		if (itemToTaskId != null) {
			String nodeId = (String) converted.getProperties().get(WorkflowReportConstants.PROP_PACKAGE);

			populateItemToTaskId(itemToTaskId);
			QName attachmentId = QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, "attachmentID");
			if (this.itemToTask != null) {
				Serializable oldAttachment = converted.getProperties().get(attachmentId);
				if (oldAttachment == null || !oldAttachment.toString().contains(this.itemToTask.toString())) {
					NodeRef workflowPackage = caseService.getNodeRef(nodeId);
					attachItemsToTaskPackage(workflowPackage, true);
					props.put(attachmentId, this.itemToTask.toString());
				}
			}
		}
		taskService.saveTask(task);
		Map<QName, Serializable> prepareTaskProperties = getWorkflowReportService().prepareTaskProperties(converted,
				props);
		getPropertyConverter().updateMetadataAttachment(localId, prepareTaskProperties, null);
		return getTypeConverter().convertStandalone(task);
	}

	/**
	 * Cancel task and sets the provided metadata if so. The method uses the
	 * delete method from activiti for task.
	 *
	 * @param taskId
	 *            the task id
	 * @param props
	 *            the props to set. if null, operation is skipped.
	 * @return the workflow task as historic instance
	 * @throws Exception
	 *             the exception on any error
	 */
	public WorkflowTask cancelTask(String taskId, Map<QName, Serializable> props) throws Exception {
		String localId = WorkflowUtil.getActivitiWorkflowManager().getWorkflowEngine().createLocalId(taskId);
		if (props != null) {
			props.put(WorkflowModel.PROP_COMPLETION_DATE, new Date());
			updateTask(taskId, props, null, true);
		}
		getActivitiUtil().getTaskService().deleteTask(localId);
		// check if task exists
		Task taskById = getActivitiUtil().getTaskInstance(localId);
		if (taskById != null) {
			throw new RuntimeException("Task is not deleted! " + taskById);
		}
		HistoricTaskInstance singleResult = getActivitiUtil().getHistoryService().createHistoricTaskInstanceQuery()
				.taskId(localId).singleResult();
		WorkflowTask convert = getTypeConverter().convert(singleResult);
		getWorkflowReportService().endTask(convert);
		return convert;
	}

	/**
	 * Completes a task and sets the provided metadata if so. The method uses
	 * the endTask method of activiti engine
	 *
	 * @param taskId
	 *            the task id
	 * @param props
	 *            the props to set. if null, operation is skipped.
	 * @return the workflow task as historic instance
	 * @throws Exception
	 *             the exception on any error
	 */
	public WorkflowTask completeTask(String taskId, Map<QName, Serializable> props) throws Exception {
		if (props != null) {
			props.put(WorkflowModel.PROP_COMPLETION_DATE, new Date());
			updateTask(taskId, props, null, true);
		}
		WorkflowTask endTask = WorkflowUtil.getActivitiWorkflowManager().getWorkflowEngine().endTask(taskId, null);
		// update the reporting
		getWorkflowReportService().endTask(endTask);
		return endTask;
	}

	/**
	 * Sets the outcome.
	 *
	 * @param task
	 *            the task
	 * @param transition
	 *            the transition to take
	 * @param updates
	 *            map containing the data to use for outcome update
	 * @throws Exception
	 *             on any error
	 */
	private void setOutcome(Task task, String transition, Map<QName, Serializable> updates) throws Exception {
		String outcomeValue = ActivitiConstants.DEFAULT_TRANSITION_NAME;

		boolean isDefaultTransition = (transition == null)
				|| ActivitiConstants.DEFAULT_TRANSITION_NAME.equals(transition);

		Map<QName, Serializable> properties = getPropertyConverter().collectPropertiesForStandaloneTask(task);
		QName outcomePropName = (QName) properties.get(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
		if (outcomePropName != null) {
			if (isDefaultTransition == false) {
				outcomeValue = transition;
				Serializable transitionValue = getPropertyConverter().convertValueToPropertyType(task, transition,
						outcomePropName);
				updates.put(outcomePropName, transitionValue);
			} else {
				Serializable rawOutcome = updates.get(outcomePropName);
				if (rawOutcome != null) {
					outcomeValue = DefaultTypeConverter.INSTANCE.convert(String.class, rawOutcome);
				} else {
					rawOutcome = properties.get(outcomePropName);
					if (rawOutcome != null) {
						outcomeValue = DefaultTypeConverter.INSTANCE.convert(String.class, rawOutcome);
					}
				}
			}
		} else if (isDefaultTransition == false) {

			throw new WorkflowException("Invalid transition and outcome");
		}
		updates.put(WorkflowModel.PROP_OUTCOME, outcomeValue);

	}

}
