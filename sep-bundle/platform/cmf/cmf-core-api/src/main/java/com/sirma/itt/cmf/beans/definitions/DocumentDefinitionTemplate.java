package com.sirma.itt.cmf.beans.definitions;

import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.state.transition.StateTransitionalModel;

/**
 * Definition for document templates. The templates are used via references in the actual instances.
 * 
 * @author BBonev
 */
public interface DocumentDefinitionTemplate extends PathElement, RegionDefinitionModel, Condition,
		StateTransitionalModel, AllowedChildrenModel {

	/**
	 * Getter method for parent.
	 *
	 * @return the parent
	 */
	public String getParent();

}
