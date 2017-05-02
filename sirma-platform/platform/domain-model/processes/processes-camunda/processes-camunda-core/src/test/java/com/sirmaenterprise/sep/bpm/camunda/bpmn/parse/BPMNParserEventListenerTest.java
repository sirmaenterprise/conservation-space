package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirmaenterprise.sep.bpm.camunda.service.event.MultiEngineEventListener;

/**
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMNParserEventListenerTest {
	@Mock
	private MultiEngineEventListener engineParseListener;
	@InjectMocks
	private BPMNParserEventListener bPMNParserEventListener;
	@Mock
	private Element startEventElement;
	@Mock
	private ScopeImpl scope;
	@Mock
	private ActivityImpl activity;
	@Mock
	private TaskDefinition taskDefinition;
	@Mock
	private Map<String, List<TaskListener>> taskDefinitions;

	@Before
	public void setUp() throws Exception {
		Mockito.when(taskDefinition.getTaskListeners()).thenReturn(taskDefinitions);
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseStartEvent(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseStartEvent() {
		bPMNParserEventListener.parseStartEvent(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseExclusiveGateway(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseExclusiveGateway() {
		bPMNParserEventListener.parseExclusiveGateway(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseInclusiveGateway(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseInclusiveGateway() {
		bPMNParserEventListener.parseInclusiveGateway(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseParallelGateway(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseParallelGateway() {
		bPMNParserEventListener.parseParallelGateway(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseScriptTask(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseScriptTask() {
		bPMNParserEventListener.parseScriptTask(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseServiceTask(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseServiceTask() {
		bPMNParserEventListener.parseServiceTask(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseBusinessRuleTask(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseBusinessRuleTask() {
		bPMNParserEventListener.parseBusinessRuleTask(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseTask(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseTask() {
		bPMNParserEventListener.parseTask(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseManualTask(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseManualTask() {
		bPMNParserEventListener.parseManualTask(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseUserTask(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseUserTask() {
		UserTaskActivityBehavior userTaskActivityBehavior = mock(UserTaskActivityBehavior.class);
		Mockito.when(activity.getActivityBehavior()).thenReturn(userTaskActivityBehavior);
		Mockito.when(userTaskActivityBehavior.getTaskDefinition()).thenReturn(taskDefinition);
		bPMNParserEventListener.parseUserTask(startEventElement, scope, activity);
		verifyStartEndListeners();
		verify(taskDefinitions).putIfAbsent(eq(TaskListener.EVENTNAME_ASSIGNMENT), Mockito.anyList());
		verify(taskDefinition).getTaskListeners(eq(TaskListener.EVENTNAME_ASSIGNMENT));
		verify(taskDefinitions).putIfAbsent(eq(TaskListener.EVENTNAME_DELETE), Mockito.anyList());
		verify(taskDefinition).getTaskListeners(eq(TaskListener.EVENTNAME_DELETE));
		verify(taskDefinitions).putIfAbsent(eq(TaskListener.EVENTNAME_CREATE), Mockito.anyList());
		verify(taskDefinition).getTaskListeners(eq(TaskListener.EVENTNAME_CREATE));
		verify(taskDefinitions).putIfAbsent(eq(TaskListener.EVENTNAME_COMPLETE), Mockito.anyList());
		verify(taskDefinition).getTaskListeners(eq(TaskListener.EVENTNAME_COMPLETE));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseEndEvent(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseEndEvent() {
		bPMNParserEventListener.parseEndEvent(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseBoundaryTimerEventDefinition(org.camunda.bpm.engine.impl.util.xml.Element, boolean, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseBoundaryTimerEventDefinition() {
		bPMNParserEventListener.parseBoundaryTimerEventDefinition(startEventElement, false, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseBoundaryErrorEventDefinition(org.camunda.bpm.engine.impl.util.xml.Element, boolean, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseBoundaryErrorEventDefinition() {
		ActivityImpl nested = mock(ActivityImpl.class);
		bPMNParserEventListener.parseBoundaryErrorEventDefinition(startEventElement, false, activity, nested);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseSubProcess(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseSubProcess() {
		bPMNParserEventListener.parseSubProcess(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseCallActivity(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseCallActivity() {
		bPMNParserEventListener.parseCallActivity(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseSequenceFlow(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.TransitionImpl)}.
	 */
	@Test
	public void testParseSequenceFlow() {
		TransitionImpl transition = mock(TransitionImpl.class);
		bPMNParserEventListener.parseSequenceFlow(startEventElement, scope, transition);
		verify(transition).addListener(eq(ExecutionListener.EVENTNAME_TAKE), eq(engineParseListener), eq(0));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseSendTask(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseSendTask() {
		bPMNParserEventListener.parseSendTask(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseMultiInstanceLoopCharacteristics(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseMultiInstanceLoopCharacteristics() {
		Element element = mock(Element.class);
		bPMNParserEventListener.parseMultiInstanceLoopCharacteristics(startEventElement, element, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseIntermediateTimerEventDefinition(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseIntermediateTimerEventDefinition() {
		bPMNParserEventListener.parseIntermediateTimerEventDefinition(startEventElement, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseReceiveTask(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseReceiveTask() {
		bPMNParserEventListener.parseReceiveTask(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseIntermediateSignalCatchEventDefinition(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseIntermediateSignalCatchEventDefinition() {
		bPMNParserEventListener.parseIntermediateSignalCatchEventDefinition(startEventElement, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseBoundarySignalEventDefinition(org.camunda.bpm.engine.impl.util.xml.Element, boolean, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseBoundarySignalEventDefinition() {
		bPMNParserEventListener.parseBoundarySignalEventDefinition(startEventElement, false, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseEventBasedGateway(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseEventBasedGateway() {
		bPMNParserEventListener.parseEventBasedGateway(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseTransaction(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseTransaction() {
		bPMNParserEventListener.parseTransaction(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#parseIntermediateThrowEvent(org.camunda.bpm.engine.impl.util.xml.Element, org.camunda.bpm.engine.impl.pvm.process.ScopeImpl, org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testParseIntermediateThrowEvent() {
		bPMNParserEventListener.parseIntermediateThrowEvent(startEventElement, scope, activity);
		verifyStartEndListeners();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#addEndEventListener(org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testAddEndEventListener() {
		bPMNParserEventListener.addEndEventListener(activity);
		verify(activity).addListener(eq(ExecutionListener.EVENTNAME_END), eq(engineParseListener), eq(0));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#addStartEventListener(org.camunda.bpm.engine.impl.pvm.process.ActivityImpl)}.
	 */
	@Test
	public void testAddStartEventListener() {
		bPMNParserEventListener.addStartEventListener(activity);
		verify(activity).addListener(eq(ExecutionListener.EVENTNAME_START), eq(engineParseListener), eq(0));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#addTaskCreateListeners(org.camunda.bpm.engine.impl.task.TaskDefinition)}.
	 */
	@Test
	public void testAddTaskCreateListeners() {
		bPMNParserEventListener.addTaskCreateListeners(taskDefinition);
		verify(taskDefinitions).putIfAbsent(eq(TaskListener.EVENTNAME_CREATE), Mockito.anyList());
		verify(taskDefinition).getTaskListeners(eq(TaskListener.EVENTNAME_CREATE));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#addTaskAssignmentListeners(org.camunda.bpm.engine.impl.task.TaskDefinition)}.
	 */
	@Test
	public void testAddTaskAssignmentListeners() {
		bPMNParserEventListener.addTaskAssignmentListeners(taskDefinition);
		verify(taskDefinitions).putIfAbsent(eq(TaskListener.EVENTNAME_ASSIGNMENT), Mockito.anyList());
		verify(taskDefinition).getTaskListeners(eq(TaskListener.EVENTNAME_ASSIGNMENT));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#addTaskCompleteListeners(org.camunda.bpm.engine.impl.task.TaskDefinition)}.
	 */
	@Test
	public void testAddTaskCompleteListeners() {
		bPMNParserEventListener.addTaskCompleteListeners(taskDefinition);
		verify(taskDefinitions).putIfAbsent(eq(TaskListener.EVENTNAME_COMPLETE), Mockito.anyList());
		verify(taskDefinition).getTaskListeners(eq(TaskListener.EVENTNAME_COMPLETE));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener#addTaskDeleteListeners(org.camunda.bpm.engine.impl.task.TaskDefinition)}.
	 */
	@Test
	public void testAddTaskDeleteListeners() {
		bPMNParserEventListener.addTaskDeleteListeners(taskDefinition);
		verify(taskDefinitions).putIfAbsent(eq(TaskListener.EVENTNAME_DELETE), Mockito.anyList());
		verify(taskDefinition).getTaskListeners(eq(TaskListener.EVENTNAME_DELETE));
	}

	private void verifyStartEndListeners() {
		verify(activity).addListener(eq(ExecutionListener.EVENTNAME_START), eq(engineParseListener), eq(0));
		verify(activity).addListener(eq(ExecutionListener.EVENTNAME_END), eq(engineParseListener), eq(0));
	}
}
