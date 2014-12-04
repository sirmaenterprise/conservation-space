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

package org.activiti.engine.impl.pvm;

import java.util.Map;



// TODO: Auto-generated Javadoc
/**
 * The Interface PvmExecution.
 *
 * @author Tom Baeyens
 */
public interface PvmExecution {

  /**
   * Signal.
   *
   * @param signalName the signal name
   * @param signalData the signal data
   */
  void signal(String signalName, Object signalData);

  /**
   * Gets the activity.
   *
   * @return the activity
   */
  PvmActivity getActivity();
  
  /**
   * Checks for variable.
   *
   * @param variableName the variable name
   * @return true, if successful
   */
  boolean hasVariable(String variableName);
  
  /**
   * Sets the variable.
   *
   * @param variableName the variable name
   * @param value the value
   */
  void setVariable(String variableName, Object value);
  
  /**
   * Gets the variable.
   *
   * @param variableName the variable name
   * @return the variable
   */
  Object getVariable(String variableName);
  
  /**
   * Gets the variables.
   *
   * @return the variables
   */
  Map<String, Object> getVariables();
}
