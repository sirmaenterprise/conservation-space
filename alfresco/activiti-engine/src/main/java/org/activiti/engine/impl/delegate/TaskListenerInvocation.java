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
package org.activiti.engine.impl.delegate;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

// TODO: Auto-generated Javadoc
/**
 * Class handling invocations of {@link TaskListener TaskListeners}.
 *
 * @author Daniel Meyer
 */
public class TaskListenerInvocation extends DelegateInvocation {

  /** The execution listener instance. */
  protected final TaskListener executionListenerInstance;
  
  /** The delegate task. */
  protected final DelegateTask delegateTask;

  /**
   * Instantiates a new task listener invocation.
   *
   * @param executionListenerInstance the execution listener instance
   * @param delegateTask the delegate task
   */
  public TaskListenerInvocation(TaskListener executionListenerInstance, DelegateTask delegateTask) {
    this.executionListenerInstance = executionListenerInstance;
    this.delegateTask = delegateTask;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.delegate.DelegateInvocation#invoke()
   */
  protected void invoke() throws Exception {
    executionListenerInstance.notify(delegateTask);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.delegate.DelegateInvocation#getTarget()
   */
  public Object getTarget() {
    return executionListenerInstance;
  }

}
