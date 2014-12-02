package com.sirma.itt.emf.definition.model;

import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.label.Displayable;

/**
 * Logical group of fields. All fields defined here will be displayed in a distinct group/region on
 * the page.
 * 
 * @author BBonev
 */
public interface RegionDefinition extends DefinitionModel, PathElement, Sortable,
		Displayable, Conditional, Controllable {

	/**
	 * Gets the display type.
	 * 
	 * @return the display type
	 */
	@Override
	DisplayType getDisplayType();

	/**
	 * Gets the control definition.
	 * 
	 * @return the control definition
	 */
	@Override
	ControlDefinition getControlDefinition();

}