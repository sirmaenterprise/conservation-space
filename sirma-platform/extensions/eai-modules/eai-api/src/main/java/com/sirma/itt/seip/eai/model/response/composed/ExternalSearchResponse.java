package com.sirma.itt.seip.eai.model.response.composed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.ServiceResponse;

/**
 * The external search response json<->java binding. Contains the retrieved items as separate list and the related items
 * as {@link #getRelated()}
 */
public class ExternalSearchResponse implements ServiceResponse {
	@JsonProperty(value = "items", required = true)
	private List<ResultItem> items;
	@JsonProperty(value = "related")
	private List<ResultItem> related;

	/**
	 * Getter method for items.
	 *
	 * @return the items
	 */
	public List<ResultItem> getItems() {
		return items;
	}

	/**
	 * Setter method for items.
	 *
	 * @param items
	 *            the items to set
	 */
	public void setItems(List<ResultItem> items) {
		this.items = items;
	}

	/**
	 * Getter method for related.
	 *
	 * @return the related
	 */
	public List<ResultItem> getRelated() {
		return related;
	}

	/**
	 * Setter method for related.
	 *
	 * @param related
	 *            the related to set
	 */
	public void setRelated(List<ResultItem> related) {
		this.related = related;
	}

}
