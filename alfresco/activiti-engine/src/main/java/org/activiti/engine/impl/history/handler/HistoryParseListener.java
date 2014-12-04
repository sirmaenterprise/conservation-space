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

package org.activiti.engine.impl.history.handler;

import java.util.List;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving historyParse events.
 * The class that is interested in processing a historyParse
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addHistoryParseListener<code> method. When
 * the historyParse event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Falko Menge
 * @author Bernd Ruecker (camunda)
 */
public class HistoryParseListener implements BpmnParseListener {

  /** The Constant START_EVENT_END_HANDLER. */
  protected static final StartEventEndHandler START_EVENT_END_HANDLER = new StartEventEndHandler();

  /** The Constant ACTIVITI_INSTANCE_END_LISTENER. */
  protected static final ActivityInstanceEndHandler ACTIVITI_INSTANCE_END_LISTENER = new ActivityInstanceEndHandler();

  /** The Constant ACTIVITY_INSTANCE_START_LISTENER. */
  protected static final ActivityInstanceStartHandler ACTIVITY_INSTANCE_START_LISTENER = new ActivityInstanceStartHandler();

  /** The Constant USER_TASK_ASSIGNMENT_HANDLER. */
  protected static final UserTaskAssignmentHandler USER_TASK_ASSIGNMENT_HANDLER = new UserTaskAssignmentHandler();

  // The history level set in the Activiti configuration
  /** The history level. */
  protected int historyLevel;

  /**
   * Instantiates a new history parse listener.
   *
   * @param historyLevel the history level
   */
  public HistoryParseListener(int historyLevel) {
    this.historyLevel = historyLevel;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseProcess(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity)
   */
  public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
    if (activityHistoryEnabled(processDefinition, historyLevel)) {
      processDefinition.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, new ProcessInstanceEndHandler());
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseExclusiveGateway(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseInclusiveGateway(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseInclusiveGateway(Element inclusiveGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseCallActivity(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseManualTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseReceiveTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseReceiveTask(Element receiveTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseScriptTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseUserTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);

    if (activityHistoryEnabled(scope, historyLevel)) {
      TaskDefinition taskDefinition = ((UserTaskActivityBehavior) activity.getActivityBehavior()).getTaskDefinition();
      taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, USER_TASK_ASSIGNMENT_HANDLER);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseServiceTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseBusinessRuleTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseSubProcess(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseStartEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl activity) {
    if (activityHistoryEnabled(activity, historyLevel)) {
      activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, START_EVENT_END_HANDLER);
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseSendTask(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseEndEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseParallelGateway(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);    
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
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateTimerEventDefinition(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateTimerEventDefinition(Element timerEventDefinition, ActivityImpl timerActivity) {
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
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseRootElement(org.activiti.engine.impl.util.xml.Element, java.util.List)
   */
  public void parseRootElement(Element rootElement, List<ProcessDefinitionEntity> processDefinitions) {
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
    // TODO: Shall we add audit logging here as well? 
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseMultiInstanceLoopCharacteristics(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseMultiInstanceLoopCharacteristics(Element activityElement, 
          Element multiInstanceLoopCharacteristicsElement, ActivityImpl activity) {
    // Remove any history parse listeners already attached: the Multi instance behavior will
    // call them for every instance that will be created
  }

  // helper methods ///////////////////////////////////////////////////////////

  /**
   * Adds the activity handlers.
   *
   * @param activity the activity
   */
  protected void addActivityHandlers(ActivityImpl activity) {
    if (activityHistoryEnabled(activity, historyLevel)) {
      activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, ACTIVITY_INSTANCE_START_LISTENER, 0);
      activity.addExecutionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, ACTIVITI_INSTANCE_END_LISTENER);
    }
  }

  /**
   * Full history enabled.
   *
   * @param historyLevel the history level
   * @return true, if successful
   */
  public static boolean fullHistoryEnabled(int historyLevel) {
    return historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL;
  }

  /**
   * Audit history enabled.
   *
   * @param scopeElement the scope element
   * @param historyLevel the history level
   * @return true, if successful
   */
  public static boolean auditHistoryEnabled(ScopeImpl scopeElement, int historyLevel) {
    return historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT;
  }

  /**
   * Activity history enabled.
   *
   * @param scopeElement the scope element
   * @param historyLevel the history level
   * @return true, if successful
   */
  public static boolean activityHistoryEnabled(ScopeImpl scopeElement, int historyLevel) {
    return historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateSignalCatchEventDefinition(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateSignalCatchEventDefinition(Element signalEventDefinition, ActivityImpl signalActivity) {
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
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateCatchEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateCatchEvent(Element intermediateEventElement, ScopeImpl scope, ActivityImpl activity) {
    addActivityHandlers(activity);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseBoundaryEvent(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ScopeImpl, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseBoundaryEvent(Element boundaryEventElement, ScopeImpl scopeElement, ActivityImpl activity) {
    // TODO: Add to audit logging? Discuss
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.parser.BpmnParseListener#parseIntermediateMessageCatchEventDefinition(org.activiti.engine.impl.util.xml.Element, org.activiti.engine.impl.pvm.process.ActivityImpl)
   */
  public void parseIntermediateMessageCatchEventDefinition(Element messageEventDefinition, ActivityImpl nestedActivity) {
  }

}
