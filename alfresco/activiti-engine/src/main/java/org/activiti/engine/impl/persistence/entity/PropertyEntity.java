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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.db.PersistentObject;



// TODO: Auto-generated Javadoc
/**
 * The Class PropertyEntity.
 *
 * @author Tom Baeyens
 */
public class PropertyEntity implements PersistentObject, Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The name. */
  String name;
  
  /** The revision. */
  int revision;
  
  /** The value. */
  String value;

  /**
   * Instantiates a new property entity.
   */
  public PropertyEntity() {
  }

  /**
   * Instantiates a new property entity.
   *
   * @param name the name
   * @param value the value
   */
  public PropertyEntity(String name, String value) {
    this.name = name;
    this.value = value;
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
  
  /**
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }
  
  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(String value) {
    this.value = value;
  }
  
  // persistent object methods ////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getId()
   */
  public String getId() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    return value;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    throw new ActivitiException("only provided id generation allowed for properties");
  }
  
  /**
   * Gets the revision next.
   *
   * @return the revision next
   */
  public int getRevisionNext() {
    return revision+1;
  }
}
