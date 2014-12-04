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

package org.activiti.engine.repository;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.query.Query;

// TODO: Auto-generated Javadoc
/**
 * Allows programmatic querying of {@link ProcessDefinition}s.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Daniel Meyer
 * @author Saeid Mirzaei
 */
public interface ProcessDefinitionQuery extends Query<ProcessDefinitionQuery, ProcessDefinition> {
  
  /**
   * Only select process definiton with the given id.
   *
   * @param processDefinitionId the process definition id
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionId(String processDefinitionId);
  
  /**
   * Only select process definitions with the given category.
   *
   * @param processDefinitionCategory the process definition category
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionCategory(String processDefinitionCategory);
  
  /**
   * Only select process definitions where the category matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param processDefinitionCategoryLike the process definition category like
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionCategoryLike(String processDefinitionCategoryLike);

  /**
   * Only select process definitions with the given name.
   *
   * @param processDefinitionName the process definition name
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionName(String processDefinitionName);
  
  /**
   * Only select process definitions where the name matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param processDefinitionNameLike the process definition name like
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionNameLike(String processDefinitionNameLike);

  /**
   * Only select process definitions that are deployed in a deployment with the
   * given deployment id.
   *
   * @param deploymentId the deployment id
   * @return the process definition query
   */
  ProcessDefinitionQuery deploymentId(String deploymentId);

  /**
   * Only select process definition with the given key.
   *
   * @param processDefinitionKey the process definition key
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionKey(String processDefinitionKey);

  /**
   * Only select process definitions where the key matches the given parameter.
   * The syntax that should be used is the same as in SQL, eg. %activiti%
   *
   * @param processDefinitionKeyLike the process definition key like
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionKeyLike(String processDefinitionKeyLike);
  
  /**
   * Only select process definition with a certain version.
   * Particulary useful when used in combination with {@link #processDefinitionKey(String)}
   *
   * @param processDefinitionVersion the process definition version
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionVersion(Integer processDefinitionVersion);
  
  /**
   * Only select the process definitions which are the latest deployed
   * (ie. which have the highest version number for the given key).
   * 
   * Can only be used in combinatioin with {@link #processDefinitionKey(String)} of {@link #processDefinitionKeyLike(String)}.
   * Can also be used without any other criteria (ie. query.latest().list()), which
   * will then give all the latest versions of all the deployed process definitions.
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery latestVersion();
  
  /**
   * Only select process definition with the given resource name.
   *
   * @param resourceName the resource name
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionResourceName(String resourceName);

  /**
   * Only select process definition with a resource name like the given .
   *
   * @param resourceNameLike the resource name like
   * @return the process definition query
   */
  ProcessDefinitionQuery processDefinitionResourceNameLike(String resourceNameLike);
  
  /**
   * Only selects process definitions which given userId is authoriezed to start.
   *
   * @param userId the user id
   * @return the process definition query
   */
  ProcessDefinitionQuery startableByUser(String userId);

  /**
   * Only selects process definitions which are suspended.
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery suspended();
  
  /**
   * Only selects process definitions which are active.
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery active();
  
  // Support for event subscriptions /////////////////////////////////////
  
  /**
   * Message event subscription.
   *
   * @param messageName the message name
   * @return the process definition query
   * @see #messageEventSubscriptionName(String)
   */
  @Deprecated
  ProcessDefinitionQuery messageEventSubscription(String messageName);
  
  /**
   * Selects the single process definition which has a start message event
   * with the messageName.
   *
   * @param messageName the message name
   * @return the process definition query
   */
  ProcessDefinitionQuery messageEventSubscriptionName(String messageName);

  // ordering ////////////////////////////////////////////////////////////
  
  /**
   * Order by the category of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery orderByProcessDefinitionCategory();
  
  /**
   * Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery orderByProcessDefinitionKey();

  /**
   * Order by the id of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery orderByProcessDefinitionId();
  
  /**
   * Order by the version of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery orderByProcessDefinitionVersion();
  
  /**
   * Order by the name of the process definitions (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery orderByProcessDefinitionName();
  
  /**
   * Order by deployment id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the process definition query
   */
  ProcessDefinitionQuery orderByDeploymentId();
  
}
