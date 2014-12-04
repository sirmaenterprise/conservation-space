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

package org.activiti.engine.impl.persistence;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.entity.AttachmentManager;
import org.activiti.engine.impl.persistence.entity.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.ExecutionManager;
import org.activiti.engine.impl.persistence.entity.GroupManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceManager;
import org.activiti.engine.impl.persistence.entity.HistoricDetailManager;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceManager;
import org.activiti.engine.impl.persistence.entity.IdentityInfoManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkManager;
import org.activiti.engine.impl.persistence.entity.MembershipManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.activiti.engine.impl.persistence.entity.ResourceManager;
import org.activiti.engine.impl.persistence.entity.TaskManager;
import org.activiti.engine.impl.persistence.entity.UserManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceManager;


// TODO: Auto-generated Javadoc
/**
 * The Class AbstractManager.
 *
 * @author Tom Baeyens
 */
public abstract class AbstractManager implements Session {
  
  /**
   * Insert.
   *
   * @param persistentObject the persistent object
   */
  public void insert(PersistentObject persistentObject) {
    getDbSqlSession().insert(persistentObject);
  }

  /**
   * Delete.
   *
   * @param persistentObject the persistent object
   */
  public void delete(PersistentObject persistentObject) {
    getDbSqlSession().delete(persistentObject.getClass(), persistentObject.getId());
  }

  /**
   * Gets the db sql session.
   *
   * @return the db sql session
   */
  protected DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }

  /**
   * Gets the session.
   *
   * @param <T> the generic type
   * @param sessionClass the session class
   * @return the session
   */
  protected <T> T getSession(Class<T> sessionClass) {
    return Context.getCommandContext().getSession(sessionClass);
  }

  /**
   * Gets the deployment manager.
   *
   * @return the deployment manager
   */
  protected DeploymentManager getDeploymentManager() {
    return getSession(DeploymentManager.class);
  }

  /**
   * Gets the resource manager.
   *
   * @return the resource manager
   */
  protected ResourceManager getResourceManager() {
    return getSession(ResourceManager.class);
  }
  
  /**
   * Gets the process definition manager.
   *
   * @return the process definition manager
   */
  protected ProcessDefinitionManager getProcessDefinitionManager() {
    return getSession(ProcessDefinitionManager.class);
  }

  /**
   * Gets the process instance manager.
   *
   * @return the process instance manager
   */
  protected ExecutionManager getProcessInstanceManager() {
    return getSession(ExecutionManager.class);
  }

  /**
   * Gets the task manager.
   *
   * @return the task manager
   */
  protected TaskManager getTaskManager() {
    return getSession(TaskManager.class);
  }

  /**
   * Gets the identity link manager.
   *
   * @return the identity link manager
   */
  protected IdentityLinkManager getIdentityLinkManager() {
    return getSession(IdentityLinkManager.class);
  }

  /**
   * Gets the variable instance manager.
   *
   * @return the variable instance manager
   */
  protected VariableInstanceManager getVariableInstanceManager() {
    return getSession(VariableInstanceManager.class);
  }

  /**
   * Gets the historic process instance manager.
   *
   * @return the historic process instance manager
   */
  protected HistoricProcessInstanceManager getHistoricProcessInstanceManager() {
    return getSession(HistoricProcessInstanceManager.class);
  }

  /**
   * Gets the historic detail manager.
   *
   * @return the historic detail manager
   */
  protected HistoricDetailManager getHistoricDetailManager() {
    return getSession(HistoricDetailManager.class);
  }

  /**
   * Gets the historic activity instance manager.
   *
   * @return the historic activity instance manager
   */
  protected HistoricActivityInstanceManager getHistoricActivityInstanceManager() {
    return getSession(HistoricActivityInstanceManager.class);
  }
  
  /**
   * Gets the historic task instance manager.
   *
   * @return the historic task instance manager
   */
  protected HistoricTaskInstanceManager getHistoricTaskInstanceManager() {
    return getSession(HistoricTaskInstanceManager.class);
  }
  
  /**
   * Gets the user manager.
   *
   * @return the user manager
   */
  protected UserManager getUserManager() {
    return getSession(UserManager.class);
  }
  
  /**
   * Gets the group manager.
   *
   * @return the group manager
   */
  protected GroupManager getGroupManager() {
    return getSession(GroupManager.class);
  }
  
  /**
   * Gets the identity info manager.
   *
   * @return the identity info manager
   */
  protected IdentityInfoManager getIdentityInfoManager() {
    return getSession(IdentityInfoManager.class);
  }
  
  /**
   * Gets the membership manager.
   *
   * @return the membership manager
   */
  protected MembershipManager getMembershipManager() {
    return getSession(MembershipManager.class);
  }
  
  /**
   * Gets the attachment manager.
   *
   * @return the attachment manager
   */
  protected AttachmentManager getAttachmentManager() {
    return getSession(AttachmentManager.class);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Session#close()
   */
  public void close() {
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Session#flush()
   */
  public void flush() {
  }
}
