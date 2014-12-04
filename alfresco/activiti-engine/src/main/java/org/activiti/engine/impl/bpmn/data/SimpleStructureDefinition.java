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

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Represents a simple in memory structure.
 *
 * @author Esteban Robles Luna
 */
public class SimpleStructureDefinition implements FieldBaseStructureDefinition {

  /** The id. */
  protected String id;
  
  /** The field names. */
  protected List<String> fieldNames;

  /** The field types. */
  protected List<Class<?>> fieldTypes;

  /**
   * Instantiates a new simple structure definition.
   *
   * @param id the id
   */
  public SimpleStructureDefinition(String id) {
    this.id = id;
    this.fieldNames = new ArrayList<String>();
    this.fieldTypes = new ArrayList<Class<?>>();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.FieldBaseStructureDefinition#getFieldSize()
   */
  public int getFieldSize() {
    return this.fieldNames.size();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.StructureDefinition#getId()
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * Sets the field name.
   *
   * @param index the index
   * @param fieldName the field name
   * @param type the type
   */
  public void setFieldName(int index, String fieldName, Class<?> type) {
    this.growListToContain(index, this.fieldNames);
    this.growListToContain(index, this.fieldTypes);
    this.fieldNames.set(index, fieldName);
    this.fieldTypes.set(index, type);
  }

  /**
   * Grow list to contain.
   *
   * @param index the index
   * @param list the list
   */
  private void growListToContain(int index, List<?> list) {
    if (!(list.size() - 1 >= index)) {
      for (int i = list.size(); i <= index; i++) {
        list.add(null);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.FieldBaseStructureDefinition#getFieldNameAt(int)
   */
  public String getFieldNameAt(int index) {
    return this.fieldNames.get(index);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.FieldBaseStructureDefinition#getFieldTypeAt(int)
   */
  public Class<?> getFieldTypeAt(int index) {
    return this.fieldTypes.get(index);
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.StructureDefinition#createInstance()
   */
  public StructureInstance createInstance() {
    return new FieldBaseStructureInstance(this);
  }
}
