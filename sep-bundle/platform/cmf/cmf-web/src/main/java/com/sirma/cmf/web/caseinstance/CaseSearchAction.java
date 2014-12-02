package com.sirma.cmf.web.caseinstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.NavigationActionConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.menu.NavigationMenu;
import com.sirma.cmf.web.menu.NavigationMenuEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryType;
import com.sirma.cmf.web.search.SearchAction;
import com.sirma.cmf.web.search.SearchConstants;
import com.sirma.cmf.web.search.SearchPageType;
import com.sirma.cmf.web.search.SearchTypeSelectedEvent;
import com.sirma.cmf.web.search.facet.CaseFilterType;
import com.sirma.cmf.web.search.facet.FacetSearchAction;
import com.sirma.cmf.web.search.facet.FacetSearchFilter;
import com.sirma.cmf.web.search.facet.SelectedFilternameHolder;
import com.sirma.cmf.web.search.facet.event.FacetEvent;
import com.sirma.cmf.web.search.facet.event.FacetEventType;
import com.sirma.cmf.web.search.facet.event.SearchFilterUpdateEvent;
import com.sirma.cmf.web.search.facet.event.UpdatedSearchFilterBinding;
import com.sirma.cmf.web.search.sort.CaseSortingActionTypesUpdateEvent;
import com.sirma.cmf.web.search.sort.SortActionItem;
import com.sirma.cmf.web.search.sort.SortActionType;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.codelist.event.CodelistFilter;
import com.sirma.itt.emf.codelist.event.CodelistFiltered;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.search.SearchFilterProperties;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.state.StateService;

