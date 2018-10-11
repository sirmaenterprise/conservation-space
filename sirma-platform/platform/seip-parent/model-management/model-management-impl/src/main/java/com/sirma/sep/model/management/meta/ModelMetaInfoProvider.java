package com.sirma.sep.model.management.meta;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.exception.EmfRuntimeException;

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

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private LabelService labelService;

	/**
	 * Provides models meta information calculated with up to date data from the semantic database and definitions.
	 *
	 * @return the models meta information
	 */
	public ModelsMetaInfo getModelsMetaInfo() {
		ModelsMetaInfo metaInfo;
		try {
			ObjectMapper jsonMapper = new ObjectMapper();
			metaInfo = jsonMapper.readValue(getMapping(), ModelsMetaInfo.class);
		} catch (IOException e) {
			LOGGER.error("Cannot read meta info mapping file because of {}", e.getMessage());
			throw new EmfRuntimeException(e);
		}

		// Populate semantic labels
		assignSemanticLabels(metaInfo.getSemantics());
		assignSemanticLabels(metaInfo.getProperties());

		// Populate labels
		assignDefinitionLabels(metaInfo.getDefinitions(), DEFINITION_LABEL_PREFIX);
		assignDefinitionLabels(metaInfo.getFields(), FIELD_LABEL_PREFIX);
		assignDefinitionLabels(metaInfo.getRegions(), REGION_LABEL_PREFIX);

		return metaInfo;
	}

	private void assignSemanticLabels(Collection<ModelMetaInfo> metaInfoCollection) {
		metaInfoCollection.forEach(metaInfo -> {
			PropertyInstance property = getProperty(metaInfo.getUri());
			if (property != null) {
				metaInfo.setLabels(property.getLabels());
			} else {
				LOGGER.warn("Missing semantic property \"{}\" for meta info model {}", metaInfo.getUri(), metaInfo.getId());
			}
		});
	}

	private void assignDefinitionLabels(Collection<ModelMetaInfo> metaInfoCollection, String metaInfoPrefix) {
		metaInfoCollection.forEach(metaInfo -> {
			String labelId = LABEL_PREFIX + metaInfoPrefix + metaInfo.getId();
			LabelDefinition labelDefinition = labelService.getLabel(labelId);
			if (labelDefinition != null) {
				metaInfo.setLabels(labelDefinition.getLabels());
			} else {
				LOGGER.warn("Missing definition label {} for meta info model {}", labelId, metaInfo.getId());
			}
		});
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
