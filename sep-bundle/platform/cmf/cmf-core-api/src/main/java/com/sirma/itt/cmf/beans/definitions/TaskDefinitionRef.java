package com.sirma.itt.cmf.beans.definitions;

import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Defines a definition for a task definition that is part of a workflow and references a task
 * template
 * 
 * @author BBonev
 */
public interface TaskDefinitionRef extends PathElement, RegionDefinitionModel,
		Condition, StateTransitionalModel, AllowedChildrenModel {

	/**
	 * Getter method for purpose.
	 *
	 * @return the purpose
	 */
	String getPurpose();

	/**
	 * Getter method for referenceTaskId.
	 *
	 * @return the referenceTaskId
	 */
	String getReferenceTaskId();

	/**
	 * Gets the workflow definition.
	 *
	 * @return the workflow definition
	 */
	WorkflowDefinition getWorkflowDefinition();

}