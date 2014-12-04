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

import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;


// TODO: Auto-generated Javadoc
/**
 * The Class DeploymentManager.
 *
 * @author Tom Baeyens
 */
public class DeploymentManager extends AbstractManager {
  
  /**
   * Insert deployment.
   *
   * @param deployment the deployment
   */
  public void insertDeployment(DeploymentEntity deployment) {
    getDbSqlSession().insert(deployment);
    
    for (ResourceEntity resource : deployment.getResources().values()) {
      resource.setDeploymentId(deployment.getId());
      getResourceManager().insertResource(resource);
    }
    
    Context
      .getProcessEngineConfiguration()
      .getDeploymentCache()
      .deploy(deployment);
  }
  
  /**
   * Delete deployment.
   *
   * @param deploymentId the deployment id
   * @param cascade the cascade
   */
  public void deleteDeployment(String deploymentId, boolean cascade) {
    List<ProcessDefinition> processDefinitions = getDbSqlSession()
            .createProcessDefinitionQuery()
            .deploymentId(deploymentId)
            .list();

    if (cascade) {

      // delete process instances
      for (ProcessDefinition processDefinition: processDefinitions) {
        String processDefinitionId = processDefinition.getId();
        
        getProcessInstanceManager()
          .deleteProcessInstancesByProcessDefinition(processDefinitionId, "deleted deployment", cascade);
    
      }
    }

    for (ProcessDefinition processDefinition : processDefinitions) {
      String processDefinitionId = processDefinition.getId();
      // remove related authorization parameters in IdentityLink table
      getIdentityLinkManager().deleteIdentityLinksByProcDef(processDefinitionId);
    }

    // delete process definitions from db
    getProcessDefinitionManager()
      .deleteProcessDefinitionsByDeploymentId(deploymentId);
    
    for (ProcessDefinition processDefinition : processDefinitions) {
      String processDefinitionId = processDefinition.getId();
      
      // remove process definitions from cache:
      Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .removeProcessDefinition(processDefinitionId);
      
      // remove timer start events:
      List<Job> timerStartJobs = Context.getCommandContext()
        .getJobManager()
        .findJobsByConfiguration(TimerStartEventJobHandler.TYPE, processDefinition.getKey());
      for (Job job : timerStartJobs) {
        ((JobEntity)job).delete();        
      }
      
      // remove message event subscriptions:
      List<EventSubscriptionEntity> findEventSubscriptionsByConfiguration = Context
        .getCommandContext()
        .getEventSubscriptionManager()
        .findEventSubscriptionsByConfiguration(MessageEventHandler.EVENT_HANDLER_TYPE, processDefinition.getId());
      for (EventSubscriptionEntity eventSubscriptionEntity : findEventSubscriptionsByConfiguration) {
        eventSubscriptionEntity.delete();        
      }
    }
    
    getResourceManager()
      .deleteResourcesByDeploymentId(deploymentId);
    
    getDbSqlSession().delete("deleteDeployment", deploymentId);
  }


  /**
   * Find latest deployment by name.
   *
   * @param deploymentName the deployment name
   * @return the deployment entity
   */
  public DeploymentEntity findLatestDeploymentByName(String deploymentName) {
    List<?> list = getDbSqlSession().selectList("selectDeploymentsByName", deploymentName, 0, 1);
    if (list!=null && !list.isEmpty()) {
      return (DeploymentEntity) list.get(0);
    }
    return null;
  }
  
  /**
   * Find deployment by id.
   *
   * @param deploymentId the deployment id
   * @return the deployment entity
   */
  public DeploymentEntity findDeploymentById(String deploymentId) {
    return (DeploymentEntity) getDbSqlSession().selectOne("selectDeploymentById", deploymentId);
  }
  
  /**
   * Find deployment count by query criteria.
   *
   * @param deploymentQuery the deployment query
   * @return the long
   */
  public long findDeploymentCountByQueryCriteria(DeploymentQueryImpl deploymentQuery) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByQueryCriteria", deploymentQuery);
  }

  /**
   * Find deployments by query criteria.
   *
   * @param deploymentQuery the deployment query
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<Deployment> findDeploymentsByQueryCriteria(DeploymentQueryImpl deploymentQuery, Page page) {
    final String query = "selectDeploymentsByQueryCriteria";
    return getDbSqlSession().selectList(query, deploymentQuery, page);
  }
  
  /**
   * Gets the deployment resource names.
   *
   * @param deploymentId the deployment id
   * @return the deployment resource names
   */
  @SuppressWarnings("unchecked")
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return getDbSqlSession().getSqlSession().selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.AbstractManager#close()
   */
  public void close() {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.persistence.AbstractManager#flush()
   */
  public void flush() {
  }
}
