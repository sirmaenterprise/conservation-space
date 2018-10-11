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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.alfresco.model.ContentModel;
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
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
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
import org.springframework.core.io.ClassPathResource;

import com.sirma.itt.cmf.integration.exception.SEIPRuntimeException;
import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.service.CMFService;

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
	/** Root path for task storage. */
	public static final String SYSTEM_TASK_INDEXES_SPACE = "/sys:system/cmfwf:taskIndexesSpace//*";
	/**
	 * MAX size of cache.
	 */
	public static final int MAX_CACHE_SIZE = 2000000;
	/** logger. */
	private static final Logger LOGGER = Logger.getLogger(WorkflowReportServiceImpl.class);

	/** The Constant DEBUG_ENABLED. */
	private static final boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();
	private static final boolean TRACE_ENABLED = LOGGER.isTraceEnabled();
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
	private Map<String, NodeRef> taskSpaceNodes = new HashMap<String, NodeRef>();

	/** The Constant UPDATED_DATA. */
	private static final Map<QName, QName> UPDATED_DATA = new HashMap<QName, QName>();

	private static final Set<QName> SKIPPED_DATA = new HashSet<QName>();

	/** semaphore lock. */
	private final Map<String, ReentrantLock> taskAddingLock = new HashMap<String, ReentrantLock>();

	/** The cache size. */
	private int cacheSize = getCacheSize();

	/** The task cache. */
	private MemoryCache<String, NodeRef> taskCache = new MemoryCache<String, NodeRef>(cacheSize);
	private PersonService personService;
	private AuthorityService authorityService;
	private String baseQuery = "PATH:\"" + SYSTEM_TASK_INDEXES_SPACE + "\" AND ASPECT: \""
			+ CMFModel.ASPECT_TASK_NODE.toString() + "\" AND cm:name:\"";

	/**
	 * Gets the cache size from config or fallback to default of
	 * {@value #MAX_CACHE_SIZE}
	 *
	 * @return the cache size
	 */
	private int getCacheSize() {
		int size = MAX_CACHE_SIZE;
		try {
			ClassPathResource globalProps = new ClassPathResource("alfresco-global.properties");
			Properties globalProperties = new Properties();
			globalProperties.load(globalProps.getInputStream());
			Object object = globalProperties.get("cache.inmemory.task.size");
			if (object != null) {
				size = Integer.valueOf(object.toString());
			}
		} catch (Exception e) {
			throw new SEIPRuntimeException("Fail to config task cache size!", e);
		}
		return size;
	}

	/**
	 * inits the service.
	 */
	public void init() {
		//
		UPDATED_DATA.put(WorkflowModel.TYPE_PACKAGE, WorkflowReportConstants.PROP_PACKAGE);
		UPDATED_DATA.put(WorkflowModel.ASSOC_ASSIGNEE, WorkflowReportConstants.PROP_ASSIGNED);
		SKIPPED_DATA.add(WorkflowModel.ASSOC_POOLED_ACTORS);
	}

	/**
	 * Invocation of this method simply updates the corresponding node for this
	 * task.
	 *
	 * @param endedTask
	 *            is the task to update
	 */
	@Override
	public void endTask(WorkflowTask endedTask) {
		if (!isEnabled()) {
			return;
		}
		updateTask(endedTask);
	}

	@Override
	public void addTask(final WorkflowPath workflowPath) {
		if (!isEnabled()) {
			return;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("ADD TASK FOR PATH " + workflowPath.getId());
		}
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		acquiredLock.lock();
		try {
			WorkflowInstance instance = workflowPath.getInstance();
			final List<WorkflowTask> queryTasks = listTasks(null, instance.getId());
			AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {

				@Override
				public NodeRef doWork() throws Exception {
					WorkflowTask task = queryTasks.get(0);
					return addTaskInternal(task);

				}
			}, CMFService.getSystemUser());
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			acquiredLock.unlock();
		}
	}

	private ReentrantLock acquireLock(String id) {
		synchronized (WorkflowReportServiceImpl.class) {
			String lockId = StringUtils.isBlank(id) ? "ReentrantLockDefaultLockId" : id;
			if (!taskAddingLock.containsKey(lockId)) {
				taskAddingLock.put(lockId, new ReentrantLock(true));
			}
			return taskAddingLock.get(lockId);
		}
	}

	@Override
	public NodeRef addTask(final WorkflowTask task) {
		if (!isEnabled()) {
			return null;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("ADD TASK " + task.getId());
		}
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		acquiredLock.lock();
		try {
			final List<WorkflowTask> queryTasks = listTasks(task.getId(), null);
			return AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {

				@Override
				public NodeRef doWork() throws Exception {
					WorkflowTask task = queryTasks.get(0);
					return addTaskInternal(task);

				}
			}, CMFService.getSystemUser());
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			acquiredLock.unlock();
		}
		return null;
	}

	@Override
	public NodeRef addStandaloneTask(final String taskId, final Map<QName, Serializable> props) {
		if (!isEnabled()) {
			return null;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("ADD TASK FOR STANDALONE " + taskId);
		}
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		acquiredLock.lock();
		try {
			final List<WorkflowTask> queryTasks = listTasks(taskId, null);
			return AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {

				@Override
				public NodeRef doWork() throws Exception {
					WorkflowTask task = queryTasks.get(0);
					task.getProperties().putAll(props);
					return addTaskInternal(task);

				}
			}, CMFService.getSystemUser());
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			acquiredLock.unlock();
		}
		return null;
	}

	private List<WorkflowTask> listTasks(final String taskId, final String processId) {
		return AuthenticationUtil.runAs(new RunAsWork<List<WorkflowTask>>() {

			@Override
			public List<WorkflowTask> doWork() throws Exception {
				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				if (taskId != null) {
					taskQuery.setTaskId(taskId);
				}
				if (processId != null) {
					taskQuery.setProcessId(processId);
				}
				return workflowService.queryTasks(taskQuery, true);
			}
		}, AuthenticationUtil.getSystemUserName());
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
		NodeRef workfklowSpace = getNodeForTask(task.getPath());
		Map<QName, Serializable> properties = extractTaskProperties(task);
		if (DEBUG_ENABLED) {
			LOGGER.debug("WorkflowReportServiceImpl.addTaskInternal() " + properties);
		}
		NodeRef createNode = createNode(workfklowSpace, QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, id),
				ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT, properties);

		if (taskCache.getKeys().size() > cacheSize) {
			LOGGER.warn("Cache size limit reached. Please expand size of parameter 'cache.inmemory.task.size'!");
			// taskCache.getKeys().clear();
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("CACHE SIZE: " + taskCache.getKeys().size());
		}
		// put the id
		task.getProperties().put(ContentModel.PROP_NODE_UUID, createNode.getId());
		taskCache.put(id, createNode);

		return createNode;

	}

	@Override
	public void cancelWorkflow(final WorkflowInstance workflowInstance) {
		if (!isEnabled()) {
			return;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("CANCEL WORKFLOW (task) " + workflowInstance.getId());
		}
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		acquiredLock.lock();
		try {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {
					fixWorkflowMetadata(null, workflowInstance, CANCELLED,
							getWorkflowItems(workflowInstance.getWorkflowPackage(), CMFModel.PROP_TYPE));
					return null;
				}
			}, CMFService.getSystemUser());
		} catch (Exception e) {
			LOGGER.error(e);
			// just print and skip
		} finally {
			acquiredLock.unlock();
		}
	}

	@Override
	public void updateTask(final WorkflowTask updatedTask) {
		if (!isEnabled()) {
			return;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("UPDATE TASK (task) " + updatedTask.getId());
		}
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		acquiredLock.lock();
		try {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {

					final String id = updatedTask.getId();
					if (!taskCache.contains(id)) {
						ResultSet query = registry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
								SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, buildTaskQuery(updatedTask));
						List<NodeRef> nodeRefs = query.getNodeRefs();
						if ((nodeRefs == null) || nodeRefs.isEmpty()) {
							addTaskInternal(updatedTask);
						} else {
							nodeService.addProperties(nodeRefs.get(0), extractTaskProperties(updatedTask));
						}
						query = null;
					} else {
						nodeService.addProperties(taskCache.get(id), extractTaskProperties(updatedTask));
					}

					return null;
				}
			}, CMFService.getSystemUser());
		} catch (Exception e) {
			// just print and skip
			LOGGER.error(e);
		} finally {
			acquiredLock.unlock();
		}
	}

	/**
	 * Gets the node for workflow so to store task grouped by workflow name or
	 * for standalone task. If node does not exist is created lazily.
	 *
	 * @param startWorkflow
	 *            is the path for the workflow.
	 * @return the container for the workflow specified in 'startWorkflow'
	 */
	private NodeRef getNodeForTask(WorkflowPath startWorkflow) {
		String name = null;
		if (startWorkflow == null) {
			name = "standalonetasks";
			return getTaskSpecificSpace(name);
		}
		WorkflowDefinition definition = startWorkflow.getInstance().getDefinition();
		name = definition.getName();
		return getTaskSpecificSpace(name);
	}

	/**
	 * Get the name of the specified container using the provded name.Current
	 * space is calculated using timestaml
	 *
	 * @param name
	 *            the wf name or the standalone space
	 * @return the current container for tasks
	 */
	private NodeRef getTaskSpecificSpace(String name) {
		NodeRef workflowNode = getChildFolderByName(getTaskSpace(), name);
		if (workflowNode == null) {
			workflowNode = createNode(getTaskSpace(), QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, name),
					ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER);
		}
		Calendar instance = Calendar.getInstance();
		int month = instance.get(Calendar.MONTH) + 1;
		int year = instance.get(Calendar.YEAR);
		// TODO may be the wf id
		String subspaceName = "tasks_" + year + "_" + month;
		NodeRef taskSpace = getChildFolderByName(workflowNode, subspaceName);
		if (taskSpace == null) {
			taskSpace = createNode(workflowNode, QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, subspaceName),
					ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER);
		}
		return taskSpace;
	}

	/**
	 * Simply creates new node by invoking
	 * {@link #createNode(NodeRef, QName, QName, QName, Map)}
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
	private NodeRef createNode(final NodeRef parent, final QName name, final QName assoc, final QName type,
			final Map<QName, Serializable> properties) {
		boolean newTransaction = AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE;
		if (newTransaction) {
			return registry.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {

				@Override
				public NodeRef execute() throws Throwable {
					NodeRef childByName = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS,
							name.getLocalName());
					if (childByName != null) {
						return childByName;
					}
					properties.put(ContentModel.PROP_NAME, name.getLocalName());
					NodeRef childRef = nodeService.createNode(parent, assoc, name, type, properties).getChildRef();
					if (!ContentModel.TYPE_FOLDER.equals(type)) {
						nodeService.addAspect(childRef, CMFModel.ASPECT_TASK_NODE, null);
					}
					return childRef;
				}
			}, false, newTransaction);
		}
		properties.put(ContentModel.PROP_NAME, name.getLocalName());
		if (TRACE_ENABLED) {
			LOGGER.trace("WorkflowReportServiceImpl.createNode() " + parent + " " + assoc + " " + name + " " + type
					+ " " + properties);
		}
		NodeRef childRef = nodeService.createNode(parent, assoc, name, type, properties).getChildRef();
		if (!ContentModel.TYPE_FOLDER.equals(type)) {
			nodeService.addAspect(childRef, CMFModel.ASPECT_TASK_NODE, null);
		}
		return childRef;

	}

	/**
	 * Gets the person.
	 *
	 * @param value
	 *            the value
	 * @return the person
	 */
	private Pair<NodeRef, String> getActor(Object value) {
		String stringValue = value.toString();
		if (NodeRef.isNodeRef(stringValue)) {
			NodeRef ref = new NodeRef(stringValue.toString());
			if (nodeService.exists(ref)) {
				if (ContentModel.TYPE_AUTHORITY_CONTAINER.equals(nodeService.getType(ref))) {
					return new Pair<NodeRef, String>(ref,
							(String) getNodeService().getProperty(ref, ContentModel.PROP_AUTHORITY_NAME));
				}
				return new Pair<NodeRef, String>(ref,
						(String) getNodeService().getProperty(ref, ContentModel.PROP_USERNAME));
			}
		} else if (value instanceof String) {
			// could be collision
			return new Pair<NodeRef, String>(null, stringValue);

		}

		return null;
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
		if (DEBUG_ENABLED) {
			LOGGER.debug("Extracting data for " + task.getId() + " : " + task);
		}

		Map<QName, Serializable> propertiesCurrent = task.getProperties();
		return prepareTaskProperties(task, propertiesCurrent);
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Map<QName, Serializable> prepareTaskProperties(WorkflowTask task,
			Map<QName, Serializable> propertiesCurrent) {
		Map<QName, Serializable> taskProperties = new HashMap<QName, Serializable>();
		Set<Entry<QName, Serializable>> entrySet = propertiesCurrent.entrySet();

		if (DEBUG_ENABLED) {
			LOGGER.debug("Metadata of '" + task.getId() + "' is :" + propertiesCurrent);
		}
		if (propertiesCurrent.get(WorkflowModel.ASSOC_POOLED_ACTORS) instanceof Collection) {
			Collection<?> pooledActors = (Collection<?>) propertiesCurrent.remove(WorkflowModel.ASSOC_POOLED_ACTORS);
			// process if actually there is data
			if (pooledActors.size() > 0) {
				LinkedHashSet<String> pooledUsers = new LinkedHashSet<String>();
				LinkedHashSet<String> pooledGroups = new LinkedHashSet<String>();
				for (Object object : pooledActors) {
					String actorId = getActor(object).getSecond();
					AuthorityType authorityType = AuthorityType.getAuthorityType(actorId);
					if (authorityType == AuthorityType.GROUP) {
						pooledGroups.add(actorId);
					} else if (authorityType == AuthorityType.USER) {
						pooledUsers.add(actorId);
					}
				}
				taskProperties.put(WorkflowReportConstants.PROP_POOLED_GROUPS, pooledGroups);
				taskProperties.put(WorkflowReportConstants.PROP_POOLED_ACTORS, pooledUsers);
			}
		}
		if (propertiesCurrent.get(WorkflowModel.ASSOC_ASSIGNEES) instanceof Collection) {
			Collection<?> pooledActors = (Collection<?>) propertiesCurrent.get(WorkflowModel.ASSOC_ASSIGNEES);

			taskProperties.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable) pooledActors);
		}
		if (propertiesCurrent.get(WorkflowModel.ASSOC_GROUP_ASSIGNEE) != null) {
			String pooledGroup = propertiesCurrent.get(WorkflowModel.ASSOC_GROUP_ASSIGNEE).toString();
			String actorId = getActor(pooledGroup).getSecond();
			taskProperties.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, actorId);
		}
		for (Entry<QName, Serializable> entry : entrySet) {
			QName qName = entry.getKey();
			if (SKIPPED_DATA.contains(qName)) {
				continue;
			}
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
		taskProperties.put(ContentModel.PROP_NAME, task.getId());
		taskProperties.put(ContentModel.PROP_TITLE, task.getName());

		Serializable taskId = taskProperties.remove(WorkflowModel.PROP_TASK_ID);
		if (taskId != null) {
			taskId = taskId.toString().replaceAll("\\D?", "");
		}
		taskProperties.put(WorkflowModel.PROP_TASK_ID, taskId);
		if (!taskProperties.containsKey(WorkflowModel.PROP_OUTCOME)) {
			taskProperties.put(WorkflowModel.PROP_OUTCOME, "Not Yet Started");
		}
		taskProperties.put(CMFModel.PROP_WF_TASK_STATE, task.getState().toString());
		QName packageId = UPDATED_DATA.get(WorkflowModel.TYPE_PACKAGE);
		Object nodeRef = taskProperties.get(packageId);
		if (nodeRef != null && !nodeRef.toString().trim().isEmpty()) {
			NodeRef nodeRefCreated = new NodeRef(nodeRef.toString());
			if (nodeService.exists(nodeRefCreated)) {
				Pair<String, String> workflowItems = getWorkflowItems(nodeRefCreated, CMFModel.PROP_TYPE);
				taskProperties.put(CMFModel.PROP_WF_CONTEXT_TYPE, workflowItems.getFirst());
				taskProperties.put(CMFModel.PROP_WF_CONTEXT_ID, workflowItems.getSecond());
			}
		}

		WorkflowPath workflowPath = task.getPath();
		if (workflowPath == null) {
			// skip wf data
			return taskProperties;
		}

		WorkflowInstance instance = workflowPath.getInstance();
		taskProperties.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_ID,
				workflowPath.getInstance().getDefinition().getId());

		taskProperties.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME, instance.getDefinition().getName());
		taskProperties.put(WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, instance.getId());

		String definitionId = task.getDefinition().getNode().getName();
		if (definitionId != null) {
			taskProperties.put(CMFModel.PROP_TYPE, definitionId);
		}
		// fix priority if not set
		if (!propertiesCurrent.containsKey(WorkflowModel.PROP_PRIORITY)) {
			taskProperties.put(WorkflowModel.PROP_PRIORITY, workflowPath.getInstance().getPriority());
		} else {
			taskProperties.put(WorkflowModel.PROP_PRIORITY, propertiesCurrent.get(WorkflowModel.PROP_PRIORITY));
		}
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
	private void fixWorkflowMetadata(Map<QName, Serializable> taskProperties, WorkflowInstance workflow, String status,
			Pair<String, String> packageItems) {
		NodeRef workflowPackage = workflow.getWorkflowPackage();
		if (workflowPackage == null) {
			return;
		}
		String statusLocal = status;
		if (statusLocal == null) {
			// what is the task
			Serializable taskName = taskProperties.get(WorkflowModel.PROP_WORKFLOW_DEFINITION_ID);
			if (DEBUG_ENABLED) {
				LOGGER.debug("fixWorkflowMetadata()  " + taskName + " " + workflow.getEndDate() + " " + taskProperties);
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
		if ((statusLocal != null) || initial) {
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
			} else if (CANCELLED.equalsIgnoreCase(statusLocal) || COMPLETED.equalsIgnoreCase(statusLocal)) {
				newProps.put(WorkflowModel.PROP_COMPLETION_DATE, new Date());
			}

			if (packageItems != null) {
				newProps.put(CMFModel.PROP_WF_CONTEXT_TYPE, packageItems.getFirst());
				newProps.put(CMFModel.PROP_WF_CONTEXT_ID, packageItems.getSecond());
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
	private Pair<String, String> getWorkflowItems(NodeRef nodeRef, QName property) {
		List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
		String result = "";
		StringBuilder nodes = new StringBuilder(60);

		for (ChildAssociationRef childAssociationRef : childAssocs) {
			NodeRef childRef = childAssociationRef.getChildRef();
			Serializable caseType = nodeService.getProperty(childRef, property);
			if (caseType != null) {
				if (result.length() > 0) {
					result += "|";
				}
				result += caseType;
			}
			if (nodes.length() > 0) {
				nodes.append("|");
			}
			nodes.append(childRef);
		}
		return new Pair<String, String>(result, nodes.toString());
	}

	/**
	 * Gets the root space for tasks - under system node.
	 *
	 * @return the task space initialized lazy
	 */
	private NodeRef getTaskSpace() {
		String tenantId = CMFService.getTenantId();
		NodeRef taskSpaceNode = taskSpaceNodes.get(tenantId);
		if (taskSpaceNode != null) {
			return taskSpaceNode;
		}
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		acquiredLock.lock();
		try {

			// for optimization lock here and check the node again
			taskSpaceNode = taskSpaceNodes.get(tenantId);
			if (taskSpaceNode != null) {
				return taskSpaceNode;
			}
			List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(repository.getRootHome());
			NodeRef systemNode = null;
			for (ChildAssociationRef childAssociationRef : childAssocs) {
				if (CMFModel.SYSTEM_QNAME.equals(childAssociationRef.getQName())) {
					systemNode = childAssociationRef.getChildRef();
					break;
				}
			}
			taskSpaceNode = getChildContainerByName(systemNode, WorkflowReportConstants.TASK_SPACE_ID);
			if (taskSpaceNode == null) {
				taskSpaceNode = createNode(systemNode,
						QName.createQName(CMFModel.CMF_WORKFLOW_MODEL_1_0_URI, WorkflowReportConstants.TASK_SPACE_ID),
						ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_FOLDER);
			}

			taskSpaceNodes.put(tenantId, taskSpaceNode);
			return taskSpaceNode;
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			acquiredLock.unlock();
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
	 * Deletes task corresponding node
	 * 
	 * @param task
	 *            the task to delete
	 */
	private void deleteTask(final WorkflowTask task) {
		if (!isEnabled()) {
			return;
		}
		if (DEBUG_ENABLED) {
			LOGGER.debug("DELETE TASK (task) " + task);
		}
		ResultSet query = registry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, buildTaskQuery(task));
		List<NodeRef> nodeRefs = query.getNodeRefs();
		if (nodeRefs != null) {
			for (NodeRef nodeRef : nodeRefs) {
				getNodeService().deleteNode(nodeRef);
			}
		}
		query = null;
	}

	/**
	 * Invoked on delete event. Not implemented yet
	 *
	 * @param workflowId
	 *            is the workflow id
	 */
	@Override
	public void deleteWorkflow(final String workflowId) {
		if (!isEnabled()) {
			return;
		}
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		acquiredLock.lock();
		try {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {
					try {
						List<WorkflowPath> workflowById = workflowService.getWorkflowPaths(workflowId);
						for (WorkflowPath workflowPath : workflowById) {
							List<WorkflowTask> tasksForWorkflowPath = workflowService
									.getTasksForWorkflowPath(workflowPath.getId());
							for (WorkflowTask workflowTask : tasksForWorkflowPath) {
								deleteTask(workflowTask);
							}
						}
					} catch (Exception e) {
						// just print and skip so not to break alfresco code
						LOGGER.error(e);
					}
					return null;
				}
			}, CMFService.getSystemUser());
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			acquiredLock.unlock();
		}
	}

	/**
	 * Creates new task if not exists - this method will process work only as
	 * last resort.
	 *
	 * @param createdTask
	 *            is the task to process
	 */
	@Override
	public void createIfNotExists(final WorkflowTask createdTask) {
		if (!isEnabled()) {
			return;
		}
		if (checkTaskIfExists(createdTask)) {
			return;
		}
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		// use the same lock as in write mode
		acquiredLock.lock();
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
					ResultSet query = registry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
							SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, buildTaskQuery(createdTask));
					List<NodeRef> nodeRefs = query.getNodeRefs();
					if ((nodeRefs == null) || nodeRefs.isEmpty()) {
						addTaskInternal(createdTask);
					}
					query = null;
					return null;
				}
			}, CMFService.getSystemUser());
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			acquiredLock.unlock();
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
		if (taskSpaceNodes.get(CMFService.getTenantId()) == null) {
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
	 * Builds the task query.
	 *
	 * @param task
	 *            the task
	 * @return the string
	 */
	private String buildTaskQuery(final WorkflowTask task) {
		// for all tenants it is the same
		String query = new StringBuffer(baseQuery).append(task.getId()).append("\"").toString();
		return query;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.workflow.WorkflowReportService#getTaskNode(org.alfresco
	 * .service.cmr.workflow.WorkflowTask)
	 */
	@Override
	public NodeRef getTaskNode(final WorkflowTask task) {
		ReentrantLock acquiredLock = acquireLock(CMFService.getTenantId());
		acquiredLock.lock();
		try {
			if (taskCache.contains(task.getId())) {
				return taskCache.get(task.getId());
			}
			return AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
				@Override
				public NodeRef doWork() throws Exception {
					ResultSet query = registry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
							SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, buildTaskQuery(task));
					List<NodeRef> nodeRefs = query.getNodeRefs();
					if ((nodeRefs == null) || nodeRefs.size() != 1) {
						return null;
					}
					query = null;
					return nodeRefs.get(0);
				}

			}, CMFService.getSystemUser());
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			acquiredLock.unlock();
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

}
