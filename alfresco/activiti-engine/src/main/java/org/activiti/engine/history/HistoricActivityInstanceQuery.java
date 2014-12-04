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

package org.activiti.engine.history;

import org.activiti.engine.query.Query;


// TODO: Auto-generated Javadoc
/**
 * Programmatic querying for {@link HistoricActivityInstance}s.
 * 
 * @author Tom Baeyens
 */
public interface HistoricActivityInstanceQuery extends Query<HistoricActivityInstanceQuery, HistoricActivityInstance>{

  /**
   * Only select historic activity instances with the given id.
   *
   * @param processInstanceId the process instance id
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery activityInstanceId(String processInstanceId);
  
  /**
   * Only select historic activity instances with the given process instance.
   *
   * @param processInstanceId the process instance id
   * @return the historic activity instance query
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match.
   */
  HistoricActivityInstanceQuery processInstanceId(String processInstanceId);
  
  /**
   * Only select historic activity instances for the given process definition.
   *
   * @param processDefinitionId the process definition id
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery processDefinitionId(String processDefinitionId);

  /**
   * Only select historic activity instances for the given execution.
   *
   * @param executionId the execution id
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery executionId(String executionId);

  /**
   * Only select historic activity instances for the given activity.
   *
   * @param activityId the activity id
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery activityId(String activityId);

  /**
   * Only select historic activity instances for activities with the given name.
   *
   * @param activityName the activity name
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery activityName(String activityName);

  /**
   * Only select historic activity instances for activities with the given activity type.
   *
   * @param activityType the activity type
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery activityType(String activityType);

  /**
   * Only select historic activity instances for userTask activities assigned to the given user.
   *
   * @param userId the user id
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery taskAssignee(String userId);
  
  /**
   * Only select historic activity instances that are finished.
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery finished();

  /**
   * Only select historic activity instances that are not finished yet.
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery unfinished();

  // ordering /////////////////////////////////////////////////////////////////
  /**
   * Order by id (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceId();

  /**
   * Order by processInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByProcessInstanceId();

  /**
   * Order by executionId (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByExecutionId();
  
  /**
   * Order by activityId (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByActivityId();
  
  /**
   * Order by activityName (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByActivityName();
  
  /**
   * Order by activityType (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByActivityType();
  
  /**
   * Order by start (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceStartTime();
  
  /**
   * Order by end (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceEndTime();
  
  /**
   * Order by duration (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByHistoricActivityInstanceDuration();
  
  /**
   * Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}).
   *
   * @return the historic activity instance query
   */
  HistoricActivityInstanceQuery orderByProcessDefinitionId();
}
