package com.sirma.itt.emf.label;

import com.sirma.itt.emf.domain.DisplayType;

/**
 * Interface that marks the element as displayable and defines the means to
 * retrieve the element label and tooltip
 *
 * @author BBonev
 */
public interface Displayable {

	/**
	 * Gets the display type.
	 * 
	 * @return the display type
	 */
	public DisplayType getDisplayType();
	/**
	 * Gets the display label for the current field
	 *
	 * @return The label for this property.
	 */
	public String getLabel();

	/**
	 * Gets the tooltip for the current field.
	 *
	 * @return the tooltip
	 */
	public String getTooltip();

	/**
	 * Gets the label id.
	 *
	 * @return the label id
	 */
	public String getLabelId();

	/**
	 * Gets the tooltip id.
	 *
	 * @return the tooltip id
	 */
	public String getTooltipId();

	/**
	 * Setter method for labelProvider.
	 *
	 * @param labelProvider
	 *            the labelProvider to set
	 */
	public void setLabelProvider(LabelProvider labelProvider);
}
