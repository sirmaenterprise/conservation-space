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

package org.activiti.engine.impl.variable;

// TODO: Auto-generated Javadoc
/**
 * Interface describing a container for all available {@link VariableType}s of variables.
 * @author dsyer
 * @author Frederik Heremans
 */
public interface VariableTypes {

  /**
   * Gets the variable type.
   *
   * @param typeName the type name
   * @return the type for the given type name.
   * Returns null if no type was found with the name.
   */
  public VariableType getVariableType(String typeName);

  /**
   * Find variable type.
   *
   * @param value the value
   * @return the variable type to be used to store the given value as a variable.
   */
  public VariableType findVariableType(Object value);
  
  /**
   * Adds the type.
   *
   * @param type the type
   * @return the variable types
   */
  public VariableTypes addType(VariableType type);

  /**
   * Add type at the given index. The index is used when finding a type for an object. When
   * different types can store a specific object value, the one with the smallest
   * index will be used.
   *
   * @param type the type
   * @param index the index
   * @return the variable types
   */
  public VariableTypes addType(VariableType type, int index);
  
  /**
   * Gets the type index.
   *
   * @param type the type
   * @return the type index
   */
  public int getTypeIndex(VariableType type);
  
  /**
   * Gets the type index.
   *
   * @param typeName the type name
   * @return the type index
   */
  public int getTypeIndex(String typeName);
  
  /**
   * Removes the type.
   *
   * @param type the type
   * @return the variable types
   */
  public VariableTypes removeType(VariableType type);
}