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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

// TODO: Auto-generated Javadoc
/**
 * Class containing meta-data about Entity-classes.
 * 
 * @author Frederik Heremans
 */
public class EntityMetaData {

  /** The is jpa entity. */
  private boolean isJPAEntity = false;
  
  /** The entity class. */
  private Class< ? > entityClass;
  
  /** The id method. */
  private Method idMethod;
  
  /** The id field. */
  private Field idField;

  /**
   * Checks if is jPA entity.
   *
   * @return true, if is jPA entity
   */
  public boolean isJPAEntity() {
    return isJPAEntity;
  }

  /**
   * Sets the jPA entity.
   *
   * @param isJPAEntity the new jPA entity
   */
  public void setJPAEntity(boolean isJPAEntity) {
    this.isJPAEntity = isJPAEntity;
  }

  /**
   * Gets the entity class.
   *
   * @return the entity class
   */
  public Class< ? > getEntityClass() {
    return entityClass;
  }

  /**
   * Sets the entity class.
   *
   * @param entityClass the new entity class
   */
  public void setEntityClass(Class< ? > entityClass) {
    this.entityClass = entityClass;
  }

  /**
   * Gets the id method.
   *
   * @return the id method
   */
  public Method getIdMethod() {
    return idMethod;
  }

  /**
   * Sets the id method.
   *
   * @param idMethod the new id method
   */
  public void setIdMethod(Method idMethod) {
    this.idMethod = idMethod;
    idMethod.setAccessible(true);
  }

  /**
   * Gets the id field.
   *
   * @return the id field
   */
  public Field getIdField() {
    return idField;
  }

  /**
   * Sets the id field.
   *
   * @param idField the new id field
   */
  public void setIdField(Field idField) {
    this.idField = idField;
    idField.setAccessible(true);
  }

  /**
   * Gets the id type.
   *
   * @return the id type
   */
  public Class<?> getIdType() {
    Class<?> idType = null;
    if(idField != null) {
      idType = idField.getType();
    } else if (idMethod != null) {
      idType = idMethod.getReturnType();
    } 
    return idType;
  }
}
