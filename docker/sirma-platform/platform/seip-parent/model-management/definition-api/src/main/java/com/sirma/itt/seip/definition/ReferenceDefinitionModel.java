package com.sirma.itt.seip.definition;

/**
 * Model that specifies a model that could be a reference to other models
 *
 * @author BBonev
 */
public interface ReferenceDefinitionModel {

	/**
	 * Gets the reference id. This field to reference other definition when building model.
	 *
	 * @return the reference id
	 */
	String getReferenceId();

}
