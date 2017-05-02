package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.AtMost;

public class SepBpmnParseTest {

	private SepBpmnParse sepBpmnParse;

	@Before
	public void setUp() throws Exception {
		BpmnParser bpmnParser = new BpmnParser(mock(ExpressionManager.class), mock(BpmnParseFactory.class));
		sepBpmnParse = new SepBpmnParse(bpmnParser);
	}

	@Test
	public void testParseActivityInputOutputOnSupported_withNoElements() throws Exception {

		Element activityElement = new Element(null, null, "Task", null, null);
		ActivityImpl activity = mock(ActivityImpl.class);
		sepBpmnParse.parseActivityInputOutput(activityElement, activity);
		Assert.assertEquals(1, activityElement.elements().size());
		Element element = activityElement.elements().iterator().next();
		Assert.assertEquals("extensionElements", element.getTagName());

		Assert.assertEquals(1, element.elements().size());
		Element io = element.elements().iterator().next();
		Assert.assertEquals("inputOutput", io.getTagName());
		verify(activity).setIoMapping(any());

	}

	@Test
	public void testParseActivityInputOutputOnSupported_withPartialInit() throws Exception {

		Element activityElement = new Element(null, null, "Task", null, null);
		activityElement.add(new Element(null, null, "extensionElements", null, null));
		ActivityImpl activity = mock(ActivityImpl.class);
		sepBpmnParse.parseActivityInputOutput(activityElement, activity);
		assertEquals(1, activityElement.elements().size());
		Element element = activityElement.elements().iterator().next();
		Assert.assertEquals("extensionElements", element.getTagName());

		assertEquals(1, element.elements().size());
		Element io = element.elements().iterator().next();
		assertEquals("inputOutput", io.getTagName());
		verify(activity).setIoMapping(any());

	}

	@Test
	public void testParseActivityInputOutputNotSupported() throws Exception {

		Element activityElement = new Element(null, null, "ExlusiveGateway", null, null);
		ActivityImpl activity = mock(ActivityImpl.class);
		sepBpmnParse.parseActivityInputOutput(activityElement, activity);
		assertEquals(0, activityElement.elements().size());
		verify(activity, new AtMost(0)).setIoMapping(any());

	}
}
