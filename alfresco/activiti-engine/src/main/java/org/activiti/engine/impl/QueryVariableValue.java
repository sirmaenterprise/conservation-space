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

package org.activiti.engine.impl;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.variable.ByteArrayType;
import org.activiti.engine.impl.variable.JPAEntityVariableType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;


// TODO: Auto-generated Javadoc
/**
 * Represents a variable value used in queries.
 * 
 * @author Frederik Heremans
 */
public class QueryVariableValue implements Serializable {
  
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;
  
  /** The name. */
  private String name;
  
  /** The value. */
  private Object value;
  
  /** The operator. */
  private QueryOperator operator;
  
  /** The variable instance entity. */
  private VariableInstanceEntity variableInstanceEntity;
    
  /**
   * Instantiates a new query variable value.
   *
   * @param name the name
   * @param value the value
   * @param operator the operator
   */
  public QueryVariableValue(String name, Object value, QueryOperator operator) {
    this.name = name;
    this.value = value;
    this.operator = operator;
  }
  
  /**
   * Initialize.
   *
   * @param types the types
   */
  public void initialize(VariableTypes types) {
    if(variableInstanceEntity == null) {
      VariableType type = types.findVariableType(value);
      if(type instanceof ByteArrayType) {
        throw new ActivitiException("Variables of type ByteArray cannot be used to query");
      } else if(type instanceof JPAEntityVariableType && operator != QueryOperator.EQUALS) {
        throw new ActivitiException("JPA entity variables can only be used in 'variableValueEquals'");
      } else {
        // Type implementation determines which fields are set on the entity
        variableInstanceEntity = VariableInstanceEntity.create(name, type, value);
      }
    }
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
   * Gets the operator.
   *
   * @return the operator
   */
  public String getOperator() {
    if(operator != null) {
      return operator.toString();      
    }
    return QueryOperator.EQUALS.toString();
  }
  
  /**
   * Gets the text value.
   *
   * @return the text value
   */
  public String getTextValue() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getTextValue();
    }
    return null;
  }
  
  /**
   * Gets the long value.
   *
   * @return the long value
   */
  public Long getLongValue() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getLongValue();
    }
    return null;
  }
  
  /**
   * Gets the double value.
   *
   * @return the double value
   */
  public Double getDoubleValue() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getDoubleValue();
    }
    return null;
  }
  
  /**
   * Gets the text value2.
   *
   * @return the text value2
   */
  public String getTextValue2() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getTextValue2();
    }
    return null;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getType().getTypeName();
    }
    return null;
  }
}