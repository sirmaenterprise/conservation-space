package com.sirma.cmf.web.search.facet;

import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * FacetSearchFilter data holder.
 * 
 * @author svelikov
 */
public class FacetSearchFilter {

	/** The filter type. */
	private String filterType;

	/** The disabled. */
	private boolean disabled;

	/** The rendered. */
	private boolean rendered;

	/**
	 * Label provider instance.
	 */
	private LabelProvider labelProvider;

	/**
	 * Instantiates a new facet search filter.
	 * 
	 * @param filterType
	 *            the filter type
	 */
	public FacetSearchFilter(String filterType) {
		this.filterType = filterType;
		this.disabled = false;
		this.rendered = true;
	}

	/**
	 * Instantiates a new facet search filter.
	 * 
	 * @param filterType
	 *            the filter type
	 * @param disabled
	 *            the disabled
	 * @param rendered
	 *            the rendered
	 */
	public FacetSearchFilter(String filterType, boolean disabled, boolean rendered) {
		this.filterType = filterType;
		this.disabled = disabled;
		this.rendered = rendered;
	}

	/**
	 * Getter method for label.
	 * 
	 * @param entity
	 *            the entity
	 * @return the label
	 */
	public String getLabel(String entity) {
		String prefix = "";
		if ("case".equals(entity)) {
			prefix = LabelConstants.FACET_CASE_FILTER_PROPERTY_PREF;
		} else if ("task".equals(entity)) {
			prefix = LabelConstants.FACET_TASK_FILTER_PROPERTY_PREF;
		} else if ("message".equals(entity)) {
			prefix = LabelConstants.FACET_MESSAGE_FILTER_PROPERTY_PREF;
		}
		return labelProvider.getValue(prefix + filterType);
	}

	/**
	 * Getter method for filterType.
	 * 
	 * @return the filterType
	 */
	public String getFilterType() {
		return filterType;
	}

	/**
	 * Setter method for filterType.
	 * 
	 * @param filterType
	 *            the filterType to set
	 */
	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	/**
	 * Getter method for disabled.
	 * 
	 * @return the disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Setter method for disabled.
	 * 
	 * @param disabled
	 *            the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * Getter method for rendered.
	 * 
	 * @return the rendered
	 */
	public boolean isRendered() {
		return rendered;
	}

	/**
	 * Setter method for rendered.
	 * 
	 * @param rendered
	 *            the rendered to set
	 */
	public void setRendered(boolean rendered) {
		this.rendered = rendered;
	}

	/**
	 * Setter method for labelProvider.
	 * 
	 * @param labelProvider
	 *            the labelProvider to set
	 */
	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

}
