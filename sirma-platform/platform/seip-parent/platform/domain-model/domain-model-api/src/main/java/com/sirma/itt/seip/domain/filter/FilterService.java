package com.sirma.itt.seip.domain.filter;

import java.util.List;
import java.util.Set;

/**
 * Service for managing filters
 *
 * @author BBonev
 */
public interface FilterService {

	/**
	 * Gets a filter for given id.
	 *
	 * @param filterId
	 *            the filter id
	 * @return the label
	 */
	Filter getFilter(String filterId);

	/**
	 * Saves all filters.
	 *
	 * @param filters
	 *            the filter definitions
	 * @return true, if successful
	 */
	boolean saveFilters(List<Filter> filters);

	/**
	 * Filter the given values based on the given filer
	 *
	 * @param filter
	 *            the filter to use
	 * @param toFilter
	 *            the data to filter
	 */
	void filter(Filter filter, Set<String> toFilter);

	/**
	 * Filter the given values based on the list of filters
	 *
	 * @param filters
	 *            the filters to use
	 * @param toFilter
	 *            the to filter
	 */
	void filter(List<Filter> filters, Set<String> toFilter);

}
