/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of
 * Alfresco Alfresco is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. Alfresco is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.workflow;

import static org.alfresco.repo.workflow.WorkflowReportConstants.CANCELLED;
import static org.alfresco.repo.workflow.WorkflowReportConstants.COMPLETED;
import static org.alfresco.repo.workflow.WorkflowReportConstants.IN_PROGRESS;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sirma.itt.cmf.integration.model.CMFModel;

/**
 * Extension of {@link org.alfresco.repo.workflow.jbpm.QviJBPMEngine} to support
 * reporting of workflow tasks. When task is created/ended/updated corresponding
 * method from this class should be invoked to register the change. All tasks
 * are kept as nodes in 'system' container of workspace store. This subsystem
 * could be disabled by setting <code>enabled = false</code> in context xml
 * 
 * @author Borislav Banchev
 */
@SuppressWarnings("synthetic-access")
public class WorkflowReportServiceImpl implements WorkflowReportService {
	/**
	 * MAX size of cache.
	 */
	private static final int MAX_CACHE_SIZE = 100000;
	/** logger. */
	private static final Logger LOGGER = Logger.getLogger(WorkflowReportServiceImpl.class);

	/** The Constant DEBUG_ENABLED. */
	private static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();

	/** The enabled. */
	private boolean enabled = true;

	/** The node service. */
	private NodeService nodeService;

	/** The workflow service. */
	private WorkflowService workflowService;

	/** The repository. */
	private Repository repository;

	/** The registry. */
	private ServiceRegistry registry;

	/** The task node. */
	private NodeRef taskSpaceNode;

	/** The system node. */
	private NodeRef systemNode;


	/** The Constant UPDATED_DATA. */
	private static final Map<QName, QName> UPDATED_DATA = new HashMap<QName, QName>();

	/** The Constant COMPLETITION_TASKS. */
	private static final Map<String, Pair<QName, String>> COMPLETITION_TASKS = new HashMap<String, Pair<QName, String>>();

	/** The Constant CANCELLATION_TASKS. */
	private static final Map<String, Pair<QName, String>> CANCELLATION_TASKS = new HashMap<String, Pair<QName, String>>();

	/** semaphore lock. */
	private final ReentrantReadWriteLock taskAddingLock = new ReentrantReadWriteLock(true);

	/** The task cache. */
	private SimpleCache<String, NodeRef> taskCache = new MemoryCache<String, NodeRef>();

	/**
	 * inits the service.
	 */
	public void init() {
		//
		UPDATED_DATA.put(WorkflowModel.TYPE_PACKAGE, WorkflowReportConstants.PROP_PACKAGE);
		UPDATED_DATA.put(WorkflowModel.ASSOC_ASSIGNEE, WorkflowReportConstants.PROP_ASSIGNED);

		// adhoc
		COMPLETITION_TASKS.put("wf:completedAdhocTask", new Pair<QName, String>(
				WorkflowModel.PROP_STATUS, COMPLETED));
		CANCELLATION_TASKS.put("wf:completedAdhocTask", new Pair<QName, String>(
				WorkflowModel.PROP_STATUS, CANCELLED));

		// review & approve parallel
		COMPLETITION_TASKS.put("wf:approvedParallelTask", new Pair<QName, String>(
				WorkflowModel.PROP_STATUS, COMPLETED));
		CANCELLATION_TASKS.put("wf:rejectedParallelTask", new Pair<QName, String>(
				WorkflowModel.PROP_STATUS, COMPLETED));

		// review & approve
		COMPLETITION_TASKS.put("wf:approvedTask", new Pair<QName, String>(
				WorkflowModel.PROP_STATUS, COMPLETED));
		CANCELLATION_TASKS.put("wf:rejectedTask", new Pair<QName, String>(
				WorkflowModel.PROP_STATUS, COMPLETED));

		// draft ecn
		COMPLETITION_TASKS.put("engwf:DraftECNReviewAndAppovalByResponsibleEngineer",
				new Pair<QName, String>(WorkflowModel.PROP_OUTCOME, "Complete"));
		CANCELLATION_TASKS.put("engwf:DraftECNReviewCommentsAndUpdate", new Pair<QName, String>(
				WorkflowModel.PROP_OUTCOME, "Cancel"));
		// ecn
		COMPLETITION_TASKS.put("engwf:ECNRAAReviewAndApprovalByApprovalAuthorizationAuthorities",
				new Pair<QName, String>(WorkflowModel.PROP_OUTCOME, "Approve"));
		CANCELLATION_TASKS.put("engwf:ECNRAAUpdateOfECNWIPFiles", new Pair<QName, String>(
				WorkflowModel.PROP_OUTCOME, "Cancel"));
		// dcr
		COMPLETITION_TASKS.put("sirmawf:DCRWorkflowCompletion", new Pair<QName, String>(
				WorkflowModel.PROP_OUTCOME, "Complete workflow"));
		CANCELLATION_TASKS.put("sirmawf:DCRRejection", new Pair<QName, String>(
				WorkflowModel.PROP_OUTCOME, "Cancel Workflow"));

		// dra
		COMPLETITION_TASKS.put("sirmawf:DRAReviewTaskAndDocument04", new Pair<QName, String>(
				WorkflowModel.PROP_OUTCOME, "Complete workflow"));
		CANCELLATION_TASKS.put("sirmawf:DRAReviewTaskAndDocument07", new Pair<QName, String>(
				WorkflowModel.PROP_OUTCOME, "Cancel Workflow"));
		// rcc
		COMPLETITION_TASKS.put("sirmawf:DCCReviewTaskForCompletion", new Pair<QName, String>(
				WorkflowModel.PROP_OUTCOME, "Complete workflow"));
		CANCELLATION_TASKS.put("sirmawf:DCCReviewTaskForCancelation", new Pair<QName, String>(
				WorkflowModel.PROP_OUTCOME, "Cancel Workflow"));

	}

