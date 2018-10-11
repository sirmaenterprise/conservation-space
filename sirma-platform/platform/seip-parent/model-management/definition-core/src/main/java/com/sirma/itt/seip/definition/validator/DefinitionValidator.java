package com.sirma.itt.seip.definition.validator;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;

/**
 * Defines methods for definitions validating. The implementation must support {@link RegionDefinitionModel},
 * {@link DefinitionModel} and common {@link Identity} model
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
	default List<String> validate(RegionDefinitionModel model) {
		return Collections.emptyList();
	}

	/**
	 * Validates the given {@link GenericDefinition}
	 *
	 * @param model
	 *            the model
	 * @return true, if valid
	 */
	default List<String> validate(DefinitionModel model) {
		return Collections.emptyList();
	}

	/**
	 * Validates the given {@link Identity}
	 *
	 * @param model
	 *            the model
	 * @return true, if valid
	 */
	default List<String> validate(Identity model) {
		return Collections.emptyList();
	}
}
