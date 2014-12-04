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

package org.activiti.engine.impl.bpmn.parser;



// TODO: Auto-generated Javadoc
/**
 * Represents a field declaration in object form:
 * 
 * &lt;field name='someField&gt; &lt;string ...
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class FieldDeclaration {
  
  /** The name. */
  protected String name;
  
  /** The type. */
  protected String type;
  
  /** The value. */
  protected Object value;
  
  /**
   * Instantiates a new field declaration.
   *
   * @param name the name
   * @param type the type
   * @param value the value
   */
  public FieldDeclaration(String name, String type, Object value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }
  
  /**
   * Instantiates a new field declaration.
   */
  public FieldDeclaration() {
    
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
  
  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }
  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * Gets the value.
   *
   * @return the value
   */
  public Object getValue() {
    return value;
  }
  
  /**
   * Sets the value.
   *
   * @param value the new value
   */
  public void setValue(Object value) {
    this.value = value;
  }
  
}
 