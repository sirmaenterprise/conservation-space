package com.sirma.itt.pm.web.project.dashboard.filters;

import com.sirma.itt.emf.label.LabelProvider;
/**
 * This class will holds all the filters for project case dashlet.
 *
 * @author cdimitrov
 *
 */
public enum DashboardCaseFilter {

	/** Filter value for all available project */
	ALL_CASES("all_cases", true),

	/** Filter value for all active projects */
	ACTIVE_CASES("active_cases", true);

	/** The filter name */
	private String filterName;

	/** The is enabled */
	private boolean isEnabled;

	/** The Constant PM_PROJECT_DASHBOARD_CASE_FILTER_PREF */
	public static final String PM_PROJECT_DASHBOARD_CASE_FILTER_PREF = "pm.project.dashboard.case.filter.";
	/**
	 * Constructor.
	 *
	 * @param filterName
	 *            The filter name.
	 * @param enabled
	 *            the enabled
	 */
	private DashboardCaseFilter(String filterName, boolean enabled) {
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
	public static DashboardCaseFilter getFilterType(String filterName) {

		DashboardCaseFilter[] availableTypes = values();

		for (DashboardCaseFilter projectFilterType : availableTypes) {

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

		return labelProvider.getValue(PM_PROJECT_DASHBOARD_CASE_FILTER_PREF
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