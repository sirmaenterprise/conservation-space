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
package org.activiti.engine.impl.pvm.delegate;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.PvmTransition;

// TODO: Auto-generated Javadoc
/**
 * The Interface ActivityExecution.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface ActivityExecution extends DelegateExecution {
  
  /* Process instance/activity/transition retrieval */

  /**
   * returns the current {@link PvmActivity} of the execution.
   *
   * @return the activity
   */
  PvmActivity getActivity();
  
  /**
   * leaves the current activity by taking the given transition.
   *
   * @param transition the transition
   */
  void take(PvmTransition transition);
  
  
  /* Execution management */
  
  /**
   * creates a new execution. This execution will be the parent of the newly created execution.
   * properties processDefinition, processInstance and activity will be initialized.
   *
   * @return the activity execution
   */
  ActivityExecution createExecution();
  
  /**
   * creates a new sub process instance.
   * The current execution will be the super execution of the created execution.
   *
   * @param processDefinition The {@link PvmProcessDefinition} of the subprocess.
   * @return the pvm process instance
   */
  PvmProcessInstance createSubProcessInstance(PvmProcessDefinition processDefinition);
  
  /**
   * returns the parent of this execution, or null if there no parent.
   *
   * @return the parent
   */
  ActivityExecution getParent();
  
  /**
   * returns the list of execution of which this execution the parent of.
   *
   * @return the executions
   */
  List<? extends ActivityExecution> getExecutions();
  
  /**
   * ends this execution.
   */
  void end();
  
  
  /* State management */
  
  /**
   * makes this execution active or inactive.
   *
   * @param isActive the new active
   */
  void setActive(boolean isActive);
  
  /**
   * returns whether this execution is currently active.
   *
   * @return true, if is active
   */
  boolean isActive();
  
  /**
   * returns whether this execution has ended or not.
   *
   * @return true, if is ended
   */
  boolean isEnded();
  
  /**
   * changes the concurrent indicator on this execution.
   *
   * @param isConcurrent the new concurrent
   */
  void setConcurrent(boolean isConcurrent);
  
  /**
   * returns whether this execution is concurrent or not.
   *
   * @return true, if is concurrent
   */
  boolean isConcurrent();
  
  /**
   * returns whether this execution is a process instance or not.
   *
   * @return true, if is process instance
   */
  boolean isProcessInstance();

  /**
   * Inactivates this execution.
   * This is useful for example in a join: the execution
   * still exists, but it is not longer active.
   */
  void inactivate();
  
  /**
   * Returns whether this execution is a scope.
   *
   * @return true, if is scope
   */
  boolean isScope();
  
  /**
   * Changes whether this execution is a scope or not.
   *
   * @param isScope the new scope
   */
  void setScope(boolean isScope);

  /**
   * Retrieves all executions which are concurrent and inactive at the given activity.
   *
   * @param activity the activity
   * @return the list
   */
  List<ActivityExecution> findInactiveConcurrentExecutions(PvmActivity activity);
  
  /**
   * Takes the given outgoing transitions, and potentially reusing
   * the given list of executions that were previously joined.
   *
   * @param outgoingTransitions the outgoing transitions
   * @param joinedExecutions the joined executions
   */
  void takeAll(List<PvmTransition> outgoingTransitions, List<ActivityExecution> joinedExecutions);

  /**
   * Executes the {@link ActivityBehavior} associated with the given activity.
   *
   * @param activity the activity
   */
  void executeActivity(PvmActivity activity);
}
