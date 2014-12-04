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
package org.activiti.engine.impl.util;

import java.util.Date;


// TODO: Auto-generated Javadoc
/**
 * The Class ClockUtil.
 *
 * @author Joram Barrez
 */
public class ClockUtil {
  
  /** The current time. */
  private volatile static Date CURRENT_TIME = null;
  
  /**
   * Sets the current time.
   *
   * @param currentTime the new current time
   */
  public static void setCurrentTime(Date currentTime) {
    ClockUtil.CURRENT_TIME = currentTime;
  }
  
  /**
   * Reset.
   */
  public static void reset() {
    ClockUtil.CURRENT_TIME = null;
  } 
  
  /**
   * Gets the current time.
   *
   * @return the current time
   */
  public static Date getCurrentTime() {
    if (CURRENT_TIME != null) {
      return CURRENT_TIME;
    }
    return new Date();
  }

}
