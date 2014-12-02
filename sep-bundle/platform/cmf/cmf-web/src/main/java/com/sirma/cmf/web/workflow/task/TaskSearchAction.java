package com.sirma.cmf.web.workflow.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryType;
import com.sirma.cmf.web.search.SearchConstants;
import com.sirma.cmf.web.search.SearchPageType;
import com.sirma.cmf.web.search.SearchTypeSelectedEvent;
import com.sirma.cmf.web.search.facet.FacetSearchAction;
import com.sirma.cmf.web.search.facet.FacetSearchFilter;
import com.sirma.cmf.web.search.facet.SelectedFilternameHolder;
import com.sirma.cmf.web.search.facet.TaskFilterType;
import com.sirma.cmf.web.search.facet.event.FacetEvent;
import com.sirma.cmf.web.search.facet.event.FacetEventType;
import com.sirma.cmf.web.search.facet.event.SearchFilterUpdateEvent;
import com.sirma.cmf.web.search.facet.event.UpdatedSearchFilterBinding;
import com.sirma.cmf.web.search.sort.SortActionItem;
import com.sirma.cmf.web.search.sort.TaskSortingActionTypesUpdateEvent;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.search.Query;
import com.sirma.itt.emf.search.SearchFilterProperties;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * Action bean behind the task search form.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class TaskSearchAction extends
		FacetSearchAction<AbstractTaskInstance, SearchArguments<AbstractTaskInstance>> implements
		Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1962648569577964984L;

	/** The priority cl. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_SERVICE_PRIORITY, defaultValue = "208")
	private Integer priorityCL;

	/** The task definition cl. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_TASK_DEFINITION, defaultValue = "227")
	private Integer taskDefinitionCL;

	/** The task status cl. */
	@Inject
	@Config(name = CmfConfigurationProperties.CODELIST_TASK_STATUS, defaultValue = "102")
	private Integer taskStatusCL;

	/**
	 * Available task filters.
	 */
	private List<FacetSearchFilter> taskFilters;

	/** The sort action items. */
	private ArrayList<SortActionItem> sortActionItems;

	/** The selected sorter type. */
	private String selectedType;

	@Inject
	private SelectedFilternameHolder selectedFilternameHolder;

	/**
	 * On search page selected.
	 * 
	 * @param event
	 *            the event
	 */
	public void onSearchPageSelected(
			@Observes @SearchPageType(SearchConstants.TASK_SEARCH) SearchTypeSelectedEvent event) {

		onCreate();
	}

	/**
	 * History open task list page observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void historyOpenTaskListPageObserver(
			@Observes @NavigationHistoryType(NavigationConstants.TASK_LIST_PAGE) NavigationHistoryEvent event) {
		log.debug("CMFWeb: Executing observer TaskSearchAction.historyOpenTaskListPageObserver");

		// TODO: implement search filter invoking if user returns back in history
	}

	/**
	 * Sets a currently selected task filter.
	 * 
	 * @param facetEvent
	 *            Event object.
	 */
	public void selectActiveTaskFilter(
			@Observes @FacetEventType("AbstractTaskInstance") FacetEvent facetEvent) {
		log.debug("CMFWeb: Executing observer TaskSearchAction.selectActiveTaskFilter: ["
				+ facetEvent.getActiveFilterName() + "]");
		selectedFilternameHolder.setSelectedFilterName(facetEvent.getActiveFilterName());
	}

	@Override
	public List<SortActionItem> getSortActions() {
		if (sortActionItems == null) {
			TaskSortActionType[] values = TaskSortActionType.values();
			sortActionItems = new ArrayList<SortActionItem>(values.length);
			for (TaskSortActionType sortActionType : values) {
				SortActionItem item = new SortActionItem(sortActionType.getType(),
						sortActionType.getLabel(labelProvider));
				sortActionItems.add(item);
			}
		}

		TaskSortingActionTypesUpdateEvent event = new TaskSortingActionTypesUpdateEvent(
				sortActionItems);
		eventService.fire(event);

		return sortActionItems;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String applySearchFilter(String filterType) {

		SearchArguments<AbstractTaskInstance> searchArguments = null;
		Context<String, Object> searchContext = new Context<String, Object>(1);
		searchContext.put(SearchFilterProperties.INCLUDE_OWNER, Boolean.TRUE);

		if (TaskFilterType.ALL_TASKS.getFilterName().equals(filterType)) {
			searchArguments = searchService.getFilter("getAllTasksFilter",
					AbstractTaskInstance.class, searchContext);
		} else if (TaskFilterType.ACTIVE_TASKS.getFilterName().equals(filterType)) {
			searchArguments = searchService.getFilter("getOpenTaskFilter",
					AbstractTaskInstance.class, searchContext);
		} else if (TaskFilterType.HIGH_PRIORITY_TASKS.getFilterName().equals(filterType)) {
			searchArguments = searchService.getFilter("getHighPriorityTaskFilter",
					AbstractTaskInstance.class, searchContext);
		} else if (TaskFilterType.DUE_DATE_TODAY_TASKS.getFilterName().equals(filterType)) {
			searchArguments = searchService.getFilter("getDueDateTodayTaskFilter",
					AbstractTaskInstance.class, searchContext);
		} else if (TaskFilterType.OVERDUE_DATE_TASKS.getFilterName().equals(filterType)) {
			searchArguments = searchService.getFilter("getOverdueDateTaskFilter",
					AbstractTaskInstance.class, searchContext);
		} else if (TaskFilterType.UNASSIGNED_TASKS.getFilterName().equals(filterType)) {
			searchArguments = searchService.getFilter("getPoolableTaskFilter",
					AbstractTaskInstance.class, searchContext);
		}

		setSearchData(searchArguments);

		return NavigationConstants.NAVIGATE_TASK_LIST_PAGE;
	}

	/**
	 * Getter method for taskFilters.
	 * 
	 * @return the taskFilters
	 */
	public List<FacetSearchFilter> getTaskFilters() {

		List<FacetSearchFilter> filters = new ArrayList<FacetSearchFilter>();
		filters.add(createFilter(TaskFilterType.ACTIVE_TASKS.getFilterName()));
		filters.add(createFilter(TaskFilterType.ALL_TASKS.getFilterName()));
		filters.add(createFilter(TaskFilterType.HIGH_PRIORITY_TASKS.getFilterName()));
		filters.add(createFilter(TaskFilterType.DUE_DATE_TODAY_TASKS.getFilterName()));
		filters.add(createFilter(TaskFilterType.OVERDUE_DATE_TASKS.getFilterName()));
		filters.add(createFilter(TaskFilterType.UNASSIGNED_TASKS.getFilterName()));

		SearchFilterUpdateEvent event = new SearchFilterUpdateEvent();
		event.setFacetSearchFilters(filters);

		UpdatedSearchFilterBinding updatedSearchFilterBinding = new UpdatedSearchFilterBinding(
				UpdatedSearchFilterBinding.TASK);
		searchFilterUpdateEvent.select(updatedSearchFilterBinding).fire(event);

		taskFilters = event.getFacetSearchFilters();
		return taskFilters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String fetchResults() {

		SearchArguments<AbstractTaskInstance> searchData = getSearchData();
		Context<String, Object> searchContext = new Context<String, Object>(1);
		searchContext.put(SearchFilterProperties.INCLUDE_OWNER, Boolean.TRUE);
		// add the explicit checkbox param
		Serializable overdueTask = getSearchData().getArguments().get("overdueTask");
		if ((overdueTask != null) && Boolean.TRUE.equals(Boolean.valueOf(overdueTask.toString()))) {
			Query query = searchService.getFilter("getOverdueDateTaskFilter",
					AbstractTaskInstance.class, searchContext).getQuery();
			Query entry = query.getEntry(TaskProperties.PLANNED_END_DATE);
			if (entry != null) {
				searchData.getArguments().put(entry.getKey(), entry.getValue());
			}
		}
		// part of EGOV-3605
		wildCardSearchEntry(searchData.getArguments(), DefaultProperties.UNIQUE_IDENTIFIER, '*');
		searchService.search(AbstractTaskInstance.class, searchData);

		return NavigationConstants.NAVIGATE_TASK_LIST_PAGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<AbstractTaskInstance> getEntityClass() {
		return AbstractTaskInstance.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SearchArguments<AbstractTaskInstance> initSearchData() {
		SearchArguments<AbstractTaskInstance> searchArguments = searchService.getFilter(
				"getBaseTaskFilter", AbstractTaskInstance.class, null);
		return searchArguments;
	}

	/**
	 * Gets the priority cl.
	 * 
	 * @return the priorityCL
	 */
	public Integer getPriorityCL() {
		return priorityCL;
	}

	/**
	 * Gets the task definition cl.
	 * 
	 * @return the taskDefinitionsCL
	 */
	public Integer getTaskDefinitionCL() {
		return taskDefinitionCL;
	}

	/**
	 * Gets the task status cl.
	 * 
	 * @return the taskStatesCL
	 */
	public Integer getTaskStatusCL() {
		return taskStatusCL;
	}

}
