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
 * Implementation of the BPMN 2.0 'itemDefinition'
 * 
 * @author Esteban Robles Luna
 */
public class ItemDefinition {

  /** The id. */
  protected String id;
  
  /** The structure. */
  protected StructureDefinition structure;
  
  /** The is collection. */
  protected boolean isCollection;
  
  /** The item kind. */
  protected ItemKind itemKind;
  
  /**
   * Instantiates a new item definition.
   */
  private ItemDefinition() {
    this.isCollection = false;
    this.itemKind = ItemKind.Information;
  }
  
  /**
   * Instantiates a new item definition.
   *
   * @param id the id
   * @param structure the structure
   */
  public ItemDefinition(String id, StructureDefinition structure) {
    this();
    this.id = id;
    this.structure = structure;
  }
  
  /**
   * Creates the instance.
   *
   * @return the item instance
   */
  public ItemInstance createInstance() {
    return new ItemInstance(this, this.structure.createInstance());
  }
  
  /**
   * Gets the structure definition.
   *
   * @return the structure definition
   */
  public StructureDefinition getStructureDefinition() {
    return this.structure;
  }

  /**
   * Checks if is collection.
   *
   * @return true, if is collection
   */
  public boolean isCollection() {
    return isCollection;
  }

  /**
   * Sets the collection.
   *
   * @param isCollection the new collection
   */
  public void setCollection(boolean isCollection) {
    this.isCollection = isCollection;
  }

  /**
   * Gets the item kind.
   *
   * @return the item kind
   */
  public ItemKind getItemKind() {
    return itemKind;
  }
  
  /**
   * Sets the item kind.
   *
   * @param itemKind the new item kind
   */
  public void setItemKind(ItemKind itemKind) {
    this.itemKind = itemKind;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return this.id;
  }
}
