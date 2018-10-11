package com.sirma.itt.seip.definition.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.Controllable;
import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Validator class that checks for duplicate ID
 *
 * @author BBonev
 */
public class DuplicateControlFieldIdValidator implements DefinitionValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateControlFieldIdValidator.class);

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		if (model == null) {
			return Collections.emptyList();
		}

		List<String> messages = new ArrayList<>();

		Set<Identity> set = new HashSet<>();
		int originalSize = 0;
		if (model.getFields() != null && !model.getFields().isEmpty()) {
			set.addAll(model.getFields());
			originalSize += model.getFields().size();
			if (!checkConditions(model.getFields(), messages)) {
				String message = "Found duplicate field IDs in region model " + model.getIdentifier();
				messages.add(message);
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
				return messages;
			}
		}

		validateRegions(model, set, originalSize, messages);

		return messages;
	}

	private boolean validateRegions(RegionDefinitionModel model, Set<Identity> set, int originalSize, List<String> messages) {
		if (model.getRegions() != null && !model.getRegions().isEmpty()) {
			set.addAll(model.getRegions());
			originalSize += model.getRegions().size();
			if (!checkConditions(model.getRegions(), messages)) {
				String message = "Found duplicate field IDs in regions controls " + model.getIdentifier();
				messages.add(message);
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
				return false;
			}
			for (RegionDefinition definition : model.getRegions()) {
				set.addAll(definition.getFields());
				originalSize += definition.getFields().size();
				if (!checkConditions(definition.getFields(), messages)) {
					String message = "Found duplicate field IDs in region " + definition.getIdentifier();
					messages.add(message);
					LOGGER.error(message);
					ValidationLoggingUtil.addErrorMessage(message);
					return false;
				}
			}
		}
		return set.size() == originalSize;
	}

	private boolean validateDefinitionModel(DefinitionModel model, List<String> messages) {
		if (model == null) {
			return true;
		}
		Set<Identity> set = new HashSet<>();
		int originalSize = 0;
		if (model.getFields() != null && !model.getFields().isEmpty()) {
			set.addAll(model.getFields());
			originalSize += model.getFields().size();
			if (!checkConditions(model.getFields(), messages)) {
				return false;
			}
		}
		return set.size() == originalSize;
	}

	private <E extends Controllable> boolean checkConditions(List<E> list, List<String> messages) {
		if (list == null || list.isEmpty()) {
			return true;
		}
		for (E e : list) {
			ControlDefinition controlDefinition = e.getControlDefinition();
			if (controlDefinition != null && controlDefinition.getFields() != null
					&& !controlDefinition.getFields().isEmpty()) {
				boolean validate = validateDefinitionModel(controlDefinition, messages);
				if (!validate) {
					String message = "Found duplicate field IDs in control " + controlDefinition.getIdentifier();
					messages.add(message);
					ValidationLoggingUtil.addErrorMessage(message);
					LOGGER.warn(message);
					return false;
				}
				// for now more deep checks are not needed
				// BB: NOTE: if the line bellow is enabled and there is a definition if has a default
				// value set for several check boxes then the validation will fail due to same names
				// of that fields
				// size = checkConditions(controlDefinition.getFields(), size, set)
			}
		}
		return true;
	}

}