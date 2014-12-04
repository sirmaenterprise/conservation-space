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
 * An instance of {@link PrimitiveStructureDefinition}.
 *
 * @author Esteban Robles Luna
 */
public class PrimitiveStructureInstance implements StructureInstance {

  /** The primitive. */
  protected Object primitive;

  /** The definition. */
  protected PrimitiveStructureDefinition definition;

  /**
   * Instantiates a new primitive structure instance.
   *
   * @param definition the definition
   */
  public PrimitiveStructureInstance(PrimitiveStructureDefinition definition) {
    this(definition, null);
  }

  /**
   * Instantiates a new primitive structure instance.
   *
   * @param definition the definition
   * @param primitive the primitive
   */
  public PrimitiveStructureInstance(PrimitiveStructureDefinition definition, Object primitive) {
    this.definition = definition;
    this.primitive = primitive;
  }

  /**
   * Gets the primitive.
   *
   * @return the primitive
   */
  public Object getPrimitive() {
    return this.primitive;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.StructureInstance#toArray()
   */
  public Object[] toArray() {
    return new Object[] { this.primitive };
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.StructureInstance#loadFrom(java.lang.Object[])
   */
  public void loadFrom(Object[] array) {
    for (int i = 0; i < array.length; i++) {
      Object object = array[i];
      if (this.definition.getPrimitiveClass().isInstance(object)) {
        this.primitive = object;
        return;
      }
    }
  }
}
