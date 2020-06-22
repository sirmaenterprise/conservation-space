package com.sirma.itt.seip.definition;

import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.Controllable;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.itt.seip.json.JsonRepresentable;

/**
 * Logical group of fields. All fields defined here will be displayed in a distinct group/region on the page.
 *
 * @author BBonev
 */
public interface RegionDefinition
		extends DefinitionModel, PathElement, Ordinal, Displayable, Conditional, Controllable, JsonRepresentable {

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

	@Override
	default String getType() {
		return null;
	}

}