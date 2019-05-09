package com.sirma.sep.model.management.meta;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.sep.model.management.definition.LabelProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provider for the available models meta information.
 * <p>
 * A correct JSON mapping must be managed in <i>models-meta-info.json</i> resource.
 * <p>
 * Labels for semantic meta models are retrieved from their corresponding {@link PropertyInstance} obtained via
 * {@link SemanticDefinitionService#getProperty(String)} where definitions meta info use {@link LabelDefinition}.
 *
 * @author Mihail Radkov
 */
public class ModelMetaInfoProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String LABEL_PREFIX = "model.management.";
	private static final String DEFINITION_LABEL_PREFIX = "definition.";
	private static final String FIELD_LABEL_PREFIX = "field.";
	private static final String REGION_LABEL_PREFIX = "region.";
	private static final String HEADER_LABEL_PREFIX = "header.";
	private static final String ACTION_LABEL_PREFIX = "action.";
	private static final String ACTION_EXECUTION_LABEL_PREFIX = "actionExecution.";
	private static final String ACTION_GROUP_LABEL_PREFIX = "actionGroup.";
	private static final String DESCRIPTION_SUFFIX = ".description";
	private static final String DESCRIPTION_PROPERTY_NAME = "definition";

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Provides models meta information calculated with up to date data from the semantic database and definitions.
	 *
	 * @return the models meta information
	 */
	public ModelsMetaInfo getModelsMetaInfo() {
		ModelsMetaInfo modelsMetaInfo;
		try {
			ObjectMapper jsonMapper = new ObjectMapper();
			modelsMetaInfo = jsonMapper.readValue(getMapping(), ModelsMetaInfo.class);
		} catch (IOException e) {
			LOGGER.error("Cannot read meta info mapping file because of {}", e.getMessage());
			throw new EmfRuntimeException(e);
		}

		// Populate semantic labels and descriptions
		assignSemanticLabelsAndDescriptions(modelsMetaInfo.getSemantics());
		assignSemanticLabelsAndDescriptions(modelsMetaInfo.getProperties());

		// Populate labels and descriptions
		assignDefinitionLabelsAndDefinitions(modelsMetaInfo.getDefinitions(), DEFINITION_LABEL_PREFIX);
		assignDefinitionLabelsAndDefinitions(modelsMetaInfo.getFields(), FIELD_LABEL_PREFIX);
		assignDefinitionLabelsAndDefinitions(modelsMetaInfo.getRegions(), REGION_LABEL_PREFIX);
		assignDefinitionLabelsAndDefinitions(modelsMetaInfo.getHeaders(), HEADER_LABEL_PREFIX);
		assignDefinitionLabelsAndDefinitions(modelsMetaInfo.getActions(), ACTION_LABEL_PREFIX);
		assignDefinitionLabelsAndDefinitions(modelsMetaInfo.getActionExecutions(), ACTION_EXECUTION_LABEL_PREFIX);
		assignDefinitionLabelsAndDefinitions(modelsMetaInfo.getActionGroups(), ACTION_GROUP_LABEL_PREFIX);

		modelsMetaInfo.seal();
		return modelsMetaInfo;
	}

	private void assignSemanticLabelsAndDescriptions(Collection<ModelMetaInfo> metaInfoCollection) {
		metaInfoCollection.forEach(metaInfo -> {
			PropertyInstance property = getProperty(metaInfo.getUri());
			if (property != null) {
				metaInfo.setLabels(property.getLabels());
				Map<String, String> descriptions = property.get(DESCRIPTION_PROPERTY_NAME, Map.class);
				metaInfo.setDescriptions(descriptions);
			} else {
				LOGGER.warn("Missing semantic property \"{}\" for meta info model {}", metaInfo.getUri(), metaInfo.getId());
			}
		});
	}

	private void assignDefinitionLabelsAndDefinitions(Collection<ModelMetaInfo> metaInfoCollection, String metaInfoPrefix) {
		metaInfoCollection.stream().filter(ModelMetaInfo::isVisible).forEach(metaInfo -> {
			assignDefinitionLabels(metaInfo, metaInfoPrefix);
			assignDefinitionDescriptions(metaInfo, metaInfoPrefix);
		});
	}

	private void assignDefinitionLabels(ModelMetaInfo metaInfo, String metaInfoPrefix) {
		String labelId = LABEL_PREFIX + metaInfoPrefix + metaInfo.getId();
		Map<String, String> labels = labelProvider.getLabels(labelId);
		if (CollectionUtils.isNotEmpty(labels)) {
			metaInfo.setLabels(labels);
		} else {
			LOGGER.warn("Missing definition label {} for meta info model {}", labelId, metaInfo.getId());
		}
	}

	private void assignDefinitionDescriptions(ModelMetaInfo metaInfo, String metaInfoPrefix) {
		String descriptionId = LABEL_PREFIX + metaInfoPrefix + metaInfo.getId() + DESCRIPTION_SUFFIX;

		Map<String, String> descriptions = labelProvider.getLabels(descriptionId);
		if (CollectionUtils.isNotEmpty(descriptions)) {
			metaInfo.setDescriptions(descriptions);
		} else {
			LOGGER.warn("Missing definition description {} for meta info model {}", descriptionId, metaInfo.getId());
		}
	}

	private PropertyInstance getProperty(String uri) {
		PropertyInstance property = semanticDefinitionService.getProperty(uri);
		if (property == null) {
			return semanticDefinitionService.getRelation(uri);
		}
		return property;
	}

	private InputStream getMapping() {
		return getClass().getResourceAsStream("models-meta-info.json");
	}
}
