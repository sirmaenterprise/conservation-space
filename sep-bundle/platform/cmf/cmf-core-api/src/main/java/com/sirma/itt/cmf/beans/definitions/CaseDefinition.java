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
 * Defines case definition contract.
 */
public interface CaseDefinition extends PathElement, TopLevelDefinition, RegionDefinitionModel,
		Condition, AllowedChildrenModel, StateTransitionalModel {

	/**
	 * Gets the value of the sectionDefinitions property.
	 * 
	 * @return the section defs possible object is {@link List<SectionDef> }
	 */
	public List<SectionDefinition> getSectionDefinitions();

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
