package com.sirma.itt.seip.definition.validator;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.TriConsumer;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.domain.validation.ValidationMessageBuilder;

/**
 * Validates the values of definition fields. This includes regular fields, transition fields as well as region fields.
 *
 * @author Vilizar Tsonev
 */
public class FieldValueValidator implements DefinitionValidator {

	private static Map<String, TriConsumer<String, PropertyDefinition, FieldValueMessageBuilder>> fieldValueValidators;

	static {
		fieldValueValidators = new HashMap<>(2);
		fieldValueValidators.put("json", FieldValueValidator::validateJsonTypeField);
		fieldValueValidators.put("boolean", FieldValueValidator::validateBooleanTypeField);
	}

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		FieldValueMessageBuilder messageBuilder = new FieldValueMessageBuilder(definition);
		String defId = definition.getIdentifier();
		validateFields(definition.getConfigurations(), defId, messageBuilder);
		validateFields(definition.getFields(), defId, messageBuilder);
		validateRegions(definition.getRegions(), defId, messageBuilder);
		validateTransitions(definition.getTransitions(), defId, messageBuilder);
		return messageBuilder.getMessages();
	}

	private static void validateFields(List<PropertyDefinition> fields, String definitionId, FieldValueMessageBuilder messageBuilder) {
		fields.stream()
				.filter(field -> StringUtils.isNotBlank(field.getDefaultValue()))
				.forEach(field -> FieldValueValidator.validateField(definitionId, field, messageBuilder));
	}

	private static void validateRegions(List<RegionDefinition> regions, String defId, FieldValueMessageBuilder messageBuilder) {
		regions.stream()
				.forEach(region -> FieldValueValidator.validateFields(region.getFields(), defId, messageBuilder));
	}

	private static void validateTransitions(List<TransitionDefinition> transitions, String defId,
			FieldValueMessageBuilder messageBuilder) {
		transitions.stream()
				.forEach(transition -> FieldValueValidator.validateFields(transition.getFields(), defId, messageBuilder));
	}

	private static void validateField(String definitionId, PropertyDefinition field, FieldValueMessageBuilder messageBuilder) {
		TriConsumer<String, PropertyDefinition, FieldValueMessageBuilder> fieldValueValidator = fieldValueValidators
				.get(field.getType());
		if (fieldValueValidator != null) {
			fieldValueValidator.accept(definitionId, field, messageBuilder);
		}
	}

	private static void validateJsonTypeField(String definitionId, PropertyDefinition field,
			FieldValueMessageBuilder messageBuilder) {
		String value = field.getDefaultValue();
		try (JsonReader reader = Json.createReader(new StringReader(value))) {
			reader.readObject();
		} catch (JsonParsingException e) {
			messageBuilder.invalidJson(definitionId, field.getName(), e.getMessage());
		}
	}

	private static void validateBooleanTypeField(String definitionId, PropertyDefinition field,
			FieldValueMessageBuilder messageBuilder) {
		String defaultValue = field.getDefaultValue();
		if (!Boolean.FALSE.toString().equals(defaultValue) && !Boolean.TRUE.toString().equals(defaultValue)) {
			messageBuilder.invalidBooleanFieldValue(definitionId, field.getName(), defaultValue);
		}
	}

	public class FieldValueMessageBuilder extends ValidationMessageBuilder {

		private final GenericDefinition definition;

		public static final String FIELD_WITH_INVALID_JSON = "definition.validation.field.invalid.json";
		public static final String FIELD_WITH_INVALID_BOOLEAN = "definition.validation.field.invalid.boolean";

		public FieldValueMessageBuilder(GenericDefinition definition) {
			this.definition = definition;
		}

		private void invalidJson(String definitionId, String fieldName, String causeMessage) {
			error(definitionId, FIELD_WITH_INVALID_JSON, definitionId, fieldName, causeMessage);
		}

		private void invalidBooleanFieldValue(String definitionId, String fieldName, String invalidValue) {
			error(definitionId, FIELD_WITH_INVALID_BOOLEAN, definitionId, fieldName, invalidValue);
		}
	}
}