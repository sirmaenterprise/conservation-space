/**
 *
 */
package com.sirma.itt.cmf.integration.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.webscript.BaseAlfrescoScript;
import com.sirma.itt.cmf.integration.workflow.alfresco4.WorkflowModelBuilder;

/**
 * Class responsible to processes all requests regarding state of workflow.
 *
 * @author bbanchev
 */
public class WorkflowInstanceScript extends BaseAlfrescoScript {

	/** The Constant KEY_TASK_ID. */
	private static final String KEY_TASK_ID = "taskId";
	/** The Constant KEY_PARENT_TASK_ID. */
	private static final String KEY_PARENT_TASK_ID = "taskParentId";

	/** The Constant KEY_TASK_TYPE. */
	private static final String KEY_TASK_TYPE = "type";
	/** The Constant KEY_WORKFLOW_ID. */
	private static final String KEY_WORKFLOW_ID = "workflowId";

	/** The Constant NEXT_STATE_PROP_MAP. */
	private static final String NEXT_STATE_PROP_MAP = "nextStateProperties";
	private static final String CURRENT_STATE_PROP_MAP = "currentStateProperties";
	/** The model builder. */
	private WorkflowModelBuilder modelBuilder;

	/**
	 * Execute internal. Wrapper for system user action.
	 *
	 * @param req
	 *            the original request
	 * @return the updated model
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		Map<String, Object> model = new HashMap<String, Object>(2);
		model.put(KEY_WORKING_MODE, "unknown");
		String servicePath = req.getServicePath();
		try {
			JSONObject jsonObject = new JSONObject(req.getContent().getContent());
			if (servicePath.contains("/workflow/instance/start")) {
				startWorkflow(model, jsonObject);
			} else if (servicePath.contains("/workflow/instance/transition")) {
				navigateTask(model, jsonObject);
			} else if (servicePath.contains("/workflow/instance/taskupdate")) {
				updateTask(model, jsonObject);
			} else if (servicePath.contains("/workflow/instance/cancel")) {
				cancelWorkflow(model, jsonObject);
			} else if (servicePath.contains("/task/instance/start")) {
				startTask(model, jsonObject);
			} else if (servicePath.contains("/task/instance/cancel")) {
				cancelTask(model, jsonObject);
			} else if (servicePath.contains("/task/instance/complete")) {
				navigateTask(model, jsonObject);
			} else if (servicePath.contains("/task/instance/taskupdate")) {
				updateTask(model, jsonObject);
			}
		} catch (WebScriptException e) {
			throw e;
		} catch (Exception e) {
			throw new WebScriptException(500,
					"Unexpected error occurred during workflow/task operation: " + e.getMessage(), e);
		}
		return model;
	}

	/**
	 * Cancel workflow.
	 *
	 * @param model
	 *            the model
	 * @param data
	 *            the data
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void cancelWorkflow(Map<String, Object> model, JSONObject data) throws JSONException {
		model.put(KEY_WORKING_MODE, "cancel");
		String workflowId = data.getString(KEY_WORKFLOW_ID);
		JSONObject currentStateData = data.has(CURRENT_STATE_PROP_MAP) ? data
				.getJSONObject(CURRENT_STATE_PROP_MAP) : null;
		if (currentStateData != null) {
			if (getWorkflowService().getWorkflowById(workflowId).getEndDate() == null) {
				// add the modification date for task
				currentStateData.put(CMFModel.PROP_TASK_MODIFIED.toString(), new Date().getTime());
				WorkflowTaskQuery query = new WorkflowTaskQuery();
				query.setProcessId(workflowId);
				query.setTaskState(WorkflowTaskState.IN_PROGRESS);
				buildStateModel(model, query, currentStateData, false);
			} else {
				debug(workflowId, " workflow ended");
			}
		}

		WorkflowInstance canceledWorkflow = getWorkflowService().cancelWorkflow(workflowId);
		if (canceledWorkflow != null) {
			model.put("workflowInstance", getModelBuilder().buildSimple(canceledWorkflow));
			return;
		}
	}

	/**
	 * Cancel task.
	 *
	 * @param model
	 *            the model
	 * @param data
	 *            the data
	 * @throws Exception
	 *             on cancel error
	 */
	private void cancelTask(Map<String, Object> model, JSONObject data) throws Exception {
		model.put(KEY_WORKING_MODE, "transition");
		Map<QName, Serializable> props = data.has(KEY_PROPERTIES) ? toConvertedMap(data
				.getJSONObject(KEY_PROPERTIES)) : null;
		WorkflowTask task = createStartTaskWizard().cancelTask(data.getString(KEY_TASK_ID), props);
		if (task != null) {
			model.put("currentTasks", Collections.singletonList(getModelBuilder().buildSimple(task,
					null, false, true)));
			return;
		}
	}

