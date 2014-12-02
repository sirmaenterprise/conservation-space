package com.sirma.cmf.web.search;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * The Class SearchTypeSelectedEvent. Fired when an item is selected from the menu that holds the
 * search types. Allows some initialization work to be performed before the search arguments page to
 * be loaded (for example - initializing the search arguments).
 */
@Documentation("The Class SearchTypeSelectedEvent. Fired when an item is selected from the menu that holds the search types. Allows some initialization work to be performed before the search arguments page to be loaded (for example - initializing the search arguments).")
public class SearchTypeSelectedEvent implements EmfEvent {

	/** The search page type. */
	private String searchPageType;

	/**
	 * Instantiates a new search page initialize event.
	 * 
	 * @param searchPageType
	 *            the search page type
	 */
	public SearchTypeSelectedEvent(String searchPageType) {
		this.searchPageType = searchPageType;
	}

	/**
	 * Getter method for searchPageType.
	 * 
	 * @return the searchPageType
	 */
	public String getSearchPageType() {
		return searchPageType;
	}

	/**
	 * Setter method for searchPageType.
	 * 
	 * @param searchPageType
	 *            the searchPageType to set
	 */
	public void setSearchPageType(String searchPageType) {
		this.searchPageType = searchPageType;
	}

}
