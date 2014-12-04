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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

// TODO: Auto-generated Javadoc
/**
 * Class handling invocations of JavaDelegates.
 *
 * @author Daniel Meyer
 */
public class JavaDelegateInvocation extends DelegateInvocation {

  /** The delegate instance. */
  protected final JavaDelegate delegateInstance;
  
  /** The execution. */
  protected final DelegateExecution execution;

  /**
   * Instantiates a new java delegate invocation.
   *
   * @param delegateInstance the delegate instance
   * @param execution the execution
   */
  public JavaDelegateInvocation(JavaDelegate delegateInstance, DelegateExecution execution) {
    this.delegateInstance = delegateInstance;
    this.execution = execution;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.delegate.DelegateInvocation#invoke()
   */
  protected void invoke() throws Exception {
    delegateInstance.execute((DelegateExecution) execution);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.delegate.DelegateInvocation#getTarget()
   */
  public Object getTarget() {
    return delegateInstance;
  }

}
