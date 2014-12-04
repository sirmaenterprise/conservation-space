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

package org.activiti.engine.impl.cfg;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.FormServiceImpl;
import org.activiti.engine.impl.HistoryServiceImpl;
import org.activiti.engine.impl.IdentityServiceImpl;
import org.activiti.engine.impl.ManagementServiceImpl;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.webservice.MessageInstance;
import org.activiti.engine.impl.calendar.BusinessCalendarManager;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.calendar.MapBusinessCalendarManager;
import org.activiti.engine.impl.cfg.standalone.StandaloneMybatisTransactionContextFactory;
import org.activiti.engine.impl.db.DbIdGenerator;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.delegate.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.event.CompensationEventHandler;
import org.activiti.engine.impl.event.EventHandler;
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.event.SignalEventHandler;
import org.activiti.engine.impl.form.AbstractFormType;
import org.activiti.engine.impl.form.BooleanFormType;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.FormEngine;
import org.activiti.engine.impl.form.FormTypes;
import org.activiti.engine.impl.form.JuelFormEngine;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.impl.history.handler.HistoryParseListener;
import org.activiti.engine.impl.interceptor.CommandContextFactory;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.CommandExecutorImpl;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.CallerRunsRejectedJobsHandler;
import org.activiti.engine.impl.jobexecutor.DefaultFailedJobCommandFactory;
import org.activiti.engine.impl.jobexecutor.DefaultJobExecutor;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.jobexecutor.RejectedJobsHandler;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.mail.MailScanner;
import org.activiti.engine.impl.persistence.GenericManagerFactory;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
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
import org.activiti.engine.impl.scripting.BeansResolverFactory;
import org.activiti.engine.impl.scripting.ResolverFactory;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.scripting.VariableScopeResolverFactory;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.BooleanType;
import org.activiti.engine.impl.variable.ByteArrayType;
import org.activiti.engine.impl.variable.CustomObjectType;
import org.activiti.engine.impl.variable.DateType;
import org.activiti.engine.impl.variable.DefaultVariableTypes;
import org.activiti.engine.impl.variable.DoubleType;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.impl.variable.IntegerType;
import org.activiti.engine.impl.variable.JPAEntityVariableType;
import org.activiti.engine.impl.variable.LongType;
import org.activiti.engine.impl.variable.NullType;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.ShortType;
import org.activiti.engine.impl.variable.StringType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.JdbcType;


// TODO: Auto-generated Javadoc
/**
 * The Class ProcessEngineConfigurationImpl.
 *
 * @author Tom Baeyens
 */
public abstract class ProcessEngineConfigurationImpl extends ProcessEngineConfiguration {  

  /** The log. */
  private static Logger log = Logger.getLogger(ProcessEngineConfigurationImpl.class.getName());
  
  /** The Constant DB_SCHEMA_UPDATE_CREATE. */
  public static final String DB_SCHEMA_UPDATE_CREATE = "create";
  
  /** The Constant DB_SCHEMA_UPDATE_DROP_CREATE. */
  public static final String DB_SCHEMA_UPDATE_DROP_CREATE = "drop-create";

  /** The Constant HISTORYLEVEL_NONE. */
  public static final int HISTORYLEVEL_NONE = 0;
  
  /** The Constant HISTORYLEVEL_ACTIVITY. */
  public static final int HISTORYLEVEL_ACTIVITY = 1;
  
  /** The Constant HISTORYLEVEL_AUDIT. */
  public static final int HISTORYLEVEL_AUDIT = 2;
  
  /** The Constant HISTORYLEVEL_FULL. */
  public static final int HISTORYLEVEL_FULL = 3;

  /** The Constant DEFAULT_WS_SYNC_FACTORY. */
  public static final String DEFAULT_WS_SYNC_FACTORY = "org.activiti.engine.impl.webservice.CxfWebServiceClientFactory";
  
  /** The Constant DEFAULT_MYBATIS_MAPPING_FILE. */
  public static final String DEFAULT_MYBATIS_MAPPING_FILE = "org/activiti/db/mapping/mappings.xml";

  // SERVICES /////////////////////////////////////////////////////////////////

  /** The repository service. */
  protected RepositoryService repositoryService = new RepositoryServiceImpl();
  
  /** The runtime service. */
  protected RuntimeService runtimeService = new RuntimeServiceImpl();
  
  /** The history service. */
  protected HistoryService historyService = new HistoryServiceImpl();
  
  /** The identity service. */
  protected IdentityService identityService = new IdentityServiceImpl();
  
  /** The task service. */
  protected TaskService taskService = new TaskServiceImpl();
  
  /** The form service. */
  protected FormService formService = new FormServiceImpl();
  
  /** The management service. */
  protected ManagementService managementService = new ManagementServiceImpl();
  
  // COMMAND EXECUTORS ////////////////////////////////////////////////////////
  
  // Command executor and interceptor stack
  /** the configurable list which will be {@link #initInterceptorChain(java.util.List) processed} to build the {@link #commandExecutorTxRequired} */
  protected List<CommandInterceptor> customPreCommandInterceptorsTxRequired;
  
  /** The custom post command interceptors tx required. */
  protected List<CommandInterceptor> customPostCommandInterceptorsTxRequired;
  
  /** The command interceptors tx required. */
  protected List<CommandInterceptor> commandInterceptorsTxRequired;

  /** this will be initialized during the configurationComplete(). */
  protected CommandExecutor commandExecutorTxRequired;
  
  /** the configurable list which will be {@link #initInterceptorChain(List) processed} to build the {@link #commandExecutorTxRequiresNew}. */
  protected List<CommandInterceptor> customPreCommandInterceptorsTxRequiresNew;
  
  /** The custom post command interceptors tx requires new. */
  protected List<CommandInterceptor> customPostCommandInterceptorsTxRequiresNew;

  /** The command interceptors tx requires new. */
  protected List<CommandInterceptor> commandInterceptorsTxRequiresNew;

  /** this will be initialized during the configurationComplete(). */
  protected CommandExecutor commandExecutorTxRequiresNew;
  
  // SESSION FACTORIES ////////////////////////////////////////////////////////

  /** The custom session factories. */
  protected List<SessionFactory> customSessionFactories;
  
  /** The db sql session factory. */
  protected DbSqlSessionFactory dbSqlSessionFactory;
  
  /** The session factories. */
  protected Map<Class<?>, SessionFactory> sessionFactories;
  
  // DEPLOYERS ////////////////////////////////////////////////////////////////

  /** The custom pre deployers. */
  protected List<Deployer> customPreDeployers;
  
  /** The custom post deployers. */
  protected List<Deployer> customPostDeployers;
  
  /** The deployers. */
  protected List<Deployer> deployers;
  
  /** The deployment cache. */
  protected DeploymentCache deploymentCache;

  // JOB EXECUTOR /////////////////////////////////////////////////////////////
  
  /** The custom job handlers. */
  protected List<JobHandler> customJobHandlers;
  
  /** The job handlers. */
  protected Map<String, JobHandler> jobHandlers;
  
  /** The job executor. */
  protected JobExecutor jobExecutor;

  // MAIL SCANNER /////////////////////////////////////////////////////////////
  
  /** The mail scanner. */
  protected MailScanner mailScanner;
  
  // MYBATIS SQL SESSION FACTORY //////////////////////////////////////////////
  
  /** The sql session factory. */
  protected SqlSessionFactory sqlSessionFactory;
  
  /** The transaction factory. */
  protected TransactionFactory transactionFactory;


  // ID GENERATOR /////////////////////////////////////////////////////////////
  /** The id generator. */
  protected IdGenerator idGenerator;
  
  /** The id generator data source. */
  protected DataSource idGeneratorDataSource;
  
  /** The id generator data source jndi name. */
  protected String idGeneratorDataSourceJndiName;

  // OTHER ////////////////////////////////////////////////////////////////////
  /** The custom form engines. */
  protected List<FormEngine> customFormEngines;
  
  /** The form engines. */
  protected Map<String, FormEngine> formEngines;

  /** The custom form types. */
  protected List<AbstractFormType> customFormTypes;
  
  /** The form types. */
  protected FormTypes formTypes;

  /** The custom pre variable types. */
  protected List<VariableType> customPreVariableTypes;
  
  /** The custom post variable types. */
  protected List<VariableType> customPostVariableTypes;
  
  /** The variable types. */
  protected VariableTypes variableTypes;
  
  /** The expression manager. */
  protected ExpressionManager expressionManager;
  
  /** The custom scripting engine classes. */
  protected List<String> customScriptingEngineClasses;
  
