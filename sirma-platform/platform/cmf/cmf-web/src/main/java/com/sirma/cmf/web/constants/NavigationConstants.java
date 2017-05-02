package com.sirma.cmf.web.constants;

/**
 * navigation constants. REVIEW tozi class da se napravi na interface. Sled kato imeto na class-a e NavigationConstants
 * nqma nujda kym konstantite da se dobavq prefix NAVIGATE_ - primer NAVIGATE_USER_DASHBOARD.
 *
 * @author svelikov
 */
public class NavigationConstants {

	public static final String RELOAD_PAGE = null;
	/**
	 * When we should return back to a page where some action was executed (such case is the 'cancel' action).
	 */
	public static final String BACKWARD = "BACKWARD";
	public static final String RETURN = "RETURN";
	public static final String NAVIGATE_HOME = "dashboard";

	// user dashboard
	public static final String NAVIGATE_USER_DASHBOARD = "dashboard";
	public static final String PROJECT_DASHBOARD = "project-dashboard";

	// case instance dashboard tabs
	public static final String NAVIGATE_TAB_CASE_DETAILS = "case-details";
	public static final String NAVIGATE_TAB_CASE_DASHBOARD = "caseinstance";
	public static final String NAVIGATE_TAB_CASE_DOCUMENTS = "case-documents";
	public static final String NAVIGATE_TAB_CASE_DISCUSSIONS = "case-discussions";
	public static final String NAVIGATE_TAB_CASE_WORKFLOW = "case-workflow";
	public static final String NAVIGATE_TAB_CASE_HISTORY = "case-history";

	// menu navigation
	public static final String NAVIGATE_LIBRARY = "document-library";
	public static final String NAVIGATE_MENU_CASE_LIST = "case-list";
	public static final String NAVIGATE_MENU_DASHBOARD = "dashboard";
	public static final String NAVIGATE_MENU_SEARCH = "search";
	public static final String NAVIGATE_MENU_REPORTS = "reports";
	public static final String NAVIGATE_MENU_ADMINISTRATION = "administration";

	// case instance action navigation
	public static final String NAVIGATE_NEW_CASE = "case-form";
	public static final String NAVIGATE_EDIT_CASE = "case-form";
	public static final String NAVIGATE_CASE_LINK = "case-link";

	// document instance operation navigations
	public static final String NAVIGATE_DOCUMENT_DETAILS = "documentinstance";
	public static final String NAVIGATE_DOCUMENT_EDIT_PROPERTIES = "document-edit-properties";

	// workflow and task navigations
	public static final String NAVIGATE_WORKFLOW_START = "workflowinstancecontext";
	public static final String WORKFLOW_LANDING_PAGE = "workflowinstancecontext";
	public static final String NAVIGATE_TASK_LIST_PAGE = "task-list";
	public static final String NAVIGATE_TASK_DETAILS_PAGE = "taskinstance";
	public static final String STANDALONE_TASK_DETAILS_PAGE = "standalonetaskinstance";

	//
	public static final String NAVIGATE_CASE_LIST_PAGE = "case-list";
	public static final String TASK_LIST_PAGE = "task-list";

	// administration navigation
	public static final String NAVIGATE_SYNCHRONIZED = "server-synchronized";
	public static final String NAVIGATE_DOCUMENT_LIST_PAGE = "document-list";

}
