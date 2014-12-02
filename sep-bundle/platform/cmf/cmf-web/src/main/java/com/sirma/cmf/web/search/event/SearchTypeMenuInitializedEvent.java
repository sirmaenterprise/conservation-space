package com.sirma.cmf.web.search.event;

import java.util.List;

import com.sirma.cmf.web.search.SearchPage;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * SearchTypeMenuInitializedEvent fired when the search page is selected trough the application
 * menu. Allows the dropdown menu to be initialized with different values or to be filtered.
 * 
 * @author svelikov
 */
@Documentation("SearchTypeMenuInitializedEvent fired when the search page is selected trough the application menu. Allows the dropdown menu to be initialized with different values or to be filtered.")
public class SearchTypeMenuInitializedEvent implements EmfEvent {

	/** The search pages. */
	private List<SearchPage> searchPages;

	/**
	 * Instantiates a new search page initialize event.
	 * 
	 * @param searchPages
	 *            the search pages
	 */
	public SearchTypeMenuInitializedEvent(List<SearchPage> searchPages) {
		this.searchPages = searchPages;
	}

	/**
	 * Getter method for searchPages.
	 * 
	 * @return the searchPages
	 */
	public List<SearchPage> getSearchPages() {
		return searchPages;
	}

	/**
	 * Setter method for searchPages.
	 * 
	 * @param searchPages
	 *            the searchPages to set
	 */
	public void setSearchPages(List<SearchPage> searchPages) {
		this.searchPages = searchPages;
	}

}
