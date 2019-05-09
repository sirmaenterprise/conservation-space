package com.sirma.sep.model.management.definition;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.addAttribute;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ID;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.TYPE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.VALUE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.PHASE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ASYNC;
import static com.sirma.sep.model.management.ModelActionExecution.CREATE_RELATION_DEFINITION;
import static com.sirma.sep.model.management.ModelActionExecution.CREATE_RELATION_MODELING;
import static com.sirma.sep.model.management.ModelActionExecution.EXECUTE_SCRIPT_MODELING;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.sep.model.management.ModelActionExecution;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

/**
 * Converts transition {@link PropertyDefinition}s to {@link ModelActionExecution}s.
 *
 * @author Boyan Tonchev.
 */
class DefinitionModelActionExecutionConverter {

	private DefinitionModelActionExecutionConverter() {
		// prevent instantiation
	}

	static Map<String, ModelActionExecution> constructModelActionExecutions(TransitionDefinition definitionAction,
			ModelsMetaInfo modelsMetaInfo) {
		return definitionAction.fieldsStream()
				.map(definitionActionExecution -> createModelActionExecution(definitionActionExecution, modelsMetaInfo))
				.collect(CollectionUtils.toIdentityMap(ModelActionExecution::getId, LinkedHashMap::new));
	}

	private static ModelActionExecution createModelActionExecution(PropertyDefinition actionField,
			ModelsMetaInfo modelsMetaInfo) {
		if (CREATE_RELATION_DEFINITION.equals(actionField.getControlDefinition().getIdentifier())) {
			return createRelationActionExecution(actionField, modelsMetaInfo);
		}
		return createScriptActionExecution(actionField, modelsMetaInfo);
	}

	private static ModelActionExecution createRelationActionExecution(PropertyDefinition actionField,
			ModelsMetaInfo modelsMetaInfo) {
		ModelActionExecution actionExecution = new ModelActionExecution();
		actionExecution.setId(actionField.getName());
		actionExecution.setModelsMetaInfo(modelsMetaInfo);
		addAttribute(actionExecution, ID, actionField.getName());
		addAttribute(actionExecution, TYPE, CREATE_RELATION_MODELING);
		addAttribute(actionExecution, VALUE, actionField.getDefaultValue());
		return actionExecution;
	}

	private static ModelActionExecution createScriptActionExecution(PropertyDefinition actionField,
			ModelsMetaInfo modelsMetaInfo) {
		ModelActionExecution actionExecution = new ModelActionExecution();
		actionExecution.setId(actionField.getName());
		actionExecution.setModelsMetaInfo(modelsMetaInfo);
		addAttribute(actionExecution, ID, actionField.getName());
		addAttribute(actionExecution, TYPE, EXECUTE_SCRIPT_MODELING);
		addAttribute(actionExecution, VALUE, actionField.getDefaultValue());
		processConfigParam(actionExecution, PHASE, actionField);
		processConfigParam(actionExecution, ASYNC, actionField);
		return actionExecution;
	}

	private static void processConfigParam(ModelActionExecution actionExecution, String paramName,
			PropertyDefinition actionField) {
		actionField.getControlDefinition()
				.getParam(paramName)
				.ifPresent(param -> addAttribute(actionExecution, paramName, param.getValue()));
	}
}
