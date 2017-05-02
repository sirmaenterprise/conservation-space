package com.sirma.itt.seip.rest.handlers.writers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Properties filter that selects the provided fields and their dependencies based on the provided definition model upon
 * filter building.
 *
 * @author BBonev
 */
public class SelectedPropertiesFilter implements PropertiesFilterBuilder {

	private final Set<String> fields;

	/**
	 * Instantiates a new selected properties filter.
	 *
	 * @param fields
	 *            the requested fields to filter
	 */
	public SelectedPropertiesFilter(Collection<String> fields) {
		this.fields = new HashSet<>(fields);
	}

	@Override
	public Predicate<String> buildFilter(DefinitionModel model) {
		Set<String> dependencies = model
				.getFieldsAndDependencies(fields)
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());
		return dependencies::contains;
	}

}
