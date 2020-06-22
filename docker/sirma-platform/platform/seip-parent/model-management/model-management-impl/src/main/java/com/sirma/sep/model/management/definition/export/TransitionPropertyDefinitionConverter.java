package com.sirma.sep.model.management.definition.export;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.copyAttribute;
import static com.sirma.itt.seip.collections.CollectionUtils.toIdentityMap;
import static com.sirma.sep.model.management.ModelActionExecution.CREATE_RELATION_MODELING;
import static com.sirma.sep.model.management.ModelActionExecution.CREATE_RELATION_DEFINITION;
import static com.sirma.sep.model.management.ModelActionExecution.EXECUTE_SCRIPT_MODELING;
import static com.sirma.sep.model.management.ModelActionExecution.EXECUTE_SCRIPT_DEFINITION;
import static com.sirma.sep.model.management.ModelAttributeType.TYPE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.VALUE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.PERSISTENT;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.PHASE;
import static com.sirma.sep.model.management.definition.DefinitionModelAttributes.ASYNC;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.model.ControlDefinitionImpl;
import com.sirma.itt.seip.definition.model.ControlParamImpl;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.TransitionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.sep.model.management.ModelAction;
import com.sirma.sep.model.management.ModelActionExecution;

/**
 * Converter for copying {@link ModelActionExecution} attributes into {@link PropertyDefinition}.
 *
 * @author Boyan Tonchev.
 */
class TransitionPropertyDefinitionConverter {

	private TransitionPropertyDefinitionConverter() {
		// prevent instantiation
	}

	/**
	 * Updates <code>definitionAction</code> executions with the new values from <code>modelAction</code> executions.
	 *
	 * Fetches differences between {@link ModelActionExecution}s (called model action execution) from <code>modelAction</code>
	 * and {@link PropertyDefinition}s (called definition action executions) from <code>definitionAction</code>.
	 * Updates the definition action executions with differences:
	 * 1.  Removes definition action executions missed in model action executions from the definitionAction action executions;
	 * 2.  Updates definition action executions with the new values from model action executions.
	 * 3.  Creates and add the model action executions missed in definition action executions into the definitionAction action executions list.
	 *
	 * @param definitionAction the definition action which have to be updated.
	 * @param modelAction      the model action which holds the new actual data of a action.
	 */
	static void updateDefinitionActionExecution(TransitionDefinitionImpl definitionAction, ModelAction modelAction) {
		Map<String, PropertyDefinition> definitionActionExecutions = definitionAction.fieldsStream()
				.collect(toIdentityMap(PropertyDefinition::getName, LinkedHashMap::new));
		definitionAction.getFields().clear();
		definitionAction.getFields().addAll(updateActionExecutions(modelAction, definitionActionExecutions));
	}

	private static List<PropertyDefinition> updateActionExecutions(ModelAction modelAction,
			Map<String, PropertyDefinition> definitionActionExecutions) {
		return modelAction.getActionExecutions().stream().map(modelActionExecution -> {
			PropertyDefinition definitionActionExecution = getOrCreateActionExecution(modelActionExecution,
																					  definitionActionExecutions);
			updateDefinitionActionExecution((PropertyDefinitionProxy) definitionActionExecution, modelActionExecution);
			return definitionActionExecution;
		}).collect(Collectors.toList());
	}

	private static PropertyDefinition getOrCreateActionExecution(ModelActionExecution modelActionExecution,
			Map<String, PropertyDefinition> definitionActionExecutions) {
		PropertyDefinition definitionActionExecution = definitionActionExecutions.get(modelActionExecution.getId());
		if (definitionActionExecution == null) {
			definitionActionExecution = new PropertyDefinitionProxy();
			WritablePropertyDefinition fieldDefinition = new FieldDefinitionImpl();
			((PropertyDefinitionProxy) definitionActionExecution).setTarget(fieldDefinition);
			definitionActionExecution.setIdentifier(modelActionExecution.getId());
		}
		return definitionActionExecution;
	}

	private static void updateDefinitionActionExecution(PropertyDefinitionProxy definitionActionExecution,
			ModelActionExecution modelActionExecution) {
		copyAttribute(modelActionExecution, VALUE, Object::toString, definitionActionExecution::setValue);
		modelActionExecution.getAttribute(TYPE).ifPresent(actionExecutionType -> {
			ControlDefinition controlDefinition = getOrCreateControlDefinition(definitionActionExecution);
			Map<String, ControlParam> controlParameters = controlDefinition.paramsStream()
					.collect(toIdentityMap(ControlParam::getIdentifier, LinkedHashMap::new));
			controlDefinition.getControlParams().clear();
			if (CREATE_RELATION_MODELING.equals(actionExecutionType.getValue())) {
				controlDefinition.setIdentifier(CREATE_RELATION_DEFINITION);
			} else if (EXECUTE_SCRIPT_MODELING.equals(actionExecutionType.getValue())) {
				controlDefinition.setIdentifier(EXECUTE_SCRIPT_DEFINITION);
				updateExecutionScriptActionExecution(controlDefinition, modelActionExecution, controlParameters);
			}
		});
	}

	private static ControlDefinition getOrCreateControlDefinition(PropertyDefinitionProxy definitionActionExecution) {
		ControlDefinition controlDefinition = definitionActionExecution.getControlDefinition();
		if (controlDefinition == null) {
			controlDefinition = new ControlDefinitionImpl();
			definitionActionExecution.setControlDefinition(controlDefinition);
		}
		return controlDefinition;
	}

	private static void updateExecutionScriptActionExecution(ControlDefinition controlDefinition,
			ModelActionExecution modelActionExecution, Map<String, ControlParam> controlParameters) {
		ControlParam controlParam = controlParameters.get(PERSISTENT);
		if (controlParam != null) {
			controlDefinition.getControlParams().add(controlParam);
		}
		updateControlParam(controlDefinition, PHASE, modelActionExecution, controlParameters);
		updateControlParam(controlDefinition, ASYNC, modelActionExecution, controlParameters);
	}

	private static void updateControlParam(ControlDefinition controlDefinition, String controlParamName,
			ModelActionExecution modelActionExecution, Map<String, ControlParam> controlParameters) {
		ControlParamImpl controlParam = getOrCreateControlParam(controlParamName, controlParameters);
		controlDefinition.getControlParams().add(controlParam);
		copyAttribute(modelActionExecution, controlParamName, Object::toString, controlParam::setValue);
	}

	private static ControlParamImpl getOrCreateControlParam(String controlParamName,
			Map<String, ControlParam> controlParameters) {
		ControlParam controlParam = controlParameters.get(controlParamName);
		if (controlParam == null) {
			controlParam = new ControlParamImpl();
			((ControlParamImpl) controlParam).setName(controlParamName);
			controlParam.setIdentifier(controlParamName);
		}
		return (ControlParamImpl) controlParam;
	}
}
