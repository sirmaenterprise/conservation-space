package com.sirmaenterprise.sep.bpm.camunda.bpmn.service;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.START;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.STOP;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService.isProcess;
import static com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter.mergeProperties;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.getActivityId;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.getSingleValue;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.isActivity;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMServiceImpl.loadTask;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.OUTCOME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.VersionMode;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.properties.BPMTaskProperties;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService;
import com.sirmaenterprise.sep.bpm.camunda.service.SecureProcessEngine;
import com.sirmaenterprise.sep.bpm.exception.BPMException;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;

/**
 * {@link CamundaBPMNServiceImpl} is the default implementation of {@link ProcessService}. Service relays on the
 * {@link ProcessEngine} to execute the start/stop/transition in the BPM engine. In addition to core logic of
 * {@link ProcessService} it is included implementation of the Camunda specific bpmn service -
 * {@link CamundaBPMNService}
 *
 * @author bbanchev
 */
@ApplicationScoped
public class CamundaBPMNServiceImpl implements ProcessService, CamundaBPMNService {

	public static final String WORKFLOW_ID = "workflowId";
	public static final String PROCESS_INSTANCE_ID = "processInstanceId";
	public static final String WORKFLOW_PROPERTIES = "workflowProperties";
	private static final String EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED = "Task instance could not be resolved for activity: ";

	private static final Set<String> SYSTEM_PROPERTIES = Collections.unmodifiableSet(new LinkedHashSet<>(
			Arrays.asList(DefaultProperties.MODIFIED_BY, DefaultProperties.MODIFIED_ON,
						  DefaultProperties.HEADER_BREADCRUMB, DefaultProperties.HEADER_TOOLTIP,
						  DefaultProperties.IS_ACTIVE, DefaultProperties.IS_DELETED, DefaultProperties.LOCKED_INFO,
						  DefaultProperties.HEADER_COMPACT, DefaultProperties.HEADER_DEFAULT, DefaultProperties.VERSION,
						  DefaultProperties.URI, DefaultProperties.REVISION_NUMBER, DefaultProperties.PUBLISHED_ON,
						  DefaultProperties.PUBLISHED_BY, DefaultProperties.REVISION_TYPE, DefaultProperties.STATUS)));
	@DestinationDef(value = "java:/jms.queue.camundaStartWorkflowQueue")
	protected static final String CAMUNDA_BPMN_JMS_QUEQUE = "java:/jms.queue.camundaStartWorkflowQueue";

