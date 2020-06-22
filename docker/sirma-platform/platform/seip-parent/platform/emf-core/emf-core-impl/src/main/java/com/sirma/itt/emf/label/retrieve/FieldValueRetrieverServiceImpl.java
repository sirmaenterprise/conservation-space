package com.sirma.itt.emf.label.retrieve;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;

/**
 * Implementation of {@link FieldValueRetrieverService}
 */
@ApplicationScoped
public class FieldValueRetrieverServiceImpl implements FieldValueRetrieverService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** The extension mapping. */
	private Map<String, FieldValueRetriever> extensionMapping;

	/** The extensions. */
	@Inject
	@ExtensionPoint(FieldValueRetriever.TARGET_NAME)
	private Iterable<FieldValueRetriever> extensions;

	/**
	 * Initialize extension mapping.
	 */
	@PostConstruct
	public void initializeExtensionMapping() {
		extensionMapping = PluginUtil.parseSupportedObjects(extensions, false);
	}

	@Override
	public String getLabel(String fieldId, String value) {
		return getLabel(fieldId, value, null);
	}

	@Override
	public String getLabel(String fieldName, String value, SearchRequest additionalParameters) {
		FieldValueRetriever fieldValueRetriever = extensionMapping.get(fieldName);
		if (fieldValueRetriever != null) {
			String label = fieldValueRetriever.getLabel(value, additionalParameters);
			if (StringUtils.isNotBlank(label)) {
				return label;
			}
		}
		return value;
	}

	@Override
	public RetrieveResponse getValues(String fieldId, String filter, Integer offset, Integer limit) {
		return getValues(fieldId, filter, null, offset, limit);
	}

	@Override
	public RetrieveResponse getValues(String fieldId, String filter, SearchRequest additionalParameters, Integer offset,
			Integer limit) {
		FieldValueRetriever fieldValueRetriever = extensionMapping.get(fieldId);
		if (fieldValueRetriever != null) {
			RetrieveResponse values = fieldValueRetriever.getValues(filter, additionalParameters, offset, limit);
			if (values != null) {
				return values;
			}
		}
		return new RetrieveResponse(0L, null);
	}

	@Override
	public Map<String, String> getLabels(String fieldId, String[] values) {
		return getLabels(fieldId, values, null);
	}

	@Override
	public Map<String, String> getLabels(String fieldId, String[] values, SearchRequest additionalParameters) {
		FieldValueRetriever fieldValueRetriever = extensionMapping.get(fieldId);
		if (fieldValueRetriever == null) {
			LOGGER.warn("No value retriver for field id=[{}]", fieldId);
			return CollectionUtils.emptyMap();
		}

		Map<String, String> labels = fieldValueRetriever.getLabels(values, additionalParameters);
		if (labels == null) {
			labels = CollectionUtils.emptyMap();
		}
		return labels;
	}

}
