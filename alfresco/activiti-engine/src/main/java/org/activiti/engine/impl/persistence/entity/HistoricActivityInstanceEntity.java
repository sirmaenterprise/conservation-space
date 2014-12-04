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

import org.activiti.engine.history.HistoricActivityInstance;

// TODO: Auto-generated Javadoc
/**
 * The Class HistoricActivityInstanceEntity.
 *
 * @author Christian Stettler
 */
public class HistoricActivityInstanceEntity extends HistoricScopeInstanceEntity implements HistoricActivityInstance {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The activity id. */
  protected String activityId;
  
  /** The activity name. */
  protected String activityName;
  
  /** The activity type. */
  protected String activityType;
  
  /** The execution id. */
  protected String executionId;
  
  /** The assignee. */
  protected String assignee;
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = (Map<String, Object>) new HashMap<String, Object>();
    persistentState.put("endTime", endTime);
    persistentState.put("durationInMillis", durationInMillis);
    persistentState.put("deleteReason", deleteReason);
    persistentState.put("executionId", executionId);
    persistentState.put("assignee", assignee);
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstance#getActivityId()
   */
  public String getActivityId() {
    return activityId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstance#getActivityName()
   */
  public String getActivityName() {
    return activityName;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstance#getActivityType()
   */
  public String getActivityType() {
    return activityType;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstance#getExecutionId()
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
   * Sets the activity id.
   *
   * @param activityId the new activity id
   */
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
  
  /**
   * Sets the activity name.
   *
   * @param activityName the new activity name
   */
  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }
  
  /**
   * Sets the activity type.
   *
   * @param activityType the new activity type
   */
  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricActivityInstance#getAssignee()
   */
  public String getAssignee() {
    return assignee;
  }

  /**
   * Sets the assignee.
   *
   * @param assignee the new assignee
   */
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }
}
