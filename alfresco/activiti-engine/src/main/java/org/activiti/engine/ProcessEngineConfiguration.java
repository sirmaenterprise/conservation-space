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

package org.activiti.engine;

import java.io.InputStream;

import javax.sql.DataSource;

import org.activiti.engine.impl.cfg.BeansConfigurationHelper;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;


// TODO: Auto-generated Javadoc
/** Configuration information from which a process engine can be build.
 * 
 * <p>Most common is to create a process engine based on the default configuration file:
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createProcessEngineConfigurationFromResourceDefault()
 *   .buildProcessEngine();
 * </pre>
 * </p>
 * 
 * <p>To create a process engine programatic, without a configuration file, 
 * the first option is {@link #createStandaloneProcessEngineConfiguration()}
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createStandaloneProcessEngineConfiguration()
 *   .buildProcessEngine();
 * </pre>
 * This creates a new process engine with all the defaults to connect to 
 * a remote h2 database (jdbc:h2:tcp://localhost/activiti) in standalone 
 * mode.  Standalone mode means that Activiti will manage the transactions 
 * on the JDBC connections that it creates.  One transaction per 
 * service method.
 * For a description of how to write the configuration files, see the 
 * userguide.
 * </p>
 * 
 * <p>The second option is great for testing: {@link #createStandalonInMemeProcessEngineConfiguration()}
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createStandaloneInMemProcessEngineConfiguration()
 *   .buildProcessEngine();
 * </pre>
 * This creates a new process engine with all the defaults to connect to 
 * an memory h2 database (jdbc:h2:tcp://localhost/activiti) in standalone 
 * mode.  The DB schema strategy default is in this case <code>create-drop</code>.  
 * Standalone mode means that Activiti will manage the transactions 
 * on the JDBC connections that it creates.  One transaction per 
 * service method.
 * </p>
 * 
 * <p>On all forms of creating a process engine, you can first customize the configuration 
 * before calling the {@link #buildProcessEngine()} method by calling any of the 
 * setters like this:
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createProcessEngineConfigurationFromResourceDefault()
 *   .setMailServerHost("gmail.com")
 *   .setJdbcUsername("mickey")
 *   .setJdbcPassword("mouse")
 *   .buildProcessEngine();
 * </pre>
 * </p>
 * 
 * @see ProcessEngines 
 * @author Tom Baeyens
 */
public abstract class ProcessEngineConfiguration {
  
  /** Checks the version of the DB schema against the library when 
   * the process engine is being created and throws an exception
   * if the versions don't match. */
  public static final String DB_SCHEMA_UPDATE_FALSE = "false";
  
  /** Creates the schema when the process engine is being created and 
   * drops the schema when the process engine is being closed. */
  public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";

  /** Upon building of the process engine, a check is performed and 
   * an update of the schema is performed if it is necessary. */
  public static final String DB_SCHEMA_UPDATE_TRUE = "true";

  /** Value for {@link #setHistory(String)} to ensure that no history is being recorded. */
  public static final String HISTORY_NONE = "none";
  /** Value for {@link #setHistory(String)} to ensure that only historic process instances and 
   * historic activity instances are being recorded. 
   * This means no details for those entities. */
  public static final String HISTORY_ACTIVITY = "activity";
  /** Value for {@link #setHistory(String)} to ensure that only historic process instances, 
   * historic activity instances and submitted form property values are being recorded. */ 
  public static final String HISTORY_AUDIT = "audit";
  /** Value for {@link #setHistory(String)} to ensure that all historic information is 
   * being recorded, including the variable updates. */ 
  public static final String HISTORY_FULL = "full";
  
  /** The process engine name. */
  protected String processEngineName = ProcessEngines.NAME_DEFAULT;
  
  /** The id block size. */
  protected int idBlockSize = 100;
  
  /** The history. */
  protected String history = HISTORY_AUDIT;
  
  /** The job executor activate. */
  protected boolean jobExecutorActivate;

  /** The mail server host. */
  protected String mailServerHost = "localhost";
  
  /** The mail server username. */
  protected String mailServerUsername; // by default no name and password are provided, which 
  
  /** The mail server password. */
  protected String mailServerPassword; // means no authentication for mail server
  
