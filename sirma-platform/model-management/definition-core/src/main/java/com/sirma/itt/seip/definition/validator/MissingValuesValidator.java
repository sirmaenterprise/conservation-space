/*
 *
 */
package com.sirma.itt.seip.definition.validator;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.compile.DefinitionValidator;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;

/**
 * Validator class that checks for missing fields.
 *
 * @author BBonev
 */
public class MissingValuesValidator implements DefinitionValidator {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MissingValuesValidator.class);
	/** The trace. */
	private boolean trace;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		trace = LOGGER.isTraceEnabled();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(RegionDefinitionModel model) {
		if (model == null) {
			return true;
		}
		boolean valid = true;
		List<Pair<String, PropertyDefinition>> errors = new LinkedList<Pair<String, PropertyDefinition>>();
		valid &= checkForMissingValues(model, errors);
		if (model.getRegions() != null && !model.getRegions().isEmpty()) {
			for (RegionDefinition regionDefinition : model.getRegions()) {
				valid &= checkForMissingValues(regionDefinition, errors);
			}
		}
		if (!valid) {
			printErrorMessages(model, errors);
		}
		return valid;
	}

	/**
	 * Prints the error messages.
	 *
	 * @param model
	 *            the model
	 * @param errors
	 *            the errors
	 */
	private void printErrorMessages(DefinitionModel model, List<Pair<String, PropertyDefinition>> errors) {
		StringBuilder builder = new StringBuilder(errors.size() * 60);
		builder
				.append("\n=======================================================================\nFound errors in definition: ")
					.append(model.getIdentifier())
					.append(" (missing types) :\n");
		for (Pair<String, PropertyDefinition> pair : errors) {
			builder.append(pair.getFirst()).append("\n");
		}
		builder.append("=======================================================================");
		LOGGER.error(builder.toString());
		ValidationLoggingUtil.addErrorMessage(builder.toString());
		if (trace) {
			LOGGER.trace("Found errors in {} the following fields: ", model.getIdentifier());
			for (Pair<String, PropertyDefinition> pair : errors) {
				LOGGER.trace(pair.getSecond().toString());
			}
			LOGGER.trace("End errors for {}", model.getIdentifier());
		}
	}

	/**
	 * Internal method for checking missing values.
	 *
	 * @param model
	 *            the model
	 * @param errors
	 *            the errors
	 * @return true, if valid
	 */
	private boolean checkForMissingValues(DefinitionModel model, List<Pair<String, PropertyDefinition>> errors) {
		if (model == null) {
			return true;
		}
		boolean valid = true;
		if (model.getFields() != null && !model.getFields().isEmpty()) {
			for (PropertyDefinition propertyDefinition : model.getFields()) {
				if (!validate(propertyDefinition)) {
					errors.add(new Pair<String, PropertyDefinition>(
							propertyDefinition.getParentPath() + ":" + propertyDefinition.getName(),
							propertyDefinition));
					valid = false;
				}
				valid &= checkForMissingValues(propertyDefinition.getControlDefinition(), errors);
			}
		}
		return valid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(DefinitionModel model) {
		if (model == null) {
			return true;
		}
		boolean valid = true;
		List<Pair<String, PropertyDefinition>> errors = new LinkedList<Pair<String, PropertyDefinition>>();
		valid &= checkForMissingValues(model, errors);

		if (!valid) {
			printErrorMessages(model, errors);
		}
		return valid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(Identity model) {
		if (model instanceof PropertyDefinition) {
			PropertyDefinition propertyDefinition = (PropertyDefinition) model;
			return StringUtils.isNotNullOrEmpty(propertyDefinition.getType())
					&& propertyDefinition.getDataType() != null;
		}
		return true;
	}

}
