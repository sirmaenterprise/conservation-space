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

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.JavaDelegateInvocation;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


// TODO: Auto-generated Javadoc
/**
 * The Class ServiceTaskJavaDelegateActivityBehavior.
 *
 * @author Tom Baeyens
 */
public class ServiceTaskJavaDelegateActivityBehavior extends TaskActivityBehavior implements ActivityBehavior, ExecutionListener {
  
  /** The java delegate. */
  protected JavaDelegate javaDelegate;
  
  /**
   * Instantiates a new service task java delegate activity behavior.
   */
  protected ServiceTaskJavaDelegateActivityBehavior() {
  }

  /**
   * Instantiates a new service task java delegate activity behavior.
   *
   * @param javaDelegate the java delegate
   */
  public ServiceTaskJavaDelegateActivityBehavior(JavaDelegate javaDelegate) {
    this.javaDelegate = javaDelegate;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.behavior.FlowNodeActivityBehavior#execute(org.activiti.engine.impl.pvm.delegate.ActivityExecution)
   */
  public void execute(ActivityExecution execution) throws Exception {
    execute((DelegateExecution) execution);
    leave(execution);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.ExecutionListener#notify(org.activiti.engine.delegate.DelegateExecution)
   */
  public void notify(DelegateExecution execution) throws Exception {
    execute((DelegateExecution) execution);
  }
  
  /**
   * Execute.
   *
   * @param execution the execution
   * @throws Exception the exception
   */
  public void execute(DelegateExecution execution) throws Exception {
    Context.getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(new JavaDelegateInvocation(javaDelegate, execution));    
  }
}
