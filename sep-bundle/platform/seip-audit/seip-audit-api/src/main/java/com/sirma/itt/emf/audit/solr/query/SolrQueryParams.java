package com.sirma.itt.emf.audit.solr.query;

/**
 * POJO used for solr query building.
 * 
 * @author Nikolay Velkov
 * @author Vilizar Tsonev
 */
public class SolrQueryParams {

	/** The query. */
	private String query;

	/** The filters. */
	private String[] filters;

	/** the start offset **/
	private int start;

	/** how many rows to be retrieved **/
	private int rows;

	/** The field by which the results will be sorted */
	private String sortField;
	
	/** The sort order (DESC/ASC) by which the results will be sorted */
	private String sortOrder;

	/**
	 * Gets the query.
	 * 
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query.
	 * 
	 * @param query
	 *            the new query
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Gets the filters.
	 * 
	 * @return the filters
	 */
	public String[] getFilters() {
		return filters;
	}

	/**
	 * Sets the filters.
	 * 
	 * @param filters
	 *            the new filters
	 */
	public void setFilters(String[] filters) {
		this.filters = filters;
	}

	/**
	 * Getter method for start.
	 * 
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Setter method for start.
	 * 
	 * @param start
	 *            the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * Getter method for rows.
	 * 
	 * @return the rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Setter method for rows.
	 * 
	 * @param rows
	 *            the rows to set
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * Getter method for sortField.
	 * 
	 * @return the sortField
	 */
	public String getSortField() {
		return sortField;
	}

	/**
	 * Setter method for sortField.
	 * 
	 * @param sortField
	 *            the sortField to set
	 */
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	/**
	 * Getter method for sortOrder.
	 * 
	 * @return the sortOrder
	 */
	public String getSortOrder() {
		return sortOrder;
	}

	/**
	 * Setter method for sortOrder.
	 * 
	 * @param sortOrder
	 *            the sortOrder to set
	 */
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
}
