package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import org.camunda.bpm.model.bpmn.impl.BpmnModelInstanceImpl;
import org.camunda.bpm.model.bpmn.impl.BpmnParser;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.Test;

public class SequenceFlowParserTest {

	@Test
	public void testGetSequenceFlowModelModelElementInstance1() throws Exception {
		ModelElementInstance startField = null;
		try (InputStream in = SequenceFlowParserTest.class.getClassLoader().getResourceAsStream("transitions1.bpmn")) {
			BpmnModelInstanceImpl parseModelFromStream = new BpmnParser().parseModelFromStream(in);
			startField = parseModelFromStream.getModelElementById("StartEvent_1");
		}
		SequenceFlowModel sequenceFlowModel = SequenceFlowParser.getSequenceFlowModel(startField);
		assertNotNull(sequenceFlowModel.getModel());
		assertEquals("Two transitions after gateway", 2, sequenceFlowModel.getModel().size());
		Iterator<SequenceFlowEntry> flows = sequenceFlowModel.getModel().iterator();
		assertEquals("One task after the exclusive gateway", 1, flows.next().getCheckpoints().size());
		assertEquals("One task after the exclusive gateway", 1, flows.next().getCheckpoints().size());
	}

	@Test
	public void testGetSequenceFlowModelModelElementInstance2() throws Exception {
		ModelElementInstance tested = null;
		try (InputStream in = SequenceFlowParserTest.class.getClassLoader().getResourceAsStream("transitions2.bpmn")) {
			BpmnModelInstanceImpl parseModelFromStream = new BpmnParser().parseModelFromStream(in);
			tested = parseModelFromStream.getModelElementById("EXPWFTS04UPD");
		}
		SequenceFlowModel sequenceFlowModel = SequenceFlowParser.getSequenceFlowModel(tested);
		assertNotNull(sequenceFlowModel.getModel());
		assertEquals("One transition after gateway", 1, sequenceFlowModel.getModel().size());
		assertEquals("Two tasks after the exclusive gateway", 2,
				sequenceFlowModel.getModel().iterator().next().getCheckpoints().size());
	}

	@Test
	public void testGetSequenceFlowModelModelElementInstance3() throws Exception {
		ModelElementInstance tested = null;
		try (InputStream in = SequenceFlowParserTest.class.getClassLoader().getResourceAsStream("transitions3.bpmn")) {
			BpmnModelInstanceImpl parseModelFromStream = new BpmnParser().parseModelFromStream(in);
			tested = parseModelFromStream.getModelElementById("TASKST100");
		}
		SequenceFlowModel sequenceFlowModel = SequenceFlowParser.getSequenceFlowModel(tested);
		Set<SequenceFlowEntry> model = sequenceFlowModel.getModel();
		assertNotNull(model);
		assertEquals("Two transition after gateway", 2, model.size());
		Iterator<SequenceFlowEntry> iterator = model.iterator();
		assertEquals("No tasks after multiinstance task", 0, iterator.next().getCheckpoints().size());
		assertEquals("No tasks after multiinstance task", 0, iterator.next().getCheckpoints().size());
	}

	@Test
	public void test_flow_model_parser_with_two_events() throws Exception {
		ModelElementInstance tested = null;
		try (InputStream in = SequenceFlowParserTest.class.getClassLoader().getResourceAsStream("WFLTYPE11.bpmn")) {
			BpmnModelInstanceImpl parseModelFromStream = new BpmnParser().parseModelFromStream(in);
			tested = parseModelFromStream.getModelElementById("TASKST101");
		}
		SequenceFlowModel sequenceFlowModel = SequenceFlowParser.getSequenceFlowModel(tested);
		Set<SequenceFlowEntry> model = sequenceFlowModel.getModel();
		System.out.println(model);
	}
}
