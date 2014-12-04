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
 * Custom object type.
 *
 * @author Esteban Robles Luna
 */
public class CustomObjectType implements VariableType {
  
  /** The type name. */
  protected String typeName;
  
  /** The class. */
  protected Class<?> theClass;

  /**
   * Instantiates a new custom object type.
   *
   * @param typeName the type name
   * @param theClass the the class
   */
  public CustomObjectType(String typeName, Class<?> theClass) {
    this.theClass = theClass;
    this.typeName = typeName;
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#getTypeName()
   */
  public String getTypeName() {
    return this.typeName;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#getValue(org.activiti.engine.impl.variable.ValueFields)
   */
  public Object getValue(ValueFields valueFields) {
    return valueFields.getCachedValue();
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#isAbleToStore(java.lang.Object)
   */
  public boolean isAbleToStore(Object value) {
    if (value==null) {
      return true;
    }
    return this.theClass.isAssignableFrom(value.getClass());
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#isCachable()
   */
  public boolean isCachable() {
    return true;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#setValue(java.lang.Object, org.activiti.engine.impl.variable.ValueFields)
   */
  public void setValue(Object value, ValueFields valueFields) {
    valueFields.setCachedValue(value);
  }
}
