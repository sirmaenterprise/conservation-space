/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.ClockUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class HistoricProcessInstanceEntity.
 *
 * @author Tom Baeyens
 * @author Christian Stettler
 */
public class HistoricProcessInstanceEntity extends HistoricScopeInstanceEntity implements HistoricProcessInstance {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The end activity id. */
  protected String endActivityId;
  
  /** The business key. */
  protected String businessKey;
  
  /** The start user id. */
  protected String startUserId;
  
  /** The start activity id. */
  protected String startActivityId;
  
  /** The super process instance id. */
  protected String superProcessInstanceId;

  /**
   * Instantiates a new historic process instance entity.
   */
  public HistoricProcessInstanceEntity() {
  }

  /**
   * Instantiates a new historic process instance entity.
   *
   * @param processInstance the process instance
   */
  public HistoricProcessInstanceEntity(ExecutionEntity processInstance) {
    id = processInstance.getId();
    processInstanceId = processInstance.getId();
    businessKey = processInstance.getBusinessKey();
    processDefinitionId = processInstance.getProcessDefinitionId();
    startTime = ClockUtil.getCurrentTime();
    startUserId = Authentication.getAuthenticatedUserId();
    startActivityId = processInstance.getActivityId();
    superProcessInstanceId = processInstance.getSuperExecution() != null ? processInstance.getSuperExecution().getProcessInstanceId() : null;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = (Map<String, Object>) new HashMap<String, Object>();
    persistentState.put("endTime", endTime);
    persistentState.put("durationInMillis", durationInMillis);
    persistentState.put("deleteReason", deleteReason);
    persistentState.put("endStateName", endActivityId);
    persistentState.put("superProcessInstanceId", superProcessInstanceId);
    persistentState.put("processDefinitionId", processDefinitionId);
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstance#getEndActivityId()
   */
  public String getEndActivityId() {
    return endActivityId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstance#getBusinessKey()
   */
  public String getBusinessKey() {
    return businessKey;
  }
  
  /**
   * Sets the business key.
   *
   * @param businessKey the new business key
   */
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
  
  /**
   * Sets the end activity id.
   *
   * @param endActivityId the new end activity id
   */
  public void setEndActivityId(String endActivityId) {
    this.endActivityId = endActivityId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstance#getStartUserId()
   */
  public String getStartUserId() {
    return startUserId;
  }
  
  /**
   * Sets the start user id.
   *
   * @param startUserId the new start user id
   */
  public void setStartUserId(String startUserId) {
    this.startUserId = startUserId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstance#getStartActivityId()
   */
  public String getStartActivityId() {
    return startActivityId;
  }
  
  /**
   * Sets the start activity id.
   *
   * @param startUserId the new start activity id
   */
  public void setStartActivityId(String startUserId) {
    this.startActivityId = startUserId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricProcessInstance#getSuperProcessInstanceId()
   */
  public String getSuperProcessInstanceId() {
	return superProcessInstanceId;
  }
  
  /**
   * Sets the super process instance id.
   *
   * @param superProcessInstanceId the new super process instance id
   */
  public void setSuperProcessInstanceId(String superProcessInstanceId) {
	this.superProcessInstanceId = superProcessInstanceId;
  }
  
}
