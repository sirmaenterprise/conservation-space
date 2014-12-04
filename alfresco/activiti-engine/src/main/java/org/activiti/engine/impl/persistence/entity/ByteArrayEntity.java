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
 * The Class ByteArrayEntity.
 *
 * @author Tom Baeyens
 */
public class ByteArrayEntity implements Serializable, PersistentObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant PERSISTENTSTATE_NULL. */
  private static final Object PERSISTENTSTATE_NULL = new Object();

  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision;
  
  /** The name. */
  protected String name;
  
  /** The bytes. */
  protected byte[] bytes;
  
  /** The deployment id. */
  protected String deploymentId;

  /**
   * Instantiates a new byte array entity.
   */
  public ByteArrayEntity() {
  }

  /**
   * Instantiates a new byte array entity.
   *
   * @param name the name
   * @param bytes the bytes
   */
  public ByteArrayEntity(String name, byte[] bytes) {
    this.name = name;
    this.bytes = bytes;
  }

  /**
   * Instantiates a new byte array entity.
   *
   * @param bytes the bytes
   */
  public ByteArrayEntity(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Gets the bytes.
   *
   * @return the bytes
   */
  public byte[] getBytes() {
    return bytes;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    return (bytes != null ? bytes : PERSISTENTSTATE_NULL);
  }
  
  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision+1;
  }

  // getters and setters //////////////////////////////////////////////////////

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
  
  /**
   * Sets the bytes.
   *
   * @param bytes the new bytes
   */
  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }
  
  /**
   * Gets the revision.
   *
   * @return the revision
   */
  public int getRevision() {
    return revision;
  }
  
  /**
   * Sets the revision.
   *
   * @param revision the new revision
   */
  public void setRevision(int revision) {
    this.revision = revision;
  }
}
