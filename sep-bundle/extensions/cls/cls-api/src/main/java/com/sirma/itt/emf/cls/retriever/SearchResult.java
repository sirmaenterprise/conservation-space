package com.sirma.itt.emf.cls.retriever;

import java.util.List;

import com.sirma.itt.emf.cls.entity.Code;

/**
 * POJO wrapping the list with paginated search results, total result count, results offset and
 * results limit.
 * 
 * @author Mihail Radkov
 */
public class SearchResult {

	private int total;

	private int offset;

	private int limit;

	private List<? extends Code> results;

	/**
	 * Getter method for results.
	 * 
	 * @return the results
	 */
	public List<? extends Code> getResults() {
		return results;
	}

	/**
	 * Setter method for results.
	 * 
	 * @param results
	 *            the results to set
	 */
	public void setResults(List<? extends Code> results) {
		this.results = results;
	}

	/**
	 * Gets the total.
	 * 
	 * @return the total
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * Sets the total.
	 * 
	 * @param total
	 *            the new total
	 */
	public void setTotal(int total) {
		this.total = total;
	}

	/**
	 * Gets the offset.
	 * 
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset.
	 * 
	 * @param offset
	 *            the new offset
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Gets the limit.
	 * 
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Sets the limit.
	 * 
	 * @param limit
	 *            the new limit
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

}
