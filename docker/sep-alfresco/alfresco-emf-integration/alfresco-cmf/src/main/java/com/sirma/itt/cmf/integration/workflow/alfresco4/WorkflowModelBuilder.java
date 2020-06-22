/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.WorkflowReportService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * The Class WorkflowModelBuilder.
 *
 * @author Nick Smith
 * @since 3.4
 */
public class WorkflowModelBuilder {

	/** The Constant PERSON_LAST_NAME. */
	public static final String PERSON_LAST_NAME = "lastName";

	/** The Constant PERSON_FIRST_NAME. */
	public static final String PERSON_FIRST_NAME = "firstName";

	/** The Constant PERSON_USER_NAME. */
	public static final String PERSON_USER_NAME = "userName";

	/** The Constant PERSON_AVATAR. */
	public static final String PERSON_AVATAR = "avatarUrl";

	/** The Constant TASK_PROPERTIES. */
	public static final String TASK_PROPERTIES = "properties";

	/** The Constant TASK_PROPERTIY_LABELS. */
	public static final String TASK_PROPERTIY_LABELS = "propertyLabels";

	/** The Constant TASK_OWNER. */
	public static final String TASK_OWNER = "owner";

	/** The Constant TASK_STATE. */
	public static final String TASK_STATE = "state";

	/** The Constant TASK_DESCRIPTION. */
	public static final String TASK_DESCRIPTION = "description";

	/** The Constant TASK_TITLE. */
	public static final String TASK_TITLE = "title";

	/** The Constant TASK_NAME. */
	public static final String TASK_NAME = "name";

	/** The Constant TASK_URL. */
	public static final String TASK_URL = "url";

	/** The Constant TASK_IS_POOLED. */
	public static final String TASK_IS_POOLED = "isPooled";

	/** The Constant TASK_IS_EDITABLE. */
	public static final String TASK_IS_EDITABLE = "isEditable";

	/** The Constant TASK_IS_REASSIGNABLE. */
	public static final String TASK_IS_REASSIGNABLE = "isReassignable";

	/** The Constant TASK_IS_CLAIMABLE. */
	public static final String TASK_IS_CLAIMABLE = "isClaimable";

	/** The Constant TASK_IS_RELEASABLE. */
	public static final String TASK_IS_RELEASABLE = "isReleasable";

	/** The Constant TASK_ID. */
	public static final String TASK_ID = "id";

	/** The Constant TASK_PATH. */
	public static final String TASK_PATH = "path";

	/** The Constant TASK_DEFINITION. */
	public static final String TASK_DEFINITION = "definition";

	/** The Constant TASK_OUTCOME. */
	public static final String TASK_OUTCOME = "outcome";

	/** The Constant TASK_DEFINITION_ID. */
	public static final String TASK_DEFINITION_ID = "id";

	/** The Constant TASK_DEFINITION_URL. */
	public static final String TASK_DEFINITION_URL = "url";

	/** The Constant TASK_DEFINITION_TYPE. */
	public static final String TASK_DEFINITION_TYPE = "type";

	/** The Constant TASK_DEFINITION_NODE. */
	public static final String TASK_DEFINITION_NODE = "node";

	/** The Constant TASK_WORKFLOW_INSTANCE. */
	public static final String TASK_WORKFLOW_INSTANCE = "workflowInstance";

	/** The Constant TASK_WORKFLOW_INSTANCE_ID. */
	public static final String TASK_WORKFLOW_INSTANCE_ID = "id";

	/** The Constant TASK_WORKFLOW_INSTANCE_URL. */
	public static final String TASK_WORKFLOW_INSTANCE_URL = "url";

	/** The Constant TASK_WORKFLOW_INSTANCE_NAME. */
	public static final String TASK_WORKFLOW_INSTANCE_NAME = "name";

	/** The Constant TASK_WORKFLOW_INSTANCE_TITLE. */
	public static final String TASK_WORKFLOW_INSTANCE_TITLE = "title";

	/** The Constant TASK_WORKFLOW_INSTANCE_DESCRIPTION. */
	public static final String TASK_WORKFLOW_INSTANCE_DESCRIPTION = "description";

	/** The Constant TASK_WORKFLOW_INSTANCE_MESSAGE. */
	public static final String TASK_WORKFLOW_INSTANCE_MESSAGE = "message";

	/** The Constant TASK_WORKFLOW_INSTANCE_IS_ACTIVE. */
	public static final String TASK_WORKFLOW_INSTANCE_IS_ACTIVE = "isActive";

	/** The Constant TASK_WORKFLOW_INSTANCE_START_DATE. */
	public static final String TASK_WORKFLOW_INSTANCE_START_DATE = "startDate";

	/** The Constant TASK_WORKFLOW_INSTANCE_DUE_DATE. */
	public static final String TASK_WORKFLOW_INSTANCE_DUE_DATE = "dueDate";

	/** The Constant TASK_WORKFLOW_INSTANCE_END_DATE. */
	public static final String TASK_WORKFLOW_INSTANCE_END_DATE = "endDate";

	/** The Constant TASK_WORKFLOW_INSTANCE_PRIORITY. */
	public static final String TASK_WORKFLOW_INSTANCE_PRIORITY = "priority";

