package com.sirma.sep.model.management.definition.export;

import static com.sirma.sep.model.management.definition.export.TransitionDefinitionConverter.copyToTransitions;
import static com.sirma.sep.model.management.definition.export.TransitionGroupDefinitionConverter.copyToTransitionGroups;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.ModelHeader;
import com.sirma.sep.model.management.ModelRegion;

/**
 * Converter for copying {@link ModelDefinition} attributes into {@link GenericDefinition} along with any related {@link ModelField} and
 * {@link ModelRegion}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @author Mihail Radkov
 * @see PropertyDefinitionConverter
 * @see RegionDefinitionConverter
 */
public class GenericDefinitionConverter {

	private final PropertyDefinitionConverter propertyDefinitionConverter;
	private final RegionDefinitionConverter regionDefinitionConverter;

	/**
	 * Constructs the definition converter with the dependent converters.
	 *
	 * @param propertyDefinitionConverter converts {@link ModelField}
	 * @param regionDefinitionConverter converts {@link ModelRegion}
	 */
	@Inject
	public GenericDefinitionConverter(
			PropertyDefinitionConverter propertyDefinitionConverter,
			RegionDefinitionConverter regionDefinitionConverter) {
		this.propertyDefinitionConverter = propertyDefinitionConverter;
		this.regionDefinitionConverter = regionDefinitionConverter;
	}

	/**
	 * Read the provided {@link ModelDefinition} and copies attributes to the given {@link GenericDefinition}
	 *
	 * @param modelDefinition the model to read and transfer attributes to GenericDefinition
	 * @param definition the definition into which attributes will be copied
	 */
	public void copyToDefinition(ModelDefinition modelDefinition, GenericDefinitionImpl definition) {
		definition.setIdentifier(modelDefinition.getId());
		definition.setAbstract(modelDefinition.isAbstract());
		definition.setParentDefinitionId(modelDefinition.getParent());

		Map<String, PropertyDefinition> definitionProperties = getDefinitionProperties(definition);

		copyFieldsData(modelDefinition, definition, definitionProperties);

		copyRegionsData(modelDefinition, definition);

		copyRegionFields(modelDefinition, definition, definitionProperties);

		copyHeaders(modelDefinition, definition, definitionProperties);

		copyToTransitions(modelDefinition, definition);

		copyToTransitionGroups(modelDefinition, definition);
	}

	private static Map<String, PropertyDefinition> getDefinitionProperties(GenericDefinition definition) {
		// Top level properties + those in regions
		// This way of mapping ensures that system fields will be replaced with the real fields (from regions)
		return definition.fieldsStream()
				.collect(Collectors.toMap(PropertyDefinition::getName, Function.identity(), (o, o2) -> o2, LinkedHashMap::new));
	}

	/**
	 * Copies all {@link ModelField} and their attributes from the provided {@link ModelDefinition} in the given
	 * {@link GenericDefinitionImpl} as {@link PropertyDefinition} by using {@link PropertyDefinitionConverter}.
	 * <p>
	 * This will not process region fields.
	 * <p>
	 * The process will clear all top level fields in the provided {@link GenericDefinitionImpl} and then repopulate them by reusing
	 * property definitions from the provided map or create new ones and then copy the data from {@link ModelField}.
	 *
	 * @param modelDefinition the source definition to copy fields from
	 * @param definition the target definition to copy fields to
	 * @param allDefinitionProperties properties from the definition including those from regions too
	 */
	private void copyFieldsData(ModelDefinition modelDefinition, GenericDefinitionImpl definition,
			Map<String, PropertyDefinition> allDefinitionProperties) {
		// Skip all region fields
		List<ModelField> modelFields = modelDefinition.getFields()
				.stream()
				.filter(field -> !field.hasRegionId())
				.collect(Collectors.toList());

		copyFields(modelFields, definition, allDefinitionProperties);
	}

	private void copyFields(List<ModelField> fields, DefinitionModel definitions, Map<String, PropertyDefinition> definitionProperties) {
		// Clearing as they will be repopulated using the provided map or simply creating new properties
		// Reusing the map ensures no field controls or conditions will be lost if the field has been moved in or out of region
		definitions.getFields().clear();

		fields.forEach(field -> {
			PropertyDefinition propertyDefinition = definitionProperties.get(field.getId());
			if (propertyDefinition == null) {
				propertyDefinition = propertyDefinitionConverter.newProperty(field);
			} else {
				propertyDefinitionConverter.copyField(field, (WritablePropertyDefinition) propertyDefinition);
			}
			definitions.getFields().add(propertyDefinition);
		});
	}

	private void copyRegionsData(ModelDefinition modelDefinition, GenericDefinitionImpl definition) {

		// Only regions that have fields
		List<ModelRegion> modelRegions = modelDefinition.getRegions()
				.stream()
				.filter(region -> !region.isEmpty())
				.collect(Collectors.toList());

		Map<String, RegionDefinition> definitionRegions = definition.getRegions()
				.stream()
				.collect(CollectionUtils.toIdentityMap(RegionDefinition::getIdentifier));

		definition.getRegions().clear();

		modelRegions.forEach(modelRegion -> {
			RegionDefinition regionDefinition = definitionRegions.get(modelRegion.getId());
			if (regionDefinition == null) {
				regionDefinition = regionDefinitionConverter.newRegion(modelRegion);
			} else {
				regionDefinitionConverter.copyRegion(modelRegion, (RegionDefinitionImpl) regionDefinition);
			}
			definition.getRegions().add(regionDefinition);
		});
	}

	private void copyRegionFields(ModelDefinition modelDefinition, GenericDefinitionImpl definition,
			Map<String, PropertyDefinition> definitionProperties) {

		Map<String, List<ModelField>> regionToFieldsMap = modelDefinition.getFields()
				.stream()
				.filter(ModelField::hasRegionId)
				.collect(Collectors.groupingBy(ModelField::getRegionId));

		definition.getRegions().forEach(regionDefinition -> {
			// A region may be emptied of fields
			List<ModelField> regionFields = regionToFieldsMap.getOrDefault(regionDefinition.getIdentifier(), CollectionUtils.emptyList());
			copyFields(regionFields, regionDefinition, definitionProperties);
		});
	}

	private void copyHeaders(ModelDefinition modelDefinition, GenericDefinitionImpl definition,
			Map<String, PropertyDefinition> removedProperties) {

		// Header fields should be removed by now, try to get them from the removed ones or create new
		modelDefinition
				.getHeaders()
				.stream()
				.filter(ModelNode::hasAttributes)
				.map(header -> getHeaderProperty(definition, removedProperties, header))
				.forEach(definition.getFields()::add);
	}

	private WritablePropertyDefinition getHeaderProperty(GenericDefinitionImpl definition, Map<String, PropertyDefinition> properties,
			ModelHeader header) {
		String headerKey = header.getId();
		WritablePropertyDefinition headerProperty = (WritablePropertyDefinition)
				properties.getOrDefault(headerKey, propertyDefinitionConverter.newHeaderProperty(definition, headerKey));
		propertyDefinitionConverter.normalizeHeaderProperty(header, headerProperty);
		return headerProperty;
	}
}
