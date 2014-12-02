package com.sirma.itt.cmf.beans.definitions;

import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Document definition that is a reference of a {@link DocumentDefinitionTemplate} and is a part of
 * a {@link SectionDefinition}.
 * 
 * @author BBonev
 */
public interface DocumentDefinitionRef extends PathElement, RegionDefinitionModel, Condition,
		StateTransitionalModel, Purposable, AllowedChildrenModel {

	/**
	 * Checks if the document definition can be repeated more then once.
	 * 
	 * @return the multiple
	 * @see #getMaxInstances()
	 */
	boolean isMultiple();

	/**
	 * Gets the max number of instances of the current definition.
	 * 
	 * @return the max instances
	 */
	Integer getMaxInstances();

	/**
	 * Getter method for mandatory.
	 * 
	 * @return the mandatory
	 */
	Boolean getMandatory();

	/**
	 * If the file is structured document or not
	 * 
	 * @return the structured
	 */
	Boolean getStructured();

	/**
	 * Gets the document purpose.
	 * 
	 * @return the purpose
	 */
	@Override
	String getPurpose();

	/**
	 * Getter method for referenceId.
	 * 
	 * @return the referenceId
	 */
	String getReferenceId();

	/**
	 * Getter method for sectionDefinition.
	 * 
	 * @return the sectionDefinition
	 */
	SectionDefinition getSectionDefinition();

}
