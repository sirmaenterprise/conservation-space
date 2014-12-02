package com.sirma.cmf.web.search;

/**
 * The Class SearchPage.
 * 
 * @author svelikov
 */
public class SearchPage {

	/** The search page type. */
	private String searchPageType;

	/** The label. */
	private String label;

	/**
	 * Instantiates a new search page type.
	 * 
	 * @param searchPageType
	 *            the search page type
	 * @param label
	 *            the label
	 */
	public SearchPage(String searchPageType, String label) {
		this.searchPageType = searchPageType;
		this.label = label;
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

	/**
	 * Getter method for label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Setter method for label.
	 * 
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

}