	/**
	 * Start task.
	 *
	 * @param model
	 *            the model
	 * @param data
	 *            the data
	 * @throws Exception
	 *             the exception
	 */
	private void startTask(Map<String, Object> model, JSONObject data) throws Exception {
		String taskType = data.getString(KEY_TASK_TYPE);
		String parentId = data.has(KEY_PARENT_TASK_ID) ? data.getString(KEY_PARENT_TASK_ID) : null;
		String elementId = data.has(KEY_REFERENCE_ID) ? data.getString(KEY_REFERENCE_ID) : null;
		model.put(KEY_WORKING_MODE, "starttask");
		StandaloneTaskWizard taskWizard = createStartTaskWizard();
		taskWizard.init(taskType, elementId, parentId);
		Map<QName, Serializable> props = toConvertedMap(data.getJSONObject(KEY_PROPERTIES));
		// add the modification date for start
		props.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		Map<QName, Serializable> variablesDefault = taskWizard.prepareData(props);
		// actual start of task
		WorkflowTask startTask = taskWizard.startTask(variablesDefault);
		model.put(
				"currentTasks",
				Collections.singletonList(getModelBuilder().buildSimple(startTask, null, false,
						true)));
	}

	/**
	 * Navigate task.
	 *
	 * @param model
	 *            the model
	 * @param jsonObject
	 *            the json object
	 * @throws Exception
	 *             on any error
	 */
	private void updateTask(Map<String, Object> model, JSONObject jsonObject) throws Exception {

		model.put("currentTasks", Collections.emptyList());
		model.put(KEY_WORKING_MODE, "update");
		String taskId = jsonObject.getString(KEY_TASK_ID);

		WorkflowTask taskById = getWorkflowService().getTaskById(taskId);
		if (taskById == null) {
			throw new WebScriptException("Invalid task id: " + taskId + ". Task is not found!");
		}

		if (taskById.getState() == WorkflowTaskState.COMPLETED) {
			throw new WebScriptException(taskId + " already completed!");
		}
		Map<QName, Serializable> props = toConvertedMap(jsonObject.getJSONObject(KEY_PROPERTIES));
		// serviceRegistry.getWorkflowService().getWorkflowById(
		// taskById.getPath().getInstance().getId());
		if (taskById.getPath() == null) {
			updateStandaloneTask(model, taskById, props, jsonObject);
			return;
		}
		String pathId = taskById.getPath().getId();
		debug("pathId: ", pathId, " and updating task: ", taskId);
		// add the modification date
		props.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		WorkflowTask updateTask = getWorkflowService().updateTask(taskId, props, null, null);

		if (getWorkflowService().getWorkflowById(updateTask.getPath().getInstance().getId())
				.getEndDate() == null) {
			WorkflowTaskQuery query = new WorkflowTaskQuery();
			query.setProcessId(updateTask.getPath().getInstance().getId());
			query.setTaskState(WorkflowTaskState.IN_PROGRESS);
			buildStateModel(model, query, null, true);
		} else {
			debug(updateTask.getPath().getInstance().getId(), " ended");
		}
	}

	/**
	 * Update standalone task.
	 *
	 * @param model
	 *            the model
	 * @param taskById
	 *            the task by id
	 * @param props
	 *            the props
	 * @param request
	 *            is the request from service
	 * @throws Exception
	 *             the exception
	 */
	private void updateStandaloneTask(Map<String, Object> model, WorkflowTask taskById,
			Map<QName, Serializable> props, JSONObject request) throws Exception {
		StandaloneTaskWizard startTaskWizard = createStartTaskWizard();
		String referenceId = null;
		if (request.has(KEY_REFERENCE_ID)) {
			referenceId = request.getString(KEY_REFERENCE_ID);
		}
		WorkflowTask updateMetadata = startTaskWizard.updateTask(taskById.getId(), props,
				referenceId);
		model.put(
				"currentTasks",
				Collections.singletonList(getModelBuilder().buildSimple(updateMetadata, null,
						false, true)));

	}

