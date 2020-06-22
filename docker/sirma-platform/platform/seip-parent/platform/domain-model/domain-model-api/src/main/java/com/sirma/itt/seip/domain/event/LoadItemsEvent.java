package com.sirma.itt.seip.domain.event;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.annotation.Documentation;

/**
 * The Class LoadItemsEvent fired when a form in edit mode is opened by the user and in the form definition there is a
 * picklist control defined. This event allows the full list with items to be filtered using some predefined criteria
 * provided in the definition. This event can be caught in third party application.
 *
 * @author svelikov
 */
@Documentation("The Class LoadItemsEvent fired when a form in edit mode is opened by the user and in the form definition there is a picklist control defined. This event allows the full list with items to be filtered using some predefined criteria provided in the definition. This event can be caught in third party application.")
// TODO: think about abstracting - for now this event can carry the items only
public class LoadItemsEvent implements HandledEvent {

	/** Observers should set this variable to be true if the event is handled. */
	private boolean isHandled;

	/** The keywords map contains keyword lists mapped to keys. */
	private Map<String, Object> keywords;

	private List<?> items;

	private String type;

	private String sortBy;

	/**
	 * Instantiates a new load items event.
	 */
	public LoadItemsEvent() {
		items = new LinkedList<Serializable>();
	}

	/**
	 * Getter method for items.
	 *
	 * @return the items
	 */
	public List<?> getItems() {
		return items;
	}

	/**
	 * Setter method for items.
	 *
	 * @param items
	 *            the items to set
	 */
	public void setItems(List<?> items) {
		this.items = items;
	}

	/**
	 * Getter method for keywords.
	 *
	 * @return the keywords
	 */
	public Map<String, Object> getKeywords() {
		return keywords;
	}

	/**
	 * Setter method for keywords.
	 *
	 * @param keywords
	 *            the keywords to set
	 */
	public void setKeywords(Map<String, Object> keywords) {
		this.keywords = keywords;
	}

	@Override
	public boolean isHandled() {
		return isHandled;
	}

	@Override
	public void setHandled(boolean isHandled) {
		this.isHandled = isHandled;
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for sortBy.
	 *
	 * @return the sortBy
	 */
	public String getSortBy() {
		return sortBy;
	}

	/**
	 * Setter method for sortBy.
	 *
	 * @param sortBy
	 *            the sortBy to set
	 */
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

}
