package com.sirma.cmf.web.navigation;

/**
 * Constants used in navigation handling functionality.
 *
 * @author svelikov
 */
public abstract class NavigationHandlerConstants {

	/**
	 * Constant for request parameter name added to every leaved page url. When returning in browser history, this
	 * parameter is used to determine if a page is from history and to perform appropriate navigation.
	 */
	public static final String BACK = "back";

	/**
	 * Constant used as action method outcome/navigation string and should be returned from every action method that is
	 * required to return to previous page.
	 */
	public static final String BACKWARD = "BACKWARD";

	/** If page is refreshed trough the browser or programmatically. */
	public static final String REFRESH = "REFRESH";

	/**
	 * Request parameter added immediately before user to be redirected to a new view.
	 */
	public static final CharSequence HISTORY_PAGE = "history=true";
	/**
	 * Request parameter added when a new page is loaded.
	 */
	public static final CharSequence CURRENT_PAGE = "history=false";
	/**
	 * Request parameter added when a link is not a jsf action link but just an anchor.
	 */
	public static final CharSequence SIMPLE_LINK = "simpleLink=true";
	/**
	 * This is passed as actionMethod name when navigation handler is invoked by the EmfPhaseListener when it detects a
	 * navigation trough a simple link. This way the navigation handler can decide to store a navigation point for pages
	 * accessed trough simple links and not to handle the navigation trough the NavigationHandler default
	 * implementation.
	 */
	public static final String SIMPLE_LINK_OUTCOME = "simpleLink";

}
