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
package org.activiti.engine.runtime;

import java.io.Serializable;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.query.Query;



// TODO: Auto-generated Javadoc
/** Allows programmatic querying of {@link Execution}s.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public interface ExecutionQuery extends Query<ExecutionQuery, Execution>{
  
  /**
   * Only select executions which have the given process definition key. *
   *
   * @param processDefinitionKey the process definition key
   * @return the execution query
   */
  ExecutionQuery processDefinitionKey(String processDefinitionKey);
  
  /**
   * Only select executions which have the given process definition id. *
   *
   * @param processDefinitionId the process definition id
   * @return the execution query
   */
  ExecutionQuery processDefinitionId(String processDefinitionId);
  
  /**
   * Only select executions which have the given process instance id. *
   *
   * @param processInstanceId the process instance id
   * @return the execution query
   */
  ExecutionQuery processInstanceId(String processInstanceId);
  
  /**
   * Only executions with the given business key.
   *
   * @param processInstanceBusinessKey the process instance business key
   * @return the execution query
   */
  ExecutionQuery processInstanceBusinessKey(String processInstanceBusinessKey);

  /**
   * Only select executions with the given id. *
   *
   * @param executionId the execution id
   * @return the execution query
   */
  ExecutionQuery executionId(String executionId);
  
  /**
   * Only select executions which contain an activity with the given id. *
   *
   * @param activityId the activity id
   * @return the execution query
   */
  ExecutionQuery activityId(String activityId);
  
  /**
   * Only select executions which have a local variable with the given value. The type
   * of variable is determined based on the value, using types configured in
   *
   * @param name name of the variable, cannot be null.
   * @param value the value
   * @return the execution query
   * {@link ProcessEngineConfiguration#getVariableTypes()}.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   */
  ExecutionQuery variableValueEquals(String name, Object value);
  
  /**
   * Only select executions which have a local variable with the given name, but
   * with a different value than the passed value.
   * Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name name of the variable, cannot be null.
   * @param value the value
   * @return the execution query
   */
  ExecutionQuery variableValueNotEquals(String name, Object value);
  

  /**
   * Only select executions which have a local variable value greater than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   * @return the execution query
   */
  ExecutionQuery variableValueGreaterThan(String name, Object value);
  
  /**
   * Only select executions which have a local variable value greater than or equal to
   * the passed value. Booleans, Byte-arrays and {@link Serializable} objects (which
   * are not primitive type wrappers) are not supported.
   *
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   * @return the execution query
   */
  ExecutionQuery variableValueGreaterThanOrEqual(String name, Object value);
  
  /**
   * Only select executions which have a local variable value less than the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   * @return the execution query
   */
  ExecutionQuery variableValueLessThan(String name, Object value);
  
  /**
   * Only select executions which have a local variable value less than or equal to the passed value.
   * Booleans, Byte-arrays and {@link Serializable} objects (which are not primitive type wrappers)
   * are not supported.
   *
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null.
   * @return the execution query
   */
  ExecutionQuery variableValueLessThanOrEqual(String name, Object value);
  
  /**
   * Only select executions which have a local variable value like the given value.
   * This be used on string variables only.
   *
   * @param name variable name, cannot be null.
   * @param value variable value, cannot be null. The string can include the
   * wildcard character '%' to express like-strategy:
   * starts with (string%), ends with (%string) or contains (%string%).
   * @return the execution query
   */
  ExecutionQuery variableValueLike(String name, String value);
  
  // event subscriptions //////////////////////////////////////////////////
  
  /**
   * Signal event subscription.
   *
   * @param signalName the signal name
   * @return the execution query
   * @see #signalEventSubscriptionName(String)
   */
  @Deprecated
  ExecutionQuery signalEventSubscription(String signalName);

  /**
   * Only select executions which have a signal event subscription
   * for the given signal name.
   * 
   * (The signalName is specified using the 'name' attribute of the signal element
   * in the BPMN 2.0 XML.)
   *
   * @param signalName the name of the signal the execution has subscribed to
   * @return the execution query
   */
  ExecutionQuery signalEventSubscriptionName(String signalName);
  
  /**
   * Only select executions which have a message event subscription
   * for the given messageName.
   * 
   * (The messageName is specified using the 'name' attribute of the message element
   * in the BPMN 2.0 XML.)
   *
   * @param messageName the name of the message the execution has subscribed to
   * @return the execution query
   */
  ExecutionQuery messageEventSubscriptionName(String messageName);
  
  //ordering //////////////////////////////////////////////////////////////
  
  /**
   * Order by id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the execution query
   */
  ExecutionQuery orderByProcessInstanceId();
  
  /**
   * Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the execution query
   */
  ExecutionQuery orderByProcessDefinitionKey();
  
  /**
   * Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the execution query
   */
  ExecutionQuery orderByProcessDefinitionId();
  
}
