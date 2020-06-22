package com.sirma.itt.seip.instance.state;

import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.StateTransitionalModel;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.definition.model.TransitionGroupDefinitionImpl;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.mocks.ActionMock;
import com.sirma.itt.seip.testutil.mocks.ConditionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.StateTransitionMock;
import com.sirma.itt.seip.testutil.mocks.TransitionDefinitionMock;

/**
 * Test for {@link StateTransitionManagerImpl}.
 *
 * @author BBonev
 * @author A. Kunchev
 */
public class StateTransitionManagerImplTest {

	@InjectMocks
	private StateTransitionManager manager;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private StateService stateService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Spy
	private Plugins<DynamicStateTransitionProvider> transitionProviders = new Plugins<>(
			DynamicStateTransitionProvider.TARGET_NAME, Collections.emptyList());

	@Before
	public void init() {
		manager = new StateTransitionManagerImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsStateManagementSupported() {
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);
		Assert.assertFalse(manager.isStateManagementSupported(instance));

		model.getStateTransitions().add(new StateTransitionMock());
		Assert.assertTrue(manager.isStateManagementSupported(instance));
	}

	private static StateTransitionalModel createModel() {
		StateTransitionalModel model = new DefinitionMock();
		model.setIdentifier("testDefinition");
		return model;
	}

