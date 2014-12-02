package com.sirma.itt.cmf.constants;

import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Defines the property names of the generic case.
 * 
 * @author BBonev
 */
public interface CaseProperties extends DefaultProperties {
	
	/**
	 * Properties for automatic action settings
	 * 
	 * @author bbanchev
	 */
	public interface CaseAutomaticProperties {
		/** holder for automatic actions' settings. */
		String AUTOMATIC_ACTIONS_SET = "automaticActionsProperties";
		/** automatically cancel all active workflows. */
		String AUTOMATIC_CANCEL_ACTIVE_WF = "automaticCancelWorkflow";
		String ACTIVE_TASKS_PROPS_UPDATE = "taskPropsUpdate";

		String MARK_AS_DELETED_WF = "markAsDeletedWorkflow";
	}

	/** The case instance type. */
	String TYPE_CASE_INSTANCE = "caseInstance";

	/** The Constant CONTAINER. */
	String CONTAINER = "site";

	/** The Constant SECONDARY_STATE. */
	String SECONDARY_STATE = "secondaryState";

	/** The Constant ARCHIVE_ID. */
	String ARCHIVE_ID = "archiveId";

	/** The Constant CLOSED_ON. */
	String CLOSED_ON = "closedOn";

	/** The Constant CLOSED_FROM. */
	String CLOSED_BY = "closedFrom";

	/** The Constant ARCHIVE_DATE. */
	String ARCHIVE_DATE = "archivedOn";

	/** The Constant ARCHIVED_FROM. */
	String ARCHIVED_BY = "archivedFrom";

	/** The Constant CLOSED_REASON. */
	String CLOSED_REASON = "closedReason";

	/**
	 * The Constant DMS_TOUCH. Special property used to forcefully modify the DMS
	 */
	String DMS_TOUCH = "dmsTouch";

}
