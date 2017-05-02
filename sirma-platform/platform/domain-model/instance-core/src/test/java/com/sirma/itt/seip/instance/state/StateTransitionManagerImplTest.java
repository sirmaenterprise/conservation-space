package com.sirma.itt.seip.instance.state;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.StateTransition;
import com.sirma.itt.seip.domain.definition.StateTransitionalModel;
import com.sirma.itt.seip.domain.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.testutil.mocks.ActionMock;
import com.sirma.itt.seip.testutil.mocks.ConditionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.StateTransitionMock;
import com.sirma.itt.seip.testutil.mocks.TransitionDefinitionMock;

/**
 * @author BBonev
 */

public class StateTransitionManagerImplTest {

	private StateTransitionManager manager;
	private DictionaryService dictionaryService;
	private TypeConverter typeConverter;
	private StateService stateService;

	/**
	 * Initializes the class
	 */
	@Before
	public void init() {
		manager = new StateTransitionManagerImpl();
		dictionaryService = Mockito.mock(DictionaryService.class);
		typeConverter = mock(TypeConverter.class);
		stateService = Mockito.mock(StateService.class);
		ReflectionUtils.setField(manager, "typeConverter", typeConverter);
		ReflectionUtils.setField(manager, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(manager, "stateServiceInstance", new InstanceProxyMock<>(stateService));
		ReflectionUtils.setField(manager, "transitionProviders",
				new Plugins<>(DynamicStateTransitionProvider.TARGET_NAME, Collections.emptyList()));

	}

	/**
	 * Test is state management supported.
	 */
	@Test
	public void testIsStateManagementSupported() {
		Definition model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);
		Assert.assertFalse(manager.isStateManagementSupported(instance));

		model.getStateTransitions().add(new StateTransitionMock());
		Assert.assertTrue(manager.isStateManagementSupported(instance));
	}

	/**
	 * Creates the model.
	 *
	 * @return the definition
	 */
	private Definition createModel() {
		Definition model = new Definition();
		model.setIdentifier("testDefinition");
		return model;
	}

	/**
	 * Creates the instance.
	 *
	 * @return the emf instance
	 */
	private EmfInstance createInstance() {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());
		return instance;
	}

	/**
	 * Test get next state.
	 */
	@Test
	public void testGetNextState() {
		Definition model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

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

	/**
	 * Test get required fields.
	 */
	@Test
	public void testGetRequiredFields() {
		Definition model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

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

	/**
	 * Test get required fields expression.
	 */
	@Test
	public void testGetRequiredFieldsExpression() {
		Definition model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

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

	/**
	 * Test get allowed operations.
	 */
	@Test
	public void testGetAllowedOperations() {
		Definition model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

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

	/**
	 * Test get allowed actions_default.
	 */
	@Test
	public void testGetAllowedActions_default() {
		Definition model = createModel();
		EmfInstance instance = createInstance();
		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

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

	/**
	 * Test get allowed actions_filtered.
	 */
	@Test
	public void testGetAllowedActions_filtered() {
		when(typeConverter.convert(eq(Boolean.class), anyString()))
				.then(a -> Boolean.valueOf(a.getArgumentAt(1, Object.class).toString()));
		Definition model = createModel();
		EmfInstance instance = createInstance();

		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

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

	/**
	 * Test get allowed actions_filtered_ required.
	 */
	@Test
	public void testGetAllowedActions_filtered_Required() {
		Definition model = createModel();
		EmfInstance instance = createInstance();

		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

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

	/**
	 * Test get allowed states.
	 */
	@Test
	public void testGetAllowedStates() {
		Definition model = createModel();
		EmfInstance instance = createInstance();

		Mockito.when(dictionaryService.getInstanceDefinition(instance)).thenReturn(model);

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

	/**
	 * Definition
	 *
	 * @author BBonev
	 */
	private static class Definition implements DefinitionModel, StateTransitionalModel {
		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = 1L;
		private String identifier;
		private List<StateTransition> stateTransitions = new LinkedList<>();
		private List<TransitionDefinition> transitions = new LinkedList<>();

		@Override
		public String getIdentifier() {
			return identifier;
		}

		@Override
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		@Override
		public Integer getHash() {
			return null;
		}

		@Override
		public void setHash(Integer hash) {

		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public Node getChild(String name) {
			return null;
		}

		@Override
		public List<TransitionDefinition> getTransitions() {
			return transitions;
		}

		@Override
		public List<StateTransition> getStateTransitions() {
			return stateTransitions;
		}

		@Override
		public List<PropertyDefinition> getFields() {
			return null;
		}

		@Override
		public Long getRevision() {
			return null;
		}

		@Override
		public String getType() {
			return null;
		}

	}

}
