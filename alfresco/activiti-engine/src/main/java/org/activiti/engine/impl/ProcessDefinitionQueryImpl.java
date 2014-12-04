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
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;


// TODO: Auto-generated Javadoc
/**
 * The Class ProcessDefinitionQueryImpl.
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Saeid Mirzaei
 */
public class ProcessDefinitionQueryImpl extends AbstractQuery<ProcessDefinitionQuery, ProcessDefinition> 
  implements ProcessDefinitionQuery {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The category. */
  protected String category;
  
  /** The category like. */
  protected String categoryLike;
  
  /** The name. */
  protected String name;
  
  /** The name like. */
  protected String nameLike;
  
  /** The deployment id. */
  protected String deploymentId;
  
  /** The key. */
  protected String key;
  
  /** The key like. */
  protected String keyLike;
  
  /** The resource name. */
  protected String resourceName;
  
  /** The resource name like. */
  protected String resourceNameLike;
  
  /** The version. */
  protected Integer version;
  
  /** The latest. */
  protected boolean latest = false;
  
  /** The suspension state. */
  protected SuspensionState suspensionState;
  
  /** The authorization user id. */
  protected String authorizationUserId;
  
  /** The proc def id. */
  protected String procDefId;
  
  /** The event subscription name. */
  protected String eventSubscriptionName;
  
  /** The event subscription type. */
  protected String eventSubscriptionType;

  /**
   * Instantiates a new process definition query impl.
   */
  public ProcessDefinitionQueryImpl() {
  }

  /**
   * Instantiates a new process definition query impl.
   *
   * @param commandContext the command context
   */
  public ProcessDefinitionQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  /**
   * Instantiates a new process definition query impl.
   *
   * @param commandExecutor the command executor
   */
  public ProcessDefinitionQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionId(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionId(String processDefinitionId) {
    this.id = processDefinitionId;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionCategory(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionCategory(String category) {
    if (category == null) {
      throw new ActivitiException("category is null");
    }
    this.category = category;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionCategoryLike(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionCategoryLike(String categoryLike) {
    if (categoryLike == null) {
      throw new ActivitiException("categoryLike is null");
    }
    this.categoryLike = categoryLike;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionName(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionName(String name) {
    if (name == null) {
      throw new ActivitiException("name is null");
    }
    this.name = name;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionNameLike(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionNameLike(String nameLike) {
    if (nameLike == null) {
      throw new ActivitiException("nameLike is null");
    }
    this.nameLike = nameLike;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#deploymentId(java.lang.String)
   */
  public ProcessDefinitionQueryImpl deploymentId(String deploymentId) {
    if (deploymentId == null) {
      throw new ActivitiException("id is null");
    }
    this.deploymentId = deploymentId;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionKey(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionKey(String key) {
    if (key == null) {
      throw new ActivitiException("key is null");
    }
    this.key = key;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionKeyLike(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionKeyLike(String keyLike) {
    if (keyLike == null) {
      throw new ActivitiException("keyLike is null");
    }
    this.keyLike = keyLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionResourceName(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionResourceName(String resourceName) {
    if (resourceName == null) {
      throw new ActivitiException("resourceName is null");
    }
    this.resourceName = resourceName;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionResourceNameLike(java.lang.String)
   */
  public ProcessDefinitionQueryImpl processDefinitionResourceNameLike(String resourceNameLike) {
    if (resourceNameLike == null) {
      throw new ActivitiException("resourceNameLike is null");
    }
    this.resourceNameLike = resourceNameLike;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#processDefinitionVersion(java.lang.Integer)
   */
  public ProcessDefinitionQueryImpl processDefinitionVersion(Integer version) {
    if (version == null) {
      throw new ActivitiException("version is null");
    } else if (version <= 0) {
      throw new ActivitiException("version must be positive");
    }
    this.version = version;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#latestVersion()
   */
  public ProcessDefinitionQueryImpl latestVersion() {
    this.latest = true;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#active()
   */
  public ProcessDefinitionQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;    
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#suspended()
   */
  public ProcessDefinitionQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#messageEventSubscription(java.lang.String)
   */
  public ProcessDefinitionQuery messageEventSubscription(String messageName) {
    return eventSubscription("message", messageName);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#messageEventSubscriptionName(java.lang.String)
   */
  public ProcessDefinitionQuery messageEventSubscriptionName(String messageName) {
    return eventSubscription("message", messageName);
  }

  /**
   * Process definition starter.
   *
   * @param procDefId the proc def id
   * @return the process definition query
   */
  public ProcessDefinitionQuery processDefinitionStarter(String procDefId) {
    this.procDefId = procDefId;
    return this;
  }

  /**
   * Event subscription.
   *
   * @param eventType the event type
   * @param eventName the event name
   * @return the process definition query
   */
  public ProcessDefinitionQuery eventSubscription(String eventType, String eventName) {
    if(eventName == null) {
      throw new ActivitiException("event name is null");
    }
    if(eventType == null) {
      throw new ActivitiException("event type is null");
    }
    this.eventSubscriptionType = eventType;
    this.eventSubscriptionName = eventName;
    return this;
  }
  
  //sorting ////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#orderByDeploymentId()
   */
  public ProcessDefinitionQuery orderByDeploymentId() {
    return orderBy(ProcessDefinitionQueryProperty.DEPLOYMENT_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#orderByProcessDefinitionKey()
   */
  public ProcessDefinitionQuery orderByProcessDefinitionKey() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_KEY);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#orderByProcessDefinitionCategory()
   */
  public ProcessDefinitionQuery orderByProcessDefinitionCategory() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_CATEGORY);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#orderByProcessDefinitionId()
   */
  public ProcessDefinitionQuery orderByProcessDefinitionId() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_ID);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#orderByProcessDefinitionVersion()
   */
  public ProcessDefinitionQuery orderByProcessDefinitionVersion() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_VERSION);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#orderByProcessDefinitionName()
   */
  public ProcessDefinitionQuery orderByProcessDefinitionName() {
    return orderBy(ProcessDefinitionQueryProperty.PROCESS_DEFINITION_NAME);
  }
  
  //results ////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeCount(org.activiti.engine.impl.interceptor.CommandContext)
   */
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getProcessDefinitionManager()
      .findProcessDefinitionCountByQueryCriteria(this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#executeList(org.activiti.engine.impl.interceptor.CommandContext, org.activiti.engine.impl.Page)
   */
  public List<ProcessDefinition> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getProcessDefinitionManager()
      .findProcessDefinitionsByQueryCriteria(this, page);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.AbstractQuery#checkQueryOk()
   */
  public void checkQueryOk() {
    super.checkQueryOk();
    
    // latest() makes only sense when used with key() or keyLike()
    if (latest && ( (id != null) || (name != null) || (nameLike != null) || (version != null) || (deploymentId != null) ) ){
      throw new ActivitiException("Calling latest() can only be used in combination with key(String) and keyLike(String)");
    }
  }
  
  //getters ////////////////////////////////////////////
  
  /**
   * Gets the deployment id.
   *
   * @return the deployment id
   */
  public String getDeploymentId() {
    return deploymentId;
  }
  
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
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }
  
  /**
   * Gets the key like.
   *
   * @return the key like
   */
  public String getKeyLike() {
    return keyLike;
  }
  
  /**
   * Gets the version.
   *
   * @return the version
   */
  public Integer getVersion() {
    return version;
  }
  
  /**
   * Checks if is latest.
   *
   * @return true, if is latest
   */
  public boolean isLatest() {
    return latest;
  }
  
  /**
   * Gets the category.
   *
   * @return the category
   */
  public String getCategory() {
    return category;
  }
  
  /**
   * Gets the category like.
   *
   * @return the category like
   */
  public String getCategoryLike() {
    return categoryLike;
  }
  
  /**
   * Gets the resource name.
   *
   * @return the resource name
   */
  public String getResourceName() {
    return resourceName;
  }
  
  /**
   * Gets the resource name like.
   *
   * @return the resource name like
   */
  public String getResourceNameLike() {
    return resourceNameLike;
  }  
  
  /**
   * Gets the suspension state.
   *
   * @return the suspension state
   */
  public SuspensionState getSuspensionState() {
    return suspensionState;
  }  
  
  /**
   * Sets the suspension state.
   *
   * @param suspensionState the new suspension state
   */
  public void setSuspensionState(SuspensionState suspensionState) {
    this.suspensionState = suspensionState;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.repository.ProcessDefinitionQuery#startableByUser(java.lang.String)
   */
  public ProcessDefinitionQueryImpl startableByUser(String userId) {
    if (userId == null) {
      throw new ActivitiException("userId is null");
    }
    this.authorizationUserId = userId;
    return this;
  }
}
