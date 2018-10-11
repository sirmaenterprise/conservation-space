package com.sirma.itt.seip.definition.validator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Checks if filter for definitions is added to field which can't be filtered.
 * For example field with uri "emf:type" can't be filtering it is used by system.
 *
 * @author Boyan Tonchev.
 */
public class FilterDefinitionsValidator implements DefinitionValidator {

	private static final Set<String> nonFilterableUris = new HashSet<>(2);

	static {
		nonFilterableUris.add("emf:type");
		nonFilterableUris.add("emf:status");
	}

	private static final String FILTERING_NOT_SUPPORTED = "Field with property name: \"%1s\" and uri: \"%1s\" can't be filtering!";

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		return this.validate((DefinitionModel) model);
	}

	@Override
	public List<String> validate(DefinitionModel model) {
		if (model instanceof GenericDefinition) {
			return model.fieldsStream()
					.filter(PropertyDefinition.hasUri())
					.filter(propertyDefinition -> nonFilterableUris.contains(PropertyDefinition.resolveUri().apply(propertyDefinition)))
					.filter(FilterDefinitionsValidator::hasDefinitionsFilter)
					.map(propertyDefinition -> String.format(FILTERING_NOT_SUPPORTED, propertyDefinition.getName(),
															 PropertyDefinition.resolveUri().apply(propertyDefinition)))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private static boolean hasDefinitionsFilter(PropertyDefinition propertyDefinition) {
		Set<String> filters = propertyDefinition.getFilters();
		return filters != null && !filters.isEmpty();
	}
}
