package com.sirma.itt.emf.definition.model;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.Identity;

/**
 * Prototype definition. Defines a property definition that has only type. The prototype is used for
 * loading properties from DB to entities.
 * 
 * @author BBonev
 */
public interface PrototypeDefinition extends Entity<Long>, Identity {

	/**
	 * Gets the container.
	 * 
	 * @return the container
	 */
	String getContainer();

	/**
	 * Sets the container.
	 * 
	 * @param container
	 *            the new container
	 */
	void setContainer(String container);

	/**
	 * Gets the data type.
	 * 
	 * @return the data type
	 */
	DataTypeDefinition getDataType();

	/**
	 * Sets the data type.
	 * 
	 * @param typeDefinition
	 *            the new data type
	 */
	void setDataType(DataTypeDefinition typeDefinition);

	/**
	 * Checks if is multi valued.
	 * 
	 * @return true, if is multi valued
	 */
	Boolean isMultiValued();

	/**
	 * Sets the multi valued.
	 * 
	 * @param multiValued
	 *            the new multi valued
	 */
	void setMultiValued(Boolean multiValued);
}