  /** The scripting engines. */
  protected ScriptingEngines scriptingEngines;
  
  /** The resolver factories. */
  protected List<ResolverFactory> resolverFactories;
  
  /** The business calendar manager. */
  protected BusinessCalendarManager businessCalendarManager;

  /** The ws sync factory class name. */
  protected String wsSyncFactoryClassName = DEFAULT_WS_SYNC_FACTORY;

  /** The command context factory. */
  protected CommandContextFactory commandContextFactory;
  
  /** The transaction context factory. */
  protected TransactionContextFactory transactionContextFactory;
  
  /** The history level. */
  protected int historyLevel;
  
  /** The pre parse listeners. */
  protected List<BpmnParseListener> preParseListeners;
  
  /** The post parse listeners. */
  protected List<BpmnParseListener> postParseListeners;

  /** The beans. */
  protected Map<Object, Object> beans;

  /** The is db identity used. */
  protected boolean isDbIdentityUsed = true;
  
  /** The is db history used. */
  protected boolean isDbHistoryUsed = true;
  
  /** The delegate interceptor. */
  protected DelegateInterceptor delegateInterceptor;

  /** The actual command executor. */
  protected CommandInterceptor actualCommandExecutor;
  
  /** The custom rejected jobs handler. */
  protected RejectedJobsHandler customRejectedJobsHandler;
  
  /** The event handlers. */
  protected Map<String, EventHandler> eventHandlers;
  
  /** The custom event handlers. */
  protected List<EventHandler> customEventHandlers;

  /** The failed job command factory. */
  protected FailedJobCommandFactory failedJobCommandFactory;
  
  /** The database table prefix. */
  protected String databaseTablePrefix = "";
  
  /**
   * In some situations you want to set the schema to use for table checks / generation if the database metadata
   * doesn't return that correctly, see https://jira.codehaus.org/browse/ACT-1220,
   * https://jira.codehaus.org/browse/ACT-1062
   */
  protected String databaseSchema = null;
  
  /** The is create diagram on deploy. */
  protected boolean isCreateDiagramOnDeploy = true;
  
  // buildProcessEngine ///////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#buildProcessEngine()
   */
  public ProcessEngine buildProcessEngine() {
    init();
    return new ProcessEngineImpl(this);
  }
  
  // init /////////////////////////////////////////////////////////////////////
  
  /**
   * Inits the.
   */
  protected void init() {
    initHistoryLevel();
    initExpressionManager();
    initVariableTypes();
    initBeans();
    initFormEngines();
    initFormTypes();
    initScriptingEngines();
    initBusinessCalendarManager();
    initCommandContextFactory();
    initTransactionContextFactory();
    initCommandExecutors();
    initServices();
    initIdGenerator();
    initDeployers();
    initJobExecutor();
    initMailScanner();
    initDataSource();
    initTransactionFactory();
    initSqlSessionFactory();
    initSessionFactories();
    initJpa();
    initDelegateInterceptor();
    initEventHandlers();
    initFailedJobCommandFactory();
  }

  // failedJobCommandFactory ////////////////////////////////////////////////////////
  
  /**
   * Inits the failed job command factory.
   */
  protected void initFailedJobCommandFactory() {
    if (failedJobCommandFactory == null) {
      failedJobCommandFactory = new DefaultFailedJobCommandFactory();
    }
  }

  // command executors ////////////////////////////////////////////////////////
  
  /**
   * Gets the default command interceptors tx required.
   *
   * @return the default command interceptors tx required
   */
  protected abstract Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired();
  
  /**
   * Gets the default command interceptors tx requires new.
   *
   * @return the default command interceptors tx requires new
   */
  protected abstract Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew();
  
  /**
   * Inits the command executors.
   */
  protected void initCommandExecutors() {
    initActualCommandExecutor();
    initCommandInterceptorsTxRequired();
    initCommandExecutorTxRequired();
    initCommandInterceptorsTxRequiresNew();
    initCommandExecutorTxRequiresNew();
  }

  /**
   * Inits the actual command executor.
   */
  protected void initActualCommandExecutor() {
    actualCommandExecutor = new CommandExecutorImpl();
  }

  /**
   * Inits the command interceptors tx required.
   */
  protected void initCommandInterceptorsTxRequired() {
    if (commandInterceptorsTxRequired==null) {
      if (customPreCommandInterceptorsTxRequired!=null) {
        commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>(customPreCommandInterceptorsTxRequired);
      } else {
        commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
      }
      commandInterceptorsTxRequired.addAll(getDefaultCommandInterceptorsTxRequired());
      if (customPostCommandInterceptorsTxRequired!=null) {
        commandInterceptorsTxRequired.addAll(customPostCommandInterceptorsTxRequired);
      }
      commandInterceptorsTxRequired.add(actualCommandExecutor);
    }
  }

  /**
   * Inits the command interceptors tx requires new.
   */
  protected void initCommandInterceptorsTxRequiresNew() {
    if (commandInterceptorsTxRequiresNew==null) {
      if (customPreCommandInterceptorsTxRequiresNew!=null) {
        commandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>(customPreCommandInterceptorsTxRequiresNew);
      } else {
        commandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
      }
      commandInterceptorsTxRequiresNew.addAll(getDefaultCommandInterceptorsTxRequiresNew());
      if (customPostCommandInterceptorsTxRequiresNew!=null) {
        commandInterceptorsTxRequiresNew.addAll(customPostCommandInterceptorsTxRequiresNew);
      }
      commandInterceptorsTxRequiresNew.add(actualCommandExecutor);
    }
  }

  /**
   * Inits the command executor tx required.
   */
  protected void initCommandExecutorTxRequired() {
    if (commandExecutorTxRequired==null) {
      commandExecutorTxRequired = initInterceptorChain(commandInterceptorsTxRequired);
    }
  }

  /**
   * Inits the command executor tx requires new.
   */
  protected void initCommandExecutorTxRequiresNew() {
    if (commandExecutorTxRequiresNew==null) {
      commandExecutorTxRequiresNew = initInterceptorChain(commandInterceptorsTxRequiresNew);
    }
  }

  /**
   * Inits the interceptor chain.
   *
   * @param chain the chain
   * @return the command interceptor
   */
  protected CommandInterceptor initInterceptorChain(List<CommandInterceptor> chain) {
    if (chain==null || chain.isEmpty()) {
      throw new ActivitiException("invalid command interceptor chain configuration: "+chain);
    }
    for (int i = 0; i < chain.size()-1; i++) {
      chain.get(i).setNext( chain.get(i+1) );
    }
    return chain.get(0);
  }
  
  // services /////////////////////////////////////////////////////////////////
  
  /**
   * Inits the services.
   */
  protected void initServices() {
    initService(repositoryService);
    initService(runtimeService);
    initService(historyService);
    initService(identityService);
    initService(taskService);
    initService(formService);
    initService(managementService);
  }

  /**
   * Inits the service.
   *
   * @param service the service
   */
  protected void initService(Object service) {
    if (service instanceof ServiceImpl) {
      ((ServiceImpl)service).setCommandExecutor(commandExecutorTxRequired);
    }
  }
  
  // DataSource ///////////////////////////////////////////////////////////////
  
  /**
   * Inits the data source.
   */
  protected void initDataSource() {
    if (dataSource==null) {
      if (dataSourceJndiName!=null) {
        try {
          dataSource = (DataSource) new InitialContext().lookup(dataSourceJndiName);
        } catch (Exception e) {
          throw new ActivitiException("couldn't lookup datasource from "+dataSourceJndiName+": "+e.getMessage(), e);
        }
        
      } else if (jdbcUrl!=null) {
        if ( (jdbcDriver==null) || (jdbcUrl==null) || (jdbcUsername==null) ) {
          throw new ActivitiException("DataSource or JDBC properties have to be specified in a process engine configuration");
        }
        
        log.fine("initializing datasource to db: "+jdbcUrl);
        
        PooledDataSource pooledDataSource = 
          new PooledDataSource(ReflectUtil.getClassLoader(), jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword );
        
        if (jdbcMaxActiveConnections > 0) {
          pooledDataSource.setPoolMaximumActiveConnections(jdbcMaxActiveConnections);
        }
        if (jdbcMaxIdleConnections > 0) {
          pooledDataSource.setPoolMaximumIdleConnections(jdbcMaxIdleConnections);
        }
        if (jdbcMaxCheckoutTime > 0) {
          pooledDataSource.setPoolMaximumCheckoutTime(jdbcMaxCheckoutTime);
        }
        if (jdbcMaxWaitTime > 0) {
          pooledDataSource.setPoolTimeToWait(jdbcMaxWaitTime);
        }
        if (jdbcPingEnabled == true) {
          pooledDataSource.setPoolPingEnabled(true);
          if (jdbcPingQuery != null) {
            pooledDataSource.setPoolPingQuery(jdbcPingQuery);
          }
          pooledDataSource.setPoolPingConnectionsNotUsedFor(jdbcPingConnectionNotUsedFor);
        }        
        dataSource = pooledDataSource;
      }
      
      if (dataSource instanceof PooledDataSource) {
        // ACT-233: connection pool of Ibatis is not properely initialized if this is not called!
        ((PooledDataSource)dataSource).forceCloseAll();
      }
    }

    if (databaseType == null) {
      initDatabaseType();
    }
  }
  
