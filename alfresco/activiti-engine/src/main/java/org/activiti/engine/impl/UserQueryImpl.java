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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


// TODO: Auto-generated Javadoc
/**
 * The Class UserQueryImpl.
 *
 * @author Joram Barrez
 */
public class UserQueryImpl extends AbstractQuery<UserQuery, User> implements UserQuery {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The first name. */
  protected String firstName;
  
  /** The first name like. */
  protected String firstNameLike;
  
  /** The last name. */
  protected String lastName;
  
  /** The last name like. */
  protected String lastNameLike;
  
  /** The email. */
  protected String email;
  
  /** The email like. */
  protected String emailLike;
  
  /** The group id. */
  protected String groupId;
  
  /** The proc def id. */
  protected String procDefId;
  
  /**
   * Instantiates a new user query impl.
   */
  public UserQueryImpl() {
  }

  /**
   * Instantiates a new user query impl.
   *
   * @param commandContext the command context
   */
  public UserQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  /**
   * Instantiates a new user query impl.
   *
   * @param commandExecutor the command executor
   */
  public UserQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#userId(java.lang.String)
   */
  public UserQuery userId(String id) {
    if (id == null) {
      throw new ActivitiException("Provided id is null");
    }
    this.id = id;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#userFirstName(java.lang.String)
   */
  public UserQuery userFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#userFirstNameLike(java.lang.String)
   */
  public UserQuery userFirstNameLike(String firstNameLike) {
    if (firstNameLike == null) {
      throw new ActivitiException("Provided firstNameLike is null");
    }
    this.firstNameLike = firstNameLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#userLastName(java.lang.String)
   */
  public UserQuery userLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#userLastNameLike(java.lang.String)
   */
  public UserQuery userLastNameLike(String lastNameLike) {
    if (lastNameLike == null) {
      throw new ActivitiException("Provided lastNameLike is null");
    }
    this.lastNameLike = lastNameLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#userEmail(java.lang.String)
   */
  public UserQuery userEmail(String email) {
    this.email = email;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#userEmailLike(java.lang.String)
   */
  public UserQuery userEmailLike(String emailLike) {
    if (emailLike == null) {
      throw new ActivitiException("Provided emailLike is null");
    }
    this.emailLike = emailLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#memberOfGroup(java.lang.String)
   */
  public UserQuery memberOfGroup(String groupId) {
    if (groupId == null) {
      throw new ActivitiException("Provided groupIds is null or empty");
    }
    this.groupId = groupId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#potentialStarter(java.lang.String)
   */
  public UserQuery potentialStarter(String procDefId) {
    if (procDefId == null) {
      throw new ActivitiException("Provided processDefinitionId is null or empty");
    }
    this.procDefId = procDefId;
    return this;
    
  }

  //sorting //////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#orderByUserId()
   */
  public UserQuery orderByUserId() {
    return orderBy(UserQueryProperty.USER_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#orderByUserEmail()
   */
  public UserQuery orderByUserEmail() {
    return orderBy(UserQueryProperty.EMAIL);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#orderByUserFirstName()
   */
  public UserQuery orderByUserFirstName() {
    return orderBy(UserQueryProperty.FIRST_NAME);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.UserQuery#orderByUserLastName()
   */
  public UserQuery orderByUserLastName() {
    return orderBy(UserQueryProperty.LAST_NAME);
  }
  
  //results //////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getUserManager()
      .findUserCountByQueryCriteria(this);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  public List<User> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getUserManager()
      .findUserByQueryCriteria(this, page);
  }
  
  //getters //////////////////////////////////////////////////////////

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }
  
  /**
   * Gets the first name.
   *
   * @return the first name
   */
  public String getFirstName() {
    return firstName;
  }
  
  /**
   * Gets the first name like.
   *
   * @return the first name like
   */
  public String getFirstNameLike() {
    return firstNameLike;
  }
  
  /**
   * Gets the last name.
   *
   * @return the last name
   */
  public String getLastName() {
    return lastName;
  }
  
  /**
   * Gets the last name like.
   *
   * @return the last name like
   */
  public String getLastNameLike() {
    return lastNameLike;
  }
  
  /**
   * Gets the email.
   *
   * @return the email
   */
  public String getEmail() {
    return email;
  }
  
  /**
   * Gets the email like.
   *
   * @return the email like
   */
  public String getEmailLike() {
    return emailLike;
  }
  
  /**
   * Gets the group id.
   *
   * @return the group id
   */
  public String getGroupId() {
    return groupId;
  }
}
