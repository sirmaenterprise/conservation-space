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

import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cmd.SaveUserCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;


// TODO: Auto-generated Javadoc
/**
 * The Class UserEntity.
 *
 * @author Tom Baeyens
 */
public class UserEntity implements User, Serializable, PersistentObject {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  protected String id;
  
  /** The revision. */
  protected int revision;
  
  /** The first name. */
  protected String firstName;
  
  /** The last name. */
  protected String lastName;
  
  /** The email. */
  protected String email;
  
  /** The password. */
  protected String password;
  
  /** The picture byte array id. */
  protected String pictureByteArrayId;
  
  /** The picture byte array. */
  protected ByteArrayEntity pictureByteArray;
  
  /**
   * Instantiates a new user entity.
   */
  public UserEntity() {
  }
  
  /**
   * Instantiates a new user entity.
   *
   * @param id the id
   */
  public UserEntity(String id) {
    this.id = id;
  }
  
  /**
   * update this user by copying all the given user's signalData into this user.
   *
   * @param user the user
   * @see SaveUserCmd
   */
  public void update(UserEntity user) {
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.email = user.getEmail();
    this.password = user.getPassword();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.db.PersistentObject#getPersistentState()
   */
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("firstName", firstName);
    persistentState.put("lastName", lastName);
    persistentState.put("email", email);
    persistentState.put("password", password);
    persistentState.put("pictureByteArrayId", pictureByteArrayId);
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
  
  /**
   * Gets the picture.
   *
   * @return the picture
   */
  public Picture getPicture() {
    if (pictureByteArrayId==null) {
      return null;
    }
    ByteArrayEntity pictureByteArray = getPictureByteArray();
    return new Picture(pictureByteArray.getBytes(), pictureByteArray.getName());
  }
  
  /**
   * Sets the picture.
   *
   * @param picture the new picture
   */
  public void setPicture(Picture picture) {
    if (pictureByteArrayId!=null) {
      Context
        .getCommandContext()
        .getDbSqlSession()
        .delete(ByteArrayEntity.class, pictureByteArrayId);
    }
    if (picture!=null) {
      pictureByteArray = new ByteArrayEntity(picture.getMimeType(), picture.getBytes());
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(pictureByteArray);
      pictureByteArrayId = pictureByteArray.getId();
    } else {
      pictureByteArrayId = null;
      pictureByteArray = null;
    }
  }

  /**
   * Gets the picture byte array.
   *
   * @return the picture byte array
   */
  private ByteArrayEntity getPictureByteArray() {
    if (pictureByteArrayId!=null && pictureByteArray==null) {
      pictureByteArray = Context
        .getCommandContext()
        .getDbSqlSession()
        .selectById(ByteArrayEntity.class, pictureByteArrayId);
    }
    return pictureByteArray;
  }


  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#getId()
   */
  public String getId() {
    return id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#setId(java.lang.String)
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#getFirstName()
   */
  public String getFirstName() {
    return firstName;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#setFirstName(java.lang.String)
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#getLastName()
   */
  public String getLastName() {
    return lastName;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#setLastName(java.lang.String)
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#getEmail()
   */
  public String getEmail() {
    return email;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#setEmail(java.lang.String)
   */
  public void setEmail(String email) {
    this.email = email;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#getPassword()
   */
  public String getPassword() {
    return password;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.User#setPassword(java.lang.String)
   */
  public void setPassword(String password) {
    this.password = password;
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
   * Gets the picture byte array id.
   *
   * @return the picture byte array id
   */
  public String getPictureByteArrayId() {
    return pictureByteArrayId;
  }
  
  /**
   * Sets the picture byte array id.
   *
   * @param pictureByteArrayId the new picture byte array id
   */
  public void setPictureByteArrayId(String pictureByteArrayId) {
    this.pictureByteArrayId = pictureByteArrayId;
  }
}
