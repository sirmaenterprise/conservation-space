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

package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.event.EventHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.util.ClockUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class EventSubscriptionEntity.
 *
 * @author Daniel Meyer
 */
public abstract class EventSubscriptionEntity implements PersistentObject, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  // persistent state ///////////////////////////
  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision = 1;
  
  /** The event type. */
  protected String eventType;
  
  /** The event name. */
  protected String eventName;
  
  /** The execution id. */
  protected String executionId;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The activity id. */
  protected String activityId;
  
  /** The configuration. */
  protected String configuration;
  
  /** The created. */
  protected Date created;
  
  // runtime state /////////////////////////////
  /** The execution. */
  protected ExecutionEntity execution;
  
  /** The activity. */
  protected ActivityImpl activity;  
  
  /////////////////////////////////////////////
  
  /**
   * Instantiates a new event subscription entity.
   */
  public EventSubscriptionEntity() { 
    this.created = ClockUtil.getCurrentTime();
  }

  /**
   * Instantiates a new event subscription entity.
   *
   * @param executionEntity the execution entity
   */
  public EventSubscriptionEntity(ExecutionEntity executionEntity) {
    this();
    setExecution(executionEntity);
    setActivity(execution.getActivity());
    this.processInstanceId = executionEntity.getProcessInstanceId();
  }
  
  // processing /////////////////////////////
  
  /**
   * Event received.
   *
   * @param payload the payload
   * @param processASync the process a sync
   */
  public void eventReceived(Serializable payload, boolean processASync) {
    if(processASync) {
      scheduleEventAsync(payload);
    } else {
      processEventSync(payload);
    }
  }
  
  /**
   * Process event sync.
   *
   * @param payload the payload
   */
  protected void processEventSync(Object payload) {
    EventHandler eventHandler = Context.getProcessEngineConfiguration().getEventHandler(eventType);
    if (eventHandler == null) {
      throw new ActivitiException("Could not find eventhandler for event of type '" + eventType + "'.");
    }    
    eventHandler.handleEvent(this, payload, Context.getCommandContext());
  }
  
  /**
   * Schedule event async.
   *
   * @param payload the payload
   */
  protected void scheduleEventAsync(Serializable payload) {
    
    final CommandContext commandContext = Context.getCommandContext();

    MessageEntity message = new MessageEntity();
    message.setJobHandlerType(ProcessEventJobHandler.TYPE);
    message.setJobHandlerConfiguration(id);

    // TODO: support payload
//    if(payload != null) {
//      message.setEventPayload(payload);
//    }
    
    commandContext.getJobManager().send(message);
  }
  
  // persistence behavior /////////////////////

  /**
   * Delete.
   */
  public void delete() {
    Context.getCommandContext()
      .getEventSubscriptionManager()
      .deleteEventSubscription(this);
    removeFromExecution();
  }
  
  /**
   * Insert.
   */
  public void insert() {
    Context.getCommandContext()
      .getEventSubscriptionManager()
      .insert(this);
    addToExecution();   
  }
  
 // referential integrity -> ExecutionEntity ////////////////////////////////////
  
  /**
  * Adds the to execution.
  */
 protected void addToExecution() {
    // add reference in execution
    ExecutionEntity execution = getExecution();
    if(execution != null) {
      execution.addEventSubscription(this);
    }
  }
  
  /**
   * Removes the from execution.
   */
  protected void removeFromExecution() {
    // remove reference in execution
    ExecutionEntity execution = getExecution();
    if(execution != null) {
      execution.removeEventSubscription(this);
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    HashMap<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("executionId", executionId);
    persistentState.put("configuration", configuration);
    return persistentState;
  }
  
  // getters & setters ////////////////////////////
    
  /**
   * Gets the execution.
   *
   * @return the execution
   */
  public ExecutionEntity getExecution() {
    if(execution == null && executionId != null) {
      execution = Context.getCommandContext()
              .getExecutionManager()
              .findExecutionById(executionId);
    }
    return execution;
  }
    
  /**
   * Sets the execution.
   *
   * @param execution the new execution
   */
  public void setExecution(ExecutionEntity execution) {
    this.execution = execution;
    if(execution != null) {
      this.executionId = execution.getId();
    }
  }
    
  /**
   * Gets the activity.
   *
   * @return the activity
   */
  public ActivityImpl getActivity() {
    if(activity == null && activityId != null) {
      ExecutionEntity execution = getExecution();
      if(execution != null) {
        ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
        activity = processDefinition.findActivity(activityId);
      }
    }
    return activity;
  }
    
  /**
   * Sets the activity.
   *
   * @param activity the new activity
   */
  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
    if(activity != null) {
      this.activityId = activity.getId();
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getId()
   */
  public String getId() {
    return id;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the revision.
   *
   * @return the revision
   */
  public int getRevision() {
    return revision;
  }

  /**
   * Sets the revision.
   *
   * @param revision the new revision
   */
  public void setRevision(int revision) {
    this.revision = revision;
  }
  
  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision +1;
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

  /**
   * Gets the execution id.
   *
   * @return the execution id
   */
  public String getExecutionId() {
    return executionId;
  }

  /**
   * Sets the execution id.
   *
   * @param executionId the new execution id
   */
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
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
   * Sets the process instance id.
   *
   * @param processInstanceId the new process instance id
   */
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  /**
   * Gets the configuration.
   *
   * @return the configuration
   */
  public String getConfiguration() {
    return configuration;
  }

  /**
   * Sets the configuration.
   *
   * @param configuration the new configuration
   */
  public void setConfiguration(String configuration) {
    this.configuration = configuration;
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
   * Sets the activity id.
   *
   * @param activityId the new activity id
   */
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
  
  /**
   * Gets the created.
   *
   * @return the created
   */
  public Date getCreated() {
    return created;
  }
  
  /**
   * Sets the created.
   *
   * @param created the new created
   */
  public void setCreated(Date created) {
    this.created = created;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EventSubscriptionEntity other = (EventSubscriptionEntity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
  
}
