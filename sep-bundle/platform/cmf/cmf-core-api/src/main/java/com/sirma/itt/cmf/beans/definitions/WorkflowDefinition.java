package com.sirma.itt.cmf.beans.definitions;

import java.util.Date;
import java.util.List;

import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.state.transition.StateTransition;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Define a definition for a single workflow.
 *
 * @author BBonev
 */
public interface WorkflowDefinition extends PathElement, TopLevelDefinition, RegionDefinitionModel,
		Condition, AllowedChildrenModel, StateTransitionalModel {

	/**
	 * Getter method for lastModifiedDate.
	 *
	 * @return the lastModifiedDate
	 */
	Date getLastModifiedDate();

	/**
	 * Getter method for creationDate.
	 *
	 * @return the creationDate
	 */
	Date getCreationDate();

	/**
	 * Getter method for tasks.
	 *
	 * @return the tasks
	 */
	List<TaskDefinitionRef> getTasks();

	/**
	 * Provides all possible transitions
	 * 
	 * @return the transitions
	 */
	@Override
	List<TransitionDefinition> getTransitions();

	/**
	 * Gets the state transitions.
	 * 
	 * @return the state transitions
	 */
	@Override
	List<StateTransition> getStateTransitions();

}