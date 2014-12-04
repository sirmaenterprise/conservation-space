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

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * An instance of {@link FieldBaseStructureDefinition}.
 *
 * @author Esteban Robles Luna
 */
public class FieldBaseStructureInstance implements StructureInstance {

  /** The structure definition. */
  protected FieldBaseStructureDefinition structureDefinition;

  /** The field values. */
  protected Map<String, Object> fieldValues;

  /**
   * Instantiates a new field base structure instance.
   *
   * @param structureDefinition the structure definition
   */
  public FieldBaseStructureInstance(FieldBaseStructureDefinition structureDefinition) {
    this.structureDefinition = structureDefinition;
    this.fieldValues = new HashMap<String, Object>();
  }

  /**
   * Gets the field value.
   *
   * @param fieldName the field name
   * @return the field value
   */
  public Object getFieldValue(String fieldName) {
    return this.fieldValues.get(fieldName);
  }

  /**
   * Sets the field value.
   *
   * @param fieldName the field name
   * @param value the value
   */
  public void setFieldValue(String fieldName, Object value) {
    this.fieldValues.put(fieldName, value);
  }

  /**
   * Gets the field size.
   *
   * @return the field size
   */
  public int getFieldSize() {
    return this.structureDefinition.getFieldSize();
  }

  /**
   * Gets the field name at.
   *
   * @param index the index
   * @return the field name at
   */
  public String getFieldNameAt(int index) {
    return this.structureDefinition.getFieldNameAt(index);
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.StructureInstance#toArray()
   */
  public Object[] toArray() {
    int fieldSize = this.getFieldSize();
    Object[] arguments = new Object[fieldSize];
    for (int i = 0; i < fieldSize; i++) {
      String fieldName = this.getFieldNameAt(i);
      Object argument = this.getFieldValue(fieldName);
      arguments[i] = argument;
    }
    return arguments;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.StructureInstance#loadFrom(java.lang.Object[])
   */
  public void loadFrom(Object[] array) {
    int fieldSize = this.getFieldSize();
    for (int i = 0; i < fieldSize; i++) {
      String fieldName = this.getFieldNameAt(i);
      Object fieldValue = array[i];
      this.setFieldValue(fieldName, fieldValue);
    }
  }
}
