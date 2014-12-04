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
package org.activiti.engine.impl.interceptor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.persistence.entity.AttachmentManager;
import org.activiti.engine.impl.persistence.entity.CommentManager;
import org.activiti.engine.impl.persistence.entity.DeploymentManager;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionManager;
import org.activiti.engine.impl.persistence.entity.ExecutionManager;
import org.activiti.engine.impl.persistence.entity.GroupManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceManager;
import org.activiti.engine.impl.persistence.entity.HistoricDetailManager;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceManager;
import org.activiti.engine.impl.persistence.entity.IdentityInfoManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkManager;
import org.activiti.engine.impl.persistence.entity.JobManager;
import org.activiti.engine.impl.persistence.entity.MembershipManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.activiti.engine.impl.persistence.entity.PropertyManager;
import org.activiti.engine.impl.persistence.entity.ResourceManager;
import org.activiti.engine.impl.persistence.entity.TableDataManager;
import org.activiti.engine.impl.persistence.entity.TaskManager;
import org.activiti.engine.impl.persistence.entity.UserManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceManager;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;

// TODO: Auto-generated Javadoc
/**
 * The Class CommandContext.
 *
 * @author Tom Baeyens
 * @author Agim Emruli
 */
public class CommandContext {

  /** The log. */
  private static Logger log = Logger.getLogger(CommandContext.class.getName());

  /** The command. */
  protected Command< ? > command;
  
  /** The transaction context. */
  protected TransactionContext transactionContext;
  
  /** The session factories. */
  protected Map<Class< ? >, SessionFactory> sessionFactories;
  
  /** The sessions. */
  protected Map<Class< ? >, Session> sessions = new HashMap<Class< ? >, Session>();
  
  /** The exception. */
  protected Throwable exception = null;
  
  /** The next operations. */
  protected LinkedList<AtomicOperation> nextOperations = new LinkedList<AtomicOperation>();
  
  /** The process engine configuration. */
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  
  /** The failed job command factory. */
  protected FailedJobCommandFactory failedJobCommandFactory;

  
  /**
   * Perform operation.
   *
   * @param executionOperation the execution operation
   * @param execution the execution
   */
  public void performOperation(AtomicOperation executionOperation, InterpretableExecution execution) {
    nextOperations.add(executionOperation);
    if (nextOperations.size()==1) {
      try {
        Context.setExecutionContext(execution);
        while (!nextOperations.isEmpty()) {
          AtomicOperation currentOperation = nextOperations.removeFirst();
          if (log.isLoggable(Level.FINEST)) {
            log.finest("AtomicOperation: " + currentOperation + " on " + this);
          }
          currentOperation.execute(execution);
        }
      } finally {
        Context.removeExecutionContext();
      }
    }
  }

  /**
   * Instantiates a new command context.
   *
   * @param command the command
   * @param processEngineConfiguration the process engine configuration
   */
  public CommandContext(Command<?> command, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.command = command;
    this.processEngineConfiguration = processEngineConfiguration;
    this.failedJobCommandFactory = processEngineConfiguration.getFailedJobCommandFactory();
    sessionFactories = processEngineConfiguration.getSessionFactories();
    this.transactionContext = processEngineConfiguration
      .getTransactionContextFactory()
      .openTransactionContext(this);
  }

