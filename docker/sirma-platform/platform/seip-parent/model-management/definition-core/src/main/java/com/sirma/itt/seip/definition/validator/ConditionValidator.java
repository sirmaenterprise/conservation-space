package com.sirma.itt.seip.definition.validator;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Recursively traverses the definition and validates all the conditions located in:
 * - definition fields (root level)
 * - fields of all regions
 * - transitions
 * <p>
 * Warning: the conditions of state transitions are not validated because they are used
 * for other purposes.
 * <p>
 * The validation consists of checking if all the fields used in the expression are
 * existing fields from the definition that are either editable, readonly, hidden or system.
 *
 * @author Adrian Mitev
 */
public class ConditionValidator implements DefinitionValidator {

	private static final Pattern ASCII_CHARACTER_PATTERN = Pattern.compile("^[\\x20-\\x7E\\r\\n\\t]+$");

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		ConditionValidatorMessageBuilder messageBuilder = new ConditionValidatorMessageBuilder(definition);

		Set<String> allFieldNames = collectFieldNames(definition);

		validateExpression(definition.getExpression(), allFieldNames, messageBuilder);

		definition.fieldsStream().forEach(field -> validateConditions(field, allFieldNames, messageBuilder));

		definition.getTransitions().forEach(transition -> validateConditions(transition, allFieldNames, messageBuilder));

		return messageBuilder.getMessages();
	}

	private static void validateConditions(Conditional model, Set<String> definitionFieldNames,
			ConditionValidatorMessageBuilder messageBuilder) {
		model.getConditions()
				.forEach(condition -> validateExpression(condition.getExpression(), definitionFieldNames, messageBuilder));
	}

	private static void validateExpression(String expression, Set<String> definitionFieldNames,
			ConditionValidatorMessageBuilder messageBuilder) {
		if (expression == null) {
			return;
		}

		if (StringUtils.isBlank(expression)) {
			messageBuilder.emptyExpression();
			return;
		}

		if (definitionFieldNames.isEmpty()) {
			messageBuilder.missingFieldsForExpression(expression);
			return;
		}

		if (!ASCII_CHARACTER_PATTERN.matcher(expression).matches()) {
			messageBuilder.nonAsciiExpression(expression);
			return;
		}

		Set<String> usedFieldsInExpression = new LinkedHashSet<>(DefinitionUtil.getRncFields(expression));

		if (usedFieldsInExpression.isEmpty()) {
			messageBuilder.expressionWithoutFields(expression);

			// TODO: Fix FIELD_PATTERN for accepting new condition
			// +n[i-documentType], +n[o-documentType] and negative values
			return;
		}

		// all fields used in the expression should exist in the definition
		if (!definitionFieldNames.containsAll(usedFieldsInExpression)) {
			// retain only the missing fields
			usedFieldsInExpression.removeAll(definitionFieldNames);
			messageBuilder.missingRequiredExpressionFields(expression, usedFieldsInExpression);
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

	public class ConditionValidatorMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String EMPTY_EXPRESSION = "definition.validation.expression.empty";
		public static final String MISSING_FIELDS_FOR_EXPRESSION = "definition.validation.expression.missing.fields";
		public static final String NON_ASCII_EXPRESSION = "definition.validation.expression.non.ascii";
		public static final String EXPRESSION_WITHOUT_FIELDS = "definition.validation.expression.no.fields";
		public static final String MISSING_REQUIRED_EXPRESSION_FIELDS = "definition.validation.expression.missing.required.fields";

		public ConditionValidatorMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void emptyExpression() {
			error(getId(), EMPTY_EXPRESSION, getId());
		}

		private void missingFieldsForExpression(String expression) {
			error(getId(), MISSING_FIELDS_FOR_EXPRESSION, getId(), expression);
		}

		private void nonAsciiExpression(String expression) {
			error(getId(), NON_ASCII_EXPRESSION, getId(), expression);
		}

		private void expressionWithoutFields(String expression) {
			error(getId(), EXPRESSION_WITHOUT_FIELDS, getId(), expression);
		}

		private void missingRequiredExpressionFields(String expression, Set<String> missingFields) {
			error(getId(), MISSING_REQUIRED_EXPRESSION_FIELDS, getId(), expression, missingFields);
		}
	}
}
