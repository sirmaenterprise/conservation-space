package com.sirma.itt.seip.definition.validator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.compile.DefinitionValidator;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;

/**
 * Validator class that check for different fields bound to same uri.
 *
 * @author tdossev
 */
public class DuplicateUriValidator implements DefinitionValidator {

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateUriValidator.class);

	@Override
	public boolean validate(RegionDefinitionModel model) {
		if (model == null) {
			return true;
		}

		boolean valid = true;

		Map<String, Set<String>> map = model.fieldsStream().filter(PropertyDefinition.hasUri()).collect(
				Collectors.groupingBy(PropertyDefinition::getUri,
						Collectors.mapping(PropertyDefinition::getName, Collectors.toSet())));

		for (Entry<String, Set<String>> entry : map.entrySet()) {
			if (entry.getValue().size() > 1) {
				printErrorMessages(model, entry);
				valid = false;
			}
		}
		return valid;
	}


	private static void printErrorMessages(RegionDefinitionModel model, Entry<String, Set<String>> entry) {
		StringBuilder builder = new StringBuilder();
		builder
		.append("\n=======================================================================\nFound errors in definition: ")
		.append(model.getIdentifier()).append("\n")
		.append(" (duplicate use of uri) : ").append(entry.getKey()).append("\n")
		.append(" (in fields) : ").append(entry.getValue()).append("\n")
		.append("=======================================================================");
		LOGGER.error(builder.toString());
		ValidationLoggingUtil.addErrorMessage(builder.toString());

	}

	@Override
	public boolean validate(DefinitionModel model) {
		return true;
	}

	@Override
	public boolean validate(Identity model) {
		return true;
	}

}