	/**
	 * Invocation of this method simply updates the corresponding node for this
	 * task.
	 * 
	 * @param endedTask
	 *            is the task to update
	 */
	public void endTask(WorkflowTask endedTask) {
		if (!isEnabled()) {
			return;
		}
		updateTask(endedTask);
	}

	/**
	 * Adds new task to the system in specific container, so the task to be
	 * stored as node.<br>
	 * <strong> This method should be invoked for new workflows only with one
	 * task</strong><br>
	 * Code is executed as {@link AuthenticationUtil#getSystemUserName()} to
	 * prevent update problems
	 * 
	 * @param workflowPath
	 *            is the workflow path to get the first task for.
	 */
	public void addTask(final WorkflowPath workflowPath) {
		if (!isEnabled()) {
			return;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("ADD TASK FOR PATH " + workflowPath.getId());
		}
		taskAddingLock.writeLock().lock();
		try {
			AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {

				@Override
				public NodeRef doWork() throws Exception {
					WorkflowInstance instance = workflowPath.getInstance();
					WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
					taskQuery.setProcessId(instance.getId());
					List<WorkflowTask> queryTasks = workflowService.queryTasks(taskQuery, true);
					WorkflowTask task = queryTasks.get(0);
					return addTaskInternal(task);

				}
			}, AuthenticationUtil.getSystemUserName());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			taskAddingLock.writeLock().unlock();
		}
	}

	/**
	 * The actual work for creating task node.
	 * 
	 * @param task
	 *            is the path for this taks
	 * @return the created task node
	 */
	private NodeRef addTaskInternal(final WorkflowTask task) {

		final String id = task.getId();
		// current node still may not be committed by
		// transaction but is at least kept in cache
		if (taskCache.contains(id)) {
			return taskCache.get(id);
		}
		NodeRef workfklowSpace = getNodeForWorkflow(task.getPath());
		Map<QName, Serializable> properties = extractTaskProperties(task);
		if (DEBUG_ENABLED) {
			LOGGER.debug("WorkflowReportServiceImpl.addTaskInternal() " + properties);
		}
		NodeRef createNode = createNode(workfklowSpace,
				QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, id),
				ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT, properties);

		if (taskCache.getKeys().size() > MAX_CACHE_SIZE) {
			taskCache.getKeys().clear();
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("CACHE SIZE: " + taskCache.getKeys().size());
		}
		taskCache.put(id, createNode);
		return createNode;

	}

	/**
	 * Updates tasks by getting all information for it and setting the node
	 * properties for this task to the new ones.<br>
	 * Code is executed as {@link AuthenticationUtil#getSystemUserName()} to
	 * prevent update problems
	 * 
	 * @param workflowInstance
	 *            is the task to update
	 */
	public void cancelWorkflow(final WorkflowInstance workflowInstance) {
		if (!isEnabled()) {
			return;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("CANCEL WORKFLOW (task) " + workflowInstance.getId());
		}
		taskAddingLock.writeLock().lock();
		try {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {
					fixWorkflowMetadata(
							null,
							workflowInstance,
							CANCELLED,
							getWorkflowItems(workflowInstance.getWorkflowPackage(),
									CMFModel.PROP_CASE_TYPE));
					return null;
				}
			}, AuthenticationUtil.getSystemUserName());
		} catch (Exception e) {
			LOGGER.error(e);
			// just print and skip
			e.printStackTrace();
		} finally {
			taskAddingLock.writeLock().unlock();
		}
	}

	/**
	 * Updates tasks by getting all information for it and setting the node
	 * properties for this task to the new ones.<br>
	 * Code is executed as {@link AuthenticationUtil#getSystemUserName()} to
	 * prevent update problems
	 * 
	 * @param updatedTask
	 *            is the task to update
	 */
	public void updateTask(final WorkflowTask updatedTask) {
		if (!isEnabled()) {
			return;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("UPDATE TASK (task) " + updatedTask.getId());
		}
		taskAddingLock.writeLock().lock();
		try {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {

					final String id = updatedTask.getId();
					if (!taskCache.contains(id)) {
						ResultSet query = registry.getSearchService().query(
								StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
								SearchService.LANGUAGE_LUCENE,
								"@cm\\:name:\"" + id + "\" AND ASPECT: \""
										+ CMFModel.ASPECT_TASK_NODE.toString() + "\"");
						List<NodeRef> nodeRefs = query.getNodeRefs();
						if ((nodeRefs == null) || nodeRefs.isEmpty()) {
							addTaskInternal(updatedTask);
						} else {
							nodeService.addProperties(nodeRefs.get(0),
									extractTaskProperties(updatedTask));
						}
					} else {
						nodeService.addProperties(taskCache.get(id),
								extractTaskProperties(updatedTask));
					}

					return null;
				}
			}, AuthenticationUtil.getSystemUserName());
		} catch (Exception e) {
			LOGGER.error(e);
			// just print and skip
			e.printStackTrace();
		} finally {
			taskAddingLock.writeLock().unlock();
		}
	}

	/**
	 * gets the node for workflow so to store task grouped by workflow name. If
	 * node does not exist is created lazy.
	 * 
	 * @param startWorkflow
	 *            is the path for the workflow.
	 * @return the container for the workflow specified in 'startWorkflow'
	 */
	private NodeRef getNodeForWorkflow(WorkflowPath startWorkflow) {
		WorkflowDefinition definition = startWorkflow.getInstance().getDefinition();
		final String name = definition.getName();
		NodeRef workflowNode = getChildFolderByName(getTaskSpace(), name);
		if (workflowNode == null) {
			workflowNode = createNode(getTaskSpace(),
					QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, name),
					ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER);

		}
		Calendar instance = Calendar.getInstance();
		int month = instance.get(Calendar.MONTH) + 1;
		int year = instance.get(Calendar.YEAR);
		// TODO may be the wf id
		String subspaceName = "tasks_" + year + "_" + month;
		NodeRef workfklowSubSpace = getChildFolderByName(workflowNode, subspaceName);
		if (workfklowSubSpace == null) {
			workfklowSubSpace = createNode(workflowNode,
					QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, subspaceName),
					ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER);
		}
		return workfklowSubSpace;
	}

	/**
	 * Simply creates new node by invoking.
	 * 
	 * @param parent
	 *            is the parent node
	 * @param name
	 *            is the name for created node
	 * @param assoc
	 *            is the association name
	 * @param type
	 *            is the created node type
	 * @return the create node
	 *         {@link #createNode(NodeRef, QName, QName, QName, Map)}
	 */
	private NodeRef createNode(final NodeRef parent, final QName name, QName assoc, QName type) {
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
		return createNode(parent, name, assoc, type, properties);
	}

	/**
	 * Simply creates new node by executing operation in new transacation.
	 * 
	 * @param parent
	 *            is the parent node
	 * @param name
	 *            is the name for created node
	 * @param assoc
	 *            is the association name
	 * @param type
	 *            is the created node type
	 * @param properties
	 *            are the properties for the node
	 * @return the created node.
	 */
	private NodeRef createNode(final NodeRef parent, final QName name, final QName assoc,
			final QName type, final Map<QName, Serializable> properties) {
		boolean newTransaction = AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE;
		if (newTransaction) {
			return registry.getRetryingTransactionHelper().doInTransaction(
					new RetryingTransactionCallback<NodeRef>() {

						@Override
						public NodeRef execute() throws Throwable {
							NodeRef childByName = nodeService.getChildByName(parent,
									ContentModel.ASSOC_CONTAINS, name.getLocalName());
							if (childByName != null) {
								return childByName;
							}
							properties.put(ContentModel.PROP_NAME, name.getLocalName());
							NodeRef childRef = nodeService.createNode(parent, assoc, name, type,
									properties).getChildRef();
							if (!ContentModel.TYPE_FOLDER.equals(type)) {
								nodeService.addAspect(childRef, CMFModel.ASPECT_TASK_NODE, null);
							}
							return childRef;
						}
					}, false, newTransaction);
		}
		properties.put(ContentModel.PROP_NAME, name.getLocalName());
		return nodeService.createNode(parent, assoc, name, type, properties).getChildRef();

	}

	/**
	 * Extracts properties for taks and return them as map to be used as node
	 * properties.
	 * 
	 * @param task
	 *            is the task to get properties for
	 * @return the properties as map
	 */
	private Map<QName, Serializable> extractTaskProperties(WorkflowTask task) {
		Map<QName, Serializable> taskProperties = new HashMap<QName, Serializable>();
		taskProperties.put(ContentModel.PROP_NAME, task.getId());
		taskProperties.put(ContentModel.PROP_TITLE, task.getName());

		Set<Entry<QName, Serializable>> entrySet = task.getProperties().entrySet();
		for (Entry<QName, Serializable> entry : entrySet) {
			QName qName = entry.getKey();
			if (StringUtils.isEmpty(qName.getNamespaceURI())) {
				continue;
			}
			if (UPDATED_DATA.containsKey(qName)) {
				QName qNameToUpdate = UPDATED_DATA.get(qName);
				if (qNameToUpdate != null) {
					taskProperties.put(qNameToUpdate, entry.getValue());
				} // else skip
			} else {
				taskProperties.put(qName, entry.getValue());
			}
		}
		Serializable taskId = taskProperties.remove(WorkflowModel.PROP_TASK_ID);
		if (taskId != null) {
			taskId = taskId.toString().replaceAll("\\D?", "");
		}
		taskProperties.put(WorkflowModel.PROP_TASK_ID, taskId);
		taskProperties.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_ID, task.getPath().getInstance()
				.getDefinition().getId());
		if (!taskProperties.containsKey(WorkflowModel.PROP_OUTCOME)) {
			taskProperties.put(WorkflowModel.PROP_OUTCOME, "Not Yet Started");
		}
		WorkflowInstance instance = task.getPath().getInstance();

		taskProperties.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME, instance.getDefinition()
				.getName());
		taskProperties.put(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, instance.getId());
		QName packageId = UPDATED_DATA.get(WorkflowModel.TYPE_PACKAGE);
		NodeRef nodeRef = (NodeRef) taskProperties.get(packageId);
		if (nodeService.exists(nodeRef)) {
			taskProperties.put(CMFModel.PROP_WF_CASE_TYPE,
					getWorkflowItems(nodeRef, CMFModel.PROP_CASE_TYPE));
		}
		taskProperties.put(CMFModel.PROP_WF_TASK_STATE, task.getState().toString());

		String definitionId = task.getDefinition().getNode().getName();
		if (definitionId != null) {
			taskProperties.put(CMFModel.PROP_WF_TASK_TYPE, definitionId);
		}
		// TODO fix priority
		taskProperties.put(WorkflowModel.PROP_PRIORITY, task.getPath().getInstance().getPriority());
		return taskProperties;
	}

	/**
	 * Fix workflow metadata by setting proper status and related data.
	 * 
	 * @param taskProperties
	 *            the task properties for current task (expected to be some kind
	 *            completition)
	 * @param workflow
	 *            the workflow is the workflow instance
	 * @param status
	 *            the status is some predifined status. if provided it is
	 *            directly used
	 * @param packageItems
	 *            are the attached documents.
	 */
	private void fixWorkflowMetadata(Map<QName, Serializable> taskProperties,
			WorkflowInstance workflow, String status, Serializable packageItems) {
		NodeRef workflowPackage = workflow.getWorkflowPackage();
		if (workflowPackage == null) {
			return;
		}
		String statusLocal = status;
		if (statusLocal == null) {
			// what is the task
			Serializable taskName = taskProperties.get(WorkflowModel.PROP_WORKFLOW_DEFINITION_ID);
			if (DEBUG_ENABLED) {
				LOGGER.debug("QviWorkflowReportService.fixWorkflowMetadata()  " + taskName + " "
						+ workflow.getEndDate() + " " + taskProperties);
			}
			if (taskName != null) {
				// TODO endDate?
				if (COMPLETITION_TASKS.containsKey(taskName)) {
					Pair<QName, String> pair = COMPLETITION_TASKS.get(taskName);
					Serializable propValue = taskProperties.get(pair.getFirst());
					if (pair.getSecond().equalsIgnoreCase(propValue.toString())) {
					}
				} else if (CANCELLATION_TASKS.containsKey(taskName)) {
					Pair<QName, String> pair = CANCELLATION_TASKS.get(taskName);
					Serializable propValue = taskProperties.get(pair.getFirst());
					if (pair.getSecond().equalsIgnoreCase(propValue.toString())) {
						statusLocal = CANCELLED;
					}
				}
			}
		}
		Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(8);
		boolean initial = false;
		// do optimization
		Serializable description = null;
		Serializable comment = null;
		if (statusLocal == null) {
			// is the first task to set status for WF?
			initial = nodeService.getProperty(workflowPackage, WorkflowModel.PROP_STATUS) == null;
			if (initial) {
				description = taskProperties.get(WorkflowModel.PROP_DESCRIPTION);
				comment = taskProperties.get(WorkflowModel.PROP_COMMENT);
				statusLocal = IN_PROGRESS;
			}
		}
		// do set if there is data to set
		if (statusLocal != null || initial) {
			newProps.put(WorkflowModel.PROP_STATUS, statusLocal);
			if (description != null) {
				newProps.put(WorkflowModel.PROP_DESCRIPTION, description);
			} else {
				newProps.put(WorkflowModel.PROP_DESCRIPTION, workflow.getDescription());
			}
			if (comment != null) {
				newProps.put(WorkflowModel.PROP_COMMENT, comment);
			}
			if (workflow.getDueDate() != null) {
				newProps.put(WorkflowModel.PROP_DUE_DATE, workflow.getDueDate());
			}
			if (workflow.getStartDate() != null) {
				newProps.put(WorkflowModel.PROP_START_DATE, workflow.getStartDate());
			}
			if (workflow.getEndDate() != null) {
				newProps.put(WorkflowModel.PROP_COMPLETION_DATE, workflow.getEndDate());
			} else if (CANCELLED.equalsIgnoreCase(statusLocal)
					|| COMPLETED.equalsIgnoreCase(statusLocal)) {
				newProps.put(WorkflowModel.PROP_COMPLETION_DATE, new Date());
			}
			if (packageItems != null) {
				newProps.put(CMFModel.PROP_WF_CASE_TYPE, packageItems);
			}

		}
		// do the update if there is smth to update
		if (!newProps.isEmpty()) {
			nodeService.addProperties(workflowPackage, newProps);
		}

	}

	/**
	 * Gets all items that are part of workflow and extracts names for them. All
	 * names are concated and split by '|'
	 * 
	 * @param nodeRef
	 *            is the node for package id
	 * @param property
	 *            is the property to retrieve from each child
	 * @return all package items name concated
	 */
	private String getWorkflowItems(NodeRef nodeRef, QName property) {
		List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
		String result = "";
		// if (subTypes == null) {
		// subTypes =
		// getRegistry().getDictionaryService().getSubTypes(ContentModel.TYPE_CONTENT,
		// false);
		// subTypes.add(ContentModel.TYPE_CONTENT);
		// }
		for (ChildAssociationRef childAssociationRef : childAssocs) {
			NodeRef childRef = childAssociationRef.getChildRef();
			// QName type = nodeService.getType(childRef);
			Serializable caseType = nodeService.getProperty(childRef, property);
			if (caseType != null) {
				// if (subTypes.contains(type)) {
				if (result.length() > 0) {
					result += "|";
				}
				result += caseType;
				// }
			}

		}
		return result;
	}

	/**
	 * Gets the root space for tasks - under system node.
	 * 
	 * @return the task space initialized lazy
	 */
	private NodeRef getTaskSpace() {

		if (taskSpaceNode != null) {
			return taskSpaceNode;
		}
		taskAddingLock.writeLock().lock();
		try {

			// for optimization lock here and check the node again
			if (taskSpaceNode != null) {
				return taskSpaceNode;
			}
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(repository
					.getRootHome());
			for (ChildAssociationRef childAssociationRef : childAssocs) {
				if (CMFModel.SYSTEM_QNAME.equals(childAssociationRef.getQName())) {
					systemNode = childAssociationRef.getChildRef();
					break;
				}
			}
			taskSpaceNode = getChildContainerByName(systemNode,
					WorkflowReportConstants.TASK_SPACE_ID);
			if (taskSpaceNode == null) {
				taskSpaceNode = createNode(systemNode,
						QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI,
								WorkflowReportConstants.TASK_SPACE_ID),
						ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_FOLDER);
			}

			return taskSpaceNode;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			taskAddingLock.writeLock().unlock();
		}
		return null;
	}

	/**
	 * Retrieves child by {@link ContentModel#PROP_NAME} value for assocations
	 * allowing duplicate name childs. <strong> First result is
	 * returned</strong>
	 * 
	 * @param parent
	 *            is the parent node
	 * @param name
	 *            is the value for {@link ContentModel#PROP_NAME}
	 * @return the found node or null
	 */
	private NodeRef getChildFolderByName(NodeRef parent, String name) {
		return nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);
	}

	/**
	 * Gets the child container by name.
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @return the child container by name
	 */
	private NodeRef getChildContainerByName(NodeRef parent, String name) {
		final List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(parent);
		for (final ChildAssociationRef childAssociationRef : childAssocs) {
			final NodeRef taskNodeCurrent = childAssociationRef.getChildRef();
			if (name.equals(nodeService.getProperty(taskNodeCurrent, ContentModel.PROP_NAME))) {
				return taskNodeCurrent;
			}
		}
		return null;
	}

	/**
	 * Sets the repository.
	 * 
	 * @param repository
	 *            the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * Gets the repository.
	 * 
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * Sets the node service.
	 * 
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Gets the node service.
	 * 
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * Sets the registry.
	 * 
	 * @param registry
	 *            the registry to set
	 */
	public void setRegistry(ServiceRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Gets the registry.
	 * 
	 * @return the registry
	 */
	public ServiceRegistry getRegistry() {
		return registry;
	}

	/**
	 * Sets the workflow service.
	 * 
	 * @param workflowService
	 *            the workflowService to set
	 */
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * Gets the workflow service.
	 * 
	 * @return the workflowService
	 */
	public WorkflowService getWorkflowService() {
		return workflowService;
	}

	/**
	 * Sets the enabled.
	 * 
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Checks if is enabled.
	 * 
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Updates tasks by getting all information for it and setting the node
	 * properties for this task to the new ones.<br>
	 * Code is executed as {@link AuthenticationUtil#getSystemUserName()} to
	 * prevent update problems
	 * 
	 * @param updatedTask
	 *            is the task to update
	 */
	private void deleteTask(final WorkflowTask updatedTask) {
		if (!isEnabled()) {
			return;
		}
		if (DEBUG_ENABLED) {
			System.err.println("DELETE TASK (task) " + updatedTask);
		}
		ResultSet query = registry.getSearchService().query(
				StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				SearchService.LANGUAGE_LUCENE,
				"@cm\\:name:\"" + updatedTask.getId() + "\" AND ASPECT: \""
						+ CMFModel.ASPECT_TASK_NODE.toString() + "\"");
		List<NodeRef> nodeRefs = query.getNodeRefs();
		if (nodeRefs != null) {
			for (NodeRef nodeRef : nodeRefs) {
				getNodeService().deleteNode(nodeRef);
			}
		}
	}

	/**
	 * Invoked on delete event. Not implemented yet
	 * 
	 * @param workflowId
	 *            is the workflow id
	 */
	public void deleteWorkflow(final String workflowId) {
		if (!isEnabled()) {
			return;
		}
		taskAddingLock.writeLock().lock();
		try {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {
					try {
						List<WorkflowPath> workflowById = workflowService
								.getWorkflowPaths(workflowId);
						for (WorkflowPath workflowPath : workflowById) {
							List<WorkflowTask> tasksForWorkflowPath = workflowService
									.getTasksForWorkflowPath(workflowPath.getId());
							for (WorkflowTask workflowTask : tasksForWorkflowPath) {
								deleteTask(workflowTask);
							}
						}
					} catch (Exception e) {
						// just print and skip so not to break alfresco code
						LOGGER.error(e.getLocalizedMessage());
						e.printStackTrace();
					}
					return null;
				}
			}, AuthenticationUtil.getSystemUserName());
		} catch (Exception e) {
			// just print and skip
			e.printStackTrace();
		} finally {
			taskAddingLock.writeLock().unlock();
		}
	}

	/**
	 * Creates new task if not exists - this method will process work only as
	 * last resort.
	 * 
	 * @param createdTask
	 *            is the task to process
	 */
	public void createIfNotExists(final WorkflowTask createdTask) {
		if (!isEnabled()) {
			return;
		}
		if (checkTaskIfExists(createdTask)) {
			return;
		}
		// use the same lock as in write mode
		taskAddingLock.writeLock().lock();
		try {
			// check again after lock of this node because of thread issues
			if (checkTaskIfExists(createdTask)) {
				return;
			}
			if (DEBUG_ENABLED) {
				LOGGER.debug("WILL CREATE TASK (task) " + createdTask.getId());
			}
			AuthenticationUtil.runAs(new RunAsWork<Void>() {
				@Override
				public Void doWork() throws Exception {
					ResultSet query = registry.getSearchService().query(
							StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
							SearchService.LANGUAGE_LUCENE,
							"@cm\\:name:\"" + createdTask.getId() + "\" AND ASPECT: \""
									+ CMFModel.ASPECT_TASK_NODE.toString() + "\"");
					List<NodeRef> nodeRefs = query.getNodeRefs();
					if ((nodeRefs == null) || nodeRefs.isEmpty()) {
						addTaskInternal(createdTask);
					}
					return null;
				}
			}, AuthenticationUtil.getSystemUserName());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			taskAddingLock.writeLock().unlock();
		}
	}

	/**
	 * Checks if task exists in cache. <strong>Lock mechanism is not
	 * used</strong>
	 * 
	 * @param createdTask
	 *            is the task to check
	 * @return true if is already in cache
	 */
	public boolean checkTaskIfExists(WorkflowTask createdTask) {
		// gets lock in this transaction - wait for init in write
		// transaction
		if (taskSpaceNode == null) {
			return true;
		}

		if (taskCache.contains(createdTask.getId())) {
			if (DEBUG_ENABLED) {
				LOGGER.debug("USING CACHE RETRIEVE TASK (task) " + createdTask.getId());
			}
			return true;
		}
		return false;
	}

	/**
	 * Setter method for taskCache.
	 * 
	 * @param taskCache
	 *            the taskCache to set
	 */
	public void setTaskCache(SimpleCache<String, NodeRef> taskCache) {
		this.taskCache = taskCache;
	}

	/**
	 * Getter method for taskCache.
	 * 
	 * @return the taskCache
	 */
	public SimpleCache<String, NodeRef> getTaskCache() {
		return taskCache;
	}

	/**
	 * Updates obsolete WF against last model of reporting.
	 * 
	 * @param workflowInstance
	 *            is the wf to update
	 * @param status
	 *            is the status to set for this wf
	 */
	public void setWorkflowStatus(final WorkflowInstance workflowInstance, final String status) {
		if (!isEnabled()) {
			return;
		}

		// use the same lock as in write mode
		taskAddingLock.writeLock().lock();
		try {

			if (DEBUG_ENABLED) {
				LOGGER.debug("WILL UPDATE WORKFLOW to " + status);
			}
			AuthenticationUtil.runAs(new RunAsWork<Void>() {
				@Override
				public Void doWork() throws Exception {
					fixWorkflowMetadata(
							null,
							workflowInstance,
							status,
							getWorkflowItems(workflowInstance.getWorkflowPackage(),
									CMFModel.PROP_CASE_TYPE));
					return null;
				}
			}, AuthenticationUtil.getSystemUserName());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			taskAddingLock.writeLock().unlock();
		}

	}

}
