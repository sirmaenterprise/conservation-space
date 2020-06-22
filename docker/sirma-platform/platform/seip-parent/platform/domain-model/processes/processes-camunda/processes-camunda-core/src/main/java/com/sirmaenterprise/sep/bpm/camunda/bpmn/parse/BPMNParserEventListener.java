package com.sirmaenterprise.sep.bpm.camunda.bpmn.parse;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.cdi.impl.event.CdiEventSupportBpmnParseListener;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.variable.VariableDeclaration;

import com.sirmaenterprise.sep.bpm.camunda.service.event.MultiEngineEventListener;

/**
 * Parser that registers the default CDI {@link MultiEngineEventListener} implementation as event listener. Not all
 * {@link BpmnParseListener} methods are registered, however methods are overridden to stop default behavior. The
 * registered listener is the first in order among all listeners.
 * 
 * @author bbanchev
 */
@SuppressWarnings("deprecation")
@Singleton
public class BPMNParserEventListener extends CdiEventSupportBpmnParseListener {
	@Inject
	private MultiEngineEventListener engineParseListener;

	@Override
	public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
		addStartEventListener(startEventActivity);
		addEndEventListener(startEventActivity);
	}

	@Override
	public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
		UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
		TaskDefinition taskDefinition = activityBehavior.getTaskDefinition();
		addTaskCreateListeners(taskDefinition);
		addTaskAssignmentListeners(taskDefinition);
		addTaskCompleteListeners(taskDefinition);
		addTaskDeleteListeners(taskDefinition);
	}

	@Override
	public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting,
			ActivityImpl timerActivity) {
		addStartEventListener(timerActivity);
		addEndEventListener(timerActivity);
	}

	@Override
	public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting,
			ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
		transition.addListener(ExecutionListener.EVENTNAME_TAKE, engineParseListener, 0);
	}

	@Override
	public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseMultiInstanceLoopCharacteristics(Element activityElement,
			Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
		addStartEventListener(timerActivity);
		addEndEventListener(timerActivity);
	}

	@Override
	public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition,
			ActivityImpl signalActivity) {
		addStartEventListener(signalActivity);
		addEndEventListener(signalActivity);
	}

	@Override
	public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting,
			ActivityImpl signalActivity) {
		addStartEventListener(signalActivity);
		addEndEventListener(signalActivity);
	}

	@Override
	public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
		addStartEventListener(activity);
		addEndEventListener(activity);
	}

	@Override
	protected void addEndEventListener(ActivityImpl activity) {
		activity.addListener(ExecutionListener.EVENTNAME_END, engineParseListener, 0);
	}

	@Override
	protected void addStartEventListener(ActivityImpl activity) {
		activity.addListener(ExecutionListener.EVENTNAME_START, engineParseListener, 0);
	}

	@Override
	protected void addTaskCreateListeners(TaskDefinition taskDefinition) {
		addTaskListener(taskDefinition, TaskListener.EVENTNAME_CREATE);
	}

	@Override
	protected void addTaskAssignmentListeners(TaskDefinition taskDefinition) {
		addTaskListener(taskDefinition, TaskListener.EVENTNAME_ASSIGNMENT);
	}

	@Override
	protected void addTaskCompleteListeners(TaskDefinition taskDefinition) {
		addTaskListener(taskDefinition, TaskListener.EVENTNAME_COMPLETE);
	}

	@Override
	protected void addTaskDeleteListeners(TaskDefinition taskDefinition) {
		addTaskListener(taskDefinition, TaskListener.EVENTNAME_DELETE);
	}

	private void addTaskListener(TaskDefinition taskDefinition, String eventName) {
		taskDefinition.getTaskListeners().putIfAbsent(eventName, new ArrayList<>());
		taskDefinition.getTaskListeners(eventName).add(0, engineParseListener);
	}

	@Override
	public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
		// stop default behavior
	}

	@Override
	public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
		// stop default behavior
	}

	@Override
	public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition,
			ActivityImpl nestedActivity) {
		// stop default behavior
	}

	@Override
	public void parseBoundaryMessageEventDefinition(Element element, boolean interrupting,
			ActivityImpl messageActivity) {
		// stop default behavior
	}

	@Override
	public void parseBoundaryEscalationEventDefinition(Element escalationEventDefinition, boolean interrupting,
			ActivityImpl boundaryEventActivity) {
		// stop default behavior
	}

	@Override
	public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
		// stop default behavior
	}

	@Override
	public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
		// stop default behavior
	}

	@Override
	public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
		// stop default behavior
	}

	@Override
	public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
		// stop default behavior
	}

}
