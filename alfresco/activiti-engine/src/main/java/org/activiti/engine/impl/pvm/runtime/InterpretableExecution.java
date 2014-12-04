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

package org.activiti.engine.impl.pvm.runtime;

import org.activiti.engine.impl.pvm.PvmProcessElement;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;


// TODO: Auto-generated Javadoc
/**
 * The Interface InterpretableExecution.
 *
 * @author Tom Baeyens
 */
public interface InterpretableExecution extends ActivityExecution, ExecutionListenerExecution, PvmProcessInstance {

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#take(org.activiti.engine.impl.pvm.PvmTransition)
   */
  void take(PvmTransition transition);

  /**
   * Sets the event name.
   *
   * @param eventName the new event name
   */
  void setEventName(String eventName);

  /**
   * Sets the event source.
   *
   * @param element the new event source
   */
  void setEventSource(PvmProcessElement element);

  /**
   * Gets the execution listener index.
   *
   * @return the execution listener index
   */
  Integer getExecutionListenerIndex();
  
  /**
   * Sets the execution listener index.
   *
   * @param executionListenerIndex the new execution listener index
   */
  void setExecutionListenerIndex(Integer executionListenerIndex);

  /**
   * Gets the process definition.
   *
   * @return the process definition
   */
  ProcessDefinitionImpl getProcessDefinition();

  /**
   * Sets the activity.
   *
   * @param activity the new activity
   */
  void setActivity(ActivityImpl activity);

  /**
   * Perform operation.
   *
   * @param etomicOperation the etomic operation
   */
  void performOperation(AtomicOperation etomicOperation);

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.delegate.ActivityExecution#isScope()
   */
  boolean isScope();

  /**
   * Destroy.
   */
  void destroy();

  /**
   * Removes the.
   */
  void remove();

  /**
   * Gets the replaced by.
   *
   * @return the replaced by
   */
  InterpretableExecution getReplacedBy();
  
  /**
   * Sets the replaced by.
   *
   * @param replacedBy the new replaced by
   */
  void setReplacedBy(InterpretableExecution replacedBy);

  /**
   * Gets the sub process instance.
   *
   * @return the sub process instance
   */
  InterpretableExecution getSubProcessInstance();
  
  /**
   * Sets the sub process instance.
   *
   * @param subProcessInstance the new sub process instance
   */
  void setSubProcessInstance(InterpretableExecution subProcessInstance);

  /**
   * Gets the super execution.
   *
   * @return the super execution
   */
  InterpretableExecution getSuperExecution();

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmProcessInstance#deleteCascade(java.lang.String)
   */
  void deleteCascade(String deleteReason);
  
  /**
   * Checks if is delete root.
   *
   * @return true, if is delete root
   */
  boolean isDeleteRoot();

  /**
   * Gets the transition.
   *
   * @return the transition
   */
  TransitionImpl getTransition();
  
  /**
   * Sets the transition.
   *
   * @param object the new transition
   */
  void setTransition(TransitionImpl object);

  /**
   * Initialize.
   */
  void initialize();

  /**
   * Sets the parent.
   *
   * @param parent the new parent
   */
  void setParent(InterpretableExecution parent);

  /**
   * Sets the process definition.
   *
   * @param processDefinitionImpl the new process definition
   */
  void setProcessDefinition(ProcessDefinitionImpl processDefinitionImpl);

  /**
   * Sets the process instance.
   *
   * @param processInstance the new process instance
   */
  void setProcessInstance(InterpretableExecution processInstance);
  
  /**
   * Checks if is event scope.
   *
   * @return true, if is event scope
   */
  boolean isEventScope();
  
  /**
   * Sets the event scope.
   *
   * @param isEventScope the new event scope
   */
  void setEventScope(boolean isEventScope);
  
  /**
   * Gets the starting execution.
   *
   * @return the starting execution
   */
  StartingExecution getStartingExecution();
  
  /**
   * Dispose starting execution.
   */
  void disposeStartingExecution();
}
