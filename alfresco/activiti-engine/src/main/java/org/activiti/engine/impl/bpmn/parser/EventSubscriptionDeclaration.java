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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


// TODO: Auto-generated Javadoc
/**
 * The Class EventSubscriptionDeclaration.
 *
 * @author Daniel Meyer
 */
public class EventSubscriptionDeclaration implements Serializable {

  /** The event name. */
  protected final String eventName;
  
  /** The event type. */
  protected final String eventType;
  
  /** The async. */
  protected boolean async;
  
  /** The activity id. */
  protected String activityId;
  
  /** The is start event. */
  protected boolean isStartEvent;

  /**
   * Instantiates a new event subscription declaration.
   *
   * @param eventName the event name
   * @param eventType the event type
   */
  public EventSubscriptionDeclaration(String eventName, String eventType) {
    this.eventName = eventName;
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
   * Checks if is async.
   *
   * @return true, if is async
   */
  public boolean isAsync() {
    return async;
  }
  
  /**
   * Sets the async.
   *
   * @param async the new async
   */
  public void setAsync(boolean async) {
    this.async = async;
  }
  
  /**
   * Sets the activity id.
   *
   * @param activityId the new activity id
   */
  public void setActivityId(String activityId) {
    this.activityId = activityId;   
  }
    
  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public String getActivityId() {
    return activityId;
  }
  
  /**
   * Checks if is start event.
   *
   * @return true, if is start event
   */
  public boolean isStartEvent() {
    return isStartEvent;
  }
  
  /**
   * Sets the start event.
   *
   * @param isStartEvent the new start event
   */
  public void setStartEvent(boolean isStartEvent) {
    this.isStartEvent = isStartEvent;
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
   * Prepare event subscription entity.
   *
   * @param execution the execution
   * @return the event subscription entity
   */
  public EventSubscriptionEntity prepareEventSubscriptionEntity(ExecutionEntity execution) {
    EventSubscriptionEntity eventSubscriptionEntity = null;
    if(eventType.equals("message")) {
      eventSubscriptionEntity = new MessageEventSubscriptionEntity(execution);
    }else  if(eventType.equals("signal")) {
      eventSubscriptionEntity = new SignalEventSubscriptionEntity(execution);
    }else {
      throw new ActivitiException("Found event definition of unknown type: "+eventType);
    }
    
    eventSubscriptionEntity.setEventName(eventName);
    if(activityId != null) {
      ActivityImpl activity = execution.getActivity().findActivity(activityId);
      eventSubscriptionEntity.setActivity(activity);
    }
    return eventSubscriptionEntity;
  }

}
