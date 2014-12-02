package com.sirma.itt.emf.definition.model;

import java.util.List;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;

/**
 * The Interface ControlDefinition.
 * 
 * @author BBonev
 */
public interface ControlDefinition extends PathElement, DefinitionModel {

	/**
	 * Getter method for controlParams.
	 * 
	 * @return the controlParams
	 */
	public abstract List<ControlParam> getControlParams();

	/**
	 * Getter method for uiParams.
	 * 
	 * @return the uiParams
	 */
	public abstract List<ControlParam> getUiParams();

}