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
package org.activiti.engine.impl.scripting;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;


// TODO: Auto-generated Javadoc
/**
 * Bindings implementation using an {@link ExecutionImpl} as 'back-end'.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class VariableScopeResolver implements Resolver {

  /** The variable scope. */
  protected VariableScope variableScope;
  
  /** The variable scope key. */
  protected String variableScopeKey = "execution";
  
  /**
   * Instantiates a new variable scope resolver.
   *
   * @param variableScope the variable scope
   */
  public VariableScopeResolver(VariableScope variableScope) {
    if (variableScope==null) {
      throw new ActivitiException("variableScope cannot be null");
    }
    if (variableScope instanceof ExecutionEntity) {
      variableScopeKey = "execution";
    } else if (variableScope instanceof TaskEntity){
      variableScopeKey = "task";
    } else {
      throw new ActivitiException("unsupported variable scope type: "+variableScope.getClass().getName());
    }
    this.variableScope = variableScope;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.scripting.Resolver#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key) {
    return variableScopeKey.equals(key) || variableScope.hasVariable((String) key);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.scripting.Resolver#get(java.lang.Object)
   */
  public Object get(Object key) {
    if (variableScopeKey.equals(key)) {
      return variableScope;
    }
    
    return variableScope.getVariable((String) key);
  }
}
