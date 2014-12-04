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
package org.activiti.engine.impl.bpmn.webservice;

import org.activiti.engine.impl.bpmn.data.ItemDefinition;
import org.activiti.engine.impl.bpmn.data.StructureDefinition;

// TODO: Auto-generated Javadoc
/**
 * Implementation of the BPMN 2.0 'message'
 * 
 * @author Esteban Robles Luna
 */
public class MessageDefinition {

  /** The id. */
  protected String id;
  
  /** The item definition. */
  protected ItemDefinition itemDefinition;

  /** The name. */
  protected String name;
  
  /**
   * Instantiates a new message definition.
   *
   * @param id the id
   * @param name the name
   */
  public MessageDefinition(String id, String name) {
    this.id = id;
    this.name = name;
  }
  
  /**
   * Creates the instance.
   *
   * @return the message instance
   */
  public MessageInstance createInstance() {
    return new MessageInstance(this, this.itemDefinition.createInstance());
  }
  
  /**
   * Gets the item definition.
   *
   * @return the item definition
   */
  public ItemDefinition getItemDefinition() {
    return this.itemDefinition;
  }

  /**
   * Gets the structure definition.
   *
   * @return the structure definition
   */
  public StructureDefinition getStructureDefinition() {
    return this.itemDefinition.getStructureDefinition();
  }
  
  /**
   * Sets the item definition.
   *
   * @param itemDefinition the new item definition
   */
  public void setItemDefinition(ItemDefinition itemDefinition) {
    this.itemDefinition = itemDefinition;
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
    return name;
  }
  
  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }
}
