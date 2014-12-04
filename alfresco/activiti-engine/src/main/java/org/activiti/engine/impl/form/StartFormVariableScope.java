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

package org.activiti.engine.impl.form;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.VariableScope;


// TODO: Auto-generated Javadoc
/**
 * Variable-scope only used to resolve variables when NO execution is active but
 * expression-resolving is needed. This occurs when start-form properties have default's
 * defined. Even though variables are not available yet, expressions should be resolved 
 * anyway.
 * 
 * @author Frederik Heremans
 */
public class StartFormVariableScope implements VariableScope {
  
  /** The Constant INSTANCE. */
  private static final StartFormVariableScope INSTANCE = new StartFormVariableScope();

  /**
   * Since a {@link StartFormVariableScope} has no state, it's safe to use the same
   * instance to prevent too many useless instances created.
   *
   * @return the shared instance
   */
  public static StartFormVariableScope getSharedInstance()  {
    return INSTANCE;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariables()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> getVariables() {
    return Collections.EMPTY_MAP;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariablesLocal()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> getVariablesLocal() {
    return Collections.EMPTY_MAP;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariable(java.lang.String)
   */
  @Override
  public Object getVariable(String variableName) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableLocal(java.lang.Object)
   */
  @Override
  public Object getVariableLocal(Object variableName) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableNames()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Set<String> getVariableNames() {
    return Collections.EMPTY_SET;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#getVariableNamesLocal()
   */
  @Override
  public Set<String> getVariableNamesLocal() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariable(java.lang.String, java.lang.Object)
   */
  @Override
  public void setVariable(String variableName, Object value) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariableLocal(java.lang.String, java.lang.Object)
   */
  @Override
  public Object setVariableLocal(String variableName, Object value) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariables(java.util.Map)
   */
  @Override
  public void setVariables(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#setVariablesLocal(java.util.Map)
   */
  @Override
  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException("No execution active, no variables can be set");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariables()
   */
  @Override
  public boolean hasVariables() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariablesLocal()
   */
  @Override
  public boolean hasVariablesLocal() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariable(java.lang.String)
   */
  @Override
  public boolean hasVariable(String variableName) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#hasVariableLocal(java.lang.String)
   */
  @Override
  public boolean hasVariableLocal(String variableName) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#createVariableLocal(java.lang.String, java.lang.Object)
   */
  @Override
  public void createVariableLocal(String variableName, Object value) {
    throw new UnsupportedOperationException("No execution active, no variables can be created");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#createVariablesLocal(java.util.Map)
   */
  @Override
  public void createVariablesLocal(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException("No execution active, no variables can be created");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariable(java.lang.String)
   */
  @Override
  public void removeVariable(String variableName) {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariableLocal(java.lang.String)
   */
  @Override
  public void removeVariableLocal(String variableName) {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariables()
   */
  @Override
  public void removeVariables() {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.delegate.VariableScope#removeVariablesLocal()
   */
  @Override
  public void removeVariablesLocal() {
    throw new UnsupportedOperationException("No execution active, no variables can be removed");
  }

}
