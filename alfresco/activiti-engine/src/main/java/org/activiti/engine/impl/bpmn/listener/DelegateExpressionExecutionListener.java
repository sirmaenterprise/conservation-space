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
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ExecutionListenerInvocation;
import org.activiti.engine.impl.delegate.JavaDelegateInvocation;


// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving delegateExpressionExecution events.
 * The class that is interested in processing a delegateExpressionExecution
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addDelegateExpressionExecutionListener<code> method. When
 * the delegateExpressionExecution event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Joram Barrez
 */
public class DelegateExpressionExecutionListener implements ExecutionListener {
  
  /** The expression. */
  protected Expression expression;
  
  /**
   * Instantiates a new delegate expression execution listener.
   *
   * @param expression the expression
   */
  public DelegateExpressionExecutionListener(Expression expression) {
    this.expression = expression;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.ExecutionListener#notify(org.activiti.engine.delegate.DelegateExecution)
   */
  public void notify(DelegateExecution execution) throws Exception {
    // Note: we can't cache the result of the expression, because the
    // execution can change: eg. delegateExpression='${mySpringBeanFactory.randomSpringBean()}'
    Object delegate = expression.getValue(execution);
    
    if (delegate instanceof ExecutionListener) {
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(new ExecutionListenerInvocation((ExecutionListener) delegate, execution));
    } else if (delegate instanceof JavaDelegate) {
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(new JavaDelegateInvocation((JavaDelegate) delegate, execution));
    } else {
      throw new ActivitiException("Delegate expression " + expression 
              + " did not resolve to an implementation of " + ExecutionListener.class 
              + " nor " + JavaDelegate.class);
    }
  }

  /**
   * returns the expression text for this execution listener. Comes in handy if you want to
   * check which listeners you already have.
   *
   * @return the expression text
   */  
  public String getExpressionText() {
    return expression.getExpressionText();
  }

}
