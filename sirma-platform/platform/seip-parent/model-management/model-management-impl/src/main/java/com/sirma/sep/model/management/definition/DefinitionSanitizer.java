package com.sirma.sep.model.management.definition;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.jaxb.ComplexFieldsDefinition;
import com.sirma.itt.seip.definition.jaxb.Definition;
import com.sirma.itt.seip.definition.jaxb.RegionsDefinition;
import com.sirma.itt.seip.definition.jaxb.TransitionsDefinition;

/**
 * Cleans up {@link Definition} from empty elements.
 *
 * @author Mihail Radkov
 */
class DefinitionSanitizer {

	private DefinitionSanitizer() {
		// Utility class
	}

	static void sanitizeDefinition(Definition definition) {
		// If it is not abstract (false), no need to specify it in the xml
		if (!definition.isIsAbstract()) {
			definition.setIsAbstract(null);
		}

		// Config
		if (CollectionUtils.isEmpty(definition.getConfiguration().getFields().getField())) {
			definition.setConfiguration(null);
		} else {
			sanitizeFields(definition.getConfiguration().getFields());
		}

		// Fields
		if (CollectionUtils.isEmpty(definition.getFields().getField())) {
			definition.setFields(null);
		} else {
			sanitizeFields(definition.getFields());
		}

		// Regions
		if (CollectionUtils.isEmpty(definition.getRegions().getRegion())) {
			definition.setRegions(null);
		} else {
			sanitizeRegions(definition.getRegions());
		}

		// Transitions
		if (CollectionUtils.isEmpty(definition.getTransitions().getTransition())) {
			definition.setTransitions(null);
		} else {
			sanitizeTransitions(definition.getTransitions());
		}

		// State transitions
		if (CollectionUtils.isEmpty(definition.getStateTransitions().getStateTransition())) {
			definition.setStateTransitions(null);
		}

		// Allowed children
		if (CollectionUtils.isEmpty(definition.getAllowedChildren().getChild())) {
			definition.setAllowedChildren(null);
		}

		// Labels
		if (definition.getLabels() != null && CollectionUtils.isEmpty(definition.getLabels().getLabel())) {
			definition.setLabels(null);
		}
	}

	private static void sanitizeRegions(RegionsDefinition regionsDefinition) {
		regionsDefinition.getRegion().forEach(region -> {
			if (CollectionUtils.isEmpty(region.getFields().getField())) {
				region.setFields(null);
			} else {
				sanitizeFields(region.getFields());
			}
		});
	}

	private static void sanitizeFields(ComplexFieldsDefinition fields) {
		fields.getField().forEach(field -> {
			if ("read_only".equals(field.getDisplayType())) {
				field.setDisplayType("readonly");
			}
			if (field.getControl() != null && CollectionUtils.isEmpty(field.getControl().getFields().getField())) {
				field.getControl().setFields(null);
			}
		});
	}

	private static void sanitizeTransitions(TransitionsDefinition transitionsDefinition) {
		transitionsDefinition.getTransition().forEach(transition -> {
			transition.setDefaultTransition(null);

			if (CollectionUtils.isEmpty(transition.getFields().getField())) {
				transition.setFields(null);
			} else {
				sanitizeFields(transition.getFields());
			}
		});
	}
}
