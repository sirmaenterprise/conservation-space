package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.internal.verification.AtMost;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.camunda.actions.BPMActionRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.camunda.transitions.model.TransitionModelService;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModelTest;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;

/**
 * Test for {@link BPMTransitionAction}.
 * 
 * @author bbanchev
 */
@SuppressWarnings("static-method")
@RunWith(MockitoJUnitRunner.class)
public class BPMTransitionActionTest {

	@Mock
	private ProcessService processService;
	@Mock
	private InstanceService instanceService;
	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private CamundaBPMNService camundaBPMNService;
	@Mock
	private BPMPropertiesConverter propertiesConverter;
	@Spy
	private TransactionSupportFake transactionSupport;
	@Mock
	private TransitionModelService transitionModelService;
	@Mock
	private LockService lockService;

	@InjectMocks
	private BPMTransitionAction bPMTransitionAction;

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testPerformInvalidRequest() {
		BPMTransitionRequest request = new BPMTransitionRequest();
		bPMTransitionAction.perform(request);
	}

	@Test
	public void testPerformWithMissingTransition() {
		BPMTransitionRequest request = buildWorkingRequest();
		request.setUserOperation("idMissing");
		bPMTransitionAction.perform(request);
		verify(processService, new AtMost(0)).transition(any(), any(), anyMap(), anyMap());
	}

	@Test
	public void testPerformSingleInstance() {
		BPMTransitionRequest request = buildWorkingRequest();
		request.setUserOperation("id1");
		Instance instance = request.getTransitionData().values().iterator().next();
		Map<String, Serializable> singletonMap = Collections.singletonMap("key1", "value1");
		when(instance.getOrCreateProperties()).thenReturn(singletonMap);
		when(processService.transition(eq(instance), eq("id1"), eq(singletonMap), anyMap())).thenReturn(instance);
		bPMTransitionAction.perform(request);
		verify(processService).transition(eq(instance), eq("id1"), eq(singletonMap), anyMap());
	}

	@Test
	public void testPerformSingleInstance_nullTransition() {
		String targetId = "emf:targetId";
		BPMTransitionRequest request = new BPMTransitionRequest();
		request.setTargetId(targetId);
		request.setTransitionData(new HashMap<>());
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(targetId);
		InstanceReference instanceRef = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(instanceRef);
		when(instanceRef.toInstance()).thenReturn(instance);
		when(instanceRef.getId()).thenReturn(targetId);
		when(lockService.isAllowedToModify(instanceRef)).thenReturn(Boolean.TRUE);

		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(instance.get(DomainProcessConstants.ACTIVITY_IN_PROCESS)).thenReturn(Boolean.TRUE);
		when(instance.getString(eq(ProcessConstants.ACTIVITY_ID))).thenReturn("taskId");
		when(instance.get(eq(DomainProcessConstants.TRANSITIONS))).thenReturn(null);
		when(instance.getAsString(eq(DomainProcessConstants.TRANSITIONS))).thenReturn(null);
		when(domainInstanceService.loadInstance(eq(targetId))).thenReturn(instance);
		request.getTransitionData().put(targetId, instance);
		request.setUserOperation("id1");
		when(transitionModelService.createTransitionConditionFilter(instance)).thenReturn(flow -> true);
		bPMTransitionAction.perform(request);
		verify(processService, Mockito.times(0)).transition(any(), any(), anyMap(), anyMap());
	}

	@Test
	public void testPerformSingleInstance_CompletedInstance() {
		String targetId = "emf:targetId";
		BPMTransitionRequest request = new BPMTransitionRequest();
		request.setTargetId(targetId);
		request.setTransitionData(new HashMap<>());
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(targetId);
		InstanceReference instanceRef = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(instanceRef);
		when(instanceRef.toInstance()).thenReturn(instance);
		when(instanceRef.getId()).thenReturn(targetId);
		when(lockService.isAllowedToModify(instanceRef)).thenReturn(Boolean.TRUE);

		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(instance.get(DomainProcessConstants.ACTIVITY_IN_PROCESS))
				.thenReturn(Boolean.TRUE)
					.thenReturn(Boolean.TRUE)
					.thenReturn(null);
		when(instance.get(DomainProcessConstants.COMPLETED_ON)).thenReturn(new Date());
		when(instance.getString(eq(ProcessConstants.ACTIVITY_ID))).thenReturn("taskId");
		when(instance.get(eq(DomainProcessConstants.TRANSITIONS)))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(instance.getAsString(eq(DomainProcessConstants.TRANSITIONS)))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(domainInstanceService.loadInstance(eq(targetId))).thenReturn(instance);
		request.getTransitionData().put(targetId, instance);
		request.setUserOperation("id1");
		when(transitionModelService.createTransitionConditionFilter(instance)).thenReturn(flow -> true);
		bPMTransitionAction.perform(request);
		verify(processService, Mockito.times(0)).transition(any(), any(), anyMap(), anyMap());
	}

