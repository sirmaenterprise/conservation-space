package com.sirma.cmf.web.search.facet.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class UpdatedSearchFilterBinding.
 * 
 * @author svelikov
 */
public class UpdatedSearchFilterBinding extends AnnotationLiteral<UpdatedSearchFilter> implements
		UpdatedSearchFilter {

	private static final long serialVersionUID = 5885043504416946392L;

	public static final String CASE = "case";
	public static final String TASK = "task";
	public static final String MESSAGE = "message";

	/** The filter type. */
	private final String filterType;

	/**
	 * Instantiates a new updated search filter binding.
	 * 
	 * @param filterType
	 *            the filter type
	 */
	public UpdatedSearchFilterBinding(String filterType) {
		this.filterType = filterType;
	}

	@Override
	public String value() {
		return filterType;
	}

}