	/**
	 * Navigate task.
	 *
	 * @param model
	 *            the model
	 * @param jsonObject
	 *            the json object
	 * @throws Exception
	 */
	private void navigateTask(Map<String, Object> model, JSONObject jsonObject) throws Exception {

		model.put("currentTasks", Collections.emptyList());
		model.put(KEY_WORKING_MODE, "transition");
		String taskId = jsonObject.getString(KEY_TASK_ID);
		String tranisitionId = null;// jsonObject.has(KEY_TRANITION) ?
		// jsonObject.getString(KEY_TRANITION) :
		// null;
		Map<QName, Serializable> props = toConvertedMap(jsonObject.getJSONObject(KEY_PROPERTIES));
		// serviceRegistry.getWorkflowService().getWorkflowById(
		// taskById.getPath().getInstance().getId());
		WorkflowTask taskById = getWorkflowService().getTaskById(taskId);
		if (taskById == null) {
			throw new WebScriptException("Invalid task id: " + taskId + ". Task is not found!");
		}
		props.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		if (taskById.getPath() == null) {
			navigateStandaloneTask(model, taskById, props);
			return;
		}
		String pathId = taskById.getPath().getId();

		debug("pathId: ", pathId, " and updating task: ", taskId);
		// add the modification date

		WorkflowTask updateTask = getWorkflowService().updateTask(taskId, props, null, null);
		WorkflowTransition[] transitions = updateTask.getDefinition().getNode().getTransitions();
		debug("transitions ", transitions.length, " : ", Arrays.toString(transitions));
		updateTask = getWorkflowService().endTask(taskId, tranisitionId);
		JSONObject nextStateData = jsonObject.has(NEXT_STATE_PROP_MAP) ? jsonObject
				.getJSONObject(NEXT_STATE_PROP_MAP) : new JSONObject();
		if (getWorkflowService().getWorkflowById(updateTask.getPath().getInstance().getId())
				.getEndDate() == null) {
			WorkflowTaskQuery query = new WorkflowTaskQuery();
			nextStateData.put(CMFModel.PROP_TASK_MODIFIED.toString(), new Date().getTime());
			query.setProcessId(updateTask.getPath().getInstance().getId());
			query.setTaskState(WorkflowTaskState.IN_PROGRESS);
			buildStateModel(model, query, nextStateData, true);
		} else {
			// if (nextStateData != null) {
			// getWorkflowService().updateTask(updateTask.getId(),
			// toConvertedMap(nextStateData), null,
			// null);
			// }
			debug(updateTask.getPath().getInstance().getId(), " ended");
		}
	}

	/**
	 * Navigate standalone task - completes it
	 *
	 * @param model
	 *            the model to populate
	 * @param taskById
	 *            the task by id
	 * @param props
	 *            the props to use for last update
	 * @throws Exception
	 *             the exception
	 */
	private void navigateStandaloneTask(Map<String, Object> model, WorkflowTask taskById,
			Map<QName, Serializable> props) throws Exception {
		StandaloneTaskWizard startTaskWizard = createStartTaskWizard();
		WorkflowTask endedTask = startTaskWizard.completeTask(taskById.getId(), props);
		model.put(
				"currentTasks",
				Collections.singletonList(getModelBuilder().buildSimple(endedTask, null, false,
						true)));
	}

