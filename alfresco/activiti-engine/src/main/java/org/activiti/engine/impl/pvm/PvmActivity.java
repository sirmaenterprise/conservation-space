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
 * The Interface PvmActivity.
 *
 * @author Tom Baeyens
 */
public interface PvmActivity extends PvmScope {
  
  /**
   * Checks if is async.
   *
   * @return true, if is async
   */
  boolean isAsync();
  
  /**
   * Checks if is exclusive.
   *
   * @return true, if is exclusive
   */
  boolean isExclusive();

  /**
   * Gets the parent.
   *
   * @return the parent
   */
  PvmScope getParent();

  /**
   * Gets the incoming transitions.
   *
   * @return the incoming transitions
   */
  List<PvmTransition> getIncomingTransitions();

  /**
   * Gets the outgoing transitions.
   *
   * @return the outgoing transitions
   */
  List<PvmTransition> getOutgoingTransitions();
  
  /**
   * Find outgoing transition.
   *
   * @param transitionId the transition id
   * @return the pvm transition
   */
  PvmTransition findOutgoingTransition(String transitionId);
}
