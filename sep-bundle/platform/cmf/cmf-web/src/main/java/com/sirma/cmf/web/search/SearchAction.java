package com.sirma.cmf.web.search;

/**
 * The Interface SearchAction. This is a marker interface that should be implemented from all search
 * action classes that are involved in searching in entity browser.
 * 
 * @author svelikov
 */
public interface SearchAction {

	/**
	 * If this search action class can handle the searching according to the provided action.
	 * 
	 * @param action
	 *            the action
	 * @return true, if successful
	 */
	boolean canHandle(com.sirma.itt.emf.security.model.Action action);

	/**
	 * Gets the search data form path to be used when the case search is selected.
	 * 
	 * @return the search data form path
	 */
	String getSearchDataFormPath();
}
