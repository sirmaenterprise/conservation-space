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

package org.activiti.engine.impl;

import java.io.Serializable;


// TODO: Auto-generated Javadoc
/**
 * The Class EventSubscriptionQueryValue.
 *
 * @author Daniel Meyer
 */
public class EventSubscriptionQueryValue implements Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The event type. */
  protected String eventType;
  
  /** The event name. */
  protected String eventName;
  
  /**
   * Instantiates a new event subscription query value.
   *
   * @param eventName the event name
   * @param eventType the event type
   */
  public EventSubscriptionQueryValue(String eventName, String eventType) {
    this.eventName = eventName;
    this.eventType = eventType;
  }

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  public String getEventType() {
    return eventType;
  }
  
  /**
   * Sets the event type.
   *
   * @param eventType the new event type
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
  
  /**
   * Gets the event name.
   *
   * @return the event name
   */
  public String getEventName() {
    return eventName;
  }
  
  /**
   * Sets the event name.
   *
   * @param eventName the new event name
   */
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  
    

}