  /** The database type mappings. */
  protected static Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();
  
  /**
   * Gets the default database type mappings.
   *
   * @return the default database type mappings
   */
  protected static Properties getDefaultDatabaseTypeMappings() {
    Properties databaseTypeMappings = new Properties();
    databaseTypeMappings.setProperty("H2","h2");
    databaseTypeMappings.setProperty("MySQL","mysql");
    databaseTypeMappings.setProperty("Oracle","oracle");
    databaseTypeMappings.setProperty("PostgreSQL","postgres");
    databaseTypeMappings.setProperty("Microsoft SQL Server","mssql");
    databaseTypeMappings.setProperty("DB2","db2");
    databaseTypeMappings.setProperty("DB2","db2");
    databaseTypeMappings.setProperty("DB2/NT","db2");
    databaseTypeMappings.setProperty("DB2/NT64","db2");
    databaseTypeMappings.setProperty("DB2 UDP","db2");
    databaseTypeMappings.setProperty("DB2/LINUX","db2");
    databaseTypeMappings.setProperty("DB2/LINUX390","db2");
    databaseTypeMappings.setProperty("DB2/LINUXX8664","db2");
    databaseTypeMappings.setProperty("DB2/LINUXZ64","db2");
    databaseTypeMappings.setProperty("DB2/400 SQL","db2");
    databaseTypeMappings.setProperty("DB2/6000","db2");
    databaseTypeMappings.setProperty("DB2 UDB iSeries","db2");
    databaseTypeMappings.setProperty("DB2/AIX64","db2");
    databaseTypeMappings.setProperty("DB2/HPUX","db2");
    databaseTypeMappings.setProperty("DB2/HP64","db2");
    databaseTypeMappings.setProperty("DB2/SUN","db2");
    databaseTypeMappings.setProperty("DB2/SUN64","db2");
    databaseTypeMappings.setProperty("DB2/PTX","db2");
    databaseTypeMappings.setProperty("DB2/2","db2");
    return databaseTypeMappings;
  }

