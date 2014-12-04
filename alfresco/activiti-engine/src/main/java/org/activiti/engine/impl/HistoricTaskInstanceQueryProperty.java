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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.query.QueryProperty;


// TODO: Auto-generated Javadoc
/**
 * The Class HistoricTaskInstanceQueryProperty.
 *
 * @author Tom Baeyens
 */
public class HistoricTaskInstanceQueryProperty implements QueryProperty {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant properties. */
  private static final Map<String, HistoricTaskInstanceQueryProperty> properties = new HashMap<String, HistoricTaskInstanceQueryProperty>();

  /** The Constant HISTORIC_TASK_INSTANCE_ID. */
  public static final HistoricTaskInstanceQueryProperty HISTORIC_TASK_INSTANCE_ID = new HistoricTaskInstanceQueryProperty("ID_");
  
  /** The Constant PROCESS_DEFINITION_ID. */
  public static final HistoricTaskInstanceQueryProperty PROCESS_DEFINITION_ID = new HistoricTaskInstanceQueryProperty("PROC_DEF_ID_");
  
  /** The Constant PROCESS_INSTANCE_ID. */
  public static final HistoricTaskInstanceQueryProperty PROCESS_INSTANCE_ID = new HistoricTaskInstanceQueryProperty("PROC_INST_ID_");
  
  /** The Constant EXECUTION_ID. */
  public static final HistoricTaskInstanceQueryProperty EXECUTION_ID = new HistoricTaskInstanceQueryProperty("EXECUTION_ID_");
  
  /** The Constant TASK_NAME. */
  public static final HistoricTaskInstanceQueryProperty TASK_NAME = new HistoricTaskInstanceQueryProperty("NAME_");
  
  /** The Constant TASK_DESCRIPTION. */
  public static final HistoricTaskInstanceQueryProperty TASK_DESCRIPTION = new HistoricTaskInstanceQueryProperty("DESCRIPTION_");
  
  /** The Constant TASK_ASSIGNEE. */
  public static final HistoricTaskInstanceQueryProperty TASK_ASSIGNEE = new HistoricTaskInstanceQueryProperty("ASSIGNEE_");
  
  /** The Constant TASK_OWNER. */
  public static final HistoricTaskInstanceQueryProperty TASK_OWNER = new HistoricTaskInstanceQueryProperty("OWNER_");
  
  /** The Constant TASK_DEFINITION_KEY. */
  public static final HistoricTaskInstanceQueryProperty TASK_DEFINITION_KEY = new HistoricTaskInstanceQueryProperty("TASK_DEF_KEY_");
  
  /** The Constant DELETE_REASON. */
  public static final HistoricTaskInstanceQueryProperty DELETE_REASON = new HistoricTaskInstanceQueryProperty("DELETE_REASON_");
  
  /** The Constant START. */
  public static final HistoricTaskInstanceQueryProperty START = new HistoricTaskInstanceQueryProperty("HTI.START_TIME_");
  
  /** The Constant END. */
  public static final HistoricTaskInstanceQueryProperty END = new HistoricTaskInstanceQueryProperty("HTI.END_TIME_");
  
  /** The Constant DURATION. */
  public static final HistoricTaskInstanceQueryProperty DURATION = new HistoricTaskInstanceQueryProperty("DURATION_");
  
  /** The Constant TASK_PRIORITY. */
  public static final HistoricTaskInstanceQueryProperty TASK_PRIORITY = new HistoricTaskInstanceQueryProperty("PRIORITY_");

  /** The name. */
  private String name;

  /**
   * Instantiates a new historic task instance query property.
   *
   * @param name the name
   */
  public HistoricTaskInstanceQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.query.QueryProperty#getName()
   */
  public String getName() {
    return name;
  }
  
  /**
   * Find by name.
   *
   * @param propertyName the property name
   * @return the historic task instance query property
   */
  public static HistoricTaskInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }
}
