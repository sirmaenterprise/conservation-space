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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.task.Task;


// TODO: Auto-generated Javadoc
/**
 * The Class TaskManager.
 *
 * @author Tom Baeyens
 */
public class TaskManager extends AbstractManager {

  /**
   * Delete tasks by process instance id.
   *
   * @param processInstanceId the process instance id
   * @param deleteReason the delete reason
   * @param cascade the cascade
   */
  @SuppressWarnings("unchecked")
  public void deleteTasksByProcessInstanceId(String processInstanceId, String deleteReason, boolean cascade) {
    List<TaskEntity> tasks = (List) getDbSqlSession()
      .createTaskQuery()
      .processInstanceId(processInstanceId)
      .list();
  
    String reason = (deleteReason == null || deleteReason.length() == 0) ? TaskEntity.DELETE_REASON_DELETED : deleteReason;
    
    for (TaskEntity task: tasks) {
      deleteTask(task, reason, cascade);
    }
  }

  /**
   * Delete task.
   *
   * @param task the task
   * @param deleteReason the delete reason
   * @param cascade the cascade
   */
  public void deleteTask(TaskEntity task, String deleteReason, boolean cascade) {
    if (!task.isDeleted()) {
      task.setDeleted(true);
      
      CommandContext commandContext = Context.getCommandContext();
      String taskId = task.getId();
      
      List<Task> subTasks = findTasksByParentTaskId(taskId);
      for (Task subTask: subTasks) {
        deleteTask((TaskEntity) subTask, deleteReason, cascade);
      }
      
      commandContext
        .getIdentityLinkManager()
        .deleteIdentityLinksByTaskId(taskId);

      commandContext
        .getVariableInstanceManager()
        .deleteVariableInstanceByTask(task);

      if (cascade) {
        commandContext
          .getHistoricTaskInstanceManager()
          .deleteHistoricTaskInstanceById(taskId);
      } else {
        commandContext
          .getHistoricTaskInstanceManager()
          .markTaskInstanceEnded(taskId, deleteReason);
      }
        
      getDbSqlSession().delete(TaskEntity.class, task.getId());
    }
  }


  /**
   * Find task by id.
   *
   * @param id the id
   * @return the task entity
   */
  public TaskEntity findTaskById(String id) {
    if (id == null) {
      throw new ActivitiException("Invalid task id : null");
    }
    return (TaskEntity) getDbSqlSession().selectOne("selectTask", id);
  }

  /**
   * Find tasks by execution id.
   *
   * @param executionId the execution id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<TaskEntity> findTasksByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectTasksByExecutionId", executionId);
  }
  
  /**
   * Find tasks by query criteria.
   *
   * @param taskQuery the task query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery, Page page) {
    taskQuery.setFirstResult(page.getFirstResult());
    taskQuery.setMaxResults(page.getMaxResults());
    return findTasksByQueryCriteria(taskQuery);
  }
  
  /**
   * Find tasks by query criteria.
   *
   * @param taskQuery the task query
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByQueryCriteria(TaskQueryImpl taskQuery) {
    final String query = "selectTaskByQueryCriteria";
    return getDbSqlSession().selectList(query, taskQuery);
  }

  /**
   * Find task count by query criteria.
   *
   * @param taskQuery the task query
   * @return the long
   */
  public long findTaskCountByQueryCriteria(TaskQueryImpl taskQuery) {
    return (Long) getDbSqlSession().selectOne("selectTaskCountByQueryCriteria", taskQuery);
  }

  /**
   * Find tasks by parent task id.
   *
   * @param parentTaskId the parent task id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Task> findTasksByParentTaskId(String parentTaskId) {
    return getDbSqlSession().selectList("selectTasksByParentTaskId", parentTaskId);
  }

  /**
   * Delete task.
   *
   * @param taskId the task id
   * @param cascade the cascade
   */
  public void deleteTask(String taskId, boolean cascade) {
    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);
    
    if (task!=null) {
      deleteTask(task, TaskEntity.DELETE_REASON_DELETED, cascade);

    } else if (cascade) {
      Context
        .getCommandContext()
        .getHistoricTaskInstanceManager()
        .deleteHistoricTaskInstanceById(taskId);
    }
  }
}
