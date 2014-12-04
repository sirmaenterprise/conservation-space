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
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;


// TODO: Auto-generated Javadoc
/**
 * The Class ProcessInstanceQueryImpl.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Daniel Meyer
 */
public class ProcessInstanceQueryImpl extends ExecutionVariableQueryImpl<ProcessInstanceQuery, ProcessInstance> implements ProcessInstanceQuery, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The execution id. */
  protected String executionId;
  
  /** The business key. */
  protected String businessKey;
  
  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The process instance ids. */
  protected Set<String> processInstanceIds; 
  
  /** The process definition key. */
  protected String processDefinitionKey;
  
  /** The super process instance id. */
  protected String superProcessInstanceId;
  
  /** The sub process instance id. */
  protected String subProcessInstanceId;
  
  /** The suspension state. */
  protected SuspensionState suspensionState;
  
  // Unused, see dynamic query
  /** The activity id. */
  protected String activityId;
  
  /** The event subscriptions. */
  protected List<EventSubscriptionQueryValue> eventSubscriptions;
  
  /**
   * Instantiates a new process instance query impl.
   */
  public ProcessInstanceQueryImpl() {
  }
  
  /**
   * Instantiates a new process instance query impl.
   *
   * @param commandContext the command context
   */
  public ProcessInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  /**
   * Instantiates a new process instance query impl.
   *
   * @param commandExecutor the command executor
   */
  public ProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#processInstanceId(java.lang.String)
   */
  public ProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("Process instance id is null");
    }
    this.executionId = processInstanceId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#processInstanceIds(java.util.Set)
   */
  public ProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds) {
    if (processInstanceIds == null) {
      throw new ActivitiException("Set of process instance ids is null");
    }
    if (processInstanceIds.isEmpty()) {
      throw new ActivitiException("Set of process instance ids is empty");
    }
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#processInstanceBusinessKey(java.lang.String)
   */
  public ProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    if (businessKey == null) {
      throw new ActivitiException("Business key is null");
    }
    this.businessKey = businessKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#processInstanceBusinessKey(java.lang.String, java.lang.String)
   */
  public ProcessInstanceQuery processInstanceBusinessKey(String businessKey, String processDefinitionKey) {
    if (businessKey == null) {
      throw new ActivitiException("Business key is null");
    }
    this.businessKey = businessKey;
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#processDefinitionId(java.lang.String)
   */
  public ProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiException("Process definition id is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#processDefinitionKey(java.lang.String)
   */
  public ProcessInstanceQueryImpl processDefinitionKey(String processDefinitionKey) {
    if (processDefinitionKey == null) {
      throw new ActivitiException("Process definition key is null");
    }
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#superProcessInstanceId(java.lang.String)
   */
  public ProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#subProcessInstanceId(java.lang.String)
   */
  public ProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
    return this;
  }
  

  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#orderByProcessInstanceId()
   */
  public ProcessInstanceQuery orderByProcessInstanceId() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#orderByProcessDefinitionId()
   */
  public ProcessInstanceQuery orderByProcessDefinitionId() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#orderByProcessDefinitionKey()
   */
  public ProcessInstanceQuery orderByProcessDefinitionKey() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#active()
   */
  public ProcessInstanceQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.runtime.ProcessInstanceQuery#suspended()
   */
  public ProcessInstanceQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }
  
  //results /////////////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.ExecutionVariableQueryImpl#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getExecutionManager()
      .findProcessInstanceCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.ExecutionVariableQueryImpl#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  public List<ProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getExecutionManager()
      .findProcessInstanceByQueryCriteria(this, page);
  }
  
  //getters /////////////////////////////////////////////////////////////////
  
  /**
   * Gets the only process instances.
   *
   * @return the only process instances
   */
  public boolean getOnlyProcessInstances() {
    return true; // See dynamic query in runtime.mapping.xml
  }
  
  /**
   * Gets the process instance id.
   *
   * @return the process instance id
   */
  public String getProcessInstanceId() {
    return executionId;
  }
  
  /**
   * Gets the process instance ids.
   *
   * @return the process instance ids
   */
  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
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
   * Gets the process definition id.
   *
   * @return the process definition id
   */
  public String getProcessDefinitionId() {
    return processDefinitionId;
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
   * Gets the activity id.
   *
   * @return the activity id
   */
  public String getActivityId() {
    return null; // Unused, see dynamic query
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
