package com.sirma.itt.cmf.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.log4j.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;
import com.sirma.itt.cmf.integration.workflow.StandaloneTaskWizard;

/**
 * Script for woriking with cases.
 * 
 * @author bbanchev
 */
public class CaseInstancesScript extends BaseAlfrescoScript {
	/** holder for automatic actions' settings. */
	public static final String AUTOMATIC_ACTIONS_SET = "automaticActionsProperties";
	/** automatically cancel all active workflows. */
	public static final String AUTOMATIC_CANCEL_ACTIVE_WF = "automaticCancelWorkflow";

	/** The Constant ACTIVE_TASKS_PROPS_UPDATE. */
	public static final String ACTIVE_TASKS_PROPS_UPDATE = "taskPropsUpdate";

	/** The Constant MARK_AS_DELETED_WF. */
	public static final String MARK_AS_DELETED_WF = "markAsDeletedWorkflow";

	/**
	 * Execute internal. Wrapper for system user action.
	 * 
	 * @param req
	 *            the original request
	 * @return the updated model
	 */
	@Override
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		try {
			final String serverPath = req.getServicePath();
			final String content = req.getContent().getContent();
			debug("Case request: ", serverPath, " data: ", content);

			Map<String, Object> model = new HashMap<String, Object>();
			List<NodeRef> value = new ArrayList<NodeRef>(1);
			if (serverPath.contains("/cmf/instance/create")) {
				model.put("parent", createRequest(value, null, content));
			} else if (serverPath.contains("/cmf/instance/update")) {
				updateRequest(value, content);
			} else if (serverPath.contains("/cmf/instance/close")) {
				closeRequest(value, content);
			} else if (serverPath.contains("/cmf/instance/delete")) {
				deleteRequest(value, content);
			}
			debug("Case request: ", serverPath, " response: ", model);
			model.put("results", value);
			return model;
		} catch (Exception e) {
			throw new WebScriptException("Error during case operation: " + e.getMessage(), e);
		}
	}

	/**
	 * Update request.
	 * 
	 * @param value
	 *            the value
	 * @param content
	 *            the content
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void updateRequest(List<NodeRef> value, String content) throws JSONException {
		// updates a case
		JSONObject request = new JSONObject(content);
		if (request.has(KEY_NODEID)) {
			Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
			NodeRef updateNode = updateNode(properties, cmfService.getNodeRef(request.getString(KEY_NODEID)));
			if (updateNode != null) {
				value.add(updateNode);
			}
		}
	}

	/**
	 * Delete request.
	 * 
	 * @param value
	 *            the value
	 * @param content
	 *            the content
	 * @throws Exception
	 *             the exception occurred
	 */
	private void deleteRequest(List<NodeRef> value, String content) throws Exception {
		JSONObject request = new JSONObject(content);
		if (request.has(KEY_NODEID)) {
			boolean force = request.has(KEY_FORCE) ? Boolean.valueOf(request.getString(KEY_FORCE)) : Boolean.FALSE;
			NodeRef deletable = cmfService.getNodeRef(request.getString(KEY_NODEID));
			if (deletable != null) {
				if (force) {
					// if force just delete all
					NodeRef parentRef = getNodeService().getPrimaryParent(deletable).getParentRef();
					// delete
					getNodeService().deleteNode(deletable);
					// on delete add the parent
					value.add(parentRef);
				} else {
					// do delete operation
					archiveNode(value, request, deletable, ArchiveOperation.DELETE);
				}
			}
		}
	}

	/**
	 * The Enum ArchiveOperation.
	 */
	private enum ArchiveOperation {

		/** The close. */
		CLOSE, /** The archive. */
		ARCHIVE, /** The delete. */
		DELETE;
	}

	/**
	 * Archive node. If operation is {@link ArchiveOperation#DELETE} documents
	 * are deleted and all workflow and tasks are set as archived
	 * 
	 * @param value
	 *            the value
	 * @param request
	 *            the request
	 * @param updetable
	 *            the updetable node
	 * @param type
	 *            the type
	 * @throws Exception
	 *             the exception occurred
	 */
	private void archiveNode(List<NodeRef> value, JSONObject request, NodeRef updetable, ArchiveOperation type)
			throws Exception {
		try {
			Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
			List<NodeRef> caseDocuments = prepareCaseAndWorkflows(updetable, properties, type, request);
			debug(caseDocuments, " with size:", caseDocuments.size(), " will be closed!");
			for (NodeRef nodeRef : caseDocuments) {
				if (!getNodeService().exists(nodeRef)) {
					continue;
				}
				if (type == ArchiveOperation.DELETE) {
					cmfLockService.getLockService().unlock(nodeRef);
					getNodeService().deleteNode(nodeRef);
				} else if (type == ArchiveOperation.CLOSE) {
					cmfLockService.lockNode(nodeRef);
				}
			}
			value.add(updetable);
		} finally {
			try {
				if (updetable != null) {
					String lockedOwner = cmfLockService.getLockedOwner(updetable);
					if (!CMFService.getSystemUser().equals(lockedOwner)) {
						cmfLockService.getLockService().unlock(updetable);
						cmfLockService.lockNode(updetable);
					}
				}
			} catch (Exception e) {
				throw new WebScriptException(500, "Error during lock!", e);
			}
		}
	}

	/**
	 * Clost request.
	 * 
	 * @param value
	 *            the value
	 * @param content
	 *            the content
	 * @throws Exception
	 *             the exception occurred
	 */
	private void closeRequest(List<NodeRef> value, String content) throws Exception {
		JSONObject request = new JSONObject(content);
		NodeRef closeable = null;
		if (request.has(KEY_NODEID)) {
			// do delete operation
			closeable = cmfService.getNodeRef(request.getString(KEY_NODEID));
			archiveNode(value, request, closeable, ArchiveOperation.CLOSE);
		}

	}

	/**
	 * Prepare case for archive operation.
	 * 
	 * @param updateNode
	 *            the update node
	 * @param properties
	 *            the properties
	 * @param type
	 *            the archive type operation
	 * @param request
	 *            the original request body
	 * @return the list of all children to be further processed
	 * @throws Exception
	 *             the exception thrown from invoked methods
	 */
	private List<NodeRef> prepareCaseAndWorkflows(NodeRef updateNode, Map<QName, Serializable> properties,
			ArchiveOperation type, JSONObject request) throws Exception {
		List<NodeRef> caseDocuments = cmfService.getCaseDocuments(updateNode);
		CheckOutCheckInService checkOutCheckInService = serviceRegistry.getCheckOutCheckInService();
		getNodeService().addProperties(updateNode, properties);
		List<NodeRef> list = new ArrayList<NodeRef>();
		for (NodeRef nodeRef : caseDocuments) {
			if (!getNodeService().exists(nodeRef)) {
				continue;
			}
			if (getServiceProxy().isCheckedOut(nodeRef)) {
				checkOutCheckInService.cancelCheckout(checkOutCheckInService.getWorkingCopy(nodeRef));
			}
			try {
				cmfLockService.getLockService().unlock(nodeRef);
			} catch (Exception e) {
				log(Level.WARN, e, "Failed to unlock node: " + nodeRef);
			}
			list.add(nodeRef);
		}
		updateNodeWorkflows(updateNode, properties, request, type);
		return list;

	}

	/**
	 * Update node workflows by setting params, provided by the request. Tasks
	 * and workflows might be affected. When operation is
	 * 
	 * @param updateNode
	 *            the update node
	 * @param properties
	 *            the properties
	 * @param request
	 *            the request
	 * @param type
	 *            is the archive type operation
	 * @throws Exception
	 *             the exception occurred
	 */
	private void updateNodeWorkflows(NodeRef updateNode, Map<QName, Serializable> properties, JSONObject request,
			ArchiveOperation type) throws Exception {
		boolean autoCancelWF = false;
		JSONObject nextStateData = new JSONObject();// data.has(NEXT_STATE_PROP_MAP)
		if (request.has(AUTOMATIC_ACTIONS_SET)) {
			if (request.getJSONObject(AUTOMATIC_ACTIONS_SET).has(ACTIVE_TASKS_PROPS_UPDATE)) {
				nextStateData = request.getJSONObject(AUTOMATIC_ACTIONS_SET).getJSONObject(ACTIVE_TASKS_PROPS_UPDATE);
			}
			if (request.getJSONObject(AUTOMATIC_ACTIONS_SET).has(AUTOMATIC_CANCEL_ACTIVE_WF)) {
				autoCancelWF = request.getJSONObject(AUTOMATIC_ACTIONS_SET).getBoolean(AUTOMATIC_CANCEL_ACTIVE_WF);
			}
		}
		Map<QName, Serializable> convertedMap = null;
		if (nextStateData != null) {
			convertedMap = toMap(nextStateData);
		} else {
			convertedMap = new HashMap<QName, Serializable>(1);
		}
		Map<QName, Serializable> archiveData = new HashMap<QName, Serializable>(1);

		Serializable closeReason = properties.get(CMFModel.PROP_CLOSED_REASON);

		if (closeReason != null) {
			archiveData.put(CMFModel.PROP_WF_ARCHIVE_REASON, closeReason);
		} else {
			archiveData.put(CMFModel.PROP_WF_ARCHIVE_REASON, "Automatically cancelled workflow.");
		}
		List<WorkflowInstance> workflowsForContent = null;
		// only if cancellation is requested as delete of case
		if ((type == ArchiveOperation.DELETE) && autoCancelWF) {
			// process the completed wf first as active not to be processed
			// twice
			workflowsForContent = getWorkflowService().getWorkflowsForContent(updateNode, false);
			for (WorkflowInstance workflowInstance : workflowsForContent) {
				setWorkflowTasksArchived(archiveData, workflowInstance);
			}
		}
		// process the activate wf
		workflowsForContent = getWorkflowService().getWorkflowsForContent(updateNode, true);
		for (WorkflowInstance workflowInstance : workflowsForContent) {
			List<WorkflowTask> activeTasks = null;
			// add the modification date for first work task
			if (!convertedMap.isEmpty()) {
				convertedMap.put(CMFModel.PROP_TASK_MODIFIED, new Date());
				WorkflowTaskQuery query = new WorkflowTaskQuery();
				query.setProcessId(workflowInstance.getId());
				query.setTaskState(WorkflowTaskState.IN_PROGRESS);
				activeTasks = getWorkflowService().queryTasks(query, true);
				for (WorkflowTask workflowTask : activeTasks) {

					// update with nextState data.
					getWorkflowService().updateTask(workflowTask.getId(), convertedMap, null, null);
				}
			}
			if ((type == ArchiveOperation.DELETE) && autoCancelWF) {
				setWorkflowTasksArchived(archiveData, workflowInstance);
			}
			// now it is time for cancelling the workflow.
			if (autoCancelWF) {
				getWorkflowService().cancelWorkflow(workflowInstance.getId());
			}
		}
		// CMF-2900: added standalone tasks context filtering
		Pair<List<NodeRef>, Map<String, Object>> search = cmfService.search(null,
				"PATH:\"/sys:system/cmfwf:taskIndexesSpace/sys:standalonetasks//*\" AND ASPECT: \"{http://www.sirmaitt.com/model/workflow/cmf/1.0}dmsTask\" AND cmfwf:contextId:\""
						+ updateNode + "\" AND ISNULL:\"bpm:completionDate\"",
				null, null, null);
		StandaloneTaskWizard standaloneTaskWizard = new StandaloneTaskWizard(serviceRegistry, cmfService);
		Map<QName, Serializable> props = null;
		if ((type == ArchiveOperation.DELETE) && autoCancelWF) {
			props = archiveData;
			props.putAll(convertedMap);
		} else {
			props = convertedMap;
			props.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		}
		for (NodeRef tasks : search.getFirst()) {
			String name = (String) getNodeService().getProperty(tasks, ContentModel.PROP_NAME);
			standaloneTaskWizard.cancelTask(name, props);
		}
	}

	/**
	 * Fix workflow task data by setting the provided metadata. Task that are
	 * processed are only in Completed status.
	 * 
	 * @param archiveData
	 *            the archive data to set on completed task
	 * @param workflowInstance
	 *            the workflow instance to process tasks for
	 * @param tasksToexclude
	 *            the tasks for exclude from found by query
	 */
	private void setWorkflowTasksArchived(Map<QName, Serializable> archiveData, WorkflowInstance workflowInstance) {
		NodeRef workflowPackage = workflowInstance.getWorkflowPackage();
		// upate the package
		archiveData.remove(CMFModel.PROP_TASK_MODIFIED);
		getNodeService().addProperties(workflowPackage, archiveData);
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setActive(null);
		taskQuery.setProcessId(workflowInstance.getId());
		taskQuery.setTaskState(null);
		taskQuery.setOrderBy(new OrderBy[] { OrderBy.TaskCreated_Asc });
		// set all tasks not searchable
		List<WorkflowTask> queryTasks = getWorkflowService().queryTasks(taskQuery, true);
		List<String> taskIds = null;
		archiveData.put(CMFModel.PROP_TASK_MODIFIED, new Date());
		for (WorkflowTask workflowTask : queryTasks) {
			if ((taskIds != null) && taskIds.contains(workflowTask.getId())) {
				continue;
			}
			workflowTask.getProperties().putAll(archiveData);
			getWorkflowReportService().updateTask(workflowTask);
		}
	}

	/**
	 * Creates the request.
	 * 
	 * @param value
	 *            the value
	 * @param requestPath
	 *            the request path
	 * @param content
	 *            the content
	 * @return the node ref
	 * @throws JSONException
	 *             the jSON exception
	 */
	private NodeRef createRequest(List<NodeRef> value, NodeRef requestPath, String content) throws JSONException {
		JSONObject request = new JSONObject(content);
		// TODO
		if (request.has(KEY_START_PATH)) {
			requestPath = cmfService.getCMFCaseInstanceSpace(request.getString(KEY_START_PATH));
		} else if (request.has(KEY_SITE_ID)) {
			SiteInfo site = serviceRegistry.getSiteService().getSite(request.getString(KEY_SITE_ID));
			if (site != null) {
				requestPath = cmfService.getCMFCaseInstanceSpace(site.getNodeRef());
			}
		} else if (request.has(KEY_DEFINITION_ID)) {
			NodeRef nodeRef = cmfService.getNodeRef(request.getString(KEY_DEFINITION_ID));
			if (nodeRef != null) {
				requestPath = cmfService.getCMFCaseInstanceSpace(nodeRef);
			}
		}
		if (requestPath == null) {
			throw new WebScriptException(404, "Store location for requested case not found!");
		}
		Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
		// try use the provided cm:name
		if (properties.get(ContentModel.PROP_NAME) == null) {
			// do a mapping
			String caseName = properties.get(CMFModel.PROP_IDENTIFIER) != null
					? properties.get(CMFModel.PROP_IDENTIFIER).toString() : "case_" + GUID.generate();
			properties.put(ContentModel.PROP_NAME, caseName);
		}
		NodeRef createdCMFCaseSpace = cmfService.createCMFCaseSpace(requestPath, properties);
		getOwnableService().setOwner(createdCMFCaseSpace, CMFService.getSystemUser());
		if (request.has(KEY_SECTIONS)) {
			JSONArray jsonArray = request.getJSONArray(KEY_SECTIONS);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				Map<QName, Serializable> model = toMap(object);
				NodeRef section = cmfService.createCMFSectionSpace(createdCMFCaseSpace, model);
				cmfLockService.lockNode(section);
				value.add(section);
			}
		}
		cmfLockService.lockNode(createdCMFCaseSpace);
		return createdCMFCaseSpace;
	}
}
