package com.sirma.itt.emf.audit.solr.query;

import java.util.List;

/**
 * Wraps the paged result IDs returned from the Solr service and the total amount of results (numFound).
 *
 * @author Vilizar Tsonev
 */
public class SolrResult {

	private List<Long> ids;

	private long total;

	/**
	 * Getter method for ids.
	 *
	 * @return the ids
	 */
	public List<Long> getIds() {
		return ids;
	}

	/**
	 * Setter method for ids.
	 *
	 * @param ids
	 *            the ids to set
	 */
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	/**
	 * Getter method for total.
	 *
	 * @return the total
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * Setter method for total.
	 *
	 * @param total
	 *            the total to set
	 */
	public void setTotal(long total) {
		this.total = total;
	}
}
