package com.sirma.cmf.web.search.sort;

import java.util.List;

import com.sirma.itt.emf.event.EmfEvent;

/**
 * Event fired after the sorting actions are requested and build in the CMF to allow clients to
 * contribute to the sorting options.
 * 
 * @author svelikov
 */
public class CaseSortingActionTypesUpdateEvent implements EmfEvent {

	/** The sort action items. */
	private List<SortActionItem> sortActionItems;

	/**
	 * Instantiates a new sorting action types update event.
	 * 
	 * @param sortActionItemsList
	 *            the sort action items list
	 */
	public CaseSortingActionTypesUpdateEvent(List<SortActionItem> sortActionItemsList) {
		this.sortActionItems = sortActionItemsList;
	}

	/**
	 * Getter method for sortActionItems.
	 * 
	 * @return the sortActionItems
	 */
	public List<SortActionItem> getSortActionItems() {
		return sortActionItems;
	}

	/**
	 * Setter method for sortActionItems.
	 * 
	 * @param sortActionItems
	 *            the sortActionItems to set
	 */
	public void setSortActionItems(List<SortActionItem> sortActionItems) {
		this.sortActionItems = sortActionItems;
	}
}
