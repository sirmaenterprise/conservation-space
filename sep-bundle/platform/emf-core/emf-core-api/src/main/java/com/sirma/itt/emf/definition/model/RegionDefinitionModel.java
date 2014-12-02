package com.sirma.itt.emf.definition.model;

import java.util.List;

import com.sirma.itt.emf.domain.model.DefinitionModel;

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
	public List<RegionDefinition> getRegions();

}
