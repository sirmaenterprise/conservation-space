package com.sirma.itt.cmf.beans.definitions;

import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Defines a task definition template
 *
 * @author BBonev
 */
public interface TaskDefinitionTemplate extends PathElement, RegionDefinitionModel, Condition,
		StateTransitionalModel, AllowedChildrenModel {

	/**
	 * Getter method for parentTaskId.
	 *
	 * @return the parentTaskId
	 */
	String getParentTaskId();

}