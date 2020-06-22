package com.sirma.itt.seip.definition.validator;

import java.util.List;
import java.util.regex.Pattern;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Validator class that checks the identifiers.
 *
 * @author BBonev
 */
public class InvalidIdValidator implements DefinitionValidator {

	private static final String ASCII_CHAR = "[\\x20-\\x7E\\r\\n\\t:]";
	private static final Pattern ID_PATTERN = Pattern.compile("[\\w$:]+", Pattern.UNICODE_CHARACTER_CLASS);
	private static final Pattern ASCII_CHARACTER_PATTERN = Pattern.compile("^" + ASCII_CHAR + "+$");
	private static final Pattern ASCII_CHAR_PATTERN = Pattern.compile(ASCII_CHAR);

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		InvalidIdMessageBuilder messageBuilder = new InvalidIdMessageBuilder(definition);

		// TODO: configs ?
		// TODO: control param id ?
		validateDefinitionIdentifier(definition.getIdentifier(), messageBuilder);

		validateFields(definition, messageBuilder);

		validateRegions(definition, messageBuilder);

		return messageBuilder.getMessages();
	}

	private void validateFields(GenericDefinition definition, InvalidIdMessageBuilder messageBuilder) {
		// Root + region fields
		definition.fieldsStream()
				.forEach(propertyDefinition -> {
					validateIdentifier(propertyDefinition.getIdentifier(), "<field>", messageBuilder);
					validateConditions(propertyDefinition.getConditions(), messageBuilder);
					validateControl(propertyDefinition.getControlDefinition(), messageBuilder);
				});
	}

	private void validateRegions(GenericDefinition definition, InvalidIdMessageBuilder messageBuilder) {
		definition.getRegions().forEach(regionDefinition -> {
			validateIdentifier(regionDefinition.getIdentifier(), "<region>", messageBuilder);
			validateConditions(regionDefinition.getConditions(), messageBuilder);
			validateControl(regionDefinition.getControlDefinition(), messageBuilder);
		});
	}

	private void validateControl(ControlDefinition controlDefinition, InvalidIdMessageBuilder messageBuilder) {
		if (controlDefinition != null) {
			validateIdentifier(controlDefinition.getIdentifier(), "<control>", messageBuilder);
		}
	}

	private void validateConditions(List<Condition> conditions, InvalidIdMessageBuilder messageBuilder) {
		if (CollectionUtils.isNotEmpty(conditions)) {
			conditions.forEach(condition -> validateIdentifier(condition.getIdentifier(), "<condition>", messageBuilder));
		}
	}

	private void validateDefinitionIdentifier(String definitionId, InvalidIdMessageBuilder messageBuilder) {
		if (!ASCII_CHARACTER_PATTERN.matcher(definitionId).matches()) {
			messageBuilder.nonAsciiDefinitionIdentifier(getNonAsciiCharacters(definitionId));
		} else if (!ID_PATTERN.matcher(definitionId).matches()) {
			// the ID should have only word characters [a-zA-Z0-9_:.]
			messageBuilder.nonWordCharactersDefinitionIdentifier();
		}
	}

	private void validateIdentifier(String identifier, String type, InvalidIdMessageBuilder messageBuilder) {
		if (!ASCII_CHARACTER_PATTERN.matcher(identifier).matches()) {
			messageBuilder.nonAsciiIdentifier(type, identifier, getNonAsciiCharacters(identifier));
		} else if (!ID_PATTERN.matcher(identifier).matches()) {
			// the ID should have only word characters [a-zA-Z0-9_:.]
			messageBuilder.nonWordCharactersIdentifier(type, identifier);
		}
	}

	private static String getNonAsciiCharacters(String identifier) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < identifier.length(); i++) {
			char c = identifier.charAt(i);
			if (!ASCII_CHAR_PATTERN.matcher("" + c).matches()) {
				builder.append(c).append(", ");
			}
		}
		builder.deleteCharAt(builder.length() - 1);
		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}

	public class InvalidIdMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String NON_ASCII_DEFINITION_ID = "definition.validation.non.ascii.definition.identifier";
		public static final String NON_ASCII_ID = "definition.validation.non.ascii.identifier";
		public static final String NON_WORD_CHARACTERS_DEFINITION_ID = "definition.validation.non.word.definition.identifier";
		public static final String NON_WORD_CHARACTERS_ID = "definition.validation.non.word.identifier";

		public InvalidIdMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void nonAsciiDefinitionIdentifier(String nonAsciiCharacters) {
			error(getId(), NON_ASCII_DEFINITION_ID, getId(), nonAsciiCharacters);
		}

		private void nonAsciiIdentifier(String type, String identifier, String nonAsciiCharacters) {
			error(getId(), NON_ASCII_ID, getId(), type, identifier, nonAsciiCharacters);
		}

		private void nonWordCharactersDefinitionIdentifier() {
			error(getId(), NON_WORD_CHARACTERS_DEFINITION_ID, getId());
		}

		private void nonWordCharactersIdentifier(String type, String identifier) {
			error(getId(), NON_WORD_CHARACTERS_ID, getId(), type, identifier);
		}
	}
}
