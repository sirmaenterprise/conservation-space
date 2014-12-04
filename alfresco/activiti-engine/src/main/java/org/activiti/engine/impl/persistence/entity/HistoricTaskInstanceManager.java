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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricTaskInstanceManager.
 *
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceManager extends AbstractHistoricManager {

  /**
   * Delete historic task instances by process instance id.
   *
   * @param processInstanceId the process instance id
   */
  @SuppressWarnings("unchecked")
  public void deleteHistoricTaskInstancesByProcessInstanceId(String processInstanceId) {
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      List<String> taskInstanceIds = (List<String>) getDbSqlSession().selectList("selectHistoricTaskInstanceIdsByProcessInstanceId", processInstanceId);
      for (String taskInstanceId: taskInstanceIds) {
        deleteHistoricTaskInstanceById(taskInstanceId);
      }
    }
  }

  /**
   * Find historic task instance count by query criteria.
   *
   * @param historicTaskInstanceQuery the historic task instance query
   * @return the long
   */
  public long findHistoricTaskInstanceCountByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      return (Long) getDbSqlSession().selectOne("selectHistoricTaskInstanceCountByQueryCriteria", historicTaskInstanceQuery);
    }
    return 0;
  }

  /**
   * Find historic task instances by query criteria.
   *
   * @param historicTaskInstanceQuery the historic task instance query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<HistoricTaskInstance> findHistoricTaskInstancesByQueryCriteria(HistoricTaskInstanceQueryImpl historicTaskInstanceQuery, Page page) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      return getDbSqlSession().selectList("selectHistoricTaskInstancesByQueryCriteria", historicTaskInstanceQuery, page);
    }
    return Collections.EMPTY_LIST;
  }
  
  /**
   * Find historic task instance by id.
   *
   * @param taskId the task id
   * @return the historic task instance entity
   */
  public HistoricTaskInstanceEntity findHistoricTaskInstanceById(String taskId) {
    if (taskId == null) {
      throw new ActivitiException("Invalid historic task id : null");
    }
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      return (HistoricTaskInstanceEntity) getDbSqlSession().selectOne("selectHistoricTaskInstance", taskId);
    }
    return null;
  }
  
  /**
   * Delete historic task instance by id.
   *
   * @param taskId the task id
   */
  public void deleteHistoricTaskInstanceById(String taskId) {
    if (historyLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE) {
      HistoricTaskInstanceEntity historicTaskInstance = findHistoricTaskInstanceById(taskId);
      if(historicTaskInstance!=null) {
        CommandContext commandContext = Context.getCommandContext();
        
        commandContext
          .getHistoricDetailManager()
          .deleteHistoricDetailsByTaskId(taskId);
          
        commandContext
          .getCommentManager()
          .deleteCommentsByTaskId(taskId);
        
        commandContext
          .getAttachmentManager()
          .deleteAttachmentsByTaskId(taskId);
      
        getDbSqlSession().delete(HistoricTaskInstanceEntity.class, taskId);
      }
    }
  }

  /**
   * Mark task instance ended.
   *
   * @param taskId the task id
   * @param deleteReason the delete reason
   */
  public void markTaskInstanceEnded(String taskId, String deleteReason) {
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.markEnded(deleteReason);
      }
    }
  }

  /**
   * Sets the task assignee.
   *
   * @param taskId the task id
   * @param assignee the assignee
   */
  public void setTaskAssignee(String taskId, String assignee) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setAssignee(assignee);
      }
    }
  }

  /**
   * Sets the task owner.
   *
   * @param taskId the task id
   * @param owner the owner
   */
  public void setTaskOwner(String taskId, String owner) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setOwner(owner);
      }
    }
  }

  /**
   * Sets the task name.
   *
   * @param id the id
   * @param taskName the task name
   */
  public void setTaskName(String id, String taskName) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setName(taskName);
      }
    }
  }

  /**
   * Sets the task description.
   *
   * @param id the id
   * @param description the description
   */
  public void setTaskDescription(String id, String description) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setDescription(description);
      }
    }
  }

  /**
   * Sets the task due date.
   *
   * @param id the id
   * @param dueDate the due date
   */
  public void setTaskDueDate(String id, Date dueDate) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setDueDate(dueDate);
      }
    }
  }

  /**
   * Sets the task priority.
   *
   * @param id the id
   * @param priority the priority
   */
  public void setTaskPriority(String id, int priority) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setPriority(priority);
      }
    }
  }

  /**
   * Sets the task parent task id.
   *
   * @param id the id
   * @param parentTaskId the parent task id
   */
  public void setTaskParentTaskId(String id, String parentTaskId) {
    if (historyLevel >= ProcessEngineConfigurationImpl.HISTORYLEVEL_AUDIT) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, id);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setParentTaskId(parentTaskId);
      }
    }
  }
}
