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

package org.activiti.engine.repository;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.query.Query;

// TODO: Auto-generated Javadoc
/**
 * Allows programmatic querying of {@link Deployment}s.
 * 
 * Note that it is impossible to retrieve the deployment resources through the
 * results of this operation, since that would cause a huge transfer of
 * (possibly) unneeded bytes over the wire.
 * 
 * To retrieve the actual bytes of a deployment resource use the operations on the
 * {@link RepositoryService#getDeploymentResourceNames(String)} 
 * and {@link RepositoryService#getResourceAsStream(String, String)}
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface DeploymentQuery extends Query<DeploymentQuery, Deployment>{
  
  /**
   * Only select deployments with the given deployment id.
   *
   * @param deploymentId the deployment id
   * @return the deployment query
   */
  DeploymentQuery deploymentId(String deploymentId);
  
  /**
   * Only select deployments with the given name.
   *
   * @param name the name
   * @return the deployment query
   */
  DeploymentQuery deploymentName(String name);
  
  /**
   * Only select deployments with a name like the given string.
   *
   * @param nameLike the name like
   * @return the deployment query
   */
  DeploymentQuery deploymentNameLike(String nameLike);

  //sorting ////////////////////////////////////////////////////////
  
  /**
   * Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the deployment query
   */
  DeploymentQuery orderByDeploymentId();
  
  /**
   * Order by deployment name (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the deployment query
   */
  DeploymentQuery orderByDeploymentName();
  
  /**
   * Order by deployment time (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the deployment query
   */
  DeploymentQuery orderByDeploymenTime();
}
