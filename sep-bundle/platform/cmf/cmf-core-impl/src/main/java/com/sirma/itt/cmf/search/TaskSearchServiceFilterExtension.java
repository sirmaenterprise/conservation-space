package com.sirma.itt.cmf.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.SearchFilterProperties;
import com.sirma.itt.emf.search.SearchServiceFilterExtension;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.DateRange;

/**
 * Filter extension for workflow and standalone tasks.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = SearchServiceFilterExtension.TARGET_NAME, priority = 20)
public class TaskSearchServiceFilterExtension implements
 SearchServiceFilterExtension {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			TaskInstance.class, AbstractTaskInstance.class, StandaloneTaskInstance.class));

	/** The authentication service. */
	@Inject
	private Instance<AuthenticationService> authenticationService;

	/** The resource service. */
	@Inject
	private ResourceService resourceService;

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
	@Override
	@SuppressWarnings("unchecked")
	public <S extends SearchArguments<?>> S buildSearchArguments(String filterName,
			Context<String, Object> context) {
		if ("getBaseTaskFilter".equals(filterName)) {
			return (S) getBaseTaskFilter();
		}
		if (context != null) {
			Boolean includeOwner = context.getIfSameType(SearchFilterProperties.INCLUDE_OWNER, Boolean.class, Boolean.FALSE);
			Serializable instance = context.getIfSameType(SearchFilterProperties.INSTANCE_CONTEXT, Serializable.class);
			String contextKey = context.getIfSameType(SearchFilterProperties.INSTANCE_CONTEXT_KEY, String.class);

			if ("getOpenTaskFilter".equals(filterName)) {
				return addContextQuery((S) getOpenTaskFilter(includeOwner), instance, contextKey);
			} else if ("getHighPriorityTaskFilter".equals(filterName)) {
				return addContextQuery((S) getHighPriorityTaskFilter(includeOwner), instance, contextKey);
			} else if ("getDueDateTodayTaskFilter".equals(filterName)) {
				return addContextQuery((S) getDueDateTodayTaskFilter(includeOwner), instance, contextKey);
			} else if ("getOverdueDateTaskFilter".equals(filterName)) {
				return addContextQuery((S) getOverdueDateTaskFilter(includeOwner), instance, contextKey);
			} else if ("getPoolableTaskFilter".equals(filterName)) {
				return addContextQuery((S) getPoolableTaskFilter(includeOwner), instance, contextKey);
			} else if ("getAllTasksFilter".equals(filterName)) {
				return addContextQuery((S) getAllTasksFilter(includeOwner), instance, contextKey);
			}
		}
		return null;
	}

	@Override
	public SearchFilterConfig getFilterConfiguration(String placeHolder) {
		return new SearchFilterConfig(new LinkedList<SearchFilter>(),
				new LinkedList<SearchFilter>());
	}

	/**
	 * Adds the context query.
	 *
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @param context
	 *            the context
	 * @param contextKey
	 *            the context key
	 * @return the s
	 */
	public <S extends SearchArguments<?>> S addContextQuery(S arguments,
			Serializable context, String contextKey) {
		if ((contextKey != null) && (context != null)) {
			Query finalQuery = new Query(contextKey, context, true).and(arguments.getQuery()).end();
			arguments.setQuery(finalQuery);
		}
		return arguments;
	}

	/**
	 * Gets the open task filter.
	 * 
	 * @param includeOwner
	 *            the include owner
	 * @return the open task filter
	 */
	public SearchArguments<AbstractTaskInstance> getOpenTaskFilter(boolean includeOwner) {
		SearchArguments<AbstractTaskInstance> args = new SearchArguments<AbstractTaskInstance>();
		Map<String, Serializable> arguments = getBaseSearchArgument();
		arguments.put(TaskProperties.SEARCH_STATE, TaskState.IN_PROGRESS.toString());
		User currentUser = getCurrentUser();
		if (includeOwner && (currentUser != null)) {
			arguments.put(TaskProperties.TASK_OWNER, currentUser.getIdentifier());
		}

		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		args.setQuery(query);

		args.setSorter(new Sorter(TaskProperties.ACTUAL_START_DATE,
				Sorter.SORT_DESCENDING));
		args.setOrdered(true);
		return args;
	}

	/**
	 * Gets the high priority task filter.
	 * 
	 * @param includeOwner
	 *            the include owner
	 * @return the high priority task filter
	 */
	public SearchArguments<AbstractTaskInstance> getHighPriorityTaskFilter(boolean includeOwner) {
		SearchArguments<AbstractTaskInstance> args = new SearchArguments<AbstractTaskInstance>();

		Map<String, Serializable> arguments = getBaseSearchArgument();
		arguments.put(TaskProperties.SEARCH_STATE, TaskState.IN_PROGRESS.toString());
		// 1 is the highest.
		arguments.put(TaskProperties.SEARCH_PRIORITY, Integer.valueOf(1));
		User currentUser = getCurrentUser();
		if (includeOwner && (currentUser != null)) {
			arguments.put(TaskProperties.TASK_OWNER, currentUser.getIdentifier());
		}

		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		args.setQuery(query);

		args.setSorter(new Sorter(TaskProperties.ACTUAL_START_DATE,
				Sorter.SORT_DESCENDING));
		args.setOrdered(true);
		return args;
	}

	/**
	 * Gets the due date today task filter.
	 *
	 * @param includeOwner
	 *            the include owner
	 * @return the due date today task filter
	 */
	public SearchArguments<AbstractTaskInstance> getDueDateTodayTaskFilter(boolean includeOwner) {
		SearchArguments<AbstractTaskInstance> args = new SearchArguments<AbstractTaskInstance>();

		Map<String, Serializable> arguments = getBaseSearchArgument();
		arguments.put(TaskProperties.SEARCH_STATE, TaskState.IN_PROGRESS.toString());
		Calendar dueToDate = Calendar.getInstance();
		long currentTimeMillis = System.currentTimeMillis();
		dueToDate.setTimeInMillis(currentTimeMillis);
		dueToDate.set(Calendar.HOUR_OF_DAY, 23);
		dueToDate.set(Calendar.MINUTE, 59);
		dueToDate.set(Calendar.SECOND, 59);
		dueToDate.clear(Calendar.MILLISECOND);
		Calendar dueFromDate = Calendar.getInstance();
		dueFromDate.setTimeInMillis(currentTimeMillis);
		dueFromDate.set(Calendar.HOUR_OF_DAY, 0);
		dueFromDate.set(Calendar.MINUTE, 0);
		dueFromDate.set(Calendar.SECOND, 0);
		dueFromDate.clear(Calendar.MILLISECOND);
		arguments.put(TaskProperties.PLANNED_END_DATE, new DateRange(dueFromDate.getTime(),
				dueToDate.getTime()));
		User currentUser = getCurrentUser();
		if (includeOwner && (currentUser != null)) {
			arguments.put(TaskProperties.TASK_OWNER, currentUser.getIdentifier());
		}

		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		args.setQuery(query);

		args.setSorter(new Sorter(TaskProperties.ACTUAL_START_DATE,
				Sorter.SORT_DESCENDING));
		args.setOrdered(true);
		return args;
	}

	/**
	 * Gets the overdue date task filter.
	 *
	 * @param includeOwner
	 *            the include owner
	 * @return the overdue date task filter
	 */
	public SearchArguments<AbstractTaskInstance> getOverdueDateTaskFilter(boolean includeOwner) {
		SearchArguments<AbstractTaskInstance> args = new SearchArguments<AbstractTaskInstance>();

		Map<String, Serializable> arguments = getBaseSearchArgument();
		arguments.put(TaskProperties.SEARCH_STATE, TaskState.IN_PROGRESS.toString());
		Calendar dueToDate = Calendar.getInstance();
		dueToDate.setTimeInMillis(System.currentTimeMillis());
		dueToDate.set(Calendar.HOUR_OF_DAY, 0);
		dueToDate.set(Calendar.MINUTE, 0);
		dueToDate.set(Calendar.SECOND, 0);
		dueToDate.clear(Calendar.MILLISECOND);
		arguments.put(TaskProperties.PLANNED_END_DATE,
				new DateRange(null, new Date(dueToDate.getTimeInMillis() - 1)));
		User currentUser = getCurrentUser();
		if (includeOwner && (currentUser != null)) {
			arguments.put(TaskProperties.TASK_OWNER, currentUser.getIdentifier());
		}
		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		args.setQuery(query);

		args.setSorter(new Sorter(TaskProperties.ACTUAL_START_DATE,
				Sorter.SORT_DESCENDING));
		args.setOrdered(true);
		return args;
	}

	/**
	 * Gets the poolable task filter.
	 *
	 * @param includeOwner
	 *            the include owner
	 * @return the poolable task filter
	 */
	public SearchArguments<AbstractTaskInstance> getPoolableTaskFilter(boolean includeOwner) {
		SearchArguments<AbstractTaskInstance> args = new SearchArguments<AbstractTaskInstance>();

		Map<String, Serializable> arguments = getBaseSearchArgument();
		arguments.put(TaskProperties.SEARCH_STATE, TaskState.IN_PROGRESS.toString());
		arguments.put(CommonProperties.PROPERTY_ISNULL, TaskProperties.TASK_OWNER);
		User currentUser = getCurrentUser();
		Query query = null;
		if (includeOwner && (currentUser != null)) {
			query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND, true, true);
			List<Resource> authorities = resourceService.getContainingResources(currentUser);
			query = query.and(new Query(TaskProperties.POOL_GROUP, convertToID(authorities)).or(
					TaskProperties.POOL_ACTORS, currentUser.getIdentifier()));
		} else {
			query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND, true, true);
		}
		args.setQuery(query);
		args.setSorter(new Sorter(TaskProperties.ACTUAL_START_DATE,
				Sorter.SORT_DESCENDING));
		args.setOrdered(true);
		return args;
	}

	/**
	 * Gets the base task filter.
	 *
	 * @return the base task filter
	 */
	public SearchArguments<AbstractTaskInstance> getBaseTaskFilter() {
		SearchArguments<AbstractTaskInstance> searchArguments = new SearchArguments<AbstractTaskInstance>();
		Map<String, Serializable> argumentsMap = getBaseSearchArgument();
		argumentsMap.put(TaskProperties.ACTUAL_START_DATE, new DateRange(null, null));
		argumentsMap.put(TaskProperties.PLANNED_END_DATE, new DateRange(null, null));
		searchArguments.setArguments(argumentsMap);
		searchArguments.setSorter(new Sorter(TaskProperties.ACTUAL_START_DATE,
				Sorter.SORT_DESCENDING));
		return searchArguments;
	}

	/**
	 * Gets the base search argument.
	 *
	 * @return the base search argument
	 */
	protected Map<String, Serializable> getBaseSearchArgument() {
		final Map<String, Serializable> arguments = new HashMap<String, Serializable>();
		arguments.put(CommonProperties.KEY_SEARCHED_ASPECT, WorkflowProperties.TYPE_WORKFLOW_TASK);
		return arguments;
	}

	/**
	 * Gets the all tasks filter.
	 *
	 * @param includeOwner
	 *            the include owner
	 * @return the all tasks filter
	 */
	public SearchArguments<AbstractTaskInstance> getAllTasksFilter(boolean includeOwner) {
		final Map<String, Serializable> arguments = getBaseSearchArgument();
		final SearchArguments<AbstractTaskInstance> args = new SearchArguments<AbstractTaskInstance>();
		User currentUser = getCurrentUser();
		if (includeOwner && (currentUser != null)) {
			arguments.put(TaskProperties.TASK_OWNER, currentUser.getIdentifier());
		}
		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		Query pooled = getPoolableTaskFilter(includeOwner).getQuery();
		args.setQuery(pooled.or(query));

		args.setSorter(new Sorter(TaskProperties.ACTUAL_START_DATE,
				Sorter.SORT_DESCENDING));
		args.setOrdered(true);

		return args;
	}

	/**
	 * Gets the current user.
	 *
	 * @return the current user or null if authentication service could not retrieve it.
	 */
	protected User getCurrentUser() {
		if (authenticationService.isUnsatisfied() || authenticationService.isAmbiguous()) {
			return SecurityContextManager.getFullAuthentication();
		}
		return SecurityContextManager.getCurrentUser(authenticationService.get());
	}

	/**
	 * Internal convert to strings for list of groups.
	 *
	 * @param authorities
	 *            are the groups to convert
	 * @return list of group names, to be searchable directly
	 */
	protected Serializable convertToID(List<Resource> authorities) {
		List<String> authoritiesIds = new ArrayList<String>(authorities.size());
		for (Resource emfGroup : authorities) {
			authoritiesIds.add(emfGroup.getIdentifier());
		}
		return (Serializable) authoritiesIds;
	}

	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter,
			Context<String, Object> context) {
		if (filter != null) {
			return buildSearchArguments(filter.getValue(), context);
		}
		return null;
	}

}
