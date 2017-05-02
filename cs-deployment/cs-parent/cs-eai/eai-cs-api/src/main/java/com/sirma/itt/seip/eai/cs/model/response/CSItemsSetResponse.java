package com.sirma.itt.seip.eai.cs.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.cs.model.CSResultItem;
import com.sirma.itt.seip.eai.model.ResultPaging;
import com.sirma.itt.seip.eai.model.ServiceResponse;

/**
 * The {@link CSItemsSetResponse} json<->java binding.
 */
public class CSItemsSetResponse implements ServiceResponse {
	@JsonProperty(value = "items")
	private List<CSResultItem> items;
	@JsonProperty(value = "paging")
	private ResultPaging paging;

	/**
	 * Getter method for items.
	 *
	 * @return the items
	 */
	public List<CSResultItem> getItems() {
		return items;
	}

	/**
	 * Setter method for items.
	 *
	 * @param items
	 *            the items to set
	 */
	public void setItems(List<CSResultItem> items) {
		this.items = items;
	}

	/**
	 * Gets the paging.
	 *
	 * @return the paging
	 */
	public ResultPaging getPaging() {
		return paging;
	}

	/**
	 * Sets the paging.
	 *
	 * @param paging
	 *            the new paging
	 */
	public void setPaging(ResultPaging paging) {
		this.paging = paging;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + ((paging == null) ? 0 : paging.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CSItemsSetResponse)) {
			return false;
		}
		CSItemsSetResponse other = (CSItemsSetResponse) obj;
		if (items == null) {
			if (other.items != null) {
				return false;
			}
		} else if (!items.equals(other.items)) {
			return false;
		}
		if (paging == null) {
			if (other.paging != null) {
				return false;
			}
		} else if (!paging.equals(other.paging)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append("[items=");
		builder.append(items);
		builder.append(", paging=");
		builder.append(paging);
		builder.append("]");
		return builder.toString();
	}
}
