package com.sirma.sep.model.management.definition;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.model.management.ModelAction;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.ModelHeader;
import com.sirma.sep.model.management.ModelRegion;
import com.sirma.sep.model.management.codelists.CodeListsProvider;
import com.sirma.sep.model.management.converter.ModelConverterUtilities;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
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
	private final DefinitionModelHeaderConverter headerConverter;
	private final DefinitionModelActionConverter definitionModelActionConverter;
	private final DefinitionModelActionGroupConverter definitionModelActionGroupConverter;

	/**
	 * Initializes the converted with the supplied code list provider and model converters.
	 *
	 * @param codeListsProvider provides code values
	 * @param modelFieldConverter the converter for {@link ModelField}
	 * @param regionConverter converter for {@link ModelRegion}
	 * @param headerConverter converter for headers model
	 */
	@Inject
	public DefinitionModelConverter(CodeListsProvider codeListsProvider,
			DefinitionModelFieldConverter modelFieldConverter, DefinitionModelRegionConverter regionConverter,
			DefinitionModelHeaderConverter headerConverter,
			DefinitionModelActionConverter definitionModelActionConverter,
			DefinitionModelActionGroupConverter definitionModelActionGroupConverter) {
		this.codeListsProvider = codeListsProvider;
		this.modelFieldConverter = modelFieldConverter;
		this.regionConverter = regionConverter;
		this.headerConverter = headerConverter;
		this.definitionModelActionConverter = definitionModelActionConverter;
		this.definitionModelActionGroupConverter = definitionModelActionGroupConverter;
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

		removeFieldsWithoutUris(modelDefinitions);

		assignRdfType(modelDefinitions);

		assignLabels(modelDefinitions);

		return modelDefinitions;
	}

	private static Predicate<GenericDefinition> allowedDefinitionTypes() {
		return definition -> ALLOWED_DEFINITION_TYPES.contains(definition.getType().toUpperCase());
	}

	private static Collector<ModelDefinition, ?, Map<String, ModelDefinition>> definitionCollector() {
		return Collectors.toMap(ModelDefinition::getId, Function.identity(), duplicateDefinitionMerger());
	}

	private ModelDefinition constructModelDefinition(GenericDefinition definition, ModelsMetaInfo modelsMetaInfo) {
		ModelDefinition modelDefinition = new ModelDefinition();

		modelDefinition.setModelsMetaInfo(modelsMetaInfo);

		modelDefinition.setId(definition.getIdentifier());
		modelDefinition.setParent(definition.getParentDefinitionId());
		modelDefinition.setAbstract(definition.isAbstract());

		modelDefinition.setFields(modelFieldConverter.constructModelFields(definition, modelsMetaInfo));
		modelDefinition.setRegions(regionConverter.constructModelRegions(definition, modelsMetaInfo));
		modelDefinition.setHeaders(headerConverter.constructModelHeaders(definition, modelsMetaInfo));
		modelDefinition.setActions(definitionModelActionConverter.constructModelActions(definition, modelsMetaInfo));
		modelDefinition.setActionGroups(definitionModelActionGroupConverter.constructModelActionGroups(definition, modelsMetaInfo));
		modelDefinition.setAsDeployed();

		return modelDefinition;
	}

	/**
	 * Link parent child references for the given set of definitions and their fields
	 *
	 * @param definitions the definitions mapping to process
	 */
	public void linkDefinitions(Map<String, ModelDefinition> definitions) {
		// Link the hierarchy
		definitions.values().forEach(definition -> linkDefinitionsInternal(definition, definitions));

		// Find the edge definitions and start linking with parent references up to the root definition
		definitions.values().stream()
				.filter(definition -> definition.getChildren().isEmpty())
				.forEach(definition -> {
					linkFields(definition);
					linkRegions(definition);
					linkHeaders(definition);
					linkActions(definition);
					linkActionGroups(definition);
				});
	}

	private static void linkDefinitionsInternal(ModelDefinition definition, Map<String, ModelDefinition> definitions) {
		ModelDefinition parent = definitions.get(definition.getParent());
		if (parent != null) {
			parent.addChild(definition);
		}
	}

	private static void linkFields(ModelDefinition definition) {
		definition.getFields().forEach(field -> field.setContext(definition));

		if (definition.getParentReference() != null) {
			// Link only fields without already set parent references
			definition.getFields().stream().filter(f -> f.getParentReference() == null).forEach(field -> {
				Optional<ModelField> parentField = definition.getParentReference().findFieldByName(field.getId());
				parentField.ifPresent(field::setParentReference);
			});
			linkFields(definition.getParentReference());
		}
		definition.getFields().forEach(DefinitionModelConverter::fillTypeOption);
	}

	private static void fillTypeOption(ModelField field) {
		ModelField parent = field.getParentReference();
		String fieldTypeOptopn = resolveTypeOption(field);
		if (parent != null) {
			Optional<ModelAttribute> parentTypeOption = field.findAttribute(DefinitionModelAttributes.TYPE_OPTION);

			if (parentTypeOption.isPresent() && !parentTypeOption.get().getValue().equals(fieldTypeOptopn)) {
				ModelConverterUtilities.addAttribute(field, DefinitionModelAttributes.TYPE_OPTION, fieldTypeOptopn);
			}
			updateFieldTypeOption(field, parent);
		} else {
			ModelConverterUtilities.addAttribute(field, DefinitionModelAttributes.TYPE_OPTION, fieldTypeOptopn);
		}
	}

	private static String resolveTypeOption(ModelField field) {
		return TypeOptions.resolveTypeOption((String) getAttributeValue(field, DefinitionModelAttributes.TYPE),
				(Integer) getAttributeValue(field, DefinitionModelAttributes.CODE_LIST));
	}

	private static Object getAttributeValue(ModelField field, String name) {
		return field.getAttribute(name).map(ModelAttribute::getValue).orElse(null);
	}

	private static void updateFieldTypeOption(ModelField field, ModelField parentField) {
		Optional<ModelAttribute> fieldCodelist = field.getAttribute(DefinitionModelAttributes.CODE_LIST);
		Optional<ModelAttribute> parentTypeOption = parentField.getAttribute(DefinitionModelAttributes.TYPE_OPTION);
		if ((fieldCodelist.isPresent() && isAlphaNumeric(parentTypeOption))) {
			ModelConverterUtilities.addAttribute(field, DefinitionModelAttributes.TYPE_OPTION, "CODELIST");
		}
	}

	private static boolean isAlphaNumeric(Optional<ModelAttribute> typeOption) {
		return typeOption.isPresent() && typeOption.get().getValue().equals("ALPHA_NUMERIC_TYPE");
	}

	private static void linkRegions(ModelDefinition definition) {
		definition.getRegions().forEach(region -> region.setContext(definition));

		if (definition.getParentReference() != null) {
			// Link only regions without already set parent references
			definition.getRegions().stream().filter(r -> r.getParentReference() == null).forEach(region -> {
				Optional<ModelRegion> parentRegion = definition.getParentReference().findRegionByName(region.getId());
				parentRegion.ifPresent(region::setParentReference);
			});
			linkRegions(definition.getParentReference());
		}

		assignRegionsFieldIds(definition);
	}

	private static void assignRegionsFieldIds(ModelDefinition definition) {
		definition.getFields()
				.stream()
				.filter(field -> StringUtils.isNotBlank(field.findRegionId()))
				.filter(field -> definition.getRegionsMap().containsKey(field.findRegionId()))
				.forEach(field -> definition.getRegionsMap().get(field.findRegionId()).addField(field.getId()));
	}

	private static void linkHeaders(ModelDefinition definition) {
		definition.getHeaders().forEach(header -> header.setContext(definition));

		if (definition.getParentReference() != null) {
			// Link only regions without already set parent references
			definition.getHeaders()
					.stream()
					.filter(h -> h.getParentReference() == null)
					.forEach(header -> {
						Optional<ModelHeader> parentHeader = definition.getParentReference().findHeaderByName(header.getId());
						parentHeader.ifPresent(header::setParentReference);
					});
			linkHeaders(definition.getParentReference());
		}
	}

	private static void linkActions(ModelDefinition definition) {
		Collection<ModelAction> actions = definition.getActions();

		actions.forEach(action -> action.setContext(definition));

		Optional.ofNullable(definition.getParentReference()).ifPresent(parentReference -> {
			// Link only actions without already set parent references
			actions.stream()
					.filter(action -> action.getParentReference() == null)
					.forEach(action -> parentReference.findActionById(action.getId())
							.ifPresent(action::setParentReference));
			linkActions(parentReference);
		});

		// First assign information about which action execution is in which action
		actions.forEach(action -> action.getActionExecutions()
				.forEach(actionExecution -> actionExecution.setContext(action)));

		actions.forEach(ModelAction::relinkAllExecutions);
	}

	private static void linkActionGroups(ModelDefinition definition) {
		definition.getActionGroups().forEach(actionGroup -> actionGroup.setContext(definition));

		Optional.ofNullable(definition.getParentReference()).ifPresent(parentReference -> {
			// Link only action groups without already set parent references
			definition.getActionGroups()
					.stream()
					.filter(actionGroup -> actionGroup.getParentReference() == null)
					.forEach(actionGroup -> parentReference.findActionGroupById(actionGroup.getId())
							.ifPresent(actionGroup::setParentReference));
			linkActionGroups(parentReference);
		});
	}

	private static void removeFieldsWithoutUris(Map<String, ModelDefinition> definitions) {
		// Remove them here after conversion because during definition conversion, definitions are not compiled and their fields may lack URIs at all.
		definitions.values().forEach(definition -> definition.getFields().removeIf(field -> StringUtils.isBlank(field.getUri())));
	}

	private static void assignRdfType(Map<String, ModelDefinition> definitions) {
		definitions.values().forEach(definition -> {
			Optional<ModelField> rdfTypeField = definition.findFieldByName(RDF_TYPE);
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
		Map<Integer, Map<String, CodeValue>> valuesCache = CollectionUtils.createHashMap(20);
		definitions.values().forEach(definition -> {
			Optional<ModelField> typeField = definition.findFieldByName("type");
			if (typeField.isPresent()) {
				if (StringUtils.isBlank(typeField.get().getValue())) {
					LOGGER.debug("No type defined for {}, using the ID as label.", definition.getId());
					definition.setLabels(buildLabelMapFromDefinition(definition));
				} else {
					assignLabels(definition, typeField.get(), valuesCache);
				}
			} else {
				LOGGER.warn("Couldn't determine emf:type for {}!", definition.getId());
			}
		});
	}

	private void assignLabels(ModelDefinition definition, ModelField typeField,
			Map<Integer, Map<String, CodeValue>> valuesCache) {
		Optional<ModelAttribute> codeList = typeField.findAttribute(DefinitionModelAttributes.CODE_LIST);
		if (codeList.isPresent()) {
			Object value = codeList.get().getValue();
			if (value instanceof Integer) {
				Map<String, CodeValue> codeValues = valuesCache.computeIfAbsent((Integer) value, codeListsProvider::getValues);

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
		return codeValue.getDescriptions()
				.stream()
				.filter(DefinitionModelConverter::isCodeDescriptionValid)
				.collect(Collectors.toMap(d -> d.getLanguage().toLowerCase(), CodeDescription::getName));
	}

	private static boolean isCodeDescriptionValid(CodeDescription description) {
		return StringUtils.isNotBlank(description.getLanguage()) && StringUtils.isNotBlank(description.getName());
	}

	private static BinaryOperator<ModelDefinition> duplicateDefinitionMerger() {
		return (d1, d2) -> {
			LOGGER.warn("Duplicated definition model during mapping {}", d2.getId());
			return d1;
		};
	}
}
