package com.sirma.sep.definition.validation;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.validator.DefinitionValidator;
import com.sirma.itt.seip.domain.definition.GenericDefinition;

/**
 * Validates definitions.
 *
 * @author Adrian Mitev
 */
public class DefinitionValidationService {

	@Inject
	@Any
	private Instance<DefinitionValidator> validators;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Performs formal validation of definitions.
	 * Should be used before compilation.
	 *
	 * @param definitions to validate.
	 * @return list with errors during validation
	 */
	@SuppressWarnings("static-method")
	public List<String> validateDefinitions(List<? extends GenericDefinition> definitions) {
		List<String> errors = new ArrayList<>();

		List<String> duplicateDefinitionErrors = checkForDuplicateDefinitions(definitions);
		errors.addAll(duplicateDefinitionErrors);

		List<String> definitionsCycleErrors = checkForHierarchyCycles(definitions);
		errors.addAll(definitionsCycleErrors);

		List<String> missingParentErrors = checkForMissingParent(definitions);
		errors.addAll(missingParentErrors);

		return errors;
	}

	private static List<String> checkForDuplicateDefinitions(List<? extends GenericDefinition> definitions) {
		List<String> errors = new ArrayList<>();

		Set<String> visited = new HashSet<>();

		Set<String> duplicatedIds = new HashSet<>();

		for (GenericDefinition definition : definitions) {
			String id = definition.getIdentifier();

			if (visited.contains(id)) {
				LOGGER.debug("Duplicate definition id found {}", id);
				errors.add("Multiple definitions with id '" + id + "' are found");

				duplicatedIds.add(id);
			}

			visited.add(id);
		}

		definitions.removeIf(definition -> duplicatedIds.contains(definition.getIdentifier()));

		return errors;
	}

	private static List<String> checkForMissingParent(List<? extends GenericDefinition> definitions) {
		Map<String, GenericDefinition> index = toMap(definitions);

		Set<String> definitionsWithMissingParent = new HashSet<>();

		List<String> errors = new ArrayList<>();

		definitions.stream()
					.filter(definition -> definition.getParentDefinitionId() != null)
					.forEach(definition -> {
						boolean missing = false;

						if (!index.containsKey(definition.getParentDefinitionId())) {
							missing = true;
						} else {
							GenericDefinition current = definition;

							// traverse the hierarchy upwards to find if some of the ancestors is missing
							while (current.getParentDefinitionId() != null) {
								if (!index.containsKey(current.getParentDefinitionId())) {
									missing = true;
									break;
								}

								current = index.get(current.getParentDefinitionId());
							}
						}

						if (missing) {
							definitionsWithMissingParent.add(definition.getIdentifier());

							String error = "The parent definition '" + definition.getParentDefinitionId() + "'"
									+ " of definition '" + definition.getIdentifier() + "' is missing or has validation errors";
							LOGGER.debug(error);

							errors.add(error);
						}
					});

		definitions.removeIf(definition -> definitionsWithMissingParent.contains(definition.getIdentifier()));

		return errors;
	}

	private static List<String> checkForHierarchyCycles(List<? extends GenericDefinition> definitions) {
		List<String> errors = new ArrayList<>();

		Set<String> definitionsFormingCycle = new HashSet<>();

		Map<String, GenericDefinition> index = toMap(definitions);

		for (String id : index.keySet()) {
			List<String> visited = new ArrayList<>();

			String current = id;

			while (current != null) {
				if (visited.contains(current)) {
					visited.add(current);

					String error = "Definition '" + id + "' contains hierarchy cycle " + StringUtils.join(visited, " -> ");
					errors.add(error);
					LOGGER.debug(error);

					definitionsFormingCycle.add(id);

					break;
				}

				visited.add(current);
				GenericDefinition currentDefinition = index.get(current);
				if (currentDefinition != null) {
					current = currentDefinition.getParentDefinitionId();
				} else {
					current = null;
				}
			}
		}

		definitions.removeIf(definition -> definitionsFormingCycle.contains(definition.getIdentifier()));

		return errors;
	}

	private static Map<String, GenericDefinition> toMap(List<? extends GenericDefinition> definitions) {
		return definitions.stream()
							.collect(CollectionUtils.toIdentityMap(GenericDefinition::getIdentifier));
	}

	/**
	 * Validates definitions after they're compiled and merged with their parents by invoking
	 * the {@link DefinitionValidator}s on them.
	 *
	 * @param definitions definitions to validate.
	 * @return list with error messages.
	 */
	public List<String> validateCompiledDefinitions(List<? extends GenericDefinition> definitions) {
		List<String> allErrors = new ArrayList<>();

		for (GenericDefinition definition : definitions) {
			List<String> definitionErrors = new ArrayList<>();

			validators.forEach(validator -> definitionErrors.addAll(validator.validate(definition)));

			List<String> errors = definitionErrors.stream()
							.map(message -> "Error found in definition '" + definition.getIdentifier() + "': " + message)
							.collect(Collectors.toList());

			allErrors.addAll(errors);
		}

		return allErrors;
	}

}
