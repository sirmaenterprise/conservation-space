package com.sirma.sep.model.management.definition;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.definition.model.LabelImpl;

/**
 * Provider for labels from {@link LabelService}.
 *
 * @author Mihail Radkov
 * @since 24/08/2018
 */
public class LabelProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private LabelService labelService;

	/**
	 * Retrieves labels map for the given label identifier.
	 *
	 * @param labelId the identifier to search labels for
	 * @return mutable map with translations or empty one if no labels exists for the given identifier; never <code>null</code>
	 */
	public Map<String, String> getLabels(String labelId) {
		LabelDefinition labelDefinition = labelService.getLabel(labelId);
		return toMap(labelDefinition);
	}

	/**
	 * Retrieves all available labels for the given definition identifiers.
	 *
	 * @param identifier identifier of a definition for which to search labels for
	 * @return mapping of label id and labels
	 */
	public Map<String, Map<String, String>> getDefinitionLabels(String identifier) {
		return labelService.getLabelsDefinedIn(identifier)
				.stream()
				.collect(Collectors.toMap(LabelDefinition::getIdentifier, LabelProvider::toMap));
	}

	private static Map<String, String> toMap(LabelDefinition labelDefinition) {
		if (labelDefinition != null && labelDefinition.getLabels() != null) {
			Map<String, String> labels = new HashMap<>(labelDefinition.getLabels().size());
			labels.putAll(labelDefinition.getLabels());
			return labels;
		}
		return new HashMap<>(0);
	}

	/**
	 * Returns {@link Set} of definition identifiers in which the provided label is defined.
	 *
	 * @param labelId the label to check for
	 * @return the definition identifiers or empty set if the label is not defined anywhere
	 */
	public Set<String> definedIn(String labelId) {
		LabelDefinition labelDefinition = labelService.getLabel(labelId);
		if (labelDefinition != null && CollectionUtils.isNotEmpty(labelDefinition.getDefinedIn())) {
			return labelDefinition.getDefinedIn();
		}
		return CollectionUtils.emptySet();
	}

	/**
	 * Saves the provided labels by converting them to {@link LabelDefinition} and passing it to {@link LabelService}.
	 * <p>
	 * The mapping will be merged with any existing labels and then normalized by removing null values.
	 *
	 * @param labelId the labels identifier
	 * @param definedIn definition identifier where the labels are defined
	 * @param labels the labels mapping
	 */
	public void saveLabels(String labelId, String definedIn, Map<String, String> labels) {
		LabelDefinition labelDefinition = labelService.getLabel(labelId);
		if (labelDefinition == null) {
			labelDefinition = new LabelImpl();
			labelDefinition.setIdentifier(labelId);
		}

		if (labelDefinition instanceof LabelImpl) {
			mergeLabels((LabelImpl) labelDefinition, labels);
			((LabelImpl) labelDefinition).addDefinedIn(definedIn);
			labelService.saveLabel(labelDefinition);
		} else {
			LOGGER.warn("Unexpected LabelDefinition implementation: {}", labelDefinition.getClass().getSimpleName());
		}
	}

	private static void mergeLabels(LabelImpl labelDefinition, Map<String, String> labels) {
		if (labelDefinition.getLabels() == null) {
			labelDefinition.setLabels(new HashMap(labels));
		} else {
			Map<String, String> merge = new HashMap(labelDefinition.getLabels());
			merge.putAll(labels);
			labelDefinition.setLabels(merge);
		}
		labelDefinition.getLabels().values().removeIf(Objects::isNull);
	}

	/**
	 * If a {@link LabelDefinition} exists for the provided label identifier it will remove all associated labels and finally saves it.
	 * Additionally also cleans all defined in values.
	 *
	 * @param labelId the label identifier for which labels will be removed
	 */
	public void removeLabels(String labelId) {
		LabelDefinition labelDefinition = labelService.getLabel(labelId);
		if (labelDefinition instanceof LabelImpl) {
			((LabelImpl) labelDefinition).setLabels(Collections.emptyMap());
			((LabelImpl) labelDefinition).clearDefinedIn();
			labelService.saveLabel(labelDefinition);
		} else if (labelDefinition == null) {
			LOGGER.warn("Missing label definition with id={}", labelId);
		}
	}

}
