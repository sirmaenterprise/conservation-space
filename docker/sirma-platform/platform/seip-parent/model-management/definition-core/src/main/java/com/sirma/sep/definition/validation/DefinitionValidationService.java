package com.sirma.sep.definition.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.validator.DefinitionValidator;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.sep.definition.DefinitionImportMessageBuilder;

/**
 * Validates definitions.
 *
 * @author Adrian Mitev
 */
public class DefinitionValidationService {

	@Inject
	@Any
	private Instance<DefinitionValidator> validators;

	/**
	 * Performs formal validation of definitions.
	 * Should be used before compilation.
	 *
	 * @param definitions to validate.
	 * @return list with errors during validation
	 */
	@SuppressWarnings("static-method")
	public List<ValidationMessage> validateDefinitions(List<? extends GenericDefinition> definitions) {
		DefinitionImportMessageBuilder validationBuilder = new DefinitionImportMessageBuilder();

		checkForDuplicateDefinitions(definitions, validationBuilder);

		checkForHierarchyCycles(definitions, validationBuilder);

		checkForMissingParent(definitions, validationBuilder);

		checkForDuplicatedFields(definitions, validationBuilder);

		return validationBuilder.getMessages();
	}

	private void checkForDuplicateDefinitions(List<? extends GenericDefinition> definitions,
			DefinitionImportMessageBuilder validationBuilder) {
		Set<String> visited = new HashSet<>();

		Set<String> duplicatedIds = new HashSet<>();

		for (GenericDefinition definition : definitions) {
			String id = definition.getIdentifier();

			if (visited.contains(id)) {
				duplicatedIds.add(id);
				validationBuilder.duplicatedDefinitions(id);
			}

			visited.add(id);
		}

		definitions.removeIf(definition -> duplicatedIds.contains(definition.getIdentifier()));
	}

	private void checkForMissingParent(List<? extends GenericDefinition> definitions,
			DefinitionImportMessageBuilder validationBuilder) {
		Map<String, GenericDefinition> index = toMap(definitions);

		Set<String> definitionsWithMissingParent = new HashSet<>();

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
						validationBuilder.missingParent(definition.getIdentifier(), definition.getParentDefinitionId());
					}
				});

		definitions.removeIf(definition -> definitionsWithMissingParent.contains(definition.getIdentifier()));
	}

	private void checkForHierarchyCycles(List<? extends GenericDefinition> definitions,
			DefinitionImportMessageBuilder validationBuilder) {
		Set<String> definitionsFormingCycle = new HashSet<>();

		Map<String, GenericDefinition> index = toMap(definitions);

		for (String id : index.keySet()) {
			List<String> visited = new ArrayList<>();

			String current = id;

			while (current != null) {
				if (visited.contains(current)) {
					visited.add(current);
					definitionsFormingCycle.add(id);
					validationBuilder.hierarchyCycle(id, visited);
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
	}

	private void checkForDuplicatedFields(List<? extends GenericDefinition> definitions,
			DefinitionImportMessageBuilder validationBuilder) {
		definitions.forEach(definition -> {
			// Collect all field names (root fields + from regions)
			List<String> fieldNames = definition.fieldsStream().map(PropertyDefinition::getName).collect(Collectors.toList());
			fieldNames.stream()
					.filter(fieldName -> Collections.frequency(fieldNames, fieldName) > 1)
					.collect(Collectors.toSet())
					.forEach(duplicatedField -> validationBuilder.duplicatedFields(definition.getIdentifier(), duplicatedField));
		});
	}

	private static Map<String, GenericDefinition> toMap(List<? extends GenericDefinition> definitions) {
		return definitions.stream().collect(CollectionUtils.toIdentityMap(GenericDefinition::getIdentifier));
	}

	/**
	 * Validates definitions after they're compiled and merged with their parents by invoking
	 * the {@link DefinitionValidator}s on them.
	 *
	 * @param definitions definitions to validate.
	 * @return list with error messages.
	 */
	public List<ValidationMessage> validateCompiledDefinitions(List<? extends GenericDefinition> definitions) {
		List<ValidationMessage> validationMessages = new LinkedList<>();
		for (GenericDefinition definition : definitions) {
			validators.forEach(validator -> validationMessages.addAll(validator.validate(definition)));
		}
		return validationMessages;
	}

}
