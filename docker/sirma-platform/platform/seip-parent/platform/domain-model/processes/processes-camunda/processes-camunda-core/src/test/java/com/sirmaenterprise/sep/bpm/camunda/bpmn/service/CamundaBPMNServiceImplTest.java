package com.sirmaenterprise.sep.bpm.camunda.bpmn.service;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.service.CamundaBPMNServiceImpl.CAMUNDA_BPMN_JMS_QUEQUE;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.service.CamundaBPMNServiceImpl.WORKFLOW_ID;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.properties.BPMTaskProperties;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.exception.BPMException;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;

/**
 * Tests {@link CamundaBPMNServiceImpl}
 *
 * @author bbanchev
 */
public class CamundaBPMNServiceImplTest {

	private static final String PROCESS_ID = "processId";
	private static final String TASK_ID = "taskId";
	private static final String INSTANCE_ID = "myId";
	private static final String PROCESS_DEF_KEY = "myTestProcess";
	private static final String PROCESS_DEF_ID = "myTestProcessId";
	private static final String EMF_USER_ID = "emf:userId";
	private static final String MESSAGE_ID = "messageId";

	@Mock
	private ProcessEngine processEngine;
	@Mock
	private RepositoryService repositoryService;
	@Mock
	private RuntimeService runtimeService;
	@Mock
	private FormService formService;
	@Mock
	private TaskService taskService;
	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private BPMPropertiesConverter modelConverter;
	@Mock
	private Actions actions;
	@Mock
	private SenderService senderService;
	@Spy
	private SecurityContextManager contextManager = new SecurityContextManagerFake();
	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@InjectMocks
	private CamundaBPMNServiceImpl processService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(processEngine.getName()).thenReturn(DEFAULT_ENGINE);
		when(processEngine.getRepositoryService()).thenReturn(repositoryService);
		when(processEngine.getRuntimeService()).thenReturn(runtimeService);
		when(processEngine.getFormService()).thenReturn(formService);
		when(processEngine.getTaskService()).thenReturn(taskService);
	}

	@Test
	public void test_getProcessDefinitionByMessageId() {
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.messageEventSubscriptionName(eq(MESSAGE_ID))).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.list()).thenReturn(Collections.emptyList());

		assertNull(processService.getProcessDefinitionByMessageId(MESSAGE_ID));
	}

	@Test(expected = CamundaIntegrationException.class)
	public void test_startProcess_not_found_processDefinition() throws BPMException {
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.messageEventSubscriptionName(eq(MESSAGE_ID))).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.list()).thenReturn(Collections.emptyList());
		processService.startProcess(MESSAGE_ID, null, null);
	}

	@Test(expected = CamundaIntegrationException.class)
	public void test_startProcess_suspended_processDefinition() throws BPMException {
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.messageEventSubscriptionName(eq(MESSAGE_ID))).thenReturn(processDefinitionQuery);
		ProcessDefinition processDefinition = mock(ProcessDefinition.class);
		when(processDefinition.isSuspended()).thenReturn(Boolean.TRUE);
		when(processDefinitionQuery.list()).thenReturn(Arrays.asList(processDefinition));
		processService.startProcess(MESSAGE_ID, null, null);
	}

	@Test
	public void test_startProcess_with_transitionData() throws BPMException {
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.messageEventSubscriptionName(eq(MESSAGE_ID))).thenReturn(processDefinitionQuery);
		ProcessDefinition processDefinition = mock(ProcessDefinition.class);
		when(processDefinition.isSuspended()).thenReturn(Boolean.FALSE);
		when(processDefinition.getId()).thenReturn(PROCESS_DEF_ID);
		when(processDefinition.getKey()).thenReturn(PROCESS_DEF_KEY);
		when(processDefinitionQuery.list()).thenReturn(Arrays.asList(processDefinition));

		Instance workflow = mock(Instance.class);
		when(workflow.getId()).thenReturn(INSTANCE_ID);
		when(workflow.getOrCreateProperties()).thenReturn(new HashMap<>());
		when(domainInstanceService.createInstance(eq(PROCESS_DEF_KEY), eq(null))).thenReturn(workflow);

		ProcessInstance processInstance = mock(ProcessInstance.class);
		when(processInstance.getId()).thenReturn(PROCESS_ID);
		when(runtimeService.startProcessInstanceByMessage(eq(MESSAGE_ID), eq(INSTANCE_ID), anyMap())).thenReturn(
				processInstance);
		when(actions.callAction(any(SaveRequest.class))).thenReturn(workflow);
		StartFormData form = mock(StartFormData.class);
		// without filtering
		when(form.getFormFields()).thenReturn(Collections.emptyList());
		when(formService.getStartFormData(eq(PROCESS_DEF_ID))).thenReturn(form);

		Map<String, Serializable> transitionData = new HashMap<>(1);
		transitionData.put(BPMTaskProperties.TASK_ASSIGNEE, EMF_USER_ID);
		processService.startProcess(MESSAGE_ID, null, transitionData);

		verify(actions).callAction(argThat(CustomMatcher.of((SaveRequest request) -> {
			assertEquals(workflow, request.getTarget());
			assertEquals(ActionTypeConstants.CREATE, request.getUserOperation());
			assertEquals(SaveRequest.OPERATION_NAME, request.getOperation());
		})));

		verify(senderService).send(eq(CAMUNDA_BPMN_JMS_QUEQUE),
				argThat(CustomMatcher.of((Map<String, Serializable> data) -> {
					assertEquals(INSTANCE_ID, data.get(WORKFLOW_ID));
					assertEquals(ActionTypeConstants.START, data.get(InstanceOperationProperties.OPERATION));
				})), argThat(CustomMatcher.of((SendOptions sendOptions) -> {
					assertEquals(ObjectMessageWriter.instance(), sendOptions.getWriter());
				})));
	}

	@Test(expected = CamundaIntegrationException.class)
	public void test_startProcess_exception() throws BPMException {
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.messageEventSubscriptionName(eq(MESSAGE_ID))).thenReturn(processDefinitionQuery);
		ProcessDefinition processDefinition = mock(ProcessDefinition.class);
		when(processDefinition.isSuspended()).thenReturn(Boolean.FALSE);
		when(processDefinition.getId()).thenReturn(PROCESS_DEF_ID);
		when(processDefinition.getKey()).thenReturn(PROCESS_DEF_KEY);
		when(processDefinitionQuery.list()).thenReturn(Arrays.asList(processDefinition));

		when(domainInstanceService.createInstance(eq(PROCESS_DEF_KEY), eq(null))).thenThrow(
				new CamundaIntegrationRuntimeException(""));

		processService.startProcess(MESSAGE_ID, null, null);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void test_getProcessDefinitionByMessageId_more_than_one() {
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.messageEventSubscriptionName(eq(MESSAGE_ID))).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.list()).thenReturn(
				Arrays.asList(mock(ProcessDefinition.class), mock(ProcessDefinition.class)));
		processService.getProcessDefinitionByMessageId(MESSAGE_ID);
	}

	@Test(expected = CamundaIntegrationException.class)
	public void testStartProcess_no_process_definition_found() throws Exception {
		Instance instance = mockInstance(null, true);
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.processDefinitionKey(eq(PROCESS_DEF_KEY))).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.list()).thenReturn(Collections.emptyList());

		processService.startProcess(instance, new HashMap<>());
	}

	@Test
	public void testStartProcessWithoutFiltering() throws Exception {
		Instance instance = mockInstance(null, false);
		mockProcessDefinition();
		when(instance.getOrCreateProperties()).thenReturn(new HashMap<>());
		StartFormData form = mock(StartFormData.class);
		// without filtering
		when(form.getFormFields()).thenReturn(Collections.emptyList());
		when(formService.getStartFormData(eq(PROCESS_DEF_ID))).thenReturn(form);
		ProcessInstance processInstance = mock(ProcessInstance.class);
		String processInstanceId = UUID.randomUUID().toString();
		when(processInstance.getId()).thenReturn(processInstanceId);
		when(runtimeService.startProcessInstanceById(eq(PROCESS_DEF_ID), eq(INSTANCE_ID), anyMap())).thenReturn(
				processInstance);

		processService.startProcess(instance, new HashMap<>());

		verify(repositoryService).createProcessDefinitionQuery();
		verify(runtimeService).startProcessInstanceById(eq(PROCESS_DEF_ID), eq(INSTANCE_ID), anyMap());
		verify(instance).add(eq(ACTIVITY_ID), eq(processInstanceId));

	}

	@Test(expected = CamundaIntegrationException.class)
	public void testStartProcessSuspended() throws Exception {
		Instance instance = mockInstance(null, false);
		ProcessDefinition processDefinition = mockProcessDefinition();
		when(processDefinition.isSuspended()).thenReturn(Boolean.TRUE);

		processService.startProcess(instance, new HashMap<>());
	}

	@Test(expected = CamundaIntegrationException.class)
	public void testStartProcessWithErrorInEngine() throws Exception {
		Instance instance = mockInstance(null, false);
		mockProcessDefinition();
		when(instance.getOrCreateProperties()).thenReturn(new HashMap<>());
		StartFormData form = mock(StartFormData.class);
		// without filtering
		when(form.getFormFields()).thenReturn(Collections.emptyList());
		when(formService.getStartFormData(eq(PROCESS_DEF_ID))).thenReturn(form);
		ProcessInstance processInstance = mock(ProcessInstance.class);
		String processInstanceId = UUID.randomUUID().toString();
		when(processInstance.getId()).thenReturn(processInstanceId);
		doThrow(ProcessEngineException.class).when(runtimeService)
				.startProcessInstanceById(eq(PROCESS_DEF_ID), eq(INSTANCE_ID), anyMap());

		processService.startProcess(instance, new HashMap<>());
	}

	@Test(expected = CamundaIntegrationException.class)
	public void testStartProcessWithMultipleDefinitions() throws Exception {
		Instance instance = mockInstance(null, false);

		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		List<ProcessDefinition> list = new LinkedList<>();
		list.add(mock(ProcessDefinition.class));
		list.add(mock(ProcessDefinition.class));
		when(processDefinitionQuery.list()).thenReturn(list);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.processDefinitionKey(eq(PROCESS_DEF_KEY))).thenReturn(processDefinitionQuery);
		when(instance.getOrCreateProperties()).thenReturn(new HashMap<>());
		StartFormData form = mock(StartFormData.class);
		// without filtering
		when(form.getFormFields()).thenReturn(Collections.emptyList());
		when(formService.getStartFormData(eq(PROCESS_DEF_ID))).thenReturn(form);
		ProcessInstance processInstance = mock(ProcessInstance.class);
		String processInstanceId = UUID.randomUUID().toString();
		when(processInstance.getId()).thenReturn(processInstanceId);
		doThrow(ProcessEngineException.class).when(runtimeService)
				.startProcessInstanceById(eq(PROCESS_DEF_ID), eq(INSTANCE_ID), anyMap());

		processService.startProcess(instance, new HashMap<>());
	}

	private ProcessDefinition mockProcessDefinition() {
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		ProcessDefinition processDefinition = mock(ProcessDefinition.class);
		when(processDefinition.isSuspended()).thenReturn(Boolean.FALSE);
		when(processDefinition.getId()).thenReturn(PROCESS_DEF_ID);

		when(processDefinitionQuery.count()).thenReturn(Long.valueOf(1));
		when(processDefinitionQuery.singleResult()).thenReturn(processDefinition);
		when(processDefinitionQuery.list()).thenReturn(Collections.singletonList(processDefinition));
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.processDefinitionKey(eq(PROCESS_DEF_KEY))).thenReturn(processDefinitionQuery);
		return processDefinition;
	}

	private static Instance mockInstance(String taskId, boolean isWorkflowInstance) {
		Instance activity = mock(Instance.class);
		when(activity.getId()).thenReturn(INSTANCE_ID);
		if (taskId != null) {
			when(activity.isValueNotNull(ProcessConstants.ACTIVITY_ID)).thenReturn(true);
			when(activity.getAsString(ProcessConstants.ACTIVITY_ID)).thenReturn(taskId);
		}
		when(activity.getIdentifier()).thenReturn(PROCESS_DEF_KEY);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is(eq("workflowinstancecontext"))).thenReturn(isWorkflowInstance);
		when(activity.type()).thenReturn(instanceType);
		InstanceReference reference = mock(InstanceReference.class);
		when(activity.toReference()).thenReturn(reference);
		return activity;
	}

	@Test(expected = NullPointerException.class)
	public void testCancelProcessWithError() throws Exception {
		processService.cancelProcess(mockInstance(null, false));
	}

	@Test(expected = CamundaIntegrationException.class)
	public void testCancelProcessWithErrorInEngine() throws Exception {
		Instance activityInstance = mockInstance(INSTANCE_ID, true);
		when(activityInstance.getAsString(eq("stopReason"))).thenReturn("test");
		doThrow(AuthorizationException.class).when(runtimeService).deleteProcessInstance(eq(INSTANCE_ID), eq("test"));
		processService.cancelProcess(activityInstance);
	}

	@Test
	public void testCancelProcess() throws Exception {
		Instance activityInstance = mockInstance(ACTIVITY_ID, true);
		when(activityInstance.getAsString(eq("stopReason"))).thenReturn("test");
		processService.cancelProcess(activityInstance);
		verify(runtimeService).deleteProcessInstance(eq(ACTIVITY_ID), eq("test"));
		verify(domainInstanceService).save(any(InstanceSaveContext.class));
	}

	@Test
	public void testCancelProcess_with_non_workflow_instance() throws Exception {
		Instance activityInstance = mockInstance(ACTIVITY_ID, false);
		when(activityInstance.getAsString(eq("stopReason"))).thenReturn("test");
		processService.cancelProcess(activityInstance);
		verify(runtimeService, never()).deleteProcessInstance(eq(ACTIVITY_ID), eq("test"));
		verify(domainInstanceService, never()).save(any(InstanceSaveContext.class));
	}

	@Test
	public void test_transition_with_missing_activity_id() {
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.FALSE);
		assertNull(processService.transition(instance, null, null, null));
	}

	@Test
	public void test_Transition() throws Exception {
		Instance workflowInstance = mockInstance(INSTANCE_ID, false);

		TaskFormData form = mock(TaskFormData.class);
		// without filtering
		when(form.getFormFields()).thenReturn(Collections.emptyList());
		when(formService.getTaskFormData(eq(INSTANCE_ID))).thenReturn(form);
		Map<String, Serializable> data = new HashMap<>();
		TaskQuery taskQuery = mock(TaskQuery.class);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn("processInstanceId");
		when(task.getExecutionId()).thenReturn("executionId");
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		when(taskQuery.taskId(eq(INSTANCE_ID))).thenReturn(taskQuery);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);

		Instance transition = processService.transition(workflowInstance, "approve", workflowInstance.getProperties(),
				data);
		verify(taskService).complete(eq(INSTANCE_ID));
		verify(runtimeService).setVariablesLocal(eq("executionId"), anyMap());
		verify(runtimeService).setVariables(eq("processInstanceId"), anyMap());
		assertNotNull(transition);

	}

	@Test
	public void testListActiveTasks() {
		TaskQuery taskQuery = mock(TaskQuery.class);
		Task mock = mock(Task.class);
		when(mock.getId()).thenReturn("mytaskid");
		List<Task> tasks = Collections.singletonList(mock);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(taskQuery.processInstanceId(eq(PROCESS_ID))).thenReturn(taskQuery);
		when(taskQuery.active()).thenReturn(taskQuery);
		when(taskQuery.list()).thenReturn(tasks);
		Collection<String> listActiveTasks = processService.listActiveTasksIds(PROCESS_ID);
		verify(taskQuery).active();
		verify(taskQuery).list();
		assertEquals(1, listActiveTasks.size());
		assertEquals("mytaskid", listActiveTasks.iterator().next());
	}

	@Test
	public void testGetProcessInstanceIdByActivity() {
		Instance instance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		String processId = processService.getProcessInstanceId(instance);
		assertEquals(PROCESS_ID, processId);
	}

	private TaskQuery mockTaskQuery(Task task, String taskId) {
		TaskQuery taskQuery = mock(TaskQuery.class);
		when(taskQuery.taskId(eq(taskId))).thenReturn(taskQuery);
		when(taskQuery.singleResult()).thenReturn(task);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(processEngine.getTaskService()).thenReturn(taskService);
		return taskQuery;
	}

	@Test
	public void test_GetBpmnModelInstance() {
		Instance instance = mockInstance(PROCESS_ID, false);
		ProcessDefinition processDefinition = mockProcessDefinition();
		processService.getBpmnModelInstance(instance);
		verify(repositoryService).getBpmnModelInstance(eq(processDefinition.getId()));
	}

	@Test
	public void test_getBpmnModelInstance_null_instance() {
		Instance instance = null;
		assertNull(processService.getBpmnModelInstance(instance));
		String instanceId = null;
		assertNull(processService.getBpmnModelInstance(instanceId));
	}

	@Test
	public void test_getBpmnModelInstance_not_found() {
		Instance instance = mockInstance(PROCESS_ID, false);
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class);
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.withoutTenantId()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.latestVersion()).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.processDefinitionKey(eq(PROCESS_DEF_KEY))).thenReturn(processDefinitionQuery);
		when(processDefinitionQuery.list()).thenReturn(Collections.emptyList());
		assertNull(processService.getBpmnModelInstance(instance));
	}

	@Test
	public void test_getProcessInstance_not_activity_instance() {
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.FALSE);
		assertNull(processService.getProcessInstance(instance));
	}

	@Test
	public void test_getProcessInstanceId_by_processInstance() {
		Instance instance = mockInstance(PROCESS_ID, true);
		assertEquals(PROCESS_ID, processService.getProcessInstanceId(instance));
	}

	@Test
	public void testGetExecutionEntity() {

		Instance activity = mockInstance(INSTANCE_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, INSTANCE_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		when(taskService.createTaskQuery()).thenReturn(taskQuery);

		ProcessInstanceQuery execQuery = mock(ProcessInstanceQuery.class);
		ProcessInstance process = mock(ProcessInstance.class);
		when(execQuery.processInstanceId(eq(PROCESS_ID))).thenReturn(execQuery);
		when(runtimeService.createProcessInstanceQuery()).thenReturn(execQuery);
		when(execQuery.list()).thenReturn(Collections.singletonList(process));
		ProcessInstance processInstance = processService.getProcessInstance(activity);
		assertNotNull(processInstance);

	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testGetExecutionEntityWithError() {

		Instance activity = mockInstance(INSTANCE_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, INSTANCE_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		when(taskService.createTaskQuery()).thenReturn(taskQuery);

		ProcessInstanceQuery execQuery = mock(ProcessInstanceQuery.class);
		when(execQuery.processInstanceId(eq(PROCESS_ID))).thenReturn(execQuery);
		when(runtimeService.createProcessInstanceQuery()).thenReturn(execQuery);

		List<ProcessInstance> multiReturn = new LinkedList<>();
		multiReturn.add(mock(ProcessInstance.class));
		multiReturn.add(mock(ProcessInstance.class));
		when(execQuery.list()).thenReturn(multiReturn);
		processService.getProcessInstance(activity);
	}

	@Test
	public void isTaskClaimableTest_not_a_claimable_task() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		Assert.assertFalse(processService.isTaskClaimable(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskClaimableTest_is_claimable_task() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		IdentityLink link = mock(IdentityLink.class);
		when(link.getType()).thenReturn(IdentityLinkType.CANDIDATE);
		when(link.getUserId()).thenReturn(EMF_USER_ID);
		when(taskService.getIdentityLinksForTask(TASK_ID)).thenReturn(Arrays.asList(link));
		Assert.assertTrue(processService.isTaskClaimable(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskClaimableTest_task_have_assignee() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(task.getAssignee()).thenReturn(EMF_USER_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		Assert.assertFalse(processService.isTaskClaimable(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskClaimableTest_not_an_activity_task() {
		Instance taskInstance = mockInstance(null, false);
		Assert.assertFalse(processService.isTaskClaimable(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskClaimableTest_a_process_instance() {
		Instance taskInstance = mockInstance(TASK_ID, true);
		Assert.assertFalse(processService.isTaskClaimable(taskInstance, EMF_USER_ID));
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void isTaskClaimableTest_no_task_found() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.emptyList());
		processService.isTaskClaimable(taskInstance, EMF_USER_ID);
		Assert.fail("Should not find a task!!!");
	}

	@Test
	public void isTaskAssigneeTest_not_an_activity_task() {
		Instance taskInstance = mockInstance(null, false);
		Assert.assertFalse(processService.isTaskAssignee(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskAssigneeTest_a_process_instance() {
		Instance taskInstance = mockInstance(TASK_ID, true);
		Assert.assertFalse(processService.isTaskAssignee(taskInstance, EMF_USER_ID));
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void isTaskAssigneeTest_no_task_found() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.emptyList());
		processService.isTaskAssignee(taskInstance, EMF_USER_ID);
		Assert.fail("Should not find a task!!!");
	}

	@Test
	public void isTaskAssigneeTest_successfull() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(task.getAssignee()).thenReturn(EMF_USER_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		Assert.assertTrue(processService.isTaskAssignee(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskAssigneeTest_no_task_assignee() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(task.getAssignee()).thenReturn(null);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		Assert.assertFalse(processService.isTaskAssignee(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskAssigneeTest_diff_task_assignee() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(task.getAssignee()).thenReturn("emf:otherUserId");
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		Assert.assertFalse(processService.isTaskAssignee(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskReleasableTest_not_an_activity_task() {
		Instance taskInstance = mockInstance(null, false);
		Assert.assertFalse(processService.isTaskReleasable(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskReleasableTest_a_process_instance() {
		Instance taskInstance = mockInstance(TASK_ID, true);
		Assert.assertFalse(processService.isTaskReleasable(taskInstance, EMF_USER_ID));
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void isTaskReleasableTest_no_task_found() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.emptyList());
		processService.isTaskReleasable(taskInstance, EMF_USER_ID);
		Assert.fail("Should not find a task!!!");
	}

	@Test
	public void isTaskReleasableTest_successfull() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(task.getAssignee()).thenReturn(EMF_USER_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		IdentityLink link = mock(IdentityLink.class);
		when(link.getType()).thenReturn(IdentityLinkType.CANDIDATE);
		when(link.getUserId()).thenReturn(EMF_USER_ID);
		when(taskService.getIdentityLinksForTask(TASK_ID)).thenReturn(Arrays.asList(link));
		Assert.assertTrue(processService.isTaskReleasable(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskReleasableTest_no_task_assignee() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(task.getAssignee()).thenReturn(null);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		Assert.assertFalse(processService.isTaskReleasable(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskReleasableTest_diff_task_assignee() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(task.getAssignee()).thenReturn("emf:otherUserId");
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		Assert.assertFalse(processService.isTaskReleasable(taskInstance, EMF_USER_ID));
	}

	@Test
	public void isTaskPooledTest_not_an_activity_task() {
		Instance taskInstance = mockInstance(null, false);
		Assert.assertFalse(processService.isTaskPooled(taskInstance));
	}

	@Test
	public void isTaskPooledTest_a_process_instance() {
		Instance taskInstance = mockInstance(TASK_ID, true);
		Assert.assertFalse(processService.isTaskPooled(taskInstance));
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void isTaskPooledTest_no_task_found() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.emptyList());
		processService.isTaskPooled(taskInstance);
		Assert.fail("Should not find a task!!!");
	}

	@Test
	public void isTaskPooledTest_true() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		IdentityLink link = mock(IdentityLink.class);
		when(link.getType()).thenReturn(IdentityLinkType.CANDIDATE);
		when(taskService.getIdentityLinksForTask(TASK_ID)).thenReturn(Arrays.asList(link));
		Assert.assertTrue(processService.isTaskPooled(taskInstance));
	}

	@Test
	public void isTaskPooledTest_false() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		IdentityLink link = mock(IdentityLink.class);
		when(link.getType()).thenReturn(IdentityLinkType.OWNER);
		when(taskService.getIdentityLinksForTask(TASK_ID)).thenReturn(Arrays.asList(link));
		Assert.assertFalse(processService.isTaskPooled(taskInstance));
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void claimTaskTest_no_task_found() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.emptyList());
		processService.claimTask(taskInstance, EMF_USER_ID);
		Assert.fail("Should not find a task!!!");
	}

	@Test
	public void claimTaskTest_successfull() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		processService.claimTask(taskInstance, EMF_USER_ID);
		verify(processEngine.getTaskService()).claim(TASK_ID, EMF_USER_ID);
		verify(processEngine.getTaskService()).setVariable(TASK_ID, BPMTaskProperties.TASK_ASSIGNEE, EMF_USER_ID);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void releaseTaskTest_no_task_found() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.emptyList());
		processService.releaseTask(taskInstance);
		Assert.fail("Should not find a task!!!");
	}

	@Test
	public void releaseTaskTest_successfull() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		processService.releaseTask(taskInstance);
		verify(processEngine.getTaskService()).setAssignee(TASK_ID, null);
		verify(processEngine.getTaskService()).removeVariable(TASK_ID, BPMTaskProperties.TASK_ASSIGNEE);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void reassignTaskTest_no_task_found() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.emptyList());
		processService.reassignTask(taskInstance, EMF_USER_ID);
		Assert.fail("Should not find a task!!!");
	}

	@Test
	public void should_reassign_pooledTask_no_new_identityLink() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		IdentityLink link = mock(IdentityLink.class);
		when(link.getType()).thenReturn(IdentityLinkType.CANDIDATE);
		when(link.getUserId()).thenReturn(EMF_USER_ID);
		when(taskService.getIdentityLinksForTask(TASK_ID)).thenReturn(Arrays.asList(link));
		when(taskService.getVariable(TASK_ID, BPMTaskProperties.TASK_POOL_ASSIGNEES)).thenReturn(
				new ArrayList<String>());
		processService.reassignTask(taskInstance, EMF_USER_ID);
		verify(taskService).setAssignee(TASK_ID, EMF_USER_ID);
		verify(taskService).setVariable(TASK_ID, BPMTaskProperties.TASK_ASSIGNEE, EMF_USER_ID);
		verify(taskService, never()).addUserIdentityLink(TASK_ID, EMF_USER_ID, IdentityLinkType.CANDIDATE);
	}

	@Test
	public void should_reassign_pooled_task_add_new_candidate() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		IdentityLink link = mock(IdentityLink.class);
		when(link.getType()).thenReturn(IdentityLinkType.CANDIDATE);
		when(link.getUserId()).thenReturn("emf:userId2");
		when(taskService.getIdentityLinksForTask(TASK_ID)).thenReturn(Arrays.asList(link));
		when(taskService.getVariable(TASK_ID, BPMTaskProperties.TASK_POOL_ASSIGNEES)).thenReturn(
				new ArrayList<String>());
		processService.reassignTask(taskInstance, EMF_USER_ID);
		verify(taskService).setAssignee(TASK_ID, EMF_USER_ID);
		verify(taskService).setVariable(TASK_ID, BPMTaskProperties.TASK_ASSIGNEE, EMF_USER_ID);
		verify(taskService).addUserIdentityLink(TASK_ID, EMF_USER_ID, IdentityLinkType.CANDIDATE);
	}

	@Test
	public void should_reassign_but_not_pooled_task() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		processService.reassignTask(taskInstance, EMF_USER_ID);
		verify(taskService).setAssignee(TASK_ID, EMF_USER_ID);
		verify(taskService).setVariable(eq(TASK_ID), eq(BPMTaskProperties.TASK_ASSIGNEE), eq(EMF_USER_ID));
		verify(domainInstanceService, never()).save(any(InstanceSaveContext.class));
	}

	@Test
	public void should_reassign_pooled_task_add_new_candidate_and_update_pool() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		IdentityLink identityLink = mock(IdentityLink.class);
		when(identityLink.getUserId()).thenReturn(EMF_USER_ID + 2);
		when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
		List<IdentityLink> identityLinks = new ArrayList<>(1);
		identityLinks.add(identityLink);
		when(taskService.getIdentityLinksForTask(TASK_ID)).thenReturn(identityLinks);
		ArrayList<String> poolAssignees = new ArrayList(1);
		poolAssignees.add(EMF_USER_ID + 2);
		when(taskInstance.get(BPMTaskProperties.TASK_POOL_ASSIGNEES)).thenReturn(poolAssignees);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		processService.reassignTask(taskInstance, EMF_USER_ID);
		verify(taskService).setAssignee(TASK_ID, EMF_USER_ID);
		verify(taskService).setVariable(eq(TASK_ID), eq(BPMTaskProperties.TASK_POOL_ASSIGNEES),
				argThat(CustomMatcher.of((List newPoolAssignees) -> {
					Assert.assertTrue(newPoolAssignees.size() == 2);
					verify(processEngine.getTaskService()).setVariable(eq(TASK_ID), eq(BPMTaskProperties.TASK_ASSIGNEE),
							eq(EMF_USER_ID));

				})));
		verify(domainInstanceService).save(any(InstanceSaveContext.class));
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void should_reassign_pooled_task_and_throw_exception_on_save() {
		Instance taskInstance = mockInstance(TASK_ID, false);
		IdentityLink identityLink = mock(IdentityLink.class);
		when(identityLink.getUserId()).thenReturn(EMF_USER_ID + 2);
		when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
		List<IdentityLink> identityLinks = new ArrayList<>(1);
		identityLinks.add(identityLink);
		when(taskService.getIdentityLinksForTask(TASK_ID)).thenReturn(identityLinks);
		ArrayList<String> poolAssignees = new ArrayList(1);
		poolAssignees.add(EMF_USER_ID + 2);
		when(taskInstance.get(BPMTaskProperties.TASK_POOL_ASSIGNEES)).thenReturn(poolAssignees);
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);
		TaskQuery taskQuery = mockTaskQuery(task, TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		when(domainInstanceService.save(any(InstanceSaveContext.class))).thenThrow(new EmfRuntimeException("Test"));
		processService.reassignTask(taskInstance, EMF_USER_ID);
	}

	@Test
	public void test_should_notify_signal() {
		EventSubscription eventSubscription = mock(EventSubscription.class);
		when(eventSubscription.getExecutionId()).thenReturn(PROCESS_ID);
		when(eventSubscription.getEventType()).thenReturn("signal");
		when(eventSubscription.getEventName()).thenReturn("testEventName");
		EventSubscriptionQuery eventSubscriptionQuery = mock(EventSubscriptionQuery.class);
		when(runtimeService.createEventSubscriptionQuery()).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.eventName("testEventName")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.list()).thenReturn(Collections.singletonList(eventSubscription));
		processService.notify("testEventName", null);
		verify(runtimeService).signalEventReceived("testEventName", PROCESS_ID, null);
	}

	@Test
	public void test_should_notify_message() {
		EventSubscription eventSubscription = mock(EventSubscription.class);
		when(eventSubscription.getExecutionId()).thenReturn(PROCESS_ID);
		when(eventSubscription.getEventType()).thenReturn("message");
		when(eventSubscription.getEventName()).thenReturn("testEventName");

		EventSubscriptionQuery eventSubscriptionQuery = mock(EventSubscriptionQuery.class);

		when(runtimeService.createEventSubscriptionQuery()).thenReturn(eventSubscriptionQuery);

		when(eventSubscriptionQuery.executionId(PROCESS_ID)).thenReturn(eventSubscriptionQuery);

		when(eventSubscriptionQuery.eventName("testEventName")).thenReturn(eventSubscriptionQuery);

		when(eventSubscriptionQuery.list()).thenReturn(Collections.singletonList(eventSubscription));

		processService.notify("testEventName", PROCESS_ID, null);
		verify(runtimeService).messageEventReceived("testEventName", PROCESS_ID, null);
	}

}
