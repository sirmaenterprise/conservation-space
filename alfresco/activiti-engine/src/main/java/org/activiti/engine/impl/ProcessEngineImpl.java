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

import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TransactionContextFactory;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

// TODO: Auto-generated Javadoc
/**
 * The Class ProcessEngineImpl.
 *
 * @author Tom Baeyens
 */
public class ProcessEngineImpl implements ProcessEngine {

  /** The log. */
  private static Logger log = Logger.getLogger(ProcessEngineImpl.class.getName());

  /** The name. */
  protected String name;
  
  /** The repository service. */
  protected RepositoryService repositoryService;
  
  /** The runtime service. */
  protected RuntimeService runtimeService;
  
  /** The historic data service. */
  protected HistoryService historicDataService;
  
  /** The identity service. */
  protected IdentityService identityService;
  
  /** The task service. */
  protected TaskService taskService;
  
  /** The form service. */
  protected FormService formService;
  
  /** The management service. */
  protected ManagementService managementService;
  
  /** The database schema update. */
  protected String databaseSchemaUpdate;
  
  /** The job executor. */
  protected JobExecutor jobExecutor;
  
  /** The command executor. */
  protected CommandExecutor commandExecutor;
  
  /** The session factories. */
  protected Map<Class<?>, SessionFactory> sessionFactories;
  
  /** The expression manager. */
  protected ExpressionManager expressionManager;
  
  /** The history level. */
  protected int historyLevel;
  
  /** The transaction context factory. */
  protected TransactionContextFactory transactionContextFactory;
  
  /** The process engine configuration. */
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  /**
   * Instantiates a new process engine impl.
   *
   * @param processEngineConfiguration the process engine configuration
   */
  public ProcessEngineImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
    this.name = processEngineConfiguration.getProcessEngineName();
    this.repositoryService = processEngineConfiguration.getRepositoryService();
    this.runtimeService = processEngineConfiguration.getRuntimeService();
    this.historicDataService = processEngineConfiguration.getHistoryService();
    this.identityService = processEngineConfiguration.getIdentityService();
    this.taskService = processEngineConfiguration.getTaskService();
    this.formService = processEngineConfiguration.getFormService();
    this.managementService = processEngineConfiguration.getManagementService();
    this.databaseSchemaUpdate = processEngineConfiguration.getDatabaseSchemaUpdate();
    this.jobExecutor = processEngineConfiguration.getJobExecutor();
    this.commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    this.sessionFactories = processEngineConfiguration.getSessionFactories();
    this.historyLevel = processEngineConfiguration.getHistoryLevel();
    this.transactionContextFactory = processEngineConfiguration.getTransactionContextFactory();
    
    commandExecutor.execute(new SchemaOperationsProcessEngineBuild());

    if (name == null) {
      log.info("default activiti ProcessEngine created");
    } else {
      log.info("ProcessEngine " + name + " created");
    }
    
    ProcessEngines.registerProcessEngine(this);

    if ((jobExecutor != null) && (jobExecutor.isAutoActivate())) {
      jobExecutor.start();
    }
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#close()
   */
  public void close() {
    ProcessEngines.unregister(this);
    if ((jobExecutor != null) && (jobExecutor.isActive())) {
      jobExecutor.shutdown();
    }

    commandExecutor.execute(new SchemaOperationProcessEngineClose());
  }

  /**
   * Gets the db sql session factory.
   *
   * @return the db sql session factory
   */
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return (DbSqlSessionFactory) sessionFactories.get(DbSqlSession.class);
  }
  
  // getters and setters //////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#getName()
   */
  public String getName() {
    return name;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#getIdentityService()
   */
  public IdentityService getIdentityService() {
    return identityService;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#getManagementService()
   */
  public ManagementService getManagementService() {
    return managementService;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#getTaskService()
   */
  public TaskService getTaskService() {
    return taskService;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#getHistoryService()
   */
  public HistoryService getHistoryService() {
    return historicDataService;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#getRuntimeService()
   */
  public RuntimeService getRuntimeService() {
    return runtimeService;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#getRepositoryService()
   */
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngine#getFormService()
   */
  public FormService getFormService() {
    return formService;
  }

  /**
   * Gets the process engine configuration.
   *
   * @return the process engine configuration
   */
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }
}
