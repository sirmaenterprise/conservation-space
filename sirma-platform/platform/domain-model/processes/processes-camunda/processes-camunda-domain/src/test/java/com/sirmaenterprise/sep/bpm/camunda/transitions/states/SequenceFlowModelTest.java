package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.junit.Test;

import com.sirma.itt.seip.domain.definition.StateTransition;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests {@link SequenceFlowModel}.
 *
 * @author bbanchev
 */
public class SequenceFlowModelTest {
	/** sample model for sequnce flow. */
	public static final String SERIALIZED_MODEL_SIMPLE = "[false,{\"identifier\":\"id1\",\"transitionId\":\"id1\",\"name\":\"id1\",\"properties\":{},\"mandatoryTypes\":[]},{\"identifier\":\"id2\",\"transitionId\":\"id2\",\"name\":\"id2\",\"properties\":{},\"mandatoryTypes\":[\"activity1\",\"activity2\"]}]";
	/** full model for sequnce flow. */
	public static final String SERIALIZED_MODEL_FULL = "[false,{\"identifier\":\"id1\",\"transitionId\":\"id1\",\"name\":\"id1_name\",\"properties\":{\"key1\":\"value1\",\"leavingObjectState\":\"TEST\",\"leavingObjectState1\":\"TEST1\"},\"mandatoryTypes\":[]},{\"identifier\":\"id2\",\"transitionId\":\"id2\",\"name\":\"id2_name2\",\"properties\":{\"key1\":\"value1\",\"key2\":\"value2\"},\"mandatoryTypes\":[\"activity1\",\"activity2\"]}]";

	public static final String SERIALIZED_MODEL_WITH_UEL_CONDITION = "[false,{\"identifier\":\"id2\",\"transitionId\":\"id2\",\"name\":\"id2_name2\",\"condition\":{\"scopeId\":\"testScopeId\",\"value\":\"testValue\"},\"properties\":{\"key1\":\"value1\",\"key2\":\"value2\"},\"mandatoryTypes\":[\"activity1\",\"activity2\"]}]";
	public static final String SERIALIZED_MODEL_WITH_SCRIPT_CONDITION = "[false, { \"identifier\": \"id2\", \"transitionId\": \"id2\",	\"name\": \"id2_name2\",\"properties\": { \"key1\": \"value1\",	\"key2\": \"value2\" },\"mandatoryTypes\": [\"activity1\", \"activity2\"], \"condition\" : {\"scopeId\" : \"testScopeId\", \"value\": \"testValue\", \"language\": \"testLanguage\" } }]";

