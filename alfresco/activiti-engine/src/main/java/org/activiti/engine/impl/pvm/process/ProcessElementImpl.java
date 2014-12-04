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

package org.activiti.engine.impl.pvm.process;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.pvm.PvmProcessElement;


// TODO: Auto-generated Javadoc
/** common properties for process definition, activity and transition 
 * including event listeners.
 * 
 * @author Tom Baeyens
 */
public class ProcessElementImpl implements PvmProcessElement {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The id. */
  protected String id;
  
  /** The process definition. */
  protected ProcessDefinitionImpl processDefinition;
  
  /** The properties. */
  protected Map<String, Object> properties;
  
  /**
   * Instantiates a new process element impl.
   *
   * @param id the id
   * @param processDefinition the process definition
   */
  public ProcessElementImpl(String id, ProcessDefinitionImpl processDefinition) {
    this.id = id;
    this.processDefinition = processDefinition;
  }

  /**
   * Sets the property.
   *
   * @param name the name
   * @param value the value
   */
  public void setProperty(String name, Object value) {
    if (properties==null) {
      properties = new HashMap<String, Object>();
    }
    properties.put(name, value);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmProcessElement#getProperty(java.lang.String)
   */
  public Object getProperty(String name) {
    if (properties==null) {
      return null;
    }
    return properties.get(name);
  }
  
  /**
   * Gets the properties.
   *
   * @return the properties
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getProperties() {
    if (properties==null) {
      return Collections.EMPTY_MAP;
    }
    return properties;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmProcessElement#getId()
   */
  public String getId() {
    return id;
  }
  
  /**
   * Sets the properties.
   *
   * @param properties the properties
   */
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.pvm.PvmProcessElement#getProcessDefinition()
   */
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }
}
