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
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.db.PersistentObject;


// TODO: Auto-generated Javadoc
/**
 * The Class GroupEntity.
 *
 * @author Tom Baeyens
 */
public class GroupEntity implements Group, Serializable, PersistentObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision;
  
  /** The name. */
  protected String name;
  
  /** The type. */
  protected String type;
  
  /**
   * Instantiates a new group entity.
   */
  public GroupEntity() {
  }
  
  /**
   * Instantiates a new group entity.
   *
   * @param id the id
   */
  public GroupEntity(String id) {
    this.id = id;
  }
  
  /**
   * Update.
   *
   * @param group the group
   */
  public void update(GroupEntity group) {
    this.name = group.getName();
    this.type = group.getType();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("name", name);
    persistentState.put("type", type);
    return persistentState;
  }
  
  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision+1;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.identity.Group#getId()
   */
  public String getId() {
    return id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.Group#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.Group#getName()
   */
  public String getName() {
    return name;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.Group#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.Group#getType()
   */
  public String getType() {
    return type;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.Group#setType(java.lang.String)
   */
  public void setType(String type) {
    this.type = type;
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
