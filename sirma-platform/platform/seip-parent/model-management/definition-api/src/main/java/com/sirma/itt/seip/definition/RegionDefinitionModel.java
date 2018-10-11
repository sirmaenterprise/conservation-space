package com.sirma.itt.seip.definition;

import java.util.List;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Extension of the {@link DefinitionModel} that adds a regions.
 *
 * @author BBonev
 */
public interface RegionDefinitionModel extends DefinitionModel {

	/**
	 * Gets the regions.
	 *
	 * @return the regions
	 */
	List<RegionDefinition> getRegions();

	@Override
	default Stream<PropertyDefinition> fieldsStream() {
		// append field definitions of all regions to the fields of the top level
		return Stream.concat(DefinitionModel.super.fieldsStream(),
				getRegions().stream().flatMap(DefinitionModel::fieldsStream));
	}

	@Override
	default String getType() {
		return null;
	}

}
