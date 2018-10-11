package com.sirma.itt.cmf.integration.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * Script for woriking with cases.
 *
 * @author bbanchev
 *
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
		Map<String, Object> model = new HashMap<String, Object>();
		ArrayList<NodeRef> value = new ArrayList<NodeRef>(1);
		NodeRef requestPath = null;
		String serverPath = req.getServicePath();
		try {
			String content = req.getContent().getContent();
			debug("Case request: ", serverPath, " data: ", content);
			if (serverPath.contains("/cmf/instance/create")) {
				requestPath = createRequest(value, requestPath, content);
				model.put("parent", requestPath);
			} else if (serverPath.contains("/cmf/instance/update")) {
				updateRequest(value, content);
			} else if (serverPath.contains("/cmf/instance/close")) {
				closeRequest(value, content);
			} else if (serverPath.contains("/cmf/instance/delete")) {
				deleteRequest(value, content);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebScriptException(e.getMessage());
		}
		model.put("results", value);
		debug("Case request: ", serverPath, " response: ", model);
		return model;
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
	private void updateRequest(ArrayList<NodeRef> value, String content) throws JSONException {
		// updates a case
		JSONObject request = new JSONObject(content);
		Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
		if (request.has(KEY_CASE_ID)) {
			NodeRef updateNode = updateNode(properties,
					caseService.getNodeRef(request.getString(KEY_CASE_ID)));
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
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void deleteRequest(ArrayList<NodeRef> value, String content) throws JSONException {
		JSONObject request = new JSONObject(content);
		if (request.has(KEY_CASE_ID)) {
			boolean force = request.has(KEY_FORCE) ? Boolean.valueOf(request.getString(KEY_FORCE))
					: Boolean.FALSE;
			NodeRef deletable = caseService.getNodeRef(request.getString(KEY_CASE_ID));
			if (deletable != null) {
				if (force) {
					// if force just delete all
					try {
						AuthenticationUtil.pushAuthentication();
						AuthenticationUtil.setRunAsUserSystem();
						NodeRef parentRef = nodeService.getPrimaryParent(deletable).getParentRef();
						// delete
						nodeService.deleteNode(deletable);
						// on delete add the parent
						value.add(parentRef);
					} finally {
						AuthenticationUtil.popAuthentication();
					}
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
		CLOSE,
		/** The archive. */
		ARCHIVE,
		/** The delete. */
		DELETE;
	}

	/**
	 * Archive node.
	 *
	 * @param value
	 *            the value
	 * @param request
	 *            the request
	 * @param updetable
	 *            the updetable node
	 * @param type
	 *            the type
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void archiveNode(ArrayList<NodeRef> value, JSONObject request, NodeRef updetable,
			ArchiveOperation type) throws JSONException {
		try {
			AuthenticationUtil.pushAuthentication();
			AuthenticationUtil.setRunAsUserSystem();
			Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
			List<NodeRef> caseDocuments = prepareCaseAndWorkflows(updetable, properties, type,
					request);
			debug(caseDocuments, " with size:", caseDocuments.size(), " will be closed!");
			for (NodeRef nodeRef : caseDocuments) {
				if (!nodeService.exists(nodeRef)) {
					continue;
				}
				if (type == ArchiveOperation.DELETE) {
					cmfLockService.getLockService().unlock(nodeRef);
					nodeService.deleteNode(nodeRef);
				} else if (type == ArchiveOperation.CLOSE) {
					cmfLockService.lockNode(nodeRef);
				}
			}
			value.add(updetable);
		} finally {
			try {
				if (updetable != null) {
					String lockedOwner = cmfLockService.getLockedOwner(updetable);
					if (!AuthenticationUtil.getSystemUserName().equals(lockedOwner)) {
						cmfLockService.getLockService().unlock(updetable);
						cmfLockService.lockNode(updetable);
					}
				}
			} catch (Exception e) {
				throw new WebScriptException(500, "Error during lock!", e);
			} finally {
				AuthenticationUtil.popAuthentication();
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
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void closeRequest(ArrayList<NodeRef> value, String content) throws JSONException {
		JSONObject request = new JSONObject(content);
		NodeRef closeable = null;
		if (request.has(KEY_CASE_ID)) {
			// do delete operation
			closeable = caseService.getNodeRef(request.getString(KEY_CASE_ID));
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
	 * @throws JSONException
	 *             the jSON exception
	 */
	private List<NodeRef> prepareCaseAndWorkflows(NodeRef updateNode,
			Map<QName, Serializable> properties, ArchiveOperation type, JSONObject request)
					throws JSONException {
		List<NodeRef> caseDocuments = caseService.getCaseDocuments(updateNode);
		CheckOutCheckInService checkOutCheckInService = serviceRegistry.getCheckOutCheckInService();
		nodeService.addProperties(updateNode, properties);
		List<NodeRef> list = new ArrayList<NodeRef>();
		for (NodeRef nodeRef : caseDocuments) {
			if (!nodeService.exists(nodeRef)) {
				continue;
			}
			if (getServiceProxy().isCheckedOut(nodeRef)) {
				checkOutCheckInService.cancelCheckout(checkOutCheckInService
						.getWorkingCopy(nodeRef));
			}
			try {
				cmfLockService.getLockService().unlock(nodeRef);
			} catch (Exception e) {
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
	 * @throws JSONException
	 *             the jSON exception {@link ArchiveOperation#DELETE} the tasks
	 *             are set with aspect for archiving, so could be skipped during
	 *             search.
	 */
	private void updateNodeWorkflows(NodeRef updateNode, Map<QName, Serializable> properties,
			JSONObject request, ArchiveOperation type) throws JSONException {
		boolean autoCancelWF = false;
		JSONObject nextStateData = new JSONObject();// data.has(NEXT_STATE_PROP_MAP)
		if (request.has(AUTOMATIC_ACTIONS_SET)) {
			if (request.getJSONObject(AUTOMATIC_ACTIONS_SET).has(ACTIVE_TASKS_PROPS_UPDATE)) {
				nextStateData = request.getJSONObject(AUTOMATIC_ACTIONS_SET).getJSONObject(
						ACTIVE_TASKS_PROPS_UPDATE);
			}
			if (request.getJSONObject(AUTOMATIC_ACTIONS_SET).has(AUTOMATIC_CANCEL_ACTIVE_WF)) {
				autoCancelWF = request.getJSONObject(AUTOMATIC_ACTIONS_SET).getBoolean(
						AUTOMATIC_CANCEL_ACTIVE_WF);
			}
		} // ? data
		// .getJSONObject(NEXT_STATE_PROP_MAP) : new JSONObject();
		Map<QName, Serializable> convertedMap = null;
		if (nextStateData != null) {
			convertedMap = toMap(nextStateData);
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
				setWorkflowTasksArchived(archiveData, workflowInstance, null);
			}
		}
		// process the activate wf
		workflowsForContent = getWorkflowService().getWorkflowsForContent(updateNode, true);
		for (WorkflowInstance workflowInstance : workflowsForContent) {
			List<WorkflowTask> activeTasks = null;
			// add the modification date for first work task
			if ((convertedMap != null) && (convertedMap.size() > 0)) {
				convertedMap.put(CMFModel.PROP_TASK_MODIFIED, new Date());
				WorkflowTaskQuery query = new WorkflowTaskQuery();
				query.setProcessId(workflowInstance.getId());
				query.setTaskState(WorkflowTaskState.IN_PROGRESS);
				activeTasks = getWorkflowService().queryTasks(query, true);
				for (WorkflowTask workflowTask : activeTasks) {
					// exposed as setWorkflowTasksArchived
					// if (autoCancelWF&&type==ArchiveOperation.DELETE) {
					// convertedMap.putAll(archiveData);
					// }

					// update with nextState data.
					getWorkflowService().updateTask(workflowTask.getId(), convertedMap, null, null);
				}
			}
			if ((type == ArchiveOperation.DELETE) && autoCancelWF) {
				setWorkflowTasksArchived(archiveData, workflowInstance, null);
			}
			// now it is time for cancelling the workflow.
			if (autoCancelWF) {
				getWorkflowService().cancelWorkflow(workflowInstance.getId());
			}
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
	private void setWorkflowTasksArchived(Map<QName, Serializable> archiveData,
			WorkflowInstance workflowInstance, List<WorkflowTask> tasksToexclude) {
		NodeRef workflowPackage = workflowInstance.getWorkflowPackage();
		// upate the package
		archiveData.remove(CMFModel.PROP_TASK_MODIFIED);
		nodeService.addProperties(workflowPackage, archiveData);
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setActive(null);
		taskQuery.setProcessId(workflowInstance.getId());
		taskQuery.setTaskState(null);
		taskQuery.setOrderBy(new OrderBy[] { OrderBy.TaskCreated_Asc });
		// set all tasks not searchable
		List<WorkflowTask> queryTasks = getWorkflowService().queryTasks(taskQuery, true);
		List<String> taskIds = null;
		if (tasksToexclude != null) {
			taskIds = new ArrayList<String>(tasksToexclude.size());
			for (WorkflowTask task : tasksToexclude) {
				taskIds.add(task.getId());
			}
		}
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
	private NodeRef createRequest(ArrayList<NodeRef> value, NodeRef requestPath, String content)
			throws JSONException {
		JSONObject request = new JSONObject(content);
		// TODO
		if (request.has(KEY_START_PATH)) {
			requestPath = caseService.getCMFCaseInstanceSpace(request.getString(KEY_START_PATH));
		} else if (request.has(KEY_SITE_ID)) {
			SiteInfo site = serviceRegistry.getSiteService()
					.getSite(request.getString(KEY_SITE_ID));
			if (site != null) {
				requestPath = caseService.getCMFCaseInstanceSpace(site.getNodeRef());
			}
		} else if (request.has(KEY_DEFINITION_ID)) {
			NodeRef nodeRef = caseService.getNodeRef(request.getString(KEY_DEFINITION_ID));
			if (nodeRef != null) {
				requestPath = caseService.getCMFCaseInstanceSpace(nodeRef);
			}
		}
		if (requestPath == null) {
			throw new WebScriptException(404, "Store location for requested case not found!");
		}
		Map<QName, Serializable> properties = toMap(request.getJSONObject(KEY_PROPERTIES));
		// try use the provided cm:name
		if (properties.get(ContentModel.PROP_NAME) == null) {
			String caseName = "case_" + GUID.generate();
			properties.put(ContentModel.PROP_NAME, caseName);
		}
		NodeRef createdCMFCaseSpace = caseService.createCMFCaseSpace(requestPath, properties);
		getOwnableService().setOwner(createdCMFCaseSpace, AuthenticationUtil.getSystemUserName());
		if (request.has(KEY_SECTIONS)) {
			JSONArray jsonArray = request.getJSONArray(KEY_SECTIONS);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				Map<QName, Serializable> model = toMap(object);
				NodeRef section = caseService.createCMFSectionSpace(createdCMFCaseSpace, model);
				cmfLockService.lockNode(section);
				value.add(section);
			}
		}
		cmfLockService.lockNode(createdCMFCaseSpace);
		return createdCMFCaseSpace;
	}
}
