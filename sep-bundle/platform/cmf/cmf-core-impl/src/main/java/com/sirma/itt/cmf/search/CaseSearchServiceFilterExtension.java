package com.sirma.itt.cmf.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.Query.QueryBoost;
import com.sirma.itt.emf.search.SearchFilterProperties;
import com.sirma.itt.emf.search.SearchServiceFilterExtension;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchArgumentsMap;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.time.DateRange;

/**
 * Filter extension for case and documents filters.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = SearchServiceFilterExtension.TARGET_NAME, order = 10)
public class CaseSearchServiceFilterExtension implements SearchServiceFilterExtension {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			CaseInstance.class, DocumentInstance.class));

	/** The Constant CASE_ASPECTS. */
	private final static HashSet<String> CASE_ASPECTS = new HashSet<String>(1);

	/** The Constant DOCUMENT_ASPECTS. */
	private final static HashSet<String> DOCUMENT_ASPECTS = new HashSet<String>(2);

	static {
		CASE_ASPECTS.add(CaseProperties.TYPE_CASE_INSTANCE);

		DOCUMENT_ASPECTS.add(DocumentProperties.TYPE_DOCUMENT_ATTACHMENT);
		DOCUMENT_ASPECTS.add(DocumentProperties.TYPE_DOCUMENT_STRUCTURED);
	}
	/** The state service instance. */
	@Inject
	private StateService stateService;

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

		Serializable instance = null;
		String contextKey = null;
		Query dateRangeQuery = null;
		String user = null;
		if (context != null) {
			instance = context.getIfSameType(SearchFilterProperties.INSTANCE_CONTEXT,
					Serializable.class);
			contextKey = context.getIfSameType(SearchFilterProperties.INSTANCE_CONTEXT_KEY,
					String.class);
			dateRangeQuery = context.getIfSameType(SearchFilterProperties.DATE_RANGE_QUERY,
					Query.class);
			// could add if user not specified to fetch the current user
			user = context.getIfSameType(SearchFilterProperties.USER_ID, String.class);
		}

		// used in case search with no arguments
		if ("baseCaseFilter".equals(filterName)) {
			return addContextFilters((S) baseCaseFilter(), contextKey, instance, dateRangeQuery);
		} else if ("listAllCaseInstances".equals(filterName)) {
			return addContextFilters((S) listAllCaseInstances(user), contextKey, instance,
					dateRangeQuery);
		} else if ("listActiveCaseInstances".equals(filterName)) {
			return addContextFilters((S) listActiveCaseInstances(), contextKey, instance,
					dateRangeQuery);
		} else if ("listLastModifiedDocuments".equals(filterName)) {
			String mimetype = (String) context.get(DocumentProperties.MIMETYPE);
			return addContextFilters((S) listEditedDocuments(null, mimetype), contextKey, instance,
					null);
		} else if (user != null) {
			if ("listCaseInstancesFromUser".equals(filterName)) {
				return addContextFilters((S) listCaseInstancesFromUser(user), contextKey, instance,
						dateRangeQuery);
			} else if ("listLockedDocumentsFromUser".equals(filterName)) {
				return addContextFilters((S) listLockedDocumentsFromUser(user), contextKey,
						instance, dateRangeQuery);
			} else if ("listEditedDocumentsByUser".equals(filterName)) {
				String mimetype = (String) context.get(DocumentProperties.MIMETYPE);
				return addContextFilters((S) listEditedDocuments(user, mimetype), contextKey,
						instance, dateRangeQuery);
			}
		}
		return null;
	}

	/**
	 * Base case filter.
	 *
	 * @return the search arguments
	 */
	private SearchArguments<CaseInstance> baseCaseFilter() {
		SearchArguments<CaseInstance> searchArguments = new SearchArguments<CaseInstance>();
		Map<String, Serializable> argumentsMap = getBaseSearchArgument();
		argumentsMap.put(CaseProperties.CREATED_ON, new DateRange(null, null));
		searchArguments.setArguments(argumentsMap);
		searchArguments.setSorter(new Sorter(CaseProperties.MODIFIED_ON,
				Sorter.SORT_ASCENDING));
		return searchArguments;
	}

	/**
	 * Gets the base search argument.
	 *
	 * @return the base search argument
	 */
	protected Map<String, Serializable> getBaseSearchArgument() {
		final Map<String, Serializable> arguments = new HashMap<String, Serializable>();
		arguments.put(CommonProperties.KEY_SEARCHED_ASPECT, CASE_ASPECTS);
		return arguments;
	}

	/**
	 * Adds the context filters.
	 *
	 * @param <S>
	 *            the generic type
	 * @param arguments
	 *            the arguments
	 * @param contextKey
	 *            the context key
	 * @param context
	 *            the context
	 * @param dateRangeQuery
	 *            the date range query
	 * @return the s
	 */
	protected <S extends SearchArguments<?>> S addContextFilters(S arguments,
			String contextKey, Serializable context, Query dateRangeQuery) {
		if ((contextKey != null) && (context != null)) {
			Query queryAll = new Query(contextKey, context, true).and(arguments.getQuery());
			if (dateRangeQuery != null) {
				queryAll = queryAll.and(dateRangeQuery);
			}
			queryAll = queryAll.end();
			arguments.setQuery(queryAll);
		} else if (dateRangeQuery != null) {
			arguments.setQuery(dateRangeQuery.and(arguments.getQuery()));
		}
		return arguments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchFilterConfig getFilterConfiguration(String placeHolder) {
		return new SearchFilterConfig(new LinkedList<SearchFilter>(),
				new LinkedList<SearchFilter>());
	}

	/**
	 * List all case instances.
	 *
	 * @param <C>
	 *            the generic type
	 * @param userId
	 *            the user id
	 * @return the search arguments
	 */
	protected <C extends CaseInstance> SearchArguments<C> listAllCaseInstances(String userId) {
		SearchArguments<C> args = new SearchArguments<C>();
		Map<String, Serializable> arguments = new HashMap<String, Serializable>();
		arguments.put(CommonProperties.KEY_SEARCHED_ASPECT, CASE_ASPECTS);
		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		String deletedState = getDeletedState();
		if (deletedState != null) {
			query = query.andNot(CaseProperties.STATUS, deletedState);
		}
		if (userId != null) {
			query = new Query(DefaultProperties.CREATED_BY, userId, true).and(query).end();
		}
		args.setQuery(query);

		args.setSorter(new Sorter(CaseProperties.MODIFIED_ON, "desc"));

		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * List active case instances.
	 *
	 * @param <C>
	 *            the generic type
	 * @return the search arguments
	 */
	protected <C extends CaseInstance> SearchArguments<C> listActiveCaseInstances() {
		SearchArguments<C> args = new SearchArguments<C>();
		Map<String, Serializable> arguments = new HashMap<String, Serializable>();
		arguments.put(CommonProperties.KEY_SEARCHED_ASPECT, CASE_ASPECTS);
		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		Set<String> openedState = getOpenedState();
		Query openStates = Query.getEmpty();
		boolean hasOpen = false;
		for (String state : openedState) {
			if (state != null) {
				hasOpen = true;
				openStates = openStates.or(CaseProperties.STATUS, state);
			}
		}
		if (hasOpen) {
			query = query.and(openStates);
		}

		String deletedState = getDeletedState();
		if (deletedState != null) {
			query = query.andNot(CaseProperties.STATUS, deletedState);
		}
		args.setQuery(query);

		args.setSorter(new Sorter(CaseProperties.MODIFIED_ON, "desc"));

		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * List case instances from user.
	 *
	 * @param <C>
	 *            the generic type
	 * @param user
	 *            the user
	 * @return the search arguments
	 */
	protected <C extends CaseInstance> SearchArguments<C> listCaseInstancesFromUser(String user) {
		SearchArguments<C> args = new SearchArguments<C>();
		Map<String, Serializable> arguments = new HashMap<String, Serializable>();
		arguments.put(CommonProperties.KEY_SEARCHED_ASPECT, CASE_ASPECTS);
		arguments.put(CaseProperties.CREATED_BY, user);
		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);
		String deletedState = getDeletedState();
		if (deletedState != null) {
			query = query.andNot(CaseProperties.STATUS, deletedState);
		}
		args.setQuery(query);

		args.setSorter(new Sorter(CaseProperties.MODIFIED_ON, "desc"));

		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * List locked documents from user.
	 *
	 * @param <C>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param user
	 *            the user
	 * @return the search arguments map
	 */
	protected <C extends CaseInstance, D extends List<DocumentInstance>> SearchArgumentsMap<C, D> listLockedDocumentsFromUser(
			String user) {
		SearchArgumentsMap<C, D> args = new SearchArgumentsMap<C, D>();
		Map<String, Serializable> arguments = new HashMap<String, Serializable>();
		arguments.put(CommonProperties.KEY_SEARCHED_ASPECT, DOCUMENT_ASPECTS);
		// arguments.put(DocumentProperties.LOCKED_BY, user);
		arguments.put("cm:lockOwner", user);
		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		Map<String, Serializable> map = new HashMap<String, Serializable>(3);
		Calendar instance = Calendar.getInstance();
		instance.setTimeInMillis(System.currentTimeMillis());
		instance.add(Calendar.DAY_OF_YEAR, -30);
		Date past = instance.getTime();
		map.put(DocumentProperties.MODIFIED_ON, new DateRange(past, null));
		map.put(DocumentProperties.CREATED_ON, new DateRange(past, null));

		query = query.and(Query.fromMap(map, QueryBoost.INCLUDE_OR));

		map = new HashMap<String, Serializable>(3);
		map.put(DocumentProperties.CREATED_BY, user);
		map.put(DocumentProperties.MODIFIED_BY, user);

		query = query.and(Query.fromMap(map, QueryBoost.INCLUDE_OR));

		args.setQuery(query);

		args.setSorter(new Sorter(DocumentProperties.MODIFIED_ON, "desc"));

		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * List edited documents by user.
	 *
	 * @param <C>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param user
	 *            the user
	 * @param mimetype
	 *            whether to filter only this mimetype - support wildcards as image/*
	 * @return the search arguments map
	 */
	protected <C extends CaseInstance, D extends List<DocumentInstance>> SearchArgumentsMap<C, D> listEditedDocuments(
			String user, String mimetype) {
		SearchArgumentsMap<C, D> args = new SearchArgumentsMap<C, D>();
		Map<String, Serializable> arguments = new HashMap<String, Serializable>();
		arguments.put(CommonProperties.KEY_SEARCHED_ASPECT, DOCUMENT_ASPECTS);

		Query query = Query.fromMap(arguments, QueryBoost.INCLUDE_AND);

		Map<String, Serializable> map = new HashMap<String, Serializable>(3);
		Calendar instance = Calendar.getInstance();
		instance.setTimeInMillis(System.currentTimeMillis());
		instance.add(Calendar.DAY_OF_YEAR, -28);
		Date past = instance.getTime();
		map.put(DocumentProperties.MODIFIED_ON, new DateRange(past, null));
		map.put(DocumentProperties.CREATED_ON, new DateRange(past, null));

		query = query.and(Query.fromMap(map, QueryBoost.INCLUDE_OR));
		if (mimetype != null) {
			query = query.and(new Query(DocumentProperties.MIMETYPE, mimetype));
		}
		if (user != null) {
			map = new HashMap<String, Serializable>(3);
			map.put(DocumentProperties.CREATED_BY, user);
			map.put(DocumentProperties.MODIFIED_BY, user);
			query = query.and(Query.fromMap(map, QueryBoost.INCLUDE_OR));
		}

		args.setQuery(query);

		args.setSorter(new Sorter(DocumentProperties.MODIFIED_ON, "desc"));

		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * Gets the opened state.
	 *
	 * @return the opened state
	 */
	protected Set<String> getOpenedState() {
		return new HashSet<String>(Arrays.asList(
				stateService.getState(PrimaryStates.OPENED, CaseInstance.class),
				stateService.getState(PrimaryStates.APPROVED, CaseInstance.class),
				stateService.getState(PrimaryStates.ON_HOLD, CaseInstance.class)));
	}

	/**
	 * Gets the opened state.
	 *
	 * @return the opened state
	 */
	protected String getDeletedState() {
		return stateService.getState(PrimaryStates.DELETED, CaseInstance.class);
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
