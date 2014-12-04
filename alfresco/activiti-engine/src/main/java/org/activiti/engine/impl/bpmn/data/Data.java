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
package org.activiti.engine.impl.bpmn.data;


// TODO: Auto-generated Javadoc
/**
 * Implementation of the BPMN 2.0 'dataInput' and 'dataOutput'
 * 
 * @author Esteban Robles Luna
 */
public class Data {

  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The definition. */
  protected ItemDefinition definition;
  
  /**
   * Instantiates a new data.
   *
   * @param id the id
   * @param name the name
   * @param definition the definition
   */
  public Data(String id, String name, ItemDefinition definition) {
    this.id = id;
    this.name = name;
    this.definition = definition;
  }
  
  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Gets the definition.
   *
   * @return the definition
   */
  public ItemDefinition getDefinition() {
    return this.definition;
  }
}
