package com.sirma.cmf.web.search.facet;

import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Case filter types.
 * 
 * @author svelikov
 */
public enum CaseFilterType {

	/** The all cases. */
	ALL_CASES("all_cases", true),

	/** The active cases. */
	ACTIVE_CASES("active_cases", true),

	/** The my cases. */
	MY_CASES("my_cases", true);

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
	private CaseFilterType(String filterName, boolean enabled) {
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
	public static CaseFilterType getFilterType(String filterName) {
		CaseFilterType[] availableTypes = values();
		for (CaseFilterType caseFilterType : availableTypes) {
			if (caseFilterType.filterName.equals(filterName)) {
				return caseFilterType;
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
				.getValue(LabelConstants.FACET_CASE_FILTER_PROPERTY_PREF
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
