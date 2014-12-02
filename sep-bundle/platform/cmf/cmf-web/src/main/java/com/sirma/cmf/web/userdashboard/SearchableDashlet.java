package com.sirma.cmf.web.userdashboard;

import java.util.List;

import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;

/**
 * SearchableDashlet should be implemented by all dashlets that renders some searchable content.
 * 
 * @param <E>
 *            the element type
 * @param <A>
 *            the generic type
 * @author svelikov
 */
public interface SearchableDashlet<E extends Entity, A extends SearchArguments<E>> {

	/**
	 * Sets the loaded filters.
	 * 
	 * @param filters
	 *            the new loaded filters
	 */
	void setLoadedFilters(List<SearchFilter> filters);

	/**
	 * Sets the loaded sorters.
	 * 
	 * @param sorters
	 *            the new loaded sorters
	 */
	void setLoadedSorters(List<SearchFilter> sorters);

	/**
	 * Sets the selected filter.
	 * 
	 * @param selectedFilter
	 *            the new selected filter
	 */
	void setSelectedFilter(String selectedFilter);

	/**
	 * Sets the selected sorter.
	 * 
	 * @param selectedSorter
	 *            the new selected sorter
	 */
	void setSelectedSorter(String selectedSorter);

	/**
	 * Gets the dashlet filters.
	 * 
	 * @return the dashlet filters
	 */
	List<SearchFilter> getDashletFilters();

	/**
	 * Gets the selected filter name.
	 * 
	 * @return the selected filter name
	 */
	String getSelectedFilterName();

	/**
	 * Gets the dashlet sorters.
	 * 
	 * @return the dashlet sorters
	 */
	List<SearchFilter> getDashletSorters();

	/**
	 * Checks if is order ascending.
	 * 
	 * @return true, if is order ascending
	 */
	boolean isOrderAscending();

	/**
	 * Sets the order ascending.
	 * 
	 * @param orderAscending
	 *            the new order ascending
	 */
	void setOrderAscending(boolean orderAscending);

	/**
	 * Update search context.
	 * 
	 * @param context
	 *            the context
	 */
	void updateSearchContext(Context<String, Object> context);

	/**
	 * Update search arguments.
	 * 
	 * @param searchArguments
	 *            the search arguments
	 */
	void updateSearchArguments(A searchArguments);
}
