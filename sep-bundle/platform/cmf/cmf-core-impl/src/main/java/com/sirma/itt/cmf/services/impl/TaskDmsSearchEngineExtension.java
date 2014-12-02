package com.sirma.itt.cmf.services.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.search.DmsSearchEngineExtension;
import com.sirma.itt.cmf.search.SearchType;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.services.WorkflowTaskService;
import com.sirma.itt.cmf.services.adapter.CMFWorkflowAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.exceptions.SearchException;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.event.SearchEventBinding;
import com.sirma.itt.emf.search.event.SearchEventObject;
import com.sirma.itt.emf.search.event.SearchEventType;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.PropertyModelComparator;

/**
 * Extension implementation for {@link com.sirma.itt.cmf.search.DmsSearchEngine} that handles
 * workflow and standalone tasks searches in DMS.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DmsSearchEngineExtension.TARGET_NAME, order = 20)
public class TaskDmsSearchEngineExtension implements DmsSearchEngineExtension {
	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			TaskInstance.class, AbstractTaskInstance.class, StandaloneTaskInstance.class));
	/** The logger. */
	private static Logger logger = Logger.getLogger(TaskDmsSearchEngineExtension.class);

	/** The trace enabled. */
	private boolean traceEnabled = false;
	/** The debug enabled. */
	private boolean debugEnabled = false;
	/** The authentication service. */
	@Inject
	private Instance<AuthenticationService> authenticationService;

	/** The adapter service. */
	@Inject
	private CMFWorkflowAdapterService adapterService;

	/** The workflow service. */
	@Inject
	private WorkflowService workflowService;

	/** The search event. */
	@Inject
	@Any
	private Event<SearchEventObject> searchEvent;

	/** The case instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.CASE)
	private InstanceDao<CaseInstance> caseInstanceDao;

	/** The standalone task service. */
	@Inject
	private StandaloneTaskService standaloneTaskService;

	/** The workflow task service. */
	@Inject
	private WorkflowTaskService workflowTaskService;

	/** The max results. */
	@Inject
	@Config(name = EmfConfigurationProperties.SEARCH_RESULT_MAXSIZE)
	private Integer maxResults;

	/** The excluded workflows aspects. */
	private final TreeSet<String> excludedWorkflows = new TreeSet<String>();

	/**
	 * Instantiates a new workflow search service impl.
	 */
	@PostConstruct
	public void initialize() {
		excludedWorkflows.add(WorkflowProperties.ARCHIVED_WORKFLOW_TASKS);
		traceEnabled = logger.isTraceEnabled();
		debugEnabled = logger.isDebugEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Secure
	public <E extends com.sirma.itt.emf.instance.model.Instance, S extends SearchArguments<E>> void search(
			S args) {
		TimeTracker tracker = new TimeTracker();
		tracker.begin();

		// fire events for security purposes
		fireSearchEvent(SearchEventType.BEFORE_SEARCH, SearchType.TASK, args);

		StringBuilder debugMsg = new StringBuilder();
		int returnSize = 0;
		try {
			// uncomment to enable site filtering
			// args.setQuery(args.getQuery().and(WorkflowProperties.PARENT_CONTAINER,
			// getCurrentContainer()));

			Query query;
			if (args.getQuery() == null) {
				Map<String, Serializable> map = args.getArguments();
				query = Query.fromMap(map, QueryBoost.INCLUDE_AND);
			} else {
				query = args.getQuery();
				// if someone added additional arguments to already builded
				// query or if the event builded query and we need to add the
				// arguments to it
				if (!args.getArguments().isEmpty()) {
					query = query.and(Query.fromMap(args.getArguments(), QueryBoost.INCLUDE_AND));
				}
			}
			query = query.andNot(CommonProperties.KEY_SEARCHED_ASPECT, excludedWorkflows);
			args.setQuery(query);
			tracker.begin();
			SearchArguments<Pair<String, String>> queryArgs = new SearchArguments<Pair<String, String>>();
			queryArgs.setQuery(args.getQuery());
			queryArgs.setPageNumber(args.getPageNumber());
			queryArgs.setPageSize(args.getPageSize());
			queryArgs.setSkipCount(args.getSkipCount());
			queryArgs.setTotalItems(args.getTotalItems());
			queryArgs.setSorter(args.getSorter());
			queryArgs.setMaxSize(maxResults);
			// add the tenant
			queryArgs.setContext(getCurrentTenant());
			SearchArguments<Pair<String, String>> light = adapterService
					.searchTasksLight(queryArgs);
			// SearchArguments<TaskInstance> tasks = adapterService.searchTasks(args);
			if (debugEnabled) {
				debugMsg.append("\nTask search in DMS took ").append(tracker.stopInSeconds())
						.append(" s");
			}
			tracker.begin();
			// List<TaskInstance> dmsResult = tasks.getResult();
			List<Pair<String, String>> dmsResult = light.getResult();
			if ((dmsResult == null) || dmsResult.isEmpty()) {
				args.setResult(Collections.EMPTY_LIST);
				return;
			}
			if (debugEnabled) {
				debugMsg.append("\nDMS response (").append(dmsResult.size()).append(") results");
			}

			List<String> standaloneTasksToLoad = new LinkedList<String>();
			Map<String, List<String>> tasksForWorkflow = new LinkedHashMap<String, List<String>>(
					(int) (dmsResult.size() * 1.1), 0.95f);
			List<String> tasksToLoad = new LinkedList<String>();
			// group all task based on wf

			for (Pair<String, String> pair : dmsResult) {
				if (pair.getSecond() == null) {
					standaloneTasksToLoad.add(pair.getFirst());
				} else {
					String workflowInstanceId = pair.getSecond();
					if (!tasksForWorkflow.containsKey(workflowInstanceId)) {
						tasksForWorkflow.put(workflowInstanceId, new ArrayList<String>(5));
					}
					tasksForWorkflow.get(workflowInstanceId).add(pair.getFirst());
					tasksToLoad.add(pair.getFirst());
				}
			}

			Set<String> keySet = tasksForWorkflow.keySet();
			List<WorkflowInstanceContext> contexts = workflowService
					.loadContexts(new LinkedList<String>(keySet));

			Map<String, List<WorkflowInstanceContext>> caseWf = new LinkedHashMap<String, List<WorkflowInstanceContext>>(
					(int) (contexts.size() * 1.2), 0.95f);

			List<StandaloneTaskInstance> standaloneTasks = standaloneTaskService.load(
					standaloneTasksToLoad, true);
			Map<String, AbstractTaskInstance> resultModel = new LinkedHashMap<String, AbstractTaskInstance>(
					(dmsResult.size()));

			// for (StandaloneTaskInstance standaloneTaskInstance : standaloneTasks) {
			// resultModel.put(standaloneTaskInstance.getTaskInstanceId(), standaloneTaskInstance);
			// }

			List<TaskInstance> workflowTasks = workflowTaskService.load(tasksToLoad, true);
			for (TaskInstance taskInstance : workflowTasks) {
				resultModel.put(taskInstance.getTaskInstanceId(), taskInstance);
			}

			List<AbstractTaskInstance> filtered = new ArrayList<AbstractTaskInstance>(
					standaloneTasks.size() + workflowTasks.size());
			filtered.addAll(standaloneTasks);
			filtered.addAll(workflowTasks);

			for (WorkflowInstanceContext workflowInstanceContext : contexts) {
				List<String> taskInstance = tasksForWorkflow.get(workflowInstanceContext
						.getWorkflowInstanceId());
				for (String taskId : taskInstance) {
					AbstractTaskInstance currentTask = resultModel.get(taskId);
					if (currentTask == null) {
						continue;
					}
					currentTask.setRevision(workflowInstanceContext.getRevision());

					// BUGFIX - now properties are in emf and should not be filtered
					// // first filter/convert properties
					// // then we set the context instance - that way we will add
					// // all properties of the WF to the task
					// //
					// currentTask.setProperties(adapterService.filterTaskProperties(currentTask));
					if (currentTask instanceof TaskInstance) {
						((TaskInstance) currentTask).setContext(workflowInstanceContext);
						((TaskInstance) currentTask).setOwningInstance(workflowInstanceContext);
					}
					// group all tasks by case ID so we can later set the proper
					// case instance to each one of them
					Class<?> parentClass = TypeConverterUtil.getConverter().convert(
							Class.class,
							workflowInstanceContext.getOwningReference().getReferenceType()
									.getJavaClassName());
					if (CaseInstance.class.equals(parentClass)) {
						CollectionUtils.addValueToMap(caseWf, workflowInstanceContext
								.getOwningReference().getIdentifier(), workflowInstanceContext);
					}
				}
			}

			// TODO: loading of the workflow owning instances should happen for all objects, not
			// only for case
			// set case instances to each task
			// TODO: added partial loading of the case here
			List<CaseInstance> cases = caseInstanceDao.loadInstancesByDbKey(new ArrayList<String>(
					caseWf.keySet()));
			for (CaseInstance caseInstance : cases) {
				List<WorkflowInstanceContext> list = caseWf.get(caseInstance.getId());
				for (WorkflowInstanceContext context : list) {
					context.setOwningInstance(caseInstance);
				}
			}

			// sort the
			if (args.isOrdered()) {
				Sorter sorter = args.getSorter();
				if (sorter != null) {
					Collections.sort(
							filtered,
							new PropertyModelComparator(sorter.isAscendingOrder(), sorter
									.getSortField()));
				}
			}

			args.setResult((List<E>) filtered);
			args.setTotalItems(light.getTotalItems());

			fireSearchEvent(SearchEventType.AFTER_SEARCH, SearchType.TASK, args);

			returnSize = filtered.size();
			// update task Info
		} catch (DMSException e) {
			throw new SearchException("Cannot perform task search: " + e.getMessage(), e);
		} finally {
			if (debugEnabled) {
				debugMsg.append("\nCMF DB retrieve took ").append(tracker.stopInSeconds())
						.append(" s").append("\nTotal search time for (").append(returnSize)
						.append(") results : ").append(tracker.stopInSeconds()).append(" s");
				logger.debug(debugMsg);
			}
		}
	}

	/**
	 * Gets the current user.
	 * 
	 * @return the current user or null if authentication service could not retrieve it.
	 */
	private User getCurrentUser() {
		if (authenticationService.isUnsatisfied() || authenticationService.isAmbiguous()) {
			return SecurityContextManager.getFullAuthentication();
		}
		try {
			return authenticationService.get().getCurrentUser();
		} catch (ContextNotActiveException e) {
			// logger.debug("No context for fetching current user due to " + e.getMessage());
			return SecurityContextManager.getFullAuthentication();
		}
	}

	/**
	 * Gets the current tenant or <code>null</code> if not user is logged in.
	 * 
	 * @return the current tenant
	 */
	private String getCurrentTenant() {
		User currentUser = getCurrentUser();
		if (currentUser == null) {
			// if called from web service where we does not have a context, and user is set using
			// the runAsSystem/Admin we could return that user
			currentUser = SecurityContextManager.getFullAuthentication();
		}
		if (currentUser != null) {
			return currentUser.getTenantId();
		}
		return null;
	}

	/**
	 * Fire search event.
	 * 
	 * @param eventType
	 *            the event type
	 * @param searchType
	 *            the search type
	 * @param arguments
	 *            the arguments
	 */
	protected void fireSearchEvent(SearchEventType eventType, String searchType, Object arguments) {
		TimeTracker tracker = new TimeTracker().begin();
		searchEvent.select(new SearchEventBinding(searchType, eventType)).fire(
				new SearchEventObject(arguments));

		if (traceEnabled) {
			logger.trace("Event handling took: " + tracker.stopInSeconds() + " s on " + eventType
					+ " for " + searchType);
		}
	}

}