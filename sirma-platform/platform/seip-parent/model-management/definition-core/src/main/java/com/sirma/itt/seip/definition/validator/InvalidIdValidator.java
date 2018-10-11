package com.sirma.itt.seip.definition.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.ValidationLoggingUtil;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Validator class that checks the identifiers.
 *
 * @author BBonev
 */
public class InvalidIdValidator implements DefinitionValidator {

	public static final String ASCII_CHAR = "[\\x20-\\x7E\\r\\n\\t:]";
	private static final Pattern ID_PATTERN = Pattern.compile("[\\w$:]+", Pattern.UNICODE_CHARACTER_CLASS);
	private static final Pattern ASCII_CHARACTER_PATTERN = Pattern.compile("^" + ASCII_CHAR + "+$");
	private static final Pattern ASCII_CHAR_PATTERN = Pattern.compile(ASCII_CHAR);
	private static final Logger LOGGER = LoggerFactory.getLogger(InvalidIdValidator.class);

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		if (model == null) {
			return Collections.emptyList();
		}

		List<String> errors = new ArrayList<>();

		errors.addAll(validate((DefinitionModel) model));
		for (RegionDefinition regionDefinition : model.getRegions()) {
			errors.addAll(validate(regionDefinition));
			if (regionDefinition.getControlDefinition() != null) {
				errors.addAll(validate(regionDefinition.getControlDefinition()));
			}
			errors.addAll(validateCondition(regionDefinition));
		}
		return errors;
	}

	@Override
	public List<String> validate(DefinitionModel model) {
		if (model == null) {
			return Collections.emptyList();
		}

		List<String> errors = new ArrayList<>();

		errors.addAll(validate((Identity) model));
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			errors.addAll(validate(propertyDefinition));
			if (propertyDefinition.getControlDefinition() != null) {
				errors.addAll(validate(propertyDefinition.getControlDefinition()));
			}
			errors.addAll(validateCondition(propertyDefinition));
		}

		return errors;
	}

	private List<String> validateCondition(Conditional conditional) {
		List<String> errors = new ArrayList<>();

		if (conditional.getConditions() != null && !conditional.getConditions().isEmpty()) {
			for (Condition condition : conditional.getConditions()) {
				errors.addAll(validate(condition));
			}
		}
		return errors;
	}

	@Override
	public List<String> validate(Identity model) {
		if (model == null) {
			return Collections.emptyList();
		}

		List<String> errors = new ArrayList<>();

		// check for non ASCII characters
		String identifier = model.getIdentifier();

		if (!ASCII_CHARACTER_PATTERN.matcher(identifier).matches()) {
			StringBuilder builder = new StringBuilder("Found non ASCII character in ID='").append(identifier)
					.append("'. The invalid characters are: ");
			for (int i = 0; i < identifier.length(); i++) {
				char c = identifier.charAt(i);
				if (!ASCII_CHAR_PATTERN.matcher("" + c).matches()) {
					builder.append(c).append(", ");
				}
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.deleteCharAt(builder.length() - 1);

			String message = builder.toString();

			errors.add(message);
			LOGGER.error(message);
			ValidationLoggingUtil.addErrorMessage(message);
			return errors;
		}

		// skip validation for labels of the label id for word characters
		if (model instanceof LabelDefinition) {
			return errors;
		}

		// the ID should have only word characters [a-zA-Z0-9_:.]
		if (!ID_PATTERN.matcher(identifier).matches()) {
			String message = "Found non word character in ID='" + identifier + "'";
			errors.add(message);
			LOGGER.error(message);
			ValidationLoggingUtil.addErrorMessage(message);
		}

		return errors;
	}

}
