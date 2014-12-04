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

package org.activiti.engine.impl.form;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


// TODO: Auto-generated Javadoc
/**
 * The Class FormPropertyHandler.
 *
 * @author Tom Baeyens
 */
public class FormPropertyHandler {

  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The type. */
  protected AbstractFormType type;
  
  /** The is readable. */
  protected boolean isReadable;
  
  /** The is writable. */
  protected boolean isWritable;
  
  /** The is required. */
  protected boolean isRequired;
  
  /** The variable name. */
  protected String variableName;
  
  /** The variable expression. */
  protected Expression variableExpression;
  
  /** The default expression. */
  protected Expression defaultExpression;
  
  /**
   * Creates the form property.
   *
   * @param execution the execution
   * @return the form property
   */
  public FormProperty createFormProperty(ExecutionEntity execution) {
    FormPropertyImpl formProperty = new FormPropertyImpl(this);
    Object modelValue = null;
    
    if (execution!=null) {
      if (variableName != null || variableExpression == null) {
        final String varName = variableName != null ? variableName : id;
        if (execution.hasVariable(varName)) {
          modelValue = execution.getVariable(varName);
        } else if (defaultExpression != null) {
          modelValue = defaultExpression.getValue(execution);
        }
      } else {
        modelValue = variableExpression.getValue(execution);
      }
    } else {
      // Execution is null, the form-property is used in a start-form. Default value
      // should be available (ACT-1028) even though no execution is available.
      if (defaultExpression != null) {
        modelValue = defaultExpression.getValue(StartFormVariableScope.getSharedInstance());
      }
    }

    if (modelValue instanceof String) {
      formProperty.setValue((String) modelValue);
    } else if (type != null) {
      String formValue = type.convertModelValueToFormValue(modelValue);
      formProperty.setValue(formValue);
    } else if (modelValue != null) {
      formProperty.setValue(modelValue.toString());
    }
    
    return formProperty;
  }

  /**
   * Submit form property.
   *
   * @param execution the execution
   * @param properties the properties
   */
  public void submitFormProperty(ExecutionEntity execution, Map<String, String> properties) {
    if (!isWritable && properties.containsKey(id)) {
      throw new ActivitiException("form property '"+id+"' is not writable");
    }
    
    if (isRequired && !properties.containsKey(id) && defaultExpression == null) {
      throw new ActivitiException("form property '"+id+"' is required");
    }
    
    Object modelValue = null;
    if (properties.containsKey(id)) {
      final String propertyValue = properties.remove(id);
      if (type != null) {
        modelValue = type.convertFormValueToModelValue(propertyValue);
      } else {
        modelValue = propertyValue;
      }
    } else if (defaultExpression != null) {
      final Object expressionValue = defaultExpression.getValue(execution);
      if (type != null && expressionValue != null) {
        modelValue = type.convertFormValueToModelValue(expressionValue.toString());
      } else if (expressionValue != null) {
        modelValue = expressionValue.toString();
      } else if (isRequired) {
        throw new ActivitiException("form property '"+id+"' is required");
      }
    }
    
    if (modelValue != null) {
      if (variableName != null) {
        execution.setVariable(variableName, modelValue);
      } else if (variableExpression != null) {
        variableExpression.setValue(modelValue, execution);
      } else {
        execution.setVariable(id, modelValue);
      }
    }
  }

  // getters and setters //////////////////////////////////////////////////////
  
  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }
  
  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
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
  public AbstractFormType getType() {
    return type;
  }
  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(AbstractFormType type) {
    this.type = type;
  }
  
  /**
   * Checks if is readable.
   *
   * @return true, if is readable
   */
  public boolean isReadable() {
    return isReadable;
  }
  
  /**
   * Sets the readable.
   *
   * @param isReadable the new readable
   */
  public void setReadable(boolean isReadable) {
    this.isReadable = isReadable;
  }
  
  /**
   * Checks if is required.
   *
   * @return true, if is required
   */
  public boolean isRequired() {
    return isRequired;
  }
  
  /**
   * Sets the required.
   *
   * @param isRequired the new required
   */
  public void setRequired(boolean isRequired) {
    this.isRequired = isRequired;
  }
  
  /**
   * Gets the variable name.
   *
   * @return the variable name
   */
  public String getVariableName() {
    return variableName;
  }
  
  /**
   * Sets the variable name.
   *
   * @param variableName the new variable name
   */
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }
  
  /**
   * Gets the variable expression.
   *
   * @return the variable expression
   */
  public Expression getVariableExpression() {
    return variableExpression;
  }
  
  /**
   * Sets the variable expression.
   *
   * @param variableExpression the new variable expression
   */
  public void setVariableExpression(Expression variableExpression) {
    this.variableExpression = variableExpression;
  }
  
  /**
   * Gets the default expression.
   *
   * @return the default expression
   */
  public Expression getDefaultExpression() {
    return defaultExpression;
  }
  
  /**
   * Sets the default expression.
   *
   * @param defaultExpression the new default expression
   */
  public void setDefaultExpression(Expression defaultExpression) {
    this.defaultExpression = defaultExpression;
  }
  
  /**
   * Checks if is writable.
   *
   * @return true, if is writable
   */
  public boolean isWritable() {
    return isWritable;
  }

  /**
   * Sets the writable.
   *
   * @param isWritable the new writable
   */
  public void setWritable(boolean isWritable) {
    this.isWritable = isWritable;
  }
}
