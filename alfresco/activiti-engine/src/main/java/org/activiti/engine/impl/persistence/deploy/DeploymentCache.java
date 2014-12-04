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

package org.activiti.engine.impl.persistence.deploy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class DeploymentCache.
 *
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class DeploymentCache {

  /** The process definition cache. */
  protected Map<String, ProcessDefinitionEntity> processDefinitionCache = new HashMap<String, ProcessDefinitionEntity>(); 
  
  /** The knowledge base cache. */
  protected Map<String, Object> knowledgeBaseCache = new HashMap<String, Object>(); 
  
  /** The deployers. */
  protected List<Deployer> deployers;
  
  /**
   * Deploy.
   *
   * @param deployment the deployment
   */
  public void deploy(DeploymentEntity deployment) {
    for (Deployer deployer: deployers) {
      deployer.deploy(deployment);
    }
  }

  /**
   * Find deployed process definition by id.
   *
   * @param processDefinitionId the process definition id
   * @return the process definition entity
   */
  public ProcessDefinitionEntity findDeployedProcessDefinitionById(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiException("Invalid process definition id : null");
    }
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findLatestProcessDefinitionById(processDefinitionId);
    if(processDefinition == null) {
      throw new ActivitiException("no deployed process definition found with id '" + processDefinitionId + "'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  /**
   * Find deployed latest process definition by key.
   *
   * @param processDefinitionKey the process definition key
   * @return the process definition entity
   */
  public ProcessDefinitionEntity findDeployedLatestProcessDefinitionByKey(String processDefinitionKey) {
    ProcessDefinitionEntity processDefinition = Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findLatestProcessDefinitionByKey(processDefinitionKey);
    if (processDefinition==null) {
      throw new ActivitiException("no processes deployed with key '"+processDefinitionKey+"'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  /**
   * Find deployed process definition by key and version.
   *
   * @param processDefinitionKey the process definition key
   * @param processDefinitionVersion the process definition version
   * @return the process definition entity
   */
  public ProcessDefinitionEntity findDeployedProcessDefinitionByKeyAndVersion(String processDefinitionKey, Integer processDefinitionVersion) {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) Context
      .getCommandContext()
      .getProcessDefinitionManager()
      .findProcessDefinitionByKeyAndVersion(processDefinitionKey, processDefinitionVersion);
    if (processDefinition==null) {
      throw new ActivitiException("no processes deployed with key = '" + processDefinitionKey + "' and version = '" + processDefinitionVersion + "'");
    }
    processDefinition = resolveProcessDefinition(processDefinition);
    return processDefinition;
  }

  /**
   * Resolve process definition.
   *
   * @param processDefinition the process definition
   * @return the process definition entity
   */
  protected ProcessDefinitionEntity resolveProcessDefinition(ProcessDefinitionEntity processDefinition) {
    String processDefinitionId = processDefinition.getId();
    String deploymentId = processDefinition.getDeploymentId();
    processDefinition = processDefinitionCache.get(processDefinitionId);
    if (processDefinition==null) {
      DeploymentEntity deployment = Context
        .getCommandContext()
        .getDeploymentManager()
        .findDeploymentById(deploymentId);
      deployment.setNew(false);
      deploy(deployment);
      processDefinition = processDefinitionCache.get(processDefinitionId);
      
      if (processDefinition==null) {
        throw new ActivitiException("deployment '"+deploymentId+"' didn't put process definition '"+processDefinitionId+"' in the cache");
      }
    }
    return processDefinition;
  }

  /**
   * Adds the process definition.
   *
   * @param processDefinition the process definition
   */
  public void addProcessDefinition(ProcessDefinitionEntity processDefinition) {
    processDefinitionCache.put(processDefinition.getId(), processDefinition);
  }

  /**
   * Removes the process definition.
   *
   * @param processDefinitionId the process definition id
   */
  public void removeProcessDefinition(String processDefinitionId) {
    processDefinitionCache.remove(processDefinitionId);
  }

  /**
   * Adds the knowledge base.
   *
   * @param knowledgeBaseId the knowledge base id
   * @param knowledgeBase the knowledge base
   */
  public void addKnowledgeBase(String knowledgeBaseId, Object knowledgeBase) {
    knowledgeBaseCache.put(knowledgeBaseId, knowledgeBase);
  }

  /**
   * Removes the knowledge base.
   *
   * @param knowledgeBaseId the knowledge base id
   */
  public void removeKnowledgeBase(String knowledgeBaseId) {
    knowledgeBaseCache.remove(knowledgeBaseId);
  }
  
  /**
   * Discard process definition cache.
   */
  public void discardProcessDefinitionCache() {
    processDefinitionCache.clear();
  }

  /**
   * Discard knowledge base cache.
   */
  public void discardKnowledgeBaseCache() {
    knowledgeBaseCache.clear();
  }
  // getters and setters //////////////////////////////////////////////////////

  /**
   * Gets the process definition cache.
   *
   * @return the process definition cache
   */
  public Map<String, ProcessDefinitionEntity> getProcessDefinitionCache() {
    return processDefinitionCache;
  }
  
  /**
   * Sets the process definition cache.
   *
   * @param processDefinitionCache the process definition cache
   */
  public void setProcessDefinitionCache(Map<String, ProcessDefinitionEntity> processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
  }
  
  /**
   * Gets the knowledge base cache.
   *
   * @return the knowledge base cache
   */
  public Map<String, Object> getKnowledgeBaseCache() {
    return knowledgeBaseCache;
  }
  
  /**
   * Sets the knowledge base cache.
   *
   * @param knowledgeBaseCache the knowledge base cache
   */
  public void setKnowledgeBaseCache(Map<String, Object> knowledgeBaseCache) {
    this.knowledgeBaseCache = knowledgeBaseCache;
  }
  
  /**
   * Gets the deployers.
   *
   * @return the deployers
   */
  public List<Deployer> getDeployers() {
    return deployers;
  }
  
  /**
   * Sets the deployers.
   *
   * @param deployers the new deployers
   */
  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }
}
