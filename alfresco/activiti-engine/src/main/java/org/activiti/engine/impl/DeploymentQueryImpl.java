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

package org.activiti.engine.impl;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;


// TODO: Auto-generated Javadoc
/**
 * The Class DeploymentQueryImpl.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeploymentQueryImpl extends AbstractQuery<DeploymentQuery, Deployment> implements DeploymentQuery, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;  
  
  /** The deployment id. */
  protected String deploymentId;
  
  /** The name. */
  protected String name;
  
  /** The name like. */
  protected String nameLike;

  /**
   * Instantiates a new deployment query impl.
   */
  public DeploymentQueryImpl() {
  }

  /**
   * Instantiates a new deployment query impl.
   *
   * @param commandContext the command context
   */
  public DeploymentQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  /**
   * Instantiates a new deployment query impl.
   *
   * @param commandExecutor the command executor
   */
  public DeploymentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentQuery#deploymentId(java.lang.String)
   */
  public DeploymentQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiException("Deployment id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentQuery#deploymentName(java.lang.String)
   */
  public DeploymentQueryImpl deploymentName(String deploymentName) {
    if (deploymentName == null) {
      throw new ActivitiException("deploymentName is null");
    }
    this.name = deploymentName;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentQuery#deploymentNameLike(java.lang.String)
   */
  public DeploymentQueryImpl deploymentNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("deploymentNameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }
  
  //sorting ////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentQuery#orderByDeploymentId()
   */
  public DeploymentQuery orderByDeploymentId() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentQuery#orderByDeploymenTime()
   */
  public DeploymentQuery orderByDeploymenTime() {
    return orderBy(DeploymentQueryProperty.DEPLOY_TIME);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.DeploymentQuery#orderByDeploymentName()
   */
  public DeploymentQuery orderByDeploymentName() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_NAME);
  }
  
  //results ////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getDeploymentManager()
      .findDeploymentCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  @Override
  public List<Deployment> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getDeploymentManager()
      .findDeploymentsByQueryCriteria(this, page);
  }
  
  //getters ////////////////////////////////////////////////////////
  
  /**
   * Gets the deployment id.
   *
   * @return the deployment id
   */
  public String getDeploymentId() {
    return deploymentId;
  }
  
  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the name like.
   *
   * @return the name like
   */
  public String getNameLike() {
    return nameLike;
  }
}
