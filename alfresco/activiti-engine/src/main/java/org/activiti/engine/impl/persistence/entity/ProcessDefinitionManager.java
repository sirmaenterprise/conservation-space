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
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.repository.ProcessDefinition;


// TODO: Auto-generated Javadoc
/**
 * The Class ProcessDefinitionManager.
 *
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Saeid Mirzaei
 */
public class ProcessDefinitionManager extends AbstractManager {

  /**
   * Find latest process definition by key.
   *
   * @param processDefinitionKey the process definition key
   * @return the process definition entity
   */
  public ProcessDefinitionEntity findLatestProcessDefinitionByKey(String processDefinitionKey) {
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectLatestProcessDefinitionByKey", processDefinitionKey);
  }

  /**
   * Delete process definitions by deployment id.
   *
   * @param deploymentId the deployment id
   */
  public void deleteProcessDefinitionsByDeploymentId(String deploymentId) {
    getDbSqlSession().delete("deleteProcessDefinitionsByDeploymentId", deploymentId);
  }

  /**
   * Find latest process definition by id.
   *
   * @param processDefinitionId the process definition id
   * @return the process definition entity
   */
  public ProcessDefinitionEntity findLatestProcessDefinitionById(String processDefinitionId) {
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectProcessDefinitionById", processDefinitionId);
  }
  
  /**
   * Find process definitions by query criteria.
   *
   * @param processDefinitionQuery the process definition query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<ProcessDefinition> findProcessDefinitionsByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery, Page page) {
    final String query = "selectProcessDefinitionsByQueryCriteria";
    return getDbSqlSession().selectList(query, processDefinitionQuery, page);
  }

  /**
   * Find process definition count by query criteria.
   *
   * @param processDefinitionQuery the process definition query
   * @return the long
   */
  public long findProcessDefinitionCountByQueryCriteria(ProcessDefinitionQueryImpl processDefinitionQuery) {
    return (Long) getDbSqlSession().selectOne("selectProcessDefinitionCountByQueryCriteria", processDefinitionQuery);
  }
  
  /**
   * Find process definition by deployment and key.
   *
   * @param deploymentId the deployment id
   * @param processDefinitionKey the process definition key
   * @return the process definition entity
   */
  public ProcessDefinitionEntity findProcessDefinitionByDeploymentAndKey(String deploymentId, String processDefinitionKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("processDefinitionKey", processDefinitionKey);
    return (ProcessDefinitionEntity) getDbSqlSession().selectOne("selectProcessDefinitionByDeploymentAndKey", parameters);
  }

  /**
   * Find process definition by key and version.
   *
   * @param processDefinitionKey the process definition key
   * @param processDefinitionVersion the process definition version
   * @return the process definition
   */
  public ProcessDefinition findProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionQueryImpl processDefinitionQuery = new ProcessDefinitionQueryImpl()
      .processDefinitionKey(processDefinitionKey)
      .processDefinitionVersion(processDefinitionVersion);
    List<ProcessDefinition> results = findProcessDefinitionsByQueryCriteria(processDefinitionQuery, null);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ActivitiException("There are " + results.size() + " process definitions with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'.");
    }
    return null; 
  }
  
  /**
   * Find process definitions startable by user.
   *
   * @param user the user
   * @return the list
   */
  public List<ProcessDefinition> findProcessDefinitionsStartableByUser(String user) {
    return   new ProcessDefinitionQueryImpl().startableByUser(user).list();
  }
  
  /**
   * Find process definition potential starter users.
   *
   * @return the list
   */
  public List<User> findProcessDefinitionPotentialStarterUsers() {
    return null;
  }
  
  /**
   * Find process definition potential starter groups.
   *
   * @return the list
   */
  public List<Group> findProcessDefinitionPotentialStarterGroups() {
    return null;
  }

 
}
