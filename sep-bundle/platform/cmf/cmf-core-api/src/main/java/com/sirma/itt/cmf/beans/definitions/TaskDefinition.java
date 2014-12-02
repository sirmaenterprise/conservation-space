/*
 *
 */
package com.sirma.itt.cmf.beans.definitions;

import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Defines a standalone task definition.
 * 
 * @author BBonev
 */
public interface TaskDefinition extends PathElement, RegionDefinitionModel, Condition,
		StateTransitionalModel, TopLevelDefinition, AllowedChildrenModel {

	/**
	 * Getter method for parentTaskId.
	 * 
	 * @return the parentTaskId
	 */
	String getParentTaskId();

	/**
	 * Gets task template reference id.
	 * 
	 * @return the reference id
	 */
	String getReferenceId();

	/**
	 * Gets the dms type.
	 * 
	 * @return the dms type
	 */
	String getDmsType();

}