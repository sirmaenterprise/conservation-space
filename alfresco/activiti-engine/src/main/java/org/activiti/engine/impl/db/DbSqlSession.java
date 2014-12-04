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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiWrongDbException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricDetailQueryImpl;
import org.activiti.engine.impl.HistoricProcessInstanceQueryImpl;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessDefinitionQueryImpl;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.upgrade.DbUpgradeStep;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.DeserializedObject;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;


// TODO: Auto-generated Javadoc
/**
 * responsibilities:
 * - delayed flushing of inserts updates and deletes
 * - optional dirty checking
 * - db specific statement name mapping.
 *
 * @author Tom Baeyens
 */
public class DbSqlSession implements Session {
  
  /** The log. */
  private static Logger log = Logger.getLogger(DbSqlSession.class.getName());

  /** The sql session. */
  protected SqlSession sqlSession;
  
  /** The db sql session factory. */
  protected DbSqlSessionFactory dbSqlSessionFactory;
  
  /** The inserted objects. */
  protected List<PersistentObject> insertedObjects = new ArrayList<PersistentObject>();
  
  /** The cached objects. */
  protected Map<Class<?>, Map<String, CachedObject>> cachedObjects = new HashMap<Class<?>, Map<String,CachedObject>>();
  
  /** The deleted objects. */
  protected List<DeleteOperation> deletedObjects = new ArrayList<DeleteOperation>();
  
  /** The deserialized objects. */
  protected List<DeserializedObject> deserializedObjects = new ArrayList<DeserializedObject>();
  
  /** The connection metadata default catalog. */
  protected String connectionMetadataDefaultCatalog = null;
  
  /** The connection metadata default schema. */
  protected String connectionMetadataDefaultSchema = null;

