package com.sirma.sep.export.xlsx;

import javax.json.JsonObject;

/**
 * Holds the information needed to execute search and retrieve the objects that should be exported it the excel.
 *
 * @author A. Kunchev
 */
public class SearchData {

	private final JsonObject searchCriteria;
	private final String orderBy;
	private final String orderDirection;

	/**
	 * Instantiates new search data.
	 *
	 * @param searchCriteria
	 *            the criteria used in the search to find the object that should be exported
	 * @param orderBy
	 *            clause that should be used in the search for sorting the found results
	 * @param orderDirection
	 *            the direction of the sorting for the found results
	 */
	SearchData(final JsonObject searchCriteria, final String orderBy, final String orderDirection) {
		this.searchCriteria = searchCriteria;
		this.orderBy = orderBy;
		this.orderDirection = orderDirection;
	}

	/**
	 * Gets the criteria as Json object with which could be executed search.
	 *
	 * @return search criteria as Json object
	 */
	public JsonObject getSearchCriteria() {
		return searchCriteria;
	}

	/**
	 * Gets order by configuration.
	 *
	 * @return property name which is used for data ordering
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * Gets the order direction.
	 *
	 * @return order direction "asc" or "dsc"
	 */
	public String getOrderDirection() {
		return orderDirection;
	}

}
