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

package org.activiti.engine.impl.el;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ExpressionGetInvocation;
import org.activiti.engine.impl.delegate.ExpressionSetInvocation;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.MethodNotFoundException;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.javax.el.ValueExpression;


// TODO: Auto-generated Javadoc
/**
 * Expression implementation backed by a JUEL {@link ValueExpression}.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class JuelExpression implements Expression {

  /** The expression text. */
  protected String expressionText;
  
  /** The value expression. */
  protected ValueExpression valueExpression;
  
  /** The expression manager. */
  protected ExpressionManager expressionManager;
  
  /**
   * Instantiates a new juel expression.
   *
   * @param valueExpression the value expression
   * @param expressionManager the expression manager
   * @param expressionText the expression text
   */
  public JuelExpression(ValueExpression valueExpression, ExpressionManager expressionManager, String expressionText) {
    this.valueExpression = valueExpression;
    this.expressionManager = expressionManager;
    this.expressionText = expressionText;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.el.Expression#getValue(org.activiti.engine.delegate.VariableScope)
   */
  public Object getValue(VariableScope variableScope) {
    ELContext elContext = expressionManager.getElContext(variableScope);
    try {
      ExpressionGetInvocation invocation = new ExpressionGetInvocation(valueExpression, elContext);
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);
      return invocation.getInvocationResult();      
    } catch (PropertyNotFoundException pnfe) {
      throw new ActivitiException("Unknown property used in expression", pnfe);
    } catch (MethodNotFoundException mnfe) {
      throw new ActivitiException("Unknown method used in expression", mnfe);
    } catch(ELException ele) {
      throw new ActivitiException("Error while evalutaing expression", ele);
    } catch (Exception e) {
      throw new ActivitiException("Error while evalutaing expression", e);
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.el.Expression#setValue(java.lang.Object, org.activiti.engine.delegate.VariableScope)
   */
  public void setValue(Object value, VariableScope variableScope) {
    ELContext elContext = expressionManager.getElContext(variableScope);
    try {
      ExpressionSetInvocation invocation = new ExpressionSetInvocation(valueExpression, elContext, value);
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);
    }catch (Exception e) {
      throw new ActivitiException("Error while evalutaing expression", e);
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if(valueExpression != null) {
      return valueExpression.getExpressionString();
    }
    return super.toString();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.el.Expression#getExpressionText()
   */
  public String getExpressionText() {
    return expressionText;
  }
}
