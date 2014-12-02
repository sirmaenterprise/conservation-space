package com.sirma.itt.emf.domain.model;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.emf.definition.model.PropertyDefinition;

/**
 * Common definition model for all definitions. Defines list of properties
 *
 * @author BBonev
 */
public interface DefinitionModel extends Identity, Serializable, HashableDefinition, Node {

	/**
	 * Gets the list of field definitions.
	 *
	 * @return the fields
	 */
	List<PropertyDefinition> getFields();

	/**
	 * Gets the revision.
	 * 
	 * @return the revision
	 */
	Long getRevision();
}
