package com.sirma.sep.model.management.definition.export;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.copyAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.toInteger;
import static com.sirma.itt.seip.collections.CollectionUtils.toIdentityMap;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.LABEL_ID;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.PARENT;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ORDER;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.TYPE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.definition.model.TransitionGroupDefinitionImpl;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelActionGroup;
import com.sirma.sep.model.management.ModelDefinition;

/**
 * Converter for copying {@link ModelActionGroup} attributes into {@link TransitionGroupDefinition}.
 *
 * @author Boyan Tonchev.
 */
class TransitionGroupDefinitionConverter {

	private TransitionGroupDefinitionConverter() {
		// prevent instantiation
	}

	/**
	 * Fetches differences between {@link ModelActionGroup}s (called model groups) from <code>modelDefinition</code>
	 * and {@link TransitionGroupDefinition}s (called definition groups) from <code>definition</code>.
	 * Updates the definition with differences:
	 * 1.  Removes definition groups missed in model groups from the definition's groups;
	 * 2.  Updates definition groups with the new values from model groups.
	 * 3.  Creates and add the model groups missed in definition groups into the definition's groups list.
	 *
	 * @param modelDefinition the model which holds the model groups.
	 * @param definition      the definition into which definition groups have to be updated.
	 */
	static void copyToTransitionGroups(ModelDefinition modelDefinition, GenericDefinition definition) {
		Map<String, TransitionGroupDefinition> definitionTransitionGroups = definition.getTransitionGroups()
				.stream()
				.collect(toIdentityMap(TransitionGroupDefinition::getIdentifier, LinkedHashMap::new));
		definition.getTransitionGroups().clear();
		definition.getTransitionGroups().addAll(updateTransitionGroups(modelDefinition, definitionTransitionGroups));
	}

	private static List<TransitionGroupDefinition> updateTransitionGroups(ModelDefinition modelDefinition,
			Map<String, TransitionGroupDefinition> definitionTransitionGroups) {
		return modelDefinition.getActionGroups().stream().map(actionGroup -> {
			TransitionGroupDefinition transitionGroupDefinition = getOrCreate(actionGroup, definitionTransitionGroups);
			updateBaseAttributes((TransitionGroupDefinitionImpl) transitionGroupDefinition, actionGroup);
			return transitionGroupDefinition;
		}).collect(Collectors.toList());
	}

	private static TransitionGroupDefinition getOrCreate(ModelActionGroup modelActionGroup,
			Map<String, TransitionGroupDefinition> definitionTransitionGroups) {
		TransitionGroupDefinition transitionGroupDefinition = definitionTransitionGroups.get(modelActionGroup.getId());
		if (transitionGroupDefinition == null) {
			transitionGroupDefinition = new TransitionGroupDefinitionImpl();
			transitionGroupDefinition.setIdentifier(modelActionGroup.getId());
		}
		return transitionGroupDefinition;
	}

	private static void updateBaseAttributes(TransitionGroupDefinitionImpl definitionGroup,
			ModelActionGroup actionGroup) {
		copyAttribute(actionGroup, LABEL_ID, definitionGroup::setLabelId);
		copyAttribute(actionGroup, PARENT, definitionGroup::setParent);
		copyAttribute(actionGroup, ORDER, toInteger(), definitionGroup::setOrder);
		copyAttribute(actionGroup, TYPE, definitionGroup::setType);
	}
}
