/**
 *
 */
package com.sirma.itt.cmf.integration.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Class responsible to processes all requests regarding state of workflow
 *
 * @author bbanchev
 */
public class WorkflowInstanceScript extends BaseAlfrescoScript {

	/** The Constant KEY_TRANITION. */
	private static final String KEY_TRANITION = "transitionId";

	/** The Constant KEY_TASK_ID. */
	private static final String KEY_TASK_ID = "taskId";

	/** The Constant KEY_WORKFLOW_ID. */
	private static final String KEY_WORKFLOW_ID = "workflowId";
	private static final String NEXT_STATE_PROP_MAP = "nextStateProperties";
	/** The start workflow wizard. */
	private StartWorkflowWizard startWorkflowWizard = null;

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
		model.put(KEY_MODE, "unknown");
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
			}
		} catch (WebScriptException e) {
			throw e;
		} catch (Exception e) {
			throw new WebScriptException(500, e.getMessage(), e);
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
		model.put(KEY_MODE, "cancel");
		WorkflowInstance canceledWorkflow = getWorkflowService().cancelWorkflow(
				data.getString(KEY_WORKFLOW_ID));
		if (canceledWorkflow != null) {
			model.put("workflowInstance", getModelBuilder().buildSimple(canceledWorkflow));
			return;
		}
	}

	/**
	 * Navigate task.
	 *
	 * @param model
	 *            the model
	 * @param jsonObject
	 *            the json object
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void updateTask(Map<String, Object> model, JSONObject jsonObject) throws JSONException {

		model.put("currentTasks", Collections.emptyList());
		model.put("mode", "update");
		String taskId = jsonObject.getString(KEY_TASK_ID);

		WorkflowTask taskById = getWorkflowService().getTaskById(taskId);
		if (taskById == null) {
			throw new WebScriptException("Invalid task id: " + taskId + ". Task is not found!");
		}
		String pathId = taskById.getPath().getId();
		if (taskById.getState() == WorkflowTaskState.COMPLETED) {
			throw new WebScriptException(taskId + " already completed!");
		}
		Map<QName, Serializable> props = toConvertedMap(jsonObject.getJSONObject(KEY_PROPERTIES));
		// serviceRegistry.getWorkflowService().getWorkflowById(
		// taskById.getPath().getInstance().getId());

		debug("pathId: ", pathId, " and updating task: ", taskId);
		// add the modification date
		props.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		WorkflowTask updateTask = getWorkflowService().updateTask(taskId, props, null, null);

		try {
			if (getWorkflowService().getWorkflowById(updateTask.getPath().getInstance().getId())
					.getEndDate() == null) {
				WorkflowTaskQuery query = new WorkflowTaskQuery();
				query.setProcessId(updateTask.getPath().getInstance().getId());
				query.setTaskState(WorkflowTaskState.IN_PROGRESS);
				buildNextStateModel(model, query, null);
			} else {
				debug(updateTask.getPath().getInstance().getId(), " ended");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Navigate task.
	 *
	 * @param model
	 *            the model
	 * @param jsonObject
	 *            the json object
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void navigateTask(Map<String, Object> model, JSONObject jsonObject)
			throws JSONException {

		model.put("currentTasks", Collections.emptyList());
		model.put("mode", "transition");
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
		String pathId = taskById.getPath().getId();
		debug("pathId: ", pathId, " and updating task: ", taskId);
		// add the modification date
		props.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		WorkflowTask updateTask = getWorkflowService().updateTask(taskId, props, null, null);
		WorkflowTransition[] transitions = updateTask.getDefinition().getNode().getTransitions();
		debug("transitions ", transitions.length, " : ", Arrays.toString(transitions));
		updateTask = getWorkflowService().endTask(taskId, tranisitionId);
		try {
			JSONObject nextStateData = jsonObject.has(NEXT_STATE_PROP_MAP) ? jsonObject
					.getJSONObject(NEXT_STATE_PROP_MAP) : new JSONObject();
					if (getWorkflowService().getWorkflowById(updateTask.getPath().getInstance().getId())
							.getEndDate() == null) {
						WorkflowTaskQuery query = new WorkflowTaskQuery();
						nextStateData.put(CMFModel.PROP_TASK_MODIFIED.toString(), new Date().getTime());
						query.setProcessId(updateTask.getPath().getInstance().getId());
						query.setTaskState(WorkflowTaskState.IN_PROGRESS);
						buildNextStateModel(model, query, nextStateData);
					} else {
						// if (nextStateData != null) {
						// getWorkflowService().updateTask(updateTask.getId(),
						// toConvertedMap(nextStateData), null,
						// null);
						// }
						debug(updateTask.getPath().getInstance().getId(), " ended");
					}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	 * @throws JSONException
	 */
	private void buildNextStateModel(Map<String, Object> model, WorkflowTaskQuery query,
			JSONObject nextStateData) throws JSONException {
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
			Map<String, Object> buildDetailed = getModelBuilder().buildDetailed(task, i == 0);
			tasks.add(buildDetailed);
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
						Serializable convert = (Serializable) DefaultTypeConverter.INSTANCE
								.convert(property.getDataType(), value);
						map.put(resolvedToQName, convert);
					} else {
						// check if assoc
						AssociationDefinition association = getDataDictionaryService()
								.getAssociation(resolvedToQName);
						if (association != null) {
							QName name = association.getTargetClass().getName();
							if (ContentModel.TYPE_PERSON.equals(name)) {
								NodeRef person = getPerson(value);
								if (person != null) {
									map.put(resolvedToQName, person);
								} else {
									LOGGER.warn("toMap() SKIP person " + value);
								}
							} else {
								throw new WebScriptException("Not implemented " + name);
							}
						} else {
							map.put(resolvedToQName, value.toString());
						}
					}
				}
			} else {
				LOGGER.warn("toMap() SKIP " + object);
			}
		}
		return map;
	}

	/**
	 * Gets the person.
	 *
	 * @param value
	 *            the value
	 * @return the person
	 */
	private NodeRef getPerson(Object value) {
		Serializable userName = value.toString();
		if (NodeRef.isNodeRef(value.toString())) {
			NodeRef ref = new NodeRef(value.toString());
			if (nodeService.exists(ref)) {
				userName = getNodeService().getProperty(ref, ContentModel.PROP_USERNAME);
			}
		}
		if (getPersonService().personExists(userName.toString())) {
			return getPersonService().getPerson(value.toString());
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

		model.put(KEY_MODE, "start");
		getStartWorkflowWizard().init(data.getString(KEY_WORKFLOW_ID), data.getString(KEY_CASE_ID));
		Map<QName, Serializable> props = toConvertedMap(data.getJSONObject(KEY_PROPERTIES));
		// add the modification date for start
		props.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		Pair<WorkflowTask, WorkflowInstance> process = getStartWorkflowWizard().start(props);
		if (process != null) {
			model.put("workflowInstance", getModelBuilder().buildSimple(process.getSecond()));
			try {
				String id = process.getSecond().getId();
				if (getWorkflowService().getWorkflowById(id).getEndDate() == null) {
					JSONObject nextStateData = data.has(NEXT_STATE_PROP_MAP) ? data
							.getJSONObject(NEXT_STATE_PROP_MAP) : new JSONObject();
							// add the modification date for first work task
							nextStateData.put(CMFModel.PROP_TASK_MODIFIED.toString(), new Date().getTime());
							WorkflowTaskQuery query = new WorkflowTaskQuery();
							query.setProcessId(id);
							query.setTaskState(WorkflowTaskState.IN_PROGRESS);
							buildNextStateModel(model, query, nextStateData);
				} else {
					debug(id, " workflow ended");
				}
			} catch (Exception e) {
				e.printStackTrace();
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
					getAuthenticationService(), getPersonService(), getWorkflowService());
		}
		return modelBuilder;
	}

	/**
	 * Gets the start workflow wizard.
	 *
	 * @return the start workflow wizard
	 */
	public StartWorkflowWizard getStartWorkflowWizard() {
		if (startWorkflowWizard == null) {
			startWorkflowWizard = new StartWorkflowWizard(getServiceRegistry(), getCaseService());
		}
		return startWorkflowWizard;
	}
}
