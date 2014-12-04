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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.DeploymentQuery;



// TODO: Auto-generated Javadoc
/**
 * Contains the possible properties that can be used in a {@link DeploymentQuery}.
 * 
 * @author Joram Barrez
 */
public class DeploymentQueryProperty implements QueryProperty {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant properties. */
  private static final Map<String, DeploymentQueryProperty> properties = new HashMap<String, DeploymentQueryProperty>();

  /** The Constant DEPLOYMENT_ID. */
  public static final DeploymentQueryProperty DEPLOYMENT_ID = new DeploymentQueryProperty("D.ID_");
  
  /** The Constant DEPLOYMENT_NAME. */
  public static final DeploymentQueryProperty DEPLOYMENT_NAME = new DeploymentQueryProperty("D.NAME_");
  
  /** The Constant DEPLOY_TIME. */
  public static final DeploymentQueryProperty DEPLOY_TIME = new DeploymentQueryProperty("D.DEPLOY_TIME_");
  
  /** The name. */
  private String name;

  /**
   * Instantiates a new deployment query property.
   *
   * @param name the name
   */
  public DeploymentQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.query.QueryProperty#getName()
   */
  public String getName() {
    return name;
  }
  
  /**
   * Find by name.
   *
   * @param propertyName the property name
   * @return the deployment query property
   */
  public static DeploymentQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

  
}
