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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;


// TODO: Auto-generated Javadoc
/**
 * The Class ExecutionQueryImpl.
 *
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Daniel Meyer
 */
public class ExecutionQueryImpl extends ExecutionVariableQueryImpl<ExecutionQuery, Execution> 
  implements ExecutionQuery {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The process definition key. */
  protected String processDefinitionKey;
  
  /** The activity id. */
  protected String activityId;
  
  /** The execution id. */
  protected String executionId;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The event subscriptions. */
  protected List<EventSubscriptionQueryValue> eventSubscriptions;
  
  // Not used by end-users, but needed for dynamic ibatis query
  /** The super process instance id. */
  protected String superProcessInstanceId;
  
  /** The sub process instance id. */
  protected String subProcessInstanceId;
  
  /** The suspension state. */
  protected SuspensionState suspensionState;
  
  /** The business key. */
  private String businessKey;
  
  /**
   * Instantiates a new execution query impl.
   */
  public ExecutionQueryImpl() {
  }
  
  /**
   * Instantiates a new execution query impl.
   *
   * @param commandContext the command context
   */
  public ExecutionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  /**
   * Instantiates a new execution query impl.
   *
   * @param commandExecutor the command executor
   */
  public ExecutionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /**
   * Checks if is process instances only.
   *
   * @return true, if is process instances only
   */
  public boolean isProcessInstancesOnly() {
    return false; // see dynamic query
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#processDefinitionId(java.lang.String)
   */
  public ExecutionQueryImpl processDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiException("Process definition id is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#processDefinitionKey(java.lang.String)
   */
  public ExecutionQueryImpl processDefinitionKey(String processDefinitionKey) {
    if (processDefinitionKey == null) {
      throw new ActivitiException("Process definition key is null");
    }
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#processInstanceId(java.lang.String)
   */
  public ExecutionQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("Process instance id is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#processInstanceBusinessKey(java.lang.String)
   */
  public ExecutionQuery processInstanceBusinessKey(String businessKey) {
    if (businessKey == null) {
      throw new ActivitiException("Business key is null");
    }
    this.businessKey = businessKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#executionId(java.lang.String)
   */
  public ExecutionQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiException("Execution id is null");
    }
    this.executionId = executionId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#activityId(java.lang.String)
   */
  public ExecutionQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#signalEventSubscription(java.lang.String)
   */
  public ExecutionQuery signalEventSubscription(String signalName) {    
    return eventSubscription("signal", signalName);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#signalEventSubscriptionName(java.lang.String)
   */
  public ExecutionQuery signalEventSubscriptionName(String signalName) {    
    return eventSubscription("signal", signalName);
  }  
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#messageEventSubscriptionName(java.lang.String)
   */
  public ExecutionQuery messageEventSubscriptionName(String messageName) {    
    return eventSubscription("message", messageName);
  } 
  
  /**
   * Event subscription.
   *
   * @param eventType the event type
   * @param eventName the event name
   * @return the execution query
   */
  public ExecutionQuery eventSubscription(String eventType, String eventName) {
    if(eventName == null) {
      throw new ActivitiException("event name is null");
    }
    if(eventType == null) {
      throw new ActivitiException("event type is null");
    }
    if(eventSubscriptions == null) {
      eventSubscriptions = new ArrayList<EventSubscriptionQueryValue>();
    }
    eventSubscriptions.add(new EventSubscriptionQueryValue(eventName, eventType));
    return this;
  }

  //ordering ////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#orderByProcessInstanceId()
   */
  public ExecutionQueryImpl orderByProcessInstanceId() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_INSTANCE_ID;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#orderByProcessDefinitionId()
   */
  public ExecutionQueryImpl orderByProcessDefinitionId() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_DEFINITION_ID;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ExecutionQuery#orderByProcessDefinitionKey()
   */
  public ExecutionQueryImpl orderByProcessDefinitionKey() {
    this.orderProperty = ExecutionQueryProperty.PROCESS_DEFINITION_KEY;
    return this;
  }
  
  //results ////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.ExecutionVariableQueryImpl#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getExecutionManager()
      .findExecutionCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.ExecutionVariableQueryImpl#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  @SuppressWarnings("unchecked")
  public List<Execution> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return (List) commandContext
      .getExecutionManager()
      .findExecutionsByQueryCriteria(this, page);
  }
  
  //getters ////////////////////////////////////////////////////

  /**
   * Gets the only process instances.
   *
   * @return the only process instances
   */
  public boolean getOnlyProcessInstances() {
    return false;
  }
  
  /**
   * Gets the process definition key.
   *
   * @return the process definition key
   */
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  
  /**
   * Gets the process definition id.
   *
   * @return the process definition id
   */
  public String getProcessDefinitionId() {
    return processDefinitionId;
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
   * Gets the process instance id.
   *
   * @return the process instance id
   */
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  /**
   * Gets the process instance ids.
   *
   * @return the process instance ids
   */
  public String getProcessInstanceIds() {
    return null;
  }
  
  /**
   * Gets the business key.
   *
   * @return the business key
   */
  public String getBusinessKey() {
    return businessKey;
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
   * Gets the super process instance id.
   *
   * @return the super process instance id
   */
  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }
  
  /**
   * Gets the sub process instance id.
   *
   * @return the sub process instance id
   */
  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
  }  
  
  /**
   * Gets the suspension state.
   *
   * @return the suspension state
   */
  public SuspensionState getSuspensionState() {
    return suspensionState;
  }  
  
  /**
   * Sets the suspension state.
   *
   * @param suspensionState the new suspension state
   */
  public void setSuspensionState(SuspensionState suspensionState) {
    this.suspensionState = suspensionState;
  }  
  
  /**
   * Gets the event subscriptions.
   *
   * @return the event subscriptions
   */
  public List<EventSubscriptionQueryValue> getEventSubscriptions() {
    return eventSubscriptions;
  }
  
  /**
   * Sets the event subscriptions.
   *
   * @param eventSubscriptions the new event subscriptions
   */
  public void setEventSubscriptions(List<EventSubscriptionQueryValue> eventSubscriptions) {
    this.eventSubscriptions = eventSubscriptions;
  }
}
