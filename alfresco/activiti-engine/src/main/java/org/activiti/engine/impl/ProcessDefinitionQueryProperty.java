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
import org.activiti.engine.repository.ProcessDefinitionQuery;


// TODO: Auto-generated Javadoc
/**
 * Contains the possible properties that can be used in a {@link ProcessDefinitionQuery}.
 * 
 * @author Joram Barrez
 */
public class ProcessDefinitionQueryProperty implements QueryProperty {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant properties. */
  private static final Map<String, ProcessDefinitionQueryProperty> properties = new HashMap<String, ProcessDefinitionQueryProperty>();
  
  /** The Constant PROCESS_DEFINITION_KEY. */
  public static final ProcessDefinitionQueryProperty PROCESS_DEFINITION_KEY = new ProcessDefinitionQueryProperty("PD.KEY_");
  
  /** The Constant PROCESS_DEFINITION_CATEGORY. */
  public static final ProcessDefinitionQueryProperty PROCESS_DEFINITION_CATEGORY = new ProcessDefinitionQueryProperty("PD.CATEGORY_");
  
  /** The Constant PROCESS_DEFINITION_ID. */
  public static final ProcessDefinitionQueryProperty PROCESS_DEFINITION_ID = new ProcessDefinitionQueryProperty("PD.ID_");
  
  /** The Constant PROCESS_DEFINITION_VERSION. */
  public static final ProcessDefinitionQueryProperty PROCESS_DEFINITION_VERSION = new ProcessDefinitionQueryProperty("PD.VERSION_");
  
  /** The Constant PROCESS_DEFINITION_NAME. */
  public static final ProcessDefinitionQueryProperty PROCESS_DEFINITION_NAME = new ProcessDefinitionQueryProperty("PD.NAME_");
  
  /** The Constant DEPLOYMENT_ID. */
  public static final ProcessDefinitionQueryProperty DEPLOYMENT_ID = new ProcessDefinitionQueryProperty("PD.DEPLOYMENT_ID_");

  /** The name. */
  private String name;

  /**
   * Instantiates a new process definition query property.
   *
   * @param name the name
   */
  public ProcessDefinitionQueryProperty(String name) {
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
   * @return the process definition query property
   */
  public static ProcessDefinitionQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
