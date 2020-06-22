package com.sirmaenterprise.sep.bpm.camunda.transitions.model;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.CREATE;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getCamundaFormData;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getCamundaInputOutput;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getCamundaProperties;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getExtensionElements;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getLoopCharacteristics;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getStartModelElementInstance;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.INSTANCE_SUB_TYPE;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.getActivityId;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.isActivity;
import static com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowParser.getSequenceFlowModel;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.getTypeAndSubtype;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnModelExecutionContext;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.el.ElValueProvider;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.el.UelExpressionCondition;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntityProxy;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntityFactory;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptCondition;
import org.camunda.bpm.engine.impl.util.ScriptUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil;
import com.sirmaenterprise.sep.bpm.camunda.configuration.SepCdiExpressionManager;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.BPMStateTransitionProvider;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.ConditionExpression;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.ConditionScript;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowEntry;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel;
import com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;

/**
 * The service is responsible to build a transition model activities related to BPM activity that are ready for
 * visualization by the UI.
 *
 * @author bbanchev
 */
@Singleton
public class TransitionModelService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String ERROR_DURING_EVALUATING_INPUT_PARAMETER = "Error during evaluating input parameter: {} ";
	private static final String DETAILED_ERROR = "Detailed error: ";

	@Inject
	private DefinitionService definitionService;
	@Inject
	private InstanceService instanceService;
	@Inject
	private SepCdiExpressionManager expressionManager;
	@Inject
	private CamundaBPMNService camundaBPMNService;
	@Inject
	private CommandExecutor commandExecutor;
	@Inject
	private BPMPropertiesConverter bpmPropertiesConverter;
	@Inject
	private CamundaBPMService camundaBPMService;

	/**
	 * Generate a list transition activities for current activity. The current activity is always include in the result.
	 *
	 * @param activity
	 *            the current activity
	 * @param operation
	 *            the chosen transition
	 * @return the list of current activity + all activities that are reached during this transition
	 * @throws CamundaIntegrationException
	 *             on any error during preparation of the transition activities
	 */
	public List<Instance> generateTransitionActivities(Instance activity, String operation)
			throws CamundaIntegrationException {
		List<Instance> activities = new LinkedList<>();
		if (isActivity(activity)) {
			activities.add(activity);
			buildTransitionModel(activity, operation, activities);
		} else if (activity.type().is("workflowinstancecontext")) {
			activities.add(activity);
			buildStartModel(activity, activities);
		}
		return activities;
	}

	private void buildTransitionModel(Instance activity, String operation, List<Instance> activities) {
		SequenceFlowEntry transition = BPMStateTransitionProvider.getTransition(activity, operation);
		ProcessInstance processInstance = camundaBPMNService.getProcessInstance(activity);
		BpmnModelInstance processModel = extractModelInstance(processInstance);
		processTransition(transition, activities, processInstance, processModel);
	}

	private void buildStartModel(Instance activity, List<Instance> activities) {
		BpmnModelInstance processModel = camundaBPMNService.getBpmnModelInstance(activity);
		StartEvent startEvent = getStartModelElementInstance(processModel);
		SequenceFlowModel sequenceFlowModel = getSequenceFlowModel(startEvent);
		sequenceFlowModel
				.getTransitions()
					.forEach(transition -> processTransition(transition, activities, null, processModel));
	}

	private void processTransition(SequenceFlowEntry transition, List<Instance> activities,
			ProcessInstance processInstance, BpmnModelInstance processModel) {
		Collection<Entry<String, String>> requiredTypes = Collections.emptySet();
		if (transition != null) {
			requiredTypes = transition.getCheckpoints().entrySet();
		}
		// Append the required types to the list of model to continue the transition
		for (Entry<String, String> requiredType : requiredTypes) {
			String[] typeAndSubtype = getTypeAndSubtype(requiredType.getValue());
			DefinitionModel typeModel = definitionService.find(typeAndSubtype[0]);
			if (typeModel == null) {
				throw new CamundaIntegrationRuntimeException("Failed to find a valid model for activity: "
						+ requiredType.getKey() + "/" + requiredType.getValue());
			}
			// create dummy instance to evaluate conditions and fill default values
			Instance createInstance = instanceService.createInstance(typeModel, null, new Operation(CREATE));
			if (typeAndSubtype.length > 1) {
				createInstance.add(INSTANCE_SUB_TYPE, typeAndSubtype[1]);
			}
			activities.add(injectActivityData(createInstance, requiredType.getKey(), processInstance, processModel));

		}
	}

	private Instance injectActivityData(Instance activity, String activityId, ProcessInstance processInstance,
			BpmnModelInstance processModel) {

		if (processModel == null) {
			LOGGER.warn("Process instance model not found for process: {} ", processInstance);
			return activity;
		}

		ModelElementInstance modelElementById = processModel.getModelElementById(activityId);

		if (processInstance != null) {
			injectVariables(activity, modelElementById, processInstance);
		} else {
			LOGGER.warn("Process instance not found for activity: {} ", getActivityId(activity));
		}
		injectDefaultVariables(activity, modelElementById);

		addConditionData(activity, activityId, modelElementById);

		return activity;

	}

	private static void injectDefaultVariables(Instance activity, ModelElementInstance modelElementById) {
		ExtensionElements extensionElements = getExtensionElements(modelElementById);
		CamundaFormData camundaFormData = getCamundaFormData(extensionElements);
		if (camundaFormData != null && !camundaFormData.getCamundaFormFields().isEmpty()) {
			camundaFormData.getCamundaFormFields().stream().filter(camundaFormField -> StringUtils.isNotBlank(camundaFormField.getCamundaDefaultValue()))
					.forEach(camundaFormField -> activity.add(camundaFormField.getCamundaId(), camundaFormField.getCamundaDefaultValue()));
		}
	}

	private void injectVariables(Instance activity, ModelElementInstance modelElementById,
			ProcessInstance processInstance) {
		ExtensionElements extensionElements = getExtensionElements(modelElementById);
		CamundaProperties camundaProperties = getCamundaProperties(extensionElements);
		if (camundaProperties != null && camundaProperties.getCamundaProperties() != null && !camundaProperties.getCamundaProperties().isEmpty()) {
			commandExecutor.execute((CommandContext command) -> {
				injectValueToActivityFromProperties(activity, processInstance, camundaProperties);
				return command;
			});
		}
		CamundaInputOutput inputOutput = getCamundaInputOutput(extensionElements);
		if (inputOutput == null || inputOutput.getCamundaInputParameters() == null
				|| inputOutput.getCamundaInputParameters().isEmpty()) {
			return;
		}
		commandExecutor.execute((CommandContext command) -> {
			injectValueToActivity(activity, processInstance, inputOutput);
			return command;
		});
	}

	private void injectValueToActivityFromProperties(Instance activity, ProcessInstance processInstance,
			CamundaProperties camundaProperties) {
		if (!(processInstance instanceof VariableScope)) {
			return;
		}
		for (CamundaProperty property : camundaProperties.getCamundaProperties()) {
			String camundaName = property.getCamundaName();
			String camundaValue = property.getCamundaValue();
			if (StringUtils.isBlank(camundaValue) || StringUtils.isBlank(camundaName) || !camundaName.startsWith("inject_")) {
 				continue;
			}
			try {
				Expression expression = expressionManager.createExpression(camundaValue);
				Object value = new ElValueProvider(expression).getValue((VariableScope) processInstance);
				if (value instanceof Serializable) {
					activity.add(camundaName.substring(7), (Serializable) value);
				}
			} catch (Exception e) {
				LOGGER.warn(ERROR_DURING_EVALUATING_INPUT_PARAMETER, camundaName);
				LOGGER.trace(DETAILED_ERROR, e);
			}
		}
	}

	/**
	 * Adds defined by the design data to instance that helps BA defines conditions.
	 */
	private static void addConditionData(Instance activity, String activityId, ModelElementInstance modelElementById) {
		// add a dummy ACTIVITY_ID to force condition evaluation for activities rules
		activity.add(ACTIVITY_ID, activityId);
		// if null or not MultiInstanceLoopCharacteristics is considered single
		boolean multiInstance;
		if (modelElementById instanceof Activity) {
			multiInstance = ((Activity) modelElementById)
					.getLoopCharacteristics() instanceof MultiInstanceLoopCharacteristics;
		} else {
			multiInstance = getLoopCharacteristics(
					getExtensionElements(modelElementById)) instanceof MultiInstanceLoopCharacteristics;
		}
		activity.add("multiInstanceActivity", multiInstance);
		boolean poolCandidates = false;
		// only user task has candidates
		if (modelElementById instanceof UserTask) {
			String candidateGroups = ((UserTask) modelElementById).getCamundaCandidateGroups();
			String candidateUsers = ((UserTask) modelElementById).getCamundaCandidateUsers();
			poolCandidates = StringUtils.isNotBlank(candidateGroups) || StringUtils.isNotBlank(candidateUsers);
		}
		activity.add("candidateAssigneesActivity", poolCandidates);
	}

	/**
	 * Create transition filter which to filter transitions by their conditions.
	 *
	 * @param activityTask
	 *            the current instance, which transitions are gethered
	 * @return predicate for filtering
	 */
	public Predicate<SequenceFlowEntry> createTransitionConditionFilter(Instance activityTask) {
		Optional<TaskFormData> taskForm = camundaBPMService.getTaskFormData(activityTask);
		if (!taskForm.isPresent()) {
			return entry -> true;
		}
		Map<String, Object> activityProperties = bpmPropertiesConverter
				.convertDataFromSEIPtoCamunda(activityTask.getOrCreateProperties(), taskForm.get().getFormFields());
		ProcessInstance processInstance = camundaBPMNService.getProcessInstance(activityTask);
		activityProperties.putAll(outjectActivityData(activityTask, processInstance));
		return sequenceFlowEntry -> {
			TimeTracker timer = TimeTracker.createAndStart();
			try {
				if (sequenceFlowEntry != null && sequenceFlowEntry.getName() != null
						&& StringUtils.isNotBlank(sequenceFlowEntry.getId())) {
					Condition condition = createCondition(sequenceFlowEntry);
					String id = sequenceFlowEntry.getId();
					activityProperties.put(ProcessConstants.OUTCOME, ActivityIdUtil.extractBusinessId(id.trim()));
					ActivityExecution clonedProcessInstance = cloneProcessInstance(processInstance, activityProperties);
					if (condition != null && clonedProcessInstance != null) {
						LOGGER.debug("Evaluating condition expression for transitionId: \"{}\" for activity: \"{}\"", id,
								activityTask.getId().toString());
						return executeEvaluation(clonedProcessInstance, condition);
					}
				}
				return true;
			} finally {
				LOGGER.trace("Time evaluating condition expression: {}s", timer.stopInSeconds());
			}
		};
	}

	/**
	 * Creates condition based on sequenceFlowEntry.
	 *
	 * @param sequenceFlowEntry
	 *            the flow entry to use for creating condition
	 * @return condition if flow entry have condition with expression else return null
	 */
	private Condition createCondition(SequenceFlowEntry sequenceFlowEntry) {
		ConditionExpression conditionExpression = sequenceFlowEntry.getCondition();
		// verify not null
		if (conditionExpression == null) {
			return null;
		}
		String expression = conditionExpression.getValue();
		String id = sequenceFlowEntry.getId();
		// verify id and expression
		if (StringUtils.isBlank(id) || StringUtils.isBlank(expression)) {
			return null;
		}
		if (conditionExpression instanceof ConditionScript) {
			String language = ((ConditionScript) conditionExpression).getLanguage();
			String source = ((ConditionScript) conditionExpression).getSource();
			ExecutableScript script = ScriptUtil.getScript(language, source, expression, expressionManager);
			LOGGER.debug("Created script condition with expression: \"{}\" with transionId: \"{}\"", expression, id);
			return new ScriptCondition(script);
		}
		LOGGER.debug("Created UEL condition with expression: \"{}\" with transionId: \"{}\"", expression, id);
		return new UelExpressionCondition(expressionManager.createExpression(expression));
	}

	/**
	 * Execute the evaluation of condition expression.
	 *
	 * @param condition
	 *            camunda object used to evaluate the condition expression
	 * @param activityExecution
	 *            task used only for evaluation
	 *
	 * @return true only if expression evaluation is successful and match
	 */
	private boolean executeEvaluation(ActivityExecution activityExecution, Condition condition) {
		return commandExecutor.execute((CommandContext command) -> evaluateCondition(activityExecution, condition));

	}

	private ActivityExecution cloneProcessInstance(ProcessInstance processInstance,
			Map<String, Object> activityProperties) {
		if (processInstance instanceof ExecutionEntity) {
			ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
			Collection<VariableInstanceEntity> variables = commandExecutor.execute((CommandContext command) -> buildCamundaVariables(executionEntity, activityProperties));
			ExecutionEntityProxy newExecution =  new ExecutionEntityProxy();
			newExecution.addVariables(variables);
			return newExecution;
		}
		LOGGER.warn("Fail to clone ProcessInstance class: {} with id: {} please check it out!", processInstance.getClass().getSimpleName(), processInstance.getProcessInstanceId());
		return null;
	}

	/**
	 * Build collection of {@link VariableInstanceEntity} which to be used in {@link ExecutionEntityProxy} for condition evaluation.
	 *
	 * @param activityProperties
	 * 		map with new activity properties
	 * @return collection of {@link VariableInstanceEntity}
	 */
	private static Collection<VariableInstanceEntity> buildCamundaVariables(ExecutionEntity executionEntity, Map<String, Object> activityProperties) {
		Collection<VariableInstanceEntity> processInstanceVariables = executionEntity.getVariablesInternal();
		List<VariableInstanceEntity> variables = new ArrayList<>(activityProperties.size() + processInstanceVariables.size());
		variables.addAll(processInstanceVariables);
		activityProperties.forEach((variableName, value) -> variables.add(
				VariableInstanceEntityFactory.INSTANCE.build(variableName, Variables.untypedValue(value), false)));
		return variables;
	}

	/**
	 * Evaluate the condition expression.
	 *
	 * @param condition
	 *            camunda object used to evaluate the condition expression
	 * @param activityExecution
	 *            task used only for evaluation
	 * @return true only if expression evaluation is successful and match
	 */
	private static Boolean evaluateCondition(ActivityExecution activityExecution, Condition condition) {
		return condition.tryEvaluate(activityExecution, null);
	}

	private void injectValueToActivity(Instance activity, ProcessInstance processInstance,
			CamundaInputOutput inputOutput) {
		if (!(processInstance instanceof VariableScope)) {
			return;
		}
		for (CamundaInputParameter nextField : inputOutput.getCamundaInputParameters()) {
			String paramName = nextField.getCamundaName();
			try {
				Expression expression = expressionManager.createExpression(nextField.getTextContent());
				Object value = new ElValueProvider(expression).getValue((VariableScope) processInstance);
				if (value instanceof Serializable) {
					activity.add(paramName, (Serializable) value);
				}
			} catch (Exception e) {
				LOGGER.warn(ERROR_DURING_EVALUATING_INPUT_PARAMETER, paramName);
				LOGGER.trace(DETAILED_ERROR, e);
			}
		}
	}

	private Map<String, Object> outjectActivityData(Instance activity, ProcessInstance processInstance) {
		BpmnModelInstance processModel = extractModelInstance(processInstance);
		if (processModel == null) {
			return Collections.emptyMap();
		}
		ModelElementInstance modelElementById = processModel.getModelElementById(activity.getIdentifier());
		ExtensionElements extensionElements = getExtensionElements(modelElementById);
		CamundaInputOutput inputOutput = CamundaModelElementInstanceUtil.getCamundaInputOutput(extensionElements);
		if (inputOutput == null || inputOutput.getCamundaOutputParameters() == null
				|| inputOutput.getCamundaOutputParameters().isEmpty()) {
			return Collections.emptyMap();
		}
		return commandExecutor.execute((CommandContext command) -> outjectProperties(processInstance, inputOutput));
	}

	private Map<String, Object> outjectProperties(ProcessInstance processInstance, CamundaInputOutput inputOutput) {
		Map<String, Object> outjectedProperties = new HashMap<>(inputOutput.getCamundaOutputParameters().size());
		if (!(processInstance instanceof VariableScope)) {
			return outjectedProperties;
		}
		for (CamundaOutputParameter nextField : inputOutput.getCamundaOutputParameters()) {
			String paramName = nextField.getCamundaName();
			try {
				Expression expression = expressionManager.createExpression(nextField.getTextContent());
				Object value = new ElValueProvider(expression).getValue((VariableScope) processInstance);
				outjectedProperties.put(paramName, value);
			} catch (Exception e) {
				LOGGER.warn(ERROR_DURING_EVALUATING_INPUT_PARAMETER, paramName);
				LOGGER.trace(DETAILED_ERROR, e);
			}
		}
		return outjectedProperties;
	}

	private BpmnModelInstance extractModelInstance(ProcessInstance instanceExecution) {
		if (instanceExecution instanceof BpmnModelExecutionContext) {
			return commandExecutor.execute(
					(CommandContext command) -> ((BpmnModelExecutionContext) instanceExecution).getBpmnModelInstance());
		}
		return null;
	}
}