  /**
   * Inits the database type.
   */
  public void initDatabaseType() {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      String databaseProductName = databaseMetaData.getDatabaseProductName();
      log.fine("database product name: '"+databaseProductName+"'");
      databaseType = databaseTypeMappings.getProperty(databaseProductName);
      if (databaseType==null) {
        throw new ActivitiException("couldn't deduct database type from database product name '"+databaseProductName+"'");
      }
      log.fine("using database type: "+databaseType);

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection!=null) {
          connection.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  // myBatis SqlSessionFactory ////////////////////////////////////////////////
  
  /**
   * Inits the transaction factory.
   */
  protected void initTransactionFactory() {
    if (transactionFactory==null) {
      if (transactionsExternallyManaged) {
        transactionFactory = new ManagedTransactionFactory();
      } else {
        transactionFactory = new JdbcTransactionFactory();
      }
    }
  }

  /**
   * Inits the sql session factory.
   */
  protected void initSqlSessionFactory() {
    if (sqlSessionFactory==null) {
      InputStream inputStream = null;
      try {
        inputStream = getMyBatisXmlConfigurationSteam();

        // update the jdbc parameters to the configured ones...
        Environment environment = new Environment("default", transactionFactory, dataSource);
        Reader reader = new InputStreamReader(inputStream);
        Properties properties = new Properties();
        properties.put("prefix", databaseTablePrefix);
        if(databaseType != null) {
          properties.put("limitBefore" , DbSqlSessionFactory.databaseSpecificLimitBeforeStatements.get(databaseType));
          properties.put("limitAfter" , DbSqlSessionFactory.databaseSpecificLimitAfterStatements.get(databaseType));
        }
        XMLConfigBuilder parser = new XMLConfigBuilder(reader,"", properties);
        Configuration configuration = parser.getConfiguration();
        configuration.setEnvironment(environment);
        configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
        configuration = parser.parse();

        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

      } catch (Exception e) {
        throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
      } finally {
        IoUtil.closeSilently(inputStream);
      }
    }
  }
  
  /**
   * Gets the my batis xml configuration steam.
   *
   * @return the my batis xml configuration steam
   */
  protected InputStream getMyBatisXmlConfigurationSteam() {
    return ReflectUtil.getResourceAsStream(DEFAULT_MYBATIS_MAPPING_FILE);
  }

  // session factories ////////////////////////////////////////////////////////
  
  /**
   * Inits the session factories.
   */
  protected void initSessionFactories() {
    if (sessionFactories==null) {
      sessionFactories = new HashMap<Class<?>, SessionFactory>();

      dbSqlSessionFactory = new DbSqlSessionFactory();
      dbSqlSessionFactory.setDatabaseType(databaseType);
      dbSqlSessionFactory.setIdGenerator(idGenerator);
      dbSqlSessionFactory.setSqlSessionFactory(sqlSessionFactory);
      dbSqlSessionFactory.setDbIdentityUsed(isDbIdentityUsed);
      dbSqlSessionFactory.setDbHistoryUsed(isDbHistoryUsed);
      dbSqlSessionFactory.setDatabaseTablePrefix(databaseTablePrefix);
      dbSqlSessionFactory.setDatabaseSchema(databaseSchema);
      addSessionFactory(dbSqlSessionFactory);
      
      addSessionFactory(new GenericManagerFactory(AttachmentManager.class));
      addSessionFactory(new GenericManagerFactory(CommentManager.class));
      addSessionFactory(new GenericManagerFactory(DeploymentManager.class));
      addSessionFactory(new GenericManagerFactory(ExecutionManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricActivityInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricDetailManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricProcessInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricTaskInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(IdentityInfoManager.class));
      addSessionFactory(new GenericManagerFactory(IdentityLinkManager.class));
      addSessionFactory(new GenericManagerFactory(JobManager.class));
      addSessionFactory(new GenericManagerFactory(GroupManager.class));
      addSessionFactory(new GenericManagerFactory(MembershipManager.class));
      addSessionFactory(new GenericManagerFactory(ProcessDefinitionManager.class));
      addSessionFactory(new GenericManagerFactory(PropertyManager.class));
      addSessionFactory(new GenericManagerFactory(ResourceManager.class));
      addSessionFactory(new GenericManagerFactory(TableDataManager.class));
      addSessionFactory(new GenericManagerFactory(TaskManager.class));
      addSessionFactory(new GenericManagerFactory(UserManager.class));
      addSessionFactory(new GenericManagerFactory(VariableInstanceManager.class));
      addSessionFactory(new GenericManagerFactory(EventSubscriptionManager.class));
    }
    if (customSessionFactories!=null) {
      for (SessionFactory sessionFactory: customSessionFactories) {
        addSessionFactory(sessionFactory);
      }
    }
  }
  
  /**
   * Adds the session factory.
   *
   * @param sessionFactory the session factory
   */
  protected void addSessionFactory(SessionFactory sessionFactory) {
    sessionFactories.put(sessionFactory.getSessionType(), sessionFactory);
  }
  
  // deployers ////////////////////////////////////////////////////////////////
  
  /**
   * Inits the deployers.
   */
  protected void initDeployers() {
    if (this.deployers==null) {
      this.deployers = new ArrayList<Deployer>();
      if (customPreDeployers!=null) {
        this.deployers.addAll(customPreDeployers);
      }
      this.deployers.addAll(getDefaultDeployers());
      if (customPostDeployers!=null) {
        this.deployers.addAll(customPostDeployers);
      }
    }
    if (deploymentCache==null) {
      List<Deployer> deployers = new ArrayList<Deployer>();
      if (customPreDeployers!=null) {
        deployers.addAll(customPreDeployers);
      }
      deployers.addAll(getDefaultDeployers());
      if (customPostDeployers!=null) {
        deployers.addAll(customPostDeployers);
      }

      deploymentCache = new DeploymentCache();
      deploymentCache.setDeployers(deployers);
    }
  }

  /**
   * Gets the default deployers.
   *
   * @return the default deployers
   */
  protected Collection< ? extends Deployer> getDefaultDeployers() {
    List<Deployer> defaultDeployers = new ArrayList<Deployer>();

    BpmnDeployer bpmnDeployer = new BpmnDeployer();
    bpmnDeployer.setExpressionManager(expressionManager);
    bpmnDeployer.setIdGenerator(idGenerator);
    BpmnParser bpmnParser = new BpmnParser(expressionManager);
    
    if(preParseListeners != null) {
      bpmnParser.getParseListeners().addAll(preParseListeners);
    }
    bpmnParser.getParseListeners().addAll(getDefaultBPMNParseListeners());
    if(postParseListeners != null) {
      bpmnParser.getParseListeners().addAll(postParseListeners);
    }
    
    bpmnDeployer.setBpmnParser(bpmnParser);
    
    defaultDeployers.add(bpmnDeployer);
    return defaultDeployers;
  }
  
  /**
   * Gets the default bpmn parse listeners.
   *
   * @return the default bpmn parse listeners
   */
  protected List<BpmnParseListener> getDefaultBPMNParseListeners() {
    List<BpmnParseListener> defaultListeners = new ArrayList<BpmnParseListener>();
        if (historyLevel>=ProcessEngineConfigurationImpl.HISTORYLEVEL_ACTIVITY) {
      defaultListeners.add(new HistoryParseListener(historyLevel));
    }
    return defaultListeners;
  }

  // job executor /////////////////////////////////////////////////////////////
  
  /**
   * Inits the job executor.
   */
  protected void initJobExecutor() {
    if (jobExecutor==null) {
      jobExecutor = new DefaultJobExecutor();
    }

    jobHandlers = new HashMap<String, JobHandler>();
    TimerExecuteNestedActivityJobHandler timerExecuteNestedActivityJobHandler = new TimerExecuteNestedActivityJobHandler();
    jobHandlers.put(timerExecuteNestedActivityJobHandler.getType(), timerExecuteNestedActivityJobHandler);

    TimerCatchIntermediateEventJobHandler timerCatchIntermediateEvent = new TimerCatchIntermediateEventJobHandler();
    jobHandlers.put(timerCatchIntermediateEvent.getType(), timerCatchIntermediateEvent);

    TimerStartEventJobHandler timerStartEvent = new TimerStartEventJobHandler();
    jobHandlers.put(timerStartEvent.getType(), timerStartEvent);
    
    AsyncContinuationJobHandler asyncContinuationJobHandler = new AsyncContinuationJobHandler();
    jobHandlers.put(asyncContinuationJobHandler.getType(), asyncContinuationJobHandler);
    
    ProcessEventJobHandler processEventJobHandler = new ProcessEventJobHandler();
    jobHandlers.put(processEventJobHandler.getType(), processEventJobHandler);
    
    // if we have custom job handlers, register them
    if (getCustomJobHandlers()!=null) {
      for (JobHandler customJobHandler : getCustomJobHandlers()) {
        jobHandlers.put(customJobHandler.getType(), customJobHandler);      
      }
    }

    jobExecutor.setCommandExecutor(commandExecutorTxRequired);
    jobExecutor.setAutoActivate(jobExecutorActivate);
    
    if(jobExecutor.getRejectedJobsHandler() == null) {
      if(customRejectedJobsHandler != null) {
        jobExecutor.setRejectedJobsHandler(customRejectedJobsHandler);
      } else {
        jobExecutor.setRejectedJobsHandler(new CallerRunsRejectedJobsHandler());
      }
    }
    
  }
  
  /**
   * Inits the mail scanner.
   */
  protected void initMailScanner() {
    if (mailScanner==null) {
      mailScanner = new MailScanner();
    }
    mailScanner.setCommandExecutor(commandExecutorTxRequired);
  }
  
  // history //////////////////////////////////////////////////////////////////
  
  /**
   * Inits the history level.
   */
  public void initHistoryLevel() {
    if (HISTORY_NONE.equalsIgnoreCase(history)) {
      historyLevel = 0;
    } else if (HISTORY_ACTIVITY.equalsIgnoreCase(history)) {
      historyLevel = 1;
    } else if (HISTORY_AUDIT.equalsIgnoreCase(history)) {
      historyLevel = 2;
    } else if (HISTORY_FULL.equalsIgnoreCase(history)) {
      historyLevel = 3;
    } else {
      throw new ActivitiException("invalid history level: "+history);
    }
  }
  
  // id generator /////////////////////////////////////////////////////////////
  
  /**
   * Inits the id generator.
   */
  protected void initIdGenerator() {
    if (idGenerator==null) {
      CommandExecutor idGeneratorCommandExecutor = null;
      if (idGeneratorDataSource!=null) {
        ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(idGeneratorDataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.init();
        idGeneratorCommandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
      } else if (idGeneratorDataSourceJndiName!=null) {
        ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDataSourceJndiName(idGeneratorDataSourceJndiName);
        processEngineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.init();
        idGeneratorCommandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
      } else {
        idGeneratorCommandExecutor = commandExecutorTxRequiresNew;
      }
      
      DbIdGenerator dbIdGenerator = new DbIdGenerator();
      dbIdGenerator.setIdBlockSize(idBlockSize);
      dbIdGenerator.setCommandExecutor(idGeneratorCommandExecutor);
      idGenerator = dbIdGenerator;
    }
  }

  // OTHER ////////////////////////////////////////////////////////////////////
  
  /**
   * Inits the command context factory.
   */
  protected void initCommandContextFactory() {
    if (commandContextFactory==null) {
      commandContextFactory = new CommandContextFactory();
      commandContextFactory.setProcessEngineConfiguration(this);
    }
  }

  /**
   * Inits the transaction context factory.
   */
  protected void initTransactionContextFactory() {
    if (transactionContextFactory==null) {
      transactionContextFactory = new StandaloneMybatisTransactionContextFactory();
    }
  }

  /**
   * Inits the variable types.
   */
  protected void initVariableTypes() {
    if (variableTypes==null) {
      variableTypes = new DefaultVariableTypes();
      if (customPreVariableTypes!=null) {
        for (VariableType customVariableType: customPreVariableTypes) {
          variableTypes.addType(customVariableType);
        }
      }
      variableTypes.addType(new NullType());
      variableTypes.addType(new StringType());
      variableTypes.addType(new BooleanType());
      variableTypes.addType(new ShortType());
      variableTypes.addType(new IntegerType());
      variableTypes.addType(new LongType());
      variableTypes.addType(new DateType());
      variableTypes.addType(new DoubleType());
      variableTypes.addType(new ByteArrayType());
      variableTypes.addType(new SerializableType());
      variableTypes.addType(new CustomObjectType("item", ItemInstance.class));
      variableTypes.addType(new CustomObjectType("message", MessageInstance.class));
      if (customPostVariableTypes!=null) {
        for (VariableType customVariableType: customPostVariableTypes) {
          variableTypes.addType(customVariableType);
        }
      }
    }
  }

  /**
   * Inits the form engines.
   */
  protected void initFormEngines() {
    if (formEngines==null) {
      formEngines = new HashMap<String, FormEngine>();
      FormEngine defaultFormEngine = new JuelFormEngine();
      formEngines.put(null, defaultFormEngine); // default form engine is looked up with null
      formEngines.put(defaultFormEngine.getName(), defaultFormEngine);
    }
    if (customFormEngines!=null) {
      for (FormEngine formEngine: customFormEngines) {
        formEngines.put(formEngine.getName(), formEngine);
      }
    }
  }

  /**
   * Inits the form types.
   */
  protected void initFormTypes() {
    if (formTypes==null) {
      formTypes = new FormTypes();
      formTypes.addFormType(new StringFormType());
      formTypes.addFormType(new LongFormType());
      formTypes.addFormType(new DateFormType("dd/MM/yyyy"));
      formTypes.addFormType(new BooleanFormType());
    }
    if (customFormTypes!=null) {
      for (AbstractFormType customFormType: customFormTypes) {
        formTypes.addFormType(customFormType);
      }
    }
  }

  /**
   * Inits the scripting engines.
   */
  protected void initScriptingEngines() {
    if (resolverFactories==null) {
      resolverFactories = new ArrayList<ResolverFactory>();
      resolverFactories.add(new VariableScopeResolverFactory());
      resolverFactories.add(new BeansResolverFactory());
    }
    if (scriptingEngines==null) {
      scriptingEngines = new ScriptingEngines(new ScriptBindingsFactory(resolverFactories));
    }
  }

  /**
   * Inits the expression manager.
   */
  protected void initExpressionManager() {
    if (expressionManager==null) {
      expressionManager = new ExpressionManager();
    }
  }

  /**
   * Inits the business calendar manager.
   */
  protected void initBusinessCalendarManager() {
    if (businessCalendarManager==null) {
      MapBusinessCalendarManager mapBusinessCalendarManager = new MapBusinessCalendarManager();
      mapBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar());
      mapBusinessCalendarManager.addBusinessCalendar(DueDateBusinessCalendar.NAME, new DueDateBusinessCalendar());
      mapBusinessCalendarManager.addBusinessCalendar(CycleBusinessCalendar.NAME, new CycleBusinessCalendar());

      businessCalendarManager = mapBusinessCalendarManager;
    }
  }
  
  /**
   * Inits the delegate interceptor.
   */
  protected void initDelegateInterceptor() {
    if(delegateInterceptor == null) {
      delegateInterceptor = new DefaultDelegateInterceptor();
    }
  }
  
  /**
   * Inits the event handlers.
   */
  protected void initEventHandlers() {
    if(eventHandlers == null) {
      eventHandlers = new HashMap<String, EventHandler>();
      
      SignalEventHandler signalEventHander = new SignalEventHandler();
      eventHandlers.put(signalEventHander.getEventHandlerType(), signalEventHander);
      
      CompensationEventHandler compensationEventHandler = new CompensationEventHandler();
      eventHandlers.put(compensationEventHandler.getEventHandlerType(), compensationEventHandler);
      
      MessageEventHandler messageEventHandler = new MessageEventHandler();
      eventHandlers.put(messageEventHandler.getEventHandlerType(), messageEventHandler);
      
    }
    if(customEventHandlers != null) {
      for (EventHandler eventHandler : customEventHandlers) {
        eventHandlers.put(eventHandler.getEventHandlerType(), eventHandler);        
      }
    }
  }
  
  // JPA //////////////////////////////////////////////////////////////////////
  
  /**
   * Inits the jpa.
   */
  protected void initJpa() {
    if(jpaPersistenceUnitName!=null) {
      jpaEntityManagerFactory = JpaHelper.createEntityManagerFactory(jpaPersistenceUnitName);
    }
    if(jpaEntityManagerFactory!=null) {
      sessionFactories.put(EntityManagerSession.class, new EntityManagerSessionFactory(jpaEntityManagerFactory, jpaHandleTransaction, jpaCloseEntityManager));
      VariableType jpaType = variableTypes.getVariableType(JPAEntityVariableType.TYPE_NAME);
      // Add JPA-type
      if(jpaType == null) {
        // We try adding the variable right before SerializableType, if available
        int serializableIndex = variableTypes.getTypeIndex(SerializableType.TYPE_NAME);
        if(serializableIndex > -1) {
          variableTypes.addType(new JPAEntityVariableType(), serializableIndex);
        } else {
          variableTypes.addType(new JPAEntityVariableType());
        }        
      }
    }
  }
  
  /**
   * Inits the beans.
   */
  protected void initBeans() {
    if (beans == null) {
      beans = new HashMap<Object, Object>();
    }
  }

  // getters and setters //////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#getProcessEngineName()
   */
  public String getProcessEngineName() {
    return processEngineName;
  }

  /**
   * Gets the history level.
   *
   * @return the history level
   */
  public int getHistoryLevel() {
    return historyLevel;
  }
  
  /**
   * Sets the history level.
   *
   * @param historyLevel the new history level
   */
  public void setHistoryLevel(int historyLevel) {
    this.historyLevel = historyLevel;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setProcessEngineName(java.lang.String)
   */
  public ProcessEngineConfigurationImpl setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
    return this;
  }
  
  /**
   * Gets the custom pre command interceptors tx required.
   *
   * @return the custom pre command interceptors tx required
   */
  public List<CommandInterceptor> getCustomPreCommandInterceptorsTxRequired() {
    return customPreCommandInterceptorsTxRequired;
  }
  
  /**
   * Sets the custom pre command interceptors tx required.
   *
   * @param customPreCommandInterceptorsTxRequired the custom pre command interceptors tx required
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomPreCommandInterceptorsTxRequired(List<CommandInterceptor> customPreCommandInterceptorsTxRequired) {
    this.customPreCommandInterceptorsTxRequired = customPreCommandInterceptorsTxRequired;
    return this;
  }
  
  /**
   * Gets the custom post command interceptors tx required.
   *
   * @return the custom post command interceptors tx required
   */
  public List<CommandInterceptor> getCustomPostCommandInterceptorsTxRequired() {
    return customPostCommandInterceptorsTxRequired;
  }
  
  /**
   * Sets the custom post command interceptors tx required.
   *
   * @param customPostCommandInterceptorsTxRequired the custom post command interceptors tx required
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomPostCommandInterceptorsTxRequired(List<CommandInterceptor> customPostCommandInterceptorsTxRequired) {
    this.customPostCommandInterceptorsTxRequired = customPostCommandInterceptorsTxRequired;
    return this;
  }
  
  /**
   * Gets the command interceptors tx required.
   *
   * @return the command interceptors tx required
   */
  public List<CommandInterceptor> getCommandInterceptorsTxRequired() {
    return commandInterceptorsTxRequired;
  }
  
  /**
   * Sets the command interceptors tx required.
   *
   * @param commandInterceptorsTxRequired the command interceptors tx required
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCommandInterceptorsTxRequired(List<CommandInterceptor> commandInterceptorsTxRequired) {
    this.commandInterceptorsTxRequired = commandInterceptorsTxRequired;
    return this;
  }
  
  /**
   * Gets the command executor tx required.
   *
   * @return the command executor tx required
   */
  public CommandExecutor getCommandExecutorTxRequired() {
    return commandExecutorTxRequired;
  }
  
  /**
   * Sets the command executor tx required.
   *
   * @param commandExecutorTxRequired the command executor tx required
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCommandExecutorTxRequired(CommandExecutor commandExecutorTxRequired) {
    this.commandExecutorTxRequired = commandExecutorTxRequired;
    return this;
  }
  
  /**
   * Gets the custom pre command interceptors tx requires new.
   *
   * @return the custom pre command interceptors tx requires new
   */
  public List<CommandInterceptor> getCustomPreCommandInterceptorsTxRequiresNew() {
    return customPreCommandInterceptorsTxRequiresNew;
  }
  
  /**
   * Sets the custom pre command interceptors tx requires new.
   *
   * @param customPreCommandInterceptorsTxRequiresNew the custom pre command interceptors tx requires new
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomPreCommandInterceptorsTxRequiresNew(List<CommandInterceptor> customPreCommandInterceptorsTxRequiresNew) {
    this.customPreCommandInterceptorsTxRequiresNew = customPreCommandInterceptorsTxRequiresNew;
    return this;
  }
  
  /**
   * Gets the custom post command interceptors tx requires new.
   *
   * @return the custom post command interceptors tx requires new
   */
  public List<CommandInterceptor> getCustomPostCommandInterceptorsTxRequiresNew() {
    return customPostCommandInterceptorsTxRequiresNew;
  }
  
  /**
   * Sets the custom post command interceptors tx requires new.
   *
   * @param customPostCommandInterceptorsTxRequiresNew the custom post command interceptors tx requires new
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomPostCommandInterceptorsTxRequiresNew(List<CommandInterceptor> customPostCommandInterceptorsTxRequiresNew) {
    this.customPostCommandInterceptorsTxRequiresNew = customPostCommandInterceptorsTxRequiresNew;
    return this;
  }
  
  /**
   * Gets the command interceptors tx requires new.
   *
   * @return the command interceptors tx requires new
   */
  public List<CommandInterceptor> getCommandInterceptorsTxRequiresNew() {
    return commandInterceptorsTxRequiresNew;
  }
  
  /**
   * Sets the command interceptors tx requires new.
   *
   * @param commandInterceptorsTxRequiresNew the command interceptors tx requires new
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCommandInterceptorsTxRequiresNew(List<CommandInterceptor> commandInterceptorsTxRequiresNew) {
    this.commandInterceptorsTxRequiresNew = commandInterceptorsTxRequiresNew;
    return this;
  }
  
  /**
   * Gets the command executor tx requires new.
   *
   * @return the command executor tx requires new
   */
  public CommandExecutor getCommandExecutorTxRequiresNew() {
    return commandExecutorTxRequiresNew;
  }
  
  /**
   * Sets the command executor tx requires new.
   *
   * @param commandExecutorTxRequiresNew the command executor tx requires new
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCommandExecutorTxRequiresNew(CommandExecutor commandExecutorTxRequiresNew) {
    this.commandExecutorTxRequiresNew = commandExecutorTxRequiresNew;
    return this;
  }
  
  /**
   * Gets the repository service.
   *
   * @return the repository service
   */
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }
  
  /**
   * Sets the repository service.
   *
   * @param repositoryService the repository service
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
    return this;
  }
  
  /**
   * Gets the runtime service.
   *
   * @return the runtime service
   */
  public RuntimeService getRuntimeService() {
    return runtimeService;
  }
  
  /**
   * Sets the runtime service.
   *
   * @param runtimeService the runtime service
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
    return this;
  }
  
  /**
   * Gets the history service.
   *
   * @return the history service
   */
  public HistoryService getHistoryService() {
    return historyService;
  }
  
  /**
   * Sets the history service.
   *
   * @param historyService the history service
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setHistoryService(HistoryService historyService) {
    this.historyService = historyService;
    return this;
  }
  
  /**
   * Gets the identity service.
   *
   * @return the identity service
   */
  public IdentityService getIdentityService() {
    return identityService;
  }
  
  /**
   * Sets the identity service.
   *
   * @param identityService the identity service
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
    return this;
  }
  
  /**
   * Gets the task service.
   *
   * @return the task service
   */
  public TaskService getTaskService() {
    return taskService;
  }
  
  /**
   * Sets the task service.
   *
   * @param taskService the task service
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setTaskService(TaskService taskService) {
    this.taskService = taskService;
    return this;
  }
  
  /**
   * Gets the form service.
   *
   * @return the form service
   */
  public FormService getFormService() {
    return formService;
  }
  
  /**
   * Sets the form service.
   *
   * @param formService the form service
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setFormService(FormService formService) {
    this.formService = formService;
    return this;
  }
  
  /**
   * Gets the management service.
   *
   * @return the management service
   */
  public ManagementService getManagementService() {
    return managementService;
  }
  
  /**
   * Sets the management service.
   *
   * @param managementService the management service
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setManagementService(ManagementService managementService) {
    this.managementService = managementService;
    return this;
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
   * Sets the session factories.
   *
   * @param sessionFactories the session factories
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setSessionFactories(Map<Class< ? >, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
    return this;
  }
  
  /**
   * Gets the deployers.
   *
   * @return the deployers
   */
  public List<Deployer> getDeployers() {
    return deployers;
  }
  
  /**
   * Sets the deployers.
   *
   * @param deployers the deployers
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
    return this;
  }
  
  /**
   * Gets the job executor.
   *
   * @return the job executor
   */
  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }
  
