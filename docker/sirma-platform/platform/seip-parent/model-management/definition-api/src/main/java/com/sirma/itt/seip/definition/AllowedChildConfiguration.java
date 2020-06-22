package com.sirma.itt.seip.definition;

import java.util.Set;

import com.sirma.itt.seip.domain.definition.FilterMode;

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
	 * Gets the filter mode used to compare the incoming values
	 *
	 * @return the filter mode
	 */
	FilterMode getFilterMode();

	/**
	 * Getter method for values.
	 *
	 * @return the values
	 */
	Set<String> getValues();

	/**
	 * Gets the original xml value.
	 *
	 * @return the xml values
	 */
	String getXmlValues();
}