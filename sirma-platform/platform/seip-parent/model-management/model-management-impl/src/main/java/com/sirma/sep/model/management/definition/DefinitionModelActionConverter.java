package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addLabels;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addStringAttribute;
import static com.sirma.sep.model.management.definition.DefinitionModelActionExecutionConverter.constructModelActionExecutions;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ID;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.DISPLAY_TYPE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.PURPOSE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ORDER;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ACTION_PATH;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.GROUP;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.TOOLTIP;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.TOOLTIP_ID;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.CONFIRMATION;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.CONFIRMATION_ID;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.LABEL_ID;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.sep.model.management.ModelAction;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Converts {@link TransitionDefinition} to {@link ModelAction}.
 *
 * @author Boyan Tonchev.
 */
public class DefinitionModelActionConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private LabelProvider labelProvider;

	/**
	 * Converts the {@link TransitionDefinition}s from the provided {@link GenericDefinition} into {@link ModelAction}s.
	 * <p>
	 *
	 * @param definition the definition which will be processed
	 * @param modelsMetaInfo meta information mapping
	 * @return map of converted {@link ModelAction}. It may be empty if the provided definition has no {@link TransitionDefinition}.
	 */
	public Map<String, ModelAction> constructModelActions(GenericDefinition definition, ModelsMetaInfo modelsMetaInfo) {
		return definition.getTransitions()
				.stream()
				.map(action -> buildModelAction(action, modelsMetaInfo))
				.collect(toActionsMap(definition));
	}

	private ModelAction buildModelAction(TransitionDefinition definitionAction, ModelsMetaInfo modelsMetaInfo) {
		ModelAction modelAction = new ModelAction();
		modelAction.setModelsMetaInfo(modelsMetaInfo);
		modelAction.setId(definitionAction.getIdentifier());

		addLabelAttribute(definitionAction, modelAction);
		addTooltipAttribute(definitionAction, modelAction);

		addAttribute(modelAction, ID, definitionAction.getIdentifier());
		addStringAttribute(modelAction, DISPLAY_TYPE, definitionAction.getDisplayType());
		addAttribute(modelAction, PURPOSE, definitionAction.getPurpose());
		addAttribute(modelAction, ORDER, definitionAction.getOrder());
		addAttribute(modelAction, ACTION_PATH, definitionAction.getActionPath());
		addAttribute(modelAction, GROUP, definitionAction.getGroup());
		addConfirmationAttribute(definitionAction, modelAction);
		modelAction.setActionExecutions(constructModelActionExecutions(definitionAction, modelsMetaInfo));
		modelAction.setAsDeployed();
		return modelAction;
	}

	private void addLabelAttribute(TransitionDefinition definitionAction, ModelAction modelAction) {
		addAttribute(modelAction, LABEL_ID, definitionAction.getLabelId());
		addLabels(definitionAction, modelAction, labelProvider::getLabels);
	}

	private void addTooltipAttribute(TransitionDefinition definitionAction, ModelAction modelAction) {
		String tooltipId = definitionAction.getTooltipId();
		Map<String, String> tooltips = tooltipId != null ? labelProvider.getLabels(tooltipId) : null;
		addAttribute(modelAction, TOOLTIP, tooltips);
		addAttribute(modelAction, TOOLTIP_ID, tooltipId);
	}

	private void addConfirmationAttribute(TransitionDefinition definitionAction, ModelAction modelAction) {
		String confirmationMessageId = definitionAction.getConfirmationMessageId();
		Map<String, String> confirmationMessage =
				confirmationMessageId != null ? labelProvider.getLabels(confirmationMessageId) : null;
		addAttribute(modelAction, CONFIRMATION, confirmationMessage);
		addAttribute(modelAction, CONFIRMATION_ID, confirmationMessageId);
	}

	private static Collector<ModelAction, ?, LinkedHashMap<String, ModelAction>> toActionsMap(GenericDefinition definition) {
		return Collectors.toMap(ModelAction::getId, Function.identity(), duplicateActionsMerger(definition), LinkedHashMap::new);
	}

	private static BinaryOperator<ModelAction> duplicateActionsMerger(GenericDefinition definition) {
		return (a1, a2) -> {
			LOGGER.warn("Duplicated action {} during mapping of {}", a1.getId(), definition.getIdentifier());
			return a2;
		};
	}
}
