package com.sirma.itt.emf.definition.load;

import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Identity;

/**
 * Defines methods for definitions validating. The implementation must support
 * {@link RegionDefinitionModel}, {@link DefinitionModel} and common {@link Identity} model
 *
 * @author BBonev
 */
public interface DefinitionValidator {

	/**
	 * Validates the given {@link RegionDefinitionModel}
	 *
	 * @param model
	 *            the model to check
	 * @return true, if valid
	 */
	boolean validate(RegionDefinitionModel model);

	/**
	 * Validates the given {@link DefinitionModel}
	 *
	 * @param model
	 *            the model
	 * @return true, if valid
	 */
	boolean validate(DefinitionModel model);

	/**
	 * Validates the given {@link Identity}
	 * 
	 * @param model
	 *            the model
	 * @return true, if valid
	 */
	boolean validate(Identity model);
}
