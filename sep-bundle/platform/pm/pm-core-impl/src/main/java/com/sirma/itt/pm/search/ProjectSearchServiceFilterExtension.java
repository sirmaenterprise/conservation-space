package com.sirma.itt.pm.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.CommonProperties;
import com.sirma.itt.cmf.services.adapter.CMFPermissionAdapterService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.SearchFilterProperties;
import com.sirma.itt.emf.search.SearchServiceFilterExtension;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.constants.ProjectProperties.Visbility;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Extension implementation for {@link ProjectInstance} filters.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = SearchServiceFilterExtension.TARGET_NAME, order = 30)
public class ProjectSearchServiceFilterExtension implements
SearchServiceFilterExtension {

	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(
			Arrays.asList(ProjectInstance.class));

	private final static HashSet<String> PROJECT_ASPECTS = new HashSet<>(1);

	@Inject
	private StateService stateService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private CMFPermissionAdapterService permissionAdapterService;

	{
		PROJECT_ASPECTS.add("projectInstance");
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
		if ("listAllProjects".equals(filterName)) {
			return (S) listAllProjects();
		} else if ("listActiveProjects".equals(filterName)) {
			return (S) listActiveProjects();
		} else if ("listCompletedProjects".equals(filterName)) {
			return (S) listCompletedProjects();
		} else if ("permissionsProject".equals(filterName)) {
			String userId = (String) context.get(SearchFilterProperties.USER_ID);
			Resource currentUser = null;
			if (userId != null) {
				currentUser = resourceService.getResource(userId, ResourceType.USER);
			}
			return (S) listAllowedProjects((User) currentUser);
		} else if ("visibleProject".equals(filterName)) {
			String userId = (String) context.get(SearchFilterProperties.USER_ID);
			Resource currentUser = null;
			if (userId != null) {
				currentUser = resourceService.getResource(userId, ResourceType.USER);
			}
			return (S) listAllowedAndVisibleProjects((User) currentUser);
		} else if ((context != null) && "filterAllProjects".equals(filterName)) {
			Object object = context.get(SearchFilterProperties.FILTER);
			if (object != null) {
				return (S) filterAllProjects(object.toString());
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
	 * Get the query part contatining the security information for access to project.
	 * 
	 * @param <P>
	 *            the result arguments type
	 * @param currentUser
	 *            is the user to use
	 * @return new {@link Query} with access restrictions.
	 */
	private <P extends ProjectInstance> SearchArguments<P> listAllowedProjects(User currentUser) {
		SearchArguments<P> args = new SearchArguments<>();
		Query query = new Query(CMFPermissionAdapterService.LIST_OF_ALLOWED_GROUPS,
				convertToID(resourceService.getContainingResources(currentUser))).or(
						CMFPermissionAdapterService.LIST_OF_ALLOWED_USERS, currentUser.getIdentifier());
		args.setQuery(query);
		return args;
	}

	/**
	 * Get the query part contatining the security information for access to project + the
	 * visibility metadata
	 * 
	 * @param <P>
	 *            the result arguments type
	 * @param currentUser
	 *            is the user to use
	 * @return new {@link Query} with access restrictions.
	 */
	private <P extends ProjectInstance> SearchArguments<P> listAllowedAndVisibleProjects(
			final User currentUser) {

		SearchArguments<P> args = new SearchArguments<>();
		Query query = new Query(ProjectProperties.VISIBILITY, Visbility.PUBLIC.getName(),
				Boolean.TRUE).or(listAllowedProjects(currentUser).getQuery()).end();
		args.setQuery(query);
		return args;
	}

	/**
	 * Internal convert to strings for list of groups.
	 * 
	 * @param authorities
	 *            are the groups to convert
	 * @return list of group names, to be searchable directly
	 */
	private Serializable convertToID(List<Resource> authorities) {
		ArrayList<String> authoritiesIds = new ArrayList<>(authorities.size());
		for (Resource emfGroup : authorities) {
			authoritiesIds.add(permissionAdapterService.searchableUserId(emfGroup.getIdentifier())
					.toString());
		}
		return authoritiesIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public <P extends ProjectInstance> SearchArguments<P> listAllProjects() {
		SearchArguments<P> args = new SearchArguments<>();
		// CMF-2121 - skip deleted
		String deletedState = stateService.getState(PrimaryStates.DELETED, ProjectInstance.class);
		args.setQuery(new Query(CommonProperties.KEY_SEARCHED_ASPECT, PROJECT_ASPECTS, true)
		.andNot(ProjectProperties.STATUS, deletedState).end());
		args.setSorter(new Sorter(CaseProperties.MODIFIED_ON, "desc"));
		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * {@inheritDoc}
	 */
	public <P extends ProjectInstance> SearchArguments<P> listActiveProjects() {
		SearchArguments<P> args = new SearchArguments<>();

		String completedState = stateService.getState(PrimaryStates.COMPLETED,
				ProjectInstance.class);
		String canceledState = stateService.getState(PrimaryStates.CANCELED, ProjectInstance.class);
		String deletedState = stateService.getState(PrimaryStates.DELETED, ProjectInstance.class);
		args.setQuery(new Query(CommonProperties.KEY_SEARCHED_ASPECT, PROJECT_ASPECTS)
		.andNot(new Query(ProjectProperties.STATUS, completedState, true)
		.or(ProjectProperties.STATUS, canceledState)
		.or(ProjectProperties.STATUS, deletedState).end()));

		args.setSorter(new Sorter(CaseProperties.MODIFIED_ON, "desc"));
		args.setOrdered(true);
		args.setSkipCount(0);

		return args;
	}

	/**
	 * {@inheritDoc}
	 */
	public <P extends ProjectInstance> SearchArguments<P> listCompletedProjects() {
		SearchArguments<P> args = new SearchArguments<>();

		String completedState = stateService.getState(PrimaryStates.COMPLETED,
				ProjectInstance.class);
		args.setQuery(new Query(CommonProperties.KEY_SEARCHED_ASPECT, PROJECT_ASPECTS).and(
				ProjectProperties.STATUS, completedState));
		args.setSorter(new Sorter(CaseProperties.MODIFIED_ON, "desc"));
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
	public <P extends ProjectInstance> SearchArguments<P> filterAllProjects(String filter) {
		SearchArguments<P> listAllProjects = listAllProjects();
		Query finalQuery = new Query(ProjectProperties.TITLE, filter, true)
		.or(DefaultProperties.UNIQUE_IDENTIFIER, filter).or(ProjectProperties.TYPE, filter)
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
