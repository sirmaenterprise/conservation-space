package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addLabels;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.sep.model.management.ModelHeader;
import com.sirma.sep.model.management.ModelHeaderType;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

import javax.inject.Inject;

import java.util.Map;
import java.util.Objects;

/**
 * Header definitions are regular properties with specific meaning and processing. This converter converts the header
 * properties to a map where header property identifiers are mapped to {@link ModelHeader}.
 *
 * @author svelikov
 * @author Mihai Radkov
 */
public class DefinitionModelHeaderConverter {

	private final LabelProvider labelProvider;

	/**
	 * Constructs the header converter with the given {@link LabelProvider}
	 *
	 * @param labelProvider used for retrieving labels
	 */
	@Inject
	public DefinitionModelHeaderConverter(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Constructs {@link ModelHeader} from the provided {@link GenericDefinition} header {@link PropertyDefinition} (if any are present).
	 *
	 * @param definition generic definition which contains the header definition properties
	 * @param modelsMetaInfo model meta info needed for the construction of each {@link ModelHeader}
	 * @return mapping of the constructed headers
	 */
	public Map<String, ModelHeader> constructModelHeaders(GenericDefinition definition, ModelsMetaInfo modelsMetaInfo) {
		return ModelHeaderType.HEADERS.stream()
				.map(definition.getFieldsAsMap()::get)
				.filter(Objects::nonNull)
				.map(propertyDefinition -> constructModelHeader(propertyDefinition, modelsMetaInfo))
				.collect(CollectionUtils.toIdentityMap(ModelHeader::getId));
	}

	private ModelHeader constructModelHeader(PropertyDefinition headerProperty, ModelsMetaInfo modelsMetaInfo) {
		ModelHeader modelHeader = new ModelHeader();
		modelHeader.setId(headerProperty.getName());
		modelHeader.setModelsMetaInfo(modelsMetaInfo);

		addAttribute(modelHeader, DefinitionModelAttributes.LABEL_ID, headerProperty.getLabelId());
		addLabels(headerProperty, modelHeader, labelProvider::getLabels);
		addAttribute(modelHeader, DefinitionModelAttributes.HEADER_TYPE, headerProperty.getName());

		modelHeader.setAsDeployed();
		return modelHeader;
	}

}
