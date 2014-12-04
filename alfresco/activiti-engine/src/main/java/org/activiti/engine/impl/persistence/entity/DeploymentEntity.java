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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.repository.Deployment;


// TODO: Auto-generated Javadoc
/**
 * The Class DeploymentEntity.
 *
 * @author Tom Baeyens
 */
public class DeploymentEntity implements Serializable, Deployment, PersistentObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The resources. */
  protected Map<String, ResourceEntity> resources;
  
  /** The deployment time. */
  protected Date deploymentTime;
  
  /** The validating schema. */
  protected boolean validatingSchema = true;
  
  /** The is new. */
  protected boolean isNew;
  
  /**
   * Gets the resource.
   *
   * @param resourceName the resource name
   * @return the resource
   */
  public ResourceEntity getResource(String resourceName) {
    return getResources().get(resourceName);
  }

  /**
   * Adds the resource.
   *
   * @param resource the resource
   */
  public void addResource(ResourceEntity resource) {
    if (resources==null) {
      resources = new HashMap<String, ResourceEntity>();
    }
    resources.put(resource.getName(), resource);
  }

  // lazy loading /////////////////////////////////////////////////////////////
  /**
   * Gets the resources.
   *
   * @return the resources
   */
  public Map<String, ResourceEntity> getResources() {
    if (resources==null && id!=null) {
      List<ResourceEntity> resourcesList = Context
        .getCommandContext()
        .getResourceManager()
        .findResourcesByDeploymentId(id);
      resources = new HashMap<String, ResourceEntity>();
      for (ResourceEntity resource: resourcesList) {
        resources.put(resource.getName(), resource);
      }
    }
    return resources;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    // properties of this entity are immutable
    // so always the same value is returned
    // so never will an update be issued for a DeploymentEntity
    return DeploymentEntity.class;
  }

  // getters and setters //////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.Deployment#getId()
   */
  public String getId() {
    return id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.Deployment#getName()
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Sets the resources.
   *
   * @param resources the resources
   */
  public void setResources(Map<String, ResourceEntity> resources) {
    this.resources = resources;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.Deployment#getDeploymentTime()
   */
  public Date getDeploymentTime() {
    return deploymentTime;
  }
  
  /**
   * Sets the deployment time.
   *
   * @param deploymentTime the new deployment time
   */
  public void setDeploymentTime(Date deploymentTime) {
    this.deploymentTime = deploymentTime;
  }

  /**
   * Checks if is validating schema.
   *
   * @return true, if is validating schema
   */
  public boolean isValidatingSchema() {
    return validatingSchema;
  }
  
  /**
   * Sets the validating schema.
   *
   * @param validatingSchema the new validating schema
   */
  public void setValidatingSchema(boolean validatingSchema) {
    this.validatingSchema = validatingSchema;
  }

  /**
   * Checks if is new.
   *
   * @return true, if is new
   */
  public boolean isNew() {
    return isNew;
  }
  
  /**
   * Sets the new.
   *
   * @param isNew the new new
   */
  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }
}
