package com.sirma.itt.emf.label.retrieve;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;

/**
 * Implementation of {@link FieldValueRetrieverService}
 */
@ApplicationScoped
public class FieldValueRetrieverServiceImpl implements FieldValueRetrieverService {

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
	public String getLabel(String fieldName, String... value) {
		FieldValueRetriever fieldValueRetriever = extensionMapping.get(fieldName);
		String label = fieldValueRetriever.getLabel(value);
		if (StringUtils.isNotNullOrEmpty(label)) {
			return label;
		}
		if (ArrayUtils.isNotEmpty(value)) {
			return value[0];
		} else {
			return null;
		}
	}

	@Override
	public RetrieveResponse getValues(String fieldId, String filter, Integer offset, Integer limit) {
		FieldValueRetriever fieldValueRetriever = extensionMapping.get(fieldId);
		RetrieveResponse values = fieldValueRetriever.getValues(filter, offset, limit);
		if (values != null) {
			return values;
		}
		return new RetrieveResponse(0L, null);
	}

}
