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

package org.activiti.engine.impl.bpmn.parser;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class CompensateEventDefinition.
 *
 * @author Daniel Meyer
 */
public class CompensateEventDefinition implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The activity ref. */
  protected String activityRef;
  
  /** The wait for completion. */
  protected boolean waitForCompletion;

  /**
   * Gets the activity ref.
   *
   * @return the activity ref
   */
  public String getActivityRef() {
    return activityRef;
  }

  /**
   * Sets the activity ref.
   *
   * @param activityRef the new activity ref
   */
  public void setActivityRef(String activityRef) {
    this.activityRef = activityRef;
  }

  /**
   * Checks if is wait for completion.
   *
   * @return true, if is wait for completion
   */
  public boolean isWaitForCompletion() {
    return waitForCompletion;
  }

  /**
   * Sets the wait for completion.
   *
   * @param waitForCompletion the new wait for completion
   */
  public void setWaitForCompletion(boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
  }

}
