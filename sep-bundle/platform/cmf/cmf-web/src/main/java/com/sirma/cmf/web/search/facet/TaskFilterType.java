package com.sirma.cmf.web.search.facet;

import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Task filter types.
 * 
 * @author svelikov
 */
public enum TaskFilterType {

	/** The active tasks. */
	ACTIVE_TASKS("active_tasks", true),

	/** The all tasks. */
	ALL_TASKS("all_tasks", true),

	/** The high priority tasks. */
	HIGH_PRIORITY_TASKS("high_priority_tasks", true),

	/** The due date today tasks. */
	DUE_DATE_TODAY_TASKS("due_date_today_tasks", true),

	/** The overdue date tasks. */
	OVERDUE_DATE_TASKS("overdue_date_tasks", true),
	
	/** The unassigned(pooled) tasks */
	UNASSIGNED_TASKS("unassigned_tasks", true);

	/**
	 * The filter name.
	 */
	private String filterName;

	/** The is enabled. */
	private boolean isEnabled;

	/**
	 * Constructor.
	 * 
	 * @param filterName
	 *            The filter name.
	 * @param enabled
	 *            the enabled
	 */
	private TaskFilterType(String filterName, boolean enabled) {
		this.filterName = filterName;
		this.isEnabled = enabled;
	}

	/**
	 * Get filter type by name if exists.
	 * 
	 * @param filterName
	 *            Filter name.
	 * @return {@link CaseFilterType}.
	 */
	public static TaskFilterType getFilterType(String filterName) {
		TaskFilterType[] availableTypes = values();
		for (TaskFilterType taskFilterType : availableTypes) {
			if (taskFilterType.filterName.equals(filterName)) {
				return taskFilterType;
			}
		}

		return null;
	}

	/**
	 * Getter method for filterName.
	 * 
	 * @return the filterName
	 */
	public String getFilterName() {
		return filterName;
	}

	/**
	 * Setter method for filterName.
	 * 
	 * @param filterName
	 *            the filterName to set
	 */
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	/**
	 * Getter method for label.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @return the label
	 */
	public String getLabel(LabelProvider labelProvider) {
		return labelProvider
				.getValue(LabelConstants.FACET_TASK_FILTER_PROPERTY_PREF
						+ filterName);
	}

	/**
	 * Getter method for isEnabled.
	 * 
	 * @return the isEnabled
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Setter method for isEnabled.
	 * 
	 * @param isEnabled
	 *            the isEnabled to set
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

}