	@Inject
	private ProcessEngine processEngine;
	@Inject
	private BPMPropertiesConverter propertiesConverter;
	@Inject
	private DomainInstanceService domainInstanceService;
	@Inject
	private CommandExecutor commandExecutor;
	@Inject
	private Actions actions;
	@Inject
	private SecurityContextManager contextManager;
	@Inject
	private SenderService senderService;
	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Instance startProcess(Instance process, Map<String, Serializable> transitionData) throws BPMException {
		try {
			ProcessDefinition definition = getProcessDefinition(process);
			validateDefinition(definition);
			StartFormData startFormData = processEngine.getFormService().getStartFormData(definition.getId());
			Map<String, Object> convertedData = propertiesConverter.convertDataFromSEIPtoCamunda(
					process.getOrCreateProperties(), startFormData.getFormFields());
			convertedData.keySet().removeAll(SYSTEM_PROPERTIES);
			convertedData.put(OUTCOME, "start");
			Map<String, Object> transitionDataFiltered = propertiesConverter.convertDataFromSEIPtoCamunda(
					transitionData, null);
			convertedData.putAll(transitionDataFiltered);
			ProcessInstance processInstance = processEngine.getRuntimeService()
					.startProcessInstanceById(definition.getId(), process.getId().toString(), convertedData);
			process.add(ACTIVITY_ID, processInstance.getId());
			return process;
		} catch (CamundaIntegrationException e) {
			throw e;
		} catch (Exception e) {
			throw new CamundaIntegrationException("Fail to start a process in Camunda system!", e);
		}
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public void notify(String eventName, Map<String, Object> processVariables) {
		List<EventSubscription> eventSubscriptions = processEngine.getRuntimeService()
				.createEventSubscriptionQuery()
				.eventName(eventName)
				.list();
		notify(eventSubscriptions, processVariables);
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public void notify(String eventName, String executionId, Map<String, Object> processVariables) {
		List<EventSubscription> eventSubscriptions = processEngine.getRuntimeService()
				.createEventSubscriptionQuery()
				.executionId(executionId)
				.eventName(eventName)
				.list();
		notify(eventSubscriptions, processVariables);
	}

	private void notify(List<EventSubscription> eventSubscriptions, Map<String, Object> processVariables) {
		for (EventSubscription eventSubscription : eventSubscriptions) {
			String eventType = eventSubscription.getEventType();
			if ("signal".equalsIgnoreCase(eventType)) {
				processEngine.getRuntimeService()
						.signalEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId(),
											 processVariables);
			} else if ("message".equalsIgnoreCase(eventType)) {
				processEngine.getRuntimeService()
						.messageEventReceived(eventSubscription.getEventName(), eventSubscription.getExecutionId(),
											  processVariables);
			}
		}
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Instance startProcess(String messageId, String parentId, Map<String, Serializable> transitionData)
			throws BPMException {
		try {
			ProcessDefinition definition = getProcessDefinitionByMessageId(messageId);
			validateDefinition(definition);

			StartFormData startFormData = processEngine.getFormService().getStartFormData(definition.getId());
			Instance workflowInstance = createWorkflowInstance(definition, parentId, transitionData);
			Map<String, Object> convertedData = propertiesConverter.convertDataFromSEIPtoCamunda(
					workflowInstance.getOrCreateProperties(), startFormData.getFormFields());

			SYSTEM_PROPERTIES.stream()
					.map(fieldConverter.resolverFor(workflowInstance))
					.forEach(convertedData.keySet()::remove);
			convertedData.put(OUTCOME, "start");
			if (transitionData != null && !transitionData.isEmpty()) {
				Map<String, Object> transitionDataFiltered = propertiesConverter.convertDataFromSEIPtoCamunda(
						transitionData, null);
				convertedData.putAll(transitionDataFiltered);
			}
			ProcessInstance processInstance = processEngine.getRuntimeService()
					.startProcessInstanceByMessage(messageId, workflowInstance.getId().toString(), convertedData);
			workflowInstance.add(ACTIVITY_ID, processInstance.getId());
			queueForAsyncSave(workflowInstance, processInstance, START);
			return workflowInstance;
		} catch (CamundaIntegrationException e) {
			throw e;
		} catch (Exception e) {
			throw new CamundaIntegrationException("Fail to start a process in Camunda system!", e);
		}
	}

	private void queueForAsyncSave(Instance workflowInstance, ProcessInstance processInstance, String operation) {
		VariableMap processInstanceVariables = getProcessInstanceVariables(processInstance, true);
		Map<String, Serializable> workflowProperties = propertiesConverter.convertDataFromCamundaToSEIP(
				processInstanceVariables, workflowInstance);
		Map<String, Serializable> data = new HashMap<>(4);
		data.put(WORKFLOW_ID, workflowInstance.getId());
		data.put(PROCESS_INSTANCE_ID, processInstance.getProcessInstanceId());
		data.put(WORKFLOW_PROPERTIES, new HashMap<>(workflowProperties));
		data.put(InstanceOperationProperties.OPERATION, operation);
		senderService.send(CAMUNDA_BPMN_JMS_QUEQUE, data, SendOptions.create().withWriter(ObjectMessageWriter.instance()));
	}

	@QueueListener(value = CAMUNDA_BPMN_JMS_QUEQUE)
	void receiveForAsyncSave(Message message) throws Exception {
		if (message instanceof ObjectMessage) {
			Map<String, Serializable> data = (Map<String, Serializable>) ((ObjectMessage) message).getObject();
			Serializable workflowId = data.get(WORKFLOW_ID);
			Serializable processInstanceId = data.get(PROCESS_INSTANCE_ID);
			Serializable operation = data.get(InstanceOperationProperties.OPERATION);
			Serializable workflowProperties = data.get(WORKFLOW_PROPERTIES);
			Instance workflowInstance = domainInstanceService.loadInstance((String) workflowId);
			mergeProperties(workflowInstance.getOrCreateProperties(), (Map<String, Serializable> )workflowProperties);
			workflowInstance.add(ACTIVITY_ID, processInstanceId.toString());
			saveInstanceWithoutVersion(workflowInstance, (String) operation);
		}
	}

	private Instance createWorkflowInstance(ProcessDefinition definition, String parentId, Map<String, Serializable> transitionData) {
		Instance workflowInstance = domainInstanceService.createInstance(definition.getKey(), parentId);
		workflowInstance.addAllProperties(transitionData);
		SaveRequest createRequest = SaveRequest.buildCreateRequest(workflowInstance);
		return (Instance) actions.callAction(createRequest);
	}

	private static void validateDefinition(ProcessDefinition definition) throws CamundaIntegrationException {
		if (definition == null) {
			throw new CamundaIntegrationException(" No definition model found!");
		}
		if (definition.isSuspended()) {
			throw new CamundaIntegrationException(
					" BPMN model with key: " + definition.getKey() + " is suspended for execution!");
		}
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public ProcessDefinition getProcessDefinition(Instance process) {
		return getProcessDefinitionByDefinitionId(process.getIdentifier());
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public ProcessDefinition getProcessDefinitionByMessageId(String messageId) {
		List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService()
				.createProcessDefinitionQuery()
				.messageEventSubscriptionName(messageId)
				.withoutTenantId()
				.latestVersion()
				.list();
		return CamundaBPMService.getSingleValue(processDefinitions, () -> new CamundaIntegrationRuntimeException(
				"Found: " + processDefinitions.size() + " BPMN models for messageId: " + messageId
						+ "! Expected count is 1!"));
	}

	private ProcessDefinition getProcessDefinitionByDefinitionId(String processDefinitionId) {
		List<ProcessDefinition> processDefinitions = processEngine.getRepositoryService().createProcessDefinitionQuery()
				// same key as the identifier - by design and specification
				.processDefinitionKey(processDefinitionId).withoutTenantId()
				// only latest versions
				.latestVersion().list();

		return CamundaBPMService.getSingleValue(processDefinitions, () -> new CamundaIntegrationRuntimeException(
				"Found: " + processDefinitions.size() + " BPMN models for key: " + processDefinitionId
						+ "! Expected count is 1!"));
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Instance cancelProcess(Instance process) throws BPMException {
		String executionId = getActivityId(process);
		Objects.requireNonNull(executionId, "Process instance id (" + ACTIVITY_ID
				+ ") is a required argument to indicate which process instance should be cancelled!");
		if (!CamundaBPMNService.isProcess(process)) {
			return process;
		}
		try {
			processEngine.getRuntimeService().deleteProcessInstance(executionId, process.getAsString("stopReason"));
			domainInstanceService.save(InstanceSaveContext.create(process, new Operation(STOP)));
			return process;
		} catch (Exception e) {
			throw new CamundaIntegrationException(
					"Failed to cancel a process with id: " + executionId + " in Camunda system!", e);
		}
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public void claimTask(Instance taskInstance, String userId) {
		String activityId = getActivityId(taskInstance);
		Optional<Task> activityTask = loadTaskInternal(taskInstance);
		if (!activityTask.isPresent()) {
			throw new CamundaIntegrationRuntimeException(EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED + activityId);
		}
		processEngine.getTaskService().claim(activityId, userId);
		processEngine.getTaskService().setVariable(activityId, BPMTaskProperties.TASK_ASSIGNEE, userId);
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public void reassignTask(Instance taskInstance, String userId) {
		try {
			String taskId = getActivityId(taskInstance);
			Optional<Task> activityTask = loadTaskInternal(taskInstance);
			if (!activityTask.isPresent()) {
				throw new CamundaIntegrationRuntimeException(EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED + taskId);
			}
			if (isTaskPooled(taskInstance)) {
				List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(taskId);
				if (!existIdentityLink(identityLinks, userId, IdentityLinkType.CANDIDATE)) {
					updatePoolTaskAssignee(taskInstance, userId, taskId);
				}
			}
			processEngine.getTaskService().setAssignee(taskId, userId);
			processEngine.getTaskService().setVariable(taskId, BPMTaskProperties.TASK_ASSIGNEE, userId);
		} catch (Exception e) {
			throw new CamundaIntegrationRuntimeException("Fail to reassign a task in Camunda system!", e);
		}
	}

	private void updatePoolTaskAssignee(Instance taskInstance, String userId, String taskId) throws Exception {
		processEngine.getTaskService().addUserIdentityLink(taskId, userId, IdentityLinkType.CANDIDATE);
		Serializable poolAssignees = taskInstance.get(BPMTaskProperties.TASK_POOL_ASSIGNEES);
		if (poolAssignees instanceof List) {
			ArrayList newPoolAssignees = new ArrayList<>((List) poolAssignees);
			newPoolAssignees.add(userId);
			taskInstance.add(BPMTaskProperties.TASK_POOL_ASSIGNEES, newPoolAssignees);
			processEngine.getTaskService()
					.setVariable(taskId, BPMTaskProperties.TASK_POOL_ASSIGNEES, newPoolAssignees);
			saveInstanceWithoutVersion(taskInstance, EDIT_DETAILS);
		}
	}

	private Instance saveInstanceWithoutVersion(Instance instance, String operation) throws Exception {
		try {
			InstanceSaveContext context = InstanceSaveContext.create(instance, new Operation(operation));
			context.getVersionContext().setVersionMode(VersionMode.NONE);
			Options.OVERRIDE_MODIFIER_INFO.enable();
			return contextManager.executeAsAdmin().callable(() -> domainInstanceService.save(context));
		} finally {
			Options.OVERRIDE_MODIFIER_INFO.disable();
		}
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public void releaseTask(Instance taskInstance) {
		String activityId = getActivityId(taskInstance);
		Optional<Task> activityTask = loadTaskInternal(taskInstance);
		if (!activityTask.isPresent()) {
			throw new CamundaIntegrationRuntimeException(EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED + activityId);
		}
		processEngine.getTaskService().setAssignee(activityId, null);
		processEngine.getTaskService().removeVariable(activityId, BPMTaskProperties.TASK_ASSIGNEE);
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Instance transition(Instance activity, String operation, Map<String, Serializable> activityData,
			Map<String, Serializable> transitionData) {
		if (!isActivity(activity)) {
			return null;
		}
		String activityId = getActivityId(activity);
		// set workflow data
		String processInstanceId = getProcessInstanceId(activity);
		Objects.requireNonNull(processInstanceId,
							   () -> "Process instance id could not be resolved for activity: " + activityId);
		Map<String, Object> transitionDataFiltered = propertiesConverter.convertDataFromSEIPtoCamunda(transitionData,
																									  null);
		transitionDataFiltered.put(OUTCOME, operation);
		processEngine.getRuntimeService().setVariables(processInstanceId, transitionDataFiltered);
		// set task data
		TaskFormData taskForm = processEngine.getFormService().getTaskFormData(activityId);
		Map<String, Object> taskDataFiltered = propertiesConverter.convertDataFromSEIPtoCamunda(activityData,
																								taskForm.getFormFields());
		taskDataFiltered.put(OUTCOME, operation);
		Optional<Task> activityTask = loadTaskInternal(activity);
		if (!activityTask.isPresent()) {
			throw new CamundaIntegrationRuntimeException(EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED + activityId);
		}
		// set in local context to be usable for injection/outjections
		processEngine.getRuntimeService().setVariablesLocal(activityTask.get().getExecutionId(), taskDataFiltered);
		// complete the task
		processEngine.getTaskService().complete(activityId);
		return activity;
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Collection<String> listActiveTasksIds(String processInstanceId) {
		Collection<Task> tasks = listActiveTasks(processInstanceId);
		return tasks.stream().map(Task::getId).collect(Collectors.toSet());
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Collection<Task> listActiveTasks(String processInstanceId) {
		return processEngine.getTaskService().createTaskQuery().active().processInstanceId(processInstanceId).list();
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public BpmnModelInstance getBpmnModelInstance(Instance activity) {
		if (activity == null) {
			return null;
		}
		ProcessDefinition processDefinition = getProcessDefinition(activity);
		if (processDefinition != null) {
			return getBpmnModelInstance(processDefinition.getId());
		}
		return null;
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public BpmnModelInstance getBpmnModelInstance(String processDefinitionId) {
		if (StringUtils.isBlank(processDefinitionId)) {
			return null;
		}
		return processEngine.getRepositoryService().getBpmnModelInstance(processDefinitionId);
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public ProcessInstance getProcessInstance(Instance activity) {
		String processInstanceId = getProcessInstanceId(activity);
		if (processInstanceId == null) {
			return null;
		}
		return loadProcessInstanceById(processEngine.getRuntimeService(), processInstanceId).orElse(null);
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public String getProcessInstanceId(Instance activity) {
		// check if it is task
		if (isActivity(activity)) {
			// check process
			if (isProcess(activity)) {
				return getActivityId(activity);
			}
			Optional<Task> currentActivity = loadTaskInternal(activity);
			if (currentActivity.isPresent()) {
				return currentActivity.get().getProcessInstanceId();
			}
		}
		return null;
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public VariableMap getProcessInstanceVariables(ProcessInstance processInstance, boolean all) {
		if (processInstance instanceof ExecutionEntity) {
			return commandExecutor.execute((CommandContext command) -> {
				ExecutionEntity executionEntity = (ExecutionEntity) processInstance;
				// always local variables
				VariableMap variablesTyped = new VariableMapImpl(executionEntity.getVariablesLocalTyped());
				if (all) {
					VariableMapImpl globalVariablesTyped = executionEntity.getVariablesTyped();
					globalVariablesTyped.keySet()
							.forEach(k -> variablesTyped.putValueTyped(k, globalVariablesTyped.getValueTyped(k)));
				}
				return variablesTyped;
			});
		} else if (processInstance instanceof ProcessInstanceWithVariables) {
			return new VariableMapImpl(((ProcessInstanceWithVariables) processInstance).getVariables());
		}
		return null;
	}

	/**
	 * Gets the process instance for given process instance id.
	 *
	 * @param runtimeService
	 * 		the Camunda engine service to use for this request
	 * @param processInstanceId
	 * 		the process instance id
	 * @return the process instance as {@link Optional} or {@link Optional#empty()} if not found
	 */
	public static Optional<ProcessInstance> loadProcessInstanceById(RuntimeService runtimeService,
			String processInstanceId) {
		List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstanceId)
				.list();
		return Optional.ofNullable(getSingleValue(processInstances, () -> new CamundaIntegrationRuntimeException(
				"Found " + processInstances.size() + " process instances for id " + processInstanceId)));
	}

	/**
	 * Gets the historic process instance for given process instance id.
	 *
	 * @param historyService
	 * 		the Camunda engine service to use for this request
	 * @param processInstanceId
	 * 		the historic process instance id
	 * @return the process instance as {@link Optional} or {@link Optional#empty()} if not found
	 */
	public static Optional<HistoricProcessInstance> loadHistoricProcessInstanceById(HistoryService historyService,
			String processInstanceId) {
		List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId)
				.list();
		return Optional.ofNullable(getSingleValue(historicProcessInstances,
												  () -> new CamundaIntegrationRuntimeException(
														  "Found " + historicProcessInstances.size()
																  + " historic process instances for id "
																  + processInstanceId)));
	}

	private Optional<Task> loadTaskInternal(Instance activity) {
		return loadTask(processEngine.getTaskService(), activity);
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public boolean isTaskClaimable(Instance taskInstance, String userId) {
		if (!isActivity(taskInstance) || isProcess(taskInstance)) {
			return false;
		}
		String taskId = getActivityId(taskInstance);
		Optional<Task> activityTask = loadTaskInternal(taskInstance);
		if (!activityTask.isPresent()) {
			throw new CamundaIntegrationRuntimeException(EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED + taskId);
		}
		if (StringUtils.isNotBlank(activityTask.get().getAssignee())) {
			return false;
		}
		List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(taskId);
		return existIdentityLink(identityLinks, userId, IdentityLinkType.CANDIDATE);
	}

	private static boolean existIdentityLink(List<IdentityLink> identityLinks, String userId, String identityLinkType) {
		if (identityLinkType != null) {
			return identityLinks.stream()
					.filter(link -> identityLinkType.equals(link.getType()))
					.anyMatch(link -> link.getUserId().equals(userId));
		}
		return identityLinks.stream().anyMatch(link -> link.getUserId().equals(userId));
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public boolean isTaskAssignee(Instance taskInstance, String userId) {
		if (!isActivity(taskInstance) || isProcess(taskInstance)) {
			return false;
		}
		String taskId = getActivityId(taskInstance);
		Optional<Task> activityTask = loadTaskInternal(taskInstance);
		if (!activityTask.isPresent()) {
			throw new CamundaIntegrationRuntimeException(EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED + taskId);
		}
		String currentAssignee = activityTask.get().getAssignee();
		return StringUtils.isNotBlank(currentAssignee) && currentAssignee.equals(userId);
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public boolean isTaskReleasable(Instance taskInstance, String userId) {
		if (!isActivity(taskInstance) || isProcess(taskInstance)) {
			return false;
		}
		String taskId = getActivityId(taskInstance);
		Optional<Task> activityTask = loadTaskInternal(taskInstance);
		if (!activityTask.isPresent()) {
			throw new CamundaIntegrationRuntimeException(EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED + taskId);
		}
		String currentAssignee = activityTask.get().getAssignee();
		if (StringUtils.isNotBlank(currentAssignee) && currentAssignee.equals(userId)) {
			List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(taskId);
			return identityLinks.stream()
					.filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()))
					.anyMatch(link -> link.getUserId().equals(userId));
		}
		return false;
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public boolean isTaskPooled(Instance taskInstance) {
		if (!isActivity(taskInstance) || isProcess(taskInstance)) {
			return false;
		}
		String taskId = getActivityId(taskInstance);
		Optional<Task> activityTask = loadTaskInternal(taskInstance);
		if (!activityTask.isPresent()) {
			throw new CamundaIntegrationRuntimeException(EXCEPTION_MESSAGE_TASK_COULD_NOT_BE_RESOLVED + taskId);
		}
		List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(taskId);
		return identityLinks.stream().anyMatch(link -> IdentityLinkType.CANDIDATE.equals(link.getType()));
	}
}
