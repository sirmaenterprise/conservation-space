package com.sirma.itt.seip.domain.definition;

import java.util.List;

import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.Purposable;

/**
 * Defines a generic interface for top level definition that can work with multiple actual definition types. The type
 * identifier is provided via the method {@link #getType()}.
 *
 * @author BBonev
 */
public interface GenericDefinition extends TopLevelDefinition, StateTransitionalModel, AllowedChildrenModel,
		PathElement, RegionDefinitionModel, Condition, Purposable, ReferenceDefinitionModel {

	/**
	 * Gets target object type.
	 *
	 * @return the type
	 */
	String getType();

	/**
	 * Gets the list of sub definitions if any.
	 *
	 * @return the sub definitions
	 */
	List<GenericDefinition> getSubDefinitions();
}