	/**
	 * Test serialize.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testSerialize() throws Exception {
		SequenceFlowModel sequenceFlowModel = new SequenceFlowModel();
		sequenceFlowModel.add(generateFlow("id1", "id1"), "");
		SequenceFlow transitionWithTwoCheckpoints = generateFlow("id2", "id2");
		sequenceFlowModel.add(transitionWithTwoCheckpoints, "activity1");
		sequenceFlowModel.add(transitionWithTwoCheckpoints, "activity2");
		System.out.println(sequenceFlowModel.serialize());
		JsonAssert.assertJsonEquals(sequenceFlowModel.serialize(), SERIALIZED_MODEL_SIMPLE);

	}

	/**
	 * Test deserialize.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testDeserialize() throws Exception {
		SequenceFlowModel deserialized = SequenceFlowModel.deserialize(null);
		assertNull(deserialized);
		deserialized = SequenceFlowModel.deserialize(SERIALIZED_MODEL_FULL);
		assertNotNull(deserialized);
		assertNull(deserialized.getTransition("id_notexits"));
		assertEquals(2, deserialized.getModel().size());
		assertEquals(0, deserialized.getTransition("id1").getCheckpoints().size());
		assertEquals(3, deserialized.getTransition("id1").getProperties().size());
		assertEquals("id1_name", deserialized.getTransition("id1").getName());

		assertEquals(2, deserialized.getTransition("id2").getCheckpoints().size());
		assertEquals(2, deserialized.getTransition("id2").getProperties().size());
		assertEquals("value1", deserialized.getTransition("id2").getProperty("key1"));
		assertEquals("id2_name2", deserialized.getTransition("id2").getName());

	}

	/**
	 * Test contains transition.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testContainsTransition() throws Exception {
		SequenceFlowModel deserialized = SequenceFlowModel.deserialize(SERIALIZED_MODEL_FULL);
		assertTrue(deserialized.containsTransition("id1"));
		assertTrue(deserialized.containsTransition("id2"));
		assertFalse(deserialized.containsTransition("id3"));
	}

	/**
	 * Test get transition.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testGetTransition() throws Exception {
		SequenceFlowModel deserialized = SequenceFlowModel.deserialize(SERIALIZED_MODEL_FULL);
		assertNotNull(deserialized.getTransition("id1"));
		assertNotNull(deserialized.getTransition("id2"));
		assertNull(deserialized.getTransition("id3"));
	}

	/**
	 * Test to state transition model.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testToStateTransitionModel() throws Exception {
		SequenceFlowModel deserialized = SequenceFlowModel.deserialize(SERIALIZED_MODEL_FULL);
		List<StateTransition> stateTransitionModel = deserialized.toStateTransitionModel("INIT", null);
		assertEquals(2, stateTransitionModel.size());
		StateTransition stateTransition0 = stateTransitionModel.get(0);
		StateTransition stateTransition1 = stateTransitionModel.get(1);

		assertEquals("id1", stateTransition0.getIdentifier());
		assertEquals("id2", stateTransition1.getIdentifier());

		assertEquals("INIT", stateTransition0.getFromState());
		assertEquals("TEST", stateTransition0.getToState());
		assertEquals(0, stateTransition0.getConditions().size());

		assertEquals("INIT", stateTransition1.getFromState());
		assertEquals("COMPLETED", stateTransition1.getToState());
		assertEquals(0, stateTransition1.getConditions().size());

	}

	/**
	 * Test deserialize with UEL condition.
	 */
	@Test
	public void testDeserializeWithUELCondition() {
		SequenceFlowModel sequenceFlow = SequenceFlowModel.deserialize(SERIALIZED_MODEL_WITH_UEL_CONDITION);
		List<SequenceFlowEntry> sequences = sequenceFlow.getTransitions().collect(Collectors.toList());
		assertEquals(1, sequences.size());
		SequenceFlowEntry sequenceFlowEntry = sequences.get(0);
		ConditionExpression condition = sequenceFlowEntry.getCondition();
		assertNotNull(condition);
		assertEquals("testScopeId", condition.getScopeId());
		assertEquals("testValue", condition.getValue());
	}

	/**
	 * Test deserialize/serialize with condition.
	 */
	@Test
	public void testDeserializeSerializeCondition() {
		SequenceFlowModel sequenceFlow = SequenceFlowModel.deserialize(SERIALIZED_MODEL_WITH_UEL_CONDITION);
		List<SequenceFlowEntry> sequences = sequenceFlow.getTransitions().collect(Collectors.toList());
		assertEquals(1, sequences.size());
		SequenceFlowEntry sequenceFlowEntry = sequences.get(0);
		ConditionExpression condition = sequenceFlowEntry.getCondition();
		assertNotNull(condition);
		assertEquals("testScopeId", condition.getScopeId());
		assertEquals("testValue", condition.getValue());
		String serialized = sequenceFlow.serialize();
		assertEquals(SERIALIZED_MODEL_WITH_UEL_CONDITION, serialized);
	}

	/**
	 * Test deserialize with script condition.
	 */
	@Test
	public void testDeserializeWithScriptCondition() {
		SequenceFlowModel sequenceFlow = SequenceFlowModel.deserialize(SERIALIZED_MODEL_WITH_SCRIPT_CONDITION);
		List<SequenceFlowEntry> sequences = sequenceFlow.getTransitions().collect(Collectors.toList());
		assertEquals(1, sequences.size());
		SequenceFlowEntry sequenceFlowEntry = sequences.get(0);
		ConditionExpression condition = sequenceFlowEntry.getCondition();
		assertNotNull(condition);
		assertTrue(condition instanceof ConditionScript);
		assertEquals("testScopeId", condition.getScopeId());
		assertEquals("testValue", condition.getValue());
		assertEquals("testLanguage", ((ConditionScript) condition).getLanguage());
	}

	private static SequenceFlow generateFlow(String id, String name) {
		SequenceFlow sequenceFlow = mock(SequenceFlow.class);
		when(sequenceFlow.getId()).thenReturn(id);
		when(sequenceFlow.getName()).thenReturn(name);
		return sequenceFlow;
	}

}
