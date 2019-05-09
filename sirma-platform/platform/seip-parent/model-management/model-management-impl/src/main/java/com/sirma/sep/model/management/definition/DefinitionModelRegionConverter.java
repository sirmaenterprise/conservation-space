package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addLabels;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addStringAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelRegion;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Utility for converting {@link RegionDefinition} to {@link ModelRegion}.
 *
 * @author Mihail Radkov
 */
public class DefinitionModelRegionConverter {

	private final LabelProvider labelProvider;

	/**
	 * Instantiates the converter with the provided label service.
	 *
	 * @param labelProvider provider for retrieving the labels of a region
	 */
	@Inject
	public DefinitionModelRegionConverter(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Converts all available {@link RegionDefinition} in the provided definition.
	 *
	 * @param definition definition containing regions for conversion
	 * @param modelsMetaInfo mapping used for converting {@link com.sirma.sep.model.management.ModelAttribute}
	 * @return mapping of converted regions
	 */
	public Map<String, ModelRegion> constructModelRegions(GenericDefinition definition, ModelsMetaInfo modelsMetaInfo) {
		return definition.getRegions()
				.stream()
				.map(regionDefinition -> constructModelRegion(regionDefinition, modelsMetaInfo))
				.collect(CollectionUtils.toIdentityMap(ModelRegion::getId, LinkedHashMap::new));
	}

	private ModelRegion constructModelRegion(RegionDefinition regionDefinition, ModelsMetaInfo modelsMetaInfo) {
		ModelRegion modelRegion = new ModelRegion();

		modelRegion.setModelsMetaInfo(modelsMetaInfo);
		modelRegion.setId(regionDefinition.getIdentifier());

		addAttribute(modelRegion, DefinitionModelAttributes.IDENTIFIER, regionDefinition.getIdentifier());
		addAttribute(modelRegion, DefinitionModelAttributes.ORDER, regionDefinition.getOrder());
		addStringAttribute(modelRegion, DefinitionModelAttributes.DISPLAY_TYPE, regionDefinition.getDisplayType());

		addAttribute(modelRegion, DefinitionModelAttributes.LABEL_ID, regionDefinition.getLabelId());
		addLabels(regionDefinition, modelRegion, labelProvider::getLabels);

		modelRegion.setAsDeployed();
		return modelRegion;
	}
}
