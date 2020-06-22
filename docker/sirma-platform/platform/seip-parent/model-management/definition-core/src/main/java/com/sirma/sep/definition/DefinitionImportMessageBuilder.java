package com.sirma.sep.definition;

import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.domain.validation.ValidationMessageBuilder;

public class DefinitionImportMessageBuilder extends ValidationMessageBuilder {

	public static final String DUPLICATED_DEFINITIONS = "definition.import.duplicated.definitions";
	public static final String DUPLICATED_FIELDS = "definition.import.duplicated.fields";
	public static final String DUPLICATED_LABELS = "definition.import.duplicated.labels";
	public static final String MISSING_PARENT = "definition.import.missing.parent";
	public static final String HIERARCHY_CYCLE = "definition.import.hierarchy.cycle";
	public static final String XML_PARSING_FAILURE = "definition.import.xml.parsing.failure";

	public void duplicatedDefinitions(String definitionId) {
		add(ValidationMessage.error(definitionId, DUPLICATED_DEFINITIONS).setParams(definitionId));
	}

	public void duplicatedFields(String definitionId, String fieldIdentifier) {
		add(ValidationMessage.error(definitionId, DUPLICATED_FIELDS).setParams(definitionId, fieldIdentifier));
	}

	public void duplicatedLabels(String definitionId, String labelIdentifier, String objectKind, Set<String> foundIn) {
		add(ValidationMessage.error(definitionId, DUPLICATED_LABELS)
				.setParams(definitionId, objectKind, labelIdentifier, foundIn.toString()));
	}

	public void missingParent(String definitionId, String parentIdentifier) {
		add(ValidationMessage.error(definitionId, MISSING_PARENT).setParams(definitionId, parentIdentifier));
	}

	public void hierarchyCycle(String definitionId, List<String> visited) {
		add(ValidationMessage.error(definitionId, HIERARCHY_CYCLE).setParams(definitionId, visited.toString()));
	}

	public void xmlParsingFailure(String fileName, List<String> errors) {
		// Such errors cannot be mapped to concrete definition
		error(null, XML_PARSING_FAILURE, fileName, errors.toString());
	}
}
