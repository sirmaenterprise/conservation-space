package com.sirma.itt.seip.definition.validator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.ValidationLoggingUtil;
import com.sirma.itt.seip.definition.compile.DefinitionCompilerHelper;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.expressions.ElExpressionParser;

/**
 * Definition validator that validates expressions in fields, control-params and labels.
 *
 * @author BBonev
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
public class ELExpressionValidator implements DefinitionValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ELExpressionValidator.class);

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		List<String> errors = new ArrayList<>();

		for (RegionDefinition regionDefinition : model.getRegions()) {
			errors.addAll(validate(regionDefinition));
			if (regionDefinition.getControlDefinition() != null) {
				errors.addAll(validate(regionDefinition.getControlDefinition()));
			}
		}

		return errors;
	}

	@Override
	public List<String> validate(DefinitionModel model) {
		List<String> errors = new ArrayList<>();

		for (PropertyDefinition propertyDefinition : model.getFields()) {
			errors.addAll(validate(propertyDefinition));
			if (propertyDefinition.getControlDefinition() != null) {
				errors.addAll(validate(propertyDefinition.getControlDefinition()));
			}
		}

		return errors;
	}

	@Override
	public List<String> validate(Identity model) {
		List<String> errors = new ArrayList<>();

		if (model instanceof PropertyDefinition) {
			PropertyDefinition definition = (PropertyDefinition) model;
			String defaultValue = definition.getDefaultValue();
			String rnc = definition.getRnc();
			if (!isExpressionValid(defaultValue)) {
				String message = "Found invalid expression in default value of the field: " + definition.getIdentifier() + " -> "
						+ defaultValue;
				errors.add(message);
				addAndLogErrorMessage(message);
			}
			if (!isExpressionValid(rnc)) {
				String message = "Found invalid expression in RNC value of the field: " + definition.getIdentifier() + " -> "
						+ rnc;
				errors.add(message);
				addAndLogErrorMessage(message);
			}
			if (!isControlParamExpressionValid(definition)) {
				String message = "Found invalid expression in <control-param> tag in a field with name: "
						  + definition.getIdentifier();
				errors.add(message);
				addAndLogErrorMessage(message);
			}
		} else if (model instanceof LabelDefinition) {
			LabelDefinition definition = (LabelDefinition) model;
			Map<String, String> map = definition.getLabels();
			for (Entry<String, String> entry : map.entrySet()) {
				if (!isExpressionValid(entry.getValue())) {
					String message =
							"Found invalid expression in label: " + definition.getIdentifier() + " for language "
									+ entry.getKey();

					errors.add(message);
					addAndLogErrorMessage(message);
				}
			}
		}

		return errors;
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

	/**
	 * Logs the message as warning and adds the is as error to the validation messages.
	 *
	 * @param message
	 * 		the message
	 */
	private static void addAndLogErrorMessage(String message) {
		LOGGER.warn(message);
		ValidationLoggingUtil.addErrorMessage(message);
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
	 * @param expression
	 * 		the expression as a string
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
}
