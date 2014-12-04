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

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.identity.Account;


// TODO: Auto-generated Javadoc
/**
 * The Class IdentityInfoEntity.
 *
 * @author Tom Baeyens
 */
public class IdentityInfoEntity implements PersistentObject, Account, Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The Constant TYPE_USERACCOUNT. */
  public static final String TYPE_USERACCOUNT = "account";
  
  /** The Constant TYPE_USERINFO. */
  public static final String TYPE_USERINFO = "userinfo";
  
  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision;
  
  /** The type. */
  protected String type;
  
  /** The user id. */
  protected String userId;
  
  /** The key. */
  protected String key;
  
  /** The value. */
  protected String value;
  
  /** The password. */
  protected String password;
  
  /** The password bytes. */
  protected byte[] passwordBytes;
  
  /** The parent id. */
  protected String parentId;
  
  /** The details. */
  protected Map<String, String> details;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("value", value);
    persistentState.put("password", passwordBytes);
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
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }
  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * Gets the user id.
   *
   * @return the user id
   */
  public String getUserId() {
    return userId;
  }
  
  /**
   * Sets the user id.
   *
   * @param userId the new user id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the key.
   *
   * @param key the new key
   */
  public void setKey(String key) {
    this.key = key;
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

  /**
   * Gets the password bytes.
   *
   * @return the password bytes
   */
  public byte[] getPasswordBytes() {
    return passwordBytes;
  }

  /**
   * Sets the password bytes.
   *
   * @param passwordBytes the new password bytes
   */
  public void setPasswordBytes(byte[] passwordBytes) {
    this.passwordBytes = passwordBytes;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.identity.Account#getPassword()
   */
  public String getPassword() {
    return password;
  }
  
  /**
   * Sets the password.
   *
   * @param password the new password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.identity.Account#getName()
   */
  public String getName() {
    return key;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.identity.Account#getUsername()
   */
  public String getUsername() {
    return value;
  }

  /**
   * Gets the parent id.
   *
   * @return the parent id
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * Sets the parent id.
   *
   * @param parentId the new parent id
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.identity.Account#getDetails()
   */
  public Map<String, String> getDetails() {
    return details;
  }
  
  /**
   * Sets the details.
   *
   * @param details the details
   */
  public void setDetails(Map<String, String> details) {
    this.details = details;
  }
}
