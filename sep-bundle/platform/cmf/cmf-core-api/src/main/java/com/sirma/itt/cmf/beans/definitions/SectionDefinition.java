package com.sirma.itt.cmf.beans.definitions;

import java.util.List;

import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * The Class SectionDefinition.
 */
public interface SectionDefinition extends PathElement, RegionDefinitionModel,
		StateTransitionalModel, AllowedChildrenModel, Purposable {

	/**
	 * Gets the value of the documentDefs property.
	 * 
	 * @return the document defs possible object is {@link List <DocumentDefinition> }
	 */
	public List<DocumentDefinitionRef> getDocumentDefinitions();

	/**
	 * Getter method for caseDefinition.
	 * 
	 * @return the caseDefinition
	 */
	public CaseDefinition getCaseDefinition();

	/**
	 * Getter section referenceId to fetch and initialize the current section from other section
	 * definitions.
	 * 
	 * @return the referenceId
	 */
	public String getReferenceId();

}
