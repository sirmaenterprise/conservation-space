package com.sirma.sep.model.management.converter;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.sep.model.management.AbstractModelNode;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

/**
 * Utilities for {@link com.sirma.sep.model.ModelNode} and related API classes conversions.
 *
 * @author Mihail Radkov
 */
public class ModelConverterUtilities {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ModelConverterUtilities() {
		// Prevent instantiation
	}

	/**
	 * Inserts {@link com.sirma.sep.model.management.ModelAttribute} into the provided model.
	 * <p>
	 * Based on the attribute name, it will resolve the attribute's type from the supplied model meta information.
	 * If it lacks meta information for the given name nothing will be inserted.
	 * <p>
	 * The provided attribute value will be converted to {@link String}
	 *
	 * @param metaInfos model meta information mapping containing attribute types and other related information
	 * @param model the model in which attribute will  be inserted
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public static void addStringAttribute(Map<String, ModelMetaInfo> metaInfos, AbstractModelNode model, String name, Serializable value) {
		if (value != null) {
			addAttribute(metaInfos, model, name, value.toString());
		}
	}

	/**
	 * Inserts {@link com.sirma.sep.model.management.ModelAttribute} into the provided model.
	 * <p>
	 * Based on the attribute name, it will resolve the attribute's type from the supplied model meta information.
	 * If it lacks meta information for the given name nothing will be inserted.
	 *
	 * @param metaInfos model meta information mapping containing attribute types and other related information
	 * @param model the model in which attribute will  be inserted
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public static void addAttribute(Map<String, ModelMetaInfo> metaInfos, AbstractModelNode model, String name, Serializable value) {
		if (value != null) {
			if (metaInfos.containsKey(name)) {
				ModelMetaInfo metaInfo = metaInfos.get(name);
				model.addAttribute(name, metaInfo.getType(), value);
			} else {
				LOGGER.warn("Missing meta information for {} model", name);
			}
		}
	}

	/**
	 * Assigns labels from a provided {@link LabelDefinition} to the supplied {@link AbstractModelNode} based on the {@link Displayable}
	 * label identifier.
	 * <p>
	 * If no label identifier exists or no label definition corresponds to it, then empty label map will be assigned to the model node.
	 *
	 * @param displayable used to obtain label identifier for label resolving
	 * @param modelNode target for assigning resolved labels
	 * @param labelProvider provides {@link LabelDefinition} for the provided label identifier
	 */
	public static void addLabels(Displayable displayable, AbstractModelNode modelNode, Function<String, LabelDefinition> labelProvider) {
		if (StringUtils.isNotBlank(displayable.getLabelId())) {
			LabelDefinition labelDefinition = labelProvider.apply(displayable.getLabelId());
			if (labelDefinition != null) {
				modelNode.setLabels(labelDefinition.getLabels());
				return;
			}
		}
		modelNode.setLabels(new HashMap<>(0));
	}
}
