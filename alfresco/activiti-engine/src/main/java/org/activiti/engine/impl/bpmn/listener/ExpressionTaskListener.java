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

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;


// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving expressionTask events.
 * The class that is interested in processing a expressionTask
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addExpressionTaskListener<code> method. When
 * the expressionTask event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Joram Barrez
 */
public class ExpressionTaskListener implements TaskListener {
  
  /** The expression. */
  protected Expression expression;
  
  /**
   * Instantiates a new expression task listener.
   *
   * @param expression the expression
   */
  public ExpressionTaskListener(Expression expression) {
    this.expression = expression;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.delegate.DelegateTask)
   */
  public void notify(DelegateTask delegateTask) {
      expression.getValue(delegateTask);
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