	/**
	 * Builds the next state model by retrieving the task in current state.
	 *
	 * @param model
	 *            the model to update
	 * @param query
	 *            the query to execute
	 * @param nextStateData
	 *            is the data to set for task in the current state (after
	 *            transition).
	 * @param buildModel
	 *            whether actually to build model
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void buildStateModel(Map<String, Object> model, WorkflowTaskQuery query,
			JSONObject nextStateData, boolean buildModel) throws JSONException {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>(10);
		List<WorkflowTask> tasksForWorkflowPath = getWorkflowService().queryTasks(query, true);
		Map<QName, Serializable> convertedMap = null;
		if (nextStateData != null) {
			convertedMap = toConvertedMap(nextStateData);
		}
		for (int i = 0; i < tasksForWorkflowPath.size(); i++) {
			WorkflowTask task = tasksForWorkflowPath.get(i);
			// update if data is provided
			if (convertedMap != null) {
				task = getWorkflowService().updateTask(task.getId(), convertedMap, null, null);
			}
			if (buildModel) {
				Map<String, Object> buildDetailed = getModelBuilder().buildDetailed(task, i == 0);
				tasks.add(buildDetailed);
			}
		}
		model.put("currentTasks", tasks);
	}

	/**
	 * Converts json object to hashmap (keys should be valid qname).
	 *
	 * @param jsonObject
	 *            is the json to convert
	 * @return the converted map
	 * @throws JSONException
	 *             on error
	 */
	protected Map<QName, Serializable> toConvertedMap(JSONObject jsonObject) throws JSONException {
		@SuppressWarnings("unchecked")
		Iterator<String> keys = jsonObject.keys();
		Map<QName, Serializable> map = new HashMap<QName, Serializable>(jsonObject.length());
		debug("toConvertedMap ", jsonObject);
		while (keys.hasNext()) {
			String object = keys.next().toString();
			QName resolvedToQName = QName.resolveToQName(getNamespaceService(), object);
			if (resolvedToQName != null) {
				PropertyDefinition property = getDataDictionaryService().getProperty(
						resolvedToQName);
				debug("property ", resolvedToQName);
				//
				if ((property != null)
						&& Date.class.getName().equals(property.getDataType().getJavaClassName())) {
					map.put(resolvedToQName, new Date(jsonObject.getLong(object)));
				} else {
					Object value = jsonObject.get(object);
					if (property != null) {
						Serializable convert = null;
						if (value != JSONObject.NULL) {
							convert = (Serializable) DefaultTypeConverter.INSTANCE.convert(
									property.getDataType(), value);
						}
						map.put(resolvedToQName, convert);
					} else {
						// check if assoc
						AssociationDefinition association = getDataDictionaryService()
								.getAssociation(resolvedToQName);
						if (association != null) {
							QName name = association.getTargetClass().getName();
							if (CMFModel.ASSOC_TASK_MULTI_ASSIGNEES.equals(resolvedToQName)) {
								Collection<?> convertedValue = DefaultTypeConverter.INSTANCE
										.convert(Collection.class, checkJSONMultivalue(value));
								map.put(CMFModel.ASSOC_TASK_MULTI_ASSIGNEES,
										(Serializable) convertedValue);
							} else if (ContentModel.TYPE_PERSON.equals(name)
									|| ContentModel.TYPE_AUTHORITY_CONTAINER.equals(name)) {
								boolean targetMany = association.isTargetMany();
								if (targetMany) {
									// TODO may be noderef for every node
									Serializable convertedValue = (Serializable) DefaultTypeConverter.INSTANCE
											.convert(Collection.class, checkJSONMultivalue(value));
									map.put(resolvedToQName, convertedValue);
									continue;
								}
								NodeRef assigneeAssoc = getAssigneeAuthority(value, name);
								if (assigneeAssoc != null) {
									map.put(resolvedToQName, assigneeAssoc);
								} else {
									warn("toMap() SKIP  ", association.getName(), value);
								}
							} else {
								throw new WebScriptException("Not implemented " + name);
							}
						} else {
							if (resolvedToQName.getLocalName().startsWith("assignee")) {
								if (value.toString().trim().startsWith("[")
										|| resolvedToQName.getLocalName().startsWith("assignees")) {
									Serializable convertedValue = (Serializable) DefaultTypeConverter.INSTANCE
											.convert(Collection.class, checkJSONMultivalue(value));
									map.put(resolvedToQName, convertedValue);
								} else {
									map.put(resolvedToQName, value.toString());
								}
							} else {
								map.put(resolvedToQName, value.toString());
							}
						}
					}
				}
			} else {
				warn("toMap() SKIP  ", object);
			}
		}
		return map;
	}

