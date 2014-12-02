package com.sirma.cmf.web.caseinstance.tab;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.concurrent.GenericAsyncTask;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.event.AfterSearchQueryBuildEvent;
import com.sirma.itt.emf.search.event.BeforeSearchQueryBuildEvent;
import com.sirma.itt.emf.search.event.SearchFilterEventBinding;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.search.model.Sorter;

/**
 * This class represent specific section content. Holds data for sorters, filters, counters and
 * content result based on solr queries. <br />
 * At the beginning for every section instance will be created {@link SectionContentLoader} object.
 * When
 * DOM is ready all created objects will be executed in <b>parallel</b>.
 * 
 * @author cdimitrov
 */
public class SectionContentLoader extends GenericAsyncTask {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8014247290140675769L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SectionContentLoader.class);
	/** The solr query parameter for sorter. */
	private static final String SOLR_PARAM_SORTER = "sorter";

	/** The solr query parameter for context URI. */
	private static final String SOLR_PARAM_CONTEXTURI = "contexturi";

	/** The supported filters for section content. */
	private List<SearchFilter> filters;

	/** The default selected filter. */
	private String selectedFilter;

	/** The default filter. */
	private String defaultFilter;

	/** The supported sorters for section content. */
	private List<SearchFilter> sorters;

	/** The selected sorter. */
	private String selectedSorter;

	/** The default sorter. */
	private String defaultSorter;

	/** The content order flag. */
	private boolean orderAscending = false;

	/** The content counter for specific section. */
	private int totalCount;

	/** The result of section content received from solr. */
	private List<Instance> result;

	/** Current section identifier, represent definition identifier. */
	private String sectionIdentifier;

	/** Search service for retrieving available section content. */
	private SearchService searchService;

	/** Event service for indicate specific step of search process. */
	private EventService eventService;

	/** The type converter. */
	private TypeConverter typeConverter;

	/**
	 * Initialize the default filter and sorter components for section.
	 * 
	 * @param searchCriteria
	 *            data loaded from the specific definition based on the section identifier
	 * @param searchService
	 *            service that will execute solr query
	 * @param eventService
	 *            service that will indicate search process steps
	 * @param typeConverter
	 *            type converter
	 */
	public SectionContentLoader(SearchFilterConfig searchCriteria, SearchService searchService,
			EventService eventService, TypeConverter typeConverter) {
		this.searchService = searchService;
		this.eventService = eventService;
		this.typeConverter = typeConverter;
		List<SearchFilter> localFilters = searchCriteria.getFilters();
		List<SearchFilter> localSorters = searchCriteria.getSorterFields();
		setFilters(localFilters);
		setSorters(localSorters);
		if ((localFilters != null) && !localFilters.isEmpty()) {
			String firstFilter = localFilters.get(0).getValue();
			setSelectedFilter(firstFilter);
			setDefaultFilter(firstFilter);
		}
		if ((localSorters != null) && !localSorters.isEmpty()) {
			String firstSorter = localSorters.get(0).getValue();
			setSelectedSorter(firstSorter);
			setDefaultSorter(firstSorter);
		}
	}

	@Override
	public void executeOnSuccess() {
		this.reinitialize();
	}

	@Override
	public void executeOnFail() {
		this.reinitialize();
	}

	@Override
	protected boolean executeTask() {
		searchCriteriaChanged();
		return true;
	}

	/**
	 * This method will populate arguments based on available components(filters, sorters), will
	 * send this arguments to the {@link SearchService} and retrieve the search result.
	 */
	public void searchCriteriaChanged() {
		List<SearchFilter> dashletFilters = getFilters();
		String selectedFilterName = getSelectedFilter();
		SearchFilter selectedSearchFilter = findSearchFilter(dashletFilters, selectedFilterName);
		if (selectedSearchFilter == null) {
			// does not execute anything if not found for some reason
			LOGGER.warn("Filter not found {}", selectedFilterName);
			return;
		}

		Context<String, Object> context = getSearchContext();

		BeforeSearchQueryBuildEvent beforeSearchQueryBuildEvent = new BeforeSearchQueryBuildEvent(
				context, selectedFilterName, selectedSearchFilter.getDefinition());
		SearchFilterEventBinding searchFilterEventBinding = new SearchFilterEventBinding(
				selectedFilterName);
		eventService.fire(beforeSearchQueryBuildEvent, searchFilterEventBinding);

		SearchArguments<Instance> searchArguments = getSearchArguments(selectedSearchFilter,
				context);

		AfterSearchQueryBuildEvent afterSearchQueryBuildEvent = new AfterSearchQueryBuildEvent(
				searchArguments, selectedFilterName);
		eventService.fire(afterSearchQueryBuildEvent, searchFilterEventBinding);

		searchService.search(Instance.class, searchArguments);
		setResult(searchArguments.getResult());
		setTotalCount(searchArguments.getTotalItems());
	}

	/**
	 * Building solr search arguments.
	 * 
	 * @param filter
	 *            selected filter
	 * @param context
	 *            the combined arguments between selected sorter and section URI
	 * @return generated search arguments
	 */
	private SearchArguments<Instance> getSearchArguments(SearchFilter filter,
			Context<String, Object> context) {
		SearchArguments<Instance> arguments = searchService.buildSearchArguments(filter,
				SearchInstance.class, context);
		return arguments;
	}

	/**
	 * Context data(selected sorter and section URI).
	 * 
	 * @return searched context
	 */
	private Context<String, Object> getSearchContext() {
		Context<String, Object> context = new Context<String, Object>();
		context.put(SOLR_PARAM_SORTER, new Sorter(getSelectedSorter(), isOrderAscending()));
		context.put(SOLR_PARAM_CONTEXTURI, typeConverter.convert(Uri.class, sectionIdentifier));
		return context;
	}

	/**
	 * Getter for found data after solr query execution.
	 * 
	 * @return list with instances(section content)
	 */
	public List<Instance> getResult() {
		return result;
	}

	/**
	 * Setter for currently found result.
	 * 
	 * @param result
	 *            list with instances(section content)
	 */
	public void setResult(List<Instance> result) {
		this.result = result;
	}

	/**
	 * Retrieve the filter that is selected.
	 * 
	 * @param list
	 *            list with supported filters
	 * @param target
	 *            filter value that is searching
	 * @return filter, on success
	 */
	private SearchFilter findSearchFilter(List<SearchFilter> list, String target) {
		for (SearchFilter item : list) {
			if (item.getValue().equals(target)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Check for selected filter is currently default.
	 * 
	 * @param selected
	 *            selected filter
	 * @param filter
	 *            default filter
	 * @return true on success
	 */
	public boolean isDefaultFilter(String selected, String filter) {
		if ((selected != null) && (filter != null)) {
			return selected.equals(filter);
		}
		return false;
	}

	/**
	 * Changing the order of section content.
	 */
	public void changeOrder() {
		boolean orderAscending = isOrderAscending();
		setOrderAscending(!orderAscending);
		searchCriteriaChanged();
	}

	/**
	 * Getter for section identifier.
	 * 
	 * @return section identifier
	 */
	public String getSectionIdentifier() {
		return sectionIdentifier;
	}

	/**
	 * Setter for section identifier.
	 * 
	 * @param sectionIdentifier
	 *            current section identifier
	 */
	public void setSectionIdentifier(String sectionIdentifier) {
		this.sectionIdentifier = sectionIdentifier;
	}

	/**
	 * Getter for support filters.
	 * 
	 * @return list with filter
	 */
	public List<SearchFilter> getFilters() {
		return filters;
	}

	/**
	 * Setter for supporter filter.
	 * 
	 * @param filters
	 *            supported filters
	 */
	public void setFilters(List<SearchFilter> filters) {
		this.filters = filters;
	}

	/**
	 * Getter for selected filter.
	 * 
	 * @return selected filter
	 */
	public String getSelectedFilter() {
		return selectedFilter;
	}

	/**
	 * Setter for selected filter.
	 * 
	 * @param selectedFilter
	 *            selected filter
	 */
	public void setSelectedFilter(String selectedFilter) {
		this.selectedFilter = selectedFilter;
	}

	/**
	 * Getter for default filter.
	 * 
	 * @return default filter
	 */
	public String getDefaultFilter() {
		return defaultFilter;
	}

	/**
	 * Setter for default filter.
	 * 
	 * @param defaultFilter
	 *            default filter
	 */
	public void setDefaultFilter(String defaultFilter) {
		this.defaultFilter = defaultFilter;
	}

	/**
	 * Getter for supported sorters.
	 * 
	 * @return list with supported sorters
	 */
	public List<SearchFilter> getSorters() {
		return sorters;
	}

	/**
	 * Setter for supported sorters.
	 * 
	 * @param sorters
	 *            supported sorters
	 */
	public void setSorters(List<SearchFilter> sorters) {
		this.sorters = sorters;
	}

	/**
	 * Getter for selected sorter.
	 * 
	 * @return selected sorter
	 */
	public String getSelectedSorter() {
		return selectedSorter;
	}

	/**
	 * Setter for selected sorter.
	 * 
	 * @param selectedSorter
	 *            selected sorter
	 */
	public void setSelectedSorter(String selectedSorter) {
		this.selectedSorter = selectedSorter;
	}

	/**
	 * Getter for default sorter.
	 * 
	 * @return default sorter
	 */
	public String getDefaultSorter() {
		return defaultSorter;
	}

	/**
	 * Setter for default sorter.
	 * 
	 * @param defaultSorter
	 *            default sorter.
	 */
	public void setDefaultSorter(String defaultSorter) {
		this.defaultSorter = defaultSorter;
	}

	/**
	 * Check for ascending order of section content.
	 * 
	 * @return true if order is ascending
	 */
	public boolean isOrderAscending() {
		return orderAscending;
	}

	/**
	 * Setter for ascending order of section content.
	 * 
	 * @param orderAscending
	 *            ascending order flag
	 */
	public void setOrderAscending(boolean orderAscending) {
		this.orderAscending = orderAscending;
	}

	/**
	 * Getter for total section content counter.
	 * 
	 * @return total section content counter
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * Setter for total section content counter.
	 * 
	 * @param totalCount
	 *            total counter
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

}
