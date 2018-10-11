package com.sirmaenterprise.sep.bpm.camunda.service;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockWorkflowInstance;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;

/**
 * Test for {@link CamundaBPMServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CamundaBPMServiceImplTest {

	private static final String PROCESS_ID = "processId";
	private static final String TASK_ID = "taskId";

	@Mock
	private HistoryService historyService;
	@Mock
	private TaskService taskService;
	@Mock
	private RuntimeService runtimeService;
	@Mock
	private ProcessEngine processEngine;
	@InjectMocks
	private CamundaBPMServiceImpl camundaBPMServiceImpl;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(processEngine.getName()).thenReturn(DEFAULT_ENGINE);
		when(processEngine.getHistoryService()).thenReturn(historyService);
		when(processEngine.getTaskService()).thenReturn(taskService);
	}

	@Test
	public void loadTask() {
		Instance instance = mock(Instance.class);
		// not activity
		assertFalse(camundaBPMServiceImpl.loadTask(instance).isPresent());
		// now check activity task
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		when(instance.getAsString(ACTIVITY_ID)).thenReturn(TASK_ID);
		Task task = mock(Task.class);
		TaskQuery taskQuery = mockTaskQuery(task, Collections.singletonList(task), TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		Optional<Task> loadTask = camundaBPMServiceImpl.loadTask(instance);
		assertTrue(loadTask.isPresent());
		assertNotNull(loadTask.get());
		assertEquals(task, loadTask.get());

		// no retries verification
		verify(taskService).createTaskQuery();
		verify(taskQuery).list();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void loadTaskWithRetry() {
		Task task = mock(Task.class);
		TaskQuery taskQuery = mockTaskQuery(task, Collections.singletonList(task), TASK_ID);

		// the number of the retries is controlled in thenReturn, in this case 2
		when(taskQuery.list()).thenReturn(Collections.emptyList(), Collections.singletonList(task));
		
		Instance instance = new EmfInstance();
		instance.add(ACTIVITY_ID, TASK_ID);
		Optional<Task> loadTask = camundaBPMServiceImpl.loadTask(instance);
		assertTrue(loadTask.isPresent());

		// verify the number of retries
		verify(taskService, times(2)).createTaskQuery();
		verify(taskQuery, times(2)).list();
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void loadTaskInvalidId() {
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		when(instance.getAsString(ACTIVITY_ID)).thenReturn(TASK_ID);
		Task task = mock(Task.class);
		TaskQuery taskQuery = mockTaskQuery(task, Collections.singletonList(task), TASK_ID);
		List<Task> arrayList = new ArrayList<>();
		arrayList.add(task);
		arrayList.add(task);
		when(taskQuery.list()).thenReturn(arrayList);
		when(processEngine.getTaskService()).thenReturn(taskService);
		camundaBPMServiceImpl.loadTask(instance);
	}

	private TaskQuery mockTaskQuery(Task task, List<Task> tasks, String taskId) {
		TaskQuery taskQuery = mock(TaskQuery.class);
		when(taskQuery.taskId(eq(taskId))).thenReturn(taskQuery);
		when(taskQuery.singleResult()).thenReturn(task);
		when(taskQuery.list()).thenReturn(tasks);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(processEngine.getTaskService()).thenReturn(taskService);
		return taskQuery;
	}

	private HistoricTaskInstanceQuery mockHistoricQuery(HistoricTaskInstance task, List<HistoricTaskInstance> tasks,
			String taskId) {
		HistoricTaskInstanceQuery taskQuery = mock(HistoricTaskInstanceQuery.class);
		when(taskQuery.taskId(eq(taskId))).thenReturn(taskQuery);
		when(taskQuery.singleResult()).thenReturn(task);
		when(taskQuery.list()).thenReturn(tasks);
		when(historyService.createHistoricTaskInstanceQuery()).thenReturn(taskQuery);
		when(processEngine.getHistoryService()).thenReturn(historyService);
		return taskQuery;
	}

	@Test
	public void getActivityDetailsOnNonActivity() {
		Optional<ActivityDetails> activityDetails = camundaBPMServiceImpl.getActivityDetails(null);
		assertFalse(activityDetails.isPresent());
		Instance instance = mock(Instance.class);
		activityDetails = camundaBPMServiceImpl.getActivityDetails(instance);
		assertFalse(activityDetails.isPresent());
		instance = mockWorkflowInstance(PROCESS_ID);
		ExecutionEntity process = mock(ExecutionEntity.class);
		mockProcessQuery(process, Collections.singletonList(process), PROCESS_ID);
		activityDetails = camundaBPMServiceImpl.getActivityDetails(instance);
		assertTrue(activityDetails.isPresent());

	}

	@Test
	public void getActivityDetailsOnActiveActivityInActiveProcess() {
		Instance instance = mock(Instance.class);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is("task")).thenReturn(Boolean.TRUE);
		when(instance.type()).thenReturn(instanceType);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		when(instance.getAsString(ProcessConstants.ACTIVITY_ID)).thenReturn(TASK_ID);
		Task task = mock(Task.class);
		when(task.getId()).thenReturn(TASK_ID);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);

		mockTaskQuery(task, Collections.singletonList(task), TASK_ID);
		mockHistoricQuery(null, Collections.emptyList(), TASK_ID);
		ExecutionEntity process = mock(ExecutionEntity.class);
		when(process.getBusinessKey()).thenReturn("emf:id");
		mockProcessQuery(process, Collections.singletonList(process), PROCESS_ID);
		ActivityDetails activityDetails = camundaBPMServiceImpl.getActivityDetails(instance).get();
		assertEquals("emf:id", activityDetails.getCamundaProcessBusinessId());
		assertEquals(TASK_ID, activityDetails.getCamundaActivityId());
		assertEquals(instance, activityDetails.getActivity());
		assertTrue(activityDetails.isActive());

	}

	@Test
	public void getActivityDetailsOnInactiveActivityInActiveProcess() {
		Instance instance = mock(Instance.class);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is("task")).thenReturn(Boolean.TRUE);
		when(instance.type()).thenReturn(instanceType);
		when(instance.getAsString(ProcessConstants.ACTIVITY_ID)).thenReturn(TASK_ID);
		when(instance.isValueNotNull(ProcessConstants.ACTIVITY_ID)).thenReturn(true);
		HistoricTaskInstance task = mock(HistoricTaskInstance.class);
		when(task.getId()).thenReturn(TASK_ID);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);

		mockTaskQuery(null, Collections.emptyList(), TASK_ID);
		mockHistoricQuery(task, Collections.singletonList(task), TASK_ID);
		ExecutionEntity process = mock(ExecutionEntity.class);
		when(process.getBusinessKey()).thenReturn("emf:id");
		mockProcessQuery(process, Collections.singletonList(process), PROCESS_ID);
		ActivityDetails activityDetails = camundaBPMServiceImpl.getActivityDetails(instance).get();
		assertEquals("emf:id", activityDetails.getCamundaProcessBusinessId());
		assertEquals(TASK_ID, activityDetails.getCamundaActivityId());
		assertEquals(instance, activityDetails.getActivity());
		assertFalse(activityDetails.isActive());
	}

	@Test
	public void getActivityDetailsOnInactiveActivityInInactiveProcess() {
		Instance instance = mock(Instance.class);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is("task")).thenReturn(Boolean.TRUE);
		when(instance.type()).thenReturn(instanceType);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		when(instance.getAsString(ProcessConstants.ACTIVITY_ID)).thenReturn(TASK_ID);
		HistoricTaskInstance task = mock(HistoricTaskInstance.class);
		when(task.getId()).thenReturn(TASK_ID);
		when(task.getProcessInstanceId()).thenReturn(PROCESS_ID);

		mockTaskQuery(null, Collections.emptyList(), TASK_ID);
		mockHistoricQuery(task, Collections.singletonList(task), TASK_ID);
		HistoricProcessInstance process = mock(HistoricProcessInstance.class);
		when(process.getBusinessKey()).thenReturn("emf:id");
		mockProcessQuery(null, Collections.emptyList(), PROCESS_ID);
		mockHistoricProcessQuery(process, Collections.singletonList(process), PROCESS_ID);
		ActivityDetails activityDetails = camundaBPMServiceImpl.getActivityDetails(instance).get();
		assertEquals("emf:id", activityDetails.getCamundaProcessBusinessId());
		assertEquals(TASK_ID, activityDetails.getCamundaActivityId());
		assertEquals(instance, activityDetails.getActivity());
		assertFalse(activityDetails.isActive());
	}

	private void mockProcessQuery(ProcessInstance process, List<ProcessInstance> list, String id) {
		ProcessInstanceQuery execQuery = mock(ProcessInstanceQuery.class);
		when(execQuery.processInstanceId(eq(id))).thenReturn(execQuery);
		when(runtimeService.createProcessInstanceQuery()).thenReturn(execQuery);
		when(execQuery.singleResult()).thenReturn(process);
		when(execQuery.list()).thenReturn(list);
		when(processEngine.getRuntimeService()).thenReturn(runtimeService);
	}

	private void mockHistoricProcessQuery(HistoricProcessInstance process, List<HistoricProcessInstance> list,
			String id) {
		HistoricProcessInstanceQuery execQuery = mock(HistoricProcessInstanceQuery.class);
		when(execQuery.processInstanceId(eq(id))).thenReturn(execQuery);
		when(historyService.createHistoricProcessInstanceQuery()).thenReturn(execQuery);
		when(execQuery.singleResult()).thenReturn(process);
		when(execQuery.list()).thenReturn(list);
		when(processEngine.getRuntimeService()).thenReturn(runtimeService);
	}

	@Test
	public void getTaskFormData_NotFound_Task() {
		Instance instance = mock(Instance.class);
		assertFalse(camundaBPMServiceImpl.getTaskFormData(instance).isPresent());
	}

	@Test
	public void getTaskFormData_Successful() {
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		when(instance.getAsString(ACTIVITY_ID)).thenReturn(TASK_ID);
		Task task = mock(Task.class);
		when(task.getId()).thenReturn(TASK_ID);
		TaskQuery taskQuery = mockTaskQuery(task, Collections.singletonList(task), TASK_ID);
		when(taskQuery.list()).thenReturn(Collections.singletonList(task));
		FormService formService = mock(FormService.class);
		when(processEngine.getFormService()).thenReturn(formService);
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(formService.getTaskFormData(TASK_ID)).thenReturn(taskFormData);
		assertTrue(camundaBPMServiceImpl.getTaskFormData(instance).isPresent());
	}
}
