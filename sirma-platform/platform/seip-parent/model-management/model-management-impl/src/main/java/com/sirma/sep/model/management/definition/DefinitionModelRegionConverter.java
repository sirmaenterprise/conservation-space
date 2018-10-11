package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addLabels;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addStringAttribute;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelRegion;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * Utility for converting {@link RegionDefinition} to {@link ModelRegion}.
 *
 * @author Mihail Radkov
 */
public class DefinitionModelRegionConverter {

	private final LabelService labelService;

	/**
	 * Instantiates the converter with the provided label service.
	 *
	 * @param labelService label service used for retrieving {@link LabelDefinition} of a region
	 */
	@Inject
	public DefinitionModelRegionConverter(LabelService labelService) {
		this.labelService = labelService;
	}

	/**
	 * Converts all available {@link RegionDefinition} in the provided definition.
	 *
	 * @param definition definition containing regions for conversion
	 * @param regionsMetaInfo mapping used for converting {@link com.sirma.sep.model.management.ModelAttribute}
	 * @return list of converted regions
	 */
	public List<ModelRegion> constructModelRegions(GenericDefinition definition, Map<String, ModelMetaInfo> regionsMetaInfo) {
		return definition.getRegions()
				.stream()
				.map(regionDefinition -> constructModelRegion(regionDefinition, regionsMetaInfo))
				.collect(Collectors.toList());
	}

	private ModelRegion constructModelRegion(RegionDefinition regionDefinition, Map<String, ModelMetaInfo> regionsMetaInfo) {
		ModelRegion modelRegion = new ModelRegion();

		modelRegion.setId(regionDefinition.getIdentifier());
		addAttribute(regionsMetaInfo, modelRegion, DefinitionModelAttributes.ORDER, regionDefinition.getOrder());
		addStringAttribute(regionsMetaInfo, modelRegion, DefinitionModelAttributes.DISPLAY_TYPE, regionDefinition.getDisplayType());
		addLabels(regionDefinition, modelRegion, labelService::getLabel);

		return modelRegion;
	}
}
