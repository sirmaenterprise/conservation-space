package com.sirma.itt.pm.web.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryType;
import com.sirma.cmf.web.search.SearchPageType;
import com.sirma.cmf.web.search.SearchTypeSelectedEvent;
import com.sirma.cmf.web.search.facet.FacetSearchFilter;
import com.sirma.cmf.web.search.facet.SelectedFilternameHolder;
import com.sirma.cmf.web.search.facet.event.FacetEvent;
import com.sirma.cmf.web.search.facet.event.FacetEventType;
import com.sirma.cmf.web.search.facet.event.SearchFilterUpdateEvent;
import com.sirma.cmf.web.search.facet.event.UpdatedSearchFilterBinding;
import com.sirma.cmf.web.userdashboard.event.DashletToolbarActionEvent;
import com.sirma.cmf.web.userdashboard.event.SelectedDashletToolbarAction;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.SearchFilterProperties;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.time.DateRange;
import com.sirma.itt.emf.web.menu.main.event.MainMenuEvent;
import com.sirma.itt.emf.web.menu.main.event.SelectedMainMenu;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.web.constants.PmNavigationConstants;
import com.sirma.itt.pm.web.search.facet.FacetSearchActionPM;
import com.sirma.itt.pm.web.userdashboard.panel.MyProjectsPanel;

/**
 * The Class ProjectSearchAction.
 * 
 * @author BBonev
 */
