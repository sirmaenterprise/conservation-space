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
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class EventSubscriptionQueryImpl.
 *
 * @author Daniel Meyer
 */
public class EventSubscriptionQueryImpl 
                extends AbstractQuery<EventSubscriptionQueryImpl, EventSubscriptionEntity> 
                implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The event subscription id. */
  protected String eventSubscriptionId;
  
  /** The event name. */
  protected String eventName;
  
  /** The event type. */
  protected String eventType;
  
  /** The execution id. */
  protected String executionId;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The activity id. */
  protected String activityId;

  /**
   * Instantiates a new event subscription query impl.
   *
   * @param commandContext the command context
   */
  public EventSubscriptionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  /**
   * Instantiates a new event subscription query impl.
   *
   * @param commandExecutor the command executor
   */
  public EventSubscriptionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /**
   * Event subscription id.
   *
   * @param id the id
   * @return the event subscription query impl
   */
  public EventSubscriptionQueryImpl eventSubscriptionId(String id) {
    if (eventSubscriptionId == null) {
      throw new ActivitiException("Provided svent subscription id is null");
    }
    this.eventSubscriptionId = id;
    return this;
  }

  /**
   * Event name.
   *
   * @param eventName the event name
   * @return the event subscription query impl
   */
  public EventSubscriptionQueryImpl eventName(String eventName) {
    if (eventName == null) {
      throw new ActivitiException("Provided event name is null");
    }
    this.eventName = eventName;
    return this;
  }

  /**
   * Execution id.
   *
   * @param executionId the execution id
   * @return the event subscription query impl
   */
  public EventSubscriptionQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiException("Provided execution id is null");
    }
    this.executionId = executionId;
    return this;
  }

  /**
   * Process instance id.
   *
   * @param processInstanceId the process instance id
   * @return the event subscription query impl
   */
  public EventSubscriptionQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("Provided process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }

  /**
   * Activity id.
   *
   * @param activityId the activity id
   * @return the event subscription query impl
   */
  public EventSubscriptionQueryImpl activityId(String activityId) {
    if (activityId == null) {
      throw new ActivitiException("Provided activity id is null");
    }
    this.activityId = activityId;
    return this;
  }
  
  /**
   * Event type.
   *
   * @param eventType the event type
   * @return the event subscription query impl
   */
  public EventSubscriptionQueryImpl eventType(String eventType) {
    if (eventType == null) {
      throw new ActivitiException("Provided event type is null");
    }
    this.eventType = eventType;
    return this;
  }
  
  /**
   * Order by created.
   *
   * @return the event subscription query impl
   */
  public EventSubscriptionQueryImpl orderByCreated() {
    return orderBy(EventSubscriptionQueryProperty.CREATED);
  }
  
  //results //////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getEventSubscriptionManager()
      .findEventSubscriptionCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  @Override
  public List<EventSubscriptionEntity> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getEventSubscriptionManager()
      .findEventSubscriptionsByQueryCriteria(this,page);
  }
  
  //getters //////////////////////////////////////////
  
   
  /**
   * Gets the event subscription id.
   *
   * @return the event subscription id
   */
  public String getEventSubscriptionId() {
    return eventSubscriptionId;
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
   * Gets the event type.
   *
   * @return the event type
   */
  public String getEventType() {
    return eventType;
  }
  
  /**
   * Gets the execution id.
   *
   * @return the execution id
   */
  public String getExecutionId() {
    return executionId;
  }
  
  /**
   * Gets the process instance id.
   *
   * @return the process instance id
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  /**
   * Gets the activity id.
   *
   * @return the activity id
   */
  public String getActivityId() {
    return activityId;
  }

}