  /** The mail server port. */
  protected int mailServerPort = 25;
  
  /** The use tls. */
  protected boolean useTLS = false;
  
  /** The mail server default from. */
  protected String mailServerDefaultFrom = "activiti@localhost";

  /** The database type. */
  protected String databaseType;
  
  /** The database schema update. */
  protected String databaseSchemaUpdate = DB_SCHEMA_UPDATE_FALSE;
  
  /** The jdbc driver. */
  protected String jdbcDriver = "org.h2.Driver";
  
  /** The jdbc url. */
  protected String jdbcUrl = "jdbc:h2:tcp://localhost/activiti";
  
  /** The jdbc username. */
  protected String jdbcUsername = "sa";
  
  /** The jdbc password. */
  protected String jdbcPassword = "";
  
  /** The data source jndi name. */
  protected String dataSourceJndiName = null;
  
  /** The jdbc max active connections. */
  protected int jdbcMaxActiveConnections;
  
  /** The jdbc max idle connections. */
  protected int jdbcMaxIdleConnections;
  
  /** The jdbc max checkout time. */
  protected int jdbcMaxCheckoutTime;
  
  /** The jdbc max wait time. */
  protected int jdbcMaxWaitTime;
  
  /** The jdbc ping enabled. */
  protected boolean jdbcPingEnabled = false;
  
  /** The jdbc ping query. */
  protected String jdbcPingQuery = null;
  
  /** The jdbc ping connection not used for. */
  protected int jdbcPingConnectionNotUsedFor;
  
  /** The data source. */
  protected DataSource dataSource;
  
  /** The transactions externally managed. */
  protected boolean transactionsExternallyManaged = false;
  
  /** The jpa persistence unit name. */
  protected String jpaPersistenceUnitName;
  
  /** The jpa entity manager factory. */
  protected Object jpaEntityManagerFactory;
  
  /** The jpa handle transaction. */
  protected boolean jpaHandleTransaction;
  
  /** The jpa close entity manager. */
  protected boolean jpaCloseEntityManager;
  
  /** The class loader. */
  protected ClassLoader classLoader;

  /**
   * use one of the static createXxxx methods instead.
   */
  protected ProcessEngineConfiguration() {
  }

  /**
   * Builds the process engine.
   *
   * @return the process engine
   */
  public abstract ProcessEngine buildProcessEngine();
  
  /**
   * Creates the process engine configuration from resource default.
   *
   * @return the process engine configuration
   */
  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResourceDefault() {
    return createProcessEngineConfigurationFromResource("activiti.cfg.xml", "processEngineConfiguration");
  }

  /**
   * Creates the process engine configuration from resource.
   *
   * @param resource the resource
   * @return the process engine configuration
   */
  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResource(String resource) {
    return createProcessEngineConfigurationFromResource(resource, "processEngineConfiguration");
  }

  /**
   * Creates the process engine configuration from resource.
   *
   * @param resource the resource
   * @param beanName the bean name
   * @return the process engine configuration
   */
  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResource(String resource, String beanName) {
    return BeansConfigurationHelper.parseProcessEngineConfigurationFromResource(resource, beanName);
  }
  
  /**
   * Creates the process engine configuration from input stream.
   *
   * @param inputStream the input stream
   * @return the process engine configuration
   */
  public static ProcessEngineConfiguration createProcessEngineConfigurationFromInputStream(InputStream inputStream) {
    return createProcessEngineConfigurationFromInputStream(inputStream, "processEngineConfiguration");
  }

  /**
   * Creates the process engine configuration from input stream.
   *
   * @param inputStream the input stream
   * @param beanName the bean name
   * @return the process engine configuration
   */
  public static ProcessEngineConfiguration createProcessEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    return BeansConfigurationHelper.parseProcessEngineConfigurationFromInputStream(inputStream, beanName);
  }

  /**
   * Creates the standalone process engine configuration.
   *
   * @return the process engine configuration
   */
  public static ProcessEngineConfiguration createStandaloneProcessEngineConfiguration() {
    return new StandaloneProcessEngineConfiguration();
  }

  /**
   * Creates the standalone in mem process engine configuration.
   *
   * @return the process engine configuration
   */
  public static ProcessEngineConfiguration createStandaloneInMemProcessEngineConfiguration() {
    return new StandaloneInMemProcessEngineConfiguration();
  }

