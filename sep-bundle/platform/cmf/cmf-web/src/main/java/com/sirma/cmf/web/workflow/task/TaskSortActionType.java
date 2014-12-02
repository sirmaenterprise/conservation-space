package com.sirma.cmf.web.workflow.task;

import com.sirma.cmf.web.search.SortAction;
import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Sort action types for tasks <code>номер, по крайна дата, по статус, по изпълнител,
 * по име (на задачата).</code>
 * 
 * @author bbanchev
 */
public enum TaskSortActionType implements SortAction {

	/** The task modification date. */
	TASK_MODIFIED_ON(TaskProperties.MODIFIED_ON),
	/** The task modification date. */
	TASK_CREATED_ON(TaskProperties.ACTUAL_START_DATE),
	/** The task status. */
	TASK_STATUS(TaskProperties.STATUS),
	/** The task owner. */
	TASK_OWNER(TaskProperties.TASK_OWNER),
	/** The task type. */
	TASK_TYPE(TaskProperties.TYPE),
	/** The task id. */
	TASK_ID(TaskProperties.TASK_ID),
	/** The task due date. */
	TASK_DUE_DATE(TaskProperties.PLANNED_END_DATE);

	/**
	 * The sorter type.
	 */
	private String type;

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            The sorter type.
	 */
	private TaskSortActionType(String type) {
		this.type = type;
	}

	/**
	 * Get sorter type by name if exists.
	 * 
	 * @param type
	 *            Sorter type.
	 * @return {@link TaskSortActionType}.
	 */
	public static TaskSortActionType getSorterType(String type) {
		TaskSortActionType[] availableTypes = values();
		for (TaskSortActionType sortActionType : availableTypes) {
			if (sortActionType.type.equals(type)) {
				return sortActionType;
			}
		}

		return null;
	}

	/**
	 * Getter method for type.
	 * 
	 * @return the type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for label.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @return the label
	 */
	public String getLabel(LabelProvider labelProvider) {
		return labelProvider.getValue(LabelConstants.TASK_SEARCH_ARGS_PROPERTY_PREF + type);
	}

	@Override
	public String getLabel() {
		return null;
	}

}
