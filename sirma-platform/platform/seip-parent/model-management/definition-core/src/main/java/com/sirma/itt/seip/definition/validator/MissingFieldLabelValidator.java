package com.sirma.itt.seip.definition.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Validator class that checks for missing field label.
 *
 * @author Stella D
 */
public class MissingFieldLabelValidator implements DefinitionValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MissingFieldLabelValidator.class);

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		if (model == null) {
			return Collections.emptyList();
		}

		List<String> messages = new ArrayList<>();

		boolean valid = true;
		List<Pair<String, PropertyDefinition>> errors = new LinkedList<>();
		valid &= checkForMissingLabels(model, errors);
		if (model.getRegions() != null && !model.getRegions().isEmpty()) {
			for (RegionDefinition regionDefinition : model.getRegions()) {
				valid &= checkForMissingLabels(regionDefinition, errors);
			}
		}
		if (!valid) {
			printErrorMessages(model, errors, messages);
		}

		return messages;
	}

	private static void printErrorMessages(DefinitionModel model, List<Pair<String, PropertyDefinition>> errors,
			List<String> messages) {
		StringBuilder builder = new StringBuilder(errors.size() * 60);
		builder.append(
				"\n=======================================================================\nFound errors in definition: ")
				.append(model.getIdentifier()).append(" (missing labels):\n");
		for (Pair<String, PropertyDefinition> pair : errors) {
			builder.append(pair.getFirst()).append("\n");
		}
		builder.append("=======================================================================");
		String errorMessage = builder.toString();
		LOGGER.error(errorMessage);
		messages.add(errorMessage);
		ValidationLoggingUtil.addErrorMessage(errorMessage);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Found errors in {} the following fields: ", model.getIdentifier());
			for (Pair<String, PropertyDefinition> pair : errors) {
				LOGGER.trace(pair.getSecond().toString());
			}
			LOGGER.trace("End errors for {}", model.getIdentifier());
		}
	}

	private boolean checkForMissingLabels(DefinitionModel model, List<Pair<String, PropertyDefinition>> errors) {
		if (model == null) {
			return true;
		}
		boolean valid = true;
		if (model.getFields() != null && !model.getFields().isEmpty()) {
			for (PropertyDefinition propertyDefinition : model.getFields()) {
				if (!validatePropertyDefinition(propertyDefinition)) {
					String parentPath = propertyDefinition.getParentPath();
					String containerPath = parentPath != null ? parentPath + ":" : "";

					errors.add(new Pair<>(containerPath + propertyDefinition.getName(), propertyDefinition));
					valid = false;
				}
				valid &= checkForMissingLabels(propertyDefinition.getControlDefinition(), errors);
			}
		}
		return valid;
	}

	@Override
	public List<String> validate(DefinitionModel model) {
		if (model == null) {
			return Collections.emptyList();
		}

		List<String> messages = new ArrayList<>();
		boolean valid = true;
		List<Pair<String, PropertyDefinition>> errors = new LinkedList<>();
		valid &= checkForMissingLabels(model, errors);

		if (!valid) {
			printErrorMessages(model, errors, messages);
		}

		return messages;
	}

	private static boolean validatePropertyDefinition(PropertyDefinition propertyDefinition) {
		// if field is system and has no condition label is not mandatory
		if (propertyDefinition.getDisplayType() == DisplayType.SYSTEM && propertyDefinition.getConditions().isEmpty()) {
			return true;
		}
		return StringUtils.isNotBlank(propertyDefinition.getLabel())
				&& StringUtils.isNotBlank(propertyDefinition.getLabelId());

	}

}
