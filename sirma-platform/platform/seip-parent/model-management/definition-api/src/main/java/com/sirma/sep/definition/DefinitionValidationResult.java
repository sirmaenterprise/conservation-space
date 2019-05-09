package com.sirma.sep.definition;

import java.util.List;

import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.validation.ValidationReport;

/**
 * DTO carrying a validation report with errors that resulted during definitions validation and the compiled {@link GenericDefinition}
 * that were compiled and validated.
 *
 * @author Mihail Radkov
 */
public class DefinitionValidationResult {

	private final ValidationReport validationReport;
	private final List<GenericDefinition> definitions;

	public DefinitionValidationResult(ValidationReport validationReport, List<GenericDefinition> definitions) {
		this.validationReport = validationReport;
		this.definitions = definitions;
	}

	public ValidationReport getValidationReport() {
		return validationReport;
	}

	public List<GenericDefinition> getDefinitions() {
		return definitions;
	}
}
