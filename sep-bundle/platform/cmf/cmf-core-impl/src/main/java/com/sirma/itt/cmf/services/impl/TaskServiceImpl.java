package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.entity.AssignedUserTasks;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.TaskType;
import com.sirma.itt.cmf.beans.model.WorkLogEntry;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.LinkConstantsCmf;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.db.DbQueryTemplates;
import com.sirma.itt.cmf.event.task.worklog.AddedWorkLogEntryEvent;
import com.sirma.itt.cmf.event.task.worklog.DeleteWorkLogEntryEvent;
import com.sirma.itt.cmf.event.task.worklog.UpdatedWorkLogEntryEvent;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowTaskService;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Quad;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceContext;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;

/**
 * Default implementation for the {@link TaskService} interface. The service tries to act as a proxy
 * for standalone and workflow task services.
 *
 * @author BBonev
 */
@Stateless
public class TaskServiceImpl implements TaskService {

	private static final String ACTIVE = "active";
	private static final String SOURCE_TYPE = "sourceType";
	private static final String SOURCE_ID = "sourceId";
	private static final String THE_LIST_IS_PERSISTED_EXTERNALLY = "The list is persisted externally";

	/** The Constant ACTIVE_USERS_ON_CASE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 50), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store the active users lists for a instance. The cache will have 2 entries per instance that has tasks.<br>Minimal value expression: users * 3"))
	private static final String ACTIVE_USERS_ON_CASE = "ACTIVE_USERS_PER_ENTITY_CACHE";
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 50), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store the pool group,users lists for a instance. The cache will have 2 entries per instance that has tasks.<br>Minimal value expression: users * 2"))
	private static final String POOL_RESOURCES_ON_CONTEXT = "POOL_RESOURCES_ON_CONTEXT_CACHE";
	/** The Constant ACTIVE_TASKS. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 150), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
			+ "Cache holding the current task attached to instance. Keys for search includes instance id, task state, task type.<br>Minimal value expression: instances * 5"))
	private static final String TASK_ON_INSTANCE = "TASK_ON_CONTEXT_CACHE";
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskServiceImpl.class);

	/** The Constant debug. */
	private static final boolean DEBUG = LOGGER.isDebugEnabled();

	/** The standalone task service. */
	@Inject
	private StandaloneTaskService standaloneTaskService;

