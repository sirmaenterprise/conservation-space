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

package org.activiti.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.AbstractManager;


// TODO: Auto-generated Javadoc
/**
 * The Class EventSubscriptionManager.
 *
 * @author Daniel Meyer
 */
public class EventSubscriptionManager extends AbstractManager {
  
  /** keep track of subscriptions created in the current command. */
  protected List<SignalEventSubscriptionEntity> createdSignalSubscriptions = new ArrayList<SignalEventSubscriptionEntity>();
  
  /**
   * Insert.
   *
   * @param persistentObject the persistent object
   */
  public void insert(EventSubscriptionEntity persistentObject) {
    super.insert(persistentObject);
    if(persistentObject instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.add((SignalEventSubscriptionEntity)persistentObject);
    }
  }
  
  /**
   * Delete event subscription.
   *
   * @param persistentObject the persistent object
   */
  public void deleteEventSubscription(EventSubscriptionEntity persistentObject) {
    getDbSqlSession().delete(persistentObject.getClass(), persistentObject.getId());
    if(persistentObject instanceof SignalEventSubscriptionEntity) {
      createdSignalSubscriptions.remove(persistentObject);
    }
  }
    
  /**
   * Find event subscriptionby id.
   *
   * @param id the id
   * @return the event subscription entity
   */
  public EventSubscriptionEntity findEventSubscriptionbyId(String id) {
    return (EventSubscriptionEntity) getDbSqlSession().selectOne("selectEventSubscription", id);
  }

  /**
   * Find event subscription count by query criteria.
   *
   * @param eventSubscriptionQueryImpl the event subscription query impl
   * @return the long
   */
  public long findEventSubscriptionCountByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl) {
    final String query = "selectEventSubscriptionCountByQueryCriteria"; 
    return (Long) getDbSqlSession().selectOne(query, eventSubscriptionQueryImpl);
  }

  /**
   * Find event subscriptions by query criteria.
   *
   * @param eventSubscriptionQueryImpl the event subscription query impl
   * @param page the page
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<EventSubscriptionEntity> findEventSubscriptionsByQueryCriteria(EventSubscriptionQueryImpl eventSubscriptionQueryImpl, Page page) {
    final String query = "selectEventSubscriptionByQueryCriteria"; 
    return getDbSqlSession().selectList(query, eventSubscriptionQueryImpl, page);
  }

  /**
   * Find signal event subscriptions by event name.
   *
   * @param eventName the event name
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByEventName(String eventName) {
    final String query = "selectSignalEventSubscriptionsByEventName";    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbSqlSession().selectList(query, eventName));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(eventName.equals(entity.getEventName())) {
        selectList.add(entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }
  
  /**
   * Find signal event subscriptions by execution.
   *
   * @param executionId the execution id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByExecution(String executionId) {
    final String query = "selectSignalEventSubscriptionsByExecution";    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbSqlSession().selectList(query, executionId));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(executionId.equals(entity.getExecutionId())) {
        selectList.add((SignalEventSubscriptionEntity) entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }
  
  /**
   * Find signal event subscriptions by name and execution.
   *
   * @param name the name
   * @param executionId the execution id
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<SignalEventSubscriptionEntity> findSignalEventSubscriptionsByNameAndExecution(String name, String executionId) {
    final String query = "selectSignalEventSubscriptionsByNameAndExecution";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventName", name);    
    Set<SignalEventSubscriptionEntity> selectList = new HashSet<SignalEventSubscriptionEntity>( getDbSqlSession().selectList(query, params));
    
    // add events created in this command (not visible yet in query)
    for (SignalEventSubscriptionEntity entity : createdSignalSubscriptions) {
      if(executionId.equals(entity.getExecutionId())
         && name.equals(entity.getEventName())) {
        selectList.add((SignalEventSubscriptionEntity) entity);        
      }
    }
    
    return new ArrayList<SignalEventSubscriptionEntity>(selectList);
  }

  /**
   * Find event subscriptions by execution and type.
   *
   * @param executionId the execution id
   * @param type the type
   * @return the list
   */
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecutionAndType(String executionId, String type) {
    final String query = "selectEventSubscriptionsByExecutionAndType";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);    
    return getDbSqlSession().selectList(query, params);    
  }
  
  /**
   * Find event subscriptions by execution.
   *
   * @param executionId the execution id
   * @return the list
   */
  public List<EventSubscriptionEntity> findEventSubscriptionsByExecution(String executionId) {
    final String query = "selectEventSubscriptionsByExecution";    
    return getDbSqlSession().selectList(query, executionId);    
  }
  
  /**
   * Find event subscriptions.
   *
   * @param executionId the execution id
   * @param type the type
   * @param activityId the activity id
   * @return the list
   */
  public List<EventSubscriptionEntity> findEventSubscriptions(String executionId, String type, String activityId) {
    final String query = "selectEventSubscriptionsByExecutionTypeAndActivity";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("executionId", executionId);
    params.put("eventType", type);
    params.put("activityId", activityId);
    return getDbSqlSession().selectList(query, params);            
  }

  /**
   * Find event subscriptions by configuration.
   *
   * @param type the type
   * @param configuration the configuration
   * @return the list
   */
  public List<EventSubscriptionEntity> findEventSubscriptionsByConfiguration(String type, String configuration) {
    final String query = "selectEventSubscriptionsByConfiguration";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("configuration", configuration);
    return getDbSqlSession().selectList(query, params);            
  }

  /**
   * Find event subscriptions by name.
   *
   * @param type the type
   * @param eventName the event name
   * @return the list
   */
  public List<EventSubscriptionEntity> findEventSubscriptionsByName(String type, String eventName) {
    final String query = "selectEventSubscriptionsByName";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);    
    return getDbSqlSession().selectList(query, params);            
  }
  
  /**
   * Find event subscriptions by name and execution.
   *
   * @param type the type
   * @param eventName the event name
   * @param executionId the execution id
   * @return the list
   */
  public List<EventSubscriptionEntity> findEventSubscriptionsByNameAndExecution(String type, String eventName, String executionId) {
    final String query = "selectEventSubscriptionsByNameAndExecution";    
    Map<String,String> params = new HashMap<String, String>();
    params.put("eventType", type);
    params.put("eventName", eventName);
    params.put("executionId", executionId);    
    return getDbSqlSession().selectList(query, params);            
  }

  /**
   * Find message start event subscription by name.
   *
   * @param messageName the message name
   * @return the message event subscription entity
   */
  public MessageEventSubscriptionEntity findMessageStartEventSubscriptionByName(String messageName) {
    MessageEventSubscriptionEntity entity = (MessageEventSubscriptionEntity) getDbSqlSession().selectOne("selectMessageStartEventSubscriptionByName", messageName);
    return entity;
  }
   
}
