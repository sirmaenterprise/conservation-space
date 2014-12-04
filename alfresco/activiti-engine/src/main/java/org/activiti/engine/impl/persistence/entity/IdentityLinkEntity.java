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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.task.IdentityLink;


// TODO: Auto-generated Javadoc
/**
 * The Class IdentityLinkEntity.
 *
 * @author Joram Barrez
 */
public class IdentityLinkEntity implements Serializable, IdentityLink, PersistentObject {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The type. */
  protected String type;
  
  /** The user id. */
  protected String userId;
  
  /** The group id. */
  protected String groupId;
  
  /** The task id. */
  protected String taskId;
  
  /** The process def id. */
  protected String processDefId;
  
  /** The task. */
  protected TaskEntity task;
  
  /** The process def. */
  protected ProcessDefinitionEntity processDef;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    return this.type;
  }
  
  /**
   * Creates the and insert.
   *
   * @return the identity link entity
   */
  public static IdentityLinkEntity createAndInsert() {
    IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
    Context
      .getCommandContext()
      .getDbSqlSession()
      .insert(identityLinkEntity);
    return identityLinkEntity;
  }
  
  /**
   * Checks if is user.
   *
   * @return true, if is user
   */
  public boolean isUser() {
    return userId != null;
  }
  
  /**
   * Checks if is group.
   *
   * @return true, if is group
   */
  public boolean isGroup() {
    return groupId != null;
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
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.IdentityLink#getType()
   */
  public String getType() {
    return type;
  }
  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.task.IdentityLink#getUserId()
   */
  public String getUserId() {
    return userId;
  }
  
  /**
   * Sets the user id.
   *
   * @param userId the new user id
   */
  public void setUserId(String userId) {
    if (this.groupId != null && userId != null) {
      throw new ActivitiException("Cannot assign a userId to a task assignment that already has a groupId");
    }
    this.userId = userId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.IdentityLink#getGroupId()
   */
  public String getGroupId() {
    return groupId;
  }
  
  /**
   * Sets the group id.
   *
   * @param groupId the new group id
   */
  public void setGroupId(String groupId) {
    if (this.userId != null && groupId != null) {
      throw new ActivitiException("Cannot assign a groupId to a task assignment that already has a userId");
    }
    this.groupId = groupId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.task.IdentityLink#getTaskId()
   */
  public String getTaskId() {
    return taskId;
  }

  /**
   * Sets the task id.
   *
   * @param taskId the new task id
   */
  void setTaskId(String taskId) {
    this.taskId = taskId;
  }
    
  /**
   * Gets the process def id.
   *
   * @return the process def id
   */
  public String getProcessDefId() {
    return processDefId;
  }
  
  /**
   * Sets the process def id.
   *
   * @param processDefId the new process def id
   */
  public void setProcessDefId(String processDefId) {
    this.processDefId = processDefId;
  }

  /**
   * Gets the task.
   *
   * @return the task
   */
  public TaskEntity getTask() {
    if ( (task==null) && (taskId!=null) ) {
      this.task = Context
        .getCommandContext()
        .getTaskManager()
        .findTaskById(taskId);
    }
    return task;
  }
  
  /**
   * Sets the task.
   *
   * @param task the new task
   */
  public void setTask(TaskEntity task) {
    this.task = task;
    this.taskId = task.getId();
  }

  /**
   * Gets the process def.
   *
   * @return the process def
   */
  public ProcessDefinitionEntity getProcessDef() {
    if ((processDef == null) && (processDefId != null)) {
      this.processDef = Context
              .getCommandContext()
              .getProcessDefinitionManager()
              .findLatestProcessDefinitionById(processDefId);
    }
    return processDef;
  }
  
  /**
   * Sets the process def.
   *
   * @param processDef the new process def
   */
  public void setProcessDef(ProcessDefinitionEntity processDef) {
    this.processDef = processDef;
    this.processDefId = processDef.getId();
  }

  
}