	/** The workflow task service. */
	@Inject
	private WorkflowTaskService workflowTaskService;

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypes.INSTANCE)
	private InstanceDao<Instance> instanceDao;

	/** The entity cache context. */
	@Inject
	private EntityLookupCacheContext entityCacheContext;
	/** The group service. */
	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

	/** The event service. */
	@Inject
	private EventService eventService;

	// @Inject
	// @Config(name = "property.task.pool.group", defaultValue =
	// TaskProperties.TASK_GROUP_ASSIGNEE)
	private String groupAssigneePoolPropertyName = TaskProperties.TASK_GROUP_ASSIGNEE;
	// @Inject
	// @Config(name = "property.task.pool.users", defaultValue =
	// TaskProperties.TASK_ASSIGNEES)
	private String userAssigneesPoolPropertyName = TaskProperties.TASK_ASSIGNEES;

	private String mixedAssigneesPoolPropertyName = TaskProperties.TASK_MULTI_ASSIGNEES;

	/**
	 * Initialize.
	 */
	@PostConstruct
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void initialize() {
		if (!entityCacheContext.containsCache(ACTIVE_USERS_ON_CASE)) {
			entityCacheContext.createCache(ACTIVE_USERS_ON_CASE, new ActiveUsersOnCaseLookup());
		}
		if (!entityCacheContext.containsCache(POOL_RESOURCES_ON_CONTEXT)) {
			entityCacheContext.createCache(POOL_RESOURCES_ON_CONTEXT,
					new PoolResourcesOnUnassignedContextLookup());
		}
		if (!entityCacheContext.containsCache(TASK_ON_INSTANCE)) {
			entityCacheContext.createCache(TASK_ON_INSTANCE, new TaskOnContextLookup());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Class<DefinitionModel> getInstanceDefinitionClass() {
		return DefinitionModel.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public AbstractTaskInstance createInstance(DefinitionModel definition, Instance parent) {
		return detectService(definition).createInstance(definition, parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public AbstractTaskInstance createInstance(DefinitionModel definition, Instance parent,
			Operation operation) {
		return detectService(definition).createInstance(definition, parent, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh(AbstractTaskInstance instance) {
		detectService(instance).refresh(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<AbstractTaskInstance> loadInstances(Instance owner) {
		return detectService(owner).loadInstances(owner);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public AbstractTaskInstance loadByDbId(Serializable id) {
		AbstractTaskInstance instance = standaloneTaskService.loadByDbId(id);
		if (instance == null) {
			instance = workflowTaskService.loadByDbId(id);
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public AbstractTaskInstance load(Serializable instanceId) {
		AbstractTaskInstance instance = standaloneTaskService.load(instanceId);
		if (instance == null) {
			instance = workflowTaskService.load(instanceId);
		}
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<AbstractTaskInstance> load(List<S> ids) {
		return loadInternal(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<AbstractTaskInstance> loadByDbId(List<S> ids) {
		return loadInternal(ids, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<AbstractTaskInstance> load(List<S> ids,
			boolean allProperties) {
		return loadInternal(ids, allProperties);
	}

	/**
	 * Load internal.
	 *
	 * @param <K>
	 *            the key type
	 * @param ids
	 *            the ids
	 * @param allProperties
	 *            the all properties
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	private <K> List<AbstractTaskInstance> loadInternal(List<K> ids, boolean allProperties) {
		if ((ids == null) || ids.isEmpty()) {
			return CollectionUtils.emptyList();
		}
		// find out what is the argument type - the DB ids are 40 chars long and instance id's are
		// like 10
		boolean isDbId = (ids.get(0) instanceof String) && (ids.get(0).toString().length() > 30);

		List<AbstractTaskInstance> tasks = new ArrayList<AbstractTaskInstance>(ids.size());
		Map<K, AbstractTaskInstance> mapping = CollectionUtils.createLinkedHashMap(ids.size());

		// first try to load the standalone tasks
		List<StandaloneTaskInstance> list;
		if (isDbId) {
			list = standaloneTaskService.loadByDbId((List<Long>) ids, allProperties);
		} else {
			list = standaloneTaskService.load((List<String>) ids, allProperties);
		}
		copyEntityToMap(list, mapping, isDbId);

		// if not all are standalone or not at all then we load as workflow
		// tasks
		if (ids.size() != list.size()) {
			List<TaskInstance> list2;
			if (isDbId) {
				list2 = workflowTaskService.loadByDbId((List<Long>) ids, allProperties);
			} else {
				list2 = workflowTaskService.load((List<String>) ids, allProperties);
			}
			copyEntityToMap(list2, mapping, isDbId);
		}
		// to keep the sort order in the final result
		for (K id : ids) {
			AbstractTaskInstance instance = mapping.get(id);
			if (instance != null) {
				tasks.add(instance);
			}
		}
		return tasks;
	}

	/**
	 * Copy entity to map.
	 * 
	 * @param <K>
	 *            the key type
	 * @param <T>
	 *            the task type
	 * @param <I>
	 *            the instance type
	 * @param list
	 *            the list
	 * @param map
	 *            the map
	 * @param primary
	 *            the primary
	 */
	@SuppressWarnings("unchecked")
	private <K, T extends AbstractTaskInstance, I extends Instance> void copyEntityToMap(
			List<T> list, Map<K, I> map, boolean primary) {
		for (T e : list) {
			if (primary) {
				map.put((K) e.getId(), (I) e);
			} else {
				map.put((K) e.getTaskInstanceId(), (I) e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <S extends Serializable> List<AbstractTaskInstance> loadByDbId(List<S> ids,
			boolean allProperties) {
		return loadInternal(ids, allProperties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, List<DefinitionModel>> getAllowedChildren(AbstractTaskInstance owner) {
		return detectService(owner).getAllowedChildren(owner);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<DefinitionModel> getAllowedChildren(AbstractTaskInstance owner, String type) {
		return detectService(owner).getAllowedChildren(owner, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isChildAllowed(AbstractTaskInstance owner, String type) {
		return detectService(owner).isChildAllowed(owner, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public AbstractTaskInstance cancel(AbstractTaskInstance instance) {
		if (instance instanceof StandaloneTaskInstance) {
			return standaloneTaskService.cancel((StandaloneTaskInstance) instance);
		}
		// no cancel for workflow tasks, yet
		return save(instance, new Operation(ActionTypeConstants.STOP));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public AbstractTaskInstance save(AbstractTaskInstance instance, Operation operation) {
		try {
			if (operation != null) {
				RuntimeConfiguration.setConfiguration(
						RuntimeConfigurationProperties.CURRENT_OPERATION, operation.getOperation());
			}
			return detectService(instance).save(instance, operation);
		} finally {
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void prepareTaskInstance(AbstractTaskInstance instance) {
		DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);
		if (definition instanceof RegionDefinitionModel) {
			instanceDao.populateProperties(instance, (RegionDefinitionModel) definition);
		} else if (definition != null) {
			instanceDao.populateProperties(instance, definition);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean hasUserTasks(Instance instance, String userid, TaskState taskState) {
		if ((userid == null) || !SequenceEntityGenerator.isPersisted(instance)) {
			return false;
		}
		TaskState state = taskState == null ? TaskState.ALL : taskState;
		InstanceReference reference = instance.toReference();
		Triplet<String, Long, TaskState> key = new Triplet<String, Long, TaskState>(
				reference.getIdentifier(), reference.getReferenceType().getId(), state);

		Pair<Triplet<String, Long, TaskState>, Set<String>> pair = getUserHistoryCache().getByKey(
				key);
		if (pair == null) {
			return hasUserTasksInternal(userid, reference, state == TaskState.IN_PROGRESS);
		}
		return pair.getSecond().contains(userid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<String> getOwnedTaskInstances(Instance instance, TaskState taskState, TaskType type) {
		if ((instance == null) || !SequenceEntityGenerator.isPersisted(instance)) {
			return Collections.emptyList();
		}
		TaskState state = taskState == null ? TaskState.ALL : taskState;
		InstanceReference reference = instance.toReference();
		Pair<Quad<String, Long, TaskState, TaskType>, List<String>> pair = getTaskHistoryCache()
				.getByKey(
						new Quad<String, Long, TaskState, TaskType>(reference.getIdentifier(),
								reference.getReferenceType().getId(), state, type));
		if (pair == null) {
			return Collections.emptyList();
		}
		return pair.getSecond();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean hasUserPooledTasks(Instance instance, String userid, TaskState taskState) {
		if ((userid == null) || !SequenceEntityGenerator.isPersisted(instance)) {
			return false;
		}
		TaskState state = taskState == null ? TaskState.ALL : taskState;
		InstanceReference reference = typeConverter.convert(InstanceReference.class, instance);
		Pair<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>> pair = getPoolUserHistoryCache()
				.getByKey(
						new Triplet<String, Long, TaskState>(reference.getIdentifier(), reference
								.getReferenceType().getId(), state));
		Resource user = resourceService.getResource(userid, ResourceType.USER);
		List<Resource> groups = resourceService.getContainingResources(user);
		Pair<Set<String>, Set<String>> assignees = null;
		if (pair == null) {
			List<String> resourceIds = new ArrayList<String>(groups.size() + 1);
			for (Resource resource : groups) {
				resourceIds.add(resource.getIdentifier());
			}
			resourceIds.add(userid);
			List<AssignedUserTasks> userPoolTasksInternal = getUserPoolTasksInternal(reference,
					state == TaskState.IN_PROGRESS);
			assignees = convertToPairAssigness(userPoolTasksInternal);
		} else {
			assignees = pair.getSecond();
		}
		Set<String> poolUsers = assignees.getFirst();
		if (poolUsers.contains(userid)) {
			return true;
		}
		Set<String> groupsPool = assignees.getSecond();
		if (groupsPool != null) {
			Iterator<String> iterator = groupsPool.iterator();
			while (iterator.hasNext()) {
				String groupIdentifier = iterator.next();
				Resource resource = resourceService
						.getResource(groupIdentifier, ResourceType.GROUP);
				if (groups.contains(resource)) {
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * Internal convert list of tasks to its pool representatives
	 *
	 * @param userPoolTasksInternal
	 *            list of tasks
	 * @return pair of users,groups
	 */
	private Pair<Set<String>, Set<String>> convertToPairAssigness(
			List<AssignedUserTasks> userPoolTasksInternal) {
		Pair<Set<String>, Set<String>> assignees = new Pair<Set<String>, Set<String>>(
				new LinkedHashSet<String>(), new LinkedHashSet<String>());
		for (AssignedUserTasks task : userPoolTasksInternal) {
			if (task.getPoolUsers() != null) {
				assignees.getFirst().addAll(task.getPoolUsers());
			}
			if (task.getPoolGroups() != null) {
				assignees.getSecond().addAll(task.getPoolGroups());
			}
		}
		return assignees;
	}

	/**
	 * Checks for user tasks internal.
	 *
	 * @param ref
	 *            the instance ref
	 * @param active
	 *            the active
	 * @return true, if successful
	 */
	private List<AssignedUserTasks> getUserPoolTasksInternal(InstanceReference ref, Boolean active) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(4);
		String queryId = DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_KEY;
		args.add(new Pair<String, Object>(SOURCE_ID, ref.getIdentifier()));
		args.add(new Pair<String, Object>(SOURCE_TYPE, ref.getReferenceType().getId()));
		args.add(new Pair<String, Object>(ACTIVE, active));
		return dbDao.fetchWithNamed(queryId, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Set<String> getUsersWithTasksForInstance(Instance instance, TaskState taskState) {
		if (instance == null) {
			return Collections.emptySet();
		}
		EntityLookupCache<Triplet<String, Long, TaskState>, Set<String>, Serializable> userHistoryCache = getUserHistoryCache();
		TaskState state = taskState == null ? TaskState.ALL : taskState;
		InstanceReference reference = typeConverter.convert(InstanceReference.class, instance);
		Pair<Triplet<String, Long, TaskState>, Set<String>> pair = userHistoryCache
				.getByKey(new Triplet<String, Long, TaskState>(reference.getIdentifier(), reference
						.getReferenceType().getId(), state));
		if (pair != null) {
			return Collections.unmodifiableSet(pair.getSecond());
		}
		return Collections.emptySet();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Pair<Set<String>, Set<String>> getPoolResourcesWithTasksForInstance(Instance instance,
			TaskState taskState) {
		String queryId = null;
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(4);
		if ((taskState == null) || (taskState == TaskState.ALL)) {
			queryId = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT_KEY;
		} else {
			queryId = DbQueryTemplates.POOL_TASKS_FOR_CONTEXT_AND_STATUS_KEY;
			args.add(new Pair<String, Object>(ACTIVE, TaskState.IN_PROGRESS == taskState));
		}
		InstanceReference reference = typeConverter.convert(InstanceReference.class, instance);
		Pair<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>> pair = null; // prepare
																							// cache
		Pair<Set<String>, Set<String>> assignees = null;
		if (pair == null) {

			args.add(new Pair<String, Object>(SOURCE_ID, reference.getIdentifier()));
			args.add(new Pair<String, Object>(SOURCE_TYPE, reference.getReferenceType().getId()));

			List<AssignedUserTasks> userPoolTasksInternal = dbDao.fetchWithNamed(queryId, args);
			assignees = convertToPairAssigness(userPoolTasksInternal);
		}

		return assignees;
	}

	/**
	 * Checks for user tasks internal.
	 *
	 * @param userId
	 *            the user id
	 * @param ref
	 *            the instance ref
	 * @param active
	 *            the active
	 * @return true, if successful
	 */
	private boolean hasUserTasksInternal(String userId, InstanceReference ref, Boolean active) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(3);
		args.add(new Pair<String, Object>("userId", userId));
		args.add(new Pair<String, Object>(SOURCE_ID, ref.getIdentifier()));
		args.add(new Pair<String, Object>(SOURCE_TYPE, ref.getReferenceType().getId()));
		args.add(new Pair<String, Object>(ACTIVE, active));
		List<Long> fetch = dbDao.fetchWithNamed(
				DbQueryTemplates.COUNT_ASSIGNED_TASKS_FOR_CASE_AND_USER_KEY, args);
		return fetch.isEmpty() ? false : fetch.get(0) > 0;
	}

	/**
	 * Adds the given tasks to DB lookup table. <br>
	 * {@inheritDoc}
	 *
	 * @param <T>
	 *            the generic type
	 * @param owningInstance
	 *            the owning instance
	 * @param tasks
	 *            the tasks
	 * @param context
	 *            the context
	 * @param active
	 *            the active
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <T extends AbstractTaskInstance> void attachTaskToInstance(Instance owningInstance,
			List<T> tasks, Instance context, boolean active) {
		if ((tasks == null) || tasks.isEmpty() || (owningInstance == null)) {
			LOGGER.warn("Tasks ({}) are not attached to instance {}", tasks,
					owningInstance == null ? null : owningInstance.toReference());
			return;
		}

		InstanceReference reference = owningInstance.toReference();

		Triplet<String, Long, TaskState> keyInProgress = new Triplet<String, Long, TaskState>(
				reference.getIdentifier(), reference.getReferenceType().getId(),
				TaskState.IN_PROGRESS);
		Triplet<String, Long, TaskState> keyAll = new Triplet<String, Long, TaskState>(
				reference.getIdentifier(), reference.getReferenceType().getId(), TaskState.ALL);
		int count = 0;
		for (AbstractTaskInstance taskInstance : tasks) {
			String userId = null;
			Set<String> poolUsers = null;
			Set<String> poolGroups = null;
			Pair<Set<String>, Set<String>> poolResources = getPoolResources(taskInstance);
			if (poolResources != null) {
				poolGroups = poolResources.getFirst();
				poolUsers = poolResources.getSecond();
			} else {
				userId = (String) taskInstance.getProperties().get(TaskProperties.TASK_OWNER);
			}

			AssignedUserTasks task = new AssignedUserTasks();
			task.setUserId(userId);
			task.setPoolGroups(poolGroups);
			task.setPoolUsers(poolUsers);
			task.setOwningInstance((LinkSourceId) reference);
			task.setTaskInstanceId(taskInstance.getTaskInstanceId());
			if (context != null) {
				task.setContextReference((LinkSourceId) typeConverter.convert(
						InstanceReference.class, context));
			}
			task.setActive(active);
			task.setTaskType(taskInstance.getTaskType());

			// CMF-5458: this is changed to save in new TX due because the data is searched before
			// flushing the transaction and this is a problem because all cache entries are remove
			// from the method: updateTaskStateAndAssignment -> removeFromCache(reference)
			// if removed invoking the method could be changed back to same transaction
			// NOTE: this could potentially lead to stale data if workflow/task persist fail and the
			// user could have permissions for tasks that does not exists
			dbDao.saveOrUpdateInNewTx(task);
			count++;

			updateTaskCache(keyInProgress, task);
			updateTaskCache(keyAll, task);
			// // resets the cache for the given instance
		}
		if (DEBUG) {
			if (context instanceof WorkflowInstanceContext) {
				LOGGER.debug("Saved " + count + " reference tasks for workflow "
						+ ((WorkflowInstanceContext) context).getWorkflowInstanceId()
						+ " and reference " + reference);
			} else {
				LOGGER.debug("Saved " + count + " standalone reference tasks for reference "
						+ reference);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Pair<Set<String>, Set<String>> getPoolResources(AbstractTaskInstance instance) {
		Serializable assignees = instance.getProperties().get(groupAssigneePoolPropertyName);
		Set<String> userIds = null;
		Set<String> groupsIds = null;
		if (assignees instanceof String) {
			groupsIds = new LinkedHashSet<String>();
			groupsIds.add(assignees.toString());
		} else if (assignees instanceof List) {
			throw new EmfRuntimeException("Unsupported pooled task with list of group assignees!");
		}
		assignees = instance.getProperties().get(userAssigneesPoolPropertyName);
		if (assignees instanceof List) {
			userIds = new LinkedHashSet<String>();
			for (String assignee : (List<String>) assignees) {
				Resource resource = resourceService.getResource(assignee, ResourceType.USER);
				userIds.add(resource.getIdentifier());
			}
		}
		assignees = instance.getProperties().get(mixedAssigneesPoolPropertyName);
		if (assignees instanceof List) {
			if (userIds == null) {
				userIds = new LinkedHashSet<String>();
			}
			if (groupsIds == null) {
				groupsIds = new LinkedHashSet<String>();
			}
			for (String assignee : (List<String>) assignees) {
				// TODO fix this code as might be buggy - GROUP_ prefix
				Resource resource = resourceService.getResource(assignee, ResourceType.UNKNOWN);
				if (resource == null) {
					LOGGER.error("Undermined resource type and id: " + assignee);
					continue;
				}
				if (resource.getType() == ResourceType.USER) {
					userIds.add(resource.getIdentifier());
				} else if (resource.getType() == ResourceType.GROUP) {
					groupsIds.add(resource.getIdentifier());
				} else {
					LOGGER.error("Undermined resource type : " + resource);
				}
			}

		}
		// there is at least one entry
		if ((groupsIds != null) || (userIds != null)) {
			return new Pair<Set<String>, Set<String>>(groupsIds, userIds);
		}
		return null;
	}

	/**
	 * Adds the user to cache.
	 *
	 * @param key
	 *            the key
	 * @param task
	 *            the user task to add
	 */
	private void updateTaskCache(Triplet<String, Long, TaskState> key, AssignedUserTasks task) {
		// set the actual users
		if (StringUtils.isNotNullOrEmpty(task.getUserId())) {
			EntityLookupCache<Triplet<String, Long, TaskState>, Set<String>, Serializable> userHistoryCache = getUserHistoryCache();

			Pair<Triplet<String, Long, TaskState>, Set<String>> taskAndAssignees = userHistoryCache
					.getByKey(key);

			Set<String> assignees;
			if (taskAndAssignees != null) {
				assignees = taskAndAssignees.getSecond();
			} else {
				assignees = new LinkedHashSet<String>();
			}
			if (assignees.add(task.getUserId()) && LOGGER.isTraceEnabled()) {
				LOGGER.trace("Updated assignees set with " + task.getUserId()
						+ " with new contents " + assignees + " for key " + key);
			}
			userHistoryCache.setValue(key, assignees);
		}
		// set the pool
		EntityLookupCache<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>, Serializable> poolUserHistoryCache = getPoolUserHistoryCache();
		Pair<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>> taskAndAssigneesPool = poolUserHistoryCache
				.getByKey(key);
		Pair<Set<String>, Set<String>> assigneesPool;
		if (taskAndAssigneesPool != null) {
			assigneesPool = taskAndAssigneesPool.getSecond();
		} else {
			assigneesPool = new Pair<Set<String>, Set<String>>(new LinkedHashSet<String>(),
					new LinkedHashSet<String>());
		}

		if (task.getPoolUsers() != null) {
			assigneesPool.getFirst().addAll(task.getPoolUsers());
		}
		if (task.getPoolGroups() != null) {
			assigneesPool.getSecond().addAll(task.getPoolGroups());
		}

		poolUserHistoryCache.setValue(key, assigneesPool);
		// update the task history cache
		List<String> tasksHistoryValue = null;
		Quad<String, Long, TaskState, TaskType> tasksKey = new Quad<String, Long, TaskState, TaskType>(
				key.getFirst(), key.getSecond(), key.getThird(), task.getTaskType());
		EntityLookupCache<Quad<String, Long, TaskState, TaskType>, List<String>, Serializable> taskHistoryCache = getTaskHistoryCache();
		Pair<Quad<String, Long, TaskState, TaskType>, List<String>> tasksHistoryKey = taskHistoryCache
				.getByKey(tasksKey);
		if (tasksHistoryKey == null) {
			tasksHistoryValue = new LinkedList<String>();
		} else {
			tasksHistoryValue = tasksHistoryKey.getSecond();
		}
		tasksHistoryValue.add(task.getTaskInstanceId());

		taskHistoryCache.setValue(tasksKey, tasksHistoryValue);
	}

	/**
	 * Gets the user history cache.
	 *
	 * @return the user history cache
	 */
	private EntityLookupCache<Triplet<String, Long, TaskState>, Set<String>, Serializable> getUserHistoryCache() {
		return entityCacheContext.getCache(ACTIVE_USERS_ON_CASE);
	}

	/**
	 * Gets the user history cache.
	 *
	 * @return the user history cache
	 */
	private EntityLookupCache<Quad<String, Long, TaskState, TaskType>, List<String>, Serializable> getTaskHistoryCache() {
		return entityCacheContext.getCache(TASK_ON_INSTANCE);
	}

	private EntityLookupCache<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>, Serializable> getPoolUserHistoryCache() {
		return entityCacheContext.getCache(POOL_RESOURCES_ON_CONTEXT);
	}

	/**
	 * Removes the from cache.
	 *
	 * @param ref
	 *            the context instance id
	 */
	private void removeFromCache(InstanceReference ref) {
		Triplet<String, Long, TaskState> key = new Triplet<String, Long, TaskState>(
				ref.getIdentifier(), ref.getReferenceType().getId(), null);
		Quad<String, Long, TaskState, TaskType> keyTask = new Quad<String, Long, TaskState, TaskType>(
				ref.getIdentifier(), ref.getReferenceType().getId(), null, null);
		TaskState[] values = TaskState.values();
		for (int i = 0; i < values.length; i++) {
			key.setThird(values[i]);
			keyTask.setThird(values[i]);
			getUserHistoryCache().removeByKey(key);
			getPoolUserHistoryCache().removeByKey(key);
			TaskType[] types = TaskType.values();
			for (TaskType taskType : types) {
				keyTask.setForth(taskType);
				getTaskHistoryCache().removeByKey(keyTask);
			}
		}

	}

	/**
	 * Removes all tasks for the given context.
	 *
	 * @param context
	 *            the context
	 * @param getExisting
	 *            the get existing
	 * @return the sets the
	 */
	@Override
	public Set<String> removeContextTasks(Instance context, boolean getExisting) {

		if (context == null) {
			return CollectionUtils.emptySet();
		}

		Set<String> old = new HashSet<String>();
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		if (getExisting) {
			old = getContextTasks(context, Boolean.TRUE);
		}

		InstanceReference reference = typeConverter.convert(InstanceReference.class, context);

		args.add(new Pair<String, Object>(SOURCE_ID, reference.getIdentifier()));
		args.add(new Pair<String, Object>(SOURCE_TYPE, reference.getReferenceType().getId()));
		int update = dbDao.executeUpdateInNewTx(
				DbQueryTemplates.DELETE_ASSIGNED_TASKS_BY_WF_ID_KEY, args);
		if (update > 0) {
			if (DEBUG) {
				LOGGER.debug("Removed " + update + " tasks for "
						+ context.getClass().getSimpleName() + " " + context.getId());
			}
		} else if (DEBUG) {
			LOGGER.debug("Nothing to remove for " + context.getClass().getSimpleName() + " "
					+ context.getId());
		}
		if (context instanceof OwnedModel) {
			// reset the user cache
			removeFromCache(((OwnedModel) context).getOwningReference());
		}
		return old;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getContextTasks(Instance context, Boolean active) {
		Set<String> old = new HashSet<String>();
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(3);
		InstanceReference reference = typeConverter.convert(InstanceReference.class, context);
		args.add(new Pair<String, Object>(SOURCE_ID, reference.getIdentifier()));
		args.add(new Pair<String, Object>(SOURCE_TYPE, reference.getReferenceType().getId()));
		String query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT_KEY;
		if (active != null) {
			query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_CONTEXT_AND_STATUS_KEY;
			args.add(new Pair<String, Object>(ACTIVE, active));
		}
		List<String> list = dbDao.fetchWithNamed(query, args);
		if (!list.isEmpty()) {
			old.addAll(list);
		}
		return old;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateTaskStateAndAssignment(Instance owningInstance, Instance context,
			AbstractTaskInstance instance, Boolean active) {
		Serializable owner = instance.getProperties().get(TaskProperties.TASK_OWNER);

		InstanceReference reference = owningInstance.toReference();
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(7);
		args.add(new Pair<String, Object>(SOURCE_ID, reference.getIdentifier()));
		args.add(new Pair<String, Object>(SOURCE_TYPE, reference.getReferenceType().getId()));
		args.add(new Pair<String, Object>("taskInstanceId", instance.getTaskInstanceId()));
		args.add(new Pair<String, Object>("userId", owner));
		args.add(new Pair<String, Object>(ACTIVE, active));
		InstanceReference contextReference = context.toReference();
		args.add(new Pair<String, Object>("contextSourceId", contextReference.getIdentifier()));
		args.add(new Pair<String, Object>("contextSourceType", contextReference.getReferenceType()
				.getId()));
		int update = dbDao.executeUpdate(DbQueryTemplates.UPDATE_ASSIGNED_TASK_STATUS_KEY, args);
		// remove the task to be updated automatically with newest state
		if (update != 1) {
			if (DEBUG) {
				LOGGER.debug("Failed to update task owner and state for task "
						+ instance.getTaskInstanceId()
						+ (context instanceof WorkflowInstanceContext ? " from workflow "
								+ ((WorkflowInstanceContext) context).getWorkflowInstanceId() : ""));
			}
		} else {
			// reset the user cache
			removeFromCache(reference);
		}
	}

	@Override
	public <T extends AbstractTaskInstance> void dettachTaskFromInstance(Instance owningInstance,
			List<T> tasks) {

		if ((owningInstance == null) || (tasks == null) || tasks.isEmpty()) {
			return;
		}

		List<String> taskIds = new ArrayList<>(tasks.size());
		for (T t : tasks) {
			taskIds.add(t.getTaskInstanceId());
		}
		List<Pair<String, Object>> params = new ArrayList<>(3);
		params.add(new Pair<String, Object>("taskInstanceId", taskIds));
		params.add(new Pair<String, Object>(SOURCE_ID, owningInstance.toReference()
				.getIdentifier()));
		params.add(new Pair<String, Object>(SOURCE_TYPE, owningInstance.toReference()
				.getReferenceType().getId()));
		int updated = dbDao.executeUpdate(DbQueryTemplates.REMOVE_TASK_FROM_CONTEXT_KEY, params);
		LOGGER.debug("Removed " + updated + " tasks from context "
				+ owningInstance.toReference().getReferenceType().getName() + " with id="
				+ owningInstance.toReference().getIdentifier());
	}

	/**
	 * Detect service based on the given instance.
	 *
	 * @param <I>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param object
	 *            the object
	 * @return the instance service
	 */
	@SuppressWarnings("unchecked")
	private <I extends AbstractTaskInstance, D extends DefinitionModel> InstanceService<I, D> detectService(
			Object object) {
		if ((object instanceof StandaloneTaskInstance) || (object instanceof TaskDefinition)) {
			return (InstanceService<I, D>) standaloneTaskService;
		}
		return (InstanceService<I, D>) workflowTaskService;
	}

	/**
	 * Checks if is claimable. Task is claimable if user is contained in the pool and the other
	 * constraints are fulfilled
	 *
	 * @param instance
	 *            is the task to check
	 * @param userId
	 *            the user to check
	 * @return true, if is claimable
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isClaimable(AbstractTaskInstance instance, String userId) {

		// if the task is complete it is not claimable
		if (instance.getState() == TaskState.COMPLETED) {
			return false;
		}

		// if the task has an owner it can not be claimed
		if (instance.getProperties().get(TaskProperties.TASK_OWNER) != null) {
			return false;
		}
		// a task can only be claimed if the user is a member of
		// of the pooled actors for the task
		return isUserInPooledActors(instance, userId);
	}

	/**
	 * Checks if is releaseable. Task is releaseable if the pool is not empty, user is current owner
	 * or creator of task
	 *
	 * @param instance
	 *            is the task to check
	 * @param userId
	 *            the user to check
	 * @return true, if is releaseable
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isReleasable(AbstractTaskInstance instance, String userId) {

		// if the task is complete it is not releasable
		if (instance.getState() == TaskState.COMPLETED) {
			return false;
		}

		// if the task doesn't have pooled actors it is not releasable
		Collection<String> actors = getPoolUsers(instance);
		if ((actors == null) || actors.isEmpty()) {
			return false;
		}

		// if the task does not have an owner it is not releasable
		String owner = (String) instance.getProperties().get(TaskProperties.TASK_OWNER);
		if (owner == null) {
			return false;
		}

		if (isUserOwnerOrInitiator(instance, owner, userId)) {
			// releasable if the current user is the task owner or initiator
			return true;
		}

		return false;
	}

	@Override
	public Collection<String> getPoolUsers(AbstractTaskInstance instance) {
		Pair<Set<String>, Set<String>> poolResources = getPoolResources(instance);
		if (poolResources != null) {
			Set<String> users = poolResources.getSecond();
			Set<String> poolUsersForTask = new LinkedHashSet<>();
			if (users != null) {
				poolUsersForTask.addAll(poolResources.getSecond());
			}
			Set<String> groups = poolResources.getFirst();
			if (groups != null) {
				Iterator<String> iterator = groups.iterator();
				while (iterator.hasNext()) {
					String groupId = iterator.next();
					Resource group = resourceService.getResource(groupId, ResourceType.GROUP);
					List<String> usersInAuthority = resourceService
							.getContainedResourceIdentifiers(group);
					poolUsersForTask.addAll(usersInAuthority);
				}

			}
			return poolUsersForTask;
		}
		return null;
	}

	/**
	 * Checks if is pooled task.
	 *
	 * @param instance
	 *            is the task to check
	 * @return true, if is pooled task
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean isPooledTask(AbstractTaskInstance instance) {
		return (instance.getProperties().get(TaskProperties.TASK_OWNER) == null)
				&& (getPoolResources(instance) != null);
	}

	/**
	 * Determines if the given user is the owner of the given task or the initiator of the workflow
	 * the task is part of.
	 *
	 * @param instance
	 *            is the task to check
	 * @param owner
	 *            the current owner
	 * @param userId
	 *            the user id
	 * @return true if the user is the owner or the workflow initiator
	 */
	private boolean isUserOwnerOrInitiator(AbstractTaskInstance instance, String owner,
			String userId) {
		boolean result = false;

		if (userId.equals(owner)) {
			// user owns the task
			result = true;
		} else if (userId.equals(instance.getProperties().get(TaskProperties.START_BY))) {
			// user is the workflow initiator
			result = true;
		}
		return result;
	}

	/**
	 * Checks if is user in pooled actors.
	 *
	 * @param instance
	 *            is the task to check
	 * @param userId
	 *            the user id
	 * @return true, if is user in pooled actors
	 */
	private boolean isUserInPooledActors(AbstractTaskInstance instance, String userId) {
		Collection<String> users = getPoolUsers(instance);
		return users != null ? users.contains(userId) : false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <A extends AbstractTaskInstance> List<A> getSubTasks(AbstractTaskInstance currentInstance) {
		List<Serializable> list = findSubtasks(currentInstance, TaskState.ALL, true);
		return (List<A>) detectService(currentInstance).loadByDbId(list);
	}

	/**
	 * Find subtasks.
	 * 
	 * @param currentInstance
	 *            the current instance
	 * @param state
	 *            the state
	 * @param includeCurrent
	 *            the include current
	 * @return the list
	 */
	private List<Serializable> findSubtasks(AbstractTaskInstance currentInstance, TaskState state,
			boolean includeCurrent) {
		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(2);
		args.add(new Pair<String, Object>("treePath", "%"
				+ InstanceUtil.buildPath(currentInstance, InstanceContext.class)));
		Serializable id = currentInstance.getId();
		if (includeCurrent) {
			id = "anyId";
		}
		args.add(new Pair<String, Object>("id", id));
		args.add(new Pair<String, Object>("taskType", currentInstance.getTaskType()));
		String query = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH_KEY;
		if ((state != null) && (state != TaskState.ALL)) {
			args.add(new Pair<String, Object>("state", state));
			query = DbQueryTemplates.QUERY_TASK_BY_PARENT_PATH_AND_STATE_KEY;
		}
		return dbDao.fetchWithNamed(query, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <A extends AbstractTaskInstance> List<A> getSubTasks(
			AbstractTaskInstance currentInstance, TaskState state, boolean includeCurrent) {
		List<Serializable> list = findSubtasks(currentInstance, state, includeCurrent);
		return (List<A>) detectService(currentInstance).loadByDbId(list);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public boolean hasSubTasks(AbstractTaskInstance currentInstance, TaskState state) {
		List<Serializable> subtasks = findSubtasks(currentInstance, state, false);
		return !subtasks.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable logWork(InstanceReference task, String userId,
			Map<String, Serializable> loggedData) {
		if ((task == null) || (userId == null) || (loggedData == null)) {
			LOGGER.warn("Missing required input data to log work on task: taskRef=" + task
					+ ", user=" + userId + ", data=" + loggedData);
			return null;
		}

		Resource resource = resourceService.getResource(userId, ResourceType.USER);
		if (resource == null) {
			LOGGER.warn("No user found in the system with name=" + userId);
			return null;
		}

		RuntimeConfiguration
				.enable(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.GENERATE_RANDOM_LINK_ID);
		try {
			Pair<Serializable, Serializable> pair = linkService.link(task,
					typeConverter.convert(InstanceReference.class, resource),
					LinkConstantsCmf.LOGGED_WORK, null, loggedData);
			if (pair.getFirst() != null) {
				eventService.fire(new AddedWorkLogEntryEvent(task, pair.getFirst(), userId,
						loggedData));
			}
			return pair.getFirst();
		} finally {
			RuntimeConfiguration
					.disable(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.GENERATE_RANDOM_LINK_ID);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Secure
	public boolean updateLoggedWork(Serializable id, Map<String, Serializable> loggedData) {
		if (id == null) {
			LOGGER.warn("The id is required parameter to update the logged data.");
			return false;
		}
		if ((loggedData == null) || loggedData.isEmpty()) {
			LOGGER.warn("No data to update was provided for id=" + id);
			return false;
		}
		RuntimeConfiguration.setConfiguration(
				RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION, Boolean.TRUE);
		try {
			LinkReference linkReference = linkService.getLinkReference(id);
			if (linkReference == null) {
				LOGGER.warn("The work log entry with id=" + id + " does not exists!");
				return false;
			}
			String originalUserId = linkReference.getTo().getIdentifier();
			Map<String, Serializable> oldLoggedData = linkReference.getProperties();

			if (linkService.updateLinkProperties(id, loggedData)) {
				// fire event for the executed operation
				String updatingUserId = SecurityContextManager.getFullAuthentication()
						.getIdentifier();
				eventService.fire(new UpdatedWorkLogEntryEvent(linkReference.getFrom(), id,
						updatingUserId, originalUserId, loggedData, oldLoggedData));
				return true;
			}
		} finally {
			RuntimeConfiguration
					.clearConfiguration(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION);
		}
		LOGGER.warn("Failed to update logged work data for entry with id=" + id);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean deleteLoggedWork(Serializable id) {
		if (id == null) {
			LOGGER.warn("The id is required parameter to remove the logged data.");
			return false;
		}
		LinkReference linkReference = linkService.getLinkReference(id);
		if (linkReference == null) {
			LOGGER.warn("No work log entry with id=" + id + " found for deletion!");
			return false;
		}
		linkService.removeLinkById(id);
		// notify for deletion
		eventService.fire(new DeleteWorkLogEntryEvent(linkReference.getFrom(), id, linkReference
				.getTo().getIdentifier(), linkReference.getProperties()));
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public List<WorkLogEntry> getLoggedData(AbstractTaskInstance instance) {
		if (instance == null) {
			return Collections.emptyList();
		}
		RuntimeConfiguration.enable(RuntimeConfigurationProperties.LOAD_LINK_PROPERTIES);
		List<LinkReference> references;
		try {
			references = linkService.getLinks(instance.toReference(), LinkConstantsCmf.LOGGED_WORK);
		} finally {
			RuntimeConfiguration.disable(RuntimeConfigurationProperties.LOAD_LINK_PROPERTIES);
		}
		List<WorkLogEntry> workLogEntries = new ArrayList<>(references.size());
		for (LinkReference linkInstance : references) {
			WorkLogEntry entry = new WorkLogEntry();
			entry.setId(linkInstance.getId());
			String userDbId = linkInstance.getTo().getIdentifier();
			Resource resource = resourceService.loadByDbId(userDbId);
			if (resource != null) {
				entry.setUser(resource.getIdentifier());
				entry.setUserDisplayName(resource.getDisplayName());
			} else {
				entry.setUser("unknow.user");
				entry.setUserDisplayName("Unknow user");
			}
			entry.setProperties(linkInstance.getProperties());
			workLogEntries.add(entry);
		}
		return workLogEntries;
	}

	/**
	 * Lookup entity for fetching users for owning instances and state.
	 *
	 * @author BBonev
	 */
	public class ActiveUsersOnCaseLookup
			extends
			EntityLookupCallbackDAOAdaptor<Triplet<String, Long, TaskState>, Set<String>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Triplet<String, Long, TaskState>, Set<String>> findByKey(
				Triplet<String, Long, TaskState> key) {
			List<String> fetch;
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
			args.add(new Pair<String, Object>(SOURCE_ID, key.getFirst()));
			args.add(new Pair<String, Object>(SOURCE_TYPE, key.getSecond()));
			if (key.getThird() == TaskState.ALL) {
				fetch = dbDao.fetchWithNamed(DbQueryTemplates.QUERY_USERS_ON_INSTANCE_KEY, args);
			} else {
				args.add(new Pair<String, Object>(ACTIVE, key.getThird() == TaskState.IN_PROGRESS));
				fetch = dbDao.fetchWithNamed(
						DbQueryTemplates.QUERY_USERS_ON_INSTANCE_AND_STATUS_KEY, args);
			}

			return new Pair<Triplet<String, Long, TaskState>, Set<String>>(key,
					new LinkedHashSet<String>(fetch));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Triplet<String, Long, TaskState>, Set<String>> createValue(Set<String> value) {
			throw new UnsupportedOperationException(THE_LIST_IS_PERSISTED_EXTERNALLY);
		}

	}

	/**
	 * Lookup for task started on instances. Returns a list of task keyed by the {@link Quad} param
	 *
	 * @author bbanchev
	 */
	public class TaskOnContextLookup
			extends
			EntityLookupCallbackDAOAdaptor<Quad<String, Long, TaskState, TaskType>, List<String>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Quad<String, Long, TaskState, TaskType>, List<String>> findByKey(
				Quad<String, Long, TaskState, TaskType> key) {
			List<String> fetch;

			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(4);
			args.add(new Pair<String, Object>(SOURCE_ID, key.getFirst()));
			args.add(new Pair<String, Object>(SOURCE_TYPE, key.getSecond()));
			List<TaskType> types = new ArrayList<TaskType>(2);
			TaskType type = key.getForth();
			if (type == null) {
				types.add(TaskType.WORKFLOW_TASK);
				types.add(TaskType.STANDALONE_TASK);
			} else {
				types.add(type);
			}
			args.add(new Pair<String, Object>("taskType", types));
			String query = null;

			if (key.getThird() == TaskState.ALL) {
				query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_KEY;
				fetch = dbDao.fetchWithNamed(query, args);
			} else {
				args.add(new Pair<String, Object>(ACTIVE, key.getThird() == TaskState.IN_PROGRESS));
				query = DbQueryTemplates.QUERY_ASSIGNED_TASKS_BY_OWNING_INSTANCE_AND_STATUS_KEY;
			}
			fetch = dbDao.fetchWithNamed(query, args);
			return new Pair<Quad<String, Long, TaskState, TaskType>, List<String>>(key,
					new LinkedList<String>(fetch));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Quad<String, Long, TaskState, TaskType>, List<String>> createValue(
				List<String> value) {
			throw new UnsupportedOperationException(THE_LIST_IS_PERSISTED_EXTERNALLY);
		}

	}

	/**
	 * Lookup entity for fetching users for owning instances and state.
	 *
	 * @author BBonev
	 */
	public class PoolResourcesOnUnassignedContextLookup
			extends
			EntityLookupCallbackDAOAdaptor<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>> findByKey(
				Triplet<String, Long, TaskState> key) {
			List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(1);
			args.add(new Pair<String, Object>(SOURCE_ID, key.getFirst()));
			args.add(new Pair<String, Object>(SOURCE_TYPE, key.getSecond()));
			List<AssignedUserTasks> fetch;
			if (key.getThird() == TaskState.ALL) {
				fetch = dbDao
						.fetchWithNamed(DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_KEY, args);
			} else {
				args.add(new Pair<String, Object>(ACTIVE, key.getThird() == TaskState.IN_PROGRESS));
				fetch = dbDao.fetchWithNamed(
						DbQueryTemplates.UNSSIGNED_TASKS_FOR_CONTEXT_AND_STATUS_KEY, args);
			}
			return new Pair<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>>(key,
					convertToPairAssigness(fetch));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Pair<Triplet<String, Long, TaskState>, Pair<Set<String>, Set<String>>> createValue(
				Pair<Set<String>, Set<String>> value) {
			throw new UnsupportedOperationException(THE_LIST_IS_PERSISTED_EXTERNALLY);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractTaskInstance clone(AbstractTaskInstance instance, Operation operation) {
		return detectService(instance).clone(instance, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(AbstractTaskInstance instance, Operation operation, boolean permanent) {
		detectService(instance).delete(instance, operation, permanent);
	}

	@Override
	public void attach(AbstractTaskInstance targetInstance, Operation operation,
			Instance... children) {
		// task does not support attaching for now
	}

	@Override
	public void detach(AbstractTaskInstance sourceInstance, Operation operation,
			Instance... instances) {
		// no detaching eigther
	}

}