/**
 * CaseSearchAction is responsible for case search operations.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class CaseSearchAction extends
		FacetSearchAction<CaseInstance, SearchArguments<CaseInstance>> implements SearchAction,
		Serializable {

	private static final long serialVersionUID = -5231850910698375979L;

	@Inject
	private StateService stateService;

	/**
	 * Available case filters.
	 */
	private List<FacetSearchFilter> caseFilters;

	private ArrayList<SortActionItem> sortActionItems;

	@Inject
	private SelectedFilternameHolder selectedFilternameHolder;

	/**
	 * Sets a currently selected case filter.
	 * 
	 * @param facetEvent
	 *            Event object.
	 */
	public void selectActiveCaseFilter(
			@Observes @FacetEventType("CaseInstance") FacetEvent facetEvent) {

		log.debug("CMFWeb: Executing observer CaseSearchAction.selectActiveCaseFilter: ["
				+ facetEvent.getActiveFilterName() + "]");

		selectedFilternameHolder.setSelectedFilterName(facetEvent.getActiveFilterName());
	}

	/**
	 * Observer for executing a default search action.
	 * 
	 * @param navigationEvent
	 *            the navigation event
	 */
	public void defaultSearch(
			@Observes @NavigationMenu(NavigationActionConstants.CASES) NavigationMenuEvent navigationEvent) {

		log.debug("CMFWeb: Executing observer CaseSearchAction.defaultSearch");

		executeDefaultCaseSearch();

		selectedFilternameHolder.setSelectedFilterName(CaseFilterType.ACTIVE_CASES.getFilterName());
	}

	/**
	 * On search page selected.
	 * 
	 * @param event
	 *            the event
	 */
	public void onSearchPageSelected(
			@Observes @SearchPageType(SearchConstants.CASE_SEARCH) SearchTypeSelectedEvent event) {
		log.debug("CMFWeb: Executing observer CaseSearchAction.onSearchPageSelected");
		onCreate();
	}

	/**
	 * History open case list page observer.
	 * 
	 * @param event
	 *            the event
	 */
	public void historyOpenCaseListPageObserver(
			@Observes @NavigationHistoryType(NavigationConstants.NAVIGATE_CASE_LIST_PAGE) NavigationHistoryEvent event) {
		log.debug("CMFWeb: Executing observer CaseSearchAction.historyOpenCaseListPageObserver");
		executeDefaultCaseSearch();
		selectedFilternameHolder.setSelectedFilterName(CaseFilterType.ACTIVE_CASES.getFilterName());
	}

	/**
	 * Execute default case search.
	 */
	public void executeDefaultCaseSearch() {

		onCreate();

		setSearchData(searchService.getFilter("listActiveCaseInstances", CaseInstance.class, null));

		search();

		selectedFilternameHolder.setSelectedFilterName(CaseFilterType.ACTIVE_CASES.getFilterName());
	}

	@Override
	public String applySearchFilter(String filterType) {

		SearchArguments<CaseInstance> argumentsMap = null;
		if (CaseFilterType.ACTIVE_CASES.getFilterName().equals(filterType)) {
			argumentsMap = searchService.getFilter("listActiveCaseInstances", CaseInstance.class,
					null);
		} else if (CaseFilterType.MY_CASES.getFilterName().equals(filterType)) {
			Context<String, Object> context = new Context<>(1);
			context.put(SearchFilterProperties.USER_ID, authenticationService.getCurrentUserId());
			argumentsMap = searchService.getFilter("listCaseInstancesFromUser", CaseInstance.class,
					context);
		} else if (CaseFilterType.ALL_CASES.getFilterName().equals(filterType)) {
			argumentsMap = searchService
					.getFilter("listAllCaseInstances", CaseInstance.class, null);
		}
		if (argumentsMap != null) {
			setSearchData(argumentsMap);
		}

		return NavigationConstants.NAVIGATE_CASE_LIST_PAGE;
	}

	/**
	 * Getter method for caseFilters.
	 * 
	 * @return the caseFilters
	 */
	public List<FacetSearchFilter> getCaseFilters() {

		List<FacetSearchFilter> filters = new ArrayList<FacetSearchFilter>();
		filters.add(createFilter(CaseFilterType.ALL_CASES.getFilterName()));
		filters.add(createFilter(CaseFilterType.ACTIVE_CASES.getFilterName()));
		filters.add(createFilter(CaseFilterType.MY_CASES.getFilterName()));

		SearchFilterUpdateEvent event = new SearchFilterUpdateEvent();
		event.setFacetSearchFilters(filters);

		UpdatedSearchFilterBinding updatedSearchFilterBinding = new UpdatedSearchFilterBinding(
				UpdatedSearchFilterBinding.CASE);
		searchFilterUpdateEvent.select(updatedSearchFilterBinding).fire(event);

		caseFilters = event.getFacetSearchFilters();
		return caseFilters;
	}

	/**
	 * Filter the CL106.
	 * 
	 * @param eventObject
	 *            Event object.
	 */
	public void filterCodelist106(
			@Observes @CodelistFilter(codelist = 106, filterEvent = "removeNewAndDeleted") CodelistFiltered eventObject) {

		Map<String, CodeValue> cl = eventObject.getValues();
		// we directly modify the returned map. It's copy of the original
		// codelist so it's save to edit it
		for (Iterator<Map.Entry<String, CodeValue>> it = cl.entrySet().iterator(); it.hasNext();) {
			Entry<String, CodeValue> entry = it.next();
			if (stateService.isState(PrimaryStates.DELETED, CaseInstance.class, entry.getKey())
					|| stateService.isState(PrimaryStates.INITIAL, CaseInstance.class,
							entry.getKey())) {
				it.remove();
			}
		}
	}

	@Override
	protected String fetchResults() {
		SearchArguments<CaseInstance> searchData = getSearchData();
		// part of EGOV-3605
		wildCardSearchEntry(searchData.getArguments(), CaseProperties.UNIQUE_IDENTIFIER, '*');
		searchService.search(CaseInstance.class, searchData);
		return NavigationConstants.NAVIGATE_CASE_LIST_PAGE;
	}

	@Override
	public List<SortActionItem> getSortActions() {
		if (sortActionItems == null) {
			SortActionType[] values = SortActionType.values();
			sortActionItems = new ArrayList<SortActionItem>(values.length);
			for (SortActionType sortActionType : values) {
				SortActionItem item = new SortActionItem(sortActionType.getType(),
						sortActionType.getLabel(labelProvider));
				sortActionItems.add(item);
			}
		}

		CaseSortingActionTypesUpdateEvent event = new CaseSortingActionTypesUpdateEvent(
				sortActionItems);
		eventService.fire(event);

		return sortActionItems;
	}

	@Override
	protected Class<CaseInstance> getEntityClass() {
		return CaseInstance.class;
	}

	@Override
	protected SearchArguments<CaseInstance> initSearchData() {
		SearchArguments<CaseInstance> searchArguments = searchService.getFilter("baseCaseFilter",
				CaseInstance.class, null);
		return searchArguments;
	}

	/**
	 * Gets the primary codelist.
	 * 
	 * @return the primary codelist
	 */
	public int getStatusCodelist() {
		return stateService.getPrimaryStateCodelist(CaseInstance.class);
	}

	/**
	 * Getter method for stateService.
	 * 
	 * @return the stateService
	 */
	public StateService getStateService() {
		return stateService;
	}

	/**
	 * Setter method for stateService.
	 * 
	 * @param stateService
	 *            the stateService to set
	 */
	public void setStateService(StateService stateService) {
		this.stateService = stateService;
	}

	@Override
	public boolean canHandle(com.sirma.itt.emf.security.model.Action action) {
		String actionId = action.getActionId();
		return ActionTypeConstants.MOVE_OTHER_CASE.equals(actionId)
				|| ActionTypeConstants.LINK.equals(actionId);
	}

	@Override
	public String getSearchDataFormPath() {
		return "/search/case-search-form.xhtml";
	}

}
