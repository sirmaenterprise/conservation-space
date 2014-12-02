package com.sirma.cmf.web.search.facet;

import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Message filter types.
 * 
 * @author svelikov
 */
public enum MessageFilterType {

	/** */
	ALL_MESSAGES("all_messages", true);

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
	private MessageFilterType(String filterName, boolean enabled) {
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
	public static MessageFilterType getFilterType(String filterName) {
		MessageFilterType[] availableTypes = values();
		for (MessageFilterType messageFilterType : availableTypes) {
			if (messageFilterType.filterName.equals(filterName)) {
				return messageFilterType;
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
				.getValue(LabelConstants.FACET_MESSAGE_FILTER_PROPERTY_PREF
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
