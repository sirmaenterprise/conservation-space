package com.sirma.itt.seip.definition.validator;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Defines methods for definitions validating.
 *
 * @author BBonev
 */
public interface DefinitionValidator {

	/**
	 * Validates the given {@link GenericDefinition}
	 *
	 * @param definition the definition to validate
	 * @return list of {@link ValidationMessage}; if empty then the definition is valid
	 */
	default List<ValidationMessage> validate(GenericDefinition definition) {
		return Collections.emptyList();
	}
}
