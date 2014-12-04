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

package org.activiti.engine.impl.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.apache.ibatis.session.SqlSessionFactory;


// TODO: Auto-generated Javadoc
/**
 * A factory for creating DbSqlSession objects.
 *
 * @author Tom Baeyens
 */
public class DbSqlSessionFactory implements SessionFactory {

  /** The Constant databaseSpecificStatements. */
  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String,String>>();
  
  /** The Constant databaseSpecificLimitBeforeStatements. */
  public static final Map<String, String> databaseSpecificLimitBeforeStatements = new HashMap<String, String>();
  
  /** The Constant databaseSpecificLimitAfterStatements. */
  public static final Map<String, String> databaseSpecificLimitAfterStatements = new HashMap<String, String>();

  static {
    // h2
    databaseSpecificLimitBeforeStatements.put("h2", "");
    databaseSpecificLimitAfterStatements.put("h2", "LIMIT #{maxResults} OFFSET #{firstResult}");
    
	  //mysql specific
    databaseSpecificLimitBeforeStatements.put("mysql", "");
    databaseSpecificLimitAfterStatements.put("mysql", "LIMIT #{maxResults} OFFSET #{firstResult}");
    addDatabaseSpecificStatement("mysql", "selectNextJobsToExecute", "selectNextJobsToExecute_mysql");
    addDatabaseSpecificStatement("mysql", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_mysql");
    addDatabaseSpecificStatement("mysql", "selectProcessDefinitionsByQueryCriteria", "selectProcessDefinitionsByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectProcessDefinitionCountByQueryCriteria", "selectProcessDefinitionCountByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectDeploymentsByQueryCriteria", "selectDeploymentsByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectDeploymentCountByQueryCriteria", "selectDeploymentCountByQueryCriteria_mysql");
    
    //postgres specific
    databaseSpecificLimitBeforeStatements.put("postgres", "");
    databaseSpecificLimitAfterStatements.put("postgres", "LIMIT #{maxResults} OFFSET #{firstResult}");
    addDatabaseSpecificStatement("postgres", "insertByteArray", "insertByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "updateByteArray", "updateByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "selectByteArray", "selectByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "selectResourceByDeploymentIdAndResourceName", "selectResourceByDeploymentIdAndResourceName_postgres");
    addDatabaseSpecificStatement("postgres", "selectResourcesByDeploymentId", "selectResourcesByDeploymentId_postgres");
    addDatabaseSpecificStatement("postgres", "selectHistoricDetailsByQueryCriteria", "selectHistoricDetailsByQueryCriteria_postgres");
    addDatabaseSpecificStatement("postgres", "insertIdentityInfo", "insertIdentityInfo_postgres");
    addDatabaseSpecificStatement("postgres", "updateIdentityInfo", "updateIdentityInfo_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoById", "selectIdentityInfoById_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoByUserIdAndKey", "selectIdentityInfoByUserIdAndKey_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoByUserId", "selectIdentityInfoByUserId_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoDetails", "selectIdentityInfoDetails_postgres");
    addDatabaseSpecificStatement("postgres", "insertComment", "insertComment_postgres");
    addDatabaseSpecificStatement("postgres", "selectCommentsByTaskId", "selectCommentsByTaskId_postgres");
    addDatabaseSpecificStatement("postgres", "selectCommentsByProcessInstanceId", "selectCommentsByProcessInstanceId_postgres");
        
    // oracle
    databaseSpecificLimitBeforeStatements.put("oracle", "select * from ( select a.*, ROWNUM rnum from (");
    databaseSpecificLimitAfterStatements.put("oracle", "  ) a where ROWNUM < #{lastRow}) where rnum  >= #{firstRow}");
    addDatabaseSpecificStatement("oracle", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    
    // db2
    databaseSpecificLimitBeforeStatements.put("db2", ""); // TODO!
    databaseSpecificLimitAfterStatements.put("db2", "");
    addDatabaseSpecificStatement("db2", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    
    // mssql
    databaseSpecificLimitBeforeStatements.put("mssql", ""); // TODO!
    databaseSpecificLimitAfterStatements.put("mssql", "");
    addDatabaseSpecificStatement("mssql", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
  }
  
  /** The database type. */
  protected String databaseType;
  
  /** The database table prefix. */
  protected String databaseTablePrefix = "";
  /**
   * In some situations you want to set the schema to use for table checks /
   * generation if the database metadata doesn't return that correctly, see
   * https://jira.codehaus.org/browse/ACT-1220,
   * https://jira.codehaus.org/browse/ACT-1062
   */
  protected String databaseSchema;
  
  /** The sql session factory. */
  protected SqlSessionFactory sqlSessionFactory;
  
  /** The id generator. */
  protected IdGenerator idGenerator;
  
  /** The statement mappings. */
  protected Map<String, String> statementMappings;
  
  /** The insert statements. */
  protected Map<Class<?>,String>  insertStatements = Collections.synchronizedMap(new HashMap<Class<?>, String>());
  
  /** The update statements. */
  protected Map<Class<?>,String>  updateStatements = Collections.synchronizedMap(new HashMap<Class<?>, String>());
  
  /** The delete statements. */
  protected Map<Class<?>,String>  deleteStatements = Collections.synchronizedMap(new HashMap<Class<?>, String>());
  
  /** The select statements. */
  protected Map<Class<?>,String>  selectStatements = Collections.synchronizedMap(new HashMap<Class<?>, String>());
  
  /** The is db identity used. */
  protected boolean isDbIdentityUsed = true;
  
  /** The is db history used. */
  protected boolean isDbHistoryUsed = true;

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.SessionFactory#getSessionType()
   */
  public Class< ? > getSessionType() {
    return DbSqlSession.class;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.SessionFactory#openSession()
   */
  public Session openSession() {
    return new DbSqlSession(this);
  }
  
  // insert, update and delete statements /////////////////////////////////////
  
  /**
   * Gets the insert statement.
   *
   * @param object the object
   * @return the insert statement
   */
  public String getInsertStatement(PersistentObject object) {
    return getStatement(object.getClass(), insertStatements, "insert");
  }

  /**
   * Gets the update statement.
   *
   * @param object the object
   * @return the update statement
   */
  public String getUpdateStatement(PersistentObject object) {
    return getStatement(object.getClass(), updateStatements, "update");
  }

  /**
   * Gets the delete statement.
   *
   * @param persistentObjectClass the persistent object class
   * @return the delete statement
   */
  public String getDeleteStatement(Class<?> persistentObjectClass) {
    return getStatement(persistentObjectClass, deleteStatements, "delete");
  }

  /**
   * Gets the select statement.
   *
   * @param persistentObjectClass the persistent object class
   * @return the select statement
   */
  public String getSelectStatement(Class<?> persistentObjectClass) {
    return getStatement(persistentObjectClass, selectStatements, "select");
  }

  /**
   * Gets the statement.
   *
   * @param persistentObjectClass the persistent object class
   * @param cachedStatements the cached statements
   * @param prefix the prefix
   * @return the statement
   */
  private String getStatement(Class<?> persistentObjectClass, Map<Class<?>,String> cachedStatements, String prefix) {
    String statement = cachedStatements.get(persistentObjectClass);
    if (statement!=null) {
      return statement;
    }
    statement = prefix+ClassNameUtil.getClassNameWithoutPackage(persistentObjectClass);
    statement = statement.substring(0, statement.length()-6);
    cachedStatements.put(persistentObjectClass, statement);
    return statement;
  }

  // db specific mappings /////////////////////////////////////////////////////
  
  /**
   * Adds the database specific statement.
   *
   * @param databaseType the database type
   * @param activitiStatement the activiti statement
   * @param ibatisStatement the ibatis statement
   */
  protected static void addDatabaseSpecificStatement(String databaseType, String activitiStatement, String ibatisStatement) {
    Map<String, String> specificStatements = databaseSpecificStatements.get(databaseType);
    if (specificStatements == null) {
      specificStatements = new HashMap<String, String>();
      databaseSpecificStatements.put(databaseType, specificStatements);
    }
    specificStatements.put(activitiStatement, ibatisStatement);
  }
  
  /**
   * Map statement.
   *
   * @param statement the statement
   * @return the string
   */
  public String mapStatement(String statement) {
    if (statementMappings==null) {
      return statement;
    }
    String mappedStatement = statementMappings.get(statement);
    return (mappedStatement!=null ? mappedStatement : statement);
  }
  
  // customized getters and setters ///////////////////////////////////////////
  
  /**
   * Sets the database type.
   *
   * @param databaseType the new database type
   */
  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
    this.statementMappings = databaseSpecificStatements.get(databaseType);
  }

  // getters and setters //////////////////////////////////////////////////////
  
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
   * @param sqlSessionFactory the new sql session factory
   */
  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
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
   * @param idGenerator the new id generator
   */
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
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
   * Gets the statement mappings.
   *
   * @return the statement mappings
   */
  public Map<String, String> getStatementMappings() {
    return statementMappings;
  }

  
  /**
   * Sets the statement mappings.
   *
   * @param statementMappings the statement mappings
   */
  public void setStatementMappings(Map<String, String> statementMappings) {
    this.statementMappings = statementMappings;
  }

  
  /**
   * Gets the insert statements.
   *
   * @return the insert statements
   */
  public Map<Class< ? >, String> getInsertStatements() {
    return insertStatements;
  }

  
  /**
   * Sets the insert statements.
   *
   * @param insertStatements the insert statements
   */
  public void setInsertStatements(Map<Class< ? >, String> insertStatements) {
    this.insertStatements = insertStatements;
  }

  
  /**
   * Gets the update statements.
   *
   * @return the update statements
   */
  public Map<Class< ? >, String> getUpdateStatements() {
    return updateStatements;
  }

  
  /**
   * Sets the update statements.
   *
   * @param updateStatements the update statements
   */
  public void setUpdateStatements(Map<Class< ? >, String> updateStatements) {
    this.updateStatements = updateStatements;
  }

  
  /**
   * Gets the delete statements.
   *
   * @return the delete statements
   */
  public Map<Class< ? >, String> getDeleteStatements() {
    return deleteStatements;
  }

  
  /**
   * Sets the delete statements.
   *
   * @param deleteStatements the delete statements
   */
  public void setDeleteStatements(Map<Class< ? >, String> deleteStatements) {
    this.deleteStatements = deleteStatements;
  }

  
  /**
   * Gets the select statements.
   *
   * @return the select statements
   */
  public Map<Class< ? >, String> getSelectStatements() {
    return selectStatements;
  }

  
  /**
   * Sets the select statements.
   *
   * @param selectStatements the select statements
   */
  public void setSelectStatements(Map<Class< ? >, String> selectStatements) {
    this.selectStatements = selectStatements;
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
   * Sets the database table prefix.
   *
   * @param databaseTablePrefix the new database table prefix
   */
  public void setDatabaseTablePrefix(String databaseTablePrefix) {
    this.databaseTablePrefix = databaseTablePrefix;
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

}