	@Test
	public void testPerformSingleInstance_inCorrectInstance() throws Exception {
		String targetId = "emf:targetId";
		BPMTransitionRequest request = new BPMTransitionRequest();
		request.setTargetId(targetId);
		request.setTransitionData(new HashMap<>());
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(targetId);
		InstanceReference instanceRef = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(instanceRef);
		when(instanceRef.toInstance()).thenReturn(instance);
		when(instanceRef.getId()).thenReturn(targetId);
		when(lockService.isAllowedToModify(instanceRef)).thenReturn(Boolean.TRUE);

		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.FALSE);
		when(instance.getString(eq(ProcessConstants.ACTIVITY_ID))).thenReturn("taskId");
		when(instance.get(eq(DomainProcessConstants.TRANSITIONS)))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(instance.getAsString(eq(DomainProcessConstants.TRANSITIONS)))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(domainInstanceService.loadInstance(eq(targetId))).thenReturn(instance);
		request.getTransitionData().put(targetId, instance);
		request.setUserOperation("id1");
		when(transitionModelService.createTransitionConditionFilter(instance)).thenReturn(flow -> true);
		bPMTransitionAction.perform(request);
		verify(processService, Mockito.times(0)).transition(any(), any(), anyMap(), anyMap());
	}

	@Test
	public void testPerformSingleInstance_NotUpdatedAfterTransition() throws Exception {
		BPMTransitionRequest request = buildWorkingRequest();
		request.setUserOperation("id1");
		Instance instance = request.getTransitionData().values().iterator().next();
		Map<String, Serializable> singletonMap = Collections.singletonMap("key1", "value1");
		when(instance.getOrCreateProperties()).thenReturn(singletonMap);
		when(processService.transition(eq(instance), eq("id1"), eq(singletonMap), anyMap())).thenReturn(null);
		bPMTransitionAction.perform(request);
		verify(processService).transition(eq(instance), eq("id1"), eq(singletonMap), anyMap());
		verify(domainInstanceService, Mockito.times(0)).save(any());
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testPerformSingleInstance_NotFoundProcessInstance() throws Exception {
		String targetId = "emf:targetId";
		BPMTransitionRequest request = new BPMTransitionRequest();
		request.setTargetId(targetId);
		request.setTransitionData(new HashMap<>());
		Instance instance = buildInstance(targetId, true);
		request.getTransitionData().put(targetId, instance);
		when(transitionModelService.createTransitionConditionFilter(instance)).thenReturn(flow -> true);
		when(camundaBPMNService.getProcessInstance(eq(instance))).thenReturn(null);
		request.setUserOperation("id1");
		Map<String, Serializable> singletonMap = Collections.singletonMap("key1", "value1");
		when(instance.getOrCreateProperties()).thenReturn(singletonMap);
		bPMTransitionAction.perform(request);
	}

	@Test(expected = BPMActionRuntimeException.class)
	public void testPerformSingleInstance_InvalidTransitionCondition() throws Exception {
		String targetId = "emf:targetId";
		BPMTransitionRequest request = new BPMTransitionRequest();
		request.setTargetId(targetId);
		request.setTransitionData(new HashMap<>());
		Instance instance = buildInstance(targetId, true);
		request.getTransitionData().put(targetId, instance);
		when(transitionModelService.createTransitionConditionFilter(instance)).thenReturn(flow -> false);
		request.setUserOperation("id1");
		bPMTransitionAction.perform(request);
	}

	@Test(expected = BPMActionRuntimeException.class)
	public void testPerformSingleInstance_LockedInstance() throws Exception {
		String targetId = "emf:targetId";
		BPMTransitionRequest request = new BPMTransitionRequest();
		request.setTargetId(targetId);
		request.setTransitionData(new HashMap<>());
		Instance instance = buildInstance(targetId, false);
		request.getTransitionData().put(targetId, instance);
		when(transitionModelService.createTransitionConditionFilter(instance)).thenReturn(flow -> true);
		request.setUserOperation("id1");
		bPMTransitionAction.perform(request);
	}

	@Test
	public void testPerformMultipleInstances() throws Exception {
		BPMTransitionRequest request = buildWorkingRequest();
		request.setUserOperation("id2");
		Instance instance = request.getTransitionData().values().iterator().next();
		Instance nextActivity = buildInstance("activity1", true);
		when(nextActivity.getIdentifier()).thenReturn("activity1");
		request.getTransitionData().put("activity1", nextActivity);
		Map<String, Serializable> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		when(instance.getOrCreateProperties()).thenReturn(map);
		when(processService.transition(eq(instance), eq("id2"), eq(map), anyMap())).thenReturn(instance);
		bPMTransitionAction.perform(request);
		verify(processService).transition(eq(instance), eq("id2"), eq(map), anyMap());
	}

	private BPMTransitionRequest buildWorkingRequest() {
		String targetId = "emf:targetId";
		BPMTransitionRequest request = new BPMTransitionRequest();
		request.setTargetId(targetId);
		request.setTransitionData(new HashMap<>());
		Instance instance = buildInstance(targetId, true);
		request.getTransitionData().put(targetId, instance);
		ProcessInstance processInstance = mock(ProcessInstance.class);
		buildInstance("emf:process", true);
		when(transitionModelService.createTransitionConditionFilter(instance)).thenReturn(flow -> true);
		when(processInstance.getBusinessKey()).thenReturn("emf:process");
		when(camundaBPMNService.getProcessInstance(eq(instance))).thenReturn(processInstance);

		return request;
	}

	private Instance buildInstance(String targetId, boolean isAllowToModify) {
		Instance instance = mock(Instance.class);
		when(instance.getId()).thenReturn(targetId);
		InstanceReference instanceRef = mock(InstanceReference.class);
		when(instance.toReference()).thenReturn(instanceRef);
		when(instanceRef.toInstance()).thenReturn(instance);
		when(instanceRef.getId()).thenReturn(targetId);
		when(lockService.isAllowedToModify(instanceRef)).thenReturn(isAllowToModify);

		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(instance.get(DomainProcessConstants.ACTIVITY_IN_PROCESS)).thenReturn(Boolean.TRUE);
		when(instance.getString(eq(ProcessConstants.ACTIVITY_ID))).thenReturn("taskId");
		when(instance.get(eq(DomainProcessConstants.TRANSITIONS)))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(instance.getAsString(eq(DomainProcessConstants.TRANSITIONS)))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(domainInstanceService.loadInstance(eq(targetId))).thenReturn(instance);
		return instance;
	}

	@Test
	public void testAddActivitiesValid() throws Exception {
		Instance instance = mock(Instance.class);
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		when(instance.getString(eq(ProcessConstants.ACTIVITY_ID))).thenReturn("taskId");
		ThreadLocal<Collection> generated = getProcessedActivities();
		generated.set(new LinkedList<>());
		bPMTransitionAction.activityInstanceCreated(new AfterInstancePersistEvent<>(instance));
		Assert.assertEquals(1, generated.get().size());
	}

	@Test
	public void testLoadAndTransferProcessProperties() {
		ProcessInstance processInstance = mock(ProcessInstance.class);
		Instance process = mock(Instance.class);
		when(process.getOrCreateProperties()).thenReturn(new HashMap<String, Serializable>());
		VariableMap variableMap = mock(VariableMap.class);
		when(camundaBPMNService.getProcessInstanceVariables(processInstance, true)).thenReturn(variableMap);
		Map<String, Serializable> workflowProperties = new HashMap<>(1);
		workflowProperties.put("testPropKey", "testPropValue");
		when(propertiesConverter.convertDataFromCamundaToSEIP(variableMap, process)).thenReturn(workflowProperties);
		Instance modifiedInstance = bPMTransitionAction.loadAndTransferProcessProperties(processInstance, process);
		Assert.assertEquals(process, modifiedInstance);
	}

	@Test
	public void testListGeneratedActivities() throws NoSuchFieldException, SecurityException {
		BPMTransitionRequest request = new BPMTransitionRequest();
		when(camundaBPMNService.listActiveTasksIds("emf:process")).thenReturn(Arrays.asList("activity1"));
		ThreadLocal<Collection> generated =  getProcessedActivities();
		Instance task = mock(Instance.class);
		when(task.getAsString(ACTIVITY_ID)).thenReturn("activity1");
		List<Instance> generatedActivities = Arrays.asList(task);
		generated.set(generatedActivities);
		Collection<Instance> instances = bPMTransitionAction.listGeneratedActivities(request, "emf:process");
		Assert.assertEquals(generatedActivities, instances);
	}

	@Test
	public void testCollectActivityProperties_null_requestData() {
		BPMTransitionRequest request = new BPMTransitionRequest();
		Assert.assertTrue(BPMTransitionAction.collectActivityProperties(request).isEmpty());
	}

	@Test
	public void testCollectActivityProperties_missing_currentActivity() {
		BPMTransitionRequest request = new BPMTransitionRequest();
		request.setTargetId("testTargetId");
		request.setTransitionData(Collections.emptyMap());
		Assert.assertTrue(BPMTransitionAction.collectActivityProperties(request).isEmpty());
	}

	@Test
	public void testCollectTransitionProperties_null_requestData() {
		BPMTransitionRequest request = new BPMTransitionRequest();
		Assert.assertTrue(BPMTransitionAction.collectTransitionProperties(request, null).isEmpty());
	}

	@Test
	public void testAddActivitiesNonValid() throws Exception {
		Instance instance = mock(Instance.class);
		ThreadLocal<Collection> generated = getProcessedActivities();
		generated.set(new LinkedList<>());
		bPMTransitionAction.activityInstanceCreated(new AfterInstancePersistEvent<>(instance));
		Assert.assertEquals(0, generated.get().size());
	}

	@SuppressWarnings("unchecked")
	private ThreadLocal<Collection> getProcessedActivities() throws NoSuchFieldException {
		Field processedActivities = bPMTransitionAction.getClass().getDeclaredField("processedActivities");
		processedActivities.setAccessible(true);

		ThreadLocal<Collection> generated = (ThreadLocal<Collection>) ReflectionUtils.getFieldValue(processedActivities, bPMTransitionAction);
		return generated;
	}
}
