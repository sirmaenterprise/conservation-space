package com.sirma.itt.seip.definition.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Recursively traverses the definition and validates all the conditions located in:
 * - definition fields (root level)
 * - fields of all regions
 * - transitions
 *
 * Warning: the conditions of state transitions are not validated beause they are used
 * for other purposes.
 *
 * The validation consists of checking if all the fields used in the expression are
 * existing fields from the definition that are either editable, readonly, hidden or system.
 *
 * @author Adrian Mitev
 */
public class ConditionValidator implements DefinitionValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConditionValidator.class);
	private static final Pattern ASCII_CHARACTER_PATTERN = Pattern.compile("^[\\x20-\\x7E\\r\\n\\t]+$");

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		if (!(model instanceof GenericDefinition)) {
			return Collections.emptyList();
		}

		return validate((GenericDefinition) model);
	}

	@Override
	public List<String> validate(DefinitionModel model) {
		if (!(model instanceof GenericDefinition)) {
			return Collections.emptyList();
		}

		return validate((GenericDefinition) model);
	}

	private static List<String> validate(GenericDefinition definition) {
		List<String> errors = new ArrayList<>();

		Set<String> allFieldNames = collectFieldNames(definition);

		validateExpression(definition.getExpression(), allFieldNames, errors);

		definition.fieldsStream().forEach(field -> validateConditions(field, allFieldNames, errors));

		definition.getTransitions().forEach(transition -> validateConditions(transition, allFieldNames, errors));

		return errors;
	}

	private static void validateConditions(Conditional model, Set<String> definitionFieldNames, List<String> errors) {
		model.getConditions().forEach(condition -> validateExpression(condition.getExpression(), definitionFieldNames, errors));
	}

	private static void validateExpression(String expression, Set<String> definitionFieldNames, List<String> errors) {
		if (expression == null) {
			return;
		}

		if (StringUtils.isBlank(expression)) {
			String message = "Expression should not be empty";
			errors.add(message);
			LOGGER.warn(message);
			return;
		}

		if (definitionFieldNames.isEmpty()) {
			String message = "No fields in the target model to support the expression: " + expression;
			errors.add(message);
			LOGGER.warn(message);
			return;
		}

		if (!ASCII_CHARACTER_PATTERN.matcher(expression).matches()) {
			String message = "Found cyrillic characters in the expression: " + expression;
			errors.add(message);
			LOGGER.warn(message);
			return;
		}

		Set<String> usedFieldsInExpression = new LinkedHashSet<>(DefinitionUtil.getRncFields(expression));

		if (usedFieldsInExpression.isEmpty()) {
			String message = "No fields found into the expression: " + expression;
			errors.add(message);
			LOGGER.warn(message);
			// TODO: Fix FIELD_PATTERN for accepting new condition
			// +n[i-documentType], +n[o-documentType] and negative values
			return;
		}

		// all fields used in the expression should exist in the definition
		if (!definitionFieldNames.containsAll(usedFieldsInExpression)) {
			// retain only the missing fields
			usedFieldsInExpression.removeAll(definitionFieldNames);

			String message = "The fields " + usedFieldsInExpression +" in the expression " + expression + " are not found into the target model!";
			errors.add(message);
			LOGGER.warn(message);
		}
	}

	private static Set<String> collectFieldNames(GenericDefinition model) {
		Set<DisplayType> allowedTypes = EnumSet.of(DisplayType.EDITABLE, DisplayType.READ_ONLY, DisplayType.HIDDEN,
				DisplayType.SYSTEM);

		return model.fieldsStream()
					.flatMap(PropertyDefinition::stream)
					.filter(property -> allowedTypes.contains(property.getDisplayType()))
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());
	}
}
