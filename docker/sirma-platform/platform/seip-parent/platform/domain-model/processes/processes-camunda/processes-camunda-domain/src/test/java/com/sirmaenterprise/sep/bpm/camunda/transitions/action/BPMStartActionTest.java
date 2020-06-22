package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelInstanceImpl;
import org.camunda.bpm.model.bpmn.impl.BpmnParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.verification.AtMost;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.camunda.transitions.model.TransitionModelService;

/**
 * Test for {@link BPMStartAction}.
 *
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMStartActionTest {
	private final String processId = "emf:targetId";
	@Mock
	private InstanceService instanceService;
	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private ProcessService processService;
	@Mock
	private CamundaBPMNService camundaBPMNService;
	@Mock
	private BPMPropertiesConverter propertiesConverter;
	@Mock
	private TransitionModelService transitionModelService;
	@Mock
	private LockService lockService;
	@Spy
	private TransactionSupportFake transactionSupport;
	@InjectMocks
	private BPMStartAction bPMStartAction;

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.transitions.action.BPMStartAction#executeBPMAction(BPMTransitionRequest)}
	 * .
	 */
	@Test
	public void testExecuteBpmOperationNotStarted() throws Exception {
		BPMTransitionRequest request = buildStartRequest();
		Instance instance = request.getTransitionData().get(processId);
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.TRUE);
		when(transitionModelService.createTransitionConditionFilter(instance)).thenReturn(entry -> true);
		bPMStartAction.perform(request);
		Map<String, Serializable> transitionData = new HashMap<>();
		transitionData.put("Task1",
				(Serializable) request.getTransitionData().get("activity1").getOrCreateProperties());
		verify(processService).startProcess(eq(instance), eq(transitionData));
		verify(domainInstanceService, new AtMost(0)).save(any(InstanceSaveContext.class));
	}

	@Test
	public void testExecuteBpmOperationStarted() throws Exception {
		BPMTransitionRequest request = buildStartRequest();
		Instance process = request.getTransitionData().get(processId);
		when(transitionModelService.createTransitionConditionFilter(process)).thenReturn(entry -> true);
		when(lockService.isAllowedToModify(any(InstanceReference.class))).thenReturn(Boolean.TRUE);
		when(processService.startProcess(any(), any())).thenReturn(process);
		bPMStartAction.perform(request);
		Map<String, Serializable> transitionData = new HashMap<>();
		transitionData.put("Task1",
				(Serializable) request.getTransitionData().get("activity1").getOrCreateProperties());

		verify(processService).startProcess(eq(process), eq(transitionData));
		verify(domainInstanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			assertEquals(process, context.getInstance());
		})));
		verify(processService).startProcess(eq(process), eq(transitionData));
	}

	private BPMTransitionRequest buildStartRequest() throws CamundaIntegrationException, IOException {
		BPMTransitionRequest request = buildWorkingRequest();
		request.setUserOperation("id2");
		Instance instance = request.getTransitionData().get(processId);

		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("transitions1.bpmn")) {
			BpmnModelInstanceImpl parseModelFromStream = new BpmnParser().parseModelFromStream(in);
			when(camundaBPMNService.getBpmnModelInstance(eq(instance))).thenReturn(parseModelFromStream);
		}

		Instance nextActivity = buildInstance("activity1");
		when(nextActivity.getIdentifier()).thenReturn("Task1");
		request.getTransitionData().put("activity1", nextActivity);
		Map<String, Serializable> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		when(instance.getOrCreateProperties()).thenReturn(map);
		ProcessInstance processInstance = mock(ProcessInstance.class);
		when(processInstance.getBusinessKey()).thenReturn(processId);
		when(camundaBPMNService.getProcessInstance(eq(instance))).thenReturn(processInstance);
		return request;
	}

	private BPMStartRequest buildWorkingRequest() {
		String targetId = processId;
		BPMStartRequest request = new BPMStartRequest();
		request.setTargetId(targetId);
		request.setTransitionData(new HashMap<>());
		Instance instance = buildInstance(targetId);
		request.getTransitionData().put(targetId, instance);
		return request;
	}

	private Instance buildInstance(String targetId) {
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(targetId);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("assignee", "assigneeval");
		properties.put("date", new Date());
		properties.put("date1", new Date());
		when(instance.getOrCreateProperties()).thenReturn(properties);
		InstanceReference instanceRef = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is("workflowinstancecontext")).thenReturn(Boolean.TRUE);
		when(instance.type()).thenReturn(instanceType);
		when(instance.toReference()).thenReturn(instanceRef);
		when(instanceRef.toInstance()).thenReturn(instance);
		when(instanceRef.getId()).thenReturn(targetId);
		when(domainInstanceService.loadInstance(eq(targetId))).thenReturn(instance);
		return instance;
	}

}
