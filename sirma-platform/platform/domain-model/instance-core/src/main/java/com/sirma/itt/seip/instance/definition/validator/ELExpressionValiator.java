package com.sirma.itt.seip.instance.definition.validator;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.definition.compile.DefinitionValidator;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;
import com.sirma.itt.seip.expressions.ElExpressionParser;

/**
 * Definition validator that validates expressions in fields and labels
 *
 * @author BBonev
 */
public class ELExpressionValiator implements DefinitionValidator {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ELExpressionValiator.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(RegionDefinitionModel model) {
		boolean valid = true;
		for (RegionDefinition regionDefinition : model.getRegions()) {
			valid &= validate(regionDefinition);
			if (regionDefinition.getControlDefinition() != null) {
				valid &= validate(regionDefinition.getControlDefinition());
			}
		}
		return valid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(DefinitionModel model) {
		boolean valid = true;
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			valid &= validate(propertyDefinition);
			if (propertyDefinition.getControlDefinition() != null) {
				valid &= validate(propertyDefinition.getControlDefinition());
			}
		}
		return valid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(Identity model) {
		boolean valid = true;
		if (model instanceof PropertyDefinition) {
			PropertyDefinition definition = (PropertyDefinition) model;
			String defaultValue = definition.getDefaultValue();
			String rnc = definition.getRnc();
			if (!validateExpression(defaultValue)) {
				valid = false;
				addAndLogErrorMessage("Found invalid expression in default value of the field: "
						+ definition.getIdentifier() + " -> " + defaultValue);
			}
			if (!validateExpression(rnc)) {
				valid = false;
				addAndLogErrorMessage("Found invalid expression in RNC value of the field: "
						+ definition.getIdentifier() + " -> " + rnc);
			}
		} else if (model instanceof LabelDefinition) {
			LabelDefinition definition = (LabelDefinition) model;
			Map<String, String> map = definition.getLabels();
			for (Entry<String, String> entry : map.entrySet()) {
				if (!validateExpression(entry.getValue())) {
					String message = "Found invalid expression in label: " + definition.getIdentifier()
							+ " for language " + entry.getKey();
					addAndLogErrorMessage(message);
					valid = false;
				}
			}
		}
		return valid;
	}

	/**
	 * Logs the message as warning and adds the is as error to the validation messages.
	 *
	 * @param message
	 *            the message
	 */
	private void addAndLogErrorMessage(String message) {
		LOGGER.warn(message);
		ValidationLoggingUtil.addErrorMessage(message);
	}

	/**
	 * Validate expression.
	 *
	 * @param expression
	 *            the expression
	 * @return true, if successful
	 */
	private boolean validateExpression(String expression) {
		if (StringUtils.isNullOrEmpty(expression)) {
			return true;
		}
		if (!ElExpressionParser.isExpression(expression)) {
			return true;
		}
		String openCBracket = expression.replaceAll("\\{", "");
		String closeCBracket = expression.replaceAll("\\}", "");

		String openBracket = expression.replaceAll("\\(", "");
		String closeBracket = expression.replaceAll("\\)", "");

		int length = expression.length();
		return length - openCBracket.length() == length - closeCBracket.length()
				&& length - openBracket.length() == length - closeBracket.length();
	}

}
