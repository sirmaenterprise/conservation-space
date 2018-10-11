package com.sirma.itt.seip.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a bean containing information about all needed search form configurations
 * 
 * @author bbanchev
 */
@JsonTypeName(value = "config")
public class SearchConfiguration {
	private SearchOrder order;

	/**
	 * Gets the order configuration.
	 *
	 * @return the order
	 */
	@JsonProperty
	public SearchOrder getOrder() {
		return order;
	}

	/**
	 * Sets the order configuration.
	 *
	 * @param order
	 *            the new order
	 */
	public void setOrder(SearchOrder order) {
		this.order = order;
	}

}
