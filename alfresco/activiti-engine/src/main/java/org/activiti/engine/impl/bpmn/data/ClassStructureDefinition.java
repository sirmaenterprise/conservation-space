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
 * Represents a structure encapsulated in a class.
 *
 * @author Esteban Robles Luna
 */
public class ClassStructureDefinition implements FieldBaseStructureDefinition {

  /** The id. */
  protected String id;

  /** The class structure. */
  protected Class< ? > classStructure;

  /**
   * Instantiates a new class structure definition.
   *
   * @param classStructure the class structure
   */
  public ClassStructureDefinition(Class<?> classStructure) {
    this(classStructure.getName(), classStructure);
  }
  
  /**
   * Instantiates a new class structure definition.
   *
   * @param id the id
   * @param classStructure the class structure
   */
  public ClassStructureDefinition(String id, Class<?> classStructure) {
    this.id = id;
    this.classStructure = classStructure;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.StructureDefinition#getId()
   */
  public String getId() {
    return this.id;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.FieldBaseStructureDefinition#getFieldSize()
   */
  public int getFieldSize() {
    // TODO
    return 0;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.FieldBaseStructureDefinition#getFieldNameAt(int)
   */
  public String getFieldNameAt(int index) {
    // TODO
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.FieldBaseStructureDefinition#getFieldTypeAt(int)
   */
  public Class< ? > getFieldTypeAt(int index) {
    // TODO
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.bpmn.data.StructureDefinition#createInstance()
   */
  public StructureInstance createInstance() {
    return new FieldBaseStructureInstance(this);
  }
}
