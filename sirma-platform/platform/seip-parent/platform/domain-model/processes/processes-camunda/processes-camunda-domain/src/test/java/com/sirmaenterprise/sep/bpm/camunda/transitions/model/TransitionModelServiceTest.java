package com.sirmaenterprise.sep.bpm.camunda.transitions.model;

import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelInstanceImpl;
import org.camunda.bpm.model.bpmn.impl.BpmnParser;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.configuration.SepCdiExpressionManager;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.ConditionExpression;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowEntry;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModelTest;

/**
 * Test for {@link TransitionModelService}.
 *
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class TransitionModelServiceTest {

	@InjectMocks
	private TransitionModelService transitionModelService;

	@Mock
	private DefinitionService definitionService;
	@Mock
	private InstanceService instanceService;
	@Mock
	private SepCdiExpressionManager expressionManager;
	@Spy
	private CommandExecutor commandExecutor = new CommandExecutorMock();
	@Mock
	private CamundaBPMNService camundaBPMNService;
	@Mock
	private BPMPropertiesConverter bpmPropertiesConverter;
	@Mock
	private CamundaBPMService camundaBPMService;
	@Mock
	private Instance instance;

	private String id;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		id = "emf:Id";

		when(instance.get(eq(DomainProcessConstants.TRANSITIONS))).thenReturn(
				SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(instance.getAsString(eq(DomainProcessConstants.TRANSITIONS))).thenReturn(
				SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		when(instance.getId()).thenReturn(id);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.getId()).thenReturn("test");
		when(instance.type()).thenReturn(instanceType);

	}

	@Test
	public void testGenerateTransitionActivitiesNullTransition() throws Exception {
		when(instance.getAsString(ACTIVITY_ID)).thenReturn("activityId");
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		when(instance.get(DomainProcessConstants.COMPLETED_ON)).thenReturn(new Date());
		when(instance.get(DomainProcessConstants.ACTIVITY_IN_PROCESS)).thenReturn(null);
		List<Instance> result = transitionModelService.generateTransitionActivities(instance, "id2");
		assertEquals(1, result.size());
		assertEquals(result.get(0).getId(), id);
	}

	@Test
	public void testGenerateTransitionActivities() throws Exception {
		when(instance.getAsString(ACTIVITY_ID)).thenReturn("activityId");
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		ExecutionEntity process = mockCamundaProcess();
		BpmnModelInstance processModel = mock(BpmnModelInstance.class);
		when(process.getBpmnModelInstance()).thenReturn(processModel);
		FlowElement activity1Model = mock(FlowElement.class);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		CamundaInputOutput inputOutput = mock(CamundaInputOutput.class);
		CamundaInputParameter input = mock(CamundaInputParameter.class);
		when(inputOutput.getCamundaInputParameters()).thenReturn(Collections.singletonList(input));
		when(extensionElements.getElements()).thenReturn(Collections.singletonList(inputOutput));
		when(activity1Model.getExtensionElements()).thenReturn(extensionElements);
		when(processModel.getModelElementById(eq("activity1"))).thenReturn(activity1Model);
		Instance newActivity1 = mock(Instance.class);
		Instance newActivity2 = mock(Instance.class);
		when(newActivity1.getId()).thenReturn("emf:activity1");
		when(newActivity2.getId()).thenReturn("emf:activity2");
		DefinitionModel model1 = mock(DefinitionModel.class);
		when(model1.getIdentifier()).thenReturn("activity1");
		DefinitionModel model2 = mock(DefinitionModel.class);
		when(model2.getIdentifier()).thenReturn("activity2");
		DefinitionModel instanceModel = mock(DefinitionModel.class);
		when(instanceModel.getIdentifier()).thenReturn("instanceModel");

		when(instanceService.createInstance(eq(model1), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity1);
		when(instanceService.createInstance(eq(model2), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity2);

		when(definitionService.find("activity1")).thenReturn(model1);
		when(definitionService.find("activity2")).thenReturn(model2);
		List<Instance> result = transitionModelService.generateTransitionActivities(instance, "id2");

		assertEquals(3, result.size());
		assertEquals(result.get(0).getId(), id);
		assertEquals(result.get(1).getId(), "emf:activity1");
		assertEquals(result.get(2).getId(), "emf:activity2");
	}

	@Test
	public void testGenerateTransitionActivitiesNotKnownType() throws Exception {
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.getId()).thenReturn("test");
		when(instanceType.is("test")).thenReturn(true);
		when(instance.type()).thenReturn(instanceType);
		List<Instance> result = transitionModelService.generateTransitionActivities(instance, "test");
		assertEquals(0, result.size());
	}

	@Test
	public void testGenerateStartActivities() throws Exception {
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.getId()).thenReturn("workflowinstancecontext");
		when(instanceType.is("workflowinstancecontext")).thenReturn(true);
		when(instance.type()).thenReturn(instanceType);
		try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("transitions1.bpmn")) {
			BpmnModelInstanceImpl parseModelFromStream = new BpmnParser().parseModelFromStream(in);
			when(camundaBPMNService.getBpmnModelInstance(eq(instance))).thenReturn(parseModelFromStream);
		}
		Instance newActivity1 = mock(Instance.class);
		Instance newActivity2 = mock(Instance.class);
		when(newActivity1.getId()).thenReturn("emf:activity1");
		when(newActivity2.getId()).thenReturn("emf:activity2");
		DefinitionModel model1 = mock(DefinitionModel.class);
		when(model1.getIdentifier()).thenReturn("Task1");
		DefinitionModel model2 = mock(DefinitionModel.class);
		when(model2.getIdentifier()).thenReturn("Task2");
		DefinitionModel instanceModel = mock(DefinitionModel.class);
		when(instanceModel.getIdentifier()).thenReturn("instanceModel");

		when(instanceService.createInstance(eq(model1), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity1);
		when(instanceService.createInstance(eq(model2), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity2);

		when(definitionService.find("Task1")).thenReturn(model1);
		when(definitionService.find("Task2")).thenReturn(model2);
		List<Instance> result = transitionModelService.generateTransitionActivities(instance, "start");

		assertEquals(3, result.size());
		assertEquals(result.get(0).getId(), id);
		assertEquals(result.get(1).getId(), "emf:activity1");
		assertEquals(result.get(2).getId(), "emf:activity2");
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testGenerateTransitionActivitiesWithMissingDefinition() throws Exception {
		mockCamundaProcess();
		when(instance.getAsString(ACTIVITY_ID)).thenReturn("activityId");
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		Instance newActivity1 = mock(Instance.class);
		Instance newActivity2 = mock(Instance.class);
		when(newActivity1.getId()).thenReturn("emf:activity1");
		when(newActivity2.getId()).thenReturn("emf:activity2");
		DefinitionModel model1 = mock(DefinitionModel.class);
		when(model1.getIdentifier()).thenReturn("activity1");
		DefinitionModel model2 = mock(DefinitionModel.class);
		when(model2.getIdentifier()).thenReturn("activity2");
		DefinitionModel instanceModel = mock(DefinitionModel.class);
		when(instanceModel.getIdentifier()).thenReturn("instanceModel");
		when(instanceService.createInstance(eq(model1), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity1);
		when(definitionService.find("activity2")).thenReturn(model2);
		transitionModelService.generateTransitionActivities(instance, "id2");

	}

	@Test
	public void testCreateTransitionConditionFilterNullFlowEntry() {
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(Collections.emptyMap());
		ExecutionEntity processInstance = new ExecutionEntity();
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(null));
	}

	@Test
	public void testCreateTransitionConditionFilterNullEntryName() {
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(Collections.emptyMap());
		ExecutionEntity processInstance = new ExecutionEntity();
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn(null);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
	}

	@Test
	public void testCreateTransitionConditionFilterNullEntryCondition() {
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(new HashMap<>(1));
		ExecutionEntity processInstance = mock(ExecutionEntity.class);
		when(processInstance.getVariablesInternal()).thenReturn(Collections.emptyList());
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		when(sequenceFlowEntry.getId()).thenReturn("testId");
		when(sequenceFlowEntry.getCondition()).thenReturn(null);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
	}

	@Test
	public void testCreateTransitionConditionFilterNullEntryIdAndCondition() {
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(new HashMap<>(1));
		ExecutionEntity processInstance = mock(ExecutionEntity.class);
		when(processInstance.getVariablesInternal()).thenReturn(Collections.emptyList());
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		when(sequenceFlowEntry.getId()).thenReturn("testId");
		ConditionExpression conditionExpression = mock(ConditionExpression.class);
		when(sequenceFlowEntry.getCondition()).thenReturn(conditionExpression);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
	}

	@Test
	public void testCreateTransitionConditionFilterNullCondition() {
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(new HashMap<>(1));
		ExecutionEntity processInstance = mock(ExecutionEntity.class);
		when(processInstance.getVariablesInternal()).thenReturn(Collections.emptyList());
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		when(sequenceFlowEntry.getId()).thenReturn("testId");
		ConditionExpression conditionExpression = mock(ConditionExpression.class);
		when(sequenceFlowEntry.getCondition()).thenReturn(conditionExpression);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
	}

	@Test
	public void testCreateTransitionConditionFilterNullEntryId() {
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(new HashMap<>(1));
		ExecutionEntity processInstance = new ExecutionEntity();
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		ConditionExpression conditionExpression = mock(ConditionExpression.class);
		when(conditionExpression.getValue()).thenReturn("${testProp=='testValue'}");
		when(sequenceFlowEntry.getCondition()).thenReturn(conditionExpression);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
	}

	@Test
	public void testCreateUelTransitionConditionFilter() {
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(new HashMap<>(1));
		ExecutionEntity processInstance = mock(ExecutionEntity.class);
		when(processInstance.getVariablesInternal()).thenReturn(Collections.emptyList());
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		when(sequenceFlowEntry.getId()).thenReturn("testId");
		ConditionExpression conditionExpression = mock(ConditionExpression.class);
		String expression = "${testProp=='testValue'}";
		when(conditionExpression.getValue()).thenReturn(expression);
		when(sequenceFlowEntry.getCondition()).thenReturn(conditionExpression);
		Expression expressionObj = mock(Expression.class);
		when(expressionObj.getValue(any(), eq(null))).thenReturn(Boolean.TRUE);
		when(expressionManager.createExpression(expression)).thenReturn(expressionObj);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
	}

	@Test
	public void testCreateTransitionConditionFilter_empty_camundaOutputParameters() {
		String expression = "${testProp=='testValue'}";
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		HashMap<String, Object> convertedProps = new HashMap<>(1);
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(convertedProps);
		ExecutionEntity processInstance = mock(ExecutionEntity.class);
		when(processInstance.getVariablesInternal()).thenReturn(Collections.emptyList());
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		BpmnModelInstance bpmnModelInstance = mock(BpmnModelInstance.class);
		FlowElement flowElement = mock(FlowElement.class);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		CamundaInputOutput inputOutput = mock(CamundaInputOutput.class);
		when(extensionElements.getElements()).thenReturn(Collections.singletonList(extensionElements));
		when(inputOutput.getCamundaOutputParameters()).thenReturn(Collections.emptyList());
		when(flowElement.getExtensionElements()).thenReturn(extensionElements);
		when(extensionElements.getElements()).thenReturn(Collections.singletonList(inputOutput));
		when(bpmnModelInstance.getModelElementById(any())).thenReturn(flowElement);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		when(sequenceFlowEntry.getId()).thenReturn("testId");
		ConditionExpression conditionExpression = mock(ConditionExpression.class);
		when(conditionExpression.getValue()).thenReturn(expression);
		when(sequenceFlowEntry.getCondition()).thenReturn(conditionExpression);
		Expression expressionObj = mock(Expression.class);
		when(expressionObj.getValue(any(), eq(null))).thenReturn(Boolean.TRUE);
		when(expressionManager.createExpression(expression)).thenReturn(expressionObj);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
		assertFalse("Converted props should not be empty!", convertedProps.isEmpty());
		assertEquals(1, convertedProps.size());
		assertTrue("Converted props must contain outcome!", convertedProps.containsKey("outcome"));
		assertEquals("testId", convertedProps.get("outcome"));
	}

	@Test
	public void testCreateTransitionConditionFilter_null_camundaOutputParameters() {
		String expression = "${testProp=='testValue'}";
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		HashMap<String, Object> convertedProps = new HashMap<>(1);
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(convertedProps);
		ExecutionEntity processInstance = mock(ExecutionEntity.class);
		when(processInstance.getVariablesInternal()).thenReturn(Collections.emptyList());
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		BpmnModelInstance bpmnModelInstance = mock(BpmnModelInstance.class);
		FlowElement flowElement = mock(FlowElement.class);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		CamundaInputOutput inputOutput = mock(CamundaInputOutput.class);
		when(extensionElements.getElements()).thenReturn(Collections.singletonList(extensionElements));
		when(inputOutput.getCamundaOutputParameters()).thenReturn(null);
		when(flowElement.getExtensionElements()).thenReturn(extensionElements);
		when(extensionElements.getElements()).thenReturn(Collections.singletonList(inputOutput));
		when(bpmnModelInstance.getModelElementById(any())).thenReturn(flowElement);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		when(sequenceFlowEntry.getId()).thenReturn("testId");
		ConditionExpression conditionExpression = mock(ConditionExpression.class);
		when(conditionExpression.getValue()).thenReturn(expression);
		when(sequenceFlowEntry.getCondition()).thenReturn(conditionExpression);
		Expression expressionObj = mock(Expression.class);
		when(expressionObj.getValue(any(), eq(null))).thenReturn(Boolean.TRUE);
		when(expressionManager.createExpression(expression)).thenReturn(expressionObj);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
		assertFalse("Converted props should not be empty!", convertedProps.isEmpty());
		assertEquals(1, convertedProps.size());
		assertTrue("Converted props must contain outcome!", convertedProps.containsKey("outcome"));
		assertEquals("testId", convertedProps.get("outcome"));
	}

	@Test
	public void testCreateTransitionConditionFilter_null_camundaInputOutput() {
		String expression = "${testProp=='testValue'}";
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		HashMap<String, Object> convertedProps = new HashMap<>(1);
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(convertedProps);
		ExecutionEntity processInstance = mock(ExecutionEntity.class);
		when(processInstance.getVariablesInternal()).thenReturn(Collections.emptyList());
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		BpmnModelInstance bpmnModelInstance = mock(BpmnModelInstance.class);
		FlowElement flowElement = mock(FlowElement.class);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(extensionElements.getElements()).thenReturn(null);
		when(flowElement.getExtensionElements()).thenReturn(extensionElements);
		when(bpmnModelInstance.getModelElementById(any())).thenReturn(flowElement);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		when(sequenceFlowEntry.getId()).thenReturn("testId");
		ConditionExpression conditionExpression = mock(ConditionExpression.class);
		when(conditionExpression.getValue()).thenReturn(expression);
		when(sequenceFlowEntry.getCondition()).thenReturn(conditionExpression);
		Expression expressionObj = mock(Expression.class);
		when(expressionObj.getValue(any(), eq(null))).thenReturn(Boolean.TRUE);
		when(expressionManager.createExpression(expression)).thenReturn(expressionObj);
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
		assertFalse("Converted props should not be empty!", convertedProps.isEmpty());
		assertEquals(1, convertedProps.size());
		assertTrue("Converted props must contain outcome!", convertedProps.containsKey("outcome"));
		assertEquals("testId", convertedProps.get("outcome"));
	}

	@Test
	public void testGenerateTransitionActivities_with_exceptions_during_inject_variables() throws CamundaIntegrationException {
		when(instance.getAsString(ACTIVITY_ID)).thenReturn("activityId");
		when(instance.isValueNotNull(ACTIVITY_ID)).thenReturn(true);
		ExecutionEntity process = mockCamundaProcess();
		BpmnModelInstance processModel = mock(BpmnModelInstance.class);
		when(process.getBpmnModelInstance()).thenReturn(processModel);
		FlowElement activity1Model = mock(FlowElement.class);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		CamundaInputOutput inputOutput = mock(CamundaInputOutput.class);
		CamundaInputParameter input = mock(CamundaInputParameter.class);
		when(inputOutput.getCamundaInputParameters()).thenReturn(Collections.singletonList(input));
		CamundaProperties camundaProperties = mock(CamundaProperties.class);
		CamundaProperty camundaProperty = mock(CamundaProperty.class);
		when(camundaProperties.getCamundaProperties()).thenReturn(Collections.singletonList(camundaProperty));
		when(extensionElements.getElements()).thenReturn(Arrays.asList(inputOutput, camundaProperties));
		when(activity1Model.getExtensionElements()).thenReturn(extensionElements);
		when(processModel.getModelElementById(eq("activity1"))).thenReturn(activity1Model);

		Instance newActivity1 = mock(Instance.class);
		Instance newActivity2 = mock(Instance.class);
		when(newActivity1.getId()).thenReturn("emf:activity1");
		when(newActivity2.getId()).thenReturn("emf:activity2");
		DefinitionModel model1 = mock(DefinitionModel.class);
		when(model1.getIdentifier()).thenReturn("activity1");
		DefinitionModel model2 = mock(DefinitionModel.class);
		when(model2.getIdentifier()).thenReturn("activity2");
		DefinitionModel instanceModel = mock(DefinitionModel.class);
		when(instanceModel.getIdentifier()).thenReturn("instanceModel");

		when(instanceService.createInstance(eq(model1), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity1);
		when(instanceService.createInstance(eq(model2), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity2);

		when(definitionService.find("activity1")).thenReturn(model1);
		when(definitionService.find("activity2")).thenReturn(model2);
		List<Instance> result = transitionModelService.generateTransitionActivities(instance, "id2");

		assertEquals(3, result.size());
		assertEquals(result.get(0).getId(), id);
		assertEquals(result.get(1).getId(), "emf:activity1");
		assertEquals(result.get(2).getId(), "emf:activity2");
	}

	@Test
	public void testGenerateTransitionActivities_with_injections() throws CamundaIntegrationException {
		Instance localInstance = new EmfInstance();
		localInstance.add(ACTIVITY_ID, "activityId");
		localInstance.add(DomainProcessConstants.TRANSITIONS, SequenceFlowModelTest.SERIALIZED_MODEL_FULL);
		ExecutionEntity process = mockCamundaProcess();
		BpmnModelInstance processModel = mock(BpmnModelInstance.class);
		when(process.getBpmnModelInstance()).thenReturn(processModel);
		FlowElement activity1Model = mock(FlowElement.class);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		CamundaInputOutput inputOutput = mock(CamundaInputOutput.class);
		CamundaInputParameter input = mock(CamundaInputParameter.class);
		Expression expressionObj = mock(Expression.class);
		when(expressionObj.getValue(process)).thenReturn("testExpressionValue");
		when(expressionManager.createExpression("${testExpression}")).thenReturn(expressionObj);
		when(input.getCamundaName()).thenReturn("camundaInputName");
		when(input.getTextContent()).thenReturn("${testExpression}");
		when(inputOutput.getCamundaInputParameters()).thenReturn(Collections.singletonList(input));
		CamundaProperties camundaProperties = mock(CamundaProperties.class);
		CamundaProperty camundaProperty = mock(CamundaProperty.class);
		when(camundaProperty.getCamundaName()).thenReturn("inject_camundaPropName");
		when(camundaProperty.getCamundaValue()).thenReturn("${testExpression}");
		when(camundaProperties.getCamundaProperties()).thenReturn(Collections.singletonList(camundaProperty));
		when(extensionElements.getElements()).thenReturn(Arrays.asList(inputOutput, camundaProperties));
		when(activity1Model.getExtensionElements()).thenReturn(extensionElements);
		when(processModel.getModelElementById(eq("activity1"))).thenReturn(activity1Model);

		Instance newActivity1 = new EmfInstance();
		newActivity1.setId("emf:activity1");
		Instance newActivity2 = new EmfInstance();
		newActivity2.setId("emf:activity2");
		DefinitionModel model1 = mock(DefinitionModel.class);
		when(model1.getIdentifier()).thenReturn("activity1");
		DefinitionModel model2 = mock(DefinitionModel.class);
		when(model2.getIdentifier()).thenReturn("activity2");
		DefinitionModel instanceModel = mock(DefinitionModel.class);
		when(instanceModel.getIdentifier()).thenReturn("instanceModel");

		when(instanceService.createInstance(eq(model1), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity1);
		when(instanceService.createInstance(eq(model2), isNull(Instance.class),
											eq(new Operation(ActionTypeConstants.CREATE)))).thenReturn(newActivity2);

		when(definitionService.find("activity1")).thenReturn(model1);
		when(definitionService.find("activity2")).thenReturn(model2);
		List<Instance> result = transitionModelService.generateTransitionActivities(localInstance, "id2");

		assertEquals(3, result.size());
		assertEquals(result.get(1).getId(), "emf:activity1");
		assertEquals(result.get(1).get("camundaInputName"), "testExpressionValue");
		assertEquals(result.get(1).get("camundaPropName"), "testExpressionValue");
		assertEquals(result.get(2).getId(), "emf:activity2");
	}

	@Test
	public void testCreateTransitionConditionFilter_outjectProperties() {
		String expression = "${testProp=='testValue'}";
		TaskFormData taskFormData = mock(TaskFormData.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.of(taskFormData));
		Map<String, Object> convertedProps = new HashMap<>(1);
		when(bpmPropertiesConverter.convertDataFromSEIPtoCamunda(any(), any())).thenReturn(convertedProps);
		ExecutionEntity processInstance = mock(ExecutionEntity.class);
		when(processInstance.getVariablesInternal()).thenReturn(Collections.emptyList());
		when(camundaBPMNService.getProcessInstance(instance)).thenReturn(processInstance);
		BpmnModelInstance bpmnModelInstance = mock(BpmnModelInstance.class);
		when(processInstance.getBpmnModelInstance()).thenReturn(bpmnModelInstance);
		FlowElement flowElement = mock(FlowElement.class);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		CamundaInputOutput inputOutput = mock(CamundaInputOutput.class);
		CamundaOutputParameter output1 = mock(CamundaOutputParameter.class);
		CamundaOutputParameter output2 = mock(CamundaOutputParameter.class);
		when(output2.getCamundaName()).thenReturn("testProp");
		when(output2.getTextContent()).thenReturn("${testProp}");
		when(inputOutput.getCamundaOutputParameters()).thenReturn(Arrays.asList(output1, output2));
		when(extensionElements.getElements()).thenReturn(Collections.singletonList(inputOutput));
		when(flowElement.getExtensionElements()).thenReturn(extensionElements);
		when(bpmnModelInstance.getModelElementById(any())).thenReturn(flowElement);
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(sequenceFlowEntry.getName()).thenReturn("testName");
		when(sequenceFlowEntry.getId()).thenReturn("testId");
		ConditionExpression conditionExpression = mock(ConditionExpression.class);
		when(conditionExpression.getValue()).thenReturn(expression);
		when(sequenceFlowEntry.getCondition()).thenReturn(conditionExpression);
		Expression expressionObj = mock(Expression.class);
		when(expressionObj.getValue(any(), eq(null))).thenReturn(Boolean.TRUE);
		Expression fieldExpressionObj = mock(Expression.class);
		when(expressionManager.createExpression(expression)).thenReturn(expressionObj);
		when(expressionManager.createExpression("${testProp}")).thenReturn(fieldExpressionObj);
		when(fieldExpressionObj.getValue(any())).thenReturn("testValue");
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
		assertFalse("Converted properties should not be empty!", convertedProps.isEmpty());
		assertEquals(2, convertedProps.size());
		assertTrue(convertedProps.containsKey("testProp"));
		assertEquals("testValue", convertedProps.get("testProp"));
	}

	@Test
	public void testCreateTransitionConditionNullTaskFormData() {
		SequenceFlowEntry sequenceFlowEntry = mock(SequenceFlowEntry.class);
		when(camundaBPMService.getTaskFormData(instance)).thenReturn(Optional.empty());
		Predicate<SequenceFlowEntry> filter = transitionModelService.createTransitionConditionFilter(instance);
		assertEquals(true, filter.test(sequenceFlowEntry));
	}

	private ExecutionEntity mockCamundaProcess() {
		ExecutionEntity process = mock(ExecutionEntity.class);
		when(camundaBPMNService.getProcessInstance(any(Instance.class))).thenReturn(process);
		return process;
	}
}
