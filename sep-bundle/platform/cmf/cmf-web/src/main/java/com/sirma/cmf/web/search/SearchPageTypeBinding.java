package com.sirma.cmf.web.search;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class SearchPageTypeBinding.
 * 
 * @author svelikov
 */
public class SearchPageTypeBinding extends AnnotationLiteral<SearchPageType> implements
		SearchPageType {

	private static final long serialVersionUID = -8118260830642993921L;

	/** The search page. */
	private final String searchPage;

	/**
	 * Instantiates a new search page type binding.
	 * 
	 * @param searchPage
	 *            the search page
	 */
	public SearchPageTypeBinding(String searchPage) {
		this.searchPage = searchPage;
	}

	@Override
	public String value() {
		return searchPage;
	}

}
