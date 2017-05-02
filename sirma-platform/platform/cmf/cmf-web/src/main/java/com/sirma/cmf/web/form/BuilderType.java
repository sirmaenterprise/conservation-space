package com.sirma.cmf.web.form;

/**
 * Predefined builder types.
 *
 * @author svelikov
 */
public enum BuilderType {

	/** The single line field builder. */
	SINGLE_LINE_FIELD,

	/** The multy line field builder. */
	MULTY_LINE_FIELD,

	/** The checkbox field builder. */
	CHECKBOX_FIELD,

	/** The date field builder. */
	DATE_FIELD,

	/** The select one menu builder. */
	SELECT_ONE_MENU,

	/** The picklist builder. */
	PICKLIST,

	/** The select one listbox builder. */
	SELECT_ONE_LISTBOX,

	/** The checklist builder. */
	CHECKLIST,

	/** The radiobutton group builder. */
	RADIO_BUTTON_GROUP,

	/** The workflow tasks table. */
	WORKFLOW_TASKS_TABLE,

	/** The outgoing task documents table. */
	OUTGOING_TASK_DOCUMENTS_TABLE,

	/** The incoming task documents table. */
	INCOMING_TASK_DOCUMENTS_TABLE,

	/** The media output builder. */
	MEDIA_OUTPUT,

	/** The username field builder. */
	USER,

	/** The injected field builder. */
	INJECTED_FIELD,

	/** The action event button. */
	ACTION_EVENT_BUTTON,

	/** The tasktree. */
	TASKTREE,

	/** The daterange. */
	DATERANGE,

	/** The relations widget. */
	RELATIONS_WIDGET,

	/** Log work on task widget */
	LOG_WORK_WIDGET,

	/** Actual effort field builder */
	ACTUAL_EFFORT,

	/** Calculated date field builder. */
	CALCULATED_DATE,

	RELATED_FIELDS;

	/**
	 * Finds {@link BuilderType} based on value string.
	 *
	 * @param value
	 *            the value whose builder type is searched for.
	 * @return{@link BuilderType} or null if not found.
	 */
	public static BuilderType getType(String value) {
		for (BuilderType type : values()) {
			if (type.name().equals(value)) {
				return type;
			}
		}
		return null;
	}

}
