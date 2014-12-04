/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Listener which can be registered within the engine to receive events during parsing (and
 * maybe influence ist). Instead of implmenting this interface you migh consider to extend 
 * the {@link AbstractBpmnParseListener}, which contains an empty implementation for all methods
 * and makes your implementation easier and more robust to future changes.
 * 
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Joram Barrez
 */
public interface BpmnParseListener {

  /**
   * Parses the process.
   *
   * @param processElement the process element
   * @param processDefinition the process definition
   */
  void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition);
  
  /**
   * Parses the start event.
   *
   * @param startEventElement the start event element
   * @param scope the scope
   * @param startEventActivity the start event activity
   */
  void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity);
  
  /**
   * Parses the exclusive gateway.
   *
   * @param exclusiveGwElement the exclusive gw element
   * @param scope the scope
   * @param activity the activity
   */
  void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the inclusive gateway.
   *
   * @param inclusiveGwElement the inclusive gw element
   * @param scope the scope
   * @param activity the activity
   */
  void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the parallel gateway.
   *
   * @param parallelGwElement the parallel gw element
   * @param scope the scope
   * @param activity the activity
   */
  void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the script task.
   *
   * @param scriptTaskElement the script task element
   * @param scope the scope
   * @param activity the activity
   */
  void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the service task.
   *
   * @param serviceTaskElement the service task element
   * @param scope the scope
   * @param activity the activity
   */
  void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the business rule task.
   *
   * @param businessRuleTaskElement the business rule task element
   * @param scope the scope
   * @param activity the activity
   */
  void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the task.
   *
   * @param taskElement the task element
   * @param scope the scope
   * @param activity the activity
   */
  void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the manual task.
   *
   * @param manualTaskElement the manual task element
   * @param scope the scope
   * @param activity the activity
   */
  void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the user task.
   *
   * @param userTaskElement the user task element
   * @param scope the scope
   * @param activity the activity
   */
  void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the end event.
   *
   * @param endEventElement the end event element
   * @param scope the scope
   * @param activity the activity
   */
  void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the boundary timer event definition.
   *
   * @param timerEventDefinition the timer event definition
   * @param interrupting the interrupting
   * @param timerActivity the timer activity
   */
  void parseBoundaryTimerEventDefinition(Element timerEventDefinition, boolean interrupting, ActivityImpl timerActivity);
  
  /**
   * Parses the boundary error event definition.
   *
   * @param errorEventDefinition the error event definition
   * @param interrupting the interrupting
   * @param activity the activity
   * @param nestedErrorEventActivity the nested error event activity
   */
  void parseBoundaryErrorEventDefinition(Element errorEventDefinition, boolean interrupting, ActivityImpl activity, ActivityImpl nestedErrorEventActivity);
  
  /**
   * Parses the sub process.
   *
   * @param subProcessElement the sub process element
   * @param scope the scope
   * @param activity the activity
   */
  void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the call activity.
   *
   * @param callActivityElement the call activity element
   * @param scope the scope
   * @param activity the activity
   */
  void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the property.
   *
   * @param propertyElement the property element
   * @param variableDeclaration the variable declaration
   * @param activity the activity
   */
  void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration, ActivityImpl activity);
  
  /**
   * Parses the sequence flow.
   *
   * @param sequenceFlowElement the sequence flow element
   * @param scopeElement the scope element
   * @param transition the transition
   */
  void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition);
  
  /**
   * Parses the send task.
   *
   * @param sendTaskElement the send task element
   * @param scope the scope
   * @param activity the activity
   */
  void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the multi instance loop characteristics.
   *
   * @param activityElement the activity element
   * @param multiInstanceLoopCharacteristicsElement the multi instance loop characteristics element
   * @param activity the activity
   */
  void parseMultiInstanceLoopCharacteristics(Element activityElement, Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity);
  
  /**
   * Parses the intermediate timer event definition.
   *
   * @param timerEventDefinition the timer event definition
   * @param timerActivity the timer activity
   */
  void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity);
  
  /**
   * Parses the root element.
   *
   * @param rootElement the root element
   * @param processDefinitions the process definitions
   */
  void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions);
  
  /**
   * Parses the receive task.
   *
   * @param receiveTaskElement the receive task element
   * @param scope the scope
   * @param activity the activity
   */
  void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the intermediate signal catch event definition.
   *
   * @param signalEventDefinition the signal event definition
   * @param signalActivity the signal activity
   */
  void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity);
  
  /**
   * Parses the intermediate message catch event definition.
   *
   * @param messageEventDefinition the message event definition
   * @param nestedActivity the nested activity
   */
  void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity);
  
  /**
   * Parses the boundary signal event definition.
   *
   * @param signalEventDefinition the signal event definition
   * @param interrupting the interrupting
   * @param signalActivity the signal activity
   */
  void parseBoundarySignalEventDefinition(Element signalEventDefinition, boolean interrupting, ActivityImpl signalActivity);
  
  /**
   * Parses the event based gateway.
   *
   * @param eventBasedGwElement the event based gw element
   * @param scope the scope
   * @param activity the activity
   */
  void parseEventBasedGateway(Element eventBasedGwElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the transaction.
   *
   * @param transactionElement the transaction element
   * @param scope the scope
   * @param activity the activity
   */
  void parseTransaction(Element transactionElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the compensate event definition.
   *
   * @param compensateEventDefinition the compensate event definition
   * @param compensationActivity the compensation activity
   */
  void parseCompensateEventDefinition(Element compensateEventDefinition, ActivityImpl compensationActivity);
  
  /**
   * Parses the intermediate throw event.
   *
   * @param intermediateEventElement the intermediate event element
   * @param scope the scope
   * @param activity the activity
   */
  void parseIntermediateThrowEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the intermediate catch event.
   *
   * @param intermediateEventElement the intermediate event element
   * @param scope the scope
   * @param activity the activity
   */
  void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity);
  
  /**
   * Parses the boundary event.
   *
   * @param boundaryEventElement the boundary event element
   * @param scopeElement the scope element
   * @param nestedActivity the nested activity
   */
  void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl nestedActivity);

}
