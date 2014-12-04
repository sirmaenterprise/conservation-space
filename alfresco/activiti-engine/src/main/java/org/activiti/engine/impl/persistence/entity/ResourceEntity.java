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

import org.activiti.engine.impl.db.PersistentObject;


// TODO: Auto-generated Javadoc
/**
 * The Class ResourceEntity.
 *
 * @author Tom Baeyens
 */
public class ResourceEntity implements Serializable, PersistentObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The bytes. */
  protected byte[] bytes;
  
  /** The deployment id. */
  protected String deploymentId;
  
  /** The generated. */
  protected boolean generated = false;
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getId()
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
  
  /**
   * Gets the name.
   *
   * @return the name
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
   * Gets the bytes.
   *
   * @return the bytes
   */
  public byte[] getBytes() {
    return bytes;
  }
  
  /**
   * Sets the bytes.
   *
   * @param bytes the new bytes
   */
  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }
  
  /**
   * Gets the deployment id.
   *
   * @return the deployment id
   */
  public String getDeploymentId() {
    return deploymentId;
  }
  
  /**
   * Sets the deployment id.
   *
   * @param deploymentId the new deployment id
   */
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    return ResourceEntity.class;
  }
  
  /**
   * Sets the generated.
   *
   * @param generated the new generated
   */
  public void setGenerated(boolean generated) {
    this.generated = generated;
  }
  
  /**
   * Indicated whether or not the resource has been generated while deploying rather than
   * being actual part of the deployment.
   *
   * @return true, if is generated
   */
  public boolean isGenerated() {
    return generated;
  }
}
