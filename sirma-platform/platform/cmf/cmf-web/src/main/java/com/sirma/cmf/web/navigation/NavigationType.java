package com.sirma.cmf.web.navigation;

/**
 * Constants for the navigation type actions that the {@link EmfNavigationHandler} can handle.
 *
 * @author svelikov
 */
public enum NavigationType {

	/** Executed on page refresh. */
	REFRESH("REFRESH"), /** Return to page after backward operation. */
	RETURN("RETURN"), /** Back to previous page. */
	BACKWARD("BACKWARD"), /** Default action. */
	PROCEED("PROCEED");

	/** The navigation type. */
	private String navigationType;

	/**
	 * Instantiates a new navigation type.
	 *
	 * @param navigationType
	 *            the navigation type
	 */
	private NavigationType(String navigationType) {
		this.navigationType = navigationType;
	}

	/**
	 * Gets the navigation type.
	 *
	 * @param navigationType
	 *            the navigation type
	 * @return the navigation type
	 */
	public static NavigationType getNavigationType(String navigationType) {
		NavigationType[] types = values();
		for (NavigationType type : types) {
			if (type.navigationType.equals(navigationType)) {
				return type;
			}
		}
		return null;
	}
}
