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

package org.activiti.engine.delegate;

import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Interface VariableScope.
 *
 * @author Tom Baeyens
 */
public interface VariableScope {

  /**
   * Gets the variables.
   *
   * @return the variables
   */
  Map<String, Object> getVariables();

  /**
   * Gets the variables local.
   *
   * @return the variables local
   */
  Map<String, Object> getVariablesLocal();

  /**
   * Gets the variable.
   *
   * @param variableName the variable name
   * @return the variable
   */
  Object getVariable(String variableName);

  /**
   * Gets the variable local.
   *
   * @param variableName the variable name
   * @return the variable local
   */
  Object getVariableLocal(Object variableName);

  /**
   * Gets the variable names.
   *
   * @return the variable names
   */
  Set<String> getVariableNames();

  /**
   * Gets the variable names local.
   *
   * @return the variable names local
   */
  Set<String> getVariableNamesLocal();

  /**
   * Sets the variable.
   *
   * @param variableName the variable name
   * @param value the value
   */
  void setVariable(String variableName, Object value);

  /**
   * Sets the variable local.
   *
   * @param variableName the variable name
   * @param value the value
   * @return the object
   */
  Object setVariableLocal(String variableName, Object value);

  /**
   * Sets the variables.
   *
   * @param variables the variables
   */
  void setVariables(Map<String, ? extends Object> variables);

  /**
   * Sets the variables local.
   *
   * @param variables the variables
   */
  void setVariablesLocal(Map<String, ? extends Object> variables);

  /**
   * Checks for variables.
   *
   * @return true, if successful
   */
  boolean hasVariables();

  /**
   * Checks for variables local.
   *
   * @return true, if successful
   */
  boolean hasVariablesLocal();

  /**
   * Checks for variable.
   *
   * @param variableName the variable name
   * @return true, if successful
   */
  boolean hasVariable(String variableName);

  /**
   * Checks for variable local.
   *
   * @param variableName the variable name
   * @return true, if successful
   */
  boolean hasVariableLocal(String variableName);

  /**
   * Creates the variable local.
   *
   * @param variableName the variable name
   * @param value the value
   */
  void createVariableLocal(String variableName, Object value);

  /**
   * Creates the variables local.
   *
   * @param variables the variables
   */
  void createVariablesLocal(Map<String, ? extends Object> variables);

  /**
   * Removes the variable.
   *
   * @param variableName the variable name
   */
  void removeVariable(String variableName);

  /**
   * Removes the variable local.
   *
   * @param variableName the variable name
   */
  void removeVariableLocal(String variableName);

  /**
   * Removes the variables.
   */
  void removeVariables();

  /**
   * Removes the variables local.
   */
  void removeVariablesLocal();
}