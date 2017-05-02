package com.sirma.cmf.web.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;
import org.richfaces.function.RichFunction;

import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.SelectorItem;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.instance.landingpage.InstanceItemSelector;
import com.sirma.cmf.web.menu.NavigationMenuAction;
import com.sirma.cmf.web.search.facet.FacetSearchFilter;
import com.sirma.cmf.web.search.facet.SearchArgumentsUpdaterExtension;
import com.sirma.cmf.web.search.facet.event.FacetEvent;
import com.sirma.cmf.web.search.facet.event.FacetEventTypeBinding;
import com.sirma.cmf.web.search.sort.SortActionItem;
import com.sirma.itt.cmf.constants.CmfConfigurationProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;
import com.sirma.itt.seip.search.SearchConfiguration;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Abstraction for classes backing search pages.
 *
 * @param <E>
 *            type of the returned results.
 * @param <A>
 *            the generic type
 * @author svelikov
 */
public abstract class SearchActionBase<E extends Entity, A extends SearchArguments<E>> extends EntityAction
		implements InstanceItemSelector {

	/** The case defintion cl. */
	@Inject
	private CmfConfigurationProperties configurationProperties;

	@Inject
	private SearchConfiguration searchConfiguration;

	/** The facet event. */
	@Inject
	protected Event<FacetEvent> facetEvent;

	/** The search service. */
	@Inject
	protected SearchService searchService;

	/** The navigation menu action. */
	@Inject
	private NavigationMenuAction navigationMenuAction;

	/** Registered context initializers. */
	private Map<Class, SearchArgumentsUpdaterExtension<E>> searchArgumentUpdaters;

	/** The extension. */
	@Inject
	@ExtensionPoint(value = SearchArgumentsUpdaterExtension.TARGET_NAME)
	private Iterable<SearchArgumentsUpdaterExtension<E>> extension;

	/** The sort order ascending. */
	protected boolean sortOrderAscending;

	/** The search data. */
	private A searchData;

	/**
	 * DataModel used by the underlying dataTable for displaying search results.
	 */
	private PagedListDataModel<E> dataModel;

	/** The selected sorter type. */
	private String selectedType;

	/** If the search should be execute in current context. */
	private boolean searchInContext = true;

	/**
	 * Called when the search action is created. Initializes the search data and sets common properties.
	 */
	public void onCreate() {

		// initialize searchData
		A searchDataInit = initSearchData();
		setSearchData(searchDataInit);

		dataModel = new PagedListDataModel<>(null, 0, searchConfiguration.getPagerPageSize());
		searchDataInit.setPageSize(searchConfiguration.getPagerPageSize());
		searchDataInit.setPageNumber(1);

		searchArgumentUpdaters = PluginUtil.parseSupportedObjects(extension, false);
	}

	/**
	 * Update search arguments.
	 */
	public void updateSearchArguments() {
		Instance contextInstance = null;
		if (searchInContext) {
			contextInstance = getDocumentContext().getRootInstance();
		}
		if (contextInstance != null) {
			log.debug("Searching in context instance[" + contextInstance.getClass().getSimpleName() + "] with id["
					+ contextInstance.getId() + "]");
			SearchArgumentsUpdaterExtension<E> updater = searchArgumentUpdaters.get(contextInstance.getClass());
			if (updater != null) {
				updater.updateArguments(getSearchData());
			}
		}
	}

	/**
	 * Performs a search by fetching search results and updating the data model.
	 *
	 * @return Navigation string.
	 */
	public String search() {
		TimeTracker timer = TimeTracker.createAndStart();
		A searchDataInit = getSearchData();
		searchDataInit.setTotalItems(0);
		searchDataInit.setPageNumber(1);
		searchDataInit.setPageSize(searchConfiguration.getPagerPageSize());

		String navigation = invokeSearch();

		if (searchDataInit.getTotalItems() == 0) {
			dataModel = new PagedListDataModel<>(null, 0, searchConfiguration.getPagerPageSize());
		} else {
			updateDataModel();
		}

		dataModel.setTotalNumRows(searchDataInit.getTotalItems());

		navigationMenuAction.setSelectedMenu(NavigationConstants.NAVIGATE_MENU_CASE_LIST);
		log.debug("Search execution took " + timer.stopInSeconds() + " s");
		return navigation;
	}

	/**
	 * Filter.
	 *
	 * @param facetSearchFilter
	 *            the facet search filter
	 * @return Navigation string
	 */
	public String filter(FacetSearchFilter facetSearchFilter) {

		getDocumentContext().clearAndLeaveContext();

		String filterType = facetSearchFilter.getFilterType();

		log.debug("CMFWeb: Executing facet search filter: type[" + filterType + "]");

		onCreate();

		String navigation = applySearchFilter(filterType);

		search();

		FacetEvent event = new FacetEvent(facetSearchFilter.getFilterType());
		facetEvent.select(new FacetEventTypeBinding(getEntityClass().getSimpleName())).fire(event);

		log.debug("CMFWeb: Found [" + getSearchData().getResult().size() + "] results from search filter");

		return navigation;
	}

	/**
	 * Performs sorting with provided sorter and the available search arguments.
	 *
	 * @param sortActionType
	 *            The SortAction chosen by the user.
	 * @return Navigation string.
	 */
	public String sort(SortActionItem sortActionType) {

		Sorter sorter = new Sorter(sortActionType.getType(), Sorter.SORT_DESCENDING);

		setSortOrderAscending(false);

		getSearchData().addSorter(sorter);
		getSearchData().setSkipCount(0);
		if (getSearchData().getPageNumber() > 1) {
			getSearchData().setSkipCount(getSearchData().getPageSize() * (getSearchData().getPageNumber() - 1));
		}
		invokeSearch();

		updateDataModel();

		return NavigationConstants.RELOAD_PAGE;
	}

	/**
	 * Gets the sort actions.
	 *
	 * @return the sort actions
	 */
	@SessionScoped
	public List<SortActionItem> getSortActions() {
		return Collections.emptyList();
	}

	/**
	 * Switch result order.
	 *
	 * @return navigation string
	 */
	public String switchOrder() {

		toggleSortingOrder();

		getSearchData().getFirstSorter().setAscendingOrder(isSortOrderAscending());
		getSearchData().setSkipCount(0);
		if (getSearchData().getPageNumber() > 1) {
			getSearchData().setSkipCount(getSearchData().getPageSize() * (getSearchData().getPageNumber() - 1));
		}
		invokeSearch();

		updateDataModel();

		dataModel.setTotalNumRows(getSearchData().getTotalItems());

		return NavigationConstants.RELOAD_PAGE;
	}

	/**
	 * Invert the sort order.
	 */
	public void toggleSortingOrder() {
		sortOrderAscending = !sortOrderAscending;
	}

	/**
	 * Called when a new page is selected from the page scroller.
	 *
	 * @param event
	 *            DataScrollerEvent
	 */
	public void onScroll(DataScrollEvent event) {

		getSearchData().setPageNumber(event.getPage());
		getSearchData().setSkipCount(0);
		if (getSearchData().getPageNumber() > 1) {
			getSearchData().setSkipCount(getSearchData().getPageSize() * (getSearchData().getPageNumber() - 1));
		}
		invokeSearch();

		updateDataModel();
	}

	/**
	 * Action method for result item selection.
	 *
	 * @param selectedInstance
	 *            the selected instance
	 * @param id
	 *            Result id
	 * @return navigation rule to the result item preview page.
	 */
	public String previewSearchResult(Class<?> selectedInstance, Long id) {
		dataModel = null;
		return NavigationConstants.NAVIGATE_TAB_CASE_DETAILS;
	}

	/**
	 * Apply search filter.
	 *
	 * @param filterType
	 *            the filter type
	 * @return the string
	 */
	public abstract String applySearchFilter(String filterType);

	/**
	 * Updates the data underlying list in the dataModel.
	 */
	protected void updateDataModel() {
		dataModel.setData(getSearchData().getResult());
	}

	/**
	 * Load case definitions.
	 *
	 * @return the list
	 */
	public List<SelectorItem> loadCaseDefinitions() {
		List<SelectorItem> items = new ArrayList<>();

		Map<String, CodeValue> codeValues = codelistService
				.getCodeValues(configurationProperties.getCaseTypeCodelist().get(), true);

		for (CodeValue codeValue : codeValues.values()) {
			String descr = codelistService.getDescription(codeValue);

			items.add(new SelectorItem(codeValue.getValue(), codeValue.getValue(), descr));
		}

		return items;
	}

	/**
	 * Gets the entry from the map and if it is string add wildcards at the beginning and end of value, if not already
	 * set.
	 *
	 * @param props
	 *            are the properties to update
	 * @param key
	 *            the key to update value for
	 * @param wildCard
	 *            the wildcard to add - '*', '?'
	 */
	protected void wildCardSearchEntry(Map<String, Serializable> props, String key, char wildCard) {
		Serializable entry = props.get(key);
		if (entry instanceof String && StringUtils.isNotNull(entry.toString())) {
			if (entry.toString().indexOf(wildCard) == 0) {
				// skip
			} else {
				entry = wildCard + entry.toString();
			}
			if (entry.toString().lastIndexOf(wildCard) == entry.toString().length() - 1) {
				// skip
			} else {
				entry = entry.toString() + wildCard;
			}
			props.put(key, entry);
		}
	}

	/**
	 * Invoke search.
	 *
	 * @return the string
	 */
	public String invokeSearch() {
		updateSearchArguments();
		return fetchResults();
	}

	@Override
	public void itemSelectedAction() {
		// not used
	}

	@Override
	public void itemSelectedAction(String componentId) {
		// get value of the select component from the request map because if
		// nasty bug related with
		// immediate attribute not working on selectOne tag
		UIInput component = (UIInput) RichFunction.findComponent(componentId);
		String submittedValue = FacesContext
				.getCurrentInstance()
					.getExternalContext()
					.getRequestParameterMap()
					.get(component.getClientId());
		setSelectedType(submittedValue);
		log.debug("CMFWeb: SearchActionBase sorting by [" + submittedValue + "]");

		String selectedItem = getSelectedType();
		SortActionItem sortActionItem = new SortActionItem(selectedItem, null);
		sort(sortActionItem);
	}

	/**
	 * Fetches the search results from the underlying data source.
	 *
	 * @return navigation string
	 */
	protected abstract String fetchResults();

	/**
	 * Initializes the search data object. Call setSearchData();
	 *
	 * @return initiated data.
	 */
	protected abstract A initSearchData();

	/**
	 * Must be implemented to return the concrete class for the current entity.
	 *
	 * @return Concrete object class
	 */
	protected abstract Class<E> getEntityClass();

	/**
	 * Getter method for searchData.
	 *
	 * @return the searchData
	 */
	public A getSearchData() {
		return searchData;
	}

	/**
	 * Setter method for searchData.
	 *
	 * @param searchData
	 *            the searchData to set
	 */
	public void setSearchData(A searchData) {
		this.searchData = searchData;
	}

	/**
	 * Getter method for dataModel.
	 *
	 * @return the dataModel
	 */
	public PagedListDataModel<E> getDataModel() {
		return dataModel;
	}

	/**
	 * Setter method for dataModel.
	 *
	 * @param dataModel
	 *            the dataModel to set
	 */
	public void setDataModel(PagedListDataModel<E> dataModel) {
		this.dataModel = dataModel;
	}

	/**
	 * Getter method for currentPageNumber.
	 *
	 * @return the currentPageNumber
	 */
	public Integer getCurrentPageNumber() {
		return Integer.valueOf(searchData.getPageNumber());
	}

	/**
	 * Setter method for currentPageNumber.
	 *
	 * @param currentPageNumber
	 *            the currentPageNumber to set
	 */
	public void setCurrentPageNumber(Integer currentPageNumber) {
		searchData.setPageNumber(currentPageNumber.intValue());
	}

	/**
	 * Getter method for sortOrderAscending.
	 *
	 * @return the sortOrderAscending
	 */
	public boolean isSortOrderAscending() {
		return sortOrderAscending;
	}

	/**
	 * Setter method for sortOrderAscending.
	 *
	 * @param sortOrderAscending
	 *            the sortOrderAscending to set
	 */
	public void setSortOrderAscending(boolean sortOrderAscending) {
		this.sortOrderAscending = sortOrderAscending;
	}

	/**
	 * Getter method for maxPagesInPager.
	 *
	 * @return the maxPagesInPager
	 */
	public int getMaxPagesInPager() {
		return searchConfiguration.getPagerMaxPages();
	}

	/**
	 * Getter method for pageSize.
	 *
	 * @return the pageSize
	 */
	public int getPageSize() {
		return searchConfiguration.getPagerPageSize();
	}

	/**
	 * Getter method for selectedType.
	 *
	 * @return the selectedType
	 */
	public String getSelectedType() {
		return selectedType;
	}

	/**
	 * Setter method for selectedType.
	 *
	 * @param selectedType
	 *            the selectedType to set
	 */
	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}

	/**
	 * Getter method for searchInContext.
	 *
	 * @return the searchInContext
	 */
	public boolean isSearchInContext() {
		return searchInContext;
	}

	/**
	 * Setter method for searchInContext.
	 *
	 * @param searchInContext
	 *            the searchInContext to set
	 */
	public void setSearchInContext(boolean searchInContext) {
		this.searchInContext = searchInContext;
	}

}