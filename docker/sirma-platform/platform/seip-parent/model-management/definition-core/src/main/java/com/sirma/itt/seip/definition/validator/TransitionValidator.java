package com.sirma.itt.seip.definition.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Validator for transitions.
 *
 * @author Boyan Tonchev.
 */
public class TransitionValidator implements DefinitionValidator {

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		TransitionValidatorMessageBuilder messageBuilder = new TransitionValidatorMessageBuilder(definition);
		definition.getTransitions()
				.forEach(transitionDefinition -> checkForDuplicateFieldName(transitionDefinition, messageBuilder));
		return messageBuilder.getMessages();
	}

	private static void checkForDuplicateFieldName(TransitionDefinition transition, TransitionValidatorMessageBuilder messageBuilder) {
		Set<String> transitionFieldNames = new HashSet<>();
		transition.fieldsStream()
				.map(PropertyDefinition::getName)
				.filter(fieldName -> !transitionFieldNames.add(fieldName))
				.forEach(duplicatedField ->
						messageBuilder.duplicatedFieldsInTransition(transition.getIdentifier(), duplicatedField));
	}

	public class TransitionValidatorMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String DUPLICATE_TRANSITION_FIELD = "definition.validation.transition.duplicated.field";

		public TransitionValidatorMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void duplicatedFieldsInTransition(String transitionId, String duplicatedField) {
			error(getId(), DUPLICATE_TRANSITION_FIELD, getId(), transitionId, duplicatedField);
		}
	}
}
