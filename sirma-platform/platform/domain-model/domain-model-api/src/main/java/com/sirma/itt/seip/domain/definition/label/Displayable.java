package com.sirma.itt.seip.domain.definition.label;

import com.sirma.itt.seip.domain.definition.DisplayType;

/**
 * Interface that marks the element as displayable and defines the means to retrieve the element label and tooltip
 *
 * @author BBonev
 */
public interface Displayable {

	/**
	 * Gets the display type.
	 *
	 * @return the display type
	 */
	DisplayType getDisplayType();

	/**
	 * Gets the display label for the current field
	 *
	 * @return The label for this property.
	 */
	String getLabel();

	/**
	 * Gets the tooltip for the current field.
	 *
	 * @return the tooltip
	 */
	String getTooltip();

	/**
	 * Gets the label id.
	 *
	 * @return the label id
	 */
	String getLabelId();

	/**
	 * Gets the tooltip id.
	 *
	 * @return the tooltip id
	 */
	String getTooltipId();

	/**
	 * Setter method for labelProvider.
	 *
	 * @param labelProvider
	 *            the labelProvider to set
	 */
	void setLabelProvider(LabelProvider labelProvider);
}
