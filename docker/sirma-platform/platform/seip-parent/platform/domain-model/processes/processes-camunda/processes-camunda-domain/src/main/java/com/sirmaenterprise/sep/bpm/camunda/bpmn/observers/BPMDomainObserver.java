package com.sirmaenterprise.sep.bpm.camunda.bpmn.observers;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.COMPLETE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.STOP;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getCamundaFormData;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getExtensionElements;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.CamundaModelElementInstanceUtil.getModelElementId;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.COMPLETED_ON;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.INSTANCE_SUB_TYPE;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.TRANSITIONS;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.TRANSITIONS_NONPERSISTED;
import static com.sirmaenterprise.sep.bpm.camunda.properties.BPMTaskProperties.TASK_ID;
import static com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter.mergeProperties;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.extractBusinessId;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.getTypeAndSubtype;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.camunda.bpm.engine.cdi.BusinessProcessEventType.CREATE_TASK;
import static org.camunda.bpm.engine.cdi.BusinessProcessEventType.DELETE_TASK;
import static org.camunda.bpm.engine.cdi.BusinessProcessEventType.END_ACTIVITY;
import static org.camunda.bpm.engine.cdi.BusinessProcessEventType.START_ACTIVITY;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent.ActivityType;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.camunda.service.SecureProcessEngine;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowParser;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil;

