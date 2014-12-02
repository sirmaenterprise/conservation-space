package com.sirma.cmf.web.browser.tabs;

/**
 * Class that will holds constants for managing browser tab titles and icons. Here we can define
 * labels for browser tab, path fragments that will be used for detect pages.
 * 
 * @author cdimitrov
 */
public class ApplicationTabConstants {

	// CONSTANTS FOR ACCESS LABELS

	/** Bundle key for user dashboard label. */
	public static final String CMF_TAB_USER_DASHBOARD = "cmf.tab.user.dashboard";

	/** Bundle key for basic search label. */
	public static final String CMF_TAB_BASIC_SEARCH = "cmf.tab.basic.search";

	/** Bundle key for case search label. */
	public static final String CMF_TAB_CASE_SEARCH = "cmf.tab.case.search";

	/** Bundle key for task search label. */
	public static final String CMF_TAB_TASK_SEARCH = "cmf.tab.task.search";

	/** Bundle key for document search label. */
	public static final String CMF_TAB_DOCUMENT_SEARCH = "cmf.tab.document.search";

	/** Bundle key for browse projects label. */
	public static final String CMF_TAB_BROWSE_PROJECTS = "cmf.tab.browse.projects";

	public static final String CMF_TAB_RESOURCE_ALLOCATION = "cmf.tab.resource.allocation";

	public static final String CMF_TAB_HELP_REQUEST = "cmf.tab.help.request";

	/** Bundle key for application label. */
	public static final String CMF_TAB_APPLICATION = "application.tab.title";

	/** Bundle key for creating new object. */
	public static final String CMF_TAB_NEW_OBJECT = "cmf.tab.new.object";

	/** Bundle key for creating new project. */
	public static final String CMF_TAB_CREATE_PROJECT = "cmf.tab.new.project";

	// CONSTANTS FOR DETECTING CURRENT PAGE

	/** Key for retrieving user dashboard page. */
	public static final String CMF_PAGE_USER_DASHBOARD = "userDashboard/dashboard.xhtml";

	/** Key for retrieving basic search page. */
	public static final String CMF_PAGE_BASIC_SEARCH = "search/basic-search.xhtml";

	/** Key for retrieving search page. */
	public static final String CMF_PAGE_SEARCH = "search/search.xhtml";

	/** Key for retrieving case list page. */
	public static final String CMF_PAGE_CASE_LIST = "case/case-list.xhtml";

	/** Key for retrieving task list page. */
	public static final String CMF_PAGE_TASK_LIST = "task/task-list.xhtml";

	/** Key for retrieving document list page. */
	public static final String CMF_PAGE_DOCUMENT_LIST = "document/document-list.xhtml";

	/** Key for retrieving project list page. */
	public static final String CMF_PAGE_PROJECT_LIST = "project/project-list.xhtml";

	/** Key for retrieving resource allocation page. */
	public static final String CMF_PAGE_RESOURCE_ALLOCATION = "project/project-resource-allocation.xhtml";

	/** Key for retrieving project page. */
	public static final String CMF_PAGE_PROJECT = "project/project.xhtml";

	/** Key for retrieving help request. */
	public static final String CMF_PAGE_HELP_REQUEST = "help/help-request.xhtml";

	// CONSTANTS REPRESENT ICONS NAMES

	/** Icon for user dashboard. */
	public static final String CMF_ICON_USER_DASHBOARD = "userdashboard";

	/** Icon for all search pages. */
	public static final String CMF_ICON_SEARCH = "search";

	/** Icon for browse projects */
	public static final String CMF_ICON_PROJECT_LIST = "project-list";

	/** Icon for task/resource allocation */
	public static final String CMF_ICON_RESOURCE_ALLOCATION = "resource-allocation";

	/** Icon for project pages. */
	public static final String CMF_ICON_PROJECT = "project";

	/** Icon for basic search. */
	public static final String CMF_ICON_BASIC_SEARCH = "basic-search";

	/** Icon for help request. */
	public static final String CMF_ICON_HELP_REQUEST = "help-request";

	/**
	 * Prevent initialize the current class.
	 */
	private ApplicationTabConstants() {
		// nothing to do
	}

}
