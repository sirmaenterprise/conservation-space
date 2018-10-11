package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.ACTIVITY_IN_PROCESS;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.COMPLETED_ON;
import static com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter.mergeProperties;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.getActivityId;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.isActivity;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.getTypeAndSubtype;
import static com.sirmaenterprise.sep.bpm.camunda.util.BPMActivityUtil.isActivityInProgress;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.OUTCOME;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.camunda.actions.BPMActionRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.actions.BPMOperationAction;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.camunda.transitions.model.TransitionModelService;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.BPMStateTransitionProvider;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowEntry;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMInstanceUtil;
import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 * Action implementation that can execute the {@link BPMTransitionRequest#OPERATION_NAME} action.<br>
 * The provided {@link BPMTransitionRequest} should contain a currently possible flow (userOperation) and the currently
 * processed instance (the target id). {@link BPMTransitionRequest} also might contain data for the target instance or
 * for the next activities ( keyed by their id in the {@link BPMTransitionRequest#getTransitionData()}).<br>
 * During the transition it is possible a set of activities to be created based on domain logic in this module for
 * handling process engine events. All such activities are stored in thread local store (all processing in done in same
 * thread) and on {@link BPMTransitionAction#executeBPMAction(BPMTransitionRequest)} completion are returned as a result of the
 * current {@link BPMTransitionRequest#OPERATION_NAME}.
 *
 * @author bbanchev
 */
@Extension(target = Action.TARGET_NAME, order = 600)
@ApplicationScoped
public class BPMTransitionAction extends BPMOperationAction<BPMTransitionRequest> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	protected ThreadLocal<List<Instance>> processedActivities = new ThreadLocal<>();

	@Inject
	protected InstanceTypeResolver instanceResolver;
	@Inject
	protected DomainInstanceService domainInstanceService;
	@Inject
	protected InstanceService instanceService;
	@Inject
	protected ProcessService processService;
	@Inject
	protected CamundaBPMNService camundaBPMNService;
	@Inject
	protected BPMPropertiesConverter propertiesConverter;
	@Inject
	protected TransitionModelService transitionModelService;
	@Inject
	protected LockService lockService;
	@Inject
	protected SecurityContext securityContext;

	@Override
	public String getName() {
		return BPMTransitionRequest.OPERATION_NAME;
	}

	@Override
	protected void preProcessBPMAction(BPMTransitionRequest request) {
		processedActivities.set(new LinkedList<>());
		Instance process = BPMInstanceUtil.resolveInstance((String) request.getTargetId(), instanceResolver);
		InstanceReference reference = process.toReference();
		request.setTargetReference(reference);
		if (!isTransitionRequestValid(request)) {
			throw new BPMActionRuntimeException("Transition: " + request.getUserOperation() + " for instance: "
					+ process + " is not possible because of false condition!", "bpm.transition.condition.failure");
		}
		if (!lockService.isAllowedToModify(reference)) {
			throw new BPMActionRuntimeException("Transition: " + request.getUserOperation() + " for instance: "
					+ process + " is not possible because of locked instance!", "bpm.transition.condition.locked");
		}
	}

	/**
	 * Handles {@link BPMTransitionRequest} and check if the the operation is part of process. If so a transition in the
	 * process engine is executed using {@link ProcessService#transition(Instance, String, Map, Map)}
	 *
	 * @param request
	 *            is the event wrapper that is used to provide additional details
	 * @return the list of newly generated instances part of the same process
	 * @throws CamundaIntegrationException
	 *             on an error in underlying processing
	 */
	@Override
	protected Object executeBPMAction(BPMTransitionRequest request) throws BPMException {
		LOGGER.debug("Going to execute request '{}'", request);
		Instance instance = request.getTargetReference().toInstance();
		SequenceFlowEntry transition;
		if (isActivity(instance) && (transition = interceptedTransition(request)) != null
				&& isActivityInProgress(instance)) {
			Map<String, Serializable> activityData = collectActivityProperties(request);
			Map<String, Serializable> transitionData = collectTransitionProperties(request, transition);
			ProcessInstance processInstance = camundaBPMNService.getProcessInstance(instance);
			if (processInstance == null) {
				throw new CamundaIntegrationException(
						"Process instance could not be resolved for activity: " + instance);
			}
			Instance updated = processService.transition(instance, request.getUserOperation(), activityData,
					transitionData);
			if (updated != null) {
				updateCompletedTask(request);
				loadAndTransferProcessProperties(processInstance);
				return listGeneratedActivities(request, processInstance.getId());
			}
		}
		return listGeneratedActivities(request, null);
	}

	private void loadAndTransferProcessProperties(ProcessInstance camundaProcess) {
		// process instance would not be null at this location
		Instance sepProcess = BPMInstanceUtil.resolveInstance(camundaProcess.getBusinessKey(), instanceResolver);
		sepProcess = loadAndTransferProcessProperties(camundaProcess, sepProcess);
		// if modified - now save it
		if (sepProcess != null) {
			// save the updated process - don't update version by specification
			instanceService.save(sepProcess, new Operation(EDIT_DETAILS));
		}
	}

	protected Instance loadAndTransferProcessProperties(ProcessInstance processInstance, Instance process) {
		Objects.requireNonNull(processInstance,
				"Camunda process instance is required argument as source value to transfer properties from Process Engine!");
		Objects.requireNonNull(process,
				"Process instance is required argument as target value to transfer properties from Process Engine!");

		VariableMap processInstanceVariables = camundaBPMNService.getProcessInstanceVariables(processInstance, true);
		Map<String, Serializable> workflowProperties = propertiesConverter
				.convertDataFromCamundaToSEIP(processInstanceVariables, process);
		boolean modified = mergeProperties(process.getOrCreateProperties(), workflowProperties);
		if (modified) {
			return process;
		}
		return null;
	}

	void activityInstanceCreated(@Observes AfterInstancePersistEvent<?, ?> persisted) {
		Instance instance = persisted.getInstance();
		if (isActivity(instance) && processedActivities.get() != null) {
			processedActivities.get().add(instance);
		}
	}

	<T extends BPMTransitionRequest> Collection<Instance> listGeneratedActivities(T request, String processInstanceId) {
		List<Instance> generatedActivities = Collections.emptyList();
		if (processInstanceId != null) {
			Collection<String> activyTasks = camundaBPMNService.listActiveTasksIds(processInstanceId);
			// filter only current active tasks for process
			generatedActivities = processedActivities
					.get()
						.stream()
						.filter(task -> activyTasks.contains(getActivityId(task)))
						.collect(Collectors.toList());

		}
		LOGGER.trace("Returning '{}' after {} request '{}'", generatedActivities, getName(), request);
		return generatedActivities;
	}

	static Map<String, Serializable> collectActivityProperties(BPMTransitionRequest request) {
		Map<String, Instance> requestData = request.getTransitionData();
		if (requestData == null) {
			return Collections.emptyMap();
		}
		String currentInstanceId = request.getTargetId().toString();
		Instance currentActivity = requestData.remove(currentInstanceId);
		if (currentActivity == null) {
			return Collections.emptyMap();
		}
		return currentActivity.getOrCreateProperties();
	}

	static Map<String, Serializable> collectTransitionProperties(BPMTransitionRequest request,
			SequenceFlowEntry transition) {
		Map<String, Instance> requestData = request.getTransitionData();
		if (requestData == null) {
			return Collections.emptyMap();
		}
		Map<String, Serializable> result = new LinkedHashMap<>(requestData.size());

		Set<Entry<String, Instance>> dataEntries = requestData.entrySet();
		for (Entry<String, Instance> entry : dataEntries) {
			Stream<Entry<String, String>> filter = transition.getCheckpoints().entrySet().stream().filter(
					checkpoint -> getTypeAndSubtype(checkpoint.getValue())[0].equals(entry.getValue().getIdentifier()));
			// bind by activity id - a uid to uniquely identify instance
			filter.forEach(checkpoint -> result.put(checkpoint.getKey(),
					new HashMap<>(entry.getValue().getOrCreateProperties())));
		}
		return result;
	}

	private void updateCompletedTask(BPMTransitionRequest request) {
		Instance completedInstance = request.getTargetReference().toInstance();
		String userOperation = request.getUserOperation();
		completedInstance.add(OUTCOME, userOperation);
		Date completedOn = new Date();
		completedInstance.add(COMPLETED_ON, completedOn);
		// marker for currently being processed activity
		completedInstance.add(ACTIVITY_IN_PROCESS, Boolean.TRUE);
		domainInstanceService
				.save(InstanceSaveContext.create(completedInstance, new Operation(userOperation), completedOn));
	}

	private static SequenceFlowEntry interceptedTransition(BPMTransitionRequest request) {
		Instance instance = request.getTargetReference().toInstance();
		return BPMStateTransitionProvider.getTransition(instance, request.getUserOperation());
	}

	private boolean isTransitionRequestValid(BPMTransitionRequest request) {
		Instance instance = request.getTargetReference().toInstance();
		SequenceFlowEntry transition = BPMStateTransitionProvider.getTransition(instance, request.getUserOperation());
		Predicate<SequenceFlowEntry> transitionConditionFilter = transitionModelService
				.createTransitionConditionFilter(instance);
		return transitionConditionFilter.test(transition);
	}

	@Override
	protected void postProcessBPMAction(BPMTransitionRequest request) {
		processedActivities.remove();
	}
}
