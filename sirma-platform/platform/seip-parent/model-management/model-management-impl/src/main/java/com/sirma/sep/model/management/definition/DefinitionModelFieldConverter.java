package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addLabels;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addStringAttribute;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Converts {@link PropertyDefinition} to {@link ModelField}.
 *
 * @author Mihail Radkov
 */
public class DefinitionModelFieldConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final NamespaceRegistryService namespaceRegistryService;
	private final LabelProvider labelProvider;

	private final DefinitionModelControlConverter definitionModelControlConverter;

	/**
	 * Instantiates the converter with the provided namespace service for URIs conversions.
	 *
	 * @param namespaceRegistryService the service for URIs conversions
	 * @param definitionModelControlConverter converter for model controls
	 * @param labelProvider provider for retrieving the labels of a field
	 */
	@Inject
	public DefinitionModelFieldConverter(NamespaceRegistryService namespaceRegistryService, LabelProvider labelProvider,
			DefinitionModelControlConverter definitionModelControlConverter) {
		this.namespaceRegistryService = namespaceRegistryService;
		this.labelProvider = labelProvider;
		this.definitionModelControlConverter = definitionModelControlConverter;
	}

	/**
	 * Converts the {@link PropertyDefinition} from the provided {@link GenericDefinition} into {@link ModelField}.
	 * <p>
	 * This will also convert fields from any {@link com.sirma.itt.seip.definition.RegionDefinition} in the provided
	 * {@link GenericDefinition}
	 *
	 * @param definition the definition which will be processed
	 * @param modelsMetaInfo models meta information mappings
	 * @return map of converted {@link ModelField}. It may be empty if the provided definition has no fields and/or
	 * regions
	 */
	public Map<String, ModelField> constructModelFields(GenericDefinition definition, ModelsMetaInfo modelsMetaInfo) {

		Map<PropertyDefinition, RegionDefinition> propertyToRegionMapping = new HashMap<>();
		definition.getRegions().forEach(region -> region.getFields().forEach(field -> propertyToRegionMapping.put(field, region)));

		return definition.fieldsStream()
				.map(propertyDefinition -> constructModelField(propertyDefinition, propertyToRegionMapping, modelsMetaInfo))
				.collect(Collectors.toMap(ModelField::getId, f -> f, duplicateFieldMerger(definition), LinkedHashMap::new));
	}

	private ModelField constructModelField(PropertyDefinition propertyDefinition,
			Map<PropertyDefinition, RegionDefinition> propertyToRegionMapping, ModelsMetaInfo modelsMetaInfo) {
		ModelField modelField = new ModelField();
		modelField.setModelsMetaInfo(modelsMetaInfo);
		modelField.setId(propertyDefinition.getName());
		if (propertyDefinition.getUri() != null) {
			modelField.setUri(getFullUri(propertyDefinition));
		}
		modelField.setValue(propertyDefinition.getDefaultValue());

		if (propertyToRegionMapping.containsKey(propertyDefinition)) {
			RegionDefinition region = propertyToRegionMapping.get(propertyDefinition);
			modelField.setRegionId(region.getIdentifier());
		}

		addLabelAttributes(propertyDefinition, modelField);
		addTooltipAttribute(propertyDefinition, modelField);
		addAttribute(modelField, DefinitionModelAttributes.TYPE, propertyDefinition.getType());
		addAttribute(modelField, DefinitionModelAttributes.RNC, propertyDefinition.getRnc());
		addAttribute(modelField, DefinitionModelAttributes.ORDER, propertyDefinition.getOrder());
		addAttribute(modelField, DefinitionModelAttributes.NAME, propertyDefinition.getIdentifier());
		addAttribute(modelField, DefinitionModelAttributes.CODE_LIST, propertyDefinition.getCodelist());
		addAttribute(modelField, DefinitionModelAttributes.PREVIEW_EMPTY, propertyDefinition.getPreviewEmpty());
		addAttribute(modelField, DefinitionModelAttributes.MULTI_VALUED, propertyDefinition.getMultiValued());
		addAttribute(modelField, DefinitionModelAttributes.MANDATORY, propertyDefinition.getMandatory());
		addStringAttribute(modelField, DefinitionModelAttributes.DISPLAY_TYPE, propertyDefinition.getDisplayType());

		modelField.setControls(definitionModelControlConverter.constructModelControls(propertyDefinition, modelsMetaInfo));

		modelField.setAsDeployed();
		return modelField;
	}

	private String getFullUri(PropertyDefinition propertyDefinition) {
		return namespaceRegistryService.buildFullUri(propertyDefinition.getUri());
	}

	private void addLabelAttributes(PropertyDefinition propertyDefinition, ModelField modelField) {
		addLabels(propertyDefinition, modelField, labelProvider::getLabels);
		addAttribute(modelField, DefinitionModelAttributes.LABEL_ID, propertyDefinition.getLabelId());
	}

	private void addTooltipAttribute(PropertyDefinition propertyDefinition, ModelField modelField) {
		String tooltipId = propertyDefinition.getTooltipId();
		if (StringUtils.isNotBlank(tooltipId)) {
			addAttribute(modelField, DefinitionModelAttributes.TOOLTIP_ID, tooltipId);
			Map<String, String> labels = labelProvider.getLabels(tooltipId);
			if (CollectionUtils.isNotEmpty(labels)) {
				addAttribute(modelField, DefinitionModelAttributes.TOOLTIP, labels);
			}
		}
	}

	private static BinaryOperator<ModelField> duplicateFieldMerger(GenericDefinition definition) {
		return (source, target) -> {
			LOGGER.warn("Found duplicated field {} in {}", source.getId(), definition.getIdentifier());
			return target;
		};
	}
}