  /**
   * Sets the job executor.
   *
   * @param jobExecutor the job executor
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
    return this;
  }
  
  /**
   * Gets the id generator.
   *
   * @return the id generator
   */
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  
  /**
   * Sets the id generator.
   *
   * @param idGenerator the id generator
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
    return this;
  }
  
  /**
   * Gets the ws sync factory class name.
   *
   * @return the ws sync factory class name
   */
  public String getWsSyncFactoryClassName() {
    return wsSyncFactoryClassName;
  }
  
  /**
   * Sets the ws sync factory class name.
   *
   * @param wsSyncFactoryClassName the ws sync factory class name
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setWsSyncFactoryClassName(String wsSyncFactoryClassName) {
    this.wsSyncFactoryClassName = wsSyncFactoryClassName;
    return this;
  }
  
  /**
   * Gets the form engines.
   *
   * @return the form engines
   */
  public Map<String, FormEngine> getFormEngines() {
    return formEngines;
  }
  
  /**
   * Sets the form engines.
   *
   * @param formEngines the form engines
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setFormEngines(Map<String, FormEngine> formEngines) {
    this.formEngines = formEngines;
    return this;
  }
  
  /**
   * Gets the form types.
   *
   * @return the form types
   */
  public FormTypes getFormTypes() {
    return formTypes;
  }
  