// TODO add later when we have test coverage for this
//  public static ProcessEngineConfiguration createJtaProcessEngineConfiguration() {
//    return new JtaProcessEngineConfiguration();
//  }
  

  // getters and setters //////////////////////////////////////////////////////
  
  /**
 * Gets the process engine name.
 *
 * @return the process engine name
 */
public String getProcessEngineName() {
    return processEngineName;
  }

  /**
   * Sets the process engine name.
   *
   * @param processEngineName the process engine name
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
    return this;
  }

  
  /**
   * Gets the id block size.
   *
   * @return the id block size
   */
  public int getIdBlockSize() {
    return idBlockSize;
  }

  
  /**
   * Sets the id block size.
   *
   * @param idBlockSize the id block size
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setIdBlockSize(int idBlockSize) {
    this.idBlockSize = idBlockSize;
    return this;
  }

  
  /**
   * Gets the history.
   *
   * @return the history
   */
  public String getHistory() {
    return history;
  }

  
  /**
   * Sets the history.
   *
   * @param history the history
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setHistory(String history) {
    this.history = history;
    return this;
  }

  
  /**
   * Gets the mail server host.
   *
   * @return the mail server host
   */
  public String getMailServerHost() {
    return mailServerHost;
  }

  
  /**
   * Sets the mail server host.
   *
   * @param mailServerHost the mail server host
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setMailServerHost(String mailServerHost) {
    this.mailServerHost = mailServerHost;
    return this;
  }

  
  /**
   * Gets the mail server username.
   *
   * @return the mail server username
   */
  public String getMailServerUsername() {
    return mailServerUsername;
  }

  
  /**
   * Sets the mail server username.
   *
   * @param mailServerUsername the mail server username
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setMailServerUsername(String mailServerUsername) {
    this.mailServerUsername = mailServerUsername;
    return this;
  }

  
  /**
   * Gets the mail server password.
   *
   * @return the mail server password
   */
  public String getMailServerPassword() {
    return mailServerPassword;
  }

  
  /**
   * Sets the mail server password.
   *
   * @param mailServerPassword the mail server password
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setMailServerPassword(String mailServerPassword) {
    this.mailServerPassword = mailServerPassword;
    return this;
  }

  
  /**
   * Gets the mail server port.
   *
   * @return the mail server port
   */
  public int getMailServerPort() {
    return mailServerPort;
  }

  
  /**
   * Sets the mail server port.
   *
   * @param mailServerPort the mail server port
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setMailServerPort(int mailServerPort) {
    this.mailServerPort = mailServerPort;
    return this;
  }

  
  /**
   * Gets the mail server use tls.
   *
   * @return the mail server use tls
   */
  public boolean getMailServerUseTLS() {
    return useTLS;
  }

  
  /**
   * Sets the mail server use tls.
   *
   * @param useTLS the use tls
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setMailServerUseTLS(boolean useTLS) {
    this.useTLS = useTLS;
    return this;
  }

  
  /**
   * Gets the mail server default from.
   *
   * @return the mail server default from
   */
  public String getMailServerDefaultFrom() {
    return mailServerDefaultFrom;
  }

  
  /**
   * Sets the mail server default from.
   *
   * @param mailServerDefaultFrom the mail server default from
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setMailServerDefaultFrom(String mailServerDefaultFrom) {
    this.mailServerDefaultFrom = mailServerDefaultFrom;
    return this;
  }

  
  /**
   * Gets the database type.
   *
   * @return the database type
   */
  public String getDatabaseType() {
    return databaseType;
  }

  
  /**
   * Sets the database type.
   *
   * @param databaseType the database type
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
    return this;
  }

  
  /**
   * Gets the database schema update.
   *
   * @return the database schema update
   */
  public String getDatabaseSchemaUpdate() {
    return databaseSchemaUpdate;
  }

  
  /**
   * Sets the database schema update.
   *
   * @param databaseSchemaUpdate the database schema update
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
    this.databaseSchemaUpdate = databaseSchemaUpdate;
    return this;
  }

  
  /**
   * Gets the data source.
   *
   * @return the data source
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  
  /**
   * Sets the data source.
   *
   * @param dataSource the data source
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  
  /**
   * Gets the jdbc driver.
   *
   * @return the jdbc driver
   */
  public String getJdbcDriver() {
    return jdbcDriver;
  }

  
  /**
   * Sets the jdbc driver.
   *
   * @param jdbcDriver the jdbc driver
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
    return this;
  }

  
  /**
   * Gets the jdbc url.
   *
   * @return the jdbc url
   */
  public String getJdbcUrl() {
    return jdbcUrl;
  }

  
  /**
   * Sets the jdbc url.
   *
   * @param jdbcUrl the jdbc url
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  
  /**
   * Gets the jdbc username.
   *
   * @return the jdbc username
   */
  public String getJdbcUsername() {
    return jdbcUsername;
  }

  
  /**
   * Sets the jdbc username.
   *
   * @param jdbcUsername the jdbc username
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    return this;
  }

  
  /**
   * Gets the jdbc password.
   *
   * @return the jdbc password
   */
  public String getJdbcPassword() {
    return jdbcPassword;
  }

  
  /**
   * Sets the jdbc password.
   *
   * @param jdbcPassword the jdbc password
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    return this;
  }

  
  /**
   * Checks if is transactions externally managed.
   *
   * @return true, if is transactions externally managed
   */
  public boolean isTransactionsExternallyManaged() {
    return transactionsExternallyManaged;
  }

  
  /**
   * Sets the transactions externally managed.
   *
   * @param transactionsExternallyManaged the transactions externally managed
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setTransactionsExternallyManaged(boolean transactionsExternallyManaged) {
    this.transactionsExternallyManaged = transactionsExternallyManaged;
    return this;
  }

  
  /**
   * Gets the jdbc max active connections.
   *
   * @return the jdbc max active connections
   */
  public int getJdbcMaxActiveConnections() {
    return jdbcMaxActiveConnections;
  }

  
  /**
   * Sets the jdbc max active connections.
   *
   * @param jdbcMaxActiveConnections the jdbc max active connections
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
    this.jdbcMaxActiveConnections = jdbcMaxActiveConnections;
    return this;
  }

  
  /**
   * Gets the jdbc max idle connections.
   *
   * @return the jdbc max idle connections
   */
  public int getJdbcMaxIdleConnections() {
    return jdbcMaxIdleConnections;
  }

  
  /**
   * Sets the jdbc max idle connections.
   *
   * @param jdbcMaxIdleConnections the jdbc max idle connections
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
    this.jdbcMaxIdleConnections = jdbcMaxIdleConnections;
    return this;
  }

  
  /**
   * Gets the jdbc max checkout time.
   *
   * @return the jdbc max checkout time
   */
  public int getJdbcMaxCheckoutTime() {
    return jdbcMaxCheckoutTime;
  }

  
  /**
   * Sets the jdbc max checkout time.
   *
   * @param jdbcMaxCheckoutTime the jdbc max checkout time
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
    this.jdbcMaxCheckoutTime = jdbcMaxCheckoutTime;
    return this;
  }

  
  /**
   * Gets the jdbc max wait time.
   *
   * @return the jdbc max wait time
   */
  public int getJdbcMaxWaitTime() {
    return jdbcMaxWaitTime;
  }
  
  /**
   * Sets the jdbc max wait time.
   *
   * @param jdbcMaxWaitTime the jdbc max wait time
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
    this.jdbcMaxWaitTime = jdbcMaxWaitTime;
    return this;
  }
  
  /**
   * Checks if is jdbc ping enabled.
   *
   * @return true, if is jdbc ping enabled
   */
  public boolean isJdbcPingEnabled() {
    return jdbcPingEnabled;
  }

  /**
   * Sets the jdbc ping enabled.
   *
   * @param jdbcPingEnabled the jdbc ping enabled
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcPingEnabled(boolean jdbcPingEnabled) {
    this.jdbcPingEnabled = jdbcPingEnabled;
    return this;
  }

  /**
   * Gets the jdbc ping query.
   *
   * @return the jdbc ping query
   */
  public String getJdbcPingQuery() {
      return jdbcPingQuery;
  }

  /**
   * Sets the jdbc ping query.
   *
   * @param jdbcPingQuery the jdbc ping query
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcPingQuery(String jdbcPingQuery) {
    this.jdbcPingQuery = jdbcPingQuery;
    return this;
  }

  /**
   * Gets the jdbc ping connection not used for.
   *
   * @return the jdbc ping connection not used for
   */
  public int getJdbcPingConnectionNotUsedFor() {
      return jdbcPingConnectionNotUsedFor;
  }

  /**
   * Sets the jdbc ping connection not used for.
   *
   * @param jdbcPingNotUsedFor the jdbc ping not used for
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJdbcPingConnectionNotUsedFor(int jdbcPingNotUsedFor) {
    this.jdbcPingConnectionNotUsedFor = jdbcPingNotUsedFor;
    return this;
  }

  /**
   * Checks if is job executor activate.
   *
   * @return true, if is job executor activate
   */
  public boolean isJobExecutorActivate() {
    return jobExecutorActivate;
  }

  
  /**
   * Sets the job executor activate.
   *
   * @param jobExecutorActivate the job executor activate
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJobExecutorActivate(boolean jobExecutorActivate) {
    this.jobExecutorActivate = jobExecutorActivate;
    return this;
  }
  
  /**
   * Gets the class loader.
   *
   * @return the class loader
   */
  public ClassLoader getClassLoader() {
    return classLoader;
  }
  
  /**
   * Sets the class loader.
   *
   * @param classLoader the class loader
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  
  /**
   * Gets the jpa entity manager factory.
   *
   * @return the jpa entity manager factory
   */
  public Object getJpaEntityManagerFactory() {
    return jpaEntityManagerFactory;
  }

  
  /**
   * Sets the jpa entity manager factory.
   *
   * @param jpaEntityManagerFactory the jpa entity manager factory
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJpaEntityManagerFactory(Object jpaEntityManagerFactory) {
    this.jpaEntityManagerFactory = jpaEntityManagerFactory;
    return this;
  }

  
  /**
   * Checks if is jpa handle transaction.
   *
   * @return true, if is jpa handle transaction
   */
  public boolean isJpaHandleTransaction() {
    return jpaHandleTransaction;
  }

  
  /**
   * Sets the jpa handle transaction.
   *
   * @param jpaHandleTransaction the jpa handle transaction
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJpaHandleTransaction(boolean jpaHandleTransaction) {
    this.jpaHandleTransaction = jpaHandleTransaction;
    return this;
  }

  
  /**
   * Checks if is jpa close entity manager.
   *
   * @return true, if is jpa close entity manager
   */
  public boolean isJpaCloseEntityManager() {
    return jpaCloseEntityManager;
  }

  
  /**
   * Sets the jpa close entity manager.
   *
   * @param jpaCloseEntityManager the jpa close entity manager
   * @return the process engine configuration
   */
  public ProcessEngineConfiguration setJpaCloseEntityManager(boolean jpaCloseEntityManager) {
    this.jpaCloseEntityManager = jpaCloseEntityManager;
    return this;
  }

  /**
   * Gets the jpa persistence unit name.
   *
   * @return the jpa persistence unit name
   */
  public String getJpaPersistenceUnitName() {
    return jpaPersistenceUnitName;
  }

  /**
   * Sets the jpa persistence unit name.
   *
   * @param jpaPersistenceUnitName the new jpa persistence unit name
   */
  public void setJpaPersistenceUnitName(String jpaPersistenceUnitName) {
    this.jpaPersistenceUnitName = jpaPersistenceUnitName;
  }

  /**
   * Gets the data source jndi name.
   *
   * @return the data source jndi name
   */
  public String getDataSourceJndiName() {
    return dataSourceJndiName;
  }

  /**
   * Sets the data source jndi name.
   *
   * @param dataSourceJndiName the new data source jndi name
   */
  public void setDataSourceJndiName(String dataSourceJndiName) {
    this.dataSourceJndiName = dataSourceJndiName;
  }

}
