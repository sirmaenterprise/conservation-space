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

import org.activiti.engine.delegate.VariableScope;


// TODO: Auto-generated Javadoc
/**
 * The Interface Expression.
 *
 * @author Frederik Heremans
 */
public interface Expression extends org.activiti.engine.delegate.Expression {

   /* (non-Javadoc)
    * @see org.activiti.engine.delegate.Expression#getValue(org.activiti.engine.delegate.VariableScope)
    */
   Object getValue(VariableScope variableScope);
   
   /* (non-Javadoc)
    * @see org.activiti.engine.delegate.Expression#setValue(java.lang.Object, org.activiti.engine.delegate.VariableScope)
    */
   void setValue(Object value, VariableScope variableScope);
   
   /* (non-Javadoc)
    * @see org.activiti.engine.delegate.Expression#getExpressionText()
    */
   String getExpressionText();
   
}
