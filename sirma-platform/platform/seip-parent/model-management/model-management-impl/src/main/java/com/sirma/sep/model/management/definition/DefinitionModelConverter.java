package com.sirma.sep.model.management.definition;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.ModelRegion;
import com.sirma.sep.model.management.codelists.CodeListsProvider;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Converts provided non compiled {@link GenericDefinition} into {@link ModelDefinition}.
 * <p>
 * This includes converting {@link com.sirma.itt.seip.domain.definition.PropertyDefinition} from each {@link GenericDefinition} into {@link
 * ModelField}.
 *
 * @author Mihail Radkov
 */
public class DefinitionModelConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String RDF_TYPE = "rdf:type";
	private static final Set<String> ALLOWED_DEFINITION_TYPES;

	static {
		ALLOWED_DEFINITION_TYPES = CollectionUtils.createHashSet(9);
		ALLOWED_DEFINITION_TYPES.addAll(
				Arrays.asList("OBJECT", "PROJECT", "DOCUMENT", "TASK", "WORKFLOW", "USER", "CASE", "GROUP", "CLASS"));
	}

	private final CodeListsProvider codeListsProvider;
	private final DefinitionModelFieldConverter modelFieldConverter;
	private final DefinitionModelRegionConverter regionConverter;

	/**
	 * Initializes the converted with the supplied code list provider and model converters.
	 *
	 * @param codeListsProvider provides code values
	 * @param modelFieldConverter the converter for {@link ModelField}
	 * @param regionConverter converter for {@link ModelRegion}
	 */
	@Inject
	public DefinitionModelConverter(CodeListsProvider codeListsProvider, DefinitionModelFieldConverter modelFieldConverter,
			DefinitionModelRegionConverter regionConverter) {
		this.codeListsProvider = codeListsProvider;
		this.modelFieldConverter = modelFieldConverter;
		this.regionConverter = regionConverter;
	}

	/**
	 * Converts the provided non compiled {@link GenericDefinition} into a map of converted {@link ModelDefinition}.
	 *
	 * @param definitions the non compiled definitions to be converted to {@link ModelDefinition}
	 * @param modelsMetaInfo meta information used for converting the models
	 * @return converted {@link ModelDefinition}
	 */
	public Map<String, ModelDefinition> convertModelDefinitions(List<GenericDefinition> definitions,
			ModelsMetaInfo modelsMetaInfo) {
		Map<String, ModelDefinition> modelDefinitions = definitions.stream()
				.filter(allowedDefinitionTypes())
				.map(definition -> constructModelDefinition(definition, modelsMetaInfo))
				.collect(definitionCollector());

		linkDefinitions(modelDefinitions);
		linkFields(modelDefinitions);
		linkRegions(modelDefinitions);

		removeFieldsWithoutUris(modelDefinitions);

		assignRdfType(modelDefinitions);
		assignLabels(modelDefinitions);

		return modelDefinitions;
	}

	private static Predicate<GenericDefinition> allowedDefinitionTypes() {
		return definition -> ALLOWED_DEFINITION_TYPES.contains(definition.getType().toUpperCase());
	}

	private static Collector<ModelDefinition, ?, Map<String, ModelDefinition>> definitionCollector() {
		return Collectors.toMap(ModelDefinition::getId, d -> d, duplicateDefinitionMerger());
	}

	private ModelDefinition constructModelDefinition(GenericDefinition definition, ModelsMetaInfo modelsMetaInfo) {
		ModelDefinition modelDefinition = new ModelDefinition();

		modelDefinition.setId(definition.getIdentifier());
		modelDefinition.setParent(definition.getParentDefinitionId());
		modelDefinition.setAbstract(definition.isAbstract());

		modelDefinition.setFields(modelFieldConverter.constructModelFields(definition, modelsMetaInfo.getFieldsMapping()));
		modelDefinition.setRegions(regionConverter.constructModelRegions(definition, modelsMetaInfo.getRegionsMapping()));

		return modelDefinition;
	}

	private static void linkDefinitions(Map<String, ModelDefinition> definitions) {
		definitions.values().forEach(definition -> {
			ModelDefinition parent = definitions.get(definition.getParent());
			if (parent != null) {
				parent.getChildren().add(definition);
				definition.setParentReference(parent);
			}
		});
	}

	private static void linkFields(Map<String, ModelDefinition> definitions) {
		// First assign information about which field is in which definition
		definitions.values().forEach(def -> def.getFields().forEach(field -> field.setDefinitionReference(def)));

		// Start from edgy definitions
		definitions.values().stream().filter(d -> d.getChildren().isEmpty()).forEach(DefinitionModelConverter::linkFields);
	}

	private static void linkFields(ModelDefinition definition) {
		if (definition.getParentReference() != null) {
			// Link only fields without already set parent references
			definition.getFields().stream().filter(f -> f.getParentReference() == null).forEach(field -> {
				Optional<ModelField> parentField = definition.getParentReference().getFieldByName(field.getId());
				parentField.ifPresent(field::setParentReference);
			});
			linkFields(definition.getParentReference());
		}
	}

	private static void linkRegions(Map<String, ModelDefinition> definitions) {
		// First assign information about which region is in which definition
		definitions.values().forEach(def -> def.getRegions().forEach(region -> region.setDefinitionReference(def)));

		// Start from edgy definitions
		definitions.values().stream().filter(d -> d.getChildren().isEmpty()).forEach(DefinitionModelConverter::linkRegions);
	}

	private static void linkRegions(ModelDefinition definition) {
		if (definition.getParentReference() != null) {
			// Link only regions without already set parent references
			definition.getRegions().stream().filter(f -> f.getParentReference() == null).forEach(region -> {
				Optional<ModelRegion> parentRegion = definition.getParentReference().getRegionByName(region.getId());
				parentRegion.ifPresent(region::setParentReference);
			});
			linkRegions(definition.getParentReference());
		}

		assignRegionsFieldIds(definition);
	}

	private static void assignRegionsFieldIds(ModelDefinition definition) {
		Map<String, ModelRegion> regionMapping = definition.getRegions()
				.stream()
				.collect(Collectors.toMap(ModelRegion::getId, Function.identity()));

		definition.getFields()
				.stream()
				.filter(field -> StringUtils.isNotBlank(field.getRegionId()))
				.filter(field -> regionMapping.containsKey(field.getRegionId()))
				.forEach(field -> regionMapping.get(field.getRegionId()).addField(field.getId()));
	}

	private static void removeFieldsWithoutUris(Map<String, ModelDefinition> definitions) {
		// Remove them here after conversion because during definition conversion, definitions are not compiled and their fields may lack URIs at all.
		definitions.values().forEach(definition -> {
			List<ModelField> filteredFields = definition.getFields()
					.stream()
					.filter(field -> StringUtils.isNotBlank(field.getUri()))
					.collect(Collectors.toList());
			definition.setFields(filteredFields);
		});
	}

	private static void assignRdfType(Map<String, ModelDefinition> definitions) {
		definitions.values().forEach(definition -> {
			Optional<ModelField> rdfTypeField = definition.getFieldByName(RDF_TYPE);
			if (rdfTypeField.isPresent()) {
				String rdfType = rdfTypeField.get().getValue();
				if (StringUtils.isNotBlank(rdfType)) {
					definition.setRdfType(rdfType);
				} else {
					LOGGER.warn("Couldn't determine the rdf:type for {}", definition.getId());
				}
			} else {
				LOGGER.warn("No rdf:type field is defined for {}", definition.getId());
			}
		});
	}

	private void assignLabels(Map<String, ModelDefinition> definitions) {
		definitions.values().forEach(definition -> {
			Optional<ModelField> typeField = definition.getFieldByName("type");
			if (typeField.isPresent()) {
				if (StringUtils.isBlank(typeField.get().getValue())) {
					LOGGER.debug("No type defined for {}, using the ID as label.", definition.getId());
					definition.setLabels(buildLabelMapFromDefinition(definition));
				} else {
					assignLabels(definition, typeField.get());
				}
			} else {
				LOGGER.warn("Couldn't determine emf:type for {}!", definition.getId());
			}
		});
	}

	private void assignLabels(ModelDefinition definition, ModelField typeField) {
		Optional<ModelAttribute> codeList = typeField.getAttribute(DefinitionModelAttributes.CODE_LIST);
		if (codeList.isPresent()) {
			Serializable value = codeList.get().getValue();
			if (value instanceof Integer) {
				Map<String, CodeValue> codeValues = codeListsProvider.getValues((Integer) value);

				CodeValue codeValue = codeValues.get(typeField.getValue());
				if (codeValue == null) {
					LOGGER.warn("No code list value is available for the type of {}, using the ID as label.", definition.getId());
					definition.setLabels(buildLabelMapFromDefinition(definition));
				} else {
					definition.setLabels(buildLabelMap(codeValue));
				}
			} else if (value != null) {
				LOGGER.warn("Incompatible type {} for code list attribute for field {} in {}!", value.getClass(), typeField.getUri(),
						definition.getId());
			} else {
				LOGGER.warn("Null value for code list attribute for field {} in {}!", typeField.getUri(),
						definition.getId());
			}
		} else {
			LOGGER.warn("Missing code list number for emf:type field in {}!", definition.getId());
		}
	}

	private static Map<String, String> buildLabelMapFromDefinition(ModelDefinition definition) {
		HashMap<String, String> langMap = new HashMap<>(1);
		langMap.put("en", definition.getId());
		return langMap;
	}

	private static Map<String, String> buildLabelMap(CodeValue codeValue) {
		return codeValue.getProperties()
				.entrySet()
				.stream()
				.filter(DefinitionModelConverter::isLanguageValid)
				.collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue().toString()));
	}

	private static boolean isLanguageValid(Map.Entry<String, Serializable> entry) {
		return StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString());
	}

	private static BinaryOperator<ModelDefinition> duplicateDefinitionMerger() {
		return (d1, d2) -> {
			LOGGER.warn("Duplicated definition model during mapping {}", d2.getId());
			return d1;
		};
	}
}
