package com.sirma.itt.emf.definition.model;

import java.util.List;

import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.domain.model.ReferenceDefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Defines a generic interface for top level definition that can work with multiple actual
 * definition types. The type identifier is provided via the method {@link #getType()}.
 * 
 * @author BBonev
 */
public interface GenericDefinition extends TopLevelDefinition, StateTransitionalModel,
		AllowedChildrenModel, PathElement, RegionDefinitionModel, Condition,
		Purposable, ReferenceDefinitionModel {

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
