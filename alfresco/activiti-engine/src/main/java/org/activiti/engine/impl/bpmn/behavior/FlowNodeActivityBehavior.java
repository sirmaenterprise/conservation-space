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
package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;


// TODO: Auto-generated Javadoc
/**
 * Superclass for all 'connectable' BPMN 2.0 process elements: tasks, gateways and events.
 * This means that any subclass can be the source or target of a sequenceflow.
 * 
 * Corresponds with the notion of the 'flownode' in BPMN 2.0.
 * 
 * @author Joram Barrez
 */
public abstract class FlowNodeActivityBehavior implements SignallableActivityBehavior {
  
  /** The bpmn activity behavior. */
  protected BpmnActivityBehavior bpmnActivityBehavior = new BpmnActivityBehavior();
  
  /**
   * Default behaviour: just leave the activity with no extra functionality.
   *
   * @param execution the execution
   * @throws Exception the exception
   */
  public void execute(ActivityExecution execution) throws Exception {
    leave(execution);
  }
  
  /**
   * Default way of leaving a BPMN 2.0 activity: evaluate the conditions on the
   * outgoing sequence flow and take those that evaluate to true.
   *
   * @param execution the execution
   */
  protected void leave(ActivityExecution execution) {
    bpmnActivityBehavior.performDefaultOutgoingBehavior(execution);
  }
  
  /**
   * Leave ignore conditions.
   *
   * @param activityContext the activity context
   */
  protected void leaveIgnoreConditions(ActivityExecution activityContext) {
    bpmnActivityBehavior.performIgnoreConditionsOutgoingBehavior(activityContext);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior#signal(org.activiti.engine.impl.pvm.delegate.ActivityExecution, java.lang.String, java.lang.Object)
   */
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    // concrete activity behaviours that do accept signals should override this method;
    throw new ActivitiException("this activity doesn't accept signals");
  }
  
}
