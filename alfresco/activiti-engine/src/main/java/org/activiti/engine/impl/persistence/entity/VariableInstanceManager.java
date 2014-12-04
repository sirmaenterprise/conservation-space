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
import java.util.Map;

import org.activiti.engine.impl.persistence.AbstractManager;


// TODO: Auto-generated Javadoc
/**
 * The Class VariableInstanceManager.
 *
 * @author Tom Baeyens
 */
public class VariableInstanceManager extends AbstractManager {

  /**
   * Delete variable instance.
   *
   * @param variableInstance the variable instance
   */
  public void deleteVariableInstance(VariableInstanceEntity variableInstance) {
    getDbSqlSession().delete(VariableInstanceEntity.class, variableInstance.getId());

    String byteArrayValueId = variableInstance.getByteArrayValueId();
    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      // @see also HistoricVariableUpdateEntity
      variableInstance.getByteArrayValue();
      getDbSqlSession().delete(ByteArrayEntity.class, byteArrayValueId);
    }
  }

  /**
   * Find variable instances by task id.
   *
   * @param taskId the task id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectVariablesByTaskId", taskId);
  }
  
  /**
   * Find variable instances by execution id.
   *
   * @param executionId the execution id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionId(String executionId) {
    return getDbSqlSession().selectList("selectVariablesByExecutionId", executionId);
  }

  /**
   * Delete variable instance by task.
   *
   * @param task the task
   */
  public void deleteVariableInstanceByTask(TaskEntity task) {
    Map<String, VariableInstanceEntity> variableInstances = task.getVariableInstances();
    if (variableInstances!=null) {
      for (VariableInstanceEntity variableInstance: variableInstances.values()) {
        deleteVariableInstance(variableInstance);
      }
    }
  }
}
