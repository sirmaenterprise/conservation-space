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
package org.activiti.engine.impl.bpmn.listener;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.TaskListenerInvocation;


// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving delegateExpressionTask events.
 * The class that is interested in processing a delegateExpressionTask
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addDelegateExpressionTaskListener<code> method. When
 * the delegateExpressionTask event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Joram Barrez
 */
public class DelegateExpressionTaskListener implements TaskListener {
  
  /** The expression. */
  protected Expression expression;
  
  /**
   * Instantiates a new delegate expression task listener.
   *
   * @param expression the expression
   */
  public DelegateExpressionTaskListener(Expression expression) {
    this.expression = expression;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.delegate.DelegateTask)
   */
  public void notify(DelegateTask delegateTask) {
    // Note: we can't cache the result of the expression, because the
    // execution can change: eg. delegateExpression='${mySpringBeanFactory.randomSpringBean()}'
    Object delegate = expression.getValue(delegateTask.getExecution());
    
    if (delegate instanceof TaskListener) {
      try {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new TaskListenerInvocation((TaskListener)delegate, delegateTask));
      }catch (Exception e) {
        throw new ActivitiException("Exception while invoking TaskListener: "+e.getMessage(), e);
      }
    } else {
      throw new ActivitiException("Delegate expression " + expression 
              + " did not resolve to an implementation of " + TaskListener.class );
    }
  }
  
  /**
   * returns the expression text for this task listener. Comes in handy if you want to
   * check which listeners you already have.
   *
   * @return the expression text
   */  
  public String getExpressionText() {
    return expression.getExpressionText();
  }  

}