	/** The Constant TASK_WORKFLOW_INSTANCE_INITIATOR. */
	public static final String TASK_WORKFLOW_INSTANCE_INITIATOR = "initiator";

	/** The Constant TASK_WORKFLOW_INSTANCE_CONTEXT. */
	public static final String TASK_WORKFLOW_INSTANCE_CONTEXT = "context";

	/** The Constant TASK_WORKFLOW_INSTANCE_PACKAGE. */
	public static final String TASK_WORKFLOW_INSTANCE_PACKAGE = "package";

	/** The Constant TASK_WORKFLOW_INSTANCE_START_TASK_INSTANCE_ID. */
	public static final String TASK_WORKFLOW_INSTANCE_START_TASK_INSTANCE_ID = "startTaskInstanceId";

	/** The Constant TASK_WORKFLOW_INSTANCE_DEFINITION. */
	public static final String TASK_WORKFLOW_INSTANCE_DEFINITION = "definition";

	/** The Constant TASK_WORKFLOW_INSTANCE_TASKS. */
	public static final String TASK_WORKFLOW_INSTANCE_TASKS = "tasks";

	/** The Constant TASK_WORKFLOW_INSTANCE_DEFINITION_URL. */
	public static final String TASK_WORKFLOW_INSTANCE_DEFINITION_URL = "definitionUrl";

	/** The Constant TASK_WORKFLOW_INSTANCE_DIAGRAM_URL. */
	public static final String TASK_WORKFLOW_INSTANCE_DIAGRAM_URL = "diagramUrl";

	/** The Constant TASK_WORKFLOW_INSTANCE_INITIATOR_USERNAME. */
	public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_USERNAME = "userName";

	/** The Constant TASK_WORKFLOW_INSTANCE_INITIATOR_FIRSTNAME. */
	public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_FIRSTNAME = "firstName";

	/** The Constant TASK_WORKFLOW_INSTANCE_INITIATOR_LASTNAME. */
	public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_LASTNAME = "lastName";

	/** The Constant TYPE_DEFINITION_NAME. */
	public static final String TYPE_DEFINITION_NAME = "name";

	/** The Constant TYPE_DEFINITION_TITLE. */
	public static final String TYPE_DEFINITION_TITLE = "title";

	/** The Constant TYPE_DEFINITION_DESCRIPTION. */
	public static final String TYPE_DEFINITION_DESCRIPTION = "description";

	/** The Constant TYPE_DEFINITION_URL. */
	public static final String TYPE_DEFINITION_URL = "url";

	/** The Constant WORKFLOW_NODE_NAME. */
	public static final String WORKFLOW_NODE_NAME = "name";

	/** The Constant WORKFLOW_NODE_TITLE. */
	public static final String WORKFLOW_NODE_TITLE = "title";

	/** The Constant WORKFLOW_NODE_DESCRIPTION. */
	public static final String WORKFLOW_NODE_DESCRIPTION = "description";

	/** The Constant WORKFLOW_NODE_IS_TASK_NODE. */
	public static final String WORKFLOW_NODE_IS_TASK_NODE = "isTaskNode";

	/** The Constant WORKFLOW_NODE_TRANSITIONS. */
	public static final String WORKFLOW_NODE_TRANSITIONS = "transitions";

	/** The Constant WORKFLOW_NODE_TRANSITION_ID. */
	public static final String WORKFLOW_NODE_TRANSITION_ID = "id";

	/** The Constant DEFINITION_ID. */
	public static final String DEFINITION_ID = "definitionId";

	/** The Constant WORKFLOW_NODE_TRANSITION_TITLE. */
	public static final String WORKFLOW_NODE_TRANSITION_TITLE = "title";

	/** The Constant WORKFLOW_NODE_TRANSITION_DESCRIPTION. */
	public static final String WORKFLOW_NODE_TRANSITION_DESCRIPTION = "description";

	/** The Constant WORKFLOW_NODE_TRANSITION_IS_DEFAULT. */
	public static final String WORKFLOW_NODE_TRANSITION_IS_DEFAULT = "isDefault";

	/** The Constant WORKFLOW_NODE_TRANSITION_IS_HIDDEN. */
	public static final String WORKFLOW_NODE_TRANSITION_IS_HIDDEN = "isHidden";

	/** The Constant WORKFLOW_DEFINITION_ID. */
	public static final String WORKFLOW_DEFINITION_ID = "id";

	/** The Constant WORKFLOW_DEFINITION_URL. */
	public static final String WORKFLOW_DEFINITION_URL = "url";

	/** The Constant WORKFLOW_DEFINITION_NAME. */
	public static final String WORKFLOW_DEFINITION_NAME = "name";

	/** The Constant WORKFLOW_DEFINITION_TITLE. */
	public static final String WORKFLOW_DEFINITION_TITLE = "title";

	/** The Constant WORKFLOW_DEFINITION_DESCRIPTION. */
	public static final String WORKFLOW_DEFINITION_DESCRIPTION = "description";

	/** The Constant WORKFLOW_DEFINITION_VERSION. */
	public static final String WORKFLOW_DEFINITION_VERSION = "version";

	/** The Constant WORKFLOW_DEFINITION_START_TASK_DEFINITION_URL. */
	public static final String WORKFLOW_DEFINITION_START_TASK_DEFINITION_URL = "startTaskDefinitionUrl";

