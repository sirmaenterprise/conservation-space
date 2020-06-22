package com.sirma.sep.model;

import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.domain.validation.ValidationMessageBuilder;
import com.sirma.itt.seip.domain.validation.ValidationReport;

public class ModelImportValidationBuilder extends ValidationMessageBuilder {

	public static final String TIMEOUT = "model.import.timeout";

	public ValidationReport importTimeout() {
		return add(ValidationMessage.error(TIMEOUT)).build();
	}

	public ValidationReport importException(Exception ex) {
		return add(ValidationMessage.error(ex.getMessage())).build();
	}
}
