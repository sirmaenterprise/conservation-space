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

import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.query.QueryProperty;


// TODO: Auto-generated Javadoc
/**
 * Contains the possible properties which can be used in a {@link HistoricActivityInstanceQuery}.
 * 
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceQueryProperty implements QueryProperty {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant properties. */
  private static final Map<String, HistoricActivityInstanceQueryProperty> properties = new HashMap<String, HistoricActivityInstanceQueryProperty>();

  /** The Constant HISTORIC_ACTIVITY_INSTANCE_ID. */
  public static final HistoricActivityInstanceQueryProperty HISTORIC_ACTIVITY_INSTANCE_ID = new HistoricActivityInstanceQueryProperty("ID_");
  
  /** The Constant PROCESS_INSTANCE_ID. */
  public static final HistoricActivityInstanceQueryProperty PROCESS_INSTANCE_ID = new HistoricActivityInstanceQueryProperty("PROC_INST_ID_");
  
  /** The Constant EXECUTION_ID. */
  public static final HistoricActivityInstanceQueryProperty EXECUTION_ID = new HistoricActivityInstanceQueryProperty("EXECUTION_ID_");
  
  /** The Constant ACTIVITY_ID. */
  public static final HistoricActivityInstanceQueryProperty ACTIVITY_ID = new HistoricActivityInstanceQueryProperty("ACT_ID_");
  
  /** The Constant ACTIVITY_NAME. */
  public static final HistoricActivityInstanceQueryProperty ACTIVITY_NAME = new HistoricActivityInstanceQueryProperty("ACT_NAME_");
  
  /** The Constant ACTIVITY_TYPE. */
  public static final HistoricActivityInstanceQueryProperty ACTIVITY_TYPE = new HistoricActivityInstanceQueryProperty("ACT_TYPE_");
  
  /** The Constant PROCESS_DEFINITION_ID. */
  public static final HistoricActivityInstanceQueryProperty PROCESS_DEFINITION_ID = new HistoricActivityInstanceQueryProperty("PROC_DEF_ID_");
  
  /** The Constant START. */
  public static final HistoricActivityInstanceQueryProperty START = new HistoricActivityInstanceQueryProperty("START_TIME_");
  
  /** The Constant END. */
  public static final HistoricActivityInstanceQueryProperty END = new HistoricActivityInstanceQueryProperty("END_TIME_");
  
  /** The Constant DURATION. */
  public static final HistoricActivityInstanceQueryProperty DURATION = new HistoricActivityInstanceQueryProperty("DURATION_");

  /** The name. */
  private String name;

  /**
   * Instantiates a new historic activity instance query property.
   *
   * @param name the name
   */
  public HistoricActivityInstanceQueryProperty(String name) {
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
   * @return the historic activity instance query property
   */
  public static HistoricActivityInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }
}