  /**
   * Close.
   */
  public void close() {
    // the intention of this method is that all resources are closed properly,
    // even
    // if exceptions occur in close or flush methods of the sessions or the
    // transaction context.

    try {
      try {
        try {

          if (exception == null) {
            flushSessions();
          }

        } catch (Throwable exception) {
          exception(exception);
        } finally {

          try {
            if (exception == null) {
              transactionContext.commit();
            }
          } catch (Throwable exception) {
            exception(exception);
          }

          if (exception != null) {
            Level loggingLevel = Level.SEVERE;
            if (exception instanceof ActivitiTaskAlreadyClaimedException) {
              loggingLevel = Level.INFO; // reduce log level, because this is not really a technical exception
            }
            log.log(loggingLevel, "Error while closing command context", exception);
            transactionContext.rollback();
          }
        }
      } catch (Throwable exception) {
        exception(exception);
      } finally {
        closeSessions();

      }
    } catch (Throwable exception) {
      exception(exception);
    } 

    // rethrow the original exception if there was one
    if (exception != null) {
      if (exception instanceof Error) {
        throw (Error) exception;
      } else if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new ActivitiException("exception while executing command " + command, exception);
      }
    }
  }
 
  /**
   * Flush sessions.
   */
  protected void flushSessions() {
    for (Session session : sessions.values()) {
      session.flush();
    }
  }

  /**
   * Close sessions.
   */
  protected void closeSessions() {
    for (Session session : sessions.values()) {
      try {
        session.close();
      } catch (Throwable exception) {
        exception(exception);
      }
    }
  }

  /**
   * Exception.
   *
   * @param exception the exception
   */
  public void exception(Throwable exception) {
    if (this.exception == null) {
      this.exception = exception;
    } else {
      log.log(Level.SEVERE, "masked exception in command context. for root cause, see below as it will be rethrown later.", exception);
    }
  }

  /**
   * Gets the session.
   *
   * @param <T> the generic type
   * @param sessionClass the session class
   * @return the session
   */
  @SuppressWarnings({"unchecked"})
  public <T> T getSession(Class<T> sessionClass) {
    Session session = sessions.get(sessionClass);
    if (session == null) {
      SessionFactory sessionFactory = sessionFactories.get(sessionClass);
      if (sessionFactory==null) {
        throw new ActivitiException("no session factory configured for "+sessionClass.getName());
      }
      session = sessionFactory.openSession();
      sessions.put(sessionClass, session);
    }

    return (T) session;
  }
  
  /**
   * Gets the db sql session.
   *
   * @return the db sql session
   */
  public DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }
  
  /**
   * Gets the deployment manager.
   *
   * @return the deployment manager
   */
  public DeploymentManager getDeploymentManager() {
    return getSession(DeploymentManager.class);
  }

  /**
   * Gets the resource manager.
   *
   * @return the resource manager
   */
  public ResourceManager getResourceManager() {
    return getSession(ResourceManager.class);
  }
  
  /**
   * Gets the process definition manager.
   *
   * @return the process definition manager
   */
  public ProcessDefinitionManager getProcessDefinitionManager() {
    return getSession(ProcessDefinitionManager.class);
  }

  /**
   * Gets the execution manager.
   *
   * @return the execution manager
   */
  public ExecutionManager getExecutionManager() {
    return getSession(ExecutionManager.class);
  }

  /**
   * Gets the task manager.
   *
   * @return the task manager
   */
  public TaskManager getTaskManager() {
    return getSession(TaskManager.class);
  }

  /**
   * Gets the identity link manager.
   *
   * @return the identity link manager
   */
  public IdentityLinkManager getIdentityLinkManager() {
    return getSession(IdentityLinkManager.class);
  }

  /**
   * Gets the variable instance manager.
   *
   * @return the variable instance manager
   */
  public VariableInstanceManager getVariableInstanceManager() {
    return getSession(VariableInstanceManager.class);
  }

  /**
   * Gets the historic process instance manager.
   *
   * @return the historic process instance manager
   */
  public HistoricProcessInstanceManager getHistoricProcessInstanceManager() {
    return getSession(HistoricProcessInstanceManager.class);
  }

  /**
   * Gets the historic detail manager.
   *
   * @return the historic detail manager
   */
  public HistoricDetailManager getHistoricDetailManager() {
    return getSession(HistoricDetailManager.class);
  }

  /**
   * Gets the historic activity instance manager.
   *
   * @return the historic activity instance manager
   */
  public HistoricActivityInstanceManager getHistoricActivityInstanceManager() {
    return getSession(HistoricActivityInstanceManager.class);
  }
  
  /**
   * Gets the historic task instance manager.
   *
   * @return the historic task instance manager
   */
  public HistoricTaskInstanceManager getHistoricTaskInstanceManager() {
    return getSession(HistoricTaskInstanceManager.class);
  }
  
  /**
   * Gets the job manager.
   *
   * @return the job manager
   */
  public JobManager getJobManager() {
    return getSession(JobManager.class);
  }

  /**
   * Gets the user manager.
   *
   * @return the user manager
   */
  public UserManager getUserManager() {
    return getSession(UserManager.class);
  }

  /**
   * Gets the group manager.
   *
   * @return the group manager
   */
  public GroupManager getGroupManager() {
    return getSession(GroupManager.class);
  }

  /**
   * Gets the identity info manager.
   *
   * @return the identity info manager
   */
  public IdentityInfoManager getIdentityInfoManager() {
    return getSession(IdentityInfoManager.class);
  }

  /**
   * Gets the membership manager.
   *
   * @return the membership manager
   */
  public MembershipManager getMembershipManager() {
    return getSession(MembershipManager.class);
  }
  
  /**
   * Gets the attachment manager.
   *
   * @return the attachment manager
   */
  public AttachmentManager getAttachmentManager() {
    return getSession(AttachmentManager.class);
  }

  /**
   * Gets the table data manager.
   *
   * @return the table data manager
   */
  public TableDataManager getTableDataManager() {
    return getSession(TableDataManager.class);
  }

  /**
   * Gets the comment manager.
   *
   * @return the comment manager
   */
  public CommentManager getCommentManager() {
    return getSession(CommentManager.class);
  }
  
  /**
   * Gets the event subscription manager.
   *
   * @return the event subscription manager
   */
  public EventSubscriptionManager getEventSubscriptionManager() {
    return getSession(EventSubscriptionManager.class);
  }

  /**
   * Gets the session factories.
   *
   * @return the session factories
   */
  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  /**
   * Gets the property manager.
   *
   * @return the property manager
   */
  public PropertyManager getPropertyManager() {
    return getSession(PropertyManager.class);
  }

  // getters and setters //////////////////////////////////////////////////////

  /**
   * Gets the transaction context.
   *
   * @return the transaction context
   */
  public TransactionContext getTransactionContext() {
    return transactionContext;
  }
  
  /**
   * Gets the command.
   *
   * @return the command
   */
  public Command< ? > getCommand() {
    return command;
  }
  
  /**
   * Gets the sessions.
   *
   * @return the sessions
   */
  public Map<Class< ? >, Session> getSessions() {
    return sessions;
  }
  
  /**
   * Gets the exception.
   *
   * @return the exception
   */
  public Throwable getException() {
    return exception;
  }
  
  /**
   * Gets the failed job command factory.
   *
   * @return the failed job command factory
   */
  public FailedJobCommandFactory getFailedJobCommandFactory() {
    return failedJobCommandFactory;
  }
}
