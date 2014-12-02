package com.sirma.itt.objects.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.SearchFilterProperties;
import com.sirma.itt.emf.search.SearchServiceFilterExtension;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Extension implementation for {@link ProjectInstance} filters.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = SearchServiceFilterExtension.TARGET_NAME, order = 50)
public class ObjectSearchServiceFilterExtension implements
SearchServiceFilterExtension {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(
			Arrays.asList(ObjectInstance.class));
	/** The Constant PROJECT_ASPECTS. */
	private final static HashSet<String> OBJECT_ASPECTS = new HashSet<String>(1);
	/** The state service. */
	@Inject
	private StateService stateService;

	{
		OBJECT_ASPECTS.add("object");
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
	@Override
	@SuppressWarnings("unchecked")
	public <S extends SearchArguments<?>> S buildSearchArguments(String filterName,
			Context<String, Object> context) {
		if ("listAllObjects".equals(filterName)) {
			return (S) listAllObjects();
		} else if ("listActiveObjects".equals(filterName)) {
			return (S) listActiveObjects();
		} else if ((context != null) && "filterAllObjects".equals(filterName)) {
			Object object = context.get(SearchFilterProperties.FILTER);
			if (object != null) {
				return (S) filterAllObjects(object.toString());
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
	 * {@inheritDoc}
	 */
	public <P extends ObjectInstance> SearchArguments<P> listAllObjects() {
		SearchArguments<P> args = new SearchArguments<P>();
		// CMF-2121 - skip deleted
		String deletedState = stateService.getState(PrimaryStates.DELETED, ObjectInstance.class);
		args.setQuery(new Query(CommonProperties.KEY_SEARCHED_ASPECT, OBJECT_ASPECTS, true).andNot(
				DefaultProperties.STATUS, deletedState).end());
		//		args.setSorter(new SearchArguments.Sorter(DefaultProperties.MODIFIED_ON, "desc"));
		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * {@inheritDoc}
	 */
	public <P extends ObjectInstance> SearchArguments<P> listActiveObjects() {
		SearchArguments<P> args = new SearchArguments<P>();

		String deletedState = stateService.getState(PrimaryStates.DELETED, ObjectInstance.class);
		args.setQuery(new Query(CommonProperties.KEY_SEARCHED_ASPECT, OBJECT_ASPECTS)
		.andNot(new Query(DefaultProperties.STATUS, deletedState, true).end()));

		args.setSorter(new Sorter(DefaultProperties.MODIFIED_ON, "desc"));
		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * Filter all projects.
	 *
	 * @param <P>
	 *            the generic type
	 * @param filter
	 *            the filter
	 * @return the search arguments
	 */
	public <P extends ObjectInstance> SearchArguments<P> filterAllObjects(String filter) {
		SearchArguments<P> listAllProjects = listAllObjects();
		Query finalQuery = new Query(DefaultProperties.TITLE, filter, true)
		.or(DefaultProperties.UNIQUE_IDENTIFIER, filter).or(DefaultProperties.TYPE, filter)
		.end().and(listAllProjects.getQuery());

		listAllProjects.setQuery(finalQuery);

		return listAllProjects;
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
