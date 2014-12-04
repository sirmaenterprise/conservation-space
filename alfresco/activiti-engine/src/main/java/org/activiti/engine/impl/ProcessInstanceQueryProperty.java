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
import org.activiti.engine.runtime.ProcessInstanceQuery;


// TODO: Auto-generated Javadoc
/**
 * Contains the possible properties that can be used in a {@link ProcessInstanceQuery}.
 * 
 * @author Joram Barrez
 */
public class ProcessInstanceQueryProperty implements QueryProperty {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant properties. */
  private static final Map<String, ProcessInstanceQueryProperty> properties = new HashMap<String, ProcessInstanceQueryProperty>();

  /** The Constant PROCESS_INSTANCE_ID. */
  public static final ProcessInstanceQueryProperty PROCESS_INSTANCE_ID = new ProcessInstanceQueryProperty("E.ID_");
  
  /** The Constant PROCESS_DEFINITION_KEY. */
  public static final ProcessInstanceQueryProperty PROCESS_DEFINITION_KEY = new ProcessInstanceQueryProperty("P.KEY_");
  
  /** The Constant PROCESS_DEFINITION_ID. */
  public static final ProcessInstanceQueryProperty PROCESS_DEFINITION_ID = new ProcessInstanceQueryProperty("P.ID_");
  
  /** The name. */
  private String name;

  /**
   * Instantiates a new process instance query property.
   *
   * @param name the name
   */
  public ProcessInstanceQueryProperty(String name) {
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
   * @return the process instance query property
   */
  public static ProcessInstanceQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
