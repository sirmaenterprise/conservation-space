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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.ExecutionVariableQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.runtime.ProcessInstance;


// TODO: Auto-generated Javadoc
/**
 * The Class ExecutionManager.
 *
 * @author Tom Baeyens
 */
public class ExecutionManager extends AbstractManager {
  
  /**
   * Delete process instances by process definition.
   *
   * @param processDefinitionId the process definition id
   * @param deleteReason the delete reason
   * @param cascade the cascade
   */
  @SuppressWarnings("unchecked")
  public void deleteProcessInstancesByProcessDefinition(String processDefinitionId, String deleteReason, boolean cascade) {
    List<String> processInstanceIds = getDbSqlSession()
      .selectList("selectProcessInstanceIdsByProcessDefinitionId", processDefinitionId);
  
    for (String processInstanceId: processInstanceIds) {
      deleteProcessInstance(processInstanceId, deleteReason, cascade);
    }
    
    if (cascade) {
      Context
        .getCommandContext()
        .getHistoricProcessInstanceManager()
        .deleteHistoricProcessInstanceByProcessDefinitionId(processDefinitionId);
    }
  }

  /**
   * Delete process instance.
   *
   * @param processInstanceId the process instance id
   * @param deleteReason the delete reason
   */
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    deleteProcessInstance(processInstanceId, deleteReason, false);
  }

  /**
   * Delete process instance.
   *
   * @param processInstanceId the process instance id
   * @param deleteReason the delete reason
   * @param cascade the cascade
   */
  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean cascade) {
    ExecutionEntity execution = findExecutionById(processInstanceId);
    
    if(execution == null) {
      throw new ActivitiException("No process instance found for id '" + processInstanceId + "'");
    }
    
    CommandContext commandContext = Context.getCommandContext();
    commandContext
      .getTaskManager()
      .deleteTasksByProcessInstanceId(processInstanceId, deleteReason, cascade);

    if (cascade) {
      commandContext
      .getHistoricProcessInstanceManager()
      .deleteHistoricProcessInstanceById(processInstanceId);
    }
    
    execution.deleteCascade(deleteReason);
  }

  /**
   * Find sub process instance by super execution id.
   *
   * @param superExecutionId the super execution id
   * @return the execution entity
   */
  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId) {
    return (ExecutionEntity) getDbSqlSession().selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
  }
  
  /**
   * Find child executions by parent execution id.
   *
   * @param parentExecutionId the parent execution id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findChildExecutionsByParentExecutionId(String parentExecutionId) {
    return getDbSqlSession().selectList("selectExecutionsByParentExecutionId", parentExecutionId);
  }

  /**
   * Find execution by id.
   *
   * @param executionId the execution id
   * @return the execution entity
   */
  public ExecutionEntity findExecutionById(String executionId) {
    return (ExecutionEntity) getDbSqlSession().selectById(ExecutionEntity.class, executionId);
  }
  
  /**
   * Find execution count by query criteria.
   *
   * @param executionQuery the execution query
   * @return the long
   */
  public long findExecutionCountByQueryCriteria(ExecutionVariableQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  /**
   * Find executions by query criteria.
   *
   * @param executionQuery the execution query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findExecutionsByQueryCriteria(ExecutionVariableQueryImpl executionQuery, Page page) {
    return getDbSqlSession().selectList("selectExecutionsByQueryCriteria", executionQuery, page);
  }

  /**
   * Find process instance count by query criteria.
   *
   * @param executionQuery the execution query
   * @return the long
   */
  public long findProcessInstanceCountByQueryCriteria(ExecutionVariableQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectProcessInstanceCountByQueryCriteria", executionQuery);
  }
  
  /**
   * Find process instance by query criteria.
   *
   * @param executionQuery the execution query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByQueryCriteria(ExecutionVariableQueryImpl executionQuery, Page page) {
    return getDbSqlSession().selectList("selectProcessInstanceByQueryCriteria", executionQuery, page);
  }

  /**
   * Find event scope executions by activity id.
   *
   * @param activityRef the activity ref
   * @param parentExecutionId the parent execution id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findEventScopeExecutionsByActivityId(String activityRef, String parentExecutionId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityRef);
    parameters.put("parentExecutionId", parentExecutionId);
    return getDbSqlSession().selectList("selectExecutionsByParentExecutionId", parameters);
  }

}
