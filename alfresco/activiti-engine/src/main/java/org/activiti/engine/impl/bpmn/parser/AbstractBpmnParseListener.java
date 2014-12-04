package org.activiti.engine.impl.bpmn.parser;

import java.util.List;

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;

// TODO: Auto-generated Javadoc
/**
 * Abstract base class for implementing a {@link BpmnParseListener} without being forced to implement
 * all methods provided, which makes the implementation more robust to future changes.
 * 
 * @author ruecker
 */
public class AbstractBpmnParseListener implements BpmnParseListener {

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseProcess(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity)
   */
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseStartEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseExclusiveGateway(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseInclusiveGateway(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseParallelGateway(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseScriptTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseServiceTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseBusinessRuleTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseManualTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseUserTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseEndEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseBoundaryTimerEventDefinition(org.activiti.engine.impl.util.xml.Element, boolean, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseBoundaryErrorEventDefinition(org.activiti.engine.impl.util.xml.Element, boolean, org.activiti.engine.impl.pvm.process.ActivityImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseSubProcess(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseCallActivity(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseProperty(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.variable.VariableDeclaration, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseSequenceFlow(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.TransitionImpl)
   */
  public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseSendTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseMultiInstanceLoopCharacteristics(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateTimerEventDefinition(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseRootElement(org.activiti.engine.impl.util.xml.Element, java.util.List)
   */
  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseReceiveTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateSignalCatchEventDefinition(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseBoundarySignalEventDefinition(org.activiti.engine.impl.util.xml.Element, boolean, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseEventBasedGateway(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseTransaction(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseCompensateEventDefinition(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateThrowEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateCatchEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseBoundaryEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity) {
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateMessageCatchEventDefinition(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
  }

}
