package com.sirmaenterprise.sep.eai.spreadsheet.service.model;

import static com.sirmaenterprise.sep.eai.spreadsheet.model.EAISpreadsheetConstants.LOCALE_BG;
import static java.util.Locale.ENGLISH;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty.EntityPropertyMapping;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Builds the internal {@link ModelConfiguration} based on the current definition, semantic and codelist model.
 *
 * @author bbanchev
 */
@Singleton
public class SemanticSpreadsheetEAIModelConfigurationProvider implements SpreadsheetEAIModelConfigurationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private CodelistService codelistService;

	@Override
	public ModelConfiguration provideModel() throws EAIModelException {
		try {
			ModelConfiguration modelConfiguration = new ModelConfiguration();
			// extract types
			populateEntityTypes(modelConfiguration);

			modelConfiguration.seal();
			LOGGER.debug("Build EAI model: {}", modelConfiguration);
			return modelConfiguration;

		} catch (Exception e) {
			throw new EAIModelException("Failed to build integration models using semantic model", e);
		}
	}

	private void populateEntityTypes(ModelConfiguration modelConfiguration) { // NOSONAR
		definitionService.getAllDefinitions().map(this::checkAndConvertDefinitionToEntityType).forEach(
				modelConfiguration::addEntityType);
	}

	private EntityType checkAndConvertDefinitionToEntityType(DefinitionModel definition) {
		Optional<PropertyDefinition> typeProperty = definition.getField(DefaultProperties.TYPE);
		if (!typeProperty.isPresent()) {
			LOGGER.warn("Missing property '{}' in definition {}!", DefaultProperties.TYPE, definition.getIdentifier());
			return null;
		}
		ClassInstance semanticClass = getSemanticClass(definition);
		if (semanticClass == null) {
			LOGGER.warn("Definition {} has no set entity class!", definition.getIdentifier());
			return null;
		}
		EntityType type = new EntityType();
		type.setUri(Objects.toString(semanticClass.getId(), null));
		type.setIdentifier(definition.getIdentifier());
		CodeValue codeValue;
		PropertyDefinition propertyDefinition = typeProperty.get();
		if (propertyDefinition.getCodelist() != null
				&& (codeValue = codelistService.getCodeValue(propertyDefinition.getCodelist(), propertyDefinition.getDefaultValue())) != null) {
			type.setTitle(codeValue.getDescription(Locale.getDefault()));
			type.setMapping(codeValue.getDescription(ENGLISH));
			type.setMapping(codeValue.getDescription(LOCALE_BG));
		} else {
			LOGGER.debug("Missing codelist definition for property'{}'! Adding default value as is!",
					propertyDefinition);
			type.setMapping(propertyDefinition.getDefaultValue());
			type.setTitle(propertyDefinition.getDefaultValue());
		}
		type.addProperties(collectDefinitionProperties(definition));

		return type;
	}

	private ClassInstance getSemanticClass(DefinitionModel definition) {
		Optional<PropertyDefinition> classProperty = definition.getField(DefaultProperties.SEMANTIC_TYPE);
		if (!classProperty.isPresent()) {
			return null;
		}
		ClassInstance classInstance = semanticDefinitionService.getClassInstance(classProperty.get().getDefaultValue());
		if (classInstance == null) {
			return null;
		}
		return classInstance;
	}

	private static List<EntityProperty> collectDefinitionProperties(DefinitionModel definition) {
		Map<String, PropertyDefinition> fields = definition.getFieldsAsMap();
		List<EntityProperty> entityProperties = new LinkedList<>();
		for (Entry<String, PropertyDefinition> property : fields.entrySet()) {
			EntityProperty entityProperty = checkAndConvertEntityProperty(definition, property);
			if (entityProperty != null) {
				entityProperties.add(entityProperty);
			}
		}
		return entityProperties;
	}

	private static EntityProperty checkAndConvertEntityProperty(DefinitionModel definition,
			Entry<String, PropertyDefinition> property) {
		PropertyDefinition propertyDefinition = property.getValue();
		String uri = propertyDefinition.getUri();
		if (uri == null) {
			uri = propertyDefinition.getIdentifier();
		}
		if (EqualsHelper.nullSafeEquals("FORBIDDEN", uri, true)) {
			LOGGER.debug("FORBIDDEN property '{}' skipped in model {}!", propertyDefinition.getIdentifier(),
					definition.getIdentifier());
			return null;
		}
		EntityProperty entityProperty = new EntityProperty();
		entityProperty.setCodelist(propertyDefinition.getCodelist());
		entityProperty.setMandatory(propertyDefinition.isMandatory().booleanValue());
		entityProperty.setPropertyId(propertyDefinition.getIdentifier());
		entityProperty.setUri(uri);
		entityProperty.setTitle(Objects.toString(propertyDefinition.getLabel(), "") + "(" + uri + ")");
		entityProperty.setType(propertyDefinition.getDataType().getName());
		// uri as mapping
		entityProperty.addMapping(EntityPropertyMapping.AS_DATA, uri);
		return entityProperty;
	}
}