	/** The Constant WORKFLOW_DEFINITION_START_TASK_DEFINITION_TYPE. */
	public static final String WORKFLOW_DEFINITION_START_TASK_DEFINITION_TYPE = "startTaskDefinitionType";

	/** The Constant WORKFLOW_DEFINITION_TASK_DEFINITIONS. */
	public static final String WORKFLOW_DEFINITION_TASK_DEFINITIONS = "taskDefinitions";

	/** The Constant TASK_OUTCOME_MESSAGE_PREFIX. */
	public static final String TASK_OUTCOME_MESSAGE_PREFIX = "workflowtask.outcome.";

	/** The node service. */
	private final NodeService nodeService;
	//
	// /** The person service. */
	// private final PersonService personService;

	/** The workflow service. */
	private final WorkflowService workflowService;

	/** The authentication service. */
	private final AuthenticationService authenticationService;

	/** The q name converter. */
	private final WorkflowQNameConverter qNameConverter;

	private static final Map<TypeDefinition, Collection<QName>> TASK_METADATA_CACHE = new HashMap<TypeDefinition, Collection<QName>>();

	private static final String TASK_NODE = "nodeRef";

	private WorkflowReportService workflowReportService;

	/**
	 * Instantiates a new workflow model builder.
	 *
	 * @param namespaceService
	 *            the namespace service
	 * @param nodeService
	 *            the node service
	 * @param authenticationService
	 *            the authentication service
	 * @param personService
	 *            the person service
	 * @param workflowService
	 *            the workflow service
	 * @param workflowReportService
	 *            is the service used as backend to store task as node
	 */
	public WorkflowModelBuilder(NamespaceService namespaceService, NodeService nodeService,
			AuthenticationService authenticationService, PersonService personService,
			WorkflowService workflowService, WorkflowReportService workflowReportService) {
		this.nodeService = nodeService;
		// this.personService = personService;
		this.workflowService = workflowService;
		this.authenticationService = authenticationService;
		this.workflowReportService = workflowReportService;
		this.qNameConverter = new WorkflowQNameConverter(namespaceService);
	}

	/**
	 * Returns a simple representation of a {@link WorkflowTask}.
	 *
	 * @param task
	 *            The task to be represented.
	 * @param propertyFilters
	 *            Specify which properties to include.
	 * @param includeWorkflow
	 *            whether to include the workflow instance data
	 * @param includeProperties
	 *            whether to build and include properties of task
	 * @return the map of data for task
	 */
	public Map<String, Object> buildSimple(WorkflowTask task, Collection<String> propertyFilters,
			boolean includeWorkflow, boolean includeProperties) {
		// get current username
		String currentUser = this.authenticationService.getCurrentUserName();

		HashMap<String, Object> model = new HashMap<String, Object>();
		NodeRef taskNode = null;
		if ((taskNode = workflowReportService.getTaskNode(task)) == null) {
			// try by metadata
			Serializable nodeId = task.getProperties().get(ContentModel.PROP_NODE_UUID);
			if (nodeId != null) {
				taskNode = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId.toString());
			}
		}
		if (taskNode == null) {
			throw new RuntimeException("NodeRef for " + task
					+ " could not be found! Please try again.");
		}
		model.put(TASK_NODE, taskNode.toString());
		if (task.getPath() == null) {
			model.put(DEFINITION_ID, task.getDefinition().getId());
			model.put(TASK_ID, task.getId());
			model.put(TASK_URL, getUrl(task));
			model.put(TASK_NAME, task.getName());
			model.put(TASK_TITLE, task.getTitle());
			model.put(TASK_DESCRIPTION, task.getDescription());
			model.put(TASK_STATE, task.getState().name());
			model.put(TASK_PATH, "");
			model.put(TASK_OUTCOME, getOutcome(task));
			model.put(TASK_IS_POOLED, isPooled(task.getProperties()));
			model.put(TASK_IS_EDITABLE, this.workflowService.isTaskEditable(task, currentUser));
			model.put(TASK_IS_REASSIGNABLE,
					this.workflowService.isTaskReassignable(task, currentUser));
			model.put(TASK_IS_CLAIMABLE, this.workflowService.isTaskClaimable(task, currentUser));
			model.put(TASK_IS_RELEASABLE, this.workflowService.isTaskReleasable(task, currentUser));

			Serializable owner = task.getProperties().get(ContentModel.PROP_OWNER);
			model.put(TASK_OWNER, getPersonModel(owner));

			// task properties
			if (includeProperties) {
				Map<String, Object> propertyModel = buildProperties(task, propertyFilters);
				model.put(TASK_PROPERTIES, propertyModel);
				model.put(TASK_PROPERTIY_LABELS, buildPropertyLabels(task, propertyModel));
			} else {
				HashMap<String, String> map = new HashMap<String, String>(1);
				model.put(TASK_PROPERTIES, map);
				model.put(TASK_PROPERTIY_LABELS, map);
			}
			return model;
		}
		if (task.getDefinition().getNode() == null) {
			return model;
		}
		String definitionId = task.getDefinition().getNode().getName();
		model.put(DEFINITION_ID, definitionId);
		model.put(TASK_ID, task.getId());
		model.put(TASK_URL, getUrl(task));
		model.put(TASK_NAME, task.getName());
		model.put(TASK_TITLE, task.getTitle());
		model.put(TASK_DESCRIPTION, task.getDescription());
		model.put(TASK_STATE, task.getState().name());
		model.put(TASK_PATH, getUrl(task.getPath()));
		model.put(TASK_OUTCOME, getOutcome(task));
		model.put(TASK_IS_POOLED, isPooled(task.getProperties()));
		model.put(TASK_IS_EDITABLE, this.workflowService.isTaskEditable(task, currentUser));
		model.put(TASK_IS_REASSIGNABLE, this.workflowService.isTaskReassignable(task, currentUser));
		model.put(TASK_IS_CLAIMABLE, this.workflowService.isTaskClaimable(task, currentUser));
		model.put(TASK_IS_RELEASABLE, this.workflowService.isTaskReleasable(task, currentUser));

