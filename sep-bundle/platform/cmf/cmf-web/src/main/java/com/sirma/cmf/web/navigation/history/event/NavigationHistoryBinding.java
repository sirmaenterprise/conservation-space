package com.sirma.cmf.web.navigation.history.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class UpdatedSearchFilterBinding.
 * 
 * @author svelikov
 */
public class NavigationHistoryBinding extends AnnotationLiteral<NavigationHistoryType> implements
		NavigationHistoryType {

	private static final long serialVersionUID = -2225889630841043291L;

	private final String path;

	/**
	 * Instantiates a new navigation history binding.
	 * 
	 * @param path
	 *            the path
	 */
	public NavigationHistoryBinding(String path) {
		this.path = path;
	}

	@Override
	public String value() {
		return path;
	}

}