  /**
   * Instantiates a new db sql session.
   *
   * @param dbSqlSessionFactory the db sql session factory
   */
  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession();
  }

  /**
   * Instantiates a new db sql session.
   *
   * @param dbSqlSessionFactory the db sql session factory
   * @param connection the connection
   * @param catalog the catalog
   * @param schema the schema
   */
  public DbSqlSession(DbSqlSessionFactory dbSqlSessionFactory, Connection connection, String catalog, String schema) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    this.sqlSession = dbSqlSessionFactory
      .getSqlSessionFactory()
      .openSession(connection);
    this.connectionMetadataDefaultCatalog = catalog;
    this.connectionMetadataDefaultSchema = schema;
  }

  // insert ///////////////////////////////////////////////////////////////////
  
  /**
   * Insert.
   *
   * @param persistentObject the persistent object
   */
  public void insert(PersistentObject persistentObject) {
    if (persistentObject.getId()==null) {
      String id = dbSqlSessionFactory.getIdGenerator().getNextId();  
      persistentObject.setId(id);
    }
    insertedObjects.add(persistentObject);
    cachePut(persistentObject, false);
  }
  
  // delete ///////////////////////////////////////////////////////////////////
  
  /**
   * Delete.
   *
   * @param persistentObjectClass the persistent object class
   * @param persistentObjectId the persistent object id
   */
  public void delete(Class<?> persistentObjectClass, String persistentObjectId) {
    for (DeleteOperation deleteOperation: deletedObjects) {
      if (deleteOperation instanceof DeleteById) {
        DeleteById deleteById = (DeleteById) deleteOperation;
        if ( persistentObjectClass.equals(deleteById.persistenceObjectClass)
             && persistentObjectId.equals(deleteById.persistentObjectId)
           ) {
          // skip this delete
          return;
        }
      }
    }
    deletedObjects.add(new DeleteById(persistentObjectClass, persistentObjectId));
  }
  
  /**
   * The Interface DeleteOperation.
   */
  public interface DeleteOperation {
    
    /**
     * Execute.
     */
    void execute();
  }

  /**
   * The Class DeleteById.
   */
  public class DeleteById implements DeleteOperation {
    
    /** The persistence object class. */
    Class<?> persistenceObjectClass;
    
    /** The persistent object id. */
    String persistentObjectId;
    
    /**
     * Instantiates a new delete by id.
     *
     * @param clazz the clazz
     * @param id the id
     */
    public DeleteById(Class< ? > clazz, String id) {
      this.persistenceObjectClass = clazz;
      this.persistentObjectId = id;
    }
    
    /* (non-Javadoc)
     * @see org.activiti.engine.impl.db.DbSqlSession.DeleteOperation#execute()
     */
    public void execute() {
      String deleteStatement = dbSqlSessionFactory.getDeleteStatement(persistenceObjectClass);
      deleteStatement = dbSqlSessionFactory.mapStatement(deleteStatement);
      if (deleteStatement==null) {
        throw new ActivitiException("no delete statement for "+persistenceObjectClass+" in the ibatis mapping files");
      }
      log.fine("deleting: "+ClassNameUtil.getClassNameWithoutPackage(persistenceObjectClass)+"["+persistentObjectId+"]");
      sqlSession.delete(deleteStatement, persistentObjectId);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return "delete "+ClassNameUtil.getClassNameWithoutPackage(persistenceObjectClass)+"["+persistentObjectId+"]";
    }
  }
  
  /**
   * Delete.
   *
   * @param statement the statement
   * @param parameter the parameter
   */
  public void delete(String statement, Object parameter) {
    deletedObjects.add(new DeleteBulk(statement, parameter));
  }
  
  /**
   * The Class DeleteBulk.
   */
  public class DeleteBulk implements DeleteOperation {
    
    /** The statement. */
    String statement;
    
    /** The parameter. */
    Object parameter;
    
    /**
     * Instantiates a new delete bulk.
     *
     * @param statement the statement
     * @param parameter the parameter
     */
    public DeleteBulk(String statement, Object parameter) {
      this.statement = dbSqlSessionFactory.mapStatement(statement);
      this.parameter = parameter;
    }
    
    /* (non-Javadoc)
     * @see org.activiti.engine.impl.db.DbSqlSession.DeleteOperation#execute()
     */
    public void execute() {
      sqlSession.delete(statement, parameter);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return "bulk delete: "+statement;
    }
  }
  
  // select ///////////////////////////////////////////////////////////////////

  /**
   * Select list.
   *
   * @param statement the statement
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List selectList(String statement) {
    return selectList(statement, null, 0, Integer.MAX_VALUE);
  }
  
  /**
   * Select list.
   *
   * @param statement the statement
   * @param parameter the parameter
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter) {  
    return selectList(statement, parameter, 0, Integer.MAX_VALUE);
  }
  
  /**
   * Select list.
   *
   * @param statement the statement
   * @param parameter the parameter
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter, Page page) {   
    if(page!=null) {
      return selectList(statement, parameter, page.getFirstResult(), page.getMaxResults());
    }else {
      return selectList(statement, parameter, 0, Integer.MAX_VALUE);
    }
  }
  
  /**
   * Select list.
   *
   * @param statement the statement
   * @param parameter the parameter
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List selectList(String statement, ListQueryParameterObject parameter, Page page) {   
    return selectList(statement, parameter);
  }

  /**
   * Select list.
   *
   * @param statement the statement
   * @param parameter the parameter
   * @param firstResult the first result
   * @param maxResults the max results
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List selectList(String statement, Object parameter, int firstResult, int maxResults) {   
    return selectList(statement, new ListQueryParameterObject(parameter, firstResult, maxResults));
  }
  
  /**
   * Select list.
   *
   * @param statement the statement
   * @param parameter the parameter
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List selectList(String statement, ListQueryParameterObject parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);    
    if(parameter.firstResult == -1 ||  parameter.maxResults==-1) {
      return Collections.EMPTY_LIST;
    }
    List loadedObjects = null;
    String databaseType = dbSqlSessionFactory.databaseType;
    if(databaseType.equals("mssql") || databaseType.equals("db2")) {
      // use mybatis paging (native database paging not yet implemented)
      loadedObjects = sqlSession.selectList(statement, parameter, new RowBounds(parameter.getFirstResult(), parameter.getMaxResults()));
    } else {
      // use native database paging
      loadedObjects = sqlSession.selectList(statement, parameter);
    }
    return filterLoadedObjects(loadedObjects);
  }

  /**
   * Select one.
   *
   * @param statement the statement
   * @param parameter the parameter
   * @return the object
   */
  public Object selectOne(String statement, Object parameter) {
    statement = dbSqlSessionFactory.mapStatement(statement);
    Object result = sqlSession.selectOne(statement, parameter);
    if (result instanceof PersistentObject) {
      PersistentObject loadedObject = (PersistentObject) result;
      result = cacheFilter(loadedObject);
    }
    return result;
  }
  
  /**
   * Select by id.
   *
   * @param <T> the generic type
   * @param entityClass the entity class
   * @param id the id
   * @return the t
   */
  @SuppressWarnings("unchecked")
  public <T extends PersistentObject> T selectById(Class<T> entityClass, String id) {
    T persistentObject = cacheGet(entityClass, id);
    if (persistentObject!=null) {
      return persistentObject;
    }
    String selectStatement = dbSqlSessionFactory.getSelectStatement(entityClass);
    selectStatement = dbSqlSessionFactory.mapStatement(selectStatement);
    persistentObject = (T) sqlSession.selectOne(selectStatement, id);
    if (persistentObject==null) {
      return null;
    }
    cachePut(persistentObject, true);
    return persistentObject;
  }

  // internal session cache ///////////////////////////////////////////////////
  
  /**
   * Filter loaded objects.
   *
   * @param loadedObjects the loaded objects
   * @return the list
   */
  @SuppressWarnings("unchecked")
  protected List filterLoadedObjects(List<Object> loadedObjects) {
    if (loadedObjects.isEmpty()) {
      return loadedObjects;
    }
    if (! (PersistentObject.class.isAssignableFrom(loadedObjects.get(0).getClass()))) {
      return loadedObjects;
    }
    List<PersistentObject> filteredObjects = new ArrayList<PersistentObject>(loadedObjects.size());
    for (Object loadedObject: loadedObjects) {
      PersistentObject cachedPersistentObject = cacheFilter((PersistentObject) loadedObject);
      filteredObjects.add(cachedPersistentObject);
    }
    return filteredObjects;
  }

  /**
   * Cache put.
   *
   * @param persistentObject the persistent object
   * @param storeState the store state
   * @return the cached object
   */
  protected CachedObject cachePut(PersistentObject persistentObject, boolean storeState) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObject.getClass());
    if (classCache==null) {
      classCache = new HashMap<String, CachedObject>();
      cachedObjects.put(persistentObject.getClass(), classCache);
    }
    CachedObject cachedObject = new CachedObject(persistentObject, storeState);
    classCache.put(persistentObject.getId(), cachedObject);
    return cachedObject;
  }
  
  /**
   * returns the object in the cache.  if this object was loaded before,
   * then the original object is returned.  if this is the first time
   * this object is loaded, then the loadedObject is added to the cache.
   *
   * @param persistentObject the persistent object
   * @return the persistent object
   */
  protected PersistentObject cacheFilter(PersistentObject persistentObject) {
    PersistentObject cachedPersistentObject = cacheGet(persistentObject.getClass(), persistentObject.getId());
    if (cachedPersistentObject!=null) {
      return cachedPersistentObject;
    }
    cachePut(persistentObject, true);
    return persistentObject;
  }

  /**
   * Cache get.
   *
   * @param <T> the generic type
   * @param entityClass the entity class
   * @param id the id
   * @return the t
   */
  @SuppressWarnings("unchecked")
  protected <T> T cacheGet(Class<T> entityClass, String id) {
    CachedObject cachedObject = null;
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache!=null) {
      cachedObject = classCache.get(id);
    }
    if (cachedObject!=null) {
      return (T) cachedObject.getPersistentObject();
    }
    return null;
  }
  
  /**
   * Cache remove.
   *
   * @param persistentObjectClass the persistent object class
   * @param persistentObjectId the persistent object id
   */
  protected void cacheRemove(Class<?> persistentObjectClass, String persistentObjectId) {
    Map<String, CachedObject> classCache = cachedObjects.get(persistentObjectClass);
    if (classCache==null) {
      return;
    }
    classCache.remove(persistentObjectId);
  }
  
  /**
   * Find in cache.
   *
   * @param <T> the generic type
   * @param entityClass the entity class
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> findInCache(Class<T> entityClass) {
    Map<String, CachedObject> classCache = cachedObjects.get(entityClass);
    if (classCache!=null) {
      ArrayList<T> entities = new ArrayList<T>(classCache.size());
      for (CachedObject cachedObject: classCache.values()) {
        entities.add((T) cachedObject.getPersistentObject());
      }
      return entities;
    }
    return Collections.emptyList();
  }

  /**
   * The Class CachedObject.
   */
  public static class CachedObject {
    
    /** The persistent object. */
    protected PersistentObject persistentObject;
    
    /** The persistent object state. */
    protected Object persistentObjectState;
    
    /**
     * Instantiates a new cached object.
     *
     * @param persistentObject the persistent object
     * @param storeState the store state
     */
    public CachedObject(PersistentObject persistentObject, boolean storeState) {
      this.persistentObject = persistentObject;
      if (storeState) {
        this.persistentObjectState = persistentObject.getPersistentState();
      }
    }

    /**
     * Gets the persistent object.
     *
     * @return the persistent object
     */
    public PersistentObject getPersistentObject() {
      return persistentObject;
    }

    /**
     * Gets the persistent object state.
     *
     * @return the persistent object state
     */
    public Object getPersistentObjectState() {
      return persistentObjectState;
    }
  }

  // deserialized objects /////////////////////////////////////////////////////
  
  /**
   * Adds the deserialized object.
   *
   * @param deserializedObject the deserialized object
   * @param serializedBytes the serialized bytes
   * @param variableInstanceEntity the variable instance entity
   */
  public void addDeserializedObject(Object deserializedObject, byte[] serializedBytes, VariableInstanceEntity variableInstanceEntity) {
    deserializedObjects.add(new DeserializedObject(deserializedObject, serializedBytes, variableInstanceEntity));
  }

  // flush ////////////////////////////////////////////////////////////////////

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Session#flush()
   */
  public void flush() {
    removeUnnecessaryOperations();
    flushDeserializedObjects();
    List<PersistentObject> updatedObjects = getUpdatedObjects();
    
    if (log.isLoggable(Level.FINE)) {
      log.fine("flush summary:");
      for (PersistentObject insertedObject: insertedObjects) {
        log.fine("  insert "+toString(insertedObject));
      }
      for (PersistentObject updatedObject: updatedObjects) {
        log.fine("  update "+toString(updatedObject));
      }
      for (Object deleteOperation: deletedObjects) {
        log.fine("  "+deleteOperation);
      }
      log.fine("now executing flush...");
    }

    flushInserts();
    flushUpdates(updatedObjects);
    flushDeletes();
  }

  /**
   * Removes the unnecessary operations.
   */
  protected void removeUnnecessaryOperations() {
    List<DeleteOperation> deletedObjectsCopy = new ArrayList<DeleteOperation>(deletedObjects);
    // for all deleted objects
    for (DeleteOperation deleteOperation: deletedObjectsCopy) {
      if (deleteOperation instanceof DeleteById) {
        DeleteById deleteById = (DeleteById) deleteOperation;
        PersistentObject insertedObject = findInsertedObject(deleteById.persistenceObjectClass, deleteById.persistentObjectId);
        // if the deleted object is inserted,
        if (insertedObject!=null) {
          // remove the insert and the delete
          insertedObjects.remove(insertedObject);
          deletedObjects.remove(deleteOperation);
        }
        // in any case, remove the deleted object from the cache
        cacheRemove(deleteById.persistenceObjectClass, deleteById.persistentObjectId);
      }
    }
    for (PersistentObject insertedObject: insertedObjects) {
      cacheRemove(insertedObject.getClass(), insertedObject.getId());
    }
  }

  /**
   * Find inserted object.
   *
   * @param persistenceObjectClass the persistence object class
   * @param persistentObjectId the persistent object id
   * @return the persistent object
   */
  protected PersistentObject findInsertedObject(Class< ? > persistenceObjectClass, String persistentObjectId) {
    for (PersistentObject insertedObject: insertedObjects) {
      if ( insertedObject.getClass().equals(persistenceObjectClass)
           && insertedObject.getId().equals(persistentObjectId)
         ) {
        return insertedObject;
      }
    }
    return null;
  }

  /**
   * Flush deserialized objects.
   */
  protected void flushDeserializedObjects() {
    for (DeserializedObject deserializedObject: deserializedObjects) {
      deserializedObject.flush();
    }
  }

  /**
   * Gets the updated objects.
   *
   * @return the updated objects
   */
  public List<PersistentObject> getUpdatedObjects() {
    List<PersistentObject> updatedObjects = new ArrayList<PersistentObject>();
    for (Class<?> clazz: cachedObjects.keySet()) {
      Map<String, CachedObject> classCache = cachedObjects.get(clazz);
      for (CachedObject cachedObject: classCache.values()) {
        PersistentObject persistentObject = (PersistentObject) cachedObject.getPersistentObject();
        if (!deletedObjects.contains(persistentObject)) {
          Object originalState = cachedObject.getPersistentObjectState();
          if (!originalState.equals(persistentObject.getPersistentState())) {
            updatedObjects.add(persistentObject);
          } else {
            log.finest("loaded object '"+persistentObject+"' was not updated");
          }
        }
      }
    }
    return updatedObjects;
  }
  
  /**
   * Prune deleted entities.
   *
   * @param <T> the generic type
   * @param listToPrune the list to prune
   * @return the list
   */
  public <T extends PersistentObject> List<T> pruneDeletedEntities(List<T> listToPrune) {   
    ArrayList<T> prunedList = new ArrayList<T>(listToPrune);
    for (T potentiallyDeleted : listToPrune) {
      for (DeleteOperation deleteOperation: deletedObjects) {
        if (deleteOperation instanceof DeleteById) {
          DeleteById deleteById = (DeleteById) deleteOperation;
          if ( potentiallyDeleted.getClass().equals(deleteById.persistenceObjectClass)
               && potentiallyDeleted.getId().equals(deleteById.persistentObjectId)
             ) {            
            prunedList.remove(potentiallyDeleted);
          }
        }
      }
    }
    return prunedList;
  }

  /**
   * Flush inserts.
   */
  protected void flushInserts() {
    for (PersistentObject insertedObject: insertedObjects) {
      String insertStatement = dbSqlSessionFactory.getInsertStatement(insertedObject);
      insertStatement = dbSqlSessionFactory.mapStatement(insertStatement);

      if (insertStatement==null) {
        throw new ActivitiException("no insert statement for "+insertedObject.getClass()+" in the ibatis mapping files");
      }
      
      log.fine("inserting: "+toString(insertedObject));
      sqlSession.insert(insertStatement, insertedObject);
    }
    insertedObjects.clear();
  }

  /**
   * Flush updates.
   *
   * @param updatedObjects the updated objects
   */
  protected void flushUpdates(List<PersistentObject> updatedObjects) {
    for (PersistentObject updatedObject: updatedObjects) {
      String updateStatement = dbSqlSessionFactory.getUpdateStatement(updatedObject);
      updateStatement = dbSqlSessionFactory.mapStatement(updateStatement);
      if (updateStatement==null) {
        throw new ActivitiException("no update statement for "+updatedObject.getClass()+" in the ibatis mapping files");
      }
      log.fine("updating: "+toString(updatedObject)+"]");
      int updatedRecords = sqlSession.update(updateStatement, updatedObject);
      if (updatedRecords!=1) {
        throw new ActivitiOptimisticLockingException(toString(updatedObject)+" was updated by another transaction concurrently");
      }
    }
    updatedObjects.clear();
  }

  /**
   * Flush deletes.
   */
  protected void flushDeletes() {
    for (DeleteOperation delete: deletedObjects) {
      log.fine("executing: "+delete);
      delete.execute();
    }
    deletedObjects.clear();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.interceptor.Session#close()
   */
  public void close() {
    sqlSession.close();
  }

  /**
   * Commit.
   */
  public void commit() {
    sqlSession.commit();
  }

  /**
   * Rollback.
   */
  public void rollback() {
    sqlSession.rollback();
  }

  /**
   * To string.
   *
   * @param persistentObject the persistent object
   * @return the string
   */
  protected String toString(PersistentObject persistentObject) {
    if (persistentObject==null) {
      return "null";
    }
    return ClassNameUtil.getClassNameWithoutPackage(persistentObject)+"["+persistentObject.getId()+"]";
  }
  
  // schema operations ////////////////////////////////////////////////////////
  
  
  /**
   * Db schema check version.
   */
  public void dbSchemaCheckVersion() {
    try {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new ActivitiWrongDbException(ProcessEngine.VERSION, dbVersion);
      }

      String errorMessage = null;
      if (!isEngineTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "engine");
      }
      if (dbSqlSessionFactory.isDbHistoryUsed() && !isHistoryTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "history");
      }
      if (dbSqlSessionFactory.isDbIdentityUsed() && !isIdentityTablePresent()) {
        errorMessage = addMissingComponent(errorMessage, "identity");
      }
      
      Integer configuredHistoryLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
      PropertyEntity historyLevelProperty = selectById(PropertyEntity.class, "historyLevel");
      if (historyLevelProperty==null) {
        if (errorMessage==null) {
          errorMessage = "";
        }
        errorMessage += "no historyLevel property specified";
      } else {
        Integer databaseHistoryLevel = new Integer(historyLevelProperty.getValue());
        if (!configuredHistoryLevel.equals(databaseHistoryLevel)) {
          if (errorMessage==null) {
            errorMessage = "";
          }
          errorMessage += "historyLevel mismatch: configuration says "+configuredHistoryLevel+" and database says "+databaseHistoryLevel;
        }
      }
      
      if (errorMessage!=null) {
        throw new ActivitiException("Activiti database problem: "+errorMessage);
      }

    } catch (Exception e) {
      if (isMissingTablesException(e)) {
        throw new ActivitiException("no activiti tables in db.  set <property name=\"databaseSchemaUpdate\" to value=\"true\" or value=\"create-drop\" (use create-drop for testing only!) in bean processEngineConfiguration in activiti.cfg.xml for automatic schema creation", e);
      } else {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        } else {
          throw new ActivitiException("couldn't get db schema version", e);
        }
      }
    }

    log.fine("activiti db schema check successful");
  }

  /**
   * Adds the missing component.
   *
   * @param missingComponents the missing components
   * @param component the component
   * @return the string
   */
  protected String addMissingComponent(String missingComponents, String component) {
    if (missingComponents==null) {
      return "Tables missing for component(s) "+component;
    }
    return missingComponents+", "+component;
  }

  /**
   * Gets the db version.
   *
   * @return the db version
   */
  protected String getDbVersion() {
    String selectSchemaVersionStatement = dbSqlSessionFactory.mapStatement("selectDbSchemaVersion");
    return (String) sqlSession.selectOne(selectSchemaVersionStatement);
  }

  /**
   * Db schema create.
   */
  public void dbSchemaCreate() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    
    int configuredHistoryLevel = processEngineConfiguration.getHistoryLevel();
    if ( (!processEngineConfiguration.isDbHistoryUsed())
         && (configuredHistoryLevel>ProcessEngineConfigurationImpl.HISTORYLEVEL_NONE)
       ) {
      throw new ActivitiException("historyLevel config is higher then 'none' and dbHistoryUsed is set to false");
    }

    if (isEngineTablePresent()) {
      String dbVersion = getDbVersion();
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new ActivitiWrongDbException(ProcessEngine.VERSION, dbVersion);
      }
    } else {
      dbSchemaCreateEngine();
    }

    if (processEngineConfiguration.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }

    if (processEngineConfiguration.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }
  }

  /**
   * Db schema create identity.
   */
  protected void dbSchemaCreateIdentity() {
    executeMandatorySchemaResource("create", "identity");
  }

  /**
   * Db schema create history.
   */
  protected void dbSchemaCreateHistory() {
    executeMandatorySchemaResource("create", "history");
  }

  /**
   * Db schema create engine.
   */
  protected void dbSchemaCreateEngine() {
    executeMandatorySchemaResource("create", "engine");
    
    int configuredHistoryLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
    PropertyEntity property = new PropertyEntity("historyLevel", Integer.toString(configuredHistoryLevel));
    insert(property);
  }

  /**
   * Db schema drop.
   */
  public void dbSchemaDrop() {
    executeMandatorySchemaResource("drop", "engine");
    if (dbSqlSessionFactory.isDbHistoryUsed()) {
      executeMandatorySchemaResource("drop", "history");
    }
    if (dbSqlSessionFactory.isDbIdentityUsed()) {
      executeMandatorySchemaResource("drop", "identity");
    }
  }

  /**
   * Db schema prune.
   */
  public void dbSchemaPrune() {
    if (isHistoryTablePresent() && !dbSqlSessionFactory.isDbHistoryUsed()) {
      executeMandatorySchemaResource("drop", "history");
    }
    if (isIdentityTablePresent() && dbSqlSessionFactory.isDbIdentityUsed()) {
      executeMandatorySchemaResource("drop", "identity");
    }
  }

  /**
   * Execute mandatory schema resource.
   *
   * @param operation the operation
   * @param component the component
   */
  public void executeMandatorySchemaResource(String operation, String component) {
    executeSchemaResource(operation, component, getResourceForDbOperation(operation, operation, component), false);
  }

  /** The jdbc metadata table types. */
  public static String[] JDBC_METADATA_TABLE_TYPES = {"TABLE"};

  /**
   * Db schema update.
   *
   * @return the string
   */
  public String dbSchemaUpdate() {
    String feedback = null;
    String dbVersion = null;
    boolean isUpgradeNeeded = false;
      
    if (isEngineTablePresent()) {
      // the next piece assumes both DB version and library versions are formatted 5.x
      PropertyEntity dbVersionProperty = selectById(PropertyEntity.class, "schema.version");
      dbVersion = dbVersionProperty.getValue();
      isUpgradeNeeded = !ProcessEngine.VERSION.equals(dbVersion);
      
      if (isUpgradeNeeded) {
        dbVersionProperty.setValue(ProcessEngine.VERSION);

        PropertyEntity dbHistoryProperty;
        if ("5.0".equals(dbVersion)) {
          dbHistoryProperty = new PropertyEntity("schema.history", "create(5.0)");
          insert(dbHistoryProperty);
        } else {
          dbHistoryProperty = selectById(PropertyEntity.class, "schema.history");
        }
        
        String dbHistoryValue = dbHistoryProperty.getValue()+" upgrade("+dbVersion+"->"+ProcessEngine.VERSION+")";
        dbHistoryProperty.setValue(dbHistoryValue);
        
        dbSchemaUpgrade("engine", dbVersion);

        feedback = "upgraded Activiti from "+dbVersion+" to "+ProcessEngine.VERSION;
      }
    } else {
      dbSchemaCreateEngine();
    }
    
    if (isHistoryTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("history", dbVersion);
      }
    } else if (dbSqlSessionFactory.isDbHistoryUsed()) {
      dbSchemaCreateHistory();
    }
    
    if (isIdentityTablePresent()) {
      if (isUpgradeNeeded) {
        dbSchemaUpgrade("identity", dbVersion);
      }
    } else if (dbSqlSessionFactory.isDbIdentityUsed()) {
      dbSchemaCreateIdentity();
    }
    
    return feedback;
  }

  /**
   * Checks if is engine table present.
   *
   * @return true, if is engine table present
   */
  public boolean isEngineTablePresent(){
    return isTablePresent("ACT_RU_EXECUTION");
  }
  
  /**
   * Checks if is history table present.
   *
   * @return true, if is history table present
   */
  public boolean isHistoryTablePresent(){
    return isTablePresent("ACT_HI_PROCINST");
  }
  
  /**
   * Checks if is identity table present.
   *
   * @return true, if is identity table present
   */
  public boolean isIdentityTablePresent(){
    return isTablePresent("ACT_ID_USER");
  }

  /**
   * Checks if is table present.
   *
   * @param tableName the table name
   * @return true, if is table present
   */
  public boolean isTablePresent(String tableName) {
    tableName = prependDatabaseTablePrefix(tableName);
    Connection connection = null;
    try {
      connection = sqlSession.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      ResultSet tables = null;
      
      String schema = this.connectionMetadataDefaultSchema;
      if (dbSqlSessionFactory.getDatabaseSchema()!=null) {
        schema = dbSqlSessionFactory.getDatabaseSchema();
      }
      
      String databaseType = dbSqlSessionFactory.getDatabaseType();
      
      if ("postgres".equals(databaseType)) {
        tableName = tableName.toLowerCase();
      }
      
      try {
        tables = databaseMetaData.getTables(this.connectionMetadataDefaultCatalog, schema, tableName, JDBC_METADATA_TABLE_TYPES);
        return tables.next();
      } finally {
        tables.close();
      }
      
    } catch (Exception e) {
      throw new ActivitiException("couldn't check if tables are already present using metadata: "+e.getMessage(), e); 
    }
  }

  /**
   * Prepend database table prefix.
   *
   * @param tableName the table name
   * @return the string
   */
  protected String prependDatabaseTablePrefix(String tableName) {
    return dbSqlSessionFactory.getDatabaseTablePrefix() + tableName;    
  }

  /**
   * Db schema upgrade.
   *
   * @param component the component
   * @param dbVersion the db version
   */
  protected void dbSchemaUpgrade(String component, String dbVersion) {
    log.info("upgrading activiti "+component+" schema from "+dbVersion+" to "+ProcessEngine.VERSION);
    
    if (dbVersion.endsWith("-SNAPSHOT")) {
      dbVersion = dbVersion.substring(0, dbVersion.length()-"-SNAPSHOT".length());
    }
    int minorDbVersionNumber = Integer.parseInt(dbVersion.substring(2));
    
    String libraryVersion = ProcessEngine.VERSION;
    if (ProcessEngine.VERSION.endsWith("-SNAPSHOT")) {
      libraryVersion = ProcessEngine.VERSION.substring(0, ProcessEngine.VERSION.length()-"-SNAPSHOT".length());
    }
    int minorLibraryVersionNumber = Integer.parseInt(libraryVersion.substring(2));
    
    while (minorDbVersionNumber<minorLibraryVersionNumber) {
      executeSchemaResource("upgrade", component, getResourceForDbOperation("upgrade", "upgradestep.5"+minorDbVersionNumber+".to.5"+(minorDbVersionNumber+1), component), true);
      minorDbVersionNumber++;
    }
  }

  /**
   * Gets the resource for db operation.
   *
   * @param directory the directory
   * @param operation the operation
   * @param component the component
   * @return the resource for db operation
   */
  public String getResourceForDbOperation(String directory, String operation, String component) {
    String databaseType = dbSqlSessionFactory.getDatabaseType();
    return "org/activiti/db/" + directory + "/activiti." + databaseType + "." + operation + "."+component+".sql";
  }

  /**
   * Execute schema resource.
   *
   * @param operation the operation
   * @param component the component
   * @param resourceName the resource name
   * @param isOptional the is optional
   */
  public void executeSchemaResource(String operation, String component, String resourceName, boolean isOptional) {
    InputStream inputStream = null;
    try {
      inputStream = ReflectUtil.getResourceAsStream(resourceName);
      if (inputStream == null) {
        if (isOptional) {
          log.fine("no schema resource "+resourceName+" for "+operation);
        } else {
          throw new ActivitiException("resource '" + resourceName + "' is not available");
        }
      } else {
        executeSchemaResource(operation, component, resourceName, inputStream);
      }

    } finally {
      IoUtil.closeSilently(inputStream);
    }
  }

  /**
   * Execute schema resource.
   *
   * @param operation the operation
   * @param component the component
   * @param resourceName the resource name
   * @param inputStream the input stream
   */
  private void executeSchemaResource(String operation, String component, String resourceName, InputStream inputStream) {
    log.info("performing "+operation+" on "+component+" with resource "+resourceName);
    String sqlStatement = null;
    String exceptionSqlStatement = null;
    try {
      Connection connection = sqlSession.getConnection();
      Exception exception = null;
      byte[] bytes = IoUtil.readInputStream(inputStream, resourceName);
      String ddlStatements = new String(bytes);
      BufferedReader reader = new BufferedReader(new StringReader(ddlStatements));
      String line = readNextTrimmedLine(reader);
      while (line != null) {
        if (line.startsWith("# ")) {
          log.fine(line.substring(2));
          
        } else if (line.startsWith("-- ")) {
          log.fine(line.substring(3));
          
        } else if (line.startsWith("execute java ")) {
          String upgradestepClassName = line.substring(13).trim();
          DbUpgradeStep dbUpgradeStep = null;
          try {
            dbUpgradeStep = (DbUpgradeStep) ReflectUtil.instantiate(upgradestepClassName);
          } catch (ActivitiException e) {
            throw new ActivitiException("database update java class '"+upgradestepClassName+"' can't be instantiated: "+e.getMessage(), e);
          }
          try {
            dbUpgradeStep.execute(this);
          } catch (Exception e) {
            throw new ActivitiException("error while executing database update java class '"+upgradestepClassName+"': "+e.getMessage(), e);
          }
          
        } else if (line.length()>0) {
          
          if (line.endsWith(";")) {
            sqlStatement = addSqlStatementPiece(sqlStatement, line.substring(0, line.length()-1));
            Statement jdbcStatement = connection.createStatement();
            try {
              
              log.fine("Executing statement: " + sqlStatement);
              
              jdbcStatement.execute(sqlStatement);
              jdbcStatement.close();
            } catch (Exception e) {
              if (exception == null) {
                exception = e;
                exceptionSqlStatement = sqlStatement;
              }
              log.log(Level.SEVERE, "problem during schema " + operation + ", statement '" + sqlStatement, e);
            } finally {
              sqlStatement = null; 
            }
          } else {
            sqlStatement = addSqlStatementPiece(sqlStatement, line);
          }
        }
        
        line = readNextTrimmedLine(reader);
      }

      if (exception != null) {
        throw exception;
      }
      
      log.fine("activiti db schema " + operation + " for component "+component+" successful");
      
    } catch (Exception e) {
      throw new ActivitiException("couldn't "+operation+" db schema: "+exceptionSqlStatement, e);
    }
  }

  /**
   * Adds the sql statement piece.
   *
   * @param sqlStatement the sql statement
   * @param line the line
   * @return the string
   */
  protected String addSqlStatementPiece(String sqlStatement, String line) {
    if (sqlStatement==null) {
      return line;
    }
    return sqlStatement + " \n" + line;
  }
  
  /**
   * Read next trimmed line.
   *
   * @param reader the reader
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected String readNextTrimmedLine(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (line!=null) {
      line = line.trim();
    }
    return line;
  }
  
  /**
   * Checks if is missing tables exception.
   *
   * @param e the e
   * @return true, if is missing tables exception
   */
  protected boolean isMissingTablesException(Exception e) {
    String exceptionMessage = e.getMessage();
    if(e.getMessage() != null) {      
      // Matches message returned from H2
      if ((exceptionMessage.indexOf("Table") != -1) && (exceptionMessage.indexOf("not found") != -1)) {
        return true;
      }
      
      // Message returned from MySQL and Oracle
      if (((exceptionMessage.indexOf("Table") != -1 || exceptionMessage.indexOf("table") != -1)) && (exceptionMessage.indexOf("doesn't exist") != -1)) {
        return true;
      }
      
      // Message returned from Postgres
      if (((exceptionMessage.indexOf("relation") != -1 || exceptionMessage.indexOf("table") != -1)) && (exceptionMessage.indexOf("does not exist") != -1)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Perform schema operations process engine build.
   */
  public void performSchemaOperationsProcessEngineBuild() {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    if (ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)) {
      try {
        dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if ( org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate) 
         || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)
         || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(databaseSchemaUpdate)
       ) {
      dbSchemaCreate();
      
    } else if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE.equals(databaseSchemaUpdate)) {
      dbSchemaCheckVersion();
      
    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE.equals(databaseSchemaUpdate)) {
      dbSchemaUpdate();
    }
  }

  /**
   * Perform schema operations process engine close.
   */
  public void performSchemaOperationsProcessEngineClose() {
    String databaseSchemaUpdate = Context.getProcessEngineConfiguration().getDatabaseSchemaUpdate();
    if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(databaseSchemaUpdate)) {
      dbSchemaDrop();
    }
  }

  // query factory methods ////////////////////////////////////////////////////  

  /**
   * Creates the deployment query.
   *
   * @return the deployment query impl
   */
  public DeploymentQueryImpl createDeploymentQuery() {
    return new DeploymentQueryImpl();
  }
  
  /**
   * Creates the process definition query.
   *
   * @return the process definition query impl
   */
  public ProcessDefinitionQueryImpl createProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl();
  }
  
  /**
   * Creates the process instance query.
   *
   * @return the process instance query impl
   */
  public ProcessInstanceQueryImpl createProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl();
  }
  
  /**
   * Creates the execution query.
   *
   * @return the execution query impl
   */
  public ExecutionQueryImpl createExecutionQuery() {
    return new ExecutionQueryImpl();
  }
  
  /**
   * Creates the task query.
   *
   * @return the task query impl
   */
  public TaskQueryImpl createTaskQuery() {
    return new TaskQueryImpl();
  }
  
  /**
   * Creates the job query.
   *
   * @return the job query impl
   */
  public JobQueryImpl createJobQuery() {
    return new JobQueryImpl();
  }
  
  /**
   * Creates the historic process instance query.
   *
   * @return the historic process instance query impl
   */
  public HistoricProcessInstanceQueryImpl createHistoricProcessInstanceQuery() {
    return new HistoricProcessInstanceQueryImpl();
  }
  
  /**
   * Creates the historic activity instance query.
   *
   * @return the historic activity instance query impl
   */
  public HistoricActivityInstanceQueryImpl createHistoricActivityInstanceQuery() {
    return new HistoricActivityInstanceQueryImpl();
  }
  
  /**
   * Creates the historic task instance query.
   *
   * @return the historic task instance query impl
   */
  public HistoricTaskInstanceQueryImpl createHistoricTaskInstanceQuery() {
    return new HistoricTaskInstanceQueryImpl();
  }
  
  /**
   * Creates the historic detail query.
   *
   * @return the historic detail query impl
   */
  public HistoricDetailQueryImpl createHistoricDetailQuery() {
    return new HistoricDetailQueryImpl();
  }
  
  /**
   * Creates the user query.
   *
   * @return the user query impl
   */
  public UserQueryImpl createUserQuery() {
    return new UserQueryImpl();
  }
  
  /**
   * Creates the group query.
   *
   * @return the group query impl
   */
  public GroupQueryImpl createGroupQuery() {
    return new GroupQueryImpl();
  }

  // getters and setters //////////////////////////////////////////////////////
  
  /**
   * Gets the sql session.
   *
   * @return the sql session
   */
  public SqlSession getSqlSession() {
    return sqlSession;
  }
  
  /**
   * Gets the db sql session factory.
   *
   * @return the db sql session factory
   */
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }
}
