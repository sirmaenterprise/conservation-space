package com.sirma.itt.seip.domain.definition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.sirma.itt.seip.definition.AllowedChildrenModel;
import com.sirma.itt.seip.definition.ReferenceDefinitionModel;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.StateTransitionalModel;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.Purposable;

/**
 * Defines a generic interface for top level definition that can work with multiple actual definition types. The type
 * identifier is provided via the method {@link #getType()}.
 *
 * WARNING: It is mandatory for this class to be in this particular package, because it path is coupled to entries
 * in the database.
 *
 * @author BBonev
 */
public interface GenericDefinition extends TopLevelDefinition, StateTransitionalModel, AllowedChildrenModel,
		PathElement, RegionDefinitionModel, Condition, Purposable, ReferenceDefinitionModel {

	/**
	 * Gets target object type.
	 *
	 * @return the type
	 */
	@Override
	String getType();

	/**
	 * Provides list of configuration fields for the current definition.
	 *
	 * @return list with configuration fields
	 */
	List<PropertyDefinition> getConfigurations();

	/**
	 * Provides a configuration by a given name.
	 *
	 * @param name configuration field name.
	 * @return configuration if exists, empty optional otherwise.
	 */
	default Optional<PropertyDefinition> getConfiguration(String name) {
		Objects.requireNonNull(name, "Configuration name must be provided");

		return getConfigurations().stream()
								.filter(property -> name.equals(property.getIdentifier()))
								.findFirst();
	}

}
