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
 * The Class MembershipEntity.
 *
 * @author Tom Baeyens
 */
public class MembershipEntity implements Serializable, PersistentObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The user. */
  protected UserEntity user;
  
  /** The group. */
  protected GroupEntity group;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    // membership is not updatable
    return MembershipEntity.class;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getId()
   */
  public String getId() {
    // membership doesn't have an id
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#setId(java.lang.String)
   */
  public void setId(String id) {
    // membership doesn't have an id
  }

  /**
   * Gets the user.
   *
   * @return the user
   */
  public UserEntity getUser() {
    return user;
  }
  
  /**
   * Sets the user.
   *
   * @param user the new user
   */
  public void setUser(UserEntity user) {
    this.user = user;
  }
  
  /**
   * Gets the group.
   *
   * @return the group
   */
  public GroupEntity getGroup() {
    return group;
  }
  
  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(GroupEntity group) {
    this.group = group;
  }
}
