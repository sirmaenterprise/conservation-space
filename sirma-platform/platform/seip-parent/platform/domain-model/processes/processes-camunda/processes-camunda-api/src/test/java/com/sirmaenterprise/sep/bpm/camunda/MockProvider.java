package com.sirmaenterprise.sep.bpm.camunda;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.mockito.Mockito;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;

/**
 * @author bbanchev
 */
public class MockProvider {
	/** Default engine name for tests. */
	public static final String DEFAULT_ENGINE = "camunda_test";

	private MockProvider() {
	}

	public static ProcessEngine mockProcessEngine(String engineName) {
		ProcessEngine engine = Mockito.mock(ProcessEngine.class);
		when(engine.getName()).thenReturn(engineName);
		return engine;
	}

	public static <T extends DelegateExecution> T mockDelegateExecution(String engineName, Class<T> clz) {
		T delegateExecution = Mockito.mock(clz);
		String activityId = UUID.randomUUID().toString();
		when(delegateExecution.getCurrentActivityId()).thenReturn(activityId);
		when(delegateExecution.getCurrentTransitionId()).thenReturn("complete");
		String execId = UUID.randomUUID().toString();
		when(delegateExecution.getId()).thenReturn(execId);
		ProcessEngine processEngine = mockProcessEngine(engineName);
		when(delegateExecution.getProcessEngineServices()).thenReturn(processEngine);
		VariableMapImpl variableMap = mock(VariableMapImpl.class);
		when(delegateExecution.getVariablesTyped()).thenReturn(variableMap);
		return delegateExecution;
	}

	public static ActivityExecution mockActivity(ActivityExecution delegateExecution) {
		ActivityImpl activity = Mockito.mock(ActivityImpl.class);
		ActivityBehavior behavior = Mockito.mock(TaskActivityBehavior.class);
		when(activity.getActivityBehavior()).thenReturn(behavior);
		when(delegateExecution.getActivity()).thenReturn(activity);
		return delegateExecution;
	}

	public static DelegateTask mockDelegateTask(DelegateExecution delegateExecution) {
		DelegateTask delegateTask = Mockito.mock(DelegateTask.class);
		when(delegateTask.getExecution()).thenReturn(delegateExecution);
		String id = delegateExecution.getId();
		when(delegateTask.getExecutionId()).thenReturn(id);
		when(delegateTask.getVariables()).thenReturn(new HashMap<>());
		when(delegateTask.getVariablesLocal()).thenReturn(new HashMap<>());
		when(delegateTask.getVariablesLocalTyped()).thenReturn(new VariableMapImpl());
		when(delegateTask.getVariablesTyped()).thenReturn(new VariableMapImpl());
		ProcessEngineServices processEngineServices = delegateExecution.getProcessEngineServices();
		when(delegateTask.getProcessEngineServices()).thenReturn(processEngineServices);
		String taskId = UUID.randomUUID().toString();
		when(delegateTask.getId()).thenReturn(taskId);
		return delegateTask;
	}

	public static Instance mockWorkflowInstance(String id) {
		Instance process = mock(Instance.class);
		when(process.getId()).thenReturn(id);
		InstanceType instanceType = mock(InstanceType.class);
		when(process.isValueNotNull(ProcessConstants.ACTIVITY_ID)).thenReturn(true);
		when(process.getAsString(ProcessConstants.ACTIVITY_ID)).thenReturn(id);
		when(instanceType.is("workflowinstancecontext")).thenReturn(Boolean.TRUE);
		when(process.type()).thenReturn(instanceType);
		return process;
	}
}