  /**
   * Sets the form types.
   *
   * @param formTypes the form types
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setFormTypes(FormTypes formTypes) {
    this.formTypes = formTypes;
    return this;
  }
  
  /**
   * Gets the scripting engines.
   *
   * @return the scripting engines
   */
  public ScriptingEngines getScriptingEngines() {
    return scriptingEngines;
  }
  
  /**
   * Sets the scripting engines.
   *
   * @param scriptingEngines the scripting engines
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setScriptingEngines(ScriptingEngines scriptingEngines) {
    this.scriptingEngines = scriptingEngines;
    return this;
  }
  
  /**
   * Gets the variable types.
   *
   * @return the variable types
   */
  public VariableTypes getVariableTypes() {
    return variableTypes;
  }
  
  /**
   * Sets the variable types.
   *
   * @param variableTypes the variable types
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setVariableTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
    return this;
  }
  
  /**
   * Gets the expression manager.
   *
   * @return the expression manager
   */
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }
  
  /**
   * Sets the expression manager.
   *
   * @param expressionManager the expression manager
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
    return this;
  }
  
  /**
   * Gets the business calendar manager.
   *
   * @return the business calendar manager
   */
  public BusinessCalendarManager getBusinessCalendarManager() {
    return businessCalendarManager;
  }
  
  /**
   * Sets the business calendar manager.
   *
   * @param businessCalendarManager the business calendar manager
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setBusinessCalendarManager(BusinessCalendarManager businessCalendarManager) {
    this.businessCalendarManager = businessCalendarManager;
    return this;
  }
  
  /**
   * Gets the command context factory.
   *
   * @return the command context factory
   */
  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }
  
  /**
   * Sets the command context factory.
   *
   * @param commandContextFactory the command context factory
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
    return this;
  }
  
  /**
   * Gets the transaction context factory.
   *
   * @return the transaction context factory
   */
  public TransactionContextFactory getTransactionContextFactory() {
    return transactionContextFactory;
  }
  
  /**
   * Sets the transaction context factory.
   *
   * @param transactionContextFactory the transaction context factory
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
    this.transactionContextFactory = transactionContextFactory;
    return this;
  }

  
  /**
   * Gets the custom pre deployers.
   *
   * @return the custom pre deployers
   */
  public List<Deployer> getCustomPreDeployers() {
    return customPreDeployers;
  }

  
  /**
   * Sets the custom pre deployers.
   *
   * @param customPreDeployers the custom pre deployers
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomPreDeployers(List<Deployer> customPreDeployers) {
    this.customPreDeployers = customPreDeployers;
    return this;
  }

  
  /**
   * Gets the custom post deployers.
   *
   * @return the custom post deployers
   */
  public List<Deployer> getCustomPostDeployers() {
    return customPostDeployers;
  }

  
  /**
   * Sets the custom post deployers.
   *
   * @param customPostDeployers the custom post deployers
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomPostDeployers(List<Deployer> customPostDeployers) {
    this.customPostDeployers = customPostDeployers;
    return this;
  }

  
  /**
   * Gets the job handlers.
   *
   * @return the job handlers
   */
  public Map<String, JobHandler> getJobHandlers() {
    return jobHandlers;
  }

  
  /**
   * Sets the job handlers.
   *
   * @param jobHandlers the job handlers
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setJobHandlers(Map<String, JobHandler> jobHandlers) {
    this.jobHandlers = jobHandlers;
    return this;
  }

  
  /**
   * Gets the sql session factory.
   *
   * @return the sql session factory
   */
  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }

  
  /**
   * Sets the sql session factory.
   *
   * @param sqlSessionFactory the sql session factory
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
    return this;
  }

  
  /**
   * Gets the db sql session factory.
   *
   * @return the db sql session factory
   */
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

  /**
   * Sets the db sql session factory.
   *
   * @param dbSqlSessionFactory the db sql session factory
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setDbSqlSessionFactory(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    return this;
  }
  
  /**
   * Gets the transaction factory.
   *
   * @return the transaction factory
   */
  public TransactionFactory getTransactionFactory() {
    return transactionFactory;
  }

  /**
   * Sets the transaction factory.
   *
   * @param transactionFactory the transaction factory
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setTransactionFactory(TransactionFactory transactionFactory) {
    this.transactionFactory = transactionFactory;
    return this;
  }

  /**
   * Gets the custom session factories.
   *
   * @return the custom session factories
   */
  public List<SessionFactory> getCustomSessionFactories() {
    return customSessionFactories;
  }
  
  /**
   * Sets the custom session factories.
   *
   * @param customSessionFactories the custom session factories
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomSessionFactories(List<SessionFactory> customSessionFactories) {
    this.customSessionFactories = customSessionFactories;
    return this;
  }
  
  /**
   * Gets the custom job handlers.
   *
   * @return the custom job handlers
   */
  public List<JobHandler> getCustomJobHandlers() {
    return customJobHandlers;
  }
  
  /**
   * Sets the custom job handlers.
   *
   * @param customJobHandlers the custom job handlers
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomJobHandlers(List<JobHandler> customJobHandlers) {
    this.customJobHandlers = customJobHandlers;
    return this;
  }
  
  /**
   * Gets the custom form engines.
   *
   * @return the custom form engines
   */
  public List<FormEngine> getCustomFormEngines() {
    return customFormEngines;
  }
  
  /**
   * Sets the custom form engines.
   *
   * @param customFormEngines the custom form engines
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomFormEngines(List<FormEngine> customFormEngines) {
    this.customFormEngines = customFormEngines;
    return this;
  }

  /**
   * Gets the custom form types.
   *
   * @return the custom form types
   */
  public List<AbstractFormType> getCustomFormTypes() {
    return customFormTypes;
  }

  
  /**
   * Sets the custom form types.
   *
   * @param customFormTypes the custom form types
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomFormTypes(List<AbstractFormType> customFormTypes) {
    this.customFormTypes = customFormTypes;
    return this;
  }

  
  /**
   * Gets the custom scripting engine classes.
   *
   * @return the custom scripting engine classes
   */
  public List<String> getCustomScriptingEngineClasses() {
    return customScriptingEngineClasses;
  }

  
  /**
   * Sets the custom scripting engine classes.
   *
   * @param customScriptingEngineClasses the custom scripting engine classes
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomScriptingEngineClasses(List<String> customScriptingEngineClasses) {
    this.customScriptingEngineClasses = customScriptingEngineClasses;
    return this;
  }

  /**
   * Gets the custom pre variable types.
   *
   * @return the custom pre variable types
   */
  public List<VariableType> getCustomPreVariableTypes() {
    return customPreVariableTypes;
  }

  
  /**
   * Sets the custom pre variable types.
   *
   * @param customPreVariableTypes the custom pre variable types
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomPreVariableTypes(List<VariableType> customPreVariableTypes) {
    this.customPreVariableTypes = customPreVariableTypes;
    return this;
  }

  
  /**
   * Gets the custom post variable types.
   *
   * @return the custom post variable types
   */
  public List<VariableType> getCustomPostVariableTypes() {
    return customPostVariableTypes;
  }

  
  /**
   * Sets the custom post variable types.
   *
   * @param customPostVariableTypes the custom post variable types
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomPostVariableTypes(List<VariableType> customPostVariableTypes) {
    this.customPostVariableTypes = customPostVariableTypes;
    return this;
  }
  
  /**
   * Gets the custom pre bpmn parse listeners.
   *
   * @return the custom pre bpmn parse listeners
   */
  public List<BpmnParseListener> getCustomPreBPMNParseListeners() {
    return preParseListeners;
  }

  /**
   * Sets the custom pre bpmn parse listeners.
   *
   * @param preParseListeners the new custom pre bpmn parse listeners
   */
  public void setCustomPreBPMNParseListeners(List<BpmnParseListener> preParseListeners) {
    this.preParseListeners = preParseListeners;
  }

  /**
   * Gets the custom post bpmn parse listeners.
   *
   * @return the custom post bpmn parse listeners
   */
  public List<BpmnParseListener> getCustomPostBPMNParseListeners() {
    return postParseListeners;
  }

  /**
   * Sets the custom post bpmn parse listeners.
   *
   * @param postParseListeners the new custom post bpmn parse listeners
   */
  public void setCustomPostBPMNParseListeners(List<BpmnParseListener> postParseListeners) {
    this.postParseListeners = postParseListeners;
  }
  
  /**
   * Gets the pre parse listeners.
   *
   * @return the pre parse listeners
   */
  public List<BpmnParseListener> getPreParseListeners() {
    return preParseListeners;
  }

  /**
   * Sets the pre parse listeners.
   *
   * @param preParseListeners the new pre parse listeners
   */
  public void setPreParseListeners(List<BpmnParseListener> preParseListeners) {
    this.preParseListeners = preParseListeners;
  }
  
  /**
   * Gets the post parse listeners.
   *
   * @return the post parse listeners
   */
  public List<BpmnParseListener> getPostParseListeners() {
    return postParseListeners;
  }
  
  /**
   * Sets the post parse listeners.
   *
   * @param postParseListeners the new post parse listeners
   */
  public void setPostParseListeners(List<BpmnParseListener> postParseListeners) {
    this.postParseListeners = postParseListeners;
  }

  /**
   * Gets the beans.
   *
   * @return the beans
   */
  public Map<Object, Object> getBeans() {
    return beans;
  }

  /**
   * Sets the beans.
   *
   * @param beans the beans
   */
  public void setBeans(Map<Object, Object> beans) {
    this.beans = beans;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setClassLoader(java.lang.ClassLoader)
   */
  @Override
  public ProcessEngineConfigurationImpl setClassLoader(ClassLoader classLoader) {
    super.setClassLoader(classLoader);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setDatabaseType(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setDatabaseType(String databaseType) {
    super.setDatabaseType(databaseType);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setDataSource(javax.sql.DataSource)
   */
  @Override
  public ProcessEngineConfigurationImpl setDataSource(DataSource dataSource) {
    super.setDataSource(dataSource);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setDatabaseSchemaUpdate(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
    super.setDatabaseSchemaUpdate(databaseSchemaUpdate);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setHistory(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setHistory(String history) {
    super.setHistory(history);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setIdBlockSize(int)
   */
  @Override
  public ProcessEngineConfigurationImpl setIdBlockSize(int idBlockSize) {
    super.setIdBlockSize(idBlockSize);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcDriver(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcDriver(String jdbcDriver) {
    super.setJdbcDriver(jdbcDriver);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcPassword(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcPassword(String jdbcPassword) {
    super.setJdbcPassword(jdbcPassword);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcUrl(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcUrl(String jdbcUrl) {
    super.setJdbcUrl(jdbcUrl);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcUsername(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcUsername(String jdbcUsername) {
    super.setJdbcUsername(jdbcUsername);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJobExecutorActivate(boolean)
   */
  @Override
  public ProcessEngineConfigurationImpl setJobExecutorActivate(boolean jobExecutorActivate) {
    super.setJobExecutorActivate(jobExecutorActivate);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setMailServerDefaultFrom(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setMailServerDefaultFrom(String mailServerDefaultFrom) {
    super.setMailServerDefaultFrom(mailServerDefaultFrom);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setMailServerHost(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setMailServerHost(String mailServerHost) {
    super.setMailServerHost(mailServerHost);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setMailServerPassword(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setMailServerPassword(String mailServerPassword) {
    super.setMailServerPassword(mailServerPassword);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setMailServerPort(int)
   */
  @Override
  public ProcessEngineConfigurationImpl setMailServerPort(int mailServerPort) {
    super.setMailServerPort(mailServerPort);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setMailServerUseTLS(boolean)
   */
  @Override
  public ProcessEngineConfigurationImpl setMailServerUseTLS(boolean useTLS) {
    super.setMailServerUseTLS(useTLS);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setMailServerUsername(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setMailServerUsername(String mailServerUsername) {
    super.setMailServerUsername(mailServerUsername);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcMaxActiveConnections(int)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
    super.setJdbcMaxActiveConnections(jdbcMaxActiveConnections);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcMaxCheckoutTime(int)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
    super.setJdbcMaxCheckoutTime(jdbcMaxCheckoutTime);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcMaxIdleConnections(int)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
    super.setJdbcMaxIdleConnections(jdbcMaxIdleConnections);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcMaxWaitTime(int)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
    super.setJdbcMaxWaitTime(jdbcMaxWaitTime);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setTransactionsExternallyManaged(boolean)
   */
  @Override
  public ProcessEngineConfigurationImpl setTransactionsExternallyManaged(boolean transactionsExternallyManaged) {
    super.setTransactionsExternallyManaged(transactionsExternallyManaged);
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJpaEntityManagerFactory(java.lang.Object)
   */
  @Override
  public ProcessEngineConfigurationImpl setJpaEntityManagerFactory(Object jpaEntityManagerFactory) {
    this.jpaEntityManagerFactory = jpaEntityManagerFactory;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJpaHandleTransaction(boolean)
   */
  @Override
  public ProcessEngineConfigurationImpl setJpaHandleTransaction(boolean jpaHandleTransaction) {
    this.jpaHandleTransaction = jpaHandleTransaction;
    return this;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJpaCloseEntityManager(boolean)
   */
  @Override
  public ProcessEngineConfigurationImpl setJpaCloseEntityManager(boolean jpaCloseEntityManager) {
    this.jpaCloseEntityManager = jpaCloseEntityManager;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcPingEnabled(boolean)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcPingEnabled(boolean jdbcPingEnabled) {
    this.jdbcPingEnabled = jdbcPingEnabled;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcPingQuery(java.lang.String)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcPingQuery(String jdbcPingQuery) {
    this.jdbcPingQuery = jdbcPingQuery;
    return this;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.ProcessEngineConfiguration#setJdbcPingConnectionNotUsedFor(int)
   */
  @Override
  public ProcessEngineConfigurationImpl setJdbcPingConnectionNotUsedFor(int jdbcPingNotUsedFor) {
    this.jdbcPingConnectionNotUsedFor = jdbcPingNotUsedFor;
    return this;
  }    
  
  /**
   * Checks if is db identity used.
   *
   * @return true, if is db identity used
   */
  public boolean isDbIdentityUsed() {
    return isDbIdentityUsed;
  }

  
  /**
   * Sets the db identity used.
   *
   * @param isDbIdentityUsed the new db identity used
   */
  public void setDbIdentityUsed(boolean isDbIdentityUsed) {
    this.isDbIdentityUsed = isDbIdentityUsed;
  }

  
  /**
   * Checks if is db history used.
   *
   * @return true, if is db history used
   */
  public boolean isDbHistoryUsed() {
    return isDbHistoryUsed;
  }
  
  /**
   * Sets the db history used.
   *
   * @param isDbHistoryUsed the new db history used
   */
  public void setDbHistoryUsed(boolean isDbHistoryUsed) {
    this.isDbHistoryUsed = isDbHistoryUsed;
  }
  
  /**
   * Gets the resolver factories.
   *
   * @return the resolver factories
   */
  public List<ResolverFactory> getResolverFactories() {
    return resolverFactories;
  }
  
  /**
   * Sets the resolver factories.
   *
   * @param resolverFactories the new resolver factories
   */
  public void setResolverFactories(List<ResolverFactory> resolverFactories) {
    this.resolverFactories = resolverFactories;
  }

  /**
   * Gets the mail scanner.
   *
   * @return the mail scanner
   */
  public MailScanner getMailScanner() {
    return mailScanner;
  }

  /**
   * Sets the mail scanner.
   *
   * @param mailScanner the new mail scanner
   */
  public void setMailScanner(MailScanner mailScanner) {
    this.mailScanner = mailScanner;
  }
  
  /**
   * Gets the deployment cache.
   *
   * @return the deployment cache
   */
  public DeploymentCache getDeploymentCache() {
    return deploymentCache;
  }
  
  /**
   * Sets the deployment cache.
   *
   * @param deploymentCache the new deployment cache
   */
  public void setDeploymentCache(DeploymentCache deploymentCache) {
    this.deploymentCache = deploymentCache;
  }
    
  /**
   * Sets the delegate interceptor.
   *
   * @param delegateInterceptor the delegate interceptor
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setDelegateInterceptor(DelegateInterceptor delegateInterceptor) {
    this.delegateInterceptor = delegateInterceptor;
    return this;
  }
    
  /**
   * Gets the delegate interceptor.
   *
   * @return the delegate interceptor
   */
  public DelegateInterceptor getDelegateInterceptor() {
    return delegateInterceptor;
  }
    
  /**
   * Gets the custom rejected jobs handler.
   *
   * @return the custom rejected jobs handler
   */
  public RejectedJobsHandler getCustomRejectedJobsHandler() {
    return customRejectedJobsHandler;
  }
    
  /**
   * Sets the custom rejected jobs handler.
   *
   * @param customRejectedJobsHandler the custom rejected jobs handler
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setCustomRejectedJobsHandler(RejectedJobsHandler customRejectedJobsHandler) {
    this.customRejectedJobsHandler = customRejectedJobsHandler;
    return this;
  }

  /**
   * Gets the event handler.
   *
   * @param eventType the event type
   * @return the event handler
   */
  public EventHandler getEventHandler(String eventType) {
    return eventHandlers.get(eventType);
  }
  
  /**
   * Sets the event handlers.
   *
   * @param eventHandlers the event handlers
   */
  public void setEventHandlers(Map<String, EventHandler> eventHandlers) {
    this.eventHandlers = eventHandlers;
  }
    
  /**
   * Gets the event handlers.
   *
   * @return the event handlers
   */
  public Map<String, EventHandler> getEventHandlers() {
    return eventHandlers;
  }
    
  /**
   * Gets the custom event handlers.
   *
   * @return the custom event handlers
   */
  public List<EventHandler> getCustomEventHandlers() {
    return customEventHandlers;
  }
    
  /**
   * Sets the custom event handlers.
   *
   * @param customEventHandlers the new custom event handlers
   */
  public void setCustomEventHandlers(List<EventHandler> customEventHandlers) {
    this.customEventHandlers = customEventHandlers;
  }
  
  /**
   * Gets the failed job command factory.
   *
   * @return the failed job command factory
   */
  public FailedJobCommandFactory getFailedJobCommandFactory() {
    return failedJobCommandFactory;
  }
  
  /**
   * Sets the failed job command factory.
   *
   * @param failedJobCommandFactory the failed job command factory
   * @return the process engine configuration impl
   */
  public ProcessEngineConfigurationImpl setFailedJobCommandFactory(FailedJobCommandFactory failedJobCommandFactory) {
    this.failedJobCommandFactory = failedJobCommandFactory;
    return this;
  }
  
  /**
   * Allows configuring a database table prefix which is used for all runtime operations of the process engine.
   * For example, if you specify a prefix named 'PRE1.', activiti will query for executions in a table named
   * 'PRE1.ACT_RU_EXECUTION_'.
   * 
   * <p />
   * <strong>NOTE: the prefix is not respected by automatic database schema management. If you use
   *
   * @param databaseTablePrefix the database table prefix
   * @return the process engine configuration
   * {@link ProcessEngineConfiguration#DB_SCHEMA_UPDATE_CREATE_DROP}
   * or {@link ProcessEngineConfiguration#DB_SCHEMA_UPDATE_TRUE}, activiti will create the database tables
   * using the default names, regardless of the prefix configured here.</strong>
   * @since 5.9
   */
  public ProcessEngineConfiguration setDatabaseTablePrefix(String databaseTablePrefix) {
    this.databaseTablePrefix = databaseTablePrefix;
    return this;
  }
    
  /**
   * Gets the database table prefix.
   *
   * @return the database table prefix
   */
  public String getDatabaseTablePrefix() {
    return databaseTablePrefix;
  }

  /**
   * Checks if is creates the diagram on deploy.
   *
   * @return true, if is creates the diagram on deploy
   */
  public boolean isCreateDiagramOnDeploy() {
    return isCreateDiagramOnDeploy;
  }

  /**
   * Sets the create diagram on deploy.
   *
   * @param createDiagramOnDeploy the create diagram on deploy
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setCreateDiagramOnDeploy(boolean createDiagramOnDeploy) {
    this.isCreateDiagramOnDeploy = createDiagramOnDeploy;
    return this;
  }
  
  /**
   * Gets the database schema.
   *
   * @return the database schema
   */
  public String getDatabaseSchema() {
    return databaseSchema;
  }
  
  /**
   * Sets the database schema.
   *
   * @param databaseSchema the new database schema
   */
  public void setDatabaseSchema(String databaseSchema) {
    this.databaseSchema = databaseSchema;
  }

  /**
   * Gets the id generator data source.
   *
   * @return the id generator data source
   */
  public DataSource getIdGeneratorDataSource() {
    return idGeneratorDataSource;
  }
  
  /**
   * Sets the id generator data source.
   *
   * @param idGeneratorDataSource the new id generator data source
   */
  public void setIdGeneratorDataSource(DataSource idGeneratorDataSource) {
    this.idGeneratorDataSource = idGeneratorDataSource;
  }
  
  /**
   * Gets the id generator data source jndi name.
   *
   * @return the id generator data source jndi name
   */
  public String getIdGeneratorDataSourceJndiName() {
    return idGeneratorDataSourceJndiName;
  }

  /**
   * Sets the id generator data source jndi name.
   *
   * @param idGeneratorDataSourceJndiName the new id generator data source jndi name
   */
  public void setIdGeneratorDataSourceJndiName(String idGeneratorDataSourceJndiName) {
    this.idGeneratorDataSourceJndiName = idGeneratorDataSourceJndiName;
  }
}