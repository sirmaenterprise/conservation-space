package com.sirma.sep.model.management.definition.export;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.copyAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.toDisplayType;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.toInteger;

import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.sep.model.management.ModelRegion;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;

/**
 * Copies attributes from {@link ModelRegion} into {@link RegionDefinition}.
 *
 * @author Mihail Radkov
 */
class RegionDefinitionConverter {

	/**
	 * Copies the attributes of the provided {@link ModelRegion} into a new {@link RegionDefinition}
	 *
	 * @param modelRegion the region which attributes will be copied into the new one
	 * @return the newly created region
	 */
	RegionDefinition newRegion(ModelRegion modelRegion) {
		RegionDefinitionImpl regionDefinition = new RegionDefinitionImpl();
		copyAttributes(modelRegion, regionDefinition);
		return regionDefinition;
	}

	/**
	 * Copies the attributes of the provided {@link ModelRegion} into the given {@link RegionDefinition}
	 *
	 * @param modelRegion the region which attributes will be copied into the region definition
	 * @param regionDefinition the region definition into which attributes will be copied
	 */
	void copyRegion(ModelRegion modelRegion, RegionDefinitionImpl regionDefinition) {
		copyAttributes(modelRegion, regionDefinition);
	}

	private static void copyAttributes(ModelRegion modelRegion, RegionDefinitionImpl regionDefinition) {
		regionDefinition.setIdentifier(modelRegion.getId());
		copyAttribute(modelRegion, DefinitionModelAttributes.ORDER, toInteger(), regionDefinition::setOrder);
		copyAttribute(modelRegion, DefinitionModelAttributes.DISPLAY_TYPE, toDisplayType(), regionDefinition::setDisplayType);
		copyAttribute(modelRegion, DefinitionModelAttributes.LABEL_ID, regionDefinition::setLabelId);
	}

}
