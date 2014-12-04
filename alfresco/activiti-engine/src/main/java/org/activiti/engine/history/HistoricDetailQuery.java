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
 * Programmatic querying for {@link HistoricDetail}s.
 * 
 * @author Tom Baeyens
 */
public interface HistoricDetailQuery extends Query<HistoricDetailQuery, HistoricDetail> {

  /**
   * Only select historic variable updates with the given process instance.
   *
   * @param processInstanceId the process instance id
   * @return the historic detail query
   * {@link ProcessInstance) ids and {@link HistoricProcessInstance} ids match.
   */
  HistoricDetailQuery processInstanceId(String processInstanceId);

  /**
   * Only select historic variable updates associated to the given {@link HistoricActivityInstance activity instance}.
   *
   * @param activityId the activity id
   * @return the historic detail query
   * @deprecated since 5.2, use {@link #activityInstanceId(String)} instead
   */
  HistoricDetailQuery activityId(String activityId);

  /**
   * Only select historic variable updates associated to the given {@link HistoricActivityInstance activity instance}.
   *
   * @param activityInstanceId the activity instance id
   * @return the historic detail query
   */
  HistoricDetailQuery activityInstanceId(String activityInstanceId);

  /**
   * Only select historic variable updates associated to the given {@link HistoricTaskInstance historic task instance}.
   *
   * @param taskId the task id
   * @return the historic detail query
   */
  HistoricDetailQuery taskId(String taskId);

  /**
   * Only select {@link HistoricFormProperty}s.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery formProperties();

  /**
   * Only select {@link HistoricVariableUpdate}s.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery variableUpdates();
  
  /**
   * Exclude all task-related {@link HistoricDetail}s, so only items which have no
   * task-id set will be selected. When used togheter with {@link #taskId(String)}, this
   * call is ignored task details are NOT excluded.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery excludeTaskDetails();

  /**
   * Order by process instance id.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery orderByProcessInstanceId();
  
  /**
   * Order by variable name.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery orderByVariableName();
  
  /**
   * Order by form property id.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery orderByFormPropertyId();
  
  /**
   * Order by variable type.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery orderByVariableType();
  
  /**
   * Order by variable revision.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery orderByVariableRevision();
  
  /**
   * Order by time.
   *
   * @return the historic detail query
   */
  HistoricDetailQuery orderByTime();
}