	/**
	 * Helper method to obtain authority id by value
	 *
	 * @param value
	 *            the value
	 * @param name
	 *            is the property name
	 * @return obtained noderef or null
	 */
	private NodeRef getAssigneeAuthority(Object value, QName name) {
		if (ContentModel.TYPE_PERSON.equals(name)) {
			return getUserNodeRef(value);
		} else if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(name)) {
			return getGroupNodeRef(value);
		} else if (ContentModel.TYPE_AUTHORITY.equals(name)) {
			AuthorityType authorityType = AuthorityType.getAuthorityType(value.toString());
			switch (authorityType) {
			case GROUP:
				return getGroupNodeRef(value);
			case USER:
				return getUserNodeRef(value);
			default:
				throw new RuntimeException("Invalid authority : " + value);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc} .Extended with string converting to collection
	 */
	@Override
	protected Object checkJSONMultivalue(Object value) throws JSONException {
		Object valueUpdated = value;
		if (value instanceof String) {
			valueUpdated = Arrays.asList(value.toString().split(","));
		}
		return super.checkJSONMultivalue(valueUpdated);
	}

	/**
	 * Gets the person noderef by given value.
	 *
	 * @param value
	 *            is either noderef or username
	 * @return the person if found or null if not
	 */
	private NodeRef getUserNodeRef(Object value) {
		Serializable userName = value.toString();
		if (NodeRef.isNodeRef(value.toString())) {
			NodeRef ref = new NodeRef(value.toString());
			if (nodeService.exists(ref)) {
				userName = getNodeService().getProperty(ref, ContentModel.PROP_USERNAME);
			}
		}
		if (getPersonService().personExists(userName.toString())) {
			return getPersonService().getPerson(userName.toString());
		}
		return null;
	}

	/**
	 * Gets the group noderef by given value.
	 *
	 * @param value
	 *            is either noderef or full group name
	 * @return the noderef of found group or null
	 */
	private NodeRef getGroupNodeRef(Object value) {
		Serializable userName = value.toString();
		if (NodeRef.isNodeRef(value.toString())) {
			NodeRef ref = new NodeRef(value.toString());
			if (nodeService.exists(ref)) {
				userName = getNodeService().getProperty(ref, ContentModel.PROP_AUTHORITY_NAME);
			}
		}
		if (serviceRegistry.getAuthorityService().authorityExists(userName.toString())) {
			return serviceRegistry.getAuthorityService().getAuthorityNodeRef(userName.toString());
		}
		return null;
	}

	/**
	 * Start workflow.
	 *
	 * @param model
	 *            the model to populate
	 * @param data
	 *            the data for request
	 * @throws Exception
	 *             the exception
	 */
	private void startWorkflow(Map<String, Object> model, JSONObject data) throws Exception {

		model.put(KEY_WORKING_MODE, "start");
		StartWorkflowWizard workflowWizard = createStartWorkflowWizard();
		workflowWizard.init(data.getString(KEY_WORKFLOW_ID), data.getString(KEY_REFERENCE_ID));
		Map<QName, Serializable> props = toConvertedMap(data.getJSONObject(KEY_PROPERTIES));
		// add the modification date for start
		props.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		// task start specific data
		Map<QName, Serializable> currentStateData = data.has(CURRENT_STATE_PROP_MAP) ? toConvertedMap(data
				.getJSONObject(CURRENT_STATE_PROP_MAP)) : new HashMap<QName, Serializable>();
		props.putAll(currentStateData);
		Pair<WorkflowTask, WorkflowInstance> process = workflowWizard.start(props);
		if (process != null) {
			model.put("workflowInstance", getModelBuilder().buildSimple(process.getSecond()));
			String id = process.getSecond().getId();
			if (getWorkflowService().getWorkflowById(id).getEndDate() == null) {
				JSONObject nextStateData = data.has(NEXT_STATE_PROP_MAP) ? data
						.getJSONObject(NEXT_STATE_PROP_MAP) : new JSONObject();
				// add the modification date for first work task
				nextStateData.put(CMFModel.PROP_TASK_MODIFIED.toString(), new Date().getTime());
				WorkflowTaskQuery query = new WorkflowTaskQuery();
				query.setProcessId(id);
				query.setTaskState(WorkflowTaskState.IN_PROGRESS);
				buildStateModel(model, query, nextStateData, true);
			} else {
				debug(id, " workflow ended");
			}
			return;
		}
		throw new WebScriptException("Workflow is not started!");
	}

	/**
	 * Gets the model builder.
	 *
	 * @return the model builder
	 */
	public WorkflowModelBuilder getModelBuilder() {
		if (modelBuilder == null) {
			modelBuilder = new WorkflowModelBuilder(getNamespaceService(), nodeService,
					getAuthenticationService(), getPersonService(), getWorkflowService(),
					getWorkflowReportService());
		}
		return modelBuilder;
	}

	/**
	 * Gets the start workflow wizard.
	 *
	 * @return the start workflow wizard
	 */
	public StartWorkflowWizard createStartWorkflowWizard() {
		return new StartWorkflowWizard(getServiceRegistry(), getCaseService());
	}

	/**
	 * Gets the start task wizard.
	 *
	 * @return the start task wizard
	 */
	public StandaloneTaskWizard createStartTaskWizard() {
		return new StandaloneTaskWizard(getServiceRegistry(), getCaseService());
	}

}
