package com.sirma.cmf.web.navigation;

/**
 * Constants used in navigation handling functionality.
 * 
 * @author svelikov
 */
public abstract class NavigationHandlerConstants {

	/**
	 * Constant for request parameter name added to every leaved page url. When returning in browser
	 * history, this parameter is used to determine if a page is from history and to perform
	 * appropriate navigation.
	 */
	public static final String BACK = "back";

	/**
	 * Constant used as action method outcome/navigation string and should be returned from every
	 * action method that is required to return to previous page.
	 */
	public static final String BACKWARD = "BACKWARD";

	/** If page is refreshed trough the browser or programmaticaly. */
	public static final String REFRESH = "REFRESH";

	/**
	 * Constant used to mark unsafe/dummy navigation points that may be added in navigation history
	 * in some cases.
	 */
	public static final String UNSAFE = "unsafe";

	public static final CharSequence HISTORY_PAGE = "history=true";
	public static final CharSequence CURRENT_PAGE = "history=false";
	public static final CharSequence SIMPLE_LINK = "simpleLink=true";

	public static final String FORWARD = "FORWARD";
}
