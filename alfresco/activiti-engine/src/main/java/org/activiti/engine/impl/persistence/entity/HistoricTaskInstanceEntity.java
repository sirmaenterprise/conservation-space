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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.util.ClockUtil;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricTaskInstanceEntity.
 *
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceEntity extends HistoricScopeInstanceEntity implements HistoricTaskInstance, PersistentObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The execution id. */
  protected String executionId;
  
  /** The name. */
  protected String name;
  
  /** The parent task id. */
  protected String parentTaskId;
  
  /** The description. */
  protected String description;
  
  /** The owner. */
  protected String owner;
  
  /** The assignee. */
  protected String assignee;
  
  /** The task definition key. */
  protected String taskDefinitionKey;
  
  /** The priority. */
  protected int priority;
  
  /** The due date. */
  protected Date dueDate;

  /**
   * Instantiates a new historic task instance entity.
   */
  public HistoricTaskInstanceEntity() {
  }

  /**
   * Instantiates a new historic task instance entity.
   *
   * @param task the task
   * @param execution the execution
   */
  public HistoricTaskInstanceEntity(TaskEntity task, ExecutionEntity execution) {
    this.id = task.getId();
    if (execution!=null) {
      this.processDefinitionId = execution.getProcessDefinitionId();
      this.processInstanceId = execution.getProcessInstanceId();
      this.executionId = execution.getId();
    }
    this.name = task.getName();
    this.parentTaskId = task.getParentTaskId();
    this.description = task.getDescription();
    this.owner = task.getOwner();
    this.assignee = task.getAssignee();
    this.startTime = ClockUtil.getCurrentTime();
    this.taskDefinitionKey = task.getTaskDefinitionKey();
    this.setPriority(task.getPriority());
  }

  // persistence //////////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", name);
    persistentState.put("owner", owner);
    persistentState.put("assignee", assignee);
    persistentState.put("endTime", endTime);
    persistentState.put("durationInMillis", durationInMillis);
    persistentState.put("description", description);
    persistentState.put("deleteReason", deleteReason);
    persistentState.put("taskDefinitionKey", taskDefinitionKey);
    persistentState.put("priority", priority);
    if(parentTaskId != null) {
      persistentState.put("parentTaskId", parentTaskId);
    }
    if(dueDate != null) {
      persistentState.put("dueDate", dueDate);
    }
    return persistentState;
  }

  // getters and setters //////////////////////////////////////////////////////
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstance#getExecutionId()
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
   * @see org.activiti.engine.history.HistoricTaskInstance#getName()
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstance#getDescription()
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstance#getAssignee()
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
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstance#getTaskDefinitionKey()
   */
  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }
  
  /**
   * Sets the task definition key.
   *
   * @param taskDefinitionKey the new task definition key
   */
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstance#getPriority()
   */
  public int getPriority() {
    return priority;
  }
  
  /**
   * Sets the priority.
   *
   * @param priority the new priority
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstance#getDueDate()
   */
  public Date getDueDate() {
    return dueDate;
  }
  
  /**
   * Sets the due date.
   *
   * @param dueDate the new due date
   */
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstance#getOwner()
   */
  public String getOwner() {
    return owner;
  }
  
  /**
   * Sets the owner.
   *
   * @param owner the new owner
   */
  public void setOwner(String owner) {
    this.owner = owner;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.history.HistoricTaskInstance#getParentTaskId()
   */
  public String getParentTaskId() {
    return parentTaskId;
  }
  
  /**
   * Sets the parent task id.
   *
   * @param parentTaskId the new parent task id
   */
  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }
}
