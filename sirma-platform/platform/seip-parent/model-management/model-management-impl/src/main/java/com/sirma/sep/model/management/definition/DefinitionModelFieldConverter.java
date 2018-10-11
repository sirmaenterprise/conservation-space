package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addLabels;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addStringAttribute;

import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Converts {@link PropertyDefinition} to {@link ModelField}.
 *
 * @author Mihail Radkov
 */
public class DefinitionModelFieldConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final NamespaceRegistryService namespaceRegistryService;
	private final LabelService labelService;

	/**
	 * Instantiates the converter with the provided namespace service for URIs conversions.
	 *
	 * @param namespaceRegistryService the service for URIs conversions
	 * @param labelService label service used for retrieving {@link LabelDefinition} of a field
	 */
	@Inject
	public DefinitionModelFieldConverter(NamespaceRegistryService namespaceRegistryService, LabelService labelService) {
		this.namespaceRegistryService = namespaceRegistryService;
		this.labelService = labelService;
	}

	/**
	 * Converts the {@link PropertyDefinition} from the provided {@link GenericDefinition} into {@link ModelField}.
	 * <p>
	 * This will also convert fields from any {@link com.sirma.itt.seip.definition.RegionDefinition} in the provided {@link
	 * GenericDefinition}
	 *
	 * @param definition the definition which will be processed
	 * @param fieldsMetaInfo meta information mapping about the model fields
	 * @return list of converted {@link ModelField}. It may be empty if the provided definition has no fields and/or regions
	 */
	public List<ModelField> constructModelFields(GenericDefinition definition,
			Map<String, ModelMetaInfo> fieldsMetaInfo) {

		Map<PropertyDefinition, RegionDefinition> propertyToRegionMapping = new HashMap<>();
		definition.getRegions().forEach(region -> region.getFields().forEach(field -> propertyToRegionMapping.put(field, region)));

		return new ArrayList<>(definition.fieldsStream()
				.map(propertyDefinition -> constructModelField(propertyDefinition, propertyToRegionMapping, fieldsMetaInfo))
				.collect(Collectors.toMap(ModelField::getId, f -> f, duplicateFieldMerger(definition)))
				.values());
	}

	private ModelField constructModelField(PropertyDefinition propertyDefinition,
			Map<PropertyDefinition, RegionDefinition> propertyToRegionMapping, Map<String, ModelMetaInfo> fieldsMetaInfo) {
		ModelField modelField = new ModelField();

		modelField.setId(propertyDefinition.getName());
		if (propertyDefinition.getUri() != null) {
			modelField.setUri(getFullUri(propertyDefinition));
		}
		modelField.setValue(propertyDefinition.getDefaultValue());

		if (propertyToRegionMapping.containsKey(propertyDefinition)) {
			RegionDefinition region = propertyToRegionMapping.get(propertyDefinition);
			modelField.setRegionId(region.getIdentifier());
		}

		addLabels(propertyDefinition, modelField, labelService::getLabel);
		addTooltipAttribute(propertyDefinition, fieldsMetaInfo, modelField);

		addAttribute(fieldsMetaInfo, modelField, DefinitionModelAttributes.TYPE, propertyDefinition.getType());
		addAttribute(fieldsMetaInfo, modelField, DefinitionModelAttributes.ORDER, propertyDefinition.getOrder());
		addAttribute(fieldsMetaInfo, modelField, DefinitionModelAttributes.CODE_LIST, propertyDefinition.getCodelist());
		addAttribute(fieldsMetaInfo, modelField, DefinitionModelAttributes.PREVIEW_EMPTY, propertyDefinition.isPreviewEnabled());
		addAttribute(fieldsMetaInfo, modelField, DefinitionModelAttributes.MULTI_VALUED, propertyDefinition.isMultiValued());
		addAttribute(fieldsMetaInfo, modelField, DefinitionModelAttributes.MANDATORY, propertyDefinition.isMandatory());
		addStringAttribute(fieldsMetaInfo, modelField, DefinitionModelAttributes.DISPLAY_TYPE, propertyDefinition.getDisplayType());

		return modelField;
	}

	private String getFullUri(PropertyDefinition propertyDefinition) {
		return namespaceRegistryService.buildFullUri(propertyDefinition.getUri());
	}

	private void addTooltipAttribute(PropertyDefinition propertyDefinition, Map<String, ModelMetaInfo> fieldsMetaInfo,
			ModelField modelField) {
		String tooltipId = propertyDefinition.getTooltipId();
		if (StringUtils.isNotBlank(tooltipId)) {
			LabelDefinition labelDefinition = labelService.getLabel(tooltipId);
			if (labelDefinition != null) {
				addAttribute(fieldsMetaInfo, modelField, DefinitionModelAttributes.TOOLTIP, new HashMap<>(labelDefinition.getLabels()));
			}
		}
	}

	private static BinaryOperator<ModelField> duplicateFieldMerger(GenericDefinition definition) {
		return (source, target) -> {
			LOGGER.warn("Merging duplicated fields {} in {}", source.getId(), definition.getIdentifier());

			source.getAttributes().forEach(attr -> {
				Optional<ModelAttribute> secondFieldAttr = target.getAttribute(attr.getName());
				if (secondFieldAttr.isPresent()) {
					secondFieldAttr
							.filter(attribute -> isValueBlank(attribute.getValue()))
							.ifPresent(attribute -> attribute.setValue(attr.getValue()));
				} else {
					target.addAttribute(attr.getName(), attr.getType(), attr.getValue());
				}
			});

			return target;
		};
	}

	private static boolean isValueBlank(Serializable value) {
		return Objects.isNull(value) || StringUtils.isBlank(value.toString());
	}

}
