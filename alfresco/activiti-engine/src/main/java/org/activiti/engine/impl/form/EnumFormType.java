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


// TODO: Auto-generated Javadoc
/**
 * The Class EnumFormType.
 *
 * @author Tom Baeyens
 */
public class EnumFormType extends AbstractFormType {

  /** The values. */
  protected Map<String, String> values;

  /**
   * Instantiates a new enum form type.
   *
   * @param values the values
   */
  public EnumFormType(Map<String, String> values) {
    this.values = values;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.form.FormType#getName()
   */
  public String getName() {
    return "enum";
  }
  
  /* (non-Javadoc)
   * @see org.activiti.engine.impl.form.AbstractFormType#getInformation(java.lang.String)
   */
  @Override
  public Object getInformation(String key) {
    if (key.equals("values")) {
      return values;
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.form.AbstractFormType#convertFormValueToModelValue(java.lang.String)
   */
  @Override
  public Object convertFormValueToModelValue(String propertyValue) {
    validateValue(propertyValue);
    return propertyValue;
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.form.AbstractFormType#convertModelValueToFormValue(java.lang.Object)
   */
  @Override
  public String convertModelValueToFormValue(Object modelValue) {
    if(modelValue != null) {
      if(!(modelValue instanceof String)) {
        throw new ActivitiException("Model value should be a String");
      }
      validateValue((String) modelValue);
    }
    return null;
  }
  
  /**
   * Validate value.
   *
   * @param value the value
   */
  protected void validateValue(String value) {
    if(value != null) {
      if(values != null && !values.containsKey(value)) {
        throw new ActivitiException("Invalid value for enum form property: " + value);
      }
    }
  }

}