@Named
@ViewAccessScoped
public class ProjectSearchAction extends
		FacetSearchActionPM<ProjectInstance, SearchArguments<ProjectInstance>> implements
		Serializable {

	private static final long serialVersionUID = -1962648569577964984L;

	private static final Sorter SORTER_CREATED_ON = new Sorter(ProjectProperties.CREATED_ON,
			Sorter.SORT_DESCENDING);

	protected static final String ALL_PROJECTS = "ALL_PROJECTS";

	/** The min. number of characters for the browse projects */
	private static final int ALLOWED_SEARCH_ARGUMENTS = 3;

	/** Available case filters. */
	private List<FacetSearchFilter> projectFilters;

	/** The browse project filter input. */
	private String browseProjectFilterInput;

	/** Flag for filter errors. */
	private boolean projectSearchArguentsError;

	@Inject
	private SelectedFilternameHolder selectedFilternameHolder;

	/**
	 * Sets a currently selected project filter.
	 * 
	 * @param facetEvent
	 *            Event object.
	 */
	public void selectActiveProjectFilter(
			@Observes @FacetEventType("ProjectInstance") FacetEvent facetEvent) {
		log.debug("PMWeb: Executing observer ProjectSearchAction.selectActiveProjectFilter: ["
				+ facetEvent.getActiveFilterName() + "]");
		selectedFilternameHolder.setSelectedFilterName(facetEvent.getActiveFilterName());
	}

	/**
	 * History open project search page observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void historyOpenProjectSearchPageObserver(
			@Observes @NavigationHistoryType(PmNavigationConstants.PROJECT_SEARCH) NavigationHistoryEvent event) {
		log.debug("PMWeb: Executing observer ProjectSearchAction.historyOpenProjectSearchPageObserver");
		init();
	}

	/**
	 * My projects dashlet search action observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void myProjectsDashletSearchActionObserver(
			@Observes @SelectedDashletToolbarAction(dashlet = MyProjectsPanel.USERDASHBOARD_DASHLET_PROJECTS, action = PmNavigationConstants.PROJECT_SEARCH) DashletToolbarActionEvent event) {
		log.debug("PMWeb: Executing observer ProjectSearchAction.myProjectsDashletSearchActionObserver");
		event.setNavigation(PmNavigationConstants.PROJECT_SEARCH);
		init();
	}

	/**
	 * Observer for executing a default search action.
	 * 
	 * @param mainMenuEvent
	 *            the main menu event
	 */
	public void defaultSearch(
			@Observes @SelectedMainMenu(PmNavigationConstants.PROJECT_SEARCH) MainMenuEvent mainMenuEvent) {
		log.debug("PMWeb: Executing observer ProjectSearchAction.defaultSearch");
		init();
	}

	/**
	 * On search page selected.
	 * 
	 * @param event
	 *            the event
	 */
	public void onSearchPageSelected(
			@Observes @SearchPageType("project-search") SearchTypeSelectedEvent event) {
		log.debug("PMWeb: Executing observer ProjectSearchAction.onSearchPageSelected");
		onCreate();
	}

	/**
	 * Inits the project search page.
	 */
	public void init() {
		executeDefaultProjectSearch();
		setBrowseProjectFilterInput(null);
		selectedFilternameHolder.setSelectedFilterName(ALL_PROJECTS);
	}

	/**
	 * Execute default project search.
	 */
	private void executeDefaultProjectSearch() {
		onCreate();
		setSearchData(searchService.getFilter("listAllProjects", ProjectInstance.class, null));
		search();
		selectedFilternameHolder.setSelectedFilterName(ALL_PROJECTS);
	}

	@Override
	public String applySearchFilter(String filterType) {
		if (ALL_PROJECTS.equals(filterType)) {
			setSearchData(searchService.getFilter("listAllProjects", ProjectInstance.class, null));
		}
		return PmNavigationConstants.PROJECT_LIST_PAGE;
	}

	@Override
	public void setSearchData(SearchArguments<ProjectInstance> searchData) {
		if (searchData != null) {
			// add the sorter
			searchData.setSorter(SORTER_CREATED_ON);
			super.setSearchData(searchData);
		}
	}

	/**
	 * Gets the project filters.
	 * 
	 * @return the projectFilters
	 */
	public List<FacetSearchFilter> getProjectFilters() {
		List<FacetSearchFilter> filters = new ArrayList<FacetSearchFilter>();
		filters.add(createFilter(ALL_PROJECTS));

		SearchFilterUpdateEvent event = fireSearchFilterUpdateEvent(filters);
		projectFilters = event.getFacetSearchFilters();
		return projectFilters;
	}

	/**
	 * Fire search filter update event.
	 * 
	 * @param filters
	 *            the filters
	 * @return the search filter update event
	 */
	protected SearchFilterUpdateEvent fireSearchFilterUpdateEvent(List<FacetSearchFilter> filters) {
		SearchFilterUpdateEvent event = new SearchFilterUpdateEvent();
		event.setFacetSearchFilters(filters);
		UpdatedSearchFilterBinding updatedSearchFilterBinding = new UpdatedSearchFilterBinding(
				"project");
		searchFilterUpdateEvent.select(updatedSearchFilterBinding).fire(event);
		return event;
	}

	@Override
	protected String fetchResults() {
		SearchArguments<ProjectInstance> searchData = getSearchData();
		Context<String, Object> context = new Context<>(1);
		context.put(SearchFilterProperties.USER_ID, currentUser.getIdentifier());
		// enrich with visibility filter
		SearchArguments<ProjectInstance> permissionQuery = searchService.getFilter(
				"visibleProject", ProjectInstance.class, context);
		searchData.setQuery(permissionQuery.getQuery().and(searchData.getQuery()));
		searchService.search(ProjectInstance.class, searchData);
		return PmNavigationConstants.PROJECT_LIST_PAGE;
	}

	@Override
	protected Class<ProjectInstance> getEntityClass() {
		return ProjectInstance.class;
	}

	@Override
	protected SearchArguments<ProjectInstance> initSearchData() {
		SearchArguments<ProjectInstance> searchArguments = new SearchArguments<ProjectInstance>();
		searchArguments
				.setQuery(new Query(ProjectProperties.CREATED_ON, new DateRange(null, null)));
		return searchArguments;
	}

	/**
	 * Search for project(s) based on the project id, project name or project type.
	 */
	public void filterProjects() {
		// found projects based on the user filter
		if (getBrowseProjectFilterInput() != null) {
			// user filter
			String trimedFilterInput = getBrowseProjectFilterInput().trim().toLowerCase();
			// prevent empty input, check for number and character
			// size(default 3 symbols allowed)
			if (!trimedFilterInput.isEmpty()
					&& ((trimedFilterInput.length() >= ALLOWED_SEARCH_ARGUMENTS) || isNumeric(trimedFilterInput))) {
				setProjectSearchArguentsError(false);
				// all available projects
				Context<String, Object> searchContext = new Context<String, Object>(1);
				searchContext.put(SearchFilterProperties.FILTER, trimedFilterInput);
				SearchArguments<ProjectInstance> listAllProjects = searchService.getFilter(
						"filterAllProjects", ProjectInstance.class, searchContext);
				setSearchData(listAllProjects);
				search();
			} else {
				// not supported filter, activate error flag
				setProjectSearchArguentsError(true);
			}
		}
	}

	/**
	 * Help method that use regex pattern for detecting numbers. We use this pattern for detect
	 * number that matching with project id. Will not detect zero.
	 * 
	 * @param str
	 *            the number represent as string
	 * @return true(is number), false(not a number)
	 */
	private boolean isNumeric(String str) {

		return str.matches("^\\d{1,20}$");
	}

	/**
	 * Filter for retrieving all projects for project browse.
	 */
	public void filterAllProjects() {
		setBrowseProjectFilterInput(null);
		// retrieve all available projects
		executeDefaultProjectSearch();
	}

	/**
	 * Get input data for project filter.
	 * 
	 * @return project filter data
	 */
	public String getBrowseProjectFilterInput() {
		return browseProjectFilterInput;
	}

	/**
	 * Set input data for project filter.
	 * 
	 * @param browseProjectFilterInput
	 *            project filter input
	 */
	public void setBrowseProjectFilterInput(String browseProjectFilterInput) {
		this.browseProjectFilterInput = browseProjectFilterInput;
	}

	/**
	 * Get method for retrieving the error flag.
	 * 
	 * @return filter error flag
	 */
	public boolean isProjectSearchArguentsError() {
		return projectSearchArguentsError;
	}

	/**
	 * Setter method for projectSearchArguentsError.
	 * 
	 * @param projectSearchArguentsError
	 *            the projectSearchArguentsError to set
	 */
	public void setProjectSearchArguentsError(boolean projectSearchArguentsError) {
		this.projectSearchArguentsError = projectSearchArguentsError;
	}
}
