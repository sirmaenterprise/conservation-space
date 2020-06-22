package com.sirma.itt.seip.search;

/**
 * The Interface SearchConfiguration.
 *
 * @author BBonev
 */
public interface SearchConfiguration {

	/**
	 * Gets the pager page size often 25
	 *
	 * @return the pager page size
	 */
	int getPagerPageSize();

	/**
	 * Gets the search result max size processed elements often 1000
	 *
	 * @return the search result max size
	 */
	Integer getSearchResultMaxSize();

	/**
	 * Gets the number of pages to render in a pager. often 5
	 *
	 * @return the pager max pages
	 */
	int getPagerMaxPages();

	/**
	 * Gets the dashlet page size often 15-25
	 *
	 * @return the dashlet page size
	 */
	Integer getDashletPageSize();

	/**
	 * Gets the search facet result exceed disable.
	 *
	 * @return the search facet result exceed disable
	 */
	Boolean getSearchFacetResultExceedDisable();

	/**
	 * Specifies if the search context should be updated on view change.
	 *
	 * @return true if it should be updated or false otherwise
	 */
	Boolean updateSearchContext();

}
