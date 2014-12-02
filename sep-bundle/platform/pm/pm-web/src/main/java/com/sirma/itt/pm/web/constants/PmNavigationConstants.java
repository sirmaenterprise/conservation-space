package com.sirma.itt.pm.web.constants;

import com.sirma.cmf.web.constants.NavigationConstants;

/**
 * Navigation constants for PM module.
 * 
 * @author BBonev
 */
public class PmNavigationConstants extends NavigationConstants {

	public static final String NAVIGATE_NEW_PROJECT = "projectinstance";
	public static final String EDIT_PROJECT = "projectinstance";
	public static final String PROJECT = "projectinstance";
	public static final String NAVIGATE_MENU_PROJECT_LIST = "project-list";
	public static final String PROJECT_SCHEDULE = "project-schedule";
	public static final String PROJECT_LIST_PAGE = "project-list";
	public static final String PROJECT_SEARCH = "project-list";
	public static final String MANAGE_RESOURCES = "manage-resources";

	/**
	 * Constant that will be used when creating object under
	 * project level. If object module is not available, this constant
	 * will not be used.
	 */
	public static final String NAVIGATE_OBJECT_PAGE = "object";

}
