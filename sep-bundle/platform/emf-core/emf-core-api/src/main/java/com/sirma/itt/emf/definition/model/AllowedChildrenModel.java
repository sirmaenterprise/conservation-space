package com.sirma.itt.emf.definition.model;

import java.util.List;

/**
 * Base common model that defines a list of allowed child definitions.
 * 
 * @author BBonev
 */
public interface AllowedChildrenModel {

	/**
	 * Gets the allowed children.
	 * 
	 * @return the allowed children
	 */
	List<AllowedChildDefinition> getAllowedChildren();

}
