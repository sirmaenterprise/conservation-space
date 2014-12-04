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
 * An instance of {@link ItemDefinition}.
 *
 * @author Esteban Robles Luna
 */
public class ItemInstance {

  /** The item. */
  protected ItemDefinition item;

  /** The structure instance. */
  protected StructureInstance structureInstance;

  /**
   * Instantiates a new item instance.
   *
   * @param item the item
   * @param structureInstance the structure instance
   */
  public ItemInstance(ItemDefinition item, StructureInstance structureInstance) {
    this.item = item;
    this.structureInstance = structureInstance;
  }

  /**
   * Gets the item.
   *
   * @return the item
   */
  public ItemDefinition getItem() {
    return this.item;
  }

  /**
   * Gets the structure instance.
   *
   * @return the structure instance
   */
  public StructureInstance getStructureInstance() {
    return this.structureInstance;
  }
  
  /**
   * Gets the field base structure instance.
   *
   * @return the field base structure instance
   */
  private FieldBaseStructureInstance getFieldBaseStructureInstance() {
    return (FieldBaseStructureInstance) this.structureInstance;
  }
  
  /**
   * Gets the field value.
   *
   * @param fieldName the field name
   * @return the field value
   */
  public Object getFieldValue(String fieldName) {
    return this.getFieldBaseStructureInstance().getFieldValue(fieldName);
  }

  /**
   * Sets the field value.
   *
   * @param fieldName the field name
   * @param value the value
   */
  public void setFieldValue(String fieldName, Object value) {
    this.getFieldBaseStructureInstance().setFieldValue(fieldName, value);
  }
}
