package com.sirma.itt.seip.definition.validator;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.itt.seip.expressions.ElExpressionParser;

/**
 * Definition validator that validates expressions in fields, control-params and labels.
 *
 * @author BBonev
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
public class ELExpressionValidator implements DefinitionValidator {

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		ELExpressionMessageBuilder messageBuilder = new ELExpressionMessageBuilder(definition);

		definition.fieldsStream().forEach(propertyDefinition -> {
			String defaultValue = propertyDefinition.getDefaultValue();
			if (!isExpressionValid(defaultValue)) {
				messageBuilder.invalidValueExpression(propertyDefinition.getName(), defaultValue);
			}

			String rnc = propertyDefinition.getRnc();
			if (!isExpressionValid(rnc)) {
				messageBuilder.invalidRncExpression(propertyDefinition.getName(), rnc);
			}

			if (!isControlParamExpressionValid(propertyDefinition)) {
				messageBuilder.invalidControlExpression(propertyDefinition.getName());
			}
		});

		return messageBuilder.getMessages();
	}

	private static boolean isControlParamExpressionValid(PropertyDefinition property) {
		if (property.getControlDefinition() == null) {
			return true;
		}
		List<ControlParam> controlParams = property.getControlDefinition().getControlParams();
		if (controlParams == null || controlParams.isEmpty()) {
			return true;
		}
		Optional<ControlParam> error = controlParams.stream()
				.filter(param -> DefinitionCompilerHelper.DEFAULT_VALUE_PATTERN_TYPE.equalsIgnoreCase(param.getType()))
				.filter(param -> "template".equalsIgnoreCase(param.getIdentifier()))
				// we check both value (for function expressions and name for property bindings.
				.filter(param -> !StringUtils.isBlank(param.getValue()) && !areBracketsValid(param.getValue()))
				.findAny();

		return !error.isPresent();
	}

	private static boolean isExpressionValid(String expression) {
		if (StringUtils.isBlank(expression)) {
			return true;
		}
		if (!ElExpressionParser.isExpression(expression)) {
			return true;
		}
		return areBracketsValid(expression);
	}

	/**
	 * Validates if an expression with parentheses is balanced. Algorithm based on the reverse polish notation.
	 *
	 * @param expression the expression as a string
	 * @return true if valid expression, false otherwise.
	 */
	private static boolean areBracketsValid(String expression) {
		HashMap<Character, Character> bracketsMapping = new HashMap<>();
		bracketsMapping.put('(', ')');
		bracketsMapping.put('[', ']');
		bracketsMapping.put('{', '}');

		Set<Character> closingBrackets = new HashSet<>(Arrays.asList(')', ']', '}'));

		Deque<Character> stack = new ArrayDeque<>();
		for (int i = 0; i < expression.length(); i++) {
			char curr = expression.charAt(i);
			if (bracketsMapping.containsKey(curr)) {
				stack.push(curr);
			} else if (closingBrackets.contains(curr)) {
				if (!stack.isEmpty() && bracketsMapping.get(stack.peek()) == curr) {
					stack.pop();
				} else {
					return false;
				}
			}
		}
		return stack.isEmpty();
	}

	public class ELExpressionMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String INVALID_VALUE_EXPRESSION = "definition.validation.expression.invalid.value";
		public static final String INVALID_RNC_VALUE = "definition.validation.expression.invalid.rnc";
		public static final String INVALID_CONTROL_EXPRESSION = "definition.validation.expression.invalid.control.value";

		public ELExpressionMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void invalidValueExpression(String fieldName, String fieldValue) {
			error(getId(), INVALID_VALUE_EXPRESSION, getId(), fieldName, fieldValue);
		}

		private void invalidRncExpression(String fieldName, String rnc) {
			error(getId(), INVALID_RNC_VALUE, getId(), fieldName, rnc);
		}

		private void invalidControlExpression(String fieldName) {
			error(getId(), INVALID_CONTROL_EXPRESSION, getId(), fieldName);
		}

	}
}
