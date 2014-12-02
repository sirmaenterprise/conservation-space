package com.sirma.itt.emf.help.history;

/**
 * Store information about a page. 
 * 1.	Url of content.
 * 2.	Navigator view: TOC, Index, Search.
 *
 * @author Boyan Tonchev
 */
public class PageUrl {
	
	/**
	 * Url of content.
	 */
	private String contentUrl;
	
	/**
	 * Navigator view: TOC, Index, Search.
	 */
	private String navigationState;
	
	private String id;

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get Url of content.
	 *
	 * @return Url of content.
	 */
	public String getContentUrl() {
		return contentUrl;
	}

	/**
	 * Set Url of content.
	 *
	 * @param contentUrl Url of content.
	 */
	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	/**
	 * Get Navigator view: TOC, Index, Search.
	 *
	 * @return Navigator view: TOC, Index, Search.
	 */
	public String getNavigationState() {
		return navigationState;
	}

	/**
	 * Set Navigator view: TOC, Index, Search.
	 *
	 * @param navigationState Navigator view: TOC, Index, Search.
	 */
	public void setNavigationState(String navigationState) {
		this.navigationState = navigationState;
	}
}