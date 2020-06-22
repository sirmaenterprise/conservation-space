package com.sirma.itt.seip.definition.validator;

import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessageBuilder;

public abstract class DefinitionValidationMessageBuilder extends ValidationMessageBuilder {

	private final GenericDefinition genericDefinition;

	public DefinitionValidationMessageBuilder(GenericDefinition genericDefinition) {
		this.genericDefinition = genericDefinition;
	}

	protected String getId() {
		return this.genericDefinition.getIdentifier();
	}
}
