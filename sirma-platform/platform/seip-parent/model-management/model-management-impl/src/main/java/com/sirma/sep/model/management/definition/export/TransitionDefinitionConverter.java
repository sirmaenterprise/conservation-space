package com.sirma.sep.model.management.definition.export;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.copyAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.toDisplayType;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.toStringOrNullIfBlank;
import static com.sirma.sep.model.management.definition.export.TransitionPropertyDefinitionConverter.updateDefinitionActionExecution;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.toInteger;
import static com.sirma.itt.seip.collections.CollectionUtils.toIdentityMap;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.LABEL_ID;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.TOOLTIP_ID;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.PURPOSE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ORDER;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.CONFIRMATION_ID;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ACTION_PATH;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.GROUP;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.DISPLAY_TYPE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelAction;
import com.sirma.sep.model.management.ModelDefinition;

/**
 * Converter for copying {@link ModelAction} attributes into {@link TransitionDefinition}.
 *
 * @author Boyan Tonchev.
 */
class TransitionDefinitionConverter {

	private TransitionDefinitionConverter() {
		// prevent instantiation
	}

	/**
	 * Fetches differences between {@link ModelAction}s (called model actions) from <code>modelDefinition</code>
	 * and {@link TransitionDefinition}s (called definition actions) from <code>definition</code>.
	 * Updates the definition with differences:
	 * 1.  Removes definition actions missed in model actions from the definition's actions;
	 * 2.  Updates definition actions with the new values from model actions.
	 * 3.  Creates and add the model actions missed in definition actions into the definition's actions list.
	 *
	 * @param modelDefinition the model which holds the model groups.
	 * @param definition      the definition into which definition groups have to be updated.
	 */
	static void copyToTransitions(ModelDefinition modelDefinition, GenericDefinition definition) {
		Map<String, TransitionDefinition> definitionTransitions = definition.getTransitions()
				.stream()
				.collect(toIdentityMap(TransitionDefinition::getIdentifier, LinkedHashMap::new));
		definition.getTransitions().clear();
		definition.getTransitions().addAll(updateTransitions(modelDefinition, definitionTransitions));
	}

	private static List<TransitionDefinition> updateTransitions(ModelDefinition modelDefinition,
			Map<String, TransitionDefinition> definitionTransitions) {
		return modelDefinition.getActions()
				.stream()
				.map(modelAction -> updateTransition(modelAction, definitionTransitions))
				.collect(Collectors.toList());
	}

	private static TransitionDefinition updateTransition(ModelAction modelAction,
			Map<String, TransitionDefinition> definitionTransitions) {
		TransitionDefinition definitionAction = getOrCreateTransition(modelAction, definitionTransitions);
		updateAttributes((TransitionDefinitionImpl) definitionAction, modelAction);
		updateDefinitionActionExecution((TransitionDefinitionImpl) definitionAction, modelAction);
		return definitionAction;
	}

	private static TransitionDefinition getOrCreateTransition(ModelAction modelAction,
			Map<String, TransitionDefinition> definitionTransitions) {
		TransitionDefinition definitionAction = definitionTransitions.get(modelAction.getId());
		if (definitionAction == null) {
			definitionAction = new TransitionDefinitionImpl();
			definitionAction.setIdentifier(modelAction.getId());
		}
		return definitionAction;
	}

	private static void updateAttributes(TransitionDefinitionImpl definitionAction, ModelAction modelAction) {
		copyAttribute(modelAction, LABEL_ID, definitionAction::setLabelId);
		copyAttribute(modelAction, TOOLTIP_ID, definitionAction::setTooltipId);
		copyAttribute(modelAction, PURPOSE, definitionAction::setPurpose);
		copyAttribute(modelAction, ORDER, toInteger(), definitionAction::setOrder);
		copyAttribute(modelAction, CONFIRMATION_ID, definitionAction::setConfirmationMessageId);
		copyAttribute(modelAction, ACTION_PATH, definitionAction::setActionPath);
		copyAttribute(modelAction, DISPLAY_TYPE, toDisplayType(), definitionAction::setDisplayType);
		copyAttribute(modelAction, GROUP, toStringOrNullIfBlank(), definitionAction::setGroup);
	}
}