package com.sirma.itt.emf.definition.model;

import com.sirma.itt.emf.domain.model.PathElement;

/**
 * The Interface ControlParam.
 * 
 * @author BBonev
 */
public interface ControlParam extends PathElement {

	/**
	 * Gets the value of the value property.
	 * 
	 * @return possible object is {@link String }
	 */
	public abstract String getValue();

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 */
	public abstract String getName();

	/**
	 * Getter method for controlDefinition.
	 * 
	 * @return the controlDefinition
	 */
	public abstract ControlDefinition getControlDefinition();

}