		Serializable owner = task.getProperties().get(ContentModel.PROP_OWNER);
		model.put(TASK_OWNER, getPersonModel(owner));
		// task properties
		if (includeProperties) {
			Map<String, Object> propertyModel = buildProperties(task, propertyFilters);
			model.put(TASK_PROPERTIES, propertyModel);
			model.put(TASK_PROPERTIY_LABELS, buildPropertyLabels(task, propertyModel));
		} else {
			HashMap<String, String> map = new HashMap<String, String>(1);
			model.put(TASK_PROPERTIES, map);
			model.put(TASK_PROPERTIY_LABELS, map);
		}
		// TODO for now skip optimization
		// if (includeWorkflow) {
		// workflow instance part
		model.put(TASK_WORKFLOW_INSTANCE, buildDetailed(task.getPath().getInstance(), false));
		// }
		return model;
	}

	/**
	 * Returns a detailed representation of a {@link WorkflowTask}.
	 *
	 * @param workflowTask
	 *            The task to be represented.
	 * @param includeWorkflow
	 *            whether to include the workflow instance data
	 * @return the map
	 */
	public Map<String, Object> buildDetailed(WorkflowTask workflowTask, boolean includeWorkflow) {
		Map<String, Object> model = buildSimple(workflowTask, null, includeWorkflow, true);
		// definition part
		model.put(TASK_DEFINITION, buildTaskDefinition(workflowTask.getDefinition(), workflowTask));

		return model;
	}

	/**
	 * Returns a simple representation of a {@link WorkflowInstance}.
	 *
	 * @param workflowInstance
	 *            The workflow instance to be represented.
	 * @return the map
	 */
	public Map<String, Object> buildSimple(WorkflowInstance workflowInstance) {
		Map<String, Object> model = new HashMap<String, Object>();

		model.put(TASK_WORKFLOW_INSTANCE_ID, workflowInstance.getId());
		model.put(TASK_WORKFLOW_INSTANCE_URL, getUrl(workflowInstance));
		model.put(TASK_WORKFLOW_INSTANCE_NAME, workflowInstance.getDefinition().getName());
		model.put(TASK_WORKFLOW_INSTANCE_TITLE, workflowInstance.getDefinition().getTitle());
		model.put(TASK_WORKFLOW_INSTANCE_DESCRIPTION, workflowInstance.getDefinition()
				.getDescription());
		model.put(TASK_WORKFLOW_INSTANCE_MESSAGE, workflowInstance.getDescription());
		model.put(TASK_WORKFLOW_INSTANCE_IS_ACTIVE, workflowInstance.isActive());
		model.put(TASK_WORKFLOW_INSTANCE_PRIORITY, workflowInstance.getPriority());
		model.put(TASK_WORKFLOW_INSTANCE_DEFINITION_URL, getUrl(workflowInstance.getDefinition()));

		if (workflowInstance.getWorkflowPackage() != null) {
			model.put(TASK_WORKFLOW_INSTANCE_PACKAGE, workflowInstance.getWorkflowPackage()
					.toString());
		}
		Map<QName, Serializable> properties = nodeService.getProperties(workflowInstance
				.getWorkflowPackage());
		Map<String, Serializable> workflowCustomProps = new HashMap<String, Serializable>(5, 10);
		Set<Entry<QName, Serializable>> entrySet = properties.entrySet();
		for (Entry<QName, Serializable> entry : entrySet) {
			if (CMFModel.CMF_WORKFLOW_MODEL_1_0_URI.equals(entry.getKey().getNamespaceURI())
					|| CMFModel.CMF_MODEL_PREFIX.equals(entry.getKey().getNamespaceURI())) {
				workflowCustomProps.put(entry.getKey().getLocalName(), entry.getValue());
			}
		}
		if (workflowInstance.getEndDate() == null) {
			workflowCustomProps.put(TASK_WORKFLOW_INSTANCE_END_DATE, workflowInstance.getEndDate());
		} else {
			workflowCustomProps.put(TASK_WORKFLOW_INSTANCE_END_DATE,
					ISO8601DateFormat.format(workflowInstance.getEndDate()));
		}
		model.put(TASK_PROPERTIES, workflowCustomProps);
		if (workflowInstance.getContext() != null) {
			model.put(TASK_WORKFLOW_INSTANCE_CONTEXT, workflowInstance.getContext().toString());
		}

		if (workflowInstance.getStartDate() == null) {
			model.put(TASK_WORKFLOW_INSTANCE_START_DATE, workflowInstance.getStartDate());
		} else {
			model.put(TASK_WORKFLOW_INSTANCE_START_DATE,
					ISO8601DateFormat.format(workflowInstance.getStartDate()));
		}

		if (workflowInstance.getDueDate() == null) {
			model.put(TASK_WORKFLOW_INSTANCE_DUE_DATE, workflowInstance.getDueDate());
		} else {
			model.put(TASK_WORKFLOW_INSTANCE_DUE_DATE,
					ISO8601DateFormat.format(workflowInstance.getDueDate()));
		}

		if (workflowInstance.getInitiator() == null
				|| !nodeService.exists(workflowInstance.getInitiator())) {
			model.put(TASK_WORKFLOW_INSTANCE_INITIATOR, null);
		} else {
			model.put(TASK_WORKFLOW_INSTANCE_INITIATOR, getPersonModel(nodeService.getProperty(
					workflowInstance.getInitiator(), ContentModel.PROP_USERNAME)));
		}

		return model;
	}

	/**
	 * Returns a detailed representation of a {@link WorkflowInstance}.
	 *
	 * @param workflowInstance
	 *            The workflow instance to be represented.
	 * @param includeTasks
	 *            should we include task in model?
	 * @return the map
	 */
	public Map<String, Object> buildDetailed(WorkflowInstance workflowInstance, boolean includeTasks) {
		Map<String, Object> model = buildSimple(workflowInstance);

		Serializable startTaskId = null;
		WorkflowTask startTask = workflowService.getStartTask(workflowInstance.getId());
		if (startTask != null) {
			startTaskId = startTask.getId();
		}

		if (workflowService.hasWorkflowImage(workflowInstance.getId())) {
			model.put(TASK_WORKFLOW_INSTANCE_DIAGRAM_URL, getDiagramUrl(workflowInstance));
		}

		model.put(TASK_WORKFLOW_INSTANCE_START_TASK_INSTANCE_ID, startTaskId);
		model.put(TASK_WORKFLOW_INSTANCE_DEFINITION,
				buildDetailed(workflowInstance.getDefinition()));

		if (includeTasks) {
			// get all tasks for workflow
			WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
			tasksQuery.setTaskState(null);
			tasksQuery.setActive(null);
			tasksQuery.setProcessId(workflowInstance.getId());
			List<WorkflowTask> tasks = workflowService.queryTasks(tasksQuery, true);

			ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>(
					tasks.size());

			for (WorkflowTask task : tasks) {
				results.add(buildSimple(task, null, false, true));
			}

			model.put(TASK_WORKFLOW_INSTANCE_TASKS, results);
		}

		return model;
	}

	/**
	 * Returns a simple representation of a {@link WorkflowDefinition}.
	 *
	 * @param workflowDefinition
	 *            the WorkflowDefinition object to be represented.
	 * @return the map
	 */
	public Map<String, Object> buildSimple(WorkflowDefinition workflowDefinition) {
		HashMap<String, Object> model = new HashMap<String, Object>();

		model.put(WORKFLOW_DEFINITION_ID, workflowDefinition.getId());
		model.put(WORKFLOW_DEFINITION_URL, getUrl(workflowDefinition));
		model.put(WORKFLOW_DEFINITION_NAME, workflowDefinition.getName());
		model.put(WORKFLOW_DEFINITION_TITLE, workflowDefinition.getTitle());
		model.put(WORKFLOW_DEFINITION_DESCRIPTION, workflowDefinition.getDescription());
		model.put(WORKFLOW_DEFINITION_VERSION, workflowDefinition.getVersion());

		return model;
	}

	/**
	 * Returns a detailed representation of a {@link WorkflowDefinition}.
	 *
	 * @param workflowDefinition
	 *            the WorkflowDefinition object to be represented.
	 * @return the map
	 */
	public Map<String, Object> buildDetailed(WorkflowDefinition workflowDefinition) {
		Map<String, Object> model = buildSimple(workflowDefinition);

		model.put(WORKFLOW_DEFINITION_START_TASK_DEFINITION_URL, getUrl(workflowDefinition
				.getStartTaskDefinition().getMetadata()));
		model.put(WORKFLOW_DEFINITION_START_TASK_DEFINITION_TYPE, workflowDefinition
				.getStartTaskDefinition().getMetadata().getName());

		ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		for (WorkflowTaskDefinition taskDefinition : workflowService
				.getTaskDefinitions(workflowDefinition.getId())) {
			if (taskDefinition.getId().equals(workflowDefinition.getStartTaskDefinition().getId())) {
				continue;
			}

			Map<String, Object> result = new HashMap<String, Object>();

			result.put(TASK_DEFINITION_URL, getUrl(taskDefinition.getMetadata()));
			result.put(TASK_DEFINITION_TYPE, taskDefinition.getMetadata().getName());

			results.add(result);
		}
		model.put(WORKFLOW_DEFINITION_TASK_DEFINITIONS, results);

		return model;
	}

	/**
	 * Checks if is pooled.
	 *
	 * @param properties
	 *            the properties
	 * @return the object
	 */
	private Object isPooled(Map<QName, Serializable> properties) {
		Collection<?> actors = (Collection<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
		return actors != null && !actors.isEmpty();
	}

	/**
	 * Builds the properties.
	 *
	 * @param task
	 *            the task
	 * @param propertyFilters
	 *            the property filters
	 * @return the map
	 */
	private Map<String, Object> buildProperties(WorkflowTask task,
			Collection<String> propertyFilters) {
		Map<QName, Serializable> properties = task.getProperties();
		Collection<QName> keys;
		if (propertyFilters == null || propertyFilters.size() == 0) {
			TypeDefinition taskType = task.getDefinition().getMetadata();
			if (TASK_METADATA_CACHE.containsKey(taskType)) {
				keys = TASK_METADATA_CACHE.get(taskType);
			} else {
				Map<QName, PropertyDefinition> propDefs = taskType.getProperties();
				Map<QName, AssociationDefinition> assocDefs = taskType.getAssociations();
				Set<QName> propKeys = properties.keySet();
				keys = new HashSet<QName>(propDefs.size() + assocDefs.size() + propKeys.size());
				keys.addAll(propDefs.keySet());
				keys.addAll(assocDefs.keySet());
				keys.addAll(propKeys);
				TASK_METADATA_CACHE.put(taskType, keys);
			}
		} else {
			keys = buildQNameKeys(propertyFilters);
		}
		boolean multiAssigned = false;
		if (properties.containsKey(CMFModel.ASSOC_TASK_MULTI_ASSIGNEES)) {
			Serializable serializable = properties.get(CMFModel.ASSOC_TASK_MULTI_ASSIGNEES);
			if (serializable != null) {
				multiAssigned = true;
				// remove the duplicated data
				properties.remove(WorkflowModel.ASSOC_GROUP_ASSIGNEES);
				properties.remove(WorkflowModel.ASSOC_ASSIGNEES);
			}
		}
		if (!multiAssigned) {
			// updates assignees as usernames/groupnames
			if (properties.containsKey(WorkflowModel.ASSOC_ASSIGNEE)) {
				Serializable serializable = properties.get(WorkflowModel.ASSOC_ASSIGNEE);
				if (serializable == null) {
					properties.put(WorkflowModel.ASSOC_ASSIGNEE, null);
				} else {
					boolean nodeRef = NodeRef.isNodeRef(serializable.toString());
					if (nodeRef) {
						NodeRef userRef = new NodeRef(serializable.toString());
						if (nodeService.exists(userRef)) {
							properties.put(WorkflowModel.ASSOC_ASSIGNEE,
									nodeService.getProperty(userRef, ContentModel.PROP_USERNAME));
						} else {
							properties.put(WorkflowModel.ASSOC_ASSIGNEE, serializable);
						}
					}
				}
			}
			if (properties.containsKey(WorkflowModel.ASSOC_GROUP_ASSIGNEE)) {
				Serializable serializable = properties.get(WorkflowModel.ASSOC_GROUP_ASSIGNEE);
				if (serializable == null) {
					properties.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, null);
				} else {
					boolean nodeRef = NodeRef.isNodeRef(serializable.toString());
					if (nodeRef) {
						NodeRef userRef = new NodeRef(serializable.toString());
						if (nodeService.exists(userRef)) {
							properties.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, nodeService
									.getProperty(userRef, ContentModel.PROP_AUTHORITY_NAME));
						} else {
							properties.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, serializable);
						}
					}
				}
			}
		}
		// properties.put(QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
		// "taskOutcome"),
		// properties.get(WorkflowModel.PROP_OUTCOME));
		return buildQNameProperties(properties, keys);
	}

	/**
	 * Builds the property labels.
	 *
	 * @param task
	 *            the task
	 * @param properties
	 *            the properties
	 * @return the map
	 */
	private Map<String, String> buildPropertyLabels(WorkflowTask task,
			Map<String, Object> properties) {
		TypeDefinition taskType = task.getDefinition().getMetadata();
		final Map<QName, PropertyDefinition> propDefs = taskType.getProperties();
		return CollectionUtils.transform(properties,
				new Function<Entry<String, Object>, Pair<String, String>>() {
					@Override
					public Pair<String, String> apply(Entry<String, Object> entry) {
						String propName = entry.getKey();
						PropertyDefinition propDef = propDefs.get(qNameConverter
								.mapNameToQName(propName));
						if (propDef != null) {
							List<ConstraintDefinition> constraints = propDef.getConstraints();
							for (ConstraintDefinition constraintDef : constraints) {
								Constraint constraint = constraintDef.getConstraint();
								if (constraint instanceof ListOfValuesConstraint) {
									ListOfValuesConstraint listConstraint = (ListOfValuesConstraint) constraint;
									String label = listConstraint.getDisplayLabel(entry.getValue()
											.toString());
									return new Pair<String, String>(propName, label);
								}
							}
						}
						return null;
					}
				});
	}

	/**
	 * Builds the q name properties.
	 *
	 * @param properties
	 *            the properties
	 * @param keys
	 *            the keys
	 * @return the map
	 */
	private Map<String, Object> buildQNameProperties(Map<QName, Serializable> properties,
			Collection<QName> keys) {
		Map<String, Object> model = new HashMap<String, Object>(properties.size());
		Set<Entry<QName, Serializable>> entrySet = properties.entrySet();
		for (Entry<QName, Serializable> entry : entrySet) {
			Object value = convertValue(entry.getValue());
			if (value != null) {
				String strKey = qNameConverter.mapQNameToName(entry.getKey());
				model.put(strKey, value);
			}
		}
		// for (QName key : keys) {
		// System.out.println(key.getLocalName());
		// if (key.getLocalName().endsWith("ActionGroup")){
		// continue;
		// }
		// Object value = convertValue(properties.get(key));
		// //
		// }
		return model;
	}

	/**
	 * Convert value.
	 *
	 * @param value
	 *            the value
	 * @return the object
	 */
	private Object convertValue(Object value) {
		if (value == null || value instanceof Boolean || value instanceof Number
				|| value instanceof String) {
			return value;
		}

		if (value instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) value;
			ArrayList<Object> results = new ArrayList<Object>(collection.size());
			for (Object obj : collection) {
				results.add(convertValue(obj));
			}
			return results;
		}

		return DefaultTypeConverter.INSTANCE.convert(String.class, value);
	}

	/**
	 * Builds the q name keys.
	 *
	 * @param keys
	 *            the keys
	 * @return the collection
	 */
	private Collection<QName> buildQNameKeys(Collection<String> keys) {
		return CollectionUtils.transform(keys, new Function<String, QName>() {
			@Override
			public QName apply(String name) {
				return qNameConverter.mapNameToQName(name);
			}
		});
	}

	/**
	 * Gets the person model.
	 *
	 * @param nameSer
	 *            the name ser
	 * @return the person model
	 */
	private Map<String, Object> getPersonModel(Serializable nameSer) {
		if (!(nameSer instanceof String))
			return null;

		String name = (String) nameSer;
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(PERSON_USER_NAME, name);

		// if (personService.personExists(name)) {
		// NodeRef person = personService.getPerson(name);
		// Map<QName, Serializable> properties =
		// nodeService.getProperties(person);
		// model.put(PERSON_FIRST_NAME,
		// properties.get(ContentModel.PROP_FIRSTNAME));
		// model.put(PERSON_LAST_NAME,
		// properties.get(ContentModel.PROP_LASTNAME));
		//
		// // add the avatar, id present
		// List<AssociationRef> avatar = nodeService.getTargetAssocs(person,
		// ContentModel.ASSOC_AVATAR);
		// if (avatar != null && !avatar.isEmpty()) {
		// model.put(PERSON_AVATAR, getAvatarUrl(avatar.get(0).getTargetRef()));
		// }
		// }

		return model;
	}

	/**
	 * Builds the task definition.
	 *
	 * @param workflowTaskDefinition
	 *            the workflow task definition
	 * @param workflowTask
	 *            the workflow task
	 * @return the map
	 */
	private Map<String, Object> buildTaskDefinition(WorkflowTaskDefinition workflowTaskDefinition,
			WorkflowTask workflowTask) {
		Map<String, Object> model = new HashMap<String, Object>();

		model.put(TASK_DEFINITION_ID, workflowTaskDefinition.getId());
		model.put(TASK_DEFINITION_URL, getUrl(workflowTaskDefinition));
		model.put(TASK_DEFINITION_TYPE, buildTypeDefinition(workflowTaskDefinition.getMetadata()));
		model.put(TASK_DEFINITION_NODE,
				buildWorkflowNode(workflowTaskDefinition.getNode(), workflowTask));

		return model;
	}

	/**
	 * Builds the type definition.
	 *
	 * @param typeDefinition
	 *            the type definition
	 * @return the map
	 */
	private Map<String, Object> buildTypeDefinition(TypeDefinition typeDefinition) {
		Map<String, Object> model = new HashMap<String, Object>();

		model.put(TYPE_DEFINITION_NAME, typeDefinition.getName());
		model.put(TYPE_DEFINITION_TITLE, typeDefinition.getTitle());
		model.put(TYPE_DEFINITION_DESCRIPTION, typeDefinition.getDescription());
		model.put(TYPE_DEFINITION_URL, getUrl(typeDefinition));

		return model;
	}

	/**
	 * Builds the workflow node.
	 *
	 * @param workflowNode
	 *            the workflow node
	 * @param workflowTask
	 *            the workflow task
	 * @return the map
	 */
	private Map<String, Object> buildWorkflowNode(WorkflowNode workflowNode,
			WorkflowTask workflowTask) {
		Map<String, Object> model = new HashMap<String, Object>();

		model.put(WORKFLOW_NODE_NAME, workflowNode.getName());
		model.put(WORKFLOW_NODE_TITLE, workflowNode.getTitle());
		model.put(WORKFLOW_NODE_DESCRIPTION, workflowNode.getDescription());
		model.put(WORKFLOW_NODE_IS_TASK_NODE, workflowNode.isTaskNode());

		List<Map<String, Object>> transitions = new ArrayList<Map<String, Object>>();
		List<?> hiddenTransitions = getHiddenTransitions(workflowTask.getProperties());
		for (WorkflowTransition workflowTransition : workflowNode.getTransitions()) {
			Map<String, Object> transitionModel = buildTransition(workflowTransition,
					hiddenTransitions);
			transitions.add(transitionModel);
		}
		model.put(WORKFLOW_NODE_TRANSITIONS, transitions);
		return model;
	}

	/**
	 * Builds the transition.
	 *
	 * @param workflowTransition
	 *            the workflow transition
	 * @param hiddenTransitions
	 *            the hidden transitions
	 * @return the map
	 */
	private Map<String, Object> buildTransition(WorkflowTransition workflowTransition,
			List<?> hiddenTransitions) {
		Map<String, Object> model = new HashMap<String, Object>();
		String id = workflowTransition.getId();
		model.put(WORKFLOW_NODE_TRANSITION_ID, id == null ? "" : id);
		model.put(WORKFLOW_NODE_TRANSITION_TITLE, workflowTransition.getTitle());
		model.put(WORKFLOW_NODE_TRANSITION_DESCRIPTION, workflowTransition.getDescription());
		model.put(WORKFLOW_NODE_TRANSITION_IS_DEFAULT, workflowTransition.isDefault());
		model.put(WORKFLOW_NODE_TRANSITION_IS_HIDDEN, isHiddenTransition(id, hiddenTransitions));
		return model;
	}

	/**
	 * Gets the hidden transitions.
	 *
	 * @param properties
	 *            the properties
	 * @return the hidden transitions
	 */
	private List<?> getHiddenTransitions(Map<QName, Serializable> properties) {
		Serializable hiddenSer = properties.get(WorkflowModel.PROP_HIDDEN_TRANSITIONS);
		if (hiddenSer instanceof List<?>) {
			return (List<?>) hiddenSer;
		} else if (hiddenSer instanceof String) {
			String hiddenStr = (String) hiddenSer;
			return Arrays.asList(hiddenStr.split(","));
		}
		return null;
	}

	/**
	 * Checks if is hidden transition.
	 *
	 * @param transitionId
	 *            the transition id
	 * @param hiddenTransitions
	 *            the hidden transitions
	 * @return true, if is hidden transition
	 */
	private boolean isHiddenTransition(String transitionId, List<?> hiddenTransitions) {
		if (hiddenTransitions == null)
			return false;

		return hiddenTransitions.contains(transitionId);
	}

	/**
	 * Gets the outcome.
	 *
	 * @param task
	 *            the task
	 * @return the outcome
	 */
	private String getOutcome(WorkflowTask task) {
		String outcomeLabel = null;

		// there will only be an outcome if the task is completed
		if (task.getState().equals(WorkflowTaskState.COMPLETED)) {
			String outcomeId = (String) task.getProperties().get(WorkflowModel.PROP_OUTCOME);
			if (outcomeId != null) {
				// find the transition with the matching id and get the label
				if (task.getDefinition() != null && task.getDefinition().getNode() != null) {
					WorkflowTransition[] transitions = task.getDefinition().getNode()
							.getTransitions();
					for (WorkflowTransition transition : transitions) {
						if (transition.getId().equals(outcomeId)) {
							outcomeLabel = transition.getTitle();
							break;
						}
					}
				} else {
					outcomeLabel = outcomeId;
				}
				if (outcomeLabel == null) {
					String translatedOutcome = I18NUtil.getMessage(TASK_OUTCOME_MESSAGE_PREFIX
							+ outcomeId);
					if (translatedOutcome != null) {
						outcomeLabel = translatedOutcome;
					} else {
						outcomeLabel = outcomeId;
					}
				}
			}
		}

		return outcomeLabel;
	}

	/**
	 * Gets the url.
	 *
	 * @param task
	 *            the task
	 * @return the url
	 */
	private String getUrl(WorkflowTask task) {
		return "api/task-instances/" + task.getId();
	}

	/**
	 * Gets the url.
	 *
	 * @param workflowDefinition
	 *            the workflow definition
	 * @return the url
	 */
	private String getUrl(WorkflowDefinition workflowDefinition) {
		return "api/workflow-definitions/" + workflowDefinition.getId();
	}

	/**
	 * Gets the url.
	 *
	 * @param workflowTaskDefinition
	 *            the workflow task definition
	 * @return the url
	 */
	private String getUrl(WorkflowTaskDefinition workflowTaskDefinition) {
		return "api/task-definitions/" + workflowTaskDefinition.getId();
	}

	/**
	 * Gets the url.
	 *
	 * @param typeDefinition
	 *            the type definition
	 * @return the url
	 */
	private String getUrl(TypeDefinition typeDefinition) {
		return "api/classes/" + qNameConverter.mapQNameToName(typeDefinition.getName());
	}

	/**
	 * Gets the url.
	 *
	 * @param path
	 *            the path
	 * @return the url
	 */
	private String getUrl(WorkflowPath path) {
		return "api/workflow-paths/" + path.getId();
	}

	/**
	 * Gets the url.
	 *
	 * @param workflowInstance
	 *            the workflow instance
	 * @return the url
	 */
	private String getUrl(WorkflowInstance workflowInstance) {
		return "api/workflow-instances/" + workflowInstance.getId();
	}

	/**
	 * Gets the diagram url.
	 *
	 * @param workflowInstance
	 *            the workflow instance
	 * @return the diagram url
	 */
	private String getDiagramUrl(WorkflowInstance workflowInstance) {
		return "api/workflow-instances/" + workflowInstance.getId() + "/diagram";
	}

	// /**
	// * Gets the avatar url.
	// *
	// * @param avatarRef
	// * the avatar ref
	// * @return the avatar url
	// */
	// private String getAvatarUrl(NodeRef avatarRef) {
	// return "api/node/" + avatarRef.toString().replace("://", "/")
	// + "/content/thumbnails/avatar";
	// }
}
