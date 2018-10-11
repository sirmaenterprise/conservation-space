package com.sirma.itt.seip.definition.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Validator class that check for different fields bound to same uri.
 *
 * @author tdossev
 */
public class DuplicateUriValidator implements DefinitionValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateUriValidator.class);

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		if (model == null) {
			return Collections.emptyList();
		}

		List<String> messages = new ArrayList<>();

		Map<String, Set<String>> map = model.fieldsStream().filter(PropertyDefinition.hasUri()).collect(
				Collectors.groupingBy(PropertyDefinition.resolveUri(),
						Collectors.mapping(PropertyDefinition::getName, Collectors.toSet())));

		for (Entry<String, Set<String>> entry : map.entrySet()) {
			if (entry.getValue().size() > 1) {
				printErrorMessages(model, entry, messages);
			}
		}

		return messages;
	}

	private static void printErrorMessages(RegionDefinitionModel model, Entry<String, Set<String>> entry, List<String> messages) {
		StringBuilder builder = new StringBuilder();
		builder
		.append("\n=======================================================================\nFound errors in definition: ")
		.append(model.getIdentifier()).append("\n")
		.append(" (duplicate use of uri) : ").append(entry.getKey()).append("\n")
		.append(" (in fields) : ").append(entry.getValue()).append("\n")
		.append("=======================================================================");

		String message = builder.toString();

		LOGGER.error(message);
		messages.add(message);
		ValidationLoggingUtil.addErrorMessage(message);
	}

}