	/**
	 * Creates the instance.
	 *
	 * @return the emf instance
	 */
	private static EmfInstance createInstance() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());
		return instance;
	}

	@Test
	public void testGetNextState() {
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		StateTransitionMock transition = new StateTransitionMock();
		transition.setFromState("FROM_STATE");
		transition.setTransitionId("operation");
		transition.setToState("TO_STATE");
		model.getStateTransitions().add(transition);
		transition = new StateTransitionMock();
		transition.setFromState("*");
		transition.setTransitionId("randomOperation");
		transition.setToState("RANDOM_STATE");
		model.getStateTransitions().add(transition);

		String nextState = manager.getNextState(instance, "FROM_STATE", "operation");
		Assert.assertEquals("TO_STATE", nextState);

		nextState = manager.getNextState(instance, "FROM_STATE", "randomOperation");
		Assert.assertEquals("RANDOM_STATE", nextState);
	}

	@Test
	public void testGetRequiredFields() {
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		StateTransitionMock transition = new StateTransitionMock();
		transition.setFromState("FROM_STATE");
		transition.setTransitionId("operation");
		transition.setToState("TO_STATE");
		model.getStateTransitions().add(transition);
		ConditionMock condition = new ConditionMock();
		condition.setIdentifier(StateTransitionManagerImpl.REQUIRED_FIELDS);
		condition.setRenderAs(StateTransitionManagerImpl.REQUIRED);
		condition.setExpression("[field1] [field2] [emf:field3]");
		transition.getConditions().add(condition);

		Set<String> requiredFields = manager.getRequiredFields(instance, "FROM_STATE", "operation");
		Assert.assertFalse(requiredFields.isEmpty());
		Assert.assertTrue(requiredFields.contains("field1"));
		Assert.assertTrue(requiredFields.contains("field2"));
		Assert.assertTrue(requiredFields.contains("emf:field3"));
	}

	@Test
	public void testGetRequiredFieldsExpression() {
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		StateTransitionMock transition = new StateTransitionMock();
		transition.setFromState("FROM_STATE");
		transition.setTransitionId("operation");
		transition.setToState("TO_STATE");
		model.getStateTransitions().add(transition);
		ConditionMock condition = new ConditionMock();
		condition.setIdentifier("conditionId");
		condition.setRenderAs(StateTransitionManagerImpl.DISABLE_SAVE);
		String expression = "[field1] [field2]";
		condition.setExpression(expression);
		transition.getConditions().add(condition);

		String requiredFields = manager.getRequiredFieldsExpression(instance, "FROM_STATE", "operation");
		Assert.assertEquals(expression, requiredFields);
	}

	@Test
	public void testGetAllowedOperations() {
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		StateTransitionMock transition = new StateTransitionMock();
		transition.setFromState("FROM_STATE");
		transition.setTransitionId("operation");
		transition.setToState("TO_STATE");
		model.getStateTransitions().add(transition);
		transition = new StateTransitionMock();
		transition.setFromState("*");
		transition.setTransitionId("randomOperation");
		transition.setToState("RANDOM_STATE");
		model.getStateTransitions().add(transition);

		Set<String> operations = manager.getAllowedOperations(instance, "FROM_STATE");
		Assert.assertFalse(operations.isEmpty());
		Assert.assertEquals(1, operations.size());
		Assert.assertFalse(operations.contains("randomOperation"));
		Assert.assertTrue(operations.contains("operation"));
	}

	@Test
	public void testGetAllowedActions_default() {
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		StateTransitionMock transition = new StateTransitionMock();
		transition.setFromState("FROM_STATE");
		transition.setTransitionId("operation");
		transition.setToState("TO_STATE");
		model.getStateTransitions().add(transition);
		transition = new StateTransitionMock();
		transition.setFromState("*");
		transition.setTransitionId("randomOperation");
		transition.setToState("RANDOM_STATE");
		model.getStateTransitions().add(transition);
		TransitionDefinitionMock transitionDefinition = new TransitionDefinitionMock();
		transitionDefinition.setIdentifier("operation");
		transitionDefinition.setPurpose(DefinitionUtil.TRANSITION_PERPOSE_ACTION);
		model.getTransitions().add(transitionDefinition);
		transitionDefinition = new TransitionDefinitionMock();
		transitionDefinition.setIdentifier("randomOperation");
		transitionDefinition.setPurpose(DefinitionUtil.TRANSITION_PERPOSE_ACTION);
		model.getTransitions().add(transitionDefinition);

		Set<Action> actions = manager.getAllowedActions(instance, "FROM_STATE");
		Assert.assertFalse(actions.isEmpty());
		Assert.assertEquals(1, actions.size());
		Assert.assertFalse(actions.contains(new ActionMock("randomOperation")));
		Assert.assertTrue(actions.contains(new ActionMock("operation")));
	}

	@Test
	public void testGetAllowedActions_filtered() {
		when(typeConverter.convert(eq(Boolean.class), anyString()))
				.then(a -> Boolean.valueOf(a.getArgumentAt(1, Object.class).toString()));
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();

		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		StateTransitionMock transition = new StateTransitionMock();
		transition.setFromState("FROM_STATE");
		transition.setTransitionId("operation");
		transition.setToState("TO_STATE");
		ConditionMock condition = new ConditionMock();
		condition.setIdentifier("property");
		condition.setRenderAs(StateTransitionManagerImpl.PROPERTY);
		condition.setExpression("true");
		transition.getConditions().add(condition);
		model.getStateTransitions().add(transition);
		transition = new StateTransitionMock();
		transition.setFromState("*");
		transition.setTransitionId("randomOperation");
		transition.setToState("RANDOM_STATE");
		model.getStateTransitions().add(transition);
		TransitionDefinitionMock transitionDefinition = new TransitionDefinitionMock();
		transitionDefinition.setIdentifier("operation");
		transitionDefinition.setPurpose(DefinitionUtil.TRANSITION_PERPOSE_ACTION);
		model.getTransitions().add(transitionDefinition);
		transitionDefinition = new TransitionDefinitionMock();
		transitionDefinition.setIdentifier("randomOperation");
		transitionDefinition.setPurpose(DefinitionUtil.TRANSITION_PERPOSE_ACTION);
		model.getTransitions().add(transitionDefinition);

		Set<Action> actions = manager.getAllowedActions(instance, "FROM_STATE");
		Assert.assertTrue(actions.isEmpty());

		instance.getProperties().put("property", true);

		actions = manager.getAllowedActions(instance, "FROM_STATE");
		Assert.assertFalse(actions.isEmpty());

		Assert.assertEquals(1, actions.size());
		Assert.assertFalse(actions.contains(new ActionMock("randomOperation")));
		Assert.assertTrue(actions.contains(new ActionMock("operation")));
	}

	@Test
	public void testGetAllowedActions_filtered_Required() {
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();

		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		StateTransitionMock transition = new StateTransitionMock();
		transition.setFromState("FROM_STATE");
		transition.setTransitionId("operation");
		transition.setToState("TO_STATE");
		ConditionMock condition = new ConditionMock();
		condition.setIdentifier("property");
		condition.setRenderAs(StateTransitionManagerImpl.REQUIRED);
		condition.setExpression("true");
		transition.getConditions().add(condition);
		model.getStateTransitions().add(transition);
		transition = new StateTransitionMock();
		transition.setFromState("*");
		transition.setTransitionId("randomOperation");
		transition.setToState("RANDOM_STATE");
		model.getStateTransitions().add(transition);
		TransitionDefinitionMock transitionDefinition = new TransitionDefinitionMock();
		transitionDefinition.setIdentifier("operation");
		transitionDefinition.setPurpose(DefinitionUtil.TRANSITION_PERPOSE_ACTION);
		model.getTransitions().add(transitionDefinition);
		transitionDefinition = new TransitionDefinitionMock();
		transitionDefinition.setIdentifier("randomOperation");
		transitionDefinition.setPurpose(DefinitionUtil.TRANSITION_PERPOSE_ACTION);
		model.getTransitions().add(transitionDefinition);

		Set<Action> actions = manager.getAllowedActions(instance, "FROM_STATE");
		Assert.assertFalse(actions.isEmpty());

		instance.getProperties().put("property", true);

		actions = manager.getAllowedActions(instance, "FROM_STATE");
		Assert.assertFalse(actions.isEmpty());

		Assert.assertEquals(1, actions.size());
		Assert.assertFalse(actions.contains(new ActionMock("randomOperation")));
		Assert.assertTrue(actions.contains(new ActionMock("operation")));
	}

	@Test
	public void testGetAllowedStates() {
		StateTransitionalModel model = createModel();
		EmfInstance instance = createInstance();

		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		StateTransitionMock transition = new StateTransitionMock();
		transition.setFromState("FROM_STATE");
		transition.setTransitionId("operation");
		transition.setToState("TO_STATE");
		model.getStateTransitions().add(transition);
		transition = new StateTransitionMock();
		transition.setFromState("*");
		transition.setTransitionId("randomOperation");
		transition.setToState("RANDOM_STATE");
		model.getStateTransitions().add(transition);

		Set<String> states = manager.getAllowedStates(instance);
		Assert.assertFalse(states.isEmpty());
		Assert.assertEquals(2, states.size());

		Assert.assertTrue(states.contains("TO_STATE"));
		Assert.assertTrue(states.contains("RANDOM_STATE"));
	}

	@Test
	public void getActions_noInstance() {
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.empty());
		Set<Action> actions = manager.getActions("instance-id", null);
		assertTrue(actions.isEmpty());
	}

	@Test
	public void getActions_noModelForInstance() {
		Instance instance = new EmfInstance();
		instance.setId("instance-id");
		InstanceReferenceMock referenceMock = new InstanceReferenceMock(instance);
		when(instanceTypeResolver.resolveReference("instance-id")).thenReturn(Optional.of(referenceMock));
		when(definitionService.getInstanceDefinition(instance)).thenReturn(null);
		Set<Action> actions = manager.getActions("instance-id", null);
		assertTrue(actions.isEmpty());
	}

	@Test
	public void getActions_withoutFilterSet() {
		Instance instance = createInstance();
		InstanceReference referenceMock = new InstanceReferenceMock(instance);
		when(instanceTypeResolver.resolveReference("emf:instance")).thenReturn(Optional.of(referenceMock));

		StateTransitionalModel transitionalModel = createModel();
		TransitionDefinition transitionOne = new TransitionDefinitionMock();
		transitionOne.setIdentifier("one");
		transitionOne.setPurpose("one");
		TransitionDefinition transitionTwo = new TransitionDefinitionMock();
		transitionTwo.setIdentifier("two");
		transitionTwo.setPurpose("two");
		transitionalModel.getTransitions().addAll(Arrays.asList(transitionOne, transitionTwo));
		when(definitionService.getInstanceDefinition(instance)).thenReturn(transitionalModel);

		Set<Action> actions = manager.getActions("emf:instance", emptySet());
		assertFalse(actions.isEmpty());
		assertEquals(2, actions.size());

		Iterator<Action> iterator = actions.iterator();
		assertEquals("one", iterator.next().getActionId());
		assertEquals("two", iterator.next().getActionId());
	}

	@Test
	public void getActions_withFilterSet() {
		Instance instance = createInstance();
		InstanceReference referenceMock = new InstanceReferenceMock(instance);
		when(instanceTypeResolver.resolveReference("emf:instance")).thenReturn(Optional.of(referenceMock));

		StateTransitionalModel transitionalModel = createModel();
		TransitionDefinition transitionOne = new TransitionDefinitionMock();
		transitionOne.setIdentifier("action");
		transitionOne.setPurpose("action");
		TransitionDefinition transitionTwo = new TransitionDefinitionMock();
		transitionTwo.setIdentifier("actionToFilter");
		transitionTwo.setPurpose("actionToFilter");
		transitionalModel.getTransitions().addAll(Arrays.asList(transitionOne, transitionTwo));
		when(definitionService.getInstanceDefinition(instance)).thenReturn(transitionalModel);

		Set<Action> actions = manager.getActions("emf:instance", Collections.singleton("action"));
		assertFalse(actions.isEmpty());
		assertEquals(1, actions.size());

		Iterator<Action> iterator = actions.iterator();
		assertEquals("action", iterator.next().getActionId());
	}

	@Test
	public void should_FetchTransitionGroupsFromInstanceModel() {
		Instance instance = createInstance();

		List<TransitionGroupDefinition> groups = new ArrayList<>();
		groups.add(new TransitionGroupDefinitionImpl());

		DefinitionMock model = new DefinitionMock();
		model.setTransitionGroups(groups);

		when(definitionService.getInstanceDefinition(instance)).thenReturn(model);

		List<TransitionGroupDefinition> result = manager.getActionGroups(instance);

		assertEquals(result, groups);
	}
}