package com.sirma.itt.seip.definition.validator;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Validates the values of definition fields. This includes regular fields, transition fields as well as region fields.
 *
 * @author Vilizar Tsonev
 */
public class FieldValueValidator implements DefinitionValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static Map<String, Function<PropertyDefinition, String>> fieldValueValidators = new HashMap<>(2);

	static {
		fieldValueValidators.put("json", FieldValueValidator::validateJsonTypeField);
		fieldValueValidators.put("boolean", FieldValueValidator::validateBooleanTypeField);
	}

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		return this.validate((DefinitionModel) model);
	}

	@Override
	public List<String> validate(DefinitionModel model) {
		List<String> errors = new ArrayList<>();
		if (model instanceof GenericDefinition) {
			GenericDefinition genericDefinition = (GenericDefinition) model;
			String defId = genericDefinition.getIdentifier();
			errors.addAll(validateFields(genericDefinition.getConfigurations(), "configuration", defId));
			errors.addAll(validateFields(genericDefinition.getFields(), "fields", defId));
			errors.addAll(validateRegions(genericDefinition.getRegions(), defId));
			errors.addAll(validateTransitions(genericDefinition.getTransitions(), defId));
		}
		return errors;
	}

	private static List<String> validateFields(List<PropertyDefinition> fields, String parentElement,
			String definitionId) {
		return fields.stream()
				.filter(field -> StringUtils.isNotBlank(field.getDefaultValue()))
				.map(FieldValueValidator::validateField)
				.filter(Objects::nonNull)
				.map(fieldErrorMessage -> FieldValueValidator.formatErrorMessage(fieldErrorMessage, parentElement,
																				 definitionId))
				.collect(Collectors.toList());
	}

	private static List<String> validateRegions(List<RegionDefinition> regions, String defId) {
		return regions.stream()
				.map(region -> FieldValueValidator.validateFields(region.getFields(),
																	  "region '" + region.getIdentifier() + "'",
																	  defId))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private static List<String> validateTransitions(List<TransitionDefinition> transitions, String defId) {
		return transitions.stream()
				.map(transition -> FieldValueValidator.validateFields(transition.getFields(),
																	  "transition '" + transition.getIdentifier() + "'",
																	  defId))
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	private static String validateField(PropertyDefinition field) {
		Function<PropertyDefinition, String> fieldValueValidator = fieldValueValidators.get(field.getType());
		if (fieldValueValidator != null) {
			return fieldValueValidator.apply(field);
		}
		return null;
	}

	private static String validateJsonTypeField(PropertyDefinition field) {
		String value = field.getDefaultValue();
		try (JsonReader reader = Json.createReader(new StringReader(value))) {
			reader.readObject();
		} catch (JsonParsingException e) {
			return "in the JSON value of field '" + field.getName() + "' : " + e.getMessage();
		}
		return null;
	}

	private static String validateBooleanTypeField(PropertyDefinition field) {
		String defaultValue = field.getDefaultValue();
		if (!Boolean.FALSE.toString().equals(defaultValue) && !Boolean.TRUE.toString().equals(defaultValue)) {
			return "Value: '" + defaultValue + "' of field " + field.getName() + " is invalid for type boolean ";
		}
		return null;
	}

	private static String formatErrorMessage(String fieldErrorMessage, String parentElement, String definitionId) {
		String errorMessage = "In " + parentElement + ", " + fieldErrorMessage;
		LOGGER.debug("Detected error in definition {} {}", definitionId, errorMessage);
		return errorMessage;
	}
}