package com.sirma.itt.emf.search.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Narrows the observer selection for SearchFilterContextUpdateEvent's.
 * 
 * @author svelikov
 */
public class SearchFilterEventBinding extends AnnotationLiteral<SearchFilter> implements
		SearchFilter {

	private static final long serialVersionUID = -1871296081810065639L;

	private final String filtername;

	/**
	 * Instantiates a new dashlet toolbar action binding.
	 * 
	 * @param filtername
	 *            the filtername
	 */
	public SearchFilterEventBinding(String filtername) {
		this.filtername = filtername;
	}

	@Override
	public String value() {
		return filtername;
	}

}
