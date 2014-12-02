package com.sirma.itt.pm.web.userdashboard.panel.filters;

import com.sirma.itt.emf.label.LabelProvider;

/**
 * This class holds all the filters for project dashlet
 * 
 * @author cdimitrov
 * 
 */
public enum PMUserDashboardProjectFilter {

	/** Filter value for all available project */
	ALL_PROJECTS("all_projects", true),

	/** Filter value for all active projects */
	ACTIVE_PROJECTS("active_projects", true),

	/**
	 * Filter value for all completed projects (all projects that are not in the
	 * end state)
	 */
	COMPLETED_PROJECTS("completed_projects", true);

	/** The filter name */
	private String filterName;

	/** The is enabled */
	private boolean isEnabled;

	/** The Constant PM_PROJECT_DASHBOARD_PROJECTS_FILTER_PREF */
	public static final String PM_PROJECT_USER_DASHBOARD_PROJECTS_FILTER_PREF = "pm.user.dashboard.projects.filter.";

	/**
	 * Constructor.
	 * 
	 * @param filterName
	 *            The filter name.
	 * @param enabled
	 *            the enabled
	 */
	private PMUserDashboardProjectFilter(String filterName, boolean enabled) {
		this.filterName = filterName;
		this.isEnabled = enabled;
	}

	/**
	 * Retrieve filter types by name
	 * 
	 * @param filterName
	 *            user specified filter name
	 * @return filter type
	 */
	public static PMUserDashboardProjectFilter getFilterType(String filterName) {
		
		PMUserDashboardProjectFilter[] availableTypes = values();
		
		for (PMUserDashboardProjectFilter projectFilterType : availableTypes) {
			
			if (projectFilterType.filterName.equals(filterName)) {
				
				return projectFilterType;
				
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

		return labelProvider.getValue(PM_PROJECT_USER_DASHBOARD_PROJECTS_FILTER_PREF
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
