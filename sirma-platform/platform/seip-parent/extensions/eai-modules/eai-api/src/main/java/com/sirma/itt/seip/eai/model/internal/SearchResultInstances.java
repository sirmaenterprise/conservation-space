package com.sirma.itt.seip.eai.model.internal;

import com.sirma.itt.seip.eai.model.ResultPaging;

/**
 * The internal model representation used as an abstraction layer between invocation service and search service.
 * Represents a list of items and a search paging data.
 * 
 * @param <E>
 *            the element type for result
 * @author bbanchev
 */
public class SearchResultInstances<E> extends BatchProcessedInstancesModel<E> {
	private ResultPaging paging;

	/**
	 * Gets the paging for the search result.
	 *
	 * @return the paging
	 */
	public ResultPaging getPaging() {
		return paging;
	}

	/**
	 * Sets the result paging.
	 *
	 * @param paging
	 *            the new paging
	 */
	public void setPaging(ResultPaging paging) {
		this.paging = paging;
	}
}
