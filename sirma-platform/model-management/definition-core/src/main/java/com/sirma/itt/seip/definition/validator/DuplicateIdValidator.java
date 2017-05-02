package com.sirma.itt.seip.definition.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sirma.itt.seip.definition.compile.DefinitionValidator;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.Controllable;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;

/**
 * Validator class that checks for duplicate ID
 *
 * @author BBonev
 */
public class DuplicateIdValidator implements DefinitionValidator {

	private static final Logger LOGGER = Logger.getLogger(DuplicateIdValidator.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(RegionDefinitionModel model) {
		if (model == null) {
			return true;
		}
		Set<Identity> set = new HashSet<Identity>();
		int originalSize = 0;
		if (model.getFields() != null && !model.getFields().isEmpty()) {
			set.addAll(model.getFields());
			originalSize += model.getFields().size();
			if (!checkConditions(model.getFields())) {
				String message = "Found duplicate field IDs in region model " + model.getIdentifier();
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
				return false;
			}
		}
		return validateRegions(model, set, originalSize);
	}

	/**
	 * Validate regions.
	 *
	 * @param model
	 *            the model
	 * @param set
	 *            the set
	 * @param originalSize
	 *            the original size
	 * @return true, if successful
	 */
	private boolean validateRegions(RegionDefinitionModel model, Set<Identity> set, int originalSize) {
		if (model.getRegions() != null && !model.getRegions().isEmpty()) {
			set.addAll(model.getRegions());
			originalSize += model.getRegions().size();
			if (!checkConditions(model.getRegions())) {
				String message = "Found duplicate field IDs in regions controls " + model.getIdentifier();
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
				return false;
			}
			for (RegionDefinition definition : model.getRegions()) {
				set.addAll(definition.getFields());
				originalSize += definition.getFields().size();
				if (!checkConditions(definition.getFields())) {
					String message = "Found duplicate field IDs in region " + definition.getIdentifier();
					LOGGER.error(message);
					ValidationLoggingUtil.addErrorMessage(message);
					return false;
				}
			}
		}
		return set.size() == originalSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(DefinitionModel model) {
		if (model == null) {
			return true;
		}
		Set<Identity> set = new HashSet<Identity>();
		int originalSize = 0;
		if (model.getFields() != null && !model.getFields().isEmpty()) {
			set.addAll(model.getFields());
			originalSize += model.getFields().size();
			if (!checkConditions(model.getFields())) {
				return false;
			}
		}
		return set.size() == originalSize;
	}

	/**
	 * Check conditions of the elements with controls.
	 *
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @return the int
	 */
	private <E extends Controllable> boolean checkConditions(List<E> list) {
		if (list == null || list.isEmpty()) {
			return true;
		}
		for (E e : list) {
			ControlDefinition controlDefinition = e.getControlDefinition();
			if (controlDefinition != null && controlDefinition.getFields() != null
					&& !controlDefinition.getFields().isEmpty()) {
				boolean validate = validate(controlDefinition);
				if (!validate) {
					String message = "Found duplicate field IDs in control " + controlDefinition.getIdentifier();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validate(Identity model) {
		return true;
	}

}
