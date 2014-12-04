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

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricDetailEntity.
 *
 * @author Tom Baeyens
 */
public class HistoricDetailEntity implements HistoricDetail, PersistentObject, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The process instance id. */
  protected String processInstanceId;
  
  /** The activity instance id. */
  protected String activityInstanceId;
  
  /** The task id. */
  protected String taskId;
  
  /** The execution id. */
  protected String executionId;
  
  /** The time. */
  protected Date time;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    // details are not updatable so we always provide the same object as the state
    return HistoricDetailEntity.class;
  }
  
  /**
   * Delete.
   */
  public void delete() {
    DbSqlSession dbSqlSession = Context
      .getCommandContext()
      .getDbSqlSession();

    dbSqlSession.delete(HistoricDetailEntity.class, id);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetail#getId()
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

  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetail#getProcessInstanceId()
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

  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetail#getExecutionId()
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

  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetail#getActivityInstanceId()
   */
  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  
  /**
   * Sets the activity instance id.
   *
   * @param activityInstanceId the new activity instance id
   */
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetail#getTime()
   */
  public Date getTime() {
    return time;
  }
  
  /**
   * Sets the time.
   *
   * @param time the new time
   */
  public void setTime(Date time) {
    this.time = time;
  }

  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricDetail#getTaskId()
   */
  public String getTaskId() {
    return taskId;
  }

  /**
   * Sets the task id.
   *
   * @param taskId the new task id
   */
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
}
