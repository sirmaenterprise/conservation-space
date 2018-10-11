package com.sirma.itt.seip.rest.models;

import java.util.List;

/**
 * Wrapper object for search response. Contains pagination information and the
 * actual results.
 *
 * @author yasko
 *
 * @param <T>
 *            Type of the results.
 */
public class SearchResponseWrapper<T> {

	private int limit;
	private int offset;
	private long total;
	private List<T> results;

	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the search response offset.
	 *
	 * @param offset
	 *            the offset
	 * @return current instance to allow method chaining
	 */
	public SearchResponseWrapper<T> setOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public int getLimit() {
		return limit;
	}

	/**
	 * Sets the search response limit.
	 *
	 * @param limit
	 *            the limit
	 * @return current instance to allow method chaining
	 */
	public SearchResponseWrapper<T> setLimit(int limit) {
		this.limit = limit;
		return this;
	}

	public long getTotal() {
		return total;
	}

	/**
	 * Sets the total found items
	 *
	 * @param total
	 *            the total
	 * @return current instance to allow method chaining
	 */
	public SearchResponseWrapper<T> setTotal(long total) {
		this.total = total;
		return this;
	}

	public List<T> getResults() {
		return results;
	}

	/**
	 * Sets the found results restricted by the offset and the limit
	 *
	 * @param results
	 *            the results
	 * @return current instance to allow method chaining
	 */
	public SearchResponseWrapper<T> setResults(List<T> results) {
		this.results = results;
		return this;
	}

}
