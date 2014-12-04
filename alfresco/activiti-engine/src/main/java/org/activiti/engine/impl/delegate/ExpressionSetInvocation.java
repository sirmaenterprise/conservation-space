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

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ValueExpression;

// TODO: Auto-generated Javadoc
/**
 * Class responsible for handling Expression.setValue() invocations.
 * 
 * @author Daniel Meyer
 */
public class ExpressionSetInvocation extends ExpressionInvocation {
    
  /** The value. */
  protected final Object value;
  
  /** The el context. */
  protected ELContext elContext;

  /**
   * Instantiates a new expression set invocation.
   *
   * @param valueExpression the value expression
   * @param elContext the el context
   * @param value the value
   */
  public ExpressionSetInvocation(ValueExpression valueExpression, ELContext elContext, Object value) {
    super(valueExpression);
    this.value = value;
    this.elContext = elContext;
    this.invocationParameters = new Object[] {value};
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.delegate.DelegateInvocation#invoke()
   */
  @Override
  protected void invoke() throws Exception {
    valueExpression.setValue(elContext, value);
  }

}
