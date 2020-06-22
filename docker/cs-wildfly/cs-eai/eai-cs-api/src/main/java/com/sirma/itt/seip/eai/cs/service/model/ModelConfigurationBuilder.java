package com.sirma.itt.seip.eai.cs.service.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.eai.cs.EAIServicesConstants;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchCriterion;
import com.sirma.itt.seip.eai.model.mapping.search.EntitySearchType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.eai.util.XlsxModelParser;
import com.sirma.itt.seip.eai.util.XlsxModelParser.ModelPropertyIds;

/**
 * Builds the internal {@link ModelConfiguration} for CS.
 *
 * @author bbanchev
 */
public class ModelConfigurationBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ModelConfigurationBuilder() {
		// utility class
	}

	/**
	 * Provide {@link ModelConfiguration} from a set of xlxs containing:
	 * <code> common.xlsx, types.xlsx, relations.xlsx</code>, other models described in types.xlsx .
	 *
	 * @param modelsPath
	 *            the models path with the xlsx set
	 * @param typeModel
	 *            the type model mapping of {@link ModelPropertyIds} ids as column number
	 * @return the model configuration or exception of failure
	 * @throws EAIModelException
	 *             on model parsing failure
	 */
	public static ModelConfiguration provideDataModelFromXlsx(File modelsPath, Map<String, Integer> typeModel)
			throws EAIModelException {
		try {
			ModelConfiguration modelConfiguration = new ModelConfiguration();
			// extract types
			populateEntityTypes(modelsPath, modelConfiguration, typeModel);
			// extract relations
			populateEntityRelations(modelsPath, modelConfiguration);

			return modelConfiguration;

		} catch (Exception e) {
			throw new EAIModelException("Failed to build data integration model using configuration: " + modelsPath, e);
		}
	}

	/**
	 * Builds a {@link SearchModelConfiguration} using the provided configuration model and an existing
	 * {@link ModelConfiguration}
	 * 
	 * @param modelsPath
	 *            the models path with the xlsx set
	 * @param modelConfiguration
	 *            obtained using {@link #provideDataModelFromXlsx(File, Map)}
	 * @param semanticDefinitionService
	 *            is the semantic service used to configure the criteria types
	 * @param defaultType
	 *            is the default searched type to set if no class configuration is provided
	 * @return the built {@link SearchModelConfiguration}
	 * @throws EAIModelException
	 *             on model parsing failure
	 */
	public static SearchModelConfiguration provideSearchModelFromXlsx(File modelsPath,
			ModelConfiguration modelConfiguration, SemanticDefinitionService semanticDefinitionService,
			String defaultType) throws EAIModelException {
		if (modelConfiguration == null) {
			throw new EAIModelException("Failed to build search integration model using invalid configuration!");
		}
		try {
			SearchModelConfiguration searchConfiguration = new SearchModelConfiguration();
			// populate entity type
			EntitySearchType type = populateEntityCriteriaTypes(modelConfiguration, defaultType,
					semanticDefinitionService);
			// extract criteria
			populateEntityCriteria(modelsPath, modelConfiguration, type, searchConfiguration);

			return searchConfiguration;

		} catch (Exception e) {
			throw new EAIModelException("Failed to build search integration model using configuration: " + modelsPath,
					e);
		}
	}

	private static EntitySearchType populateEntityCriteriaTypes(ModelConfiguration modelConfiguration,
			String defaultType, SemanticDefinitionService semanticDefinitionService) {
		String typeURI = null;
		Set<String> typeUris = modelConfiguration
				.getEntityTypes()
					.stream()
					.map(EntityType::getUri)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
		for (String uri : typeUris) {
			List<String> hierarchy = semanticDefinitionService.getHierarchy(uri);
			if (hierarchy != null) {
				if (hierarchy.contains(EAIServicesConstants.TYPE_CULTURAL_OBJECT)) {
					typeURI = EAIServicesConstants.TYPE_CULTURAL_OBJECT;
				} else if (hierarchy.contains(EAIServicesConstants.TYPE_IMAGE)) {
					typeURI = EAIServicesConstants.TYPE_IMAGE;
				}
			}
			if (typeURI != null) {
				break;
			}
		}
		if (typeURI == null) {
			typeURI = defaultType;
			LOGGER.error(
					"Search model configuration cound not be built based on model configuration. Using default model!");
		}
		EntitySearchType systemType = new EntitySearchType();
		systemType.setUri(typeURI);
		systemType.setIdentifier(TypeConverterUtil.getConverter().convert(ShortUri.class, typeURI).toString());
		ClassInstance classInstance = semanticDefinitionService.getClassInstance(typeURI);
		if (classInstance != null) {
			systemType.setType("class");
			systemType.setTitle(classInstance.getLabel());
		} else {
			systemType.setTitle(typeURI);
		}
		return systemType;
	}

	private static void populateEntityCriteria(File modelsPath, ModelConfiguration modelConfiguration,
			EntitySearchType type, SearchModelConfiguration searchConfiguration) throws IOException {
		File search = new File(modelsPath, "search.xlsx");
		try (FileInputStream stream = new FileInputStream(search)) {
			List<EntitySearchCriterion> searchCriteria = XlsxModelParser.parseSearchModelXlsx(stream, search.getName());
			for (EntitySearchCriterion criterion : searchCriteria) {
				Set<EntityProperty> propertiesByInternalName = modelConfiguration
						.getPropertyByInternalName(criterion.getPropertyId());
				if (propertiesByInternalName != null && !propertiesByInternalName.isEmpty()) {
					searchConfiguration.addCriterion(type, criterion, propertiesByInternalName.iterator().next());
				} else {
					LOGGER.error("Could not add criterion with id: " + criterion.getPropertyId()
							+ " to search configuration model!");
				}
			}
		}
	}

	private static void populateEntityRelations(File modelsPath, ModelConfiguration modelConfiguration)
			throws IOException {
		Set<EntityType> entityTypesModel = modelConfiguration.getEntityTypes();
		File relationsMapping = new File(modelsPath, "relations.xlsx");
		try (FileInputStream stream = new FileInputStream(relationsMapping)) {
			List<EntityRelation> entityRelationsModels = XlsxModelParser.parseRelationsXlsx(stream,
					relationsMapping.getName());
			addEntityRelationsToTypes(entityTypesModel, entityRelationsModels);
		}
	}

	private static void addEntityRelationsToTypes(Set<EntityType> typesModel,
			List<EntityRelation> entityRelationsModels) {

		for (EntityRelation entityRelation : entityRelationsModels) {
			// "cultural object" probably would be configured as string id
			String domain = entityRelation.getDomain();
			if ("all".equalsIgnoreCase(domain) || "entity".equalsIgnoreCase(domain) || "media".equalsIgnoreCase(domain)
					|| "cultural object".equalsIgnoreCase(domain)) {
				// wildcard relation to all current types
				typesModel.stream().forEach(entityType -> entityType.addRelation(entityRelation));
			} else {
				// this is for specific id only
				Stream<EntityType> entitiesByName = typesModel
						.stream()
							.filter(e -> e.getTitle().equalsIgnoreCase(domain))
							.map(e -> {
								e.addRelation(entityRelation);
								return e;
							});
				if (entitiesByName.count() == 0) {
					LOGGER.warn("Domain {} not found in types: {} for the relation: {}", domain, typesModel,
							entityRelation);
				}
			}
		}
	}

	private static void populateEntityTypes(File modelsPath, ModelConfiguration modelConfiguration,
			Map<String, Integer> typesModel) throws IOException {
		File typesFile = new File(modelsPath, "types.xlsx");
		List<EntityType> types;
		try (FileInputStream typesStream = new FileInputStream(typesFile)) {
			types = XlsxModelParser.parseTypesXlsx(typesStream, typesFile.getName());
		}

		File commonMapping = new File(modelsPath, "common.xlsx");

		List<EntityProperty> commonProperties = null;
		try (FileInputStream stream = new FileInputStream(commonMapping)) {
			commonProperties = XlsxModelParser.parseModelXlsx(stream, commonMapping.getName(), typesModel);
		}
		for (EntityType entityType : types) {
			File typeXlsx = new File(modelsPath, entityType.getIdentifier() + ".xlsx");
			try (FileInputStream stream = new FileInputStream(typeXlsx)) {
				List<EntityProperty> typeProperties = XlsxModelParser.parseModelXlsx(stream, typeXlsx.getName(),
						typesModel);
				// override common with the specific
				entityType.addProperties(commonProperties);
				entityType.addProperties(typeProperties);
				modelConfiguration.addEntityType(entityType);
			}
		}
	}

}
