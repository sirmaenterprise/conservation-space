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

import java.io.Serializable;
import java.util.Date;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.util.ClockUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class HistoricScopeInstanceEntity.
 *
 * @author Christian Stettler
 */
public abstract class HistoricScopeInstanceEntity implements PersistentObject, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The process definition id. */
  protected String processDefinitionId;
  
  /** The start time. */
  protected Date startTime;
  
  /** The end time. */
  protected Date endTime;
  
  /** The duration in millis. */
  protected Long durationInMillis;
  
  /** The delete reason. */
  protected String deleteReason;

  /**
   * Mark ended.
   *
   * @param deleteReason the delete reason
   */
  public void markEnded(String deleteReason) {
    this.deleteReason = deleteReason;
    this.endTime = ClockUtil.getCurrentTime();
    this.durationInMillis = endTime.getTime() - startTime.getTime();
  }
  
  // getters and setters //////////////////////////////////////////////////////

  /**
   * Gets the process instance id.
   *
   * @return the process instance id
   */
  public String getProcessInstanceId() {
    return processInstanceId;
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
   * Gets the start time.
   *
   * @return the start time
   */
  public Date getStartTime() {
    return startTime;
  }
  
  /**
   * Gets the end time.
   *
   * @return the end time
   */
  public Date getEndTime() {
    return endTime;
  }
  
  /**
   * Gets the duration in millis.
   *
   * @return the duration in millis
   */
  public Long getDurationInMillis() {
    return durationInMillis;
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
   * Sets the process instance id.
   *
   * @param processInstanceId the new process instance id
   */
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  /**
   * Sets the process definition id.
   *
   * @param processDefinitionId the new process definition id
   */
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  /**
   * Sets the start time.
   *
   * @param startTime the new start time
   */
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }
  
  /**
   * Sets the end time.
   *
   * @param endTime the new end time
   */
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }
  
  /**
   * Sets the duration in millis.
   *
   * @param durationInMillis the new duration in millis
   */
  public void setDurationInMillis(Long durationInMillis) {
    this.durationInMillis = durationInMillis;
  }
  
  /**
   * Gets the delete reason.
   *
   * @return the delete reason
   */
  public String getDeleteReason() {
    return deleteReason;
  }
  
  /**
   * Sets the delete reason.
   *
   * @param deleteReason the new delete reason
   */
  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }
}
