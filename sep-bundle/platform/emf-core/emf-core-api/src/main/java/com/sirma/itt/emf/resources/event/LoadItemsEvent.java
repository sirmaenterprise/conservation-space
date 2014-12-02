package com.sirma.itt.emf.resources.event;

import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * The Class LoadItemsEvent fired when a form in edit mode is opened by the user and in the form
 * definition there is a picklist control defined. This event allows the full list with items to be
 * filtered using some predefined criteria provided in the definition. This event can be caught in
 * third party application.
 * 
 * @author svelikov
 */
@Documentation("The Class LoadItemsEvent fired when a form in edit mode is opened by the user and in the form definition there is a picklist control defined. This event allows the full list with items to be filtered using some predefined criteria provided in the definition. This event can be caught in third party application.")
// TODO: think about abstracting - for now this event can carry the items only
public class LoadItemsEvent implements EmfEvent {

	/** Observers should set this variable to be true if the event is handled. */
	private boolean isHandled;

	/** The keywords map contains keyword lists mapped to keys. */
	private Map<String, Object> keywords;

	/** The items. */
	private List<?> items;

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

	/**
	 * Getter method for isHandled.
	 * 
	 * @return the isHandled
	 */
	public boolean isHandled() {
		return isHandled;
	}

	/**
	 * Setter method for isHandled.
	 * 
	 * @param isHandled
	 *            the isHandled to set
	 */
	public void setHandled(boolean isHandled) {
		this.isHandled = isHandled;
	}

}
