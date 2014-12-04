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

import java.util.List;


// TODO: Auto-generated Javadoc
/**
 * The Interface PvmProcessInstance.
 *
 * @author Tom Baeyens
 */
public interface PvmProcessInstance extends PvmExecution {

  /**
   * Start.
   */
  void start();

  /**
   * Find execution.
   *
   * @param activityId the activity id
   * @return the pvm execution
   */
  PvmExecution findExecution(String activityId);

  /**
   * Find active activity ids.
   *
   * @return the list
   */
  List<String> findActiveActivityIds();
  
  /**
   * Checks if is ended.
   *
   * @return true, if is ended
   */
  boolean isEnded();

  /**
   * Delete cascade.
   *
   * @param deleteReason the delete reason
   */
  void deleteCascade(String deleteReason);
}
