package com.sirma.itt.emf.definition.model;

import java.util.Set;

/**
 * Defines properties for workflow Id configurations.
 * 
 * @author BBonev
 */
public interface AllowedChildConfiguration {

	/**
	 * Getter method for codelist.
	 * 
	 * @return the codelist
	 */
	Integer getCodelist();

	/**
	 * Getter method for property.
	 * 
	 * @return the property
	 */
	String getProperty();

	/**
	 * Getter method for values.
	 * 
	 * @return the values
	 */
	Set<String> getValues();
}