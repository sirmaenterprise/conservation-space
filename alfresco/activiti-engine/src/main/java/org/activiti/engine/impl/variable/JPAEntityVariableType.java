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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;


// TODO: Auto-generated Javadoc
/**
 * Variable type capable of storing reference to JPA-entities. Only JPA-Entities which
 * are configured by annotations are supported. Use of compound primary keys is not supported.
 * 
 * @author Frederik Heremans
 */
public class JPAEntityVariableType implements VariableType {

  /** The Constant TYPE_NAME. */
  public static final String TYPE_NAME = "jpa-entity";
  
  /** The mappings. */
  private JPAEntityMappings mappings;
  
  /**
   * Instantiates a new jPA entity variable type.
   */
  public JPAEntityVariableType() {
    mappings = new JPAEntityMappings();
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#getTypeName()
   */
  public String getTypeName() {
    return TYPE_NAME;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#isCachable()
   */
  public boolean isCachable() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#isAbleToStore(java.lang.Object)
   */
  public boolean isAbleToStore(Object value) {
    if(value == null) {
      return true;
    }
    return mappings.isJPAEntity(value);      
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#setValue(java.lang.Object, org.activiti.engine.impl.variable.ValueFields)
   */
  public void setValue(Object value, ValueFields valueFields) {
    EntityManagerSession entityManagerSession = Context 
      .getCommandContext()
      .getSession(EntityManagerSession.class);
    if (entityManagerSession == null) {
      throw new ActivitiException("Cannot set JPA variable: " + EntityManagerSession.class + " not configured");
    } else {
      // Before we set the value we must flush all pending changes from the entitymanager
      // If we don't do this, in some cases the primary key will not yet be set in the object
      // which will cause exceptions down the road.
      entityManagerSession.flush();
    }
    
    if(value != null) {
      String className = mappings.getJPAClassString(value);
      String idString = mappings.getJPAIdString(value);
      valueFields.setTextValue(className);
      valueFields.setTextValue2(idString);      
    } else {
      valueFields.setTextValue(null);
      valueFields.setTextValue2(null);            
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.variable.VariableType#getValue(org.activiti.engine.impl.variable.ValueFields)
   */
  public Object getValue(ValueFields valueFields) {
    if(valueFields.getTextValue() != null && valueFields.getTextValue2() != null) {
      return mappings.getJPAEntity(valueFields.getTextValue(), valueFields.getTextValue2());      
    }
    return null;
  }

 
}
