package com.sirma.itt.objects.domain.definitions;

import java.util.Date;

import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Defines a object definition contract
 * 
 * @author BBonev
 */
public interface ObjectDefinition extends TopLevelDefinition, RegionDefinitionModel, Condition,
		PathElement, StateTransitionalModel, AllowedChildrenModel {
	/**
	 * Gets the last modified date.
	 * 
	 * @return the last modified date
	 */
	public Date getLastModifiedDate();

	/**
	 * Gets the creation date.
	 * 
	 * @return the creation date
	 */
	public Date getCreationDate();
}
