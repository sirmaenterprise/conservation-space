package com.sirma.itt.seip.definition.validator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.domain.validation.ValidationMessageBuilder;

/**
 * Validator class that check for different fields bound to same uri.
 *
 * @author tdossev
 */
public class DuplicateUriValidator implements DefinitionValidator {

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		Map<String, Set<String>> uriMapping = definition.fieldsStream()
				.filter(PropertyDefinition.hasUri())
				.collect(Collectors.groupingBy(PropertyDefinition.resolveUri(),
						Collectors.mapping(PropertyDefinition::getName, Collectors.toSet())));

		DuplicateUriMessageBuilder messageBuilder = new DuplicateUriMessageBuilder();

		for (Entry<String, Set<String>> entry : uriMapping.entrySet()) {
			if (entry.getValue().size() > 1) {
				messageBuilder.duplicatedUri(definition.getIdentifier(), entry.getKey(), entry.getValue());
			}
		}

		return messageBuilder.getMessages();
	}

	public class DuplicateUriMessageBuilder extends ValidationMessageBuilder {

		public static final String DUPLICATED_URI = "definition.validation.duplicated.field.uri";

		private void duplicatedUri(String definitionId, String uri, Set<String> fields) {
			error(definitionId, DUPLICATED_URI, definitionId, uri, fields.toString());
		}
	}

}
