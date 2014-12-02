package com.sirma.cmf.web;

/**
 * The Interface ItemSelector.
 * 
 * @author svelikov
 */
public interface ItemSelector {

	/**
	 * Item selector action
	 * 
	 * @param selectedItemId
	 *            the selected id
	 * @param selectedItemTitle
	 *            the selected title
	 */
	void itemSelectedAction(String selectedItemId, String selectedItemTitle);

}
