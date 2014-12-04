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
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


// TODO: Auto-generated Javadoc
/**
 * The Class GroupQueryImpl.
 *
 * @author Joram Barrez
 */
public class GroupQueryImpl extends AbstractQuery<GroupQuery, Group> implements GroupQuery {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The name like. */
  protected String nameLike;
  
  /** The type. */
  protected String type;
  
  /** The user id. */
  protected String userId;
  
  /** The proc def id. */
  protected String procDefId;
  

  /**
   * Instantiates a new group query impl.
   */
  public GroupQueryImpl() {
  }

  /**
   * Instantiates a new group query impl.
   *
   * @param commandContext the command context
   */
  public GroupQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  /**
   * Instantiates a new group query impl.
   *
   * @param commandExecutor the command executor
   */
  public GroupQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#groupId(java.lang.String)
   */
  public GroupQuery groupId(String id) {
    if (id == null) {
      throw new ActivitiException("Provided id is null");
    }
    this.id = id;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#groupName(java.lang.String)
   */
  public GroupQuery groupName(String name) {
    if (name == null) {
      throw new ActivitiException("Provided name is null");
    }
    this.name = name;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#groupNameLike(java.lang.String)
   */
  public GroupQuery groupNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("Provided nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#groupType(java.lang.String)
   */
  public GroupQuery groupType(String type) {
    if (type == null) {
      throw new ActivitiException("Provided type is null");
    }
    this.type = type;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#groupMember(java.lang.String)
   */
  public GroupQuery groupMember(String userId) {
    if (userId == null) {
      throw new ActivitiException("Provided userId is null");
    }
    this.userId = userId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#potentialStarter(java.lang.String)
   */
  public GroupQuery potentialStarter(String procDefId) {
    if (procDefId == null) {
      throw new ActivitiException("Provided processDefinitionId is null or empty");
    }
    this.procDefId = procDefId;
    return this;
    
  }
  
  //sorting ////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#orderByGroupId()
   */
  public GroupQuery orderByGroupId() {
    return orderBy(GroupQueryProperty.GROUP_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#orderByGroupName()
   */
  public GroupQuery orderByGroupName() {
    return orderBy(GroupQueryProperty.NAME);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.identity.GroupQuery#orderByGroupType()
   */
  public GroupQuery orderByGroupType() {
    return orderBy(GroupQueryProperty.TYPE);
  }
  
  //results ////////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getGroupManager()
      .findGroupCountByQueryCriteria(this);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  public List<Group> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getGroupManager()
      .findGroupByQueryCriteria(this, page);
  }
  
  //getters ////////////////////////////////////////////////////////
  
  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
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
   * Gets the name like.
   *
   * @return the name like
   */
  public String getNameLike() {
    return nameLike;
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
   * Gets the user id.
   *
   * @return the user id
   */
  public String getUserId() {
    return userId;
  }

  
}
