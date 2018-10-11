/*
 *
 */
package com.sirma.itt.seip.domain.search;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Search filter holder class.
 *
 * @author svelikov
 */
public class SearchFilter {

	/** The value. */
	private final String value;

	/** The label. */
	private final String label;

	/** The tooltip. */
	private String tooltip;
	/** The is default. */
	private boolean isDefault;

	/** The definition. */
	private final PropertyDefinition definition;

	/**
	 * Instantiates a new search filter.
	 *
	 * @param value
	 *            the value
	 * @param label
	 *            the label
	 * @param tooltip
	 *            the tooltip
	 * @param definition
	 *            the definition
	 */
	public SearchFilter(String value, String label, String tooltip, PropertyDefinition definition) {
		this.value = value;
		this.label = label;
		this.tooltip = tooltip;
		this.definition = definition;
		isDefault = false;
	}

	/**
	 * Getter method for value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Getter method for label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Getter method for definition.
	 *
	 * @return the definition
	 */
	public PropertyDefinition getDefinition() {
		return definition;
	}

	/**
	 * Getter method for isDefault.
	 *
	 * @return the isDefault
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * Setter method for isDefault.
	 *
	 * @param isDefault
	 *            the isDefault to set
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "SearchFilter [value=" + value + ", label=" + label + ", isDefault=" + isDefault + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SearchFilter)) {
			return false;
		}
		SearchFilter other = (SearchFilter) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/**
	 * Getter method for tooltip.
	 *
	 * @return the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * Setter method for tooltip.
	 *
	 * @param tooltip
	 *            the tooltip to set
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
}
