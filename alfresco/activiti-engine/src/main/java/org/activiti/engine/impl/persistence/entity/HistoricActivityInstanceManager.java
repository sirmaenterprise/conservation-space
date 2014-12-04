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

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.AbstractHistoricManager;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricActivityInstanceManager.
 *
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceManager extends AbstractHistoricManager {

  /**
   * Delete historic activity instances by process instance id.
   *
   * @param historicProcessInstanceId the historic process instance id
   */
  public void deleteHistoricActivityInstancesByProcessInstanceId(String historicProcessInstanceId) {
    if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      getDbSqlSession().delete("deleteHistoricActivityInstancesByProcessInstanceId", historicProcessInstanceId);
    }
  }
  
  /**
   * Insert historic activity instance.
   *
   * @param historicActivityInstance the historic activity instance
   */
  public void insertHistoricActivityInstance(HistoricActivityInstanceEntity historicActivityInstance) {
    getDbSqlSession().insert(historicActivityInstance);
  }

  /**
   * Delete historic activity instance.
   *
   * @param historicActivityInstanceId the historic activity instance id
   */
  public void deleteHistoricActivityInstance(String historicActivityInstanceId) {
    getDbSqlSession().delete(HistoricActivityInstanceEntity.class, historicActivityInstanceId);
  }

  /**
   * Find historic activity instance.
   *
   * @param activityId the activity id
   * @param processInstanceId the process instance id
   * @return the historic activity instance entity
   */
  public HistoricActivityInstanceEntity findHistoricActivityInstance(String activityId, String processInstanceId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityId);
    parameters.put("processInstanceId", processInstanceId);
  
    return (HistoricActivityInstanceEntity) getDbSqlSession().selectOne("selectHistoricActivityInstance", parameters);
  }

  /**
   * Find historic activity instance count by query criteria.
   *
   * @param historicActivityInstanceQuery the historic activity instance query
   * @return the long
   */
  public long findHistoricActivityInstanceCountByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricActivityInstanceCountByQueryCriteria", historicActivityInstanceQuery);
  }

  /**
   * Find historic activity instances by query criteria.
   *
   * @param historicActivityInstanceQuery the historic activity instance query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<HistoricActivityInstance> findHistoricActivityInstancesByQueryCriteria(HistoricActivityInstanceQueryImpl historicActivityInstanceQuery, Page page) {
    return getDbSqlSession().selectList("selectHistoricActivityInstancesByQueryCriteria", historicActivityInstanceQuery, page);
  }
}