/**
 * Observer class that listen for {@link MultiEngineBusinessProcessEvent} and executing basic operations based on the
 * event data.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class BPMDomainObserver {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DomainInstanceService domainInstanceService;
	@Inject
	private InstanceTypeResolver instanceResolver;
	@Inject
	private BPMPropertiesConverter propertiesConverter;
	@Inject
	private SchedulerService schedulerService;
	@Inject
	private BPMEventScheduler bpmEventScheduler;
	@Inject
	private Actions actions;

	/**
	 * Executes a business logic operation on the process engine and in SEIP based on the event received. Event should
	 * be fired in initialized security context.
	 *
	 * @param event
	 *            is a {@link MultiEngineBusinessProcessEvent} holding all needed information
	 */
	@SecureProcessEngine
	public void handleBPMNEvent(@Observes MultiEngineBusinessProcessEvent event) {
		LOGGER.debug("BPM event received: {}", event);
		if (event.getActivityType() == ActivityType.TASK) {
			processTaskEvent(event);
		} else if (event.getActivityType() == ActivityType.EVENT) {
			processGeneralEvent(event);
		}
	}

	private void processGeneralEvent(MultiEngineBusinessProcessEvent event) {
		try {
			if (event.getType() == END_ACTIVITY) {
				processEndEvent(event);
			} else if (event.getType() == START_ACTIVITY) {
				processStartEvent(event);
			}
		} catch (Exception e) {
			throw new CamundaIntegrationRuntimeException(
					"Can not create/modify instance based on process engine event: " + event, e);
		}
	}

	private void processStartEvent(MultiEngineBusinessProcessEvent event) {
		if (event.getActivityBehavior() instanceof IntermediateCatchEventActivityBehavior) {
			SchedulerContext context = BPMEventScheduler.createExecutorContext(event);
			Collection<Instance> contextInstances = bpmEventScheduler.findContextInstances(context, event);
			if (contextInstances.isEmpty()) {
				LOGGER.warn("Can not schedule event: {}, because of not found context instance.", event);
				return;
			}
			for (Instance instance : contextInstances) {
				EmfEvent emfEvent = BPMEventScheduler.createConfigurationEvent(context, instance);
				SchedulerConfiguration configuration = schedulerService.buildConfiguration(emfEvent, instance);
				schedulerService.schedule(BPMEventScheduler.BEAN_ID, configuration, context);
			}
		}
	}

	private void processEndEvent(MultiEngineBusinessProcessEvent event) {
		if (event.getActivityBehavior() instanceof NoneEndEventActivityBehavior) {// INUC03-UI2-S07 - end of end event
			String businessKey = event.getProcessBusinessKey();
			Instance workflow = BPMInstanceUtil.resolveInstance(businessKey, instanceResolver);
			// CMF-22509 transfer all properties from camunda to workflow when complete
			VariableMap variablesLocalTyped = event.getExecution().getVariablesLocalTyped();
			VariableMap globalVariablesTyped = event.getExecution().getVariablesTyped();
			globalVariablesTyped.keySet().forEach(
					k -> variablesLocalTyped.putValueTyped(k, globalVariablesTyped.getValueTyped(k)));
			Map<String, Serializable> workflowProperties = propertiesConverter
					.convertDataFromCamundaToSEIP(variablesLocalTyped, workflow);
			mergeProperties(workflow.getOrCreateProperties(), workflowProperties);
			// add dynamic state transition
			SequenceFlowModel buildManualTransition = new SequenceFlowModel(COMPLETE, null);
			workflow.add(TRANSITIONS_NONPERSISTED, buildManualTransition.serialize());
			domainInstanceService.save(InstanceSaveContext.create(workflow, new Operation(COMPLETE)));
		}
	}

	private void processTaskEvent(MultiEngineBusinessProcessEvent event) {
		try {
			BusinessProcessEventType type = event.getType();
			if (type == CREATE_TASK) {
				processTaskCreateEvent(event);
			} else if (type == START_ACTIVITY) {
				processActivityCreation(event);
			} else if (type == DELETE_TASK) {
				processTaskDeleteEvent(event);
			}
		} catch (Exception e) {
			throw new CamundaIntegrationRuntimeException(
					"Can not create/modify instance based on process engine event: " + event, e);
		}
	}

	private void processTaskDeleteEvent(MultiEngineBusinessProcessEvent event) {
		Object taskId = event.getExecution().getVariable(TASK_ID);
		if (taskId != null && StringUtils.isNotBlank((String) taskId)) {
			Instance instance = BPMInstanceUtil.resolveInstance((String) taskId, instanceResolver);
			Date stopDate = new Date();
			instance.add(COMPLETED_ON, stopDate);
			domainInstanceService.save(InstanceSaveContext.create(instance, new Operation(STOP), stopDate));
		} else {
			throw new CamundaIntegrationRuntimeException(
					"Can not stop instance, because null taskId based on process engine event: " + event);
		}
	}

	private void processActivityCreation(MultiEngineBusinessProcessEvent event) {
		// expand variables
		expandExecutionVariables(event);
	}

	private void expandExecutionVariables(MultiEngineBusinessProcessEvent event) {
		VariableScope globalScope = event.getExecution().getProcessInstance();
		if (globalScope == null) {
			return;
		}

		String activityId = getModelElementId(event.getActivityId());
		TypedValue activityData = globalScope.getVariableTyped(activityId);
		Map<String, Object> newValues;
		if (activityData == null || !(activityData.getValue() instanceof Map<?, ?>)) {
			LOGGER.warn("Expected variable {} not found in scope {} with variables {}", activityId, globalScope,
					globalScope.getVariableNames());
			Instance instance = preProcessCreationEvent(activityId, event.getProcessBusinessKey());
			newValues = propertiesConverter.convertDataFromSEIPtoCamunda(instance.getOrCreateProperties(), null);
		} else {
			newValues = (Map<String, Object>) activityData.getValue();
		}
		VariableScope localScope = event.getLocalVariableScope();
		if (localScope != null) {
			// remove the cached value to clean up db
			globalScope.removeVariable(activityId);
			CamundaFormData camundaFormData = getCamundaFormData(getExtensionElements(event.getExecution()));
			if (camundaFormData != null && !camundaFormData.getCamundaFormFields().isEmpty() && !newValues.isEmpty()) {
				Collection<String> camundaFormFieldsIds = camundaFormData
						.getCamundaFormFields()
							.stream()
							.map(CamundaFormField::getCamundaId)
							.collect(Collectors.toSet());
				newValues.keySet().retainAll(camundaFormFieldsIds);
			}
			// expand the values but first remove all already injected/inserted values
			LOGGER.trace("Expanding variables in execution: {}. Source values: {}. Existing variables: {}.",
					event.getExecutionId(), newValues, localScope.getVariablesLocal().keySet());
			newValues.keySet().removeAll(localScope.getVariablesLocal().keySet());
			LOGGER.trace("The local variables after removing existing values: {}.", newValues);
			localScope.setVariablesLocal(newValues);
		}
	}

	private void processTaskCreateEvent(MultiEngineBusinessProcessEvent event) {
		Instance createInstance = preProcessCreationEvent(event.getTaskDefinitionKey(), event.getProcessBusinessKey());
		createInstance.add(ACTIVITY_ID, event.getTaskId());
		postProcessCreationEvent(event, createInstance);
	}

	private Instance preProcessCreationEvent(String id, String contextId) {
		String businessId = extractBusinessId(id);
		String[] typeAndSubTypeData = getTypeAndSubtype(businessId);
		Instance createInstance = domainInstanceService.createInstance(typeAndSubTypeData[0], contextId);
		if (typeAndSubTypeData.length > 1) {
			createInstance.add(INSTANCE_SUB_TYPE, typeAndSubTypeData[1]);
		}
		return createInstance;
	}

	private void postProcessCreationEvent(MultiEngineBusinessProcessEvent event, Instance createdActivity) {
		Map<String, Serializable> dataFromCamundaToSEIP = propertiesConverter
				.convertDataFromCamundaToSEIP(event.getExecution().getVariablesLocalTyped(), createdActivity);
		createdActivity.addAllProperties(dataFromCamundaToSEIP);
		createdActivity.add(TRANSITIONS, SequenceFlowParser.getSequenceFlowModel(event.getExecution()).serialize());
		// create InstanceReference because in some scenarios workflow instance is not created yet, but we know the id
		SaveRequest createRequest = SaveRequest.buildCreateRequest(createdActivity);
		createRequest.setContextPath(Collections.singletonList(event.getProcessBusinessKey()));
		Instance savedTask = (Instance) actions.callSlowAction(createRequest);
		event.getExecution().setVariableLocal(TASK_ID, savedTask.getId().toString());
	}
